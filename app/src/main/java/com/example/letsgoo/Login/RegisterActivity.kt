package com.example.letsgoo.Login


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.example.letsgoo.Home.MainActivity
import com.example.letsgoo.Model.Car
import com.example.letsgoo.Model.Rozetler
import com.example.letsgoo.Model.Tercihler
import com.example.letsgoo.Model.Users
import com.example.letsgoo.R
import com.example.letsgoo.utils.EventbusDataEvents
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_register.*
import org.greenrobot.eventbus.EventBus


//kayıt ekranı
class RegisterActivity : AppCompatActivity(), FragmentManager.OnBackStackChangedListener {

    lateinit var manager : FragmentManager
    lateinit var mRef : DatabaseReference
    lateinit var mAuth : FirebaseAuth
    lateinit var mAuthListener:FirebaseAuth.AuthStateListener
    private lateinit var callbackManager: CallbackManager
    val TAG = "fbLogin"
    lateinit var gso: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1
    var isFb = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        manager = supportFragmentManager
        manager.addOnBackStackChangedListener(this)
        mRef = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()


        //giriş yapıılan yerler için watcher atandı
        //bu sayede 6 karakterden sonra onay butonu aktifleşecek
        etGirisYontemi.addTextChangedListener(watcher)
        etSifre.addTextChangedListener(watcher)
        setupAuthListener()

