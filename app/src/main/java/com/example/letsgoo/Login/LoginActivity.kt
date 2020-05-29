package com.example.letsgoo.Login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.eskisehir.letsgo.Home.MainActivity
import com.eskisehir.letsgo.Model.Car
import com.eskisehir.letsgo.Model.Rozetler
import com.eskisehir.letsgo.Model.Tercihler
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.R
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.buttonFacebookLogin
import kotlinx.android.synthetic.main.activity_login.etGirisYontemi
import kotlinx.android.synthetic.main.activity_login.etSifre
import kotlinx.android.synthetic.main.activity_login.google_button
import kotlinx.android.synthetic.main.activity_register.*


//daha önce kaydolmuş birinin google facebookve email ile giriş yapacağı kısım
//facebook ve google girişleri register activityde olanlar aynı kodlar
class LoginActivity : AppCompatActivity() {


    lateinit var mRef : DatabaseReference
    lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    lateinit var mAuth : FirebaseAuth
    val TAG = "fbLogin"
    lateinit var gso: GoogleSignInOptions
    lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1
    var isFb = false
    private lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance()
        mRef = FirebaseDatabase.getInstance().reference
        setupAuthListener()
        init()
    }

    private fun setupAuthListener() {
        mAuthListener = object : FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {

                var user = FirebaseAuth.getInstance().currentUser
                if (user != null){
                    val intent = Intent(this@LoginActivity, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP  or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else{

                }

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


    var watcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {

        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            if (etSifre.text.toString().length >=6 && etGirisYontemi.text.toString().length>=6){
                btnGirisYap.isEnabled = true
                btnGirisYap.setTextColor(ContextCompat.getColor(this@LoginActivity,R.color.beyaz))
                btnGirisYap.setBackgroundResource(R.drawable.button_active)
            }else{
                btnGirisYap.isEnabled = false
                btnGirisYap.setTextColor(ContextCompat.getColor(this@LoginActivity,R.color.sonukmavi))
                btnGirisYap.setBackgroundResource(R.drawable.button_inactive)
            }

        }

    }
    private fun init() {

        etSifre.addTextChangedListener(watcher)
        etGirisYontemi.addTextChangedListener(watcher)

        btnGirisYap.setOnClickListener {

            oturumAcacakKullaniciyiDenetle(etGirisYontemi.text.toString(),etSifre.text.toString())

        }

        buttonFacebookLogin.setOnClickListener {

            var reg = RegisterActivity()
            isFb = true
            fbLogin()

        }


        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        google_button.setOnClickListener {
            isFb = false
            googleLogin()

        }







    }

    private fun googleLogin() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

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

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (isFb){
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data)

        }

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








    private fun oturumAcacakKullaniciyiDenetle(emailPhoneNumberUsername: String, sifre:String) {

        var kullanıcıVar=false

        if (emailPhoneNumberUsername.equals("admin@letsgo.com")){
            oturumAc(emailPhoneNumberUsername,sifre)
        }else{
            mRef.child("users").orderByChild("email").addListenerForSingleValueEvent(object :
                    ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        var okunanKullanici = ds.getValue(Users::class.java)


                        if (okunanKullanici!!.email.equals(emailPhoneNumberUsername)){
                            kullanıcıVar = true
                            oturumAc(emailPhoneNumberUsername,sifre)

                        }else if (okunanKullanici.username.equals(emailPhoneNumberUsername)){
                            kullanıcıVar = true
                            oturumAc(emailPhoneNumberUsername,sifre)

                        }
                    }



                    if (kullanıcıVar == false){
                        Toast.makeText(this@LoginActivity,"Kullanıcı Bulunamadı", Toast.LENGTH_SHORT).show()
                    }

                }
            })
        }


    }


    //email ile giriş yapılacak kısım

    fun oturumAc(okunanKullanici:String,sifre:String){
        var girisYapcakEmail = ""

            girisYapcakEmail = okunanKullanici


        //firebase auth için emil ve şfre ile giriş isteği yapıldı
        mAuth.signInWithEmailAndPassword(girisYapcakEmail,sifre)
            .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                override fun onComplete(p0: Task<AuthResult>) {
                    if (p0.isSuccessful){
                        Toast.makeText(this@LoginActivity,"Giris yapildi: "+mAuth.currentUser!!.uid,
                            Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this@LoginActivity,"Kullanıcı adı veya şifre hatalı", Toast.LENGTH_SHORT).show()
                    }
                }

            })

    }
}
