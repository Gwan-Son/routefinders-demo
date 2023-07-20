package com.example.project_s

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.util.*

class ModeNewsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar : Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView : NavigationView
    lateinit var news1:TextView
    lateinit var news2:TextView
    lateinit var news3:TextView
    lateinit var news4:TextView
    lateinit var news5:TextView
    lateinit var news6:TextView
    lateinit var newsBtn:Button
    lateinit var headerView:View
    lateinit var text:TextView
    lateinit var menu:ImageView
    private var tts: TextToSpeech? = null
    var newsText:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news)
        toolbar = findViewById(R.id.news_layout_toolbar)
        toolbar.setBackgroundColor(Color.GRAY)
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
        initTextToSpeech()
        var inIntent = intent
        text.text = inIntent.getStringExtra("user")
        showLoadingDialog()
        newsArray.clear()
        //intent 선언
        var newsIntent = Intent(applicationContext,NewsWebView::class.java)
        news1 = findViewById(R.id.news1)
        news2 = findViewById(R.id.news2)
        news3 = findViewById(R.id.news3)
        news4 = findViewById(R.id.news4)
        news5 = findViewById(R.id.news5)
        news6 = findViewById(R.id.news6)
        newsBtn = findViewById(R.id.newsBtn)

        newsBtn.setOnClickListener{
            startActivity(newsIntent)
        }
        news1.setOnClickListener {
            newsText = ""
            newsText += news1.text
            ttsSpeak(newsText as String)
        }
        news2.setOnClickListener {
            newsText = ""
            newsText += news2.text
            ttsSpeak(newsText as String)
        }
        news3.setOnClickListener {
            newsText = ""
            newsText += news3.text
            ttsSpeak(newsText as String)
        }
        news4.setOnClickListener {
            newsText = ""
            newsText += news4.text
            ttsSpeak(newsText as String)
        }
        news5.setOnClickListener {
            newsText = ""
            newsText += news5.text
            ttsSpeak(newsText as String)
        }
        news6.setOnClickListener {
            newsText = ""
            newsText += news6.text
            ttsSpeak(newsText as String)
        }
        newsBtn.setOnClickListener{
            startActivity(newsIntent)
        }
        newsTask("https://finance.naver.com/news/")
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
    fun newsTask(url:String){
        Single.fromCallable{
            try{
                val doc = Jsoup.connect(url).get()
                val elements : Elements = doc.select("div .main_news li")
                run elemLoop@{
                    elements.forEachIndexed { index, element ->
                        var tmpNews = element.select("a").text()
                        newsArray.add(tmpNews)
                        if(index == 6) return@elemLoop
                    }
                }
            }catch (e:Exception){e.printStackTrace()}
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { text ->
                news1.text = newsArray[0]
                news2.text = newsArray[1]
                news3.text = newsArray[2]
                news4.text = newsArray[3]
                news5.text = newsArray[4]
                news6.text = newsArray[5]
            }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){

            R.id.item1 ->{
                var mainIntent = Intent(applicationContext,ModeMainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
            R.id.item2 ->{
                var categoryIntent = Intent(applicationContext,ModeCategory::class.java)
                categoryIntent.putExtra("user",text.text)
                startActivity(categoryIntent)
                finish()
            }
            R.id.item3 ->{
                drawerLayout.closeDrawers()
            }
            R.id.item4 ->{
                var helpDialog = AlertDialog.Builder(this@ModeNewsActivity)
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
                        "현재 주요 뉴스기사의 헤드라인을 노출합니다.")
                helpDialog.setIcon(R.drawable.arirang)
                helpDialog.setPositiveButton("확인",null)
                helpDialog.setCancelable(false)
                helpDialog.show()
                ttsSpeak("홈. 현재의 코스피와 코스닥, 그리고 상위권 인기 종목 10개가 노출됩니다. 상위권 인기 종목 터치(클릭)시 대상의 시세를 확인 가능합니다. 종목별 보기. 원하는 종목을 선택하면 종목별 가격과 등락율, 전일비가 노출됩니다. 원하는 종목 터치(클릭)시 대상의 시세를 확인 가능합니다. 뉴스. 현재 주요 뉴스기사의 헤드라인을 노출합니다.")
            }
        }
        return false
    }
    private fun showLoadingDialog(){
        val dialog = LoadingDialog(this@ModeNewsActivity)
        CoroutineScope(Main).launch {
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }
    }
    private fun initTextToSpeech(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            return
        }
        tts = TextToSpeech(this){
            if(it == TextToSpeech.SUCCESS){
                val result = tts?.setLanguage(Locale.KOREAN)
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                    return@TextToSpeech
                }
            }else{
            }
        }
    }
    private fun ttsSpeak(strTTS:String){
        tts?.speak(strTTS, TextToSpeech.QUEUE_FLUSH,null,null)
    }
}