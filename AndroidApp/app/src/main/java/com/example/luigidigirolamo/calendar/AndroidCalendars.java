package com.example.luigidigirolamo.calendar;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by luigidigirolamo on 28/11/15.
 */
public class AndroidCalendars extends AsyncTask<Void, Void, ArrayList<Long>> {
    Activity activity;
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    String selection = "(" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?)";
    String[] selectionArgs = new String[]{ "com.google"};

    public AndroidCalendars(Activity activity) {
        this.activity = activity;
    }

    @Override
    protected ArrayList<Long> doInBackground(Void... params) {
        ArrayList<Long> calendarIdList = new ArrayList<Long>();
        try {
            Cursor cur = activity.getApplicationContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                    EVENT_PROJECTION, selection, selectionArgs, null);
            for(int i=0; i<cur.getCount(); i++) {
                long calID = 0;
                String displayName = null;
                String accountName = null;
                String ownerName = null;

                // Get the field values
                calID = cur.getLong(PROJECTION_ID_INDEX);
                displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);

                calendarIdList.add(calID);

            }
        } catch(SecurityException e) {
            e.printStackTrace();
        }
        return calendarIdList;
    }


    protected void onPostExecute(JSONArray jsonArray) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                SyncActivity.arrayAdapter.add(jsonArray.getJSONObject(i).getString("name"));
            }
        } catch(JSONException e) {
            e.printStackTrace();
        }
        SyncActivity.done = true;
    }
}
