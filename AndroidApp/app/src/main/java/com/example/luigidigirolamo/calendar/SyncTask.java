package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by giovanni on 29/11/2015.
 */

public class SyncTask extends AsyncTask<JSONObject, Void, JSONArray> {
    Activity activity;
    static int calendarId;

    public SyncTask(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected JSONArray doInBackground(JSONObject... params) {
        URL url = null;
        UserInfos userInfos = UserInfos.getInstance();
        String response = "";
        String address = userInfos.getIpAddress();
        try {
            url = new URL(address+"mobile/"+"users/"+userInfos.getUserName()+"/token/"+userInfos.getToken()+"/calendars");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
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

    }
}
