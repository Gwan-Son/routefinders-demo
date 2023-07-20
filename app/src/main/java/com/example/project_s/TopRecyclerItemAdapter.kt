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


class TopRecyclerItemAdapter(private val items: ArrayList<Information>) : RecyclerView.Adapter<TopRecyclerItemAdapter.ViewHolder>(){

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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopRecyclerItemAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.stock_item_recycler, parent, false)
        return ViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: TopRecyclerItemAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.stockName.text = item.name
        if(item.prevPercent[0] == '+'){
            holder.stockPrice.text = item.price
            holder.stockPrevPrice.text = "▲  " + item.prevPrice
            holder.stockPrevPercent.text = item.prevPercent
            holder.stockPrice.setTextColor(Color.parseColor("#ff7b7b"))
            holder.stockPrevPrice.setTextColor(Color.parseColor("#ff7b7b"))
            holder.stockPrevPercent.setTextColor(Color.parseColor("#ff7b7b"))
        }
        else if(item.prevPercent[0] == '-'){
            holder.stockPrice.text = item.price
            holder.stockPrevPrice.text = "▼  " + item.prevPrice
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
    }
}