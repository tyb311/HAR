package com.tyb.utils;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class UtilMChartLine {
	private LineChart chart;

	public UtilMChartLine(LineChart _mChart){
		this.chart = _mChart;
//		setChartStyle
		chart.setDescription("Accelerator");
		chart.setNoDataTextDescription("正在采集数据");

		chart.setBackgroundColor(Color.TRANSPARENT);
		chart.setGridBackgroundColor(Color.TRANSPARENT);
		chart.setDrawGridBackground(true);
		chart.setDrawBorders(false);//是否禁止绘制图表边框的线
		chart.setHighlightPerTapEnabled(true);// 如果设置为true,高亮显示/选择值是可能的图表。 默认值:真
		chart.setTouchEnabled(false);
		chart.setDragEnabled(false);   //能否拖拽
		chart.setScaleEnabled(false);  //能否缩放
		chart.setPinchZoom(false);

//		setupXAxis
		XAxis axisX = chart.getXAxis();
//		axisX.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
		axisX.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);
		axisX.setTextColor(ColorTemplate.PASTEL_COLORS[0]);
//		axisX.setLabelsToSkip(5);

		axisX.setDrawAxisLine(false);//是否绘制轴线
		axisX.setDrawGridLines(false);//设置x轴上每个点对应的线
		axisX.setDrawLabels(false);//绘制标签  指x轴上的对应数值
		axisX.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
		axisX.setTextSize(12f);//设置文字大小
		axisX.setAvoidFirstLastClipping(false);//图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘

//		setupYAxis
		YAxis axisRight = chart.getAxisRight();
		axisRight.setEnabled(false);
		YAxis axisLeft = chart.getAxis(YAxis.AxisDependency.LEFT);
		axisLeft.setDrawGridLines(true);
		axisLeft.setDrawLabels(true);//绘制标签  指x轴上的对应数值
//		axisLeft.setGridColor(ColorTemplate.PASTEL_COLORS[0]);
		axisLeft.setAxisLineColor(ColorTemplate.PASTEL_COLORS[0]);
		axisLeft.setTextColor(ColorTemplate.PASTEL_COLORS[0]);

//		setup Legend
		Legend lg = chart.getLegend();
		lg.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
		lg.setForm(Legend.LegendForm.LINE);
		// l.setXEntrySpace(5f);
		// l.setFormSize(10f);
	}

	public static final int DATA_COUNT = 25;
	public void renderFloats(List<float[]> values){
		List<Entry> pointsX = new ArrayList<>();
		List<Entry> pointsY = new ArrayList<>();
		List<Entry> pointsZ = new ArrayList<>();

		int point_real_size= (values.size()< DATA_COUNT)?values.size(): DATA_COUNT;
		for (int j = 0; j < point_real_size; j++) {//循环为节点、X、Y轴添加数据
			pointsX.add(new Entry(values.get(j)[0], j));
			pointsY.add(new Entry(values.get(j)[1], j));
			pointsZ.add(new Entry(values.get(j)[2], j));
		}

		LineDataSet dataSetA = getLineDataSet(pointsX, "AccX",0);
		LineDataSet dataSetB = getLineDataSet(pointsY, "AccY",1);
		LineDataSet dataSetC = getLineDataSet(pointsZ, "AccZ",3);

		List<LineDataSet> dataSets = new ArrayList<>();
		dataSets.add(dataSetA);
		dataSets.add(dataSetB);
		dataSets.add(dataSetC);

		// LineData(List<String> Xvals座標標籤, List<Dataset> 資料集)
		List<String> labels = getLabels(point_real_size);
		LineData data = new LineData(labels, dataSets);
		chart.setData(data);
		chart.invalidate();
		chart.notifyDataSetChanged();

//		chart.animateX(2500);// 模拟水平轴上的图表值的,也就是说图表将建立在指定的时间内从左到右。
//		chart.animateY(2500);//:模拟垂直轴上的图表值的,也就是说图表将建立在指定时间从下到上。
//		chart.animateXY(durationMillisX, durationMillisY);//模拟两个水平和垂直轴的,导致左/右下/上累积。
	}


	private LineDataSet getLineDataSet(List<Entry> entries, String label, int idxColor){
		// LineDataSet(List<Entry> 資料點集合, String 類別名稱)
		LineDataSet dataSetA = new LineDataSet(entries, label);

		// 設定 datasetA 的 data point 樣式
		dataSetA.setColor(ColorTemplate.JOYFUL_COLORS[idxColor]);
		dataSetA.setCircleColor(ColorTemplate.JOYFUL_COLORS[idxColor]);
		dataSetA.setDrawCircleHole(true);
		dataSetA.setCircleColorHole(Color.WHITE);
		dataSetA.setCircleSize(2f);
		dataSetA.setDrawCubic(true);
		dataSetA.disableDashedLine();

		return  dataSetA;
	}

	/* 取得 XVals Labels 給 LineData */
	private List<String> getLabels(int count) {
		List<String> chartLabels = new ArrayList<>();
		for (int i=0; i<count; i++) {
			chartLabels.add("t" + i);
		}
		return chartLabels;
	}
}
