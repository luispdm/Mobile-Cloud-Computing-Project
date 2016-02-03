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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luigidigirolamo on 25/11/15.
 */
public class ShowCalendarOperation extends AsyncTask<Void, Void, JSONObject> {
    Activity activity;

    public ShowCalendarOperation(Activity activity) {
        this.activity = activity;
    }


    @Override
    protected JSONObject doInBackground(Void... params) {
        URL url = null;
        UserInfos userInfos = UserInfos.getInstance();
        String response = "";
        String address = userInfos.getIpAddress();
        try {
            url = new URL(address+"users/"+userInfos.getUserName()+"/token/"+userInfos.getToken()+"/calendars/"+UpdateCalendarActivity.calendarId);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            in.close();
            return jsonObject;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if(jsonObject != null){
            try {
                UpdateCalendarActivity.name.setText(jsonObject.getString("name"));
                UpdateCalendarActivity.description.setText(jsonObject.getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            activity.startActivity(new Intent(activity, MainActivity.class));
        }

    }
}
