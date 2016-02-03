package com.example.luigidigirolamo.calendar;

/**
 * Created by giovanni on 28/11/2015.
 */

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;

import com.google.api.services.calendar.*;
import com.google.api.client.util.DateTime;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SyncActivityGoogle extends Activity {
    private GoogleAccountCredential mCredential;
    private RadioGroup mOutputText;
    private TextView messageInfo;
    private ProgressDialog mProgress;
    private Button inport;
    private Context context;
    private Activity activity;
    JSONArray events,calendars;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    /**
     * Create the main activity.
     * @param savedInstanceState previously saved instance data.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;

        super.onCreate(savedInstanceState);
        LinearLayout activityLayout = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        activityLayout.setLayoutParams(lp);
        activityLayout.setOrientation(LinearLayout.VERTICAL);
        activityLayout.setPadding(16, 16, 16, 16);

        ViewGroup.LayoutParams tlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        messageInfo = new TextView(this);
        activityLayout.addView(messageInfo);

        mOutputText = new RadioGroup(this);
        mOutputText.setLayoutParams(tlp);
        mOutputText.setPadding(16, 16, 16, 16);
        mOutputText.setVerticalScrollBarEnabled(true);
        activityLayout.addView(mOutputText);

        inport = new Button(this);
        inport.setText("Inport");

        inport.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                for (int i = 0; i<events.length(); i++) {
                    try {
                       if(events.getJSONObject(i).getString("calendarId").compareTo(calendars.getJSONObject(mOutputText.getCheckedRadioButtonId()).getString("id")) == 0){
                           new NewEventOperation(activity).execute(events.getJSONObject(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        activityLayout.addView(inport);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        setContentView(activityLayout);

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    /**
     * Called whenever this activity is pushed to the foreground, such as after
     * a call to onCreate().
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            refreshResults();
        } else {
            messageInfo.setText("Google Play Services required: after installing, close and relaunch this app.");
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    isGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        SharedPreferences settings =
                                getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    messageInfo.setText("Account unspecified.");
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode != RESULT_OK) {
                    chooseAccount();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Attempt to get a set of data from the Google Calendar API to display. If the
     * email address isn't known yet, then call chooseAccount() method so the
     * user can pick an account.
     */
    private void refreshResults() {
        if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else {
            if (isDeviceOnline()) {
                    //events = new MakeRequestTask(mCredential, this).execute().get();
                    new MakeRequestTask(mCredential, this).execute();
            } else {
                messageInfo.setText("No network connection available.");
            }
        }
    }

    /**
     * Starts an activity in Google Play Services so the user can pick an
     * account.
     */
    private void chooseAccount() {
        startActivityForResult(
                mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date. Will
     * launch an error dialog for the user to update Google Play Services if
     * possible.
     * @return true if Google Play Services is available and up to
     *     date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        final int connectionStatusCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        } else if (connectionStatusCode != ConnectionResult.SUCCESS ) {
            return false;
        }
        return true;
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     * @param connectionStatusCode code describing the presence (or lack of)
     *     Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                connectionStatusCode,
                SyncActivityGoogle.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTask extends AsyncTask<Void, Void, JSONArray> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private Context context;

        public MakeRequestTask(GoogleAccountCredential credential, Context context) {
            this.context = context;
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         * @param params no parameters needed for this task.
         */
        @Override
        protected JSONArray doInBackground(Void... params) {
            try {
                return getCalendarFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        /**
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private JSONArray getDataFromApi() throws IOException {
            // List the next 10 events from the primary calendar.
            JSONArray eventJSON = new JSONArray();
            Events events = mService.events().list("primary")
                    .setTimeMin(DateTime.parseRfc3339("2015-01-01T00:00:00.000+02:00"))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();

            for (Event event : items) {
                DateTime start = event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = event.getStart().getDate();
                }

                JSONObject jo = new JSONObject();
                try {
                    jo.put("name", event.getSummary());
                    jo.put("description", event.getDescription());
                    jo.put("place", event.getLocation());
                    jo.put("dateEndEvent", event.getEnd().getDateTime());
                    jo.put("dateStartEvent", start);
                    jo.put("calendarId", event.getLocation());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                eventJSON.put(jo);
            }
            return eventJSON;
        }

        private JSONArray getCalendarFromApi() throws IOException {
            DateTime now = new DateTime(System.currentTimeMillis());
            JSONArray calendarJson = new JSONArray();
            String pageToken = null;
            JSONArray eventJSON = new JSONArray();
            CalendarList calendarList = mService.calendarList().list()
                    .setPageToken(pageToken)
                    .execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                JSONObject jo = new JSONObject();
                try {
                    jo.put("name", calendarListEntry.getSummary());
                    jo.put("id", calendarListEntry.getId());

                    Events events = mService.events().list("primary")
                            .setTimeMin(DateTime.parseRfc3339("2015-01-01T00:00:00.000+02:00"))
                            .setCalendarId(calendarListEntry.getId())
                            .setOrderBy("startTime")
                            .setSingleEvents(true)
                            .execute();
                    List<Event> Eventitems = events.getItems();

                    for (Event event : Eventitems) {
                        DateTime start = event.getStart().getDateTime();
                        if (start == null) {
                            // All-day events don't have start times, so just use
                            // the start date.
                            start = event.getStart().getDate();
                        }

                        JSONObject joE = new JSONObject();
                        try {
                            joE.put("name", event.getSummary());
                            joE.put("description", event.getDescription());
                            joE.put("place", event.getLocation());
                            joE.put("dateEndEvent", event.getEnd().getDateTime());
                            joE.put("dateStartEvent", start);
                            joE.put("calendarId", calendarListEntry.getId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        eventJSON.put(joE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                calendarJson.put(jo);
            }

            events = eventJSON;
            calendars = calendarJson;

            return eventJSON;
        }


        @Override
        protected void onPreExecute() {
            messageInfo.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(JSONArray output) {
            mProgress.hide();
            if (output == null) {
                messageInfo.setText("No results returned.");
            } else {
                messageInfo.setText("Data retrieved using the Google Calendar API:");
                RadioButton button;
                for (int i = 0; i<output.length(); i++) {
                    button = new RadioButton(context);
                    try {
                        button.setText(output.getJSONObject(i).getString("name"));
                        button.setId(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mOutputText.addView(button);
                }

            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            SyncActivityGoogle.REQUEST_AUTHORIZATION);
                } else {
                    messageInfo.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                messageInfo.setText("Request cancelled.");
            }
        }
    }
}