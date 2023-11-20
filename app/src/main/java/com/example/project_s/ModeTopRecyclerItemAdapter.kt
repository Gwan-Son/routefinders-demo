package com.example.project_s

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import kotlin.collections.ArrayList


class ModeTopRecyclerItemAdapter(private val items: ArrayList<Information>) : RecyclerView.Adapter<ModeTopRecyclerItemAdapter.ViewHolder>(){
    private var tts: TextToSpeech? = null
    override fun getItemCount(): Int = items.size
    var filteredStock = ArrayList<Information>()
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var stockName : TextView
        var stockPrice : TextView
        var stockPrevPrice : TextView
        var stockPrevPercent : TextView
        init {
            stockName = v.findViewById(R.id.stockName)
            stockPrice = v.findViewById(R.id.stockPrice)
            stockPrevPrice = v.findViewById(R.id.stockPrevPrice)
            stockPrevPercent = v.findViewById(R.id.stockPrevPercent)
        }
    }
    init {
        filteredStock.addAll(items)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeTopRecyclerItemAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.stock_item_recycler, parent, false)
        fun initTextToSpeech(){
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                return
            }
            tts = TextToSpeech(parent.context){
                if(it == TextToSpeech.SUCCESS){
                    val result = tts?.setLanguage(Locale.KOREAN)
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        return@TextToSpeech
                    }
                }else{
                }
            }
        }
        initTextToSpeech()
        return ViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: ModeTopRecyclerItemAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.stockName.text = item.name
        if(item.prevPercent[0] == '+'){
            holder.stockPrice.text = item.price
            holder.stockPrevPrice.text = "▲    " + item.prevPrice
            holder.stockPrevPercent.text = item.prevPercent
            holder.stockPrice.setTextColor(Color.parseColor("#ff7b7b"))
            holder.stockPrevPrice.setTextColor(Color.parseColor("#ff7b7b"))
            holder.stockPrevPercent.setTextColor(Color.parseColor("#ff7b7b"))
        }
        else if(item.prevPercent[0] == '-'){
            holder.stockPrice.text = item.price
            holder.stockPrevPrice.text = "▼    " + item.prevPrice
            holder.stockPrevPercent.text = item.prevPercent
            holder.stockPrice.setTextColor(Color.parseColor("#718ef4"))
            holder.stockPrevPrice.setTextColor(Color.parseColor("#718ef4"))
            holder.stockPrevPercent.setTextColor(Color.parseColor("#718ef4"))
        }
        else{
            holder.stockPrice.text = item.price
            holder.stockPrevPrice.text = item.prevPrice
            holder.stockPrevPercent.text = item.prevPercent
            holder.stockPrice.setTextColor(Color.BLACK)
            holder.stockPrevPrice.setTextColor(Color.BLACK)
            holder.stockPrevPercent.setTextColor(Color.BLACK)
        }
        holder.itemView.setOnClickListener {
            //TTS 추가
            var TopText = "" + holder.stockName.text + "의 현재가는 " + holder.stockPrice.text + "입니다. 전일가 대비 " +  holder.stockPrevPrice.text + "이 차이가 나며, " +holder.stockPrevPercent.text +"가 바뀌었습니다."
            ttsSpeak(TopText)
        }
        holder.itemView.setOnLongClickListener {
            showPurchaseDialog(holder.itemView.context,item.name,item.price)
            true
        }
    }
    fun ttsSpeak(strTTS:String){
        tts?.speak(strTTS,TextToSpeech.QUEUE_FLUSH,null,null)
    }
    private fun showPurchaseDialog(context: Context, stockName: String, stockPrice: String){
        val purchaseDialog = ModePurchaseDialog(context,stockName,stockPrice)
        purchaseDialog.show()
    }
}