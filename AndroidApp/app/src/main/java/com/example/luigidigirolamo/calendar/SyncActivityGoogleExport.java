package com.example.luigidigirolamo.calendar;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;

/**
 * Created by giovanni on 29/11/2015.
 */
public class SyncActivityGoogleExport extends AppCompatActivity {
    private RadioGroup radioGroup = null;

    private GoogleAccountCredential mCredential;
    private RadioGroup mOutputText;
    private TextView messageInfo;
    private ProgressDialog mProgress;
    private Button inport;
    private Context context;
    private Activity activity;
    JSONArray events,calendars;

    String localCalendarId;

    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR_READONLY };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activity = this;
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_sync_export);

        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();

        radioGroup = (RadioGroup) findViewById(R.id.radioGroupLocalCals);
        for (int i = 0; i<calendarIdInfos.getInfos().length(); i++) {
            RadioButton button = new RadioButton(this);
            button.setId(i);
            try {
                button.setText(calendarIdInfos.getInfos().getJSONObject(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if(i==0)
                button.setChecked(true);
            radioGroup.addView(button);
        }

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        // Initialize credentials and service object.
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff())
                .setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
    }

    public void LaunchExportCalendar(View v) {
        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();
        try {
            localCalendarId = calendarIdInfos.getInfos().getJSONObject(radioGroup.getCheckedRadioButtonId()).getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //startActivity(new Intent(this, ExportCalendaActivity.class));
        new MakeRequestTaskToAddCalendar(mCredential, this).execute();
    }

        @Override
        protected void onResume() {
            super.onResume();
            if (isGooglePlayServicesAvailable()) {
                refreshResults();
            } else {
                messageInfo.setText("Google Play Services required: after installing, close and relaunch this a.");
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
                //new MakeRequestTaskToAddCalendar(mCredential, this).execute();
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
                SyncActivityGoogleExport.this,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * An asynchronous task that handles the Google Calendar API call.
     * Placing the API calls in their own task ensures the UI stays responsive.
     */
    public class MakeRequestTaskToAddCalendar extends AsyncTask<Void, Void, String> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;
        private Context context;

        public MakeRequestTaskToAddCalendar(GoogleAccountCredential credential, Context context) {
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
        protected String doInBackground(Void... params) {
            try {
                return createCalendar();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
            }
            return null;
        }

        private String createCalendar() {

            // Create a new calendar
            com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
            calendar.setSummary("Android Inported Calendar");
            calendar.setTimeZone("Helsinki/Europe");

            //mCredential.setSelectedAccountName("mccgroup29@gmail.com");
            // Insert the new calendar
            com.google.api.services.calendar.model.Calendar createdCalendar = new com.google.api.services.calendar.model.Calendar();
            try {
                createdCalendar = mService.calendars().insert(calendar).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }catch (Exception ex){
                ex.printStackTrace();
            }

            return createdCalendar.getId();
        }


        private void addEvent(String calendarId, JSONArray localEvents) {
            for (int i = 0; i<localEvents.length(); i++) {
                try {
                    Event event = new Event()
                            .setSummary(localEvents.getJSONObject(i).getString("name"))
                            .setLocation(localEvents.getJSONObject(i).getString("place"))
                            .setDescription(localEvents.getJSONObject(i).getString("description"));

                    DateTime startDateTime = new DateTime(localEvents.getJSONObject(i).getString("dateStartEvent"));
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone("Helsinki/Europe");
                    event.setStart(start);

                    DateTime endDateTime = new DateTime(localEvents.getJSONObject(i).getString("dateEndEvent"));
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone("Helsinki/Europe");
                    event.setEnd(end);

                    try {
                        event = mService.events().insert(calendarId, event).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPostExecute(String calendarId) {
            URL url = null;
            UserInfos userInfos = UserInfos.getInstance();
            String response = "";
            String address = userInfos.getIpAddress();
            try {
                url = new URL(address + "users/" + userInfos.getUserName() + "/token/" + userInfos.getToken() + "/search");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();


                JSONObject jo = new JSONObject();
                jo.put("calendar", localCalendarId);

                OutputStreamWriter oSW = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                oSW.write(jo.toString());
                oSW.flush();
                oSW.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(urlConnection.getInputStream()));
                String inputLine;
                StringBuffer stringBuffer = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    stringBuffer.append(inputLine);
                }
                String str = stringBuffer.toString();
                JSONArray jsonEvents = new JSONArray(str);
                in.close();

                addEvent(calendarId, jsonEvents);

                return;
            } catch(Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }

}
