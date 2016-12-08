package com.vuong;

/**
 * Created by vuong on 08/12/2016.
 */
public class Config {
    public final static int CHECK_TRAFFIC_INTERVAL = 2*60*100; //check for high traffic avery 2 min
    public final static int SECTION_REPORT_INTERVAL = 10*1000; // report on most hit section every 10s
    public final static int TRAFFIC_LIMIT = 150; // limit for triggering an alert on traffic
}
