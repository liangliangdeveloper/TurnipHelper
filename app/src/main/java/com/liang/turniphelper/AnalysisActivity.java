package com.liang.turniphelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.security.KeyStore;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AnalysisActivity extends AppCompatActivity {


    private LineChart analysisChart;
    private TextView analysisText;

    private class myDate{
        int year;
        int month;
        int day;

        public myDate(int year, int month, int day){
            this.year = year;
            this.month = month;
            this.day = day;
        }
    }

    private List<myDate> weekDays = new ArrayList<>();
    private List<ILineDataSet> Valuesets = new ArrayList<>();
    private List<String> weekDaysString = new ArrayList<>();

    private XAxis xAxis;
    private YAxis yAxis;
    private YAxis yAxis_r;

    private int maxValue = 0;
    private int minValue = 99999;
    private int allValue = 0;
    private int allDays = 0;
    private myDate maxDate = new myDate(0,0,0);
    private myDate minDate = new myDate(0,0,0);
    private int interestValue = 0;
    private int averageInterestValue = 0;
    private int limit = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analysis_layout);

        Connector.getDatabase();

        Intent intent = getIntent();
        findThisWeek(intent.getIntExtra("year",2020),intent.getIntExtra("month",3),intent.getIntExtra("day",20));


        analysisChart = (LineChart) findViewById(R.id.week_chart);
        analysisText = (TextView) findViewById(R.id.analysis_text);

        analysisChart.setNoDataText("本周未录入大头菜价格数据");
        Description d = new Description();
        d.setText("");
        analysisChart.setDescription(d);


        xAxis = analysisChart.getXAxis();       //获取x轴线
        xAxis.setDrawAxisLine(true);//是否绘制轴线
        xAxis.setDrawGridLines(false);//设置x轴上每个点对应的线
        xAxis.setDrawLabels(true);//绘制标签  指x轴上的对应数值
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);//设置x轴的显示位置
        xAxis.setTextSize(12f);//设置文字大小
        xAxis.setAxisMinimum(0f);//设置x轴的最小值 //`
        xAxis.setAxisMaximum(11f);//设置最大值 //
        xAxis.setLabelCount(11);  //设置X轴的显示个数
        xAxis.setAvoidFirstLastClipping(false);//图表将避免第一个和最后一个标签条目被减掉在图表或屏幕的边缘
        xAxis.setLabelRotationAngle(90.0f);

        yAxis = analysisChart.getAxisLeft();

        yAxis_r = analysisChart.getAxisRight();
        yAxis_r.setEnabled(false);

        if(isExsit(weekDays.get(0).year,weekDays.get(0).month,weekDays.get(0).day,2)){
            List<TurnipValue> values = DataSupport.where("year = ? and month = ? and day = ?", String.valueOf(weekDays.get(0).year),String.valueOf(weekDays.get(0).month),String.valueOf(weekDays.get(0).day)).find(TurnipValue.class);
            limit = values.get(0).getValue();
            Log.d("Get","Get");
        }
        LimitLine l = new LimitLine((float)limit,"本周购入价："+String.valueOf(limit));
        l.setLineColor(R.color.mcv_text_date_dark);
        yAxis.addLimitLine(l);

        LineDataSet set = addLineChartValues();
        if(set != null){
            Valuesets.add(set);
            LineData data = new LineData(Valuesets);
            analysisChart.setData(data);

            int AxisMax, AxisMin;
            if(limit > maxValue) {
                AxisMax = limit + 10;
            } else {
                AxisMax = maxValue + 10;
            }
            if (limit < minValue) {
                AxisMin = limit - 10;
            } else {
                AxisMin = minValue - 10;
            }
            yAxis.setAxisMaximum(AxisMax);
            yAxis.setAxisMinimum(AxisMin);

            //绘制图表
            analysisChart.invalidate();

            StringBuilder builder = new StringBuilder();
            builder.append("本周分析结果：\n");
            builder.append("最大值：");
            builder.append(maxValue+ "铃钱");
            builder.append("，出现在");
            builder.append(maxDate.year+"年"+maxDate.month+"月"+maxDate.day+"日\n");
            builder.append("最小值：");
            builder.append(minValue+ "铃钱");
            builder.append("，出现在");
            builder.append(minDate.year+"年"+minDate.month+"月"+minDate.day+"日\n");
            builder.append("平均值：");
            builder.append((int)(allValue/allDays) + "铃钱\n");
            builder.append("本周购入值为：");
            builder.append(limit+"铃钱（若没有写入默认为100，实际购买价在90~110元浮动）\n");
            builder.append("当前收益值为：");
            builder.append(interestValue - limit);
            builder.append("铃钱\n");
            builder.append("平均收益值为：");
            builder.append((int)(allValue/allDays)-limit);
            builder.append("铃钱\n");
            analysisText.setText(builder.toString());
        } else {
            analysisChart.setVisibility(View.GONE);
            analysisText.setText("本周无新增数据，无法做出分析");
            analysisText.setGravity(Gravity.CENTER_HORIZONTAL);
        }


    }

    private void findThisWeek(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        calendar.add(Calendar.DAY_OF_MONTH,1 - today - 1 );
        for(int i = 0 ; i < 7; i++){
            calendar.add(calendar.DAY_OF_MONTH, 1);
            myDate d = new myDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
            weekDays.add(d);
        }
        Log.d("SUNDAY",String.valueOf(weekDays.get(0).day));
    }

    private LineDataSet addLineChartValues(){
        final List<Entry> myValues;
        myValues = new ArrayList<>();
        for(int i = 0; i < 6 ; i++) {
            for (int j = 0; j < 2; j ++){
                StringBuilder builder = new StringBuilder();
                builder.append(weekDays.get(i+1).month);
                builder.append(".");
                builder.append(weekDays.get(i+1).day);
                if(j==0){
                    builder.append(" AM");
                } else {
                    builder.append(" PM");
                }
                weekDaysString.add(builder.toString());
                if(isExsit(weekDays.get(i+1).year,weekDays.get(i+1).month,weekDays.get(i+1).day,j)){
                    List<TurnipValue> values = DataSupport.where("year = ? and month = ? and day = ? and datepart = ?", String.valueOf(weekDays.get(i+1).year),String.valueOf(weekDays.get(i+1).month),String.valueOf(weekDays.get(i+1).day), String.valueOf(j)).find(TurnipValue.class);
                    myValues.add(new Entry((i)*2+j,values.get(0).getValue(),builder.toString()));


                    //数据统计
                    if(values.get(0).getValue() > maxValue) {
                        maxValue = values.get(0).getValue();
                        maxDate.year = weekDays.get(i+1).year;
                        maxDate.month = weekDays.get(i+1).month;
                        maxDate.day = weekDays.get(i+1).day;
                    }
                    if(values.get(0).getValue() < minValue) {
                        minValue = values.get(0).getValue();
                        minDate.year = weekDays.get(i+1).year;
                        minDate.month = weekDays.get(i+1).month;
                        minDate.day = weekDays.get(i+1).day;
                    }
                    allValue += values.get(0).getValue();
                    allDays ++;
                    interestValue = values.get(0).getValue();

                    Log.d("AnalysisActivity",String.valueOf(weekDays.get(i+1).year)+String.valueOf(weekDays.get(i+1).month)+String.valueOf(weekDays.get(i+1).day)+String.valueOf(j));
                }
            }
        }

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return weekDaysString.get((int)value);
            }
        });
        LineDataSet set;
        if(myValues.isEmpty()){
            set = null;
            return set;
        }
        set = new LineDataSet(myValues, "大头菜价格");
        final DecimalFormat mFormat = new DecimalFormat("###,###,##0");
        set.setValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });
        set.setValueTextSize(12);
        set.setLineWidth(3);
        set.setCircleRadius(5);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        return set;
    }

    private boolean isExsit(int year, int month, int day, int datepart){
        List<TurnipValue> values = DataSupport.where("year = ? and month = ? and day = ? and datepart = ?", String.valueOf(year),String.valueOf(month),String.valueOf(day), String.valueOf(datepart)).find(TurnipValue.class);
        if(values.isEmpty()){
            return false;
        } else {
            return true;
        }
    }
}
