package com.example.todayseat.ui.home

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.contentValuesOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todayseat.R
import com.example.todayseat.databinding.InsertMenuDialogBinding
import com.example.todayseat.databinding.ScoreMenuDialogBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat


//bar chart 연결을 위한 global 변수 감점요소입니다!!!!!! 크면 클수록 안 좋은 거에요!!!!
var kcalS = 0f        // 칼로리점수 :  { |데이터베이스의 합 - ( 자신의 키 -100 * 0.9 * 활동지수 )| - buffer 100kcal  }* 4kcal /(4+9kcal) * 0.25}
var carboS = 0f       // 탄수화물점수 : {|총칼로리*0.6/4g - 데이터베이스 g|  - buffer 25g }* 4kcal /(4+9kcal) * 0.12
var fatS = 0f         // 지방점수 : 데이터베이스의 합 - 50g *9kcal /(4+9kcal) * 0.38
var proteinS = 0f     // 단백질점수 : (영양성분표 기준 권장g 이상 - 데이터베이스의 합g) * 4kcal /(4+9kcal) * 0.25

// 여기가 100점으로 점수화한 데이터 입니다!!! 높으면 높을 수록 좋은겁니다!!!
var fatS100 = 0f
var proteinS100 = 0f
var carboS100 =0f
var kcalS100 =0f
// 위 데이터는 임의로 불러온 점수입니다. xml 확인용입니다.

//chart를 위한 초기화
var color1 : Int = 0
var color2 : Int = 0
var color3 : Int = 0

class CustomMenuScoreDialog(var activity: Activity) : Dialog(activity),
    View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        val binding= ScoreMenuDialogBinding.inflate(layoutInflater)
        // 확인 버튼클릭시, 무시
        binding.ignoreBtn.setOnClickListener {
            dismiss()
        }
        initBarChart(binding.insertMenuRecyclerView)
        setData(binding.insertMenuRecyclerView)
        Log.d("TAG11","다이얼로그출력!")
        setContentView(binding.root)
        val sql="select * from food where F_name=' ${HomeFragment2.food_name};"

//        yes.setOnClickListener(this)
//        no.setOnClickListener(this)


        //그래프를 위한 변수 추가
        //영양분 점수 계산식
        // #####################여기를 바꾸시면 적용됩니다
        var nST = 0f //영양분 점수
        var totalKcal = 1625.77f // 총섭취칼로리
        // #####################여기를 바꾸시면 적용됩니다

        //고객 정보
        var height = 175f
        var activation = 30f

        //DB 에서 합한 합
        var totalCarbo = 202.78f // 총섭취 탄수화물 (g) 데이터 베이스상 단위 때문에 추가
        var totalFat = 62.66f // 총섭취 지방 (g) 데이터 베이스상 단위 때문에 추가
        var totalProtein = 62.69f   // 총섭취 단백질 (g) 데이터 베이스상 단위 때문에 추가

        var need_protein = 50f // 권장 단백질
        var need_calorie = 0f // 권장 칼로리 : ( 자신의 키 -100 * 0.9 * 활동지수 )


        //건강 지수 계산
        need_calorie =
            ((height - 100f) *0.9f * activation) // 권장 칼로리 (자신의 키 - 100) *0.9 * 활동지수
        kcalS = (100f*0.25f - ((kotlin.math.abs((totalKcal) - need_calorie) - 100f)*4f/(4f+9f) * 0.25f))
        carboS = (100f*0.12f - ((kotlin.math.abs(totalKcal * 0.6f / 4f - totalCarbo) - 25f)*4f/(4f+9f)*0.12f))
        fatS = (100f*0.38f - ((totalFat - 50f ) *9f /(4f+9f) * 0.38f))
        proteinS = (100f*0.25f - (( need_protein - totalProtein ) * 4f / (4f+9f)*0.25f))

        nST = kcalS+carboS+fatS+proteinS // 최종 영양분 점수 nutrientScoreTotal

        //그래프를 위한 백점 환산
        fatS100 = (fatS.toDouble() * 100.0 / 38.0).toFloat()
        if (fatS100>=100) fatS100=100f
        proteinS100 = (proteinS.toDouble()*100.0/25.0).toFloat()
        if (proteinS100>=100) proteinS100=100f
        carboS100 = (carboS.toDouble()*100.0/25.0).toFloat()
        if (carboS>=100) carboS=100f
    }

    override fun onClick(p0: View?) {
        //TODO("Not yet implemented")
    }

}

