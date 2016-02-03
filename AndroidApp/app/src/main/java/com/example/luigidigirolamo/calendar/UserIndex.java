package com.example.luigidigirolamo.calendar;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class UserIndex extends AppCompatActivity {
    public static ListView listView;
    public static EventsAdapter adapter;
    public static Button daily;
    public static Button monthly;
    public static List<LocalEvent> totalLocalEvents;
    UserInfos uI = UserInfos.getInstance();
    List<LocalEvent> dailyListToShow;
    String month;
    String year;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_index);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        monthly = (Button) findViewById(R.id.monthlyview);
        daily = (Button) findViewById(R.id.dailyview);
        totalLocalEvents = new ArrayList<LocalEvent>();
        dailyListToShow = new ArrayList<LocalEvent>();


        adapter = new EventsAdapter(new ArrayList<LocalEvent>(), this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                ShowEventOperation.eventId = adapter.getItem(position).getId();
                startActivity(new Intent(getApplicationContext(), EditEventActivity.class));
            }
        });
        listView.setAdapter(adapter);

        new EventsOperation(this).execute();
        new CalendarsIdOperation(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.refresh:
                refresh();
                return true;
            case R.id.newevent:
                newEvent();
                return true;
            case R.id.calendars:
                calendars();
                return true;
            case R.id.sync:
                startActivity(new Intent(this, SyncActivity.class));
                return true;
            case R.id.export:
                startActivity(new Intent(this, SyncExportActivity.class));
                return true;
            case R.id.logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void logout() {
        new LogoutOperation(this).execute();
    }
    public void refresh() {
        new LoginOperation(this).execute(uI.getUserName(), uI.getPassword(), uI.getIpAddress());
    }
    public void newEvent() {
        startActivity(new Intent(this, NewEventActivity.class));
    }
    public void calendars() {
        startActivity(new Intent(this, CalendarsActivity.class));
    }

    public void DailyView(View v) {
        daily.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                final Calendar mStartDate = Calendar.getInstance();
                final Calendar mEndDate = Calendar.getInstance();
                int mYear = mStartDate.get(Calendar.YEAR);
                int mMonth = mStartDate.get(Calendar.MONTH);
                int mDay = mStartDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker = new DatePickerDialog(UserIndex.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                        // TODO Auto-generated method stub
                        setDateFields(mStartDate, selectedyear, selectedmonth+1, selectedday, 00, 00, 00);
                        setDateFields(mEndDate, selectedyear, selectedmonth+1, selectedday, 23, 59, 59);
                        dailyListToShow.clear();
                        for (int i = 0; i < totalLocalEvents.size(); i++) {
                            if (CheckDoubleRange(totalLocalEvents.get(i).getRawDataStart(), totalLocalEvents.get(i).getRawDataEnd(),
                                    mStartDate.getTime(), mEndDate.getTime())) {
                                dailyListToShow.add(totalLocalEvents.get(i));
                            }
                        }
                        adapter.setItemList(dailyListToShow);
                        adapter.notifyDataSetChanged();
                    }

                }, mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });
    }
    public void MonthlyView(View v) {
        final MonthYearPicker monthYearPicker = new MonthYearPicker(this);
        monthYearPicker.build(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Calendar mDateStart = Calendar.getInstance();
                final Calendar mDateEnd = Calendar.getInstance();
                int m = Integer.parseInt(monthYearPicker.getSelectedMonthShortName());
                int y = monthYearPicker.getSelectedYear();
                int d = endDayNumber(m, y);
                setDateFields(mDateStart, y, m, 1, 00, 00, 00);
                setDateFields(mDateEnd, y, m, d, 23, 59, 59);
                dailyListToShow.clear();
                for (int i = 0; i < totalLocalEvents.size(); i++) {
                    if(CheckDoubleRange(totalLocalEvents.get(i).getRawDataStart(), totalLocalEvents.get(i).getRawDataEnd(),
                            mDateStart.getTime(), mDateEnd.getTime())) {
                        dailyListToShow.add(totalLocalEvents.get(i));
                    }
                }
                adapter.setItemList(dailyListToShow);
                adapter.notifyDataSetChanged();
            }
        }, null);
        monthYearPicker.show();
    }

    //support functions for Dates (overlap check, calculating the last day of the month, setting fields for dates)
    public boolean CheckDoubleRange(Date startEvent, Date endEvent, Date startMonth, Date endMonth) {
        if((startEvent.before(endMonth) || startEvent.compareTo(endMonth)==0) &&
                (endEvent.after(startMonth) || endEvent.compareTo(startMonth)==0))
            return true;
        return false;
    }
    public int endDayNumber(int month, int year) {
        if(month==2) {
            if(isLeapYear(year) == true)
                return 29;
            return 28;
        }
        else if(month==11||month==4||month==6||month==9)
            return 30;
        else
            return 31;
    }
    public boolean isLeapYear(int year) {

        if ((year % 400 == 0) || ((year % 4 == 0) && (year % 100 != 0))) {
            return true;
        } else {
            return false;
        }
    }
    public void setDateFields(Calendar date, int y, int m, int d, int h, int min, int s) {
        date.set(Calendar.YEAR, y);
        date.set(Calendar.MONTH, m - 1); //from 0 to 11
        date.set(Calendar.DAY_OF_MONTH, d);
        date.set(Calendar.HOUR_OF_DAY, h);
        date.set(Calendar.MINUTE, min);
        date.set(Calendar.SECOND, s);
    }
}
