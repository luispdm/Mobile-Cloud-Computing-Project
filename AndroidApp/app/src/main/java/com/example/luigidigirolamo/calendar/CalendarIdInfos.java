package com.example.luigidigirolamo.calendar;

import org.json.JSONArray;

import java.util.ArrayList;

/**
 * Created by luigidigirolamo on 25/11/15.
 */
public class CalendarIdInfos {
    private static CalendarIdInfos instance;
    private JSONArray jsonCalInfos;

    public static CalendarIdInfos getInstance() {
        if(instance==null)
            instance = new CalendarIdInfos();
        return instance;
    }

    private CalendarIdInfos() {
    }

    public void setInfos(JSONArray jsonArray) {
        this.jsonCalInfos = jsonArray;
    }
    public JSONArray getInfos() {
        return this.jsonCalInfos;
    }
}
