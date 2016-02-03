package com.example.luigidigirolamo.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SyncActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_sync);

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

                TimePickerDialog mTimePicker = new TimePickerDialog(SyncActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

                TimePickerDialog mTimePicker = new TimePickerDialog(SyncActivity.this, new TimePickerDialog.OnTimeSetListener() {
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

                DatePickerDialog mDatePicker = new DatePickerDialog(SyncActivity.this, new DatePickerDialog.OnDateSetListener() {
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

                DatePickerDialog mDatePicker = new DatePickerDialog(SyncActivity.this, new DatePickerDialog.OnDateSetListener() {
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

        List<LocalEvent> event = CalendarsUtils.getDeviceEvent(this);

        adapter = new EventsAdapter(event, this);
        ListView lv = (ListView)findViewById(R.id.importListView);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(SyncActivity.this, adapter.getItem(position).getTitle(), Toast.LENGTH_LONG).show();

                JSONObject parameter = new JSONObject();
                try {
                    parameter.put("name", adapter.getItem(position).getTitle());
                    parameter.put("dateEndEvent", adapter.getItem(position).getEnddate());
                    parameter.put("dateStartEvent", adapter.getItem(position).getStartdate());
                    parameter.put("stringa", "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new NewEventOperation(SyncActivity.this).execute(parameter);
                Toast.makeText(SyncActivity.this, adapter.getItem(position).getTitle()+" Imported", Toast.LENGTH_LONG).show();
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

        ArrayList<LocalEvent> listaEventi = CalendarsUtils.getEventList(parameter, SyncActivity.this);


        adapter = new EventsAdapter(listaEventi, this);
        ListView lv = (ListView)findViewById(R.id.importListView);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Toast.makeText(SyncActivity.this, adapter.getItem(position).getTitle(), Toast.LENGTH_LONG).show();

                JSONObject parameter = new JSONObject();
                try {
                    parameter.put("name", adapter.getItem(position).getTitle());
                    parameter.put("dateEndEvent", adapter.getItem(position).getEnddate());
                    parameter.put("dateStartEvent", adapter.getItem(position).getStartdate());
                    parameter.put("stringa", "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new NewEventOperation(SyncActivity.this).execute(parameter);
                Toast.makeText(SyncActivity.this, adapter.getItem(position).getTitle()+" Imported", Toast.LENGTH_LONG).show();
            }
        });

    }

}
