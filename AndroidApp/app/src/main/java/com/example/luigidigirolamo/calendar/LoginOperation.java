package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by luigidigirolamo on 16/11/15.
 */
public class LoginOperation extends AsyncTask<String, Void, String> {
    Activity activity;

    public LoginOperation(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        UserInfos uI = UserInfos.getInstance();
        String response = "";
        String address = params[2];
        try {
            url = new URL(address+"logincheck");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();


            MainActivity.jsonObject.put("username", params[0]);
            MainActivity.jsonObject.put("password", params[1]);

            OutputStreamWriter oSW = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
            oSW.write(MainActivity.jsonObject.toString());
            oSW.flush();
            oSW.close();

            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                uI.setUserName(params[0]);
                uI.setPassword(params[1]);
                uI.setIpAddress(address);
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
                JSONObject jsonResponse = new JSONObject(response);
                uI.setToken(jsonResponse.getString("token"));
                return "SUCCESS";
            }
            else if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_ACCEPTABLE) {
                return "WRONG";
            }
            else if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                return "SERVER ERROR";
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
            if(result == "SERVER ERROR"){
                Toast.makeText(activity, "The server is not responding", Toast.LENGTH_SHORT).show();
            }
            else if(result == "WRONG"){
                Toast.makeText(activity, "Incorrect username and/or password", Toast.LENGTH_SHORT).show();
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
