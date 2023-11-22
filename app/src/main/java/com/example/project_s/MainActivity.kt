 package com.example.project_s

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.select.Elements

 var kospiN:String=""
 var kosdaqN:String=""
 var kospiC = ArrayList<String>()
 var kosdaqC = ArrayList<String>()
 val plus = '+'
 val minus = '-'
 var topName = ArrayList<String>()
 var topPrice = ArrayList<String>()
 var topPrevPrice = ArrayList<String>()
 var topUrlCode = ArrayList<String>()
 val mainTartget_end = "\" "
 var isMainChk:Boolean = false
 class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
     lateinit var toolbar : Toolbar
     lateinit var drawerLayout: DrawerLayout
     lateinit var navView : NavigationView
     lateinit var kospiNow:TextView
     lateinit var kospiChange:TextView
     lateinit var kosdaqNow:TextView
     lateinit var kosdaqChange:TextView
     lateinit var headerView:View
     lateinit var text :TextView
     lateinit var menu:ImageView
     lateinit var helpImg:ImageView
     lateinit var helpImg2:ImageView
     lateinit var mainRecyclerView: RecyclerView
     private lateinit var auth : FirebaseAuth
     override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         setContentView(R.layout.main)
         toolbar = findViewById(R.id.main_layout_toolbar)
         setSupportActionBar(toolbar)
         supportActionBar?.setDisplayShowTitleEnabled(false)
         drawerLayout = findViewById(R.id.drawerLayout)
         navView = findViewById(R.id.main_navigationView)
         menu = toolbar.findViewById(R.id.openMenu)
         menu.setOnClickListener {
             drawerLayout.openDrawer(GravityCompat.END)
         }
         navView.setNavigationItemSelectedListener(this)
         headerView = navView.getHeaderView(0)
         text = headerView.findViewById(R.id.email_header)
         showLoadingDialog()
         kospiC.clear()
         kosdaqC.clear()
         //사용자 ID표시
         auth = FirebaseAuth.getInstance()
         text.text = auth.currentUser?.email
         kospiNow = findViewById(R.id.kospiNow)
         kospiChange = findViewById(R.id.kospiChange)
         kosdaqNow = findViewById(R.id.kosdaqNow)
         kosdaqChange = findViewById(R.id.kosdaqChange)
         helpImg = findViewById(R.id.helpImg)
         helpImg.setOnClickListener {
             var helpDialog = AlertDialog.Builder(this@MainActivity)
             helpDialog.setTitle("도움말")
             helpDialog.setMessage("현재 코스피 코스닥 지수입니다.")
             helpDialog.setIcon(R.drawable.arirang)
             helpDialog.setPositiveButton("확인",null)
             helpDialog.setCancelable(false)
             helpDialog.show()
         }
         helpImg2 = findViewById(R.id.helpImg2)
         helpImg2.setOnClickListener {
             var helpDialog = AlertDialog.Builder(this@MainActivity)
             helpDialog.setTitle("도움말")
             helpDialog.setMessage(
                     "상위권 인기 종목 10개가 노출됩니다.\n" +
                     "상위권 인기 종목 터치(클릭)시\n" +
                     "대상 종목의 상세정보를 확인할 수 있습니다.")
             helpDialog.setIcon(R.drawable.arirang)
             helpDialog.setPositiveButton("확인",null)
             helpDialog.setCancelable(false)
             helpDialog.show()
         }
         if(!isMainChk){
             isMainChk = true
             var helpDialog = AlertDialog.Builder(this@MainActivity)
             helpDialog.setTitle("환영합니다!")
             helpDialog.setMessage(
                 "루트파인더즈에 오신 것을 환영합니다.\n" +
                         "루트파인더즈는 현재 코스피/코스닥 지수와\n" +
                         "다양한 종목들의 주식의 현황을\n" +
                         "간편하게 볼 수 있는 어플리케이션입니다.\n" +
                         "자세한 사항은 도움말을 이용해주세요."
             )
             helpDialog.setIcon(R.drawable.routefinders)
             helpDialog.setPositiveButton("확인",null)
             helpDialog.setCancelable(false)
             helpDialog.show()
         }
         kosTask("https://finance.naver.com/sise/")
         topTask("https://finance.naver.com/sise/lastsearch2.naver")
     }
     //뒤로가기 버튼을 눌렀을 시 액티비티 종료
     private var backPressedTime:Long = 0
     override fun onBackPressed() {
         if(drawerLayout.isDrawerOpen(GravityCompat.START)){
             drawerLayout.closeDrawers()
         }
         else{
             if(System.currentTimeMillis() - backPressedTime < 2000){
                 finish()
                 return
             }
             Toast.makeText(this,"'뒤로' 버튼을 한번 더 누르시면 로그인 화면으로 돌아갑니다.",Toast.LENGTH_SHORT).show()
             backPressedTime = System.currentTimeMillis()
         }
     }
     @SuppressLint("CheckResult")
     fun kosTask(url:String){
         var documentTitle : String = ""
         Single.fromCallable{
             try{
                 val doc = Jsoup.connect(url).get()
                 val elements : Elements = doc.select("body div #contentarea")
                 run elemLoop@{
                     elements.forEachIndexed { index, element ->
                         kospiN = doc.getElementById("KOSPI_now")?.text().toString()
                         var tmpkospiC = doc.getElementById("KOSPI_change")?.text()
                         kospiC = tmpkospiC?.split("%") as ArrayList<String>
                         kosdaqN = doc.getElementById("KOSDAQ_now")?.text().toString()
                         var tmpkosdaqC = doc.getElementById("KOSDAQ_change")?.text()
                         kosdaqC = tmpkosdaqC?.split("%") as ArrayList<String>
                     }
                 }
                 documentTitle = doc.title()
             }catch (e:Exception){e.printStackTrace()}
             return@fromCallable documentTitle
         }
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe { text ->
                 kospiNow.text = kospiN
                 kospiChange.text = kospiC[0] + "%"
                 kosdaqNow.text = kosdaqN
                 kosdaqChange.text = kosdaqC[0] + "%"
                 val isPlusExistP = kospiC[0].contains(plus)
                 val isMinusExistP = kospiC[0].contains(minus)
                 val isPlusExistD = kosdaqC[0].contains(plus)
                 val isMinusExistD = kosdaqC[0].contains(minus)
                 if(isPlusExistP) {
                     kospiChange.text = "▲" + kospiChange.text
                     kospiChange.setTextColor(Color.parseColor("#ff7b7b"))
                 }
                 if(isMinusExistP) {
                     kospiChange.text = "▼" + kospiChange.text
                     kospiChange.setTextColor(Color.parseColor("#718ef4"))
                 }
                 if(isPlusExistD) {
                     kosdaqChange.text = "▲" + kosdaqChange.text
                     kosdaqChange.setTextColor(Color.parseColor("#ff7b7b"))
                 }
                 if(isMinusExistD) {
                     kosdaqChange.text = "▼" + kosdaqChange.text
                     kosdaqChange.setTextColor(Color.parseColor("#718ef4"))
                 }
             }
     }
     @SuppressLint("CheckResult")
     fun topTask(url:String){
         var documentTitle : String = ""
         Single.fromCallable{
             try{
                 val doc = Jsoup.connect(url).get()
                 val elements : Elements = doc.select("body div #contentarea")
                 run elemLoop@{
                     elements.forEachIndexed { index, element ->
                         var name = doc.getElementsByClass("tltle")
                         for(i in name.indices){
                             var tmpInput = name[i].getElementsByTag("a").text()
                             var target_idx = name[i].toString().indexOf(target_start)
                             var tmpCode = name[i].toString().substring(target_idx+5,(name[i].toString().substring(target_idx).indexOf(
                                 mainTartget_end)+target_idx))
                             topName.add(tmpInput)
                             topUrlCode.add(tmpCode)
                         }
                         var price = element.select(".number").text()
                         topPrice = price.split(" ") as ArrayList<String>
                         var prevPrice = element.select("span[class^=tah p11]").text()
                         topPrevPrice = prevPrice.split(" ") as ArrayList<String>
                     }
                 }
                 documentTitle = doc.title()
             }catch (e : Exception){e.printStackTrace()}
             return@fromCallable documentTitle
         }
             .subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe(
                 { text ->
                    val topList = ArrayList<Information>()
                     var idx:Int = 1
                     var prevIdx:Int = 1
                     for(i in 0..9){
                         topList.add(Information(topName[i], topPrice[idx+(i*10)], topPrevPrice[prevIdx+(i*2)+1],
                             topPrevPrice[prevIdx+(i*2)], topUrlCode[i]))
                     }
                     var mainAdapter = TopRecyclerItemAdapter(topList)
                     mainRecyclerView = findViewById(R.id.mainRecyclerView)
                     mainRecyclerView.adapter = mainAdapter
                 },
                 {it.printStackTrace()}
             )
     }
     override fun onNavigationItemSelected(item: MenuItem): Boolean {
         when (item.itemId){
             R.id.item1 ->{
                 drawerLayout.closeDrawers()
             }
             R.id.item2 ->{
                 var categoryIntent = Intent(applicationContext,Category::class.java)
                 categoryIntent.putExtra("user",text.text)
                 startActivity(categoryIntent)
                 finish()
             }
             R.id.item3 ->{
                 var newsIntent = Intent(applicationContext,NewsActivity::class.java)
                 newsIntent.putExtra("user",text.text)
                 startActivity(newsIntent)
                 finish()
             }
             R.id.item4 ->{
                 var helpDialog = AlertDialog.Builder(this@MainActivity)
                 helpDialog.setTitle("도움말")
                 helpDialog.setMessage("홈.\n" +
                         "현재의 코스피와 코스닥\n" +
                         "그리고 상위권 인기 종목 10개가 노출됩니다.\n" +
                         "상위권 인기 종목 터치(클릭)시\n" +
                         "대상 종목의 상세정보를 확인할 수 있습니다.\n" +
                         "\n" +
                         "종목별 보기.\n" +
                         "원하는 종목을 선택하면\n" +
                         "종목별 가격과 등락율,전일비가 노출됩니다.\n" +
                         "원하는 종목 터치(클릭)시\n" +
                         "대상 종목의 상세정보를 확인할 수 있습니다.\n" +
                         "\n" +
                         "뉴스.\n" +
                         "현재 주요 뉴스기사의 헤드라인을 노출합니다.\n" +
                         "더 많은 기사의 내용을 확인하고 싶다면\n" +
                         "뉴스 더보기를 터치(클릭)시\n" +
                         "네이버 주식 뉴스를 확인할 수 있습니다.")
                 helpDialog.setIcon(R.drawable.arirang)
                 helpDialog.setPositiveButton("확인",null)
                 helpDialog.setCancelable(false)
                 helpDialog.show()
             }
         }
         return false
     }
     private fun showLoadingDialog(){
         val dialog = LoadingDialog(this@MainActivity)
         CoroutineScope(Dispatchers.Main).launch {
             dialog.show()
             delay(2000)
             dialog.dismiss()
         }
     }
 }