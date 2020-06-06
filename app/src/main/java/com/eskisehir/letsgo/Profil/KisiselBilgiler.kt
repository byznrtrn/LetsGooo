package com.eskisehir.letsgo.Profil

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import com.eskisehir.letsgo.Model.Users
import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.eskisehir.letsgo.utils.UniversalImageLoader
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_kisisel_bilgiler.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

//user kişisel bilgileri burada düzenlenir
class KisiselBilgiler : AppCompatActivity() {

    lateinit var okunanKullaniciBilgileri: Users
    lateinit var mRef : DatabaseReference
    var ppUrl = ""
    var ppUri : Uri? = null
    val RESIM_SEC = 100
    lateinit var mStorageRef : StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kisisel_bilgiler)

        mRef = FirebaseDatabase.getInstance().reference
        mStorageRef = FirebaseStorage.getInstance().reference


        //resim seçmek için dosya izni var mı diye kontrol edilir
        tvResimSec.setOnClickListener {
            permissionCheck()
        }

        //cinsiyet seçmek için bir popup açılır.
        editCinsiyet.setOnClickListener {

            val popup : PopupMenu = PopupMenu(this,editCinsiyet)
            popup.inflate(R.menu.gender_menu)
            val et = editCinsiyet

            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {
                    when(item!!.itemId){
                        R.id.genderErkek ->{
                            et.setText(R.string.erkek)
                            return true
                        }

                        R.id.genderBilinmiyor-> {
                            et.setText(R.string.bilinmiyor)
                            return true
                        }

                        R.id.genderKadin-> {
                            et.setText(R.string.kadin)
                            return true
                        }
                    }
                    return false
                }

            })

            popup.show()

        }


        //kaydetme sırasında hataları engellemek için boş yerin olmaması için parametreler tutulmuştur
        editButtonKaydet.setOnClickListener {

            var degisiklikSayisi = 0
            var kayitSayisi = 0
            //her bir kısım için boşluk konrolü yapılır
            if (!okunanKullaniciBilgileri.cinsiyet.equals(editCinsiyet.text.toString())){
                degisiklikSayisi++
            }
            if (!okunanKullaniciBilgileri.adi_soyadi.equals(editAdSoyad.text.toString())){
                degisiklikSayisi++
            }
            if (!okunanKullaniciBilgileri.dogumYili!!.equals(editDogumYili.text.toString())){
                degisiklikSayisi++
            }

            if (!okunanKullaniciBilgileri.email.equals(editEmail.text.toString())){
                degisiklikSayisi++
            }

            if (!okunanKullaniciBilgileri.phone_number.equals(editTelefon.text.toString())){
                degisiklikSayisi++
            }

            if (!okunanKullaniciBilgileri.ozgecmis.equals(editOzgecmis.text.toString())){
                degisiklikSayisi++
            }

            if (!okunanKullaniciBilgileri.profile_picture.equals(ppUrl)){
                degisiklikSayisi++
            }





            //değiştirilen her kısım için kayıt işlemi yapılır ve kayıt sayısına göre sayfadan çıkma kontol edilir
            //eğer kayıt sayısı ile değişiklik sayısı eşitse çıkış yapılır. son kayıt yapılınca eşitlenmiş olur
            if (!okunanKullaniciBilgileri.cinsiyet.equals(editCinsiyet.text.toString())){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("cinsiyet").setValue(editCinsiyet.text.toString())
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }
            if (!okunanKullaniciBilgileri.adi_soyadi.equals(editAdSoyad.text.toString())){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("adi_soyadi").setValue(editAdSoyad.text.toString())
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }
            if (!okunanKullaniciBilgileri.dogumYili!!.equals(editDogumYili.text.toString())){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("dogumYili").setValue(editDogumYili.text.toString())
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }

            if (!okunanKullaniciBilgileri.email.equals(editEmail.text.toString())){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("email").setValue(editEmail.text.toString())
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }

            if (!okunanKullaniciBilgileri.phone_number.equals(editTelefon.text.toString())){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("phone_number").setValue(editTelefon.text.toString())
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }

            if (!okunanKullaniciBilgileri.ozgecmis.equals(editOzgecmis.text.toString())){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("ozgecmis").setValue(editOzgecmis.text.toString())
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }

            if (!okunanKullaniciBilgileri.profile_picture.equals(ppUrl)){
                mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("profile_picture").setValue(ppUrl)
                    .addOnCompleteListener { p0->
                        if (p0.isSuccessful){
                            kayitSayisi++
                            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                        }
                    }
            }


            degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)

        }

    }

    //bu sayılar bir birine eşitse çıkış yapılır böylece tüm veriler düzgün kaydedilmiş olur
    fun degisikliktenSonraCik(degisiklik:Int,kayit:Int){
        println(""+degisiklik+"  --  "+kayit)
        if (degisiklik == kayit){
            onBackPressed()
        }
    }

    //dosyadan fotoğraf çekme fonksiyonu
    fun pickPP(){
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }


    //dosya okuma izni isteme kısmı
    private fun permissionCheck() {
        Dexter.withActivity(this)
            .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                    if (p0!!.areAllPermissionsGranted()){
                        pickPP()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: MutableList<PermissionRequest>?,
                    p1: PermissionToken?
                ) {
                    println("Bir izin verilmedi")
                    }

            }).check()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESIM_SEC && resultCode == RESULT_OK && data!!.data != null){
            ppUri = data.data

            var dialogYukleniyor = YukleniyorFragment()
            dialogYukleniyor.show(supportFragmentManager,"YukleniyorFragmenti")


            val ref = mStorageRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("userProfilPic")
            val uploadTask = ref.putFile(ppUri!!)

            val urlTask = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    dialogYukleniyor.dismiss()
                    Toast.makeText(this,"Fotoğraf günecllendi", Toast.LENGTH_SHORT).show()
                    val downloadUri = task.result
                    mRef.child("users").child(okunanKullaniciBilgileri.user_id!!).child("user_detail").child("profile_picture").setValue(downloadUri.toString())
                        .addOnCompleteListener { p0->
                            if (p0.isSuccessful){
                                ppUrl = downloadUri.toString()
                                UniversalImageLoader.setImage(ppUrl,profileUserPic!!,null,"")
                            }
                        }
                } else {
                    // Handle failures
                    // ...
                }
            }

        }
    }


    //kullanıcının verileri gelen bilgilere göre ilgili yerlere yerleştirilir
    private fun setupKullaniciBilgileri() {
        ppUrl = okunanKullaniciBilgileri.profile_picture!!
        UniversalImageLoader.setImage(ppUrl,profileUserPic!!,null,"")
        editCinsiyet.setText(okunanKullaniciBilgileri.cinsiyet)
        editAdSoyad.setText(okunanKullaniciBilgileri.adi_soyadi)
        if (okunanKullaniciBilgileri.dogumYili.equals("")){
            editDogumYili.setHint("Doğum Tarihinizi Giriniz")
        }else{
            editDogumYili.setText(okunanKullaniciBilgileri.dogumYili.toString())
        }
        editEmail.setText(okunanKullaniciBilgileri.email)
        if (okunanKullaniciBilgileri.phone_number.equals("")){
            editTelefon.setHint("Telefon Numaranızı Giriniz")
        }else{
            editTelefon.setText(okunanKullaniciBilgileri.phone_number)
        }
        if (okunanKullaniciBilgileri.ozgecmis.equals("")){
            editOzgecmis.setHint("Lütfen Özgeçmişinizi Yazınız")
        }else{
            editOzgecmis.setText(okunanKullaniciBilgileri.ozgecmis)
        }

    }



//event bus ile bilgiler dinlenir
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onKisiselBilgiler(kullaniciBilgileri: EventbusDataEvents.KullaniciBilgileriniGonder){

        okunanKullaniciBilgileri = kullaniciBilgileri.kullanici!!
        setupKullaniciBilgileri()



    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

}
