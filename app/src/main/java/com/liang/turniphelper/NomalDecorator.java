package com.liang.turniphelper;

import android.graphics.ColorSpace;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.List;

public class NomalDecorator implements DayViewDecorator {

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        Connector.getDatabase();
        List<TurnipValue> values = DataSupport.where("year = ? and month = ? and day = ? ", String.valueOf(day.getYear()),String.valueOf(day.getMonth()+1),String.valueOf(day.getDay())).find(TurnipValue.class);
        if(!values.isEmpty()){
            Log.d("Decorate","find true"+day.getYear()+day.getMonth()+day.getDay());
            return true;
        }
        Log.d("Decorate","sorry"+day.getYear()+day.getMonth()+day.getDay());
        return false;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new DotSpan(5, R.color.dotColor));
    }
}
