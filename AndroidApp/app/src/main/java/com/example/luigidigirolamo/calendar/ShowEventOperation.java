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
public class ShowEventOperation extends AsyncTask <Void, Void, JSONObject> {
    Activity activity;
    public static String eventId;

    public ShowEventOperation(Activity activity) {
        this.activity = activity;
    }


    @Override
    protected JSONObject doInBackground(Void... params) {
        URL url = null;
        UserInfos userInfos = UserInfos.getInstance();
        String response = "";
        String address = userInfos.getIpAddress();
        try {
            url = new URL(address+"users/"+userInfos.getUserName()+"/token/"+userInfos.getToken()+"/events/"+eventId);
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
            EditEventActivity.calendarIdToCheck = jsonObject.getString("calendar");
            return jsonObject;
        } catch(Exception e) {
            e.printStackTrace();
            EditEventActivity.calendarIdToCheck = "";
            return null;
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        try {
            Date dateStart;
            Date dateEnd;
            EditEventActivity.eventName.setText(jsonObject.getString("name"));
            EditEventActivity.description.setText(jsonObject.getString("description"));
            EditEventActivity.place.setText(jsonObject.getString("place"));

            dateStart = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(jsonObject.getString("dateStartEvent").substring(0,jsonObject.getString("dateStartEvent").length()-1));
            dateEnd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(jsonObject.getString("dateEndEvent").substring(0,jsonObject.getString("dateEndEvent").length()-1));

            EditEventActivity.startDate.setText(new SimpleDateFormat("dd/MM/yyy").format(dateStart));
            EditEventActivity.endDate.setText(new SimpleDateFormat("dd/MM/yyy").format(dateEnd));
            EditEventActivity.startTime.setText(new SimpleDateFormat("HH:mm").format(dateStart));
            EditEventActivity.endTime.setText(new SimpleDateFormat("HH:mm").format(dateEnd));
            //EditEventActivity.calendarIdToCheck = jsonObject.getString("calendar");
            //EditEventActivity.radioGroup.
        } catch (Exception e) {
            e.printStackTrace();
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
    }
}
