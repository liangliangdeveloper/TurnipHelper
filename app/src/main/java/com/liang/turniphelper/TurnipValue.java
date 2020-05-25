package com.liang.turniphelper;

import org.litepal.crud.DataSupport;

public class TurnipValue extends DataSupport {

    private int id;  //按照创建顺序的id

    private String dateCode;  //8位时间码 XXXX-XX-XX

    private int year;

    private int month;

    private int day;

    private int datePart;  //上午=0，下午=1

    private int value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateCode() {
        return dateCode;
    }

    public void setDateCode(String dateCode) {
        this.dateCode = dateCode;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDatePart() {
        return datePart;
    }

    public void setDatePart(int datePart) {
        this.datePart = datePart;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
