package com.example.letsgoo.Search

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.eskisehir.letsgo.Add.LocationActivity

import com.eskisehir.letsgo.R
import com.eskisehir.letsgo.utils.EventbusDataEvents
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * Yolculuk aramak için burası kullanılır
 */
class SearchFragment : Fragment(), FragmentManager.OnBackStackChangedListener {

    var city = ""
    var lg = 0.0
    var lt = 0.0
    lateinit var rootView :View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)

        init()

        return rootView
    }

    private fun init() {


        activity!!.supportFragmentManager.addOnBackStackChangedListener(this)


        //harita açılır ve yola çıkılacak lokalizasyon belirlenir
        rootView.searchNerden.setOnClickListener {
            permissionCheck()
        }

        //nereye gideleceği bi popup ile seçilir. Bu sayede yazım hataları önlenir
        rootView.nereyeLayout.setOnClickListener {

            val popup : PopupMenu = PopupMenu(context,rootView.searchNereye)
            popup.inflate(R.menu.city_menu)
            popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener{
                override fun onMenuItemClick(item: MenuItem?): Boolean {

                    rootView.searchNereye.setText(item!!.title)

                    return false
                }

            })

            popup.show()

        }


        rootView.searchNereye.setOnClickListener {
            rootView.nereyeLayout.performClick()
        }


        //date için date picker kullanıldı
        rootView.searchDate.setOnClickListener {

            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(activity, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

                // Display Selected date in textbox
                val m = monthOfYear +1
                rootView.searchDate.setText("$dayOfMonth / $m / $year")
            }, year, month, day)

            dpd.show()


        }


        //uygun veriler girilmişse arama yapılır
        rootView.searchButton.setOnClickListener {

            var bos = 0
            var nerden = ""
            var nereye = ""
            var date = ""

            if (!rootView.searchNerden.text.toString().equals("")){
                nerden = rootView.searchNerden.text.toString()
                bos++
            }

            if (!rootView.searchNereye.text.toString().equals("")){
                nereye = rootView.searchNereye.text.toString()
                bos++
            }

            if (!rootView.searchDate.text.toString().equals("")){
                date = rootView.searchDate.text.toString()
                bos++
            }

            if (bos != 3){
                Toast.makeText(activity,"Lütfen tüm alanları doldurduğunuza emin olun!",Toast.LENGTH_LONG).show()
            }else{

                rootView.rootContainer.visibility = View.GONE
                rootView.searchContainer.visibility = View.VISIBLE
                val transaction = activity!!.supportFragmentManager.beginTransaction()
                transaction.replace(R.id.searchContainer,SearchResultFragment())
                transaction.addToBackStack("sonucEkelendi")
                transaction.commit()

                //arama verileri searchresult fragmentine gönderilir arama işlemi orada yapılacak
                EventBus.getDefault().postSticky(EventbusDataEvents.aramaBilgileriniGonder(nerden,nereye,date,lg,lt))



            }



        }



    }




    private fun permissionCheck() {
        Dexter.withActivity(activity)
                .withPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                        if (p0!!.areAllPermissionsGranted()){
                            val intent = Intent(activity!!, LocationActivity::class.java)
                            startActivity(intent)
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


    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun onMySearchLocation(lokasyonBilgileri: EventbusDataEvents.lokasyonBilgileriniGonder){

        city = lokasyonBilgileri.City!!
        lt = lokasyonBilgileri.lt!!
        lg = lokasyonBilgileri.lg!!

        rootView.searchNerden.setText(city)


    }
    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackStackChanged() {
        if(activity!!.supportFragmentManager.backStackEntryCount == 0) {
            rootView.rootContainer.visibility = View.VISIBLE

            println("sdfgsg")
        }
    }


}
