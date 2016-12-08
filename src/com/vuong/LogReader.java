package com.vuong;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by vuong on 08/12/2016.
 */
public class LogReader {

    private String path; //the path to log file
    private int interval; //time interval for each start cycle in ms
    private ConcurrentLinkedDeque<Log> logs;
    // one of the most suitable structure for this case because it is thread-safe
    // and it has the queue structure, supports FIFO operations
    // Memory consistency effects: As with other concurrent collections,
    // actions in a thread prior to placing an object into a ConcurrentLinkedQueue happen-before
    // actions subsequent to the access or removal of that element from the ConcurrentLinkedQueue in another thread.

    public LogReader(String path) {
        this.path = path;
        this.interval = Config.SECTION_REPORT_INTERVAL;
        this.logs = new ConcurrentLinkedDeque<>();
    }

    public void start(){
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
                //better support for reading lines, but the output is not continued, start blocks
                String line;
                boolean running = true;
                while (running){
                    //TODO READ only from the last line
                    if ((line = br.readLine()) != null){
                        try {
                            logs.add(new Log(line));
                            //System.out.println(l.getTime());
                            //System.out.println(l.getSection());
                        } catch (ParseException e) {
                            e.printStackTrace();
                            br.close();
                            running = false;
                        }
                    }else{
                        try {
                            Thread.sleep(interval);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            br.close();
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

    // Analyse in an other thread so that the reading process isn't disturbed
    private class Analyse implements Runnable{

        @Override
        public void run() {
            int traffic = 0;
            Date startTraffic = null; //start time for monitoring traffic
            Date startSection = null; //start time for monitoring Section
            boolean running = true;
            while(running){
                if (logs.poll()!=null){
                    Log l = logs.poll();
                    if (startSection==null){
                        startSection = l.getTime(); //Initial value, run each time a cycle has finished
                    }
                    if (startTraffic==null){
                        startTraffic =l.getTime();
                    }
                    //check time and counting traffic
                    //difference of time in ms
                    if (l.getTime().getTime()-startTraffic.getTime()<=Config.CHECK_TRAFFIC_INTERVAL){
                        traffic++;
                    } else {
                        startTraffic=null;
                        if (traffic>Config.TRAFFIC_LIMIT){
                            showTrafficAlert(traffic, l.getTime());
                        }
                    }
                } else {
                    //waiting for the reader to add new element
                    try {
                        Thread.sleep(interval+1); // (interval + 1) assures that the reader has add some elements to  the queue
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        running = false;
                    }
                }
            }

        }

        public void showTrafficAlert(int traffic, Date d){
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            System.out.println("High traffic generated an alert - hits = "+ traffic+", triggered at " + mFormat.format(d));
        }
    }
}
