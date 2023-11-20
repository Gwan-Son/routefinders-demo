package com.example.project_s

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class ModePurchaseDialog(context: Context,private val stockName: String,private val stockPrice:String) : Dialog(context){

    private val scaleUpAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.scale_up) }
    private val scaleDownAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.scale_down) }
    private val rotateAnim by lazy { AnimationUtils.loadAnimation(context,R.anim.rotate_anim) }

    private lateinit var dialogToolbar: Toolbar
    private lateinit var stockNameTextView: TextView
    private lateinit var stockPriceTextView: TextView
    private lateinit var stockAmountTextView: TextView
    private lateinit var voiceBackView: io.github.florent37.shapeofview.shapes.CircleView
    private lateinit var dialogSpeechRecognizer: SpeechRecognizer
    private lateinit var dialogRecognitionListener: RecognitionListener
    private var tts: TextToSpeech? = null

    private var itemPrice: Double = 0.0
    private var stockAmount: Int = 1
    private var recognitionText: String = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //백그라운드 투명
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_purchase)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = attributes
            params.windowAnimations = R.style.AnimationPopupStyle
            attributes = params
        }

        dialogToolbar = findViewById(R.id.dialog_toobar)
        stockNameTextView = findViewById(R.id.stockName)
        stockPriceTextView = findViewById(R.id.total_price)
        stockAmountTextView = findViewById(R.id.stock_amount)
        voiceBackView = findViewById(R.id.voiceBackButton)

        dialogToolbar.setBackgroundColor(Color.GRAY)
        //음성인식
        var recordIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recordIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context?.packageName)
        recordIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        setListener()
        findViewById<ImageView>(R.id.voiceRcButton).setOnClickListener {
            ttsSpeak("결제를 원하시면 버튼을 꾹 눌러주세요")
        }

        findViewById<ImageView>(R.id.voiceRcButton).setOnLongClickListener {
            //음성인식 버튼
            voiceBackView.startAnimation(rotateAnim)
            dialogSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this.context)
            dialogSpeechRecognizer.setRecognitionListener(dialogRecognitionListener)
            dialogSpeechRecognizer.startListening(recordIntent)
            true
        }

        initTextToSpeech()

        findViewById<ImageView>(R.id.plus_button).setOnClickListener {
            it.startAnimation(scaleUpAnimation)
            increaseStockAmount()
        }

        findViewById<ImageView>(R.id.minus_button).setOnClickListener {
            it.startAnimation(scaleUpAnimation)
            decreaseStockAmount()
        }

        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            dismiss()
        }

        findViewById<ImageView>(R.id.closeButton).setOnClickListener {
            dismiss()
        }

        setItemInfo(stockName,stockPrice)
    }

    fun setItemInfo(itemName: String, itemPrice: String){
        stockNameTextView.text = itemName
        this.itemPrice = itemPrice.replace(",","").toDouble()
        stockPriceTextView.text = "${String.format("%,.0f",this.itemPrice)} 원"
        stockAmount = 1
        stockAmountTextView.text = stockAmount.toString()
        updateTotalPrice()
    }

    private fun updateTotalPrice() {
        val totalPrice = itemPrice * stockAmount
        stockPriceTextView.startAnimation(scaleDownAnimation)
        stockPriceTextView.text = "${String.format("%,.0f",totalPrice)} 원"
        var stockInfo = stockName + stockAmount.toString() + "주의 가격은 " + stockPriceTextView.text + "입니다."
        ttsSpeak(stockInfo)
    }

    private fun decreaseStockAmount() {
        if(stockAmount > 1){
            stockAmount--
            stockAmountTextView.startAnimation(scaleDownAnimation)
            stockAmountTextView.text = stockAmount.toString()
            updateTotalPrice()
        }
    }

    private fun increaseStockAmount() {
        stockAmount++
        stockAmountTextView.startAnimation(scaleDownAnimation)
        stockAmountTextView.text = stockAmount.toString()
        updateTotalPrice()
    }

    private fun setListener() {
        dialogRecognitionListener = object: RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(context, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show()
                ttsSpeak("음성인식을 시작합니다.")
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
                Toast.makeText(context, "에러 발생 $message", Toast.LENGTH_SHORT).show()
            }

            override fun onResults(results: Bundle?) {
                var matches: ArrayList<String> = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) as ArrayList<String>

                if(!matches.isNullOrEmpty()){
                    recognitionText = matches[0]
                }
                Log.i("텍스트: ",recognitionText)
                if(certificationVoice(recognitionText)){
                    showServerDialog()
                    Toast.makeText(context,"결제완료",Toast.LENGTH_SHORT).show()
                    showConfirmDialog()
                }
                else{
                    showServerDialog()
                    Toast.makeText(context,"실패",Toast.LENGTH_SHORT).show()
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
            }
            override fun onEvent(eventType: Int, params: Bundle?) {
            }
        }
    }
    private fun initTextToSpeech(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            return
        }
        tts = TextToSpeech(context){
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

    private fun certificationVoice(recognitionText: String): Boolean{
        for(i in 0 until recognitionText.length - 1){
            if(recognitionText[i] == '결' && recognitionText[i + 1] == '제'){
                return true
            }
        }
        return false
    }

    private fun showServerDialog(){
        val dialog = ServerDialog(context)
        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            delay(2000)
            dialog.dismiss()
        }
    }

    private fun showConfirmDialog(){
        val dialog = ModeConfirmDialog(context)
        CoroutineScope(Dispatchers.Main).launch {
            dialog.show()
            dismiss()
        }
    }
}