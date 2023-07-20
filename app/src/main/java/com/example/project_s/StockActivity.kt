package com.example.project_s

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import kotlin.reflect.typeOf

var tmpName = ArrayList<String>()
var tmpPrice = ArrayList<String>()
var tmpprevPrice = ArrayList<String>()
var nameUrlCode = ArrayList<String>()
val target_start = "code="
val tartget_end = "\">"
class StockActivity :AppCompatActivity(){
    lateinit var recyclerView: RecyclerView
    lateinit var searchStock:SearchView
    lateinit var stockAdapter: RecyclerUserAdapter
    lateinit var stockMic: ImageView
    private lateinit var stockSpeechRecognizer: SpeechRecognizer
    private lateinit var stockRecognitionListener: RecognitionListener
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock)
        //로딩
        showLoadingDialog()
        //ArrayList를 초기화를 안해주면 2번째 접속부터 코드 에러남
        tmpName.clear()
        tmpPrice.clear()
        tmpprevPrice.clear()
        nameUrlCode.clear()
        searchStock = findViewById(R.id.search_stock)
        searchStock.setOnQueryTextListener(searchViewTextListener)
        stockMic = findViewById(R.id.stock_mic)
        //음성인식 파트
        var recordIntentStock = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recordIntentStock.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        recordIntentStock.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        setListenerStock()
        stockMic.setOnClickListener {
            stockSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            stockSpeechRecognizer.setRecognitionListener(stockRecognitionListener)
            stockSpeechRecognizer.startListening(recordIntentStock)
        }

        var inIntent = intent
        val url_intent = inIntent.getStringExtra("Url")
        doTask(url_intent.toString())
    }
    var searchViewTextListener: SearchView.OnQueryTextListener =
        object : SearchView.OnQueryTextListener {
            //검색버튼 입력시 호출, 검색버튼이 없으므로 사용하지 않음
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }
            //텍스트 입력/수정시에 호출
            override fun onQueryTextChange(s: String): Boolean {
                stockAdapter.filter.filter(s)
                Log.d("TAG", "SearchVies Text is changed : $s")
                return false
            }
        }
    @SuppressLint("CheckResult")
    fun doTask(url:String){
        var documentTitle : String = ""
        Single.fromCallable{
            try{
                //사이트에 접속해서 HTML문서 가져오기
                val doc = Jsoup.connect(url).get()
                //HTML 파싱해서 데이터 추출하기
                //body div #newarea아래의 특정 태그만 가져오기
                val elements : Elements = doc.select("body div #contentarea")
                //(여러 개의)elements 처리
                run elemLoop@{
                    elements.forEachIndexed { index, element ->
                        //크롤링 해온 주식 split(" ")로 찢으면 인덱스 별로 사용가능 -> 이름은 불가능
                        //number로 떼오면 [5]인덱스부터 쓰면 가능
                        //prevPrice는 [1]부터 써야함
                        //[1]이 전일비 [2]가 등락률
                        //종목명
                        var name = doc.getElementsByClass("name_area")
                        for(i in name.indices){//이름만 다르게 크롤링한 이유 -> 주식 이름에 띄어쓰기가 포함되어 있어 split으로 자르면 인덱스 초과남
                            var tmpInput = name[i].getElementsByTag("a").text()
                            var target_idx = name[i].toString().indexOf(target_start)
                            var tmpCode = name[i].toString().substring(target_idx+5,(name[i].toString().substring(target_idx).indexOf(
                                tartget_end)+target_idx))
                            //tmpname은 0번 인덱스부터 출력
                            tmpName.add(tmpInput)
                            nameUrlCode.add(tmpCode)
                        }
                        //전체 숫자
                        var price = element.select(".number").text()
                        //전일비 + 등락률
                        tmpPrice = price.split(" ") as ArrayList<String>
                        var prevPrice = element.select("span[class^=tah p11]").text()
                        tmpprevPrice = prevPrice.split(" ") as ArrayList<String>
                        //tmpprevPrice[0]은 증권 전일대비 등락률이니까 제외하고 출력 -> 1번 인덱스부터 출력
                    }
                }
                documentTitle = doc.title()
            }catch (e : Exception){e.printStackTrace()}
            return@fromCallable documentTitle
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                //documentTitle 응답 성공 시
                { text ->
                    //어느 사이트에서 따왔는지 제목 표시하는 코드
                    //showData(text.toString())
                    //html에서 추출한 값 출력하는 코드
                    val list = ArrayList<Information>()
                    var idx:Int = 5
                    var prevIdx:Int = 1
                    for(i in tmpName.indices){
                        list.add(Information(tmpName[i], tmpPrice[idx+(i*8)],tmpprevPrice[prevIdx+(i*2)], tmpprevPrice[prevIdx+(i*2)+1],
                            nameUrlCode[i]))
                    }
                    stockAdapter = RecyclerUserAdapter(list)
                    recyclerView = findViewById(R.id.recyclerView)
                    recyclerView.adapter = stockAdapter
                },
                {it.printStackTrace()}
            )
    }
    //뒤로가기 버튼을 눌렀을 시 액티비티 종료
    private var backPressedTime:Long = 0
    override fun onBackPressed() {
        if(System.currentTimeMillis() - backPressedTime < 2000){
            finish()
            return
        }
        Toast.makeText(this,"'뒤로' 버튼을 한번 더 누르시면 이전화면으로 돌아갑니다.",Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }
    private fun showLoadingDialog() {
        val dialog = LoadingDialog(this@StockActivity)
        CoroutineScope(Main).launch {
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }
    }
    private fun setListenerStock() {
        stockRecognitionListener = object: RecognitionListener {
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
                    searchStock.setQuery(matches[i],false)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
            }
            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        }
    }
}