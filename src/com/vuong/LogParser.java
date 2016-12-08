package com.vuong;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Created by vuong on 08/12/2016.
 */
public class LogParser {

    private final int CHECK_TRAFFIC_INTERVAL = 2*60*100; //check for high traffic avery 2 min
    private final int SECTION_REPORT_INTERVAL = 10*1000; // report on most hit section every 10s
    private final int TRAFFIC_LIMIT = 50; // limit for triggering an alert on traffic
    private final int READ_INTERVAL_DEFAULT = 500; //default read interval is 500ms

    private String path; //the path to log file
    private int interval; //time interval for each read cycle in ms
    Queue<Log> logs;

    public LogParser(String path) {
        this.path = path;
        this.interval = READ_INTERVAL_DEFAULT;
    }

    public void parse(){

    }

    private class Read implements Runnable{

        @Override
        public void run() {

        }
    }



}
