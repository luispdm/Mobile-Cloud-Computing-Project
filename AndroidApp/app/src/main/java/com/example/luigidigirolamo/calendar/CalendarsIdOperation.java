package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by luigidigirolamo on 25/11/15.
 */
public class CalendarsIdOperation extends AsyncTask<Void, Void, JSONArray> {
    Activity activity;

    public CalendarsIdOperation(Activity activity) {
        this.activity = activity;
    }


    @Override
    protected JSONArray doInBackground(Void... params) {
        URL url = null;
        UserInfos userInfos = UserInfos.getInstance();
        String response = "";
        String address = userInfos.getIpAddress();
        try {
            url = new URL(address+"mobile/"+"users/"+userInfos.getUserName()+"/token/"+userInfos.getToken()+"/calendars");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuffer stringBuffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                stringBuffer.append(inputLine);
            }
            String str = stringBuffer.toString();
            JSONArray jsonCalendars = new JSONArray(str);
            in.close();
            return jsonCalendars;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();
        try {
            calendarIdInfos.setInfos(jsonArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
