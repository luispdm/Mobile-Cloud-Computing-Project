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

public class NewCalendarActivity extends AppCompatActivity {
    private EditText name = null;
    private EditText description = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_calendar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void NewCalendar(View v) {
        name = (EditText) findViewById(R.id.calendarname);
        description = (EditText) findViewById(R.id.calendardescription);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name.getText().toString());
            jsonObject.put("description", description.getText().toString());
        } catch(JSONException e) {
            e.printStackTrace();
        }

        new NewCalendarOperation(this).execute(jsonObject);
    }

}
