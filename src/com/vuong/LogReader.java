package com.vuong;

import java.io.*;
import java.text.ParseException;

/**
 * Created by vuong on 08/12/2016.
 */
public class LogReader {

    private final int CHECK_TRAFFIC_INTERVAL = 2*60*100; //check for high traffic avery 2 min
    private final int SECTION_REPORT_INTERVAL = 10*1000; // report on most hit section every 10s
    private final int TRAFFIC_LIMIT = 50; // limit for triggering an alert on traffic
    private final int READ_INTERVAL_DEFAULT = 10*1000; //default read interval is 10s

    private String path; //the path to log file
    private int interval; //time interval for each read cycle in ms
    //TODO log queue
    public LogReader(String path) {
        this.path = path;
        this.interval = READ_INTERVAL_DEFAULT;
    }

    public void read(){
        Runnable read = new Read();
        new Thread(read).start();
    }

    public void stop(){
        //TODO stop the thread
    }

    private class Read implements Runnable{
        @Override
        public void run(){
            try {
                BufferedReader br = new BufferedReader(new FileReader(path));
                String line;
                boolean running = true;
                while (running){
                    if ((line = br.readLine()) != null){
                        try {
                            Log l = new Log(line);
                            System.out.println(l.getTime());
                            System.out.println(l.getSection());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            running = false;
                        }
                    }else{
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            running = false;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                //TODO
            }
        }
    }



}
