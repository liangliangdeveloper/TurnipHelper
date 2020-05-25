package com.liang.turniphelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Bundle;
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button addValue;
    private Button deleteValue;
    private Button analysisValue;

    private int select_year;
    private int select_month;
    private int select_day;
    private int select_dayOfWeek;
    private int select_datepart = -1;

    private TextView value;
    private TextView messageText;

    private RadioGroup checkbox;
    private RadioButton checkAM;
    private RadioButton checkPM;

    private MaterialCalendarView MCalender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Connector.getDatabase();

        addValue = (Button) findViewById(R.id.add_value);
        deleteValue = (Button) findViewById(R.id.delete_value);
        analysisValue = (Button) findViewById(R.id.analysis_value);
        value = (TextView) findViewById(R.id.value);
        messageText = (TextView) findViewById(R.id.day_message);
        checkbox = (RadioGroup) findViewById(R.id.checkboxs);
        checkAM = (RadioButton) findViewById(R.id.check_morning);
        checkPM = (RadioButton) findViewById(R.id.check_afternoon);
        MCalender = (MaterialCalendarView) findViewById(R.id.calendarView);

        addValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_year==0||select_month==0||select_day==0||select_datepart==-1){
                    Toast.makeText(MainActivity.this,"没有选中有效时间",Toast.LENGTH_SHORT).show();
                } else if(isExsit(select_year,select_month,select_day,select_datepart)){
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("已存储数据")
                            .setMessage("请问您要更改您的数据吗？")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    TurnipValue turnipValue = new TurnipValue();
                                    turnipValue.setValue(Integer.parseInt(value.getText().toString()));
                                    turnipValue.updateAll("datecode = ?",toDateCode(select_year,select_month,select_day,select_datepart));
                                    messageText.setText(messageBuild(select_year,select_month,select_day));
                                    messageText.setVisibility(View.VISIBLE);
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
                else {
                    TurnipValue turnipValue = new TurnipValue();
                    turnipValue.setYear(select_year);
                    turnipValue.setMonth(select_month);
                    turnipValue.setDay(select_day);
                    turnipValue.setDatePart(select_datepart);
                    turnipValue.setValue(Integer.parseInt(value.getText().toString()));
                    turnipValue.setDateCode(toDateCode(select_year,select_month,select_day,select_datepart));
                    turnipValue.save();
                    messageText.setText(messageBuild(select_year,select_month,select_day));
                    messageText.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"已添加数据",Toast.LENGTH_SHORT).show();

                }
            }
        });

        deleteValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_year==0||select_month==0||select_day==0||select_datepart==-1){
                    Toast.makeText(MainActivity.this,"没有选中有效时间",Toast.LENGTH_SHORT).show();
                } else if(isExsit(select_year,select_month,select_day,select_datepart)) {
                    DataSupport.deleteAll(TurnipValue.class,"datecode = ?",toDateCode(select_year,select_month,select_day,select_datepart));
                    Toast.makeText(MainActivity.this,"已删除",Toast.LENGTH_SHORT).show();
                    messageText.setText(messageBuild(select_year,select_month,select_day));
                    messageText.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(MainActivity.this,"未储存任何值，无法删除",Toast.LENGTH_SHORT).show();
                }
            }
        });

        checkbox.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == checkAM.getId()){
                    select_datepart = 0;
                }
                if(checkedId == checkPM.getId()){
                    select_datepart = 1;
                }
            }
        });

        MCalender.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                StringBuilder message = new StringBuilder();
                int year = date.getYear();
                int month = date.getMonth() + 1;
                int day = date.getDay();
                select_year = year;
                select_day = day;
                select_month = month;
                Calendar cal = Calendar.getInstance();
                cal.set(year, month - 1, day);
                int dayOfWeek = cal.get(cal.DAY_OF_WEEK);
                select_dayOfWeek = dayOfWeek;
                if(dayOfWeek == 1){
                    checkbox.setVisibility(View.GONE);
                    select_datepart = 2;
                    Toast.makeText(MainActivity.this,"周日只记录购买价",Toast.LENGTH_SHORT).show();
                } else {
                    checkbox.setVisibility(View.VISIBLE);
                    select_datepart = -1;
                }
                messageText.setText(messageBuild(year,month,day));
                messageText.setVisibility(View.VISIBLE);
            }
        });

        MCalender.addDecorators(new NomalDecorator());

        analysisValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(select_year==0||select_month==0||select_day==0){
                    Toast.makeText(MainActivity.this, "没有选中日期",Toast.LENGTH_SHORT).show();
                } else {
                Intent intent = new Intent(MainActivity.this, AnalysisActivity.class);
                intent.putExtra("year",select_year);
                intent.putExtra("month",select_month);
                intent.putExtra("day",select_day);
                //intent.putExtra("dayOfWeek",select_dayOfWeek);
                startActivity(intent);
                }
            }
        });
    }

    private boolean isNull(String s){
        return (s == null || "".equals(s));
    }

    private String toDateCode(int year, int month, int day,int datepart){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(year);
        if(month < 10){
            stringBuilder.append('0');
        }
        stringBuilder.append(month);
        if(day < 10) {
            stringBuilder.append('0');
        }
        stringBuilder.append(day);
        stringBuilder.append(datepart);
        return stringBuilder.toString();
    }

    private boolean isExsit(int year, int month, int day, int datepart){
        List<TurnipValue> values = DataSupport.where("year = ? and month = ? and day = ? and datepart = ?", String.valueOf(year),String.valueOf(month),String.valueOf(day), String.valueOf(datepart)).find(TurnipValue.class);
        if(values.isEmpty()){
            return false;
        } else {
            return true;
        }
    }

    private String messageBuild(int year, int month, int day){
        StringBuilder message = new StringBuilder();
        List<TurnipValue> values = DataSupport.where("year = ? and month = ? and day = ? ", String.valueOf(year),String.valueOf(month),String.valueOf(day)).order("datepart").find(TurnipValue.class);
        message.append(year);
        message.append("年");
        message.append(month);
        message.append("月");
        message.append(day);
        message.append("日的情况如下：\n");
        if(values.isEmpty()) {
            message.append("本日无记录");
        }
        for (TurnipValue v : values){
            if(v.getDatePart() == 0){
                message.append("本日上午的成交额为：");
                message.append(v.getValue());
                message.append("铃钱\n");
            }
            if(v.getDatePart() == 1){
                message.append("本日下午的成交额为：");
                message.append(v.getValue());
                message.append("铃钱\n");
            }
            if(v.getDatePart() == 2){
                message.append("周日的收购价为：");
                message.append(v.getValue());
                message.append("铃钱\n");
            }
        }
        return message.toString();
    }
}
