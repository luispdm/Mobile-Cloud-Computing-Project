package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giovanni on 29/11/2015.
 */
public class SearchEventOperation extends AsyncTask<JSONObject, Void, JSONArray> {
    Activity activity;
    private EventsAdapter adapter;

    public SearchEventOperation(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected JSONArray doInBackground(JSONObject... params) {
        URL url = null;
        UserInfos uI = UserInfos.getInstance();
        String address = uI.getIpAddress();
        String response = "";
        JSONArray jsonEvents = null;
        try {
            url = new URL(address+"users/"+uI.getUserName()+"/token/"+uI.getToken()+"/search");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setConnectTimeout(10000);
            urlConnection.connect();

            if(params[0] != null) {
                OutputStreamWriter oSW = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                oSW.write(params[0].toString());
                oSW.flush();
                oSW.close();
            }BufferedReader in = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String inputLine;
            StringBuffer stringBuffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                stringBuffer.append(inputLine);
            }
            String str = stringBuffer.toString();
            jsonEvents = new JSONArray(str);
            in.close();



        } catch(MalformedURLException mUE) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonEvents;
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        List<LocalEvent> event = new ArrayList<LocalEvent>();


        for (int i = 0; i < result.length(); i++) {
            try {
                String name = result.getJSONObject(i).getString("title");
                String startEvent = result.getJSONObject(i).getString("start");
                String dateEndEvent = result.getJSONObject(i).getString("end");
                //String id = result.getJSONObject(i).getString("id");

                LocalEvent newEvent = new LocalEvent(name, startEvent, dateEndEvent, "");
                event.add(newEvent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        adapter = new EventsAdapter(event, activity);
        ListView lv = (ListView)activity.findViewById(R.id.listLocalEvent);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                //faccio la query al calendario locale
                LocalEvent i = adapter.getItem(position);

                CalendarsUtils.insertEvent(i, activity);

                Toast.makeText(activity, adapter.getItem(position).getTitle() + " Imported", Toast.LENGTH_LONG).show();
            }
        });


    }
}