        init()
    }


    private fun init() {


        //google ile giriş yapmak için gerekli ilklendirme
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        google_button.setOnClickListener {
            //facebook login false yapıldı
            isFb = false
            googleLogin()

        }



        //email ike kayıt yapılacaksa girilen bilgilerden sonra kayıt fragmentine gönderilir
        btnIleri.setOnClickListener {
            if(isValidEmail(etGirisYontemi.text.toString())){

                //email check
                mRef.child("users").orderByChild("email").equalTo(etGirisYontemi.text.toString())
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                Toast.makeText(this@RegisterActivity,"Bu email ile daha önce kayıt yapıldı",Toast.LENGTH_SHORT).show()
                            }else{
                                loginRoot.visibility = View.GONE
                                loginContainer.visibility = View.VISIBLE
                                val transaction = supportFragmentManager.beginTransaction() //fragmanla ilgili işlemleri yapmaya başlayacağımızı haber veriyoruz begintransaction ile.
                                transaction.replace(R.id.loginContainer,KayitFragment()) //nereye eklicez:loginContainer,hangi fragmana eklemek istiyorsun:kayıt fragment
                                transaction.addToBackStack("EmailGirisYontemiFragmentEklendi")
                                transaction.commit()
                                //fragmente bilgileri gönderildi
                                EventBus.getDefault().postSticky(EventbusDataEvents.KayitBilgileriniGonder(etGirisYontemi.text.toString(),etSifre.text.toString()))
                            }
                        }
                    })
                }else{
                    Toast.makeText(this,"E-mailinizi Kontol ediniz", Toast.LENGTH_SHORT).show()
            }
        }


        //eğer facebook ile giril yapılacaksa fabebook giriş true yapıldı
        buttonFacebookLogin.setOnClickListener {
            isFb = true
            fbLogin()
        }



    }

    //google giriş için intent oluşturuldu ve başlatıldı
    private fun googleLogin() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    //facebook login işlemleri
    fun fbLogin() {
        callbackManager = CallbackManager.Factory.create()

        buttonFacebookLogin.setReadPermissions("email", "public_profile")
        buttonFacebookLogin.registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")


        //facebook bilgileri ile giriş isteği yapıldı
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Giriş başarılı olursa user bilgileri ile kayıt yapılacak
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth.currentUser
                    mRef.child("users").orderByChild("user_id").equalTo(user!!.uid)
                        .addListenerForSingleValueEvent(object  : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()){
                                    //user bilgileri kaydedilecek
                                    val username = user.email!!.split("@")[0]
                                    val adSoyad = user!!.displayName
                                    val userPic = user.photoUrl.toString()
                                    val email = user.email
                                    val userID = user!!.uid
                                    val phone = user.phoneNumber

                                    val rozetler = Rozetler(0,0,0,0,0)
                                    val car = Car("","","","","",0,"")
                                    val tercihler = Tercihler(0,0,0,0)
                                    val kaydedilecekKullanıcıBilgisi = Users(email,username,adSoyad,"Bilinmiyor",phone,userID,userPic,"","",0,rozetler,car,tercihler,1)

                                    mRef.child("users").child(userID).setValue(kaydedilecekKullanıcıBilgisi)
                                        .addOnCompleteListener { p0 ->
                                            if (p0.isSuccessful){
                                                println("başarılı")
                                                Toast.makeText(applicationContext,"Kullanıcı verileri kaydedildi",Toast.LENGTH_SHORT).show()
                                            }else{
                                                println("başarısız++++++++++++++")
                                                mAuth.currentUser!!.delete()
                                                    .addOnCompleteListener { p0 ->
                                                        if (p0.isSuccessful){
                                                            Toast.makeText(applicationContext,"Kullanıcı kaydedilemedi lütfen tekrar deneyiniz",Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            }
                                        }
                                    Toast.makeText(applicationContext,"Oturum Açıldı",Toast.LENGTH_SHORT).show()
                                }

                            }

                        })


                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                }

                // ...
            }
    }

    //facebook ve google girişleri için dönen cevapların değerlendirilip işleme alınması
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (isFb){
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data)

        }

        //google girişi için dönüş geldiyse
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleResult (task)
        }
}

    private fun handleResult (completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account)

        } catch (e: ApiException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    //facebook gibi istek yapıldı ve başarılıysa user bilgileri kaydedildi
    fun firebaseAuthWithGoogle(account : GoogleSignInAccount){

        val credential : AuthCredential = GoogleAuthProvider.getCredential(account.idToken,null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { p0 ->
                if (p0.isSuccessful){
                    Toast.makeText(this,"Google giriş oldu",Toast.LENGTH_SHORT).show()

                    val user = mAuth.currentUser



                    mRef.child("users").orderByChild("user_id").equalTo(user!!.uid)
                        .addListenerForSingleValueEvent(object  : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {
                                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()){
                                    val username = user.email!!.split("@")[0]
                                    val adSoyad = user!!.displayName
                                    val userPic = user.photoUrl.toString()
                                    val email = user.email
                                    val userID = user!!.uid
                                    val phone = user.phoneNumber

                                    val rozetler = Rozetler(0,0,0,0,0)
                                    val car = Car("","","","","",0,"")
                                    val tercihler = Tercihler(0,0,0,0)
                                    val kaydedilecekKullanıcıBilgisi = Users(email,username,adSoyad,"Bilinmiyor",phone,userID,userPic,"","",0,rozetler,car,tercihler,1)

                                    mRef.child("users").child(userID).setValue(kaydedilecekKullanıcıBilgisi)
                                        .addOnCompleteListener { p0 ->
                                            if (p0.isSuccessful){
                                                println("başarılı")
                                                Toast.makeText(applicationContext,"Kullanıcı verileri kaydedildi",Toast.LENGTH_SHORT).show()
                                            }else{
                                                println("başarısız++++++++++++++")
                                                mAuth.currentUser!!.delete()
                                                    .addOnCompleteListener { p0 ->
                                                        if (p0.isSuccessful){
                                                            Toast.makeText(applicationContext,"Kullanıcı kaydedilemedi lütfen tekrar deneyiniz",Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                            }
                                        }
                                    Toast.makeText(applicationContext,"Oturum Açıldı",Toast.LENGTH_SHORT).show()
                                }

                            }

                        })


                }else{
                    Toast.makeText(this," olmadı",Toast.LENGTH_SHORT).show()

                }
            }

    }

    var watcher:TextWatcher = object : TextWatcher{
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s!!.length > 5){

                if (etGirisYontemi.text.toString().length > 5 && etSifre.text.toString().length > 5 ){
                    btnIleri.isEnabled =true
                    btnIleri.setTextColor(ContextCompat.getColor(this@RegisterActivity,R.color.beyaz))
                    btnIleri.setBackgroundResource(R.drawable.button_active)
                }

            }else{
                btnIleri.isEnabled =false
                btnIleri.setTextColor(ContextCompat.getColor(this@RegisterActivity!!,R.color.sonukmavi))
                btnIleri.setBackgroundResource(R.drawable.button_inactive)
            }
        }


    }
//kayıt fragmentinden geri dönüş için
    override fun onBackStackChanged() {

        if(manager.backStackEntryCount == 0){
            loginRoot.visibility = View.VISIBLE
        }
    }

    //daha önce oturum açılmışsa bu ekranda kalmayıp home sayfası açılıyor
    private fun setupAuthListener() {
        mAuthListener = FirebaseAuth.AuthStateListener {
            var user = FirebaseAuth.getInstance().currentUser
            if (user != null){
                val intent = Intent(this@RegisterActivity, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_NO_ANIMATION)
                startActivity(intent)
                finish()
            }else{

            }
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthListener)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener)
        }
    }

    fun isValidEmail(email:String?):Boolean{
        if (email == null){
            return false
        }

        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }


}

