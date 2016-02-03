package com.example.luigidigirolamo.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EditEventActivity extends AppCompatActivity {
    public static EditText eventName = null;
    public static EditText description = null;
    public static EditText place = null;
    public static EditText startDate = null;
    public static EditText startTime = null;
    public static EditText endDate = null;
    public static EditText endTime = null;
    public static String calendarSelected;
    public static String calendarIdToCheck = null;
    public static RadioGroup radioGroup = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        eventName = (EditText) findViewById(R.id.Name);
        description = (EditText) findViewById(R.id.Description);
        place = (EditText) findViewById(R.id.Place);
        startDate = (EditText) findViewById(R.id.StartDate);
        endDate = (EditText) findViewById(R.id.EndDate);
        startTime = (EditText) findViewById(R.id.Starttime);
        endTime = (EditText) findViewById(R.id.Endtime);
        calendarIdToCheck=null;

        new ShowEventOperation(this).execute();

        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();

        startTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                final Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker = new TimePickerDialog(EditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mcurrentTime.set(Calendar.HOUR, selectedHour);
                        mcurrentTime.set(Calendar.MINUTE, selectedMinute);

                        startTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select time");
                mTimePicker.show();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                final Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker = new TimePickerDialog(EditEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        mcurrentTime.set(Calendar.HOUR, selectedHour);
                        mcurrentTime.set(Calendar.MINUTE, selectedMinute);

                        endTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select time");
                mTimePicker.show();
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                final Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(EditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        mcurrentDate.set(Calendar.YEAR, selectedyear);
                        mcurrentDate.set(Calendar.MONTH, selectedmonth);
                        mcurrentDate.set(Calendar.DAY_OF_MONTH, selectedday);
                        String myFormat = "dd/MM/yyyy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);

                        startDate.setText(sdf.format(mcurrentDate.getTime()));
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                final Calendar mcurrentDate = Calendar.getInstance();
                int mYear = mcurrentDate.get(Calendar.YEAR);
                int mMonth = mcurrentDate.get(Calendar.MONTH);
                int mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(EditEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        mcurrentDate.set(Calendar.YEAR, selectedyear);
                        mcurrentDate.set(Calendar.MONTH, selectedmonth);
                        mcurrentDate.set(Calendar.DAY_OF_MONTH, selectedday);
                        String myFormat = "dd/MM/yyyy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
                        endDate.setText(sdf.format(mcurrentDate.getTime()));
                    }
                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        for (int i = 0; i<calendarIdInfos.getInfos().length(); i++) {
            RadioButton button = new RadioButton(this);
            button.setId(i);
            try {
                button.setText(calendarIdInfos.getInfos().getJSONObject(i).getString("name"));
                do {
                } while(calendarIdToCheck==null);
                if (calendarIdToCheck.compareTo(calendarIdInfos.getInfos().getJSONObject(i).getString("id"))==0)
                    button.setChecked(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            radioGroup.addView(button);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void UpdateEvent(View v) throws JSONException {
        CalendarIdInfos calendarIdInfos = CalendarIdInfos.getInstance();
        JSONObject parameter = new JSONObject();
        Date dateStart = null;
        Date dateEnd = null;
        long millisecondsStart = 0;
        long millisecondsEnd = 0;

        eventName = (EditText) findViewById(R.id.Name);
        description = (EditText) findViewById(R.id.Description);
        place = (EditText) findViewById(R.id.Place);
        startDate = (EditText) findViewById(R.id.StartDate);
        endDate = (EditText) findViewById(R.id.EndDate);
        startTime = (EditText) findViewById(R.id.Starttime);
        endTime = (EditText) findViewById(R.id.Endtime);

        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        try {
            dateStart = format.parse(startDate.getText().toString() + " " + startTime.getText().toString());
            millisecondsStart = dateStart.getTime();
            dateEnd = format.parse(endDate.getText().toString() + " " + endTime.getText().toString());
            millisecondsEnd = dateEnd.getTime();

            calendarSelected = calendarIdInfos.getInfos().getJSONObject(radioGroup.getCheckedRadioButtonId()).getString("id");

            parameter.put("name", eventName.getText().toString());
            parameter.put("description", description.getText().toString());
            parameter.put("place", place.getText().toString());
            parameter.put("dateStartEvent", millisecondsStart+7200000);
            parameter.put("dateEndEvent", millisecondsEnd+7200000);
            parameter.put("calendar", calendarSelected);
        } catch (ParseException e) {

            calendarSelected = calendarIdInfos.getInfos().getJSONObject(radioGroup.getCheckedRadioButtonId()).getString("id");

            parameter.put("name", eventName.getText().toString());
            parameter.put("description", description.getText().toString());
            parameter.put("place", place.getText().toString());
            if(millisecondsStart!=0)
                parameter.put("dateStartEvent", millisecondsStart+7200000);
            else {
                parameter.put("dateStartEvent", "");
            }
            parameter.put("dateEndEvent", "");
            parameter.put("calendar", calendarSelected);
        } catch(JSONException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

        new UpdateEventOperation(this).execute(parameter);
    }

    public void DeleteEvent(View v) {
        new DeleteEventOperation(this).execute();
    }

}
