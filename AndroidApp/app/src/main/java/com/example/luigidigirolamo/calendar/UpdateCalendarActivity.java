package com.example.luigidigirolamo.calendar;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateCalendarActivity extends AppCompatActivity {
    public static EditText name = null;
    public static EditText description = null;
    public static String calendarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = (EditText) findViewById(R.id.editnamecalendar);
        description = (EditText) findViewById(R.id.editdescriptioncalendar);

        new ShowCalendarOperation(this).execute();
    }

    public void UpdateCalendar(View v) {
        name = (EditText) findViewById(R.id.editnamecalendar);
        description = (EditText) findViewById(R.id.editdescriptioncalendar);
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("name", name.getText().toString());
            parameter.put("description", description.getText().toString());
            new UpdateCalendarOperation(this).execute(parameter);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
    public void DeleteCalendar(View v) {
        new DeleteCalendarOperation(this).execute();
    }
}
