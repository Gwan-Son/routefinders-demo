package com.example.project_s

data class Information(
    val name:String,//종목명
    val price:String,//현재가
    val prevPrice:String,//전일비
    val prevPercent:String,//등락률
    val itemCode:String//종목 코드
)