private fun initBarChart(barChart: BarChart) {
    // 차트 회색 배경 설정 (default = false)
    barChart.setDrawGridBackground(false)
    // 막대 그림자 설정 (default = false)
    barChart.setDrawBarShadow(false)
    // 차트 테두리 설정 (default = false)
    barChart.setDrawBorders(false)

    val description = Description()
    // 오른쪽 하단 모서리 설명 레이블 텍스트 표시 (default = false)
    description.isEnabled = false
    barChart.description = description

    // X, Y 바의 애니메이션 효과
    barChart.animateY(1000)
    barChart.animateX(1000)

    // 바텀 좌표 값
    val xAxis: XAxis = barChart.xAxis
    // x축 위치 설정
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    // 그리드 선 수평 거리 설정
    xAxis.granularity = 1f
    // x축 텍스트 컬러 설정
    xAxis.textColor = Color.rgb(10,10,10)
    // x축 선 설정 (default = true)
    xAxis.setDrawAxisLine(true)
    // 격자선 설정 (default = true)
    xAxis.setDrawGridLines(false)
    // 라벨 표시여부
    xAxis.setDrawLabels(true)
    xAxis.valueFormatter = MyAxisFormatter()

    val leftAxis: YAxis = barChart.axisLeft
    // 좌측 선 설정 (default = true)
    leftAxis.setDrawAxisLine(false)
    // 좌측 텍스트 컬러 설정
    leftAxis.textColor = Color.rgb(186,176,164)
    leftAxis.setAxisMaximum(100f)
    leftAxis.setAxisMinimum(0f)


    val rightAxis: YAxis = barChart.axisRight
    // 우측 선 설정 (default = true) 선 지움
    rightAxis.setDrawAxisLine(false)
    rightAxis.setDrawGridLines(false)
    // 우측 텍스트 컬러 설정 (흰색으로 안 보이게)
    rightAxis.textColor = Color.rgb(255,255,255)


    //default라 3개의 네모로 표현됩니다.
    /*
    // 바차트의 타이틀
    val legend: Legend = barChart.legend
    // 범례 모양 설정 (default = 정사각형)
    legend.form = Legend.LegendForm.LINE
    // 타이틀 텍스트 사이즈 설정
    legend.textSize = 20f
    // 타이틀 텍스트 컬러 설정
    legend.textColor = Color.BLACK
    // 범례 위치 설정
    legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
    legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
    // 범례 방향 설정
    legend.orientation = Legend.LegendOrientation.HORIZONTAL
    // 차트 내부 범례 위치하게 함 (default = false)
    legend.setDrawInside(true)
    */
}

// 차트 데이터 설정
private fun setData(barChart: BarChart) {

    // Zoom In / Out 가능 여부 설정
    barChart.setScaleEnabled(false)

    val valueList = ArrayList<BarEntry>()
    val title = " "

    // 임의 데이터
    /*for (i in 0 until 3) {
        valueList.add(BarEntry(i.toFloat(), i * 100f))
    }
     */

    // 여기를 100점 만점으로 기준 잡아야할듯 탄단지가 서로 단위가 너무 다름
    valueList.add(BarEntry(1f, fatS100)) // 가장 아래에 들어감 지방
    valueList.add(BarEntry(2f, proteinS100)) // 단백질
    valueList.add(BarEntry(3f, carboS100)) // 탄수화물

    val barDataSet = BarDataSet(valueList, title)
    // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
    // 33(빨간색)이수현 66(주황색) 100(초록색)

    //var color1 by Delegates.notNull<Int>()
    //var color2 by Delegates.notNull<Int>()
    //var color3 by Delegates.notNull<Int>()

    when(carboS100.toInt()){
        in 0..33 ->
            color1 = Color.rgb(223,88,66)
        in 34..66 ->
            color1 = Color.rgb(253,208,89)
        in 67..100 ->
            color1 = Color.rgb(191,225,192)
    }

    when(proteinS100.toInt()){
        in 0..33 ->  color2 = Color.rgb(223,88,66)
        in 34..66 -> color2 = Color.rgb(253,208,89)
        in 67..100 ->  color2 = Color.rgb(191,225,192)
    }

    when(fatS100.toInt()){
        in 0..33 ->  color3 = Color.rgb(223,88,66)
        in 34..66 -> color3 = Color.rgb(253,208,89)
        in 67..100 ->  color3 = Color.rgb(191,225,192)
    }
    //아래에서부터 들어감
    barDataSet.setColors(
        color3,color2,color1)
    val data = BarData(barDataSet)
    barChart.data = data
    barChart.invalidate()
}

class MyAxisFormatter : ValueFormatter(){
    private val nutrientList = arrayOf("지방","단백질","탄수화물")
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
        return nutrientList.getOrNull(value.toInt()-1)?: value.toString()
    }
}

