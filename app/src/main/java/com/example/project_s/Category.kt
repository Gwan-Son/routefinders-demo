package com.example.project_s

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.project_s.databinding.ActivityMainBinding
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
import com.example.project_s.CategoryRecycleItemAdapter as CategoryRecycleItemAdapter
import com.example.project_s.CategoryRecycleItemAdapter as ComExampleProject_sCategoryRecycleItemAdapter

var cateName = ArrayList<String>()
var catePrice = ArrayList<String>()
var catePrevPrice = ArrayList<String>()
var cateUrlCode = ArrayList<String>()
val cateTarget_Start = "no="
val cateTarget_End = "\">"

class Category : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var toolbar : Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView : NavigationView
    lateinit var stockBtn: Button
    lateinit var headerView:View
    lateinit var text:TextView
    lateinit var menu:ImageView
    lateinit var recyclerView:RecyclerView
    lateinit var searchCategory:SearchView
    lateinit var cateAdapter: CategoryRecycleItemAdapter
    lateinit var cateMic:ImageView
    private lateinit var cateSpeechRecognizer: SpeechRecognizer
    private lateinit var cateRecognitionListener: RecognitionListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.category_main)
        toolbar = findViewById(R.id.category_layout_toolbar)
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
        var inIntent = intent
        text.text = inIntent.getStringExtra("user")
        showLoadingDialog()
        cateName.clear()
        catePrice.clear()
        catePrevPrice.clear()
        cateUrlCode.clear()
        searchCategory = findViewById(R.id.search_category)
        searchCategory.setOnQueryTextListener(searchViewTextListener)
        cateMic = findViewById(R.id.cate_mic)
        //음성인식 파트
        var recordIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recordIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        setListener()
        cateMic.setOnClickListener {
            cateSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            cateSpeechRecognizer.setRecognitionListener(cateRecognitionListener)
            cateSpeechRecognizer.startListening(recordIntent)
        }

        var stockUrl:String? = null
        //intent 선언
        var stockIntent = Intent(applicationContext,StockActivity::class.java)
        var spinData = resources.getStringArray(R.array.category)
        var spinAdapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,spinData)
        var spinner: Spinner = findViewById<Spinner>(R.id.spinner)
        spinner.adapter = spinAdapter
        var categoryURL = resources.getStringArray(R.array.categoryURL)
        stockBtn = findViewById(R.id.stockBtn)

        stockBtn.setOnClickListener {//종목별 보기
            if(stockUrl?.isEmpty() == true){
                Toast.makeText(applicationContext,"종목을 선택하여 주세요",Toast.LENGTH_SHORT).show()
            }
            else{
                //증권 url
                stockIntent.putExtra("Url",stockUrl)
                //activity_stock 실행
                startActivity(stockIntent)
            }
        }
        //스피너 동작 리스너
        spinner.setSelection(0)//시작 위치를 지정하는 코드
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if(p2 == 0){
                    stockUrl = ""
                }
                else{
                    stockUrl = categoryURL[p2 - 1]
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
        cateTask("https://finance.naver.com/sise/sise_group.naver?type=upjong")
    }
    var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }
            //텍스트 입력/수정시에 호출
            override fun onQueryTextChange(s: String): Boolean {
                cateAdapter.filter.filter(s)
                return false
            }
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
    fun cateTask(url:String){
        var documentTitle : String = ""
        Single.fromCallable{
            try{
                val doc = Jsoup.connect(url).get()
                val elements : Elements = doc.select("body div #contentarea")
                //(여러 개의)elements 처리
                run elemLoop@{
                    elements.forEachIndexed { index, element ->
                        var name = doc.select("div #contentarea_left tr")
                        for(i in name.indices){//이름만 다르게 크롤링한 이유 -> 주식 이름에 띄어쓰기가 포함되어 있어 split으로 자르면 인덱스 초과남
                            var tmpInput = name[i].getElementsByTag("a").text()
                            if(tmpInput == ""){

                            }
                            else{
                                var target_idx = name[i].toString().indexOf(cateTarget_Start)
                                var tmpCode = name[i].toString().substring(target_idx+3,(name[i].toString().substring(target_idx).indexOf(
                                    cateTarget_End)+target_idx))
                                //tmpname은 0번 인덱스부터 출력
                                cateName.add(tmpInput)
                                cateUrlCode.add(tmpCode)
                            }
                        }
                        var prevPrice = element.select("span[class^=tah p11]").text()
                        catePrevPrice = prevPrice.split(" ") as ArrayList<String>
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
                    val cateList = ArrayList<Information>()
                    for(i in cateName.indices){
                        cateList.add(Information(cateName[i],"","", catePrevPrice[i], cateUrlCode[i]))
                    }
                    cateAdapter = CategoryRecycleItemAdapter(cateList)
                    recyclerView = findViewById(R.id.cate_recyclerView)
                    recyclerView.adapter = cateAdapter
                },
                {it.printStackTrace()}
            )
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.item1 ->{
                var mainIntent = Intent(applicationContext,MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }

            R.id.item2 ->{
                drawerLayout.closeDrawers()
            }

            R.id.item3 ->{
                var newsIntent = Intent(applicationContext,NewsActivity::class.java)
                newsIntent.putExtra("user",text.text)
                startActivity(newsIntent)
                finish()
            }
            R.id.item4 ->{
                var helpDialog = AlertDialog.Builder(this@Category)
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
        val dialog = LoadingDialog(this@Category)
        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }
    }
    private fun setListener() {
        cateRecognitionListener = object: RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(applicationContext, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show()
            }
            override fun onBeginningOfSpeech() {
            }
            override fun onRmsChanged(rmsdB: Float) {
            }
            override fun onBufferReceived(buffer: ByteArray?) {
            }
            override fun onEndOfSpeech() {
            }
            override fun onError(error: Int) {
                var message: String

                when (error) {
                    SpeechRecognizer.ERROR_AUDIO ->
                        message = "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT ->
                        message = "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        message = "퍼미션 없음"
                    SpeechRecognizer.ERROR_NETWORK ->
                        message = "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        message = "네트워크 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        message = "찾을 수 없음"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                        message = "RECOGNIZER가 바쁨"
                    SpeechRecognizer.ERROR_SERVER ->
                        message = "서버가 이상함"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        message = "말하는 시간초과"
                    else ->
                        message = "알 수 없는 오류"
                }
                Toast.makeText(applicationContext, "에러 발생 $message", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                var matches: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

                for (i in 0 until matches.size) {
                    searchCategory.setQuery(matches[i],false)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
            }
            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        }
    }
}