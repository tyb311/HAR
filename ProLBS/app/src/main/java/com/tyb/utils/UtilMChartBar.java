package com.tyb.utils;

import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class UtilMChartBar {
	private BarChart chart;

	public UtilMChartBar(BarChart _mChart){
		this.chart = _mChart;
//		setChartStyle
		chart.setDescription("ActivityBar");
		chart.setNoDataTextDescription("正在采集数据");

		//背景颜色
		chart.setBackgroundColor(Color.TRANSPARENT);
		chart.setGridBackgroundColor(Color.TRANSPARENT);
		chart.setDrawGridBackground(false);
		chart.setDrawBorders(false);//是否禁止绘制图表边框的线
		chart.setDrawBarShadow(false);
		chart.setDrawHighlightArrow(false);

		chart.setTouchEnabled(false);
		chart.setDragEnabled(false);   //能否拖拽
		chart.setScaleEnabled(false);  //能否缩放
//
//		Matrix m=new Matrix();
//		m.postScale(20f,1f);//两个参数分别是x,y轴的缩放比例。例如：将x轴的数据放大为之前的1.5倍
//		chart.getViewPortHandler().refresh(m,chart,false);//将图表动画显示之前进行缩放
//		chart.animateX(1000);

		/***XY轴的设置***/
		//X轴设置显示位置在底部
//		setupXAxis
		XAxis axisX = chart.getXAxis();
		axisX.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
		axisX.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);
		axisX.setTextColor(ColorTemplate.PASTEL_COLORS[0]);
//		axisX.setLabelsToSkip(5);
		axisX.setDrawAxisLine(true);//是否绘制轴线
		axisX.setDrawGridLines(false);//设置x轴上每个点对应的线
		axisX.setDrawLabels(true);//绘制标签  指x轴上的对应数值
		axisX.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
		axisX.setTextSize(16f);//设置文字大小
		axisX.setAvoidFirstLastClipping(false);//图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘

		YAxis axisRight = chart.getAxisRight();
		axisRight.setEnabled(false);
		YAxis axisLeft = chart.getAxis(YAxis.AxisDependency.LEFT);
		axisLeft.setDrawGridLines(true);
		axisLeft.setDrawLabels(true);//绘制标签  指x轴上的对应数值
		axisLeft.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
		axisLeft.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);
		axisLeft.setTextColor(ColorTemplate.PASTEL_COLORS[0]);
		axisLeft.setAxisMaxValue(1);
		axisLeft.setAxisMinValue(0);

		/***折线图例 标签 设置***/
		Legend lg = chart.getLegend();
		lg.setForm(Legend.LegendForm.SQUARE);
		lg.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
//		lg.setXEntrySpace(16f);
//		lg.setTextSize(16f);
	}

	private BarDataSet getRandomDataSet(String label, int idxColor){
		List<BarEntry> barEntryList = new ArrayList();
		for (int i=0; i<5; i++) {
			barEntryList.add(new BarEntry((float) Math.random(), i));
		}
		BarDataSet dataset1 = new BarDataSet(barEntryList,label);
		dataset1.setColor(ColorTemplate.JOYFUL_COLORS[idxColor]);//设置第一组的颜色
		return  dataset1;
	}

	public void renderFloats(float[] data) {
		List<BarEntry> barEntryList = new ArrayList();
		for (int i=0; i<5; i++) {
			barEntryList.add(new BarEntry(data[i], i));
		}
		BarDataSet dataset = new BarDataSet(barEntryList,"Probability");
		dataset.setColor(ColorTemplate.JOYFUL_COLORS[3]);//设置第一组的颜色
		BarData barData = new BarData(new String[]{"still","walk","run","fall","sos"}, dataset);
//		data.setValueTextSize(12f);
		render(barData);
	}

	public void randomRender() {
		BarDataSet dataset = getRandomDataSet("Probability", 3);
		//如果一组就传入BarDataSet，多组时传入的List<BarDataSet>
		BarData data = new BarData(new String[]{"still","walk","run","fall","sos"}, dataset);
//		data.setValueTextSize(12f);
		render(data);
	}

	public void render(BarData data){
		chart.setData(data);
		chart.invalidate();                    //将图表重绘以显示设置的属性和数据
		chart.notifyDataSetChanged();

//		chart.animateX(100);// 模拟水平轴上的图表值的,也就是说图表将建立在指定的时间内从左到右。
//		chart.animateY(100);//:模拟垂直轴上的图表值的,也就是说图表将建立在指定时间从下到上。
//		chart.animateXY(durationMillisX, durationMillisY);//模拟两个水平和垂直轴的,导致左/右下/上累积。
	}
}