package com.example.project_s

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
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


class RecyclerUserAdapter(private val items: ArrayList<Information>) : RecyclerView.Adapter<RecyclerUserAdapter.ViewHolder>(),
    Filterable {

    override fun getItemCount(): Int = items.size
    var filteredStock = ArrayList<Information>()
    var itemFilterStock = ItemFilter()
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerUserAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.stock_item_recycler, parent, false)
        return ViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: RecyclerUserAdapter.ViewHolder, position: Int) {
        if(filteredStock.size != 0){
            if(position < filteredStock.size){
                val item = filteredStock[position]
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
                    var mobileIntent = Intent(holder.itemView?.context,WebView::class.java)
                    mobileIntent.putExtra("mobileUrl",item.itemCode)
                    startActivity(holder.itemView.context,mobileIntent,null)
                }
            }else{
                holder.stockName.text = ""
                holder.stockPrice.text = ""
                holder.stockPrevPercent.text = ""
                holder.stockPrevPrice.text = ""
            }
        }else{
            holder.stockName.text = ""
            holder.stockPrice.text = ""
            holder.stockPrevPercent.text = ""
            holder.stockPrevPrice.text = ""
        }
    }
    override fun getFilter(): Filter {
        return itemFilterStock
    }
    inner class ItemFilter : Filter() {
        override fun performFiltering(charSequence: CharSequence): FilterResults {
            val filterString = charSequence.toString()
            val results = FilterResults()
            //검색이 필요없을 경우를 위해 원본 배열을 복제
            val filteredList = ArrayList<Information>()
            //공백제외 아무런 값이 없을 경우 -> 원본 배열
            if (filterString.trim { it <= ' ' }.isEmpty()) {
                results.values = items
                results.count = items.size
                return results
                //공백제외 2글자 이상인 경우 -> 이름으로 검색
            } else {
                for (item in items) {
                    if (item.name.contains(filterString)) {
                        filteredList.add(item)
                    }
                }
            }
            results.values = filteredList
            results.count = filteredList.size
            return results
        }
        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults) {
            filteredStock.clear()
            filteredStock.addAll(filterResults.values as ArrayList<Information>)
            Log.i("TAG","items : "+filterResults.values)
            notifyDataSetChanged()
        }
    }
}