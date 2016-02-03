package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by luigidigirolamo on 25/11/15.
 */
public class DeleteEventOperation extends AsyncTask<Void, Void, String> {
    Activity activity;

    public DeleteEventOperation(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(Void... params) {
        URL url = null;
        UserInfos uI = UserInfos.getInstance();
        String address = uI.getIpAddress();
        String response = "";
        try {
            url = new URL(address+"users/"+uI.getUserName()+"/token/"+uI.getToken()+"/events/"+ShowEventOperation.eventId);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return "SUCCESS";
            }
            else if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                return "BAD CONN";
            }
        } catch(MalformedURLException mUE) {
            return "BADURL";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "FAILURE";
    }

    @Override
    protected void onPostExecute(String result) {
        if(result == "SUCCESS") {
            activity.startActivity(new Intent(activity, UserIndex.class));
        }
        else{
            if(result == "BAD CONN") {
                Toast.makeText(activity, "The server is not responding", Toast.LENGTH_SHORT).show();
            }
            else if(result == "BADURL") {
                Toast.makeText(activity, "The URL is not correct", Toast.LENGTH_SHORT).show();
            }
            else if(result == "FAILURE") {
                Toast.makeText(activity, "Network connection missing", Toast.LENGTH_SHORT).show();
            }
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
    }
}
