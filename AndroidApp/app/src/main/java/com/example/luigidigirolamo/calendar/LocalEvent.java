package com.example.luigidigirolamo.calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luigidigirolamo on 18/11/15.
 */
public class LocalEvent {
    private String title;
    private String startdate;
    private String enddate;
    private String id;
    private Date rawDataStart;
    private Date rawDataEnd;
    private Date monthYearStart;
    private Date monthYearEnd;
    public static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public LocalEvent(String title, String startdate, String enddate, String id) {
        Date datestart = null;
        Date dateend = null;
        try {
            datestart = df.parse(startdate.substring(0, startdate.length() - 1));
            dateend = df.parse(enddate.substring(0,enddate.length()-1));
            this.monthYearStart = new SimpleDateFormat("yyyy-MM").parse(startdate.substring(0,startdate.length()-1));
            this.monthYearEnd = new SimpleDateFormat("yyyy-MM").parse(enddate.substring(0,enddate.length()-1));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        startdate = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(datestart);
        enddate = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(dateend);
        this.rawDataStart = datestart;
        this.rawDataEnd = dateend;
        //this.monthYear = new SimpleDateFormat("MM-yyyy").format(datestart);

        this.title = title;
        this.startdate = startdate;
        this.enddate = enddate;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getStartdate() {
        return startdate;
    }
    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }
    public String getEnddate() {
        return enddate;
    }
    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }
    public String getId() {
        return this.id;
    }
    public Date getRawDataStart() {return  this.rawDataStart;}
    public Date getRawDataEnd() {return this.rawDataEnd;}
    public Date getMonthYearStart(){return this.monthYearStart;}
    public Date getMonthYearEnd(){return this.monthYearEnd;}
}
