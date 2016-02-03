package com.example.luigidigirolamo.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by giovanni on 29/11/2015.
 */
public class SyncExportActivity extends AppCompatActivity {

    public static ArrayAdapter<String> arrayAdapter;
    public static boolean done = false;
    private EventsAdapter adapter;

    EditText eventName = null;
    EditText startDate = null;
    EditText startTime = null;
    EditText endDate = null;
    EditText endTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);

        startDate = (EditText) findViewById(R.id.StartDate);
        endDate = (EditText) findViewById(R.id.EndDate);
        startTime = (EditText) findViewById(R.id.Starttime);
        endTime = (EditText) findViewById(R.id.Endtime);

        startTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                final Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker = new TimePickerDialog(SyncExportActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

                TimePickerDialog mTimePicker = new TimePickerDialog(SyncExportActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

                DatePickerDialog mDatePicker = new DatePickerDialog(SyncExportActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                DatePickerDialog mDatePicker = new DatePickerDialog(SyncExportActivity.this, new DatePickerDialog.OnDateSetListener() {
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
    }

    public void SearchEvent(View v) throws JSONException {
        JSONObject parameter = new JSONObject();
        Date dateStart = null;
        Date dateEnd = null;
        long millisecondsStart = 0;
        long millisecondsEnd = 0;

        eventName = (EditText) findViewById(R.id.Name);
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

            parameter.put("name", eventName.getText().toString());
            parameter.put("dateStartEvent", millisecondsStart+7200000);
            parameter.put("dateEndEvent", millisecondsEnd+7200000);
        } catch (ParseException e) {

            parameter.put("name", eventName.getText().toString());
            if(millisecondsStart!=0)
                parameter.put("dateStartEvent", millisecondsStart+7200000);
            else {
                parameter.put("dateStartEvent", "");
            }
            parameter.put("dateEndEvent", "");
        } catch(JSONException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

        new SearchEventOperation(this).execute(parameter);

    }
}