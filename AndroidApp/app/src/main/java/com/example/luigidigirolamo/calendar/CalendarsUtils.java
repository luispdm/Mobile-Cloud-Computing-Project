package com.example.luigidigirolamo.calendar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by giovanni on 29/11/2015.
 */
public class CalendarsUtils {
    public static final String[] CALENDAR_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Events.TITLE,                           // 0
            CalendarContract.Events._ID,                  // 1
            CalendarContract.Events.DESCRIPTION,                      // 2
            CalendarContract.Events.DTSTART,                       // 3
            CalendarContract.Events.DTEND                           // 4
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
    static String selection = "(" + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?)";
    static String[] selectionArgs = new String[]{ "com.google"};

    private static final int EVENT_PROJECTION_TITLE_INDEX = 0;
    private static final int EVENT_PROJECTION_ID_INDEX = 1;
    private static final int EVENT_PROJECTION_DESCRIPTION_INDEX = 2;
    private static final int EVENT_PROJECTION_DTSTART_INDEX = 3;
    private static final int EVENT_PROJECTION_DTEND_INDEX = 4;

    static String eventselection = "(" + CalendarContract.Events.CALENDAR_ID + " = ?)";


    public static List<LocalEvent> getDeviceEvent(Context c){

        ArrayList<Long> calendarIdList = new ArrayList<Long>();
        try {
            Cursor cur = c.getApplicationContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                    CALENDAR_PROJECTION, selection, selectionArgs, null);
            while (cur.moveToNext()) {
                long calID = 0;

                // Get the field values
                calID = cur.getLong(PROJECTION_ID_INDEX);
                String name = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);

                calendarIdList.add(calID);

            }
        } catch(SecurityException e) {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }

        ArrayList<LocalEvent> listaEventi = new ArrayList<LocalEvent>();


        for (long id: calendarIdList ) {
            String[] eventselectionArgs = new String[]{id+""};
            try {
                Cursor cur = c.getApplicationContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                        EVENT_PROJECTION, eventselection, eventselectionArgs, null);
                while (cur.moveToNext()) {
                    String title = cur.getString(EVENT_PROJECTION_TITLE_INDEX);
                    long start = cur.getLong(EVENT_PROJECTION_DTSTART_INDEX);
                    long end =cur.getLong(EVENT_PROJECTION_DTEND_INDEX);

                    LocalEvent newEvent = new LocalEvent(title,
                            LocalEvent.df.format(new Date(start)),
                            LocalEvent.df.format(new Date(end)),
                            cur.getString(EVENT_PROJECTION_ID_INDEX)
                            );

                    listaEventi.add(newEvent);

                }
            } catch(SecurityException e) {
                e.printStackTrace();
            }
        }
        return listaEventi;
    }


    public static List<LocalEvent> getSearchEvent(Context c){
        ArrayList<LocalEvent> listaEventi = new ArrayList<LocalEvent>();
        return listaEventi;
    }

    public static void insertEvent(LocalEvent newEvent, Context c){
        ArrayList<Long> calendarList = getCalendarList(c);

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, newEvent.getRawDataStart().getTime());
        values.put(CalendarContract.Events.DTEND, newEvent.getRawDataEnd().getTime());
        values.put(CalendarContract.Events.TITLE, newEvent.getTitle());
        values.put(CalendarContract.Events.DESCRIPTION, "");
        values.put(CalendarContract.Events.CALENDAR_ID, calendarList.get(0));
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "Europe/Helsinki");
        try {
            Uri uri = c.getApplicationContext().getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
            long eventID = Long.parseLong(uri.getLastPathSegment());
        } catch(SecurityException e) {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }


    }

    public static ArrayList<Long> getCalendarList(Context c){
        ArrayList<Long> calendarIdList = new ArrayList<Long>();
        try {
            Cursor cur = c.getApplicationContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                    CALENDAR_PROJECTION, selection, selectionArgs, null);
            while (cur.moveToNext()) {
                long calID = 0;

                // Get the field values
                calID = cur.getLong(PROJECTION_ID_INDEX);

                calendarIdList.add(calID);

            }
        } catch(SecurityException e) {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }
        return calendarIdList;
    }

    public static ArrayList<LocalEvent> getEventList(JSONObject parameter, Context c){

        ArrayList<LocalEvent> listaEventi = new ArrayList<LocalEvent>();
        try {
            String selection = new String();
            String name = parameter.get("name").toString();
            if (name.compareTo("") == 0){
                selection = "(( " + CalendarContract.Events.DTSTART + " >= " + parameter.get("dateStartEvent");
                if(parameter.get("dateEndEvent").toString().compareTo("") == 0){
                    selection = selection + " ))";
                }else{
                    selection = selection + " ) AND ( " + CalendarContract.Events.DTEND + " <= " +
                            parameter.get("dateEndEvent") + " ))";
                }

            }else if(parameter.get("dateStartEvent").toString().compareTo("") == 0){

                selection = "(  " + CalendarContract.Events.TITLE + " = '" + name + "' )";
            }else{
                selection = "(( " + CalendarContract.Events.DTSTART + " >= " + parameter.get("dateStartEvent") +
                        " ) AND ( " + CalendarContract.Events.DTEND + " <= " + parameter.get("dateEndEvent") +
                        "  ) AND ( " + CalendarContract.Events.TITLE + " =  '" + name + "' ))";
            }

            Cursor cur = c.getApplicationContext().getContentResolver().query(CalendarContract.Events.CONTENT_URI,
                    EVENT_PROJECTION, selection, null, null);
            while (cur.moveToNext()) {
                String title = cur.getString(EVENT_PROJECTION_TITLE_INDEX);
                long start = cur.getLong(EVENT_PROJECTION_DTSTART_INDEX);
                long end = cur.getLong(EVENT_PROJECTION_DTEND_INDEX);

                LocalEvent newEvent = new LocalEvent(title,
                        LocalEvent.df.format(new Date(start)),
                        LocalEvent.df.format(new Date(end)),
                        cur.getString(EVENT_PROJECTION_ID_INDEX)
                );

                listaEventi.add(newEvent);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }catch(SecurityException e) {
            e.printStackTrace();
        }catch(Exception e) {
            e.printStackTrace();
        }

    return listaEventi;

    }
}
