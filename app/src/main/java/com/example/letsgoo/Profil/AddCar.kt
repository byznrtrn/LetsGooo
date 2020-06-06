package com.example.letsgoo.Profil

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.letsgoo.Model.Car
import com.example.letsgoo.Model.Users
import com.example.letsgoo.R
import com.example.letsgoo.utils.EventbusDataEvents
import com.example.letsgoo.utils.UniversalImageLoader
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_car.*
import kotlinx.android.synthetic.main.activity_add_car.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


//araba ekleme sayfası
class AddCar : AppCompatActivity() {

    lateinit var gelenArabaBilgileri :  Car
    lateinit var mRef : DatabaseReference
    var ppUrl = ""
    var ppUri : Uri? = null
    val RESIM_SEC = 100
    lateinit var mStorageRef : StorageReference
    lateinit var mode :String
    lateinit var userId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_car)

        mRef = FirebaseDatabase.getInstance().reference
        mStorageRef = FirebaseStorage.getInstance().reference
        userId = FirebaseAuth.getInstance().currentUser!!.uid

        selectCar.setOnClickListener {
            permissionCheck()
        }

        editButtonKaydet.setOnClickListener {
            if (ppUrl.equals("")){
                Toast.makeText(this,"Lütfen araba fotoğrafı seçiniz.",Toast.LENGTH_LONG).show()
            }else{
                var degisiklikSayisi = 0
                var kayitSayisi = 0
                var bos_sayisi = 0


                if (editMarka.text.toString().equals("")){
                    bos_sayisi++
                }

                if (editModel.text.toString().equals("")){
                    bos_sayisi++
                }

                if (editRenk.text.toString().equals("")){
                    bos_sayisi++
                }

                if (editPlaka.text.toString().equals("")){
                    bos_sayisi++
                }

                if (editYil.text.toString().equals("")){
                    bos_sayisi++
                }

                if (editCarCapacity.text.toString().equals("")){
                    bos_sayisi++
                }


                if (bos_sayisi==0){

                    if (!gelenArabaBilgileri.marka.equals(editMarka.text.toString())){
                        degisiklikSayisi++
                    }

                    if (!gelenArabaBilgileri.model.equals(editModel.text.toString())){
                        degisiklikSayisi++
                    }

                    if (!gelenArabaBilgileri.renk.equals(editRenk.text.toString())){
                        degisiklikSayisi++
                    }

                    if (!gelenArabaBilgileri.yil.toString().equals(editYil.toString())){
                        degisiklikSayisi++
                    }

                    if (!gelenArabaBilgileri.plaka.equals(editPlaka.text.toString())){
                        degisiklikSayisi++
                    }

                    if (!gelenArabaBilgileri.kapasite!!.equals(editCarCapacity.text.toString())){
                        degisiklikSayisi++
                    }

                    if (!ppUrl.equals("")){
                        degisiklikSayisi++
                    }




                    if (!gelenArabaBilgileri.marka.equals(editMarka.text.toString())){
                        mRef.child("users").child(userId).child("car").child("marka").setValue(editMarka.text.toString())
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }

                    if (!gelenArabaBilgileri.model.equals(editModel.text.toString())){
                        mRef.child("users").child(userId).child("car").child("model").setValue(editModel.text.toString())
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }

                    if (!gelenArabaBilgileri.renk.equals(editRenk.text.toString())){
                        mRef.child("users").child(userId).child("car").child("renk").setValue(editRenk.text.toString())
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }

                    if (!gelenArabaBilgileri.yil.toString().equals(editYil.toString())){
                        val year = editYil.text.toString()
                        mRef.child("users").child(userId).child("car").child("yil").setValue(year)
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }

                    if (!gelenArabaBilgileri.plaka.equals(editPlaka.text.toString())){
                        mRef.child("users").child(userId).child("car").child("plaka").setValue(editPlaka.text.toString())
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }

                    if (!gelenArabaBilgileri.kapasite!!.equals(editCarCapacity.text.toString())){
                        mRef.child("users").child(userId).child("car").child("kapasite").setValue(editCarCapacity.text.toString().toInt())
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }

                    if (!ppUrl.equals("")){
                        mRef.child("users").child(userId).child("car").child("pic").setValue(ppUrl)
                                .addOnCompleteListener { p0->
                                    if (p0.isSuccessful){
                                        kayitSayisi++
                                        degisikliktenSonraCik(degisiklikSayisi,kayitSayisi)
                                    }
                                }
                    }
                }else{
                    Toast.makeText(this,"Lütfen boş alanları doldurunuz!",Toast.LENGTH_LONG).show()
                 }



            }

        }
    }

    //bu fonksiyon bi çok defa çağırılır ama geri çıkması için tüm kayıtların bitmiş olması lazım. biten her kayıttan sonra burası çağırılır son kayıttan sonra çıkış yapılır
    fun degisikliktenSonraCik(degisiklik:Int,kayit:Int){
        println("$degisiklik  --  $kayit")
        if (degisiklik == kayit){
            onBackPressed()
        }
    }

    //araba fotosu seçilir
    fun pickPP(){
        val intent = Intent()
        intent.setType("image/*")
        intent.setAction(Intent.ACTION_PICK)
        startActivityForResult(intent,RESIM_SEC)
    }
//dosya okuma izni istenir
    private fun permissionCheck() {
        Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
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


            val ref = mStorageRef.child("users").child(userId).child("car").child("pic")
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
                    Toast.makeText(this,"Fotoğraf Seçildi", Toast.LENGTH_SHORT).show()
                    ppUrl = task.result.toString()
                    UniversalImageLoader.setImage(ppUrl,profileCarPic!!,null,"")
                    /*val downloadUri = task.result
                    mRef.child("users").child(userId).child("car").child("pic").setValue(downloadUri.toString())
                            .addOnCompleteListener { p0->
                                if (p0.isSuccessful){
                                    ppUrl = downloadUri.toString()
                                    UniversalImageLoader.setImage(ppUrl,profileCarPic!!,null,"")
                                }
                            }*/
                } else {
                    // Handle failures
                    // ...
                }
            }

        }
    }

    //araba düzenleme veya neyi araba durumuna göre ilgili kısımlar gelen bilgilere göre doldurulur
    private fun setupKullaniciBilgileri() {

        if (!mode.equals("new")){
            ppUrl = gelenArabaBilgileri.pic!!
            val car = gelenArabaBilgileri
            UniversalImageLoader.setImage(ppUrl,profileCarPic!!,null,"")
            editPlaka.setText(car.plaka)
            editMarka.setText(car.marka)
            editModel.setText(car.model)
            editRenk.setText(car.renk)

            editCarCapacity.setText(car.kapasite!!.toString())
            editYil.setText(car.yil)
        }


    }



    //gelen araba bilgileri burada yakalanır ve düzenleme ise ilgili yerlere burası sayesinde aktarılır

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onArabaBilgileri(arabaBilgileri: EventbusDataEvents.arabaBilgileriniGonder){
        gelenArabaBilgileri = arabaBilgileri.car!!
        mode = arabaBilgileri.mode!!

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
