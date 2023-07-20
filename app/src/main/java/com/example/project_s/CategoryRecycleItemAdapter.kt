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
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView


class CategoryRecycleItemAdapter(private val items: ArrayList<Information>) : RecyclerView.Adapter<CategoryRecycleItemAdapter.ViewHolder>(),Filterable {

    override fun getItemCount(): Int = items.size
    var filteredCategory = ArrayList<Information>()
    var itemFilterCategory = ItemFilter()
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v){
        var stockName : TextView
        var stockPrice : TextView
        var stockPrevPrice : TextView
        var stockPrevPercent : TextView
        init {
            stockName = v.findViewById(R.id.categoryName)
            stockPrice = v.findViewById(R.id.categoryPrice)
            stockPrevPrice = v.findViewById(R.id.categoryPrevPrice)
            stockPrevPercent = v.findViewById(R.id.categoryPrevPercent)
        }
    }
    init{
        filteredCategory.addAll(items)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryRecycleItemAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.category_item_recycler, parent, false)
        return ViewHolder(inflatedView)
    }
    override fun onBindViewHolder(holder: CategoryRecycleItemAdapter.ViewHolder, position: Int) {
        if(filteredCategory.size != 0){
            if(position < filteredCategory.size){
                val cateItem = filteredCategory[position]
                holder.stockName.text = cateItem.name
                if(cateItem.prevPercent[0] == '+'){
                    holder.stockPrevPercent.text = ""
                    holder.stockPrevPrice.text = ""
                    holder.stockPrice.text = "▲    " + cateItem.prevPercent
                    holder.stockPrice.setTextColor(Color.parseColor("#ff7b7b"))
                }
                else if(cateItem.prevPercent[0] == '-'){
                    holder.stockPrevPercent.text = ""
                    holder.stockPrevPrice.text = ""
                    holder.stockPrice.text = "▼    " + cateItem.prevPercent
                    holder.stockPrice.setTextColor(Color.parseColor("#718ef4"))
                }
                else{
                    holder.stockPrevPercent.text = ""
                    holder.stockPrevPrice.text = ""
                    holder.stockPrice.text = cateItem.prevPercent
                    holder.stockPrice.setTextColor(Color.BLACK)
                }
                holder.itemView.setOnClickListener {
                    if(loginMode){
                        var mobileIntent = Intent(holder.itemView?.context,StockActivity::class.java)
                        var tempUrlIntent = "https://finance.naver.com/sise/sise_group_detail.naver?type=upjong&no="+cateItem.itemCode
                        mobileIntent.putExtra("Url",tempUrlIntent)
                        startActivity(holder.itemView.context,mobileIntent,null)
                    }
                    else{
                        var mobileIntent = Intent(holder.itemView?.context,ModeStockActivity::class.java)
                        var tempUrlIntent = "https://finance.naver.com/sise/sise_group_detail.naver?type=upjong&no="+cateItem.itemCode
                        mobileIntent.putExtra("Url",tempUrlIntent)
                        startActivity(holder.itemView.context,mobileIntent,null)
                    }

                }
            }else{
                holder.stockName.text = ""
                holder.stockPrice.text = ""
                holder.stockPrevPercent.text = ""
                holder.stockPrevPrice.text = ""
            }
        } else{
            holder.stockName.text = ""
            holder.stockPrice.text = ""
            holder.stockPrevPercent.text = ""
            holder.stockPrevPrice.text = ""
        }
    }
    override fun getFilter(): Filter {
        return itemFilterCategory
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
            filteredCategory.clear()
            filteredCategory.addAll(filterResults.values as ArrayList<Information>)
            Log.i("TAG","items : "+filterResults.values)
            notifyDataSetChanged()
        }
}
}