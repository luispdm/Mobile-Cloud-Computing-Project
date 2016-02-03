package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LogoutOperation extends AsyncTask<Void, Void, String> {
    Intent intent;
    Activity activity;

    public LogoutOperation(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        URL url = null;
        UserInfos uI = UserInfos.getInstance();
        String address = uI.getIpAddress();
        String response = "";
        try {
            url = new URL(address+"users/"+uI.getUserName()+"/token/"+uI.getToken()+"/logout");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                uI.setIpAddress("");
                uI.setToken("");
                uI.setUserName("");
                uI.setPassword("");
                return "OK";
            }
            else if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                return "SERVER ERROR";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FAILURE";
    }

    @Override
    protected void onPostExecute(String result) {
        if(result == "OK") {
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
        else{
            if(result == "SERVER ERROR") {
                Toast.makeText(activity, "The server is down", Toast.LENGTH_SHORT).show();
            }
            else if(result == "FAILURE") {
                Toast.makeText(activity, "Network connection missing", Toast.LENGTH_SHORT).show();
            }
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
    }
}