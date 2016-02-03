package com.example.luigidigirolamo.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONException;

public class CalendarsActivity extends AppCompatActivity {
    private RadioGroup radioGroup = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendars);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();

        radioGroup = (RadioGroup) findViewById(R.id.radioGroupCals);
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
    }

    public void LaunchNewCalendar(View v) {
        startActivity(new Intent(this, NewCalendarActivity.class));
    }

    public void LaunchUpdateCalendar(View v) {
        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();
        try {
            UpdateCalendarActivity.calendarId = calendarIdInfos.getInfos().getJSONObject(radioGroup.getCheckedRadioButtonId()).getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(new Intent(this, UpdateCalendarActivity.class));
    }

}
