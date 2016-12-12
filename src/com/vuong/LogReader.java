package com.vuong;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created by vuong on 08/12/2016.
 */
public class LogReader {

    private String path; //the path to log file
    private int interval; //time interval for each start cycle in ms
    private ConcurrentLinkedDeque<Log> logs;

    // one of the most suitable structure for this case because it is thread-safe (can be accessed by multiple threads at the same time)
    // and it has the queue structure, supports FIFO operations
    // Memory consistency effects: As with other concurrent collections,
    // actions in a thread prior to placing an object into a ConcurrentLinkedQueue happen-before
    // actions subsequent to the access or removal of that element from the ConcurrentLinkedQueue in another thread.

    private Thread readThread=null;
    private Read read;
    private Thread analyseThread=null;
    private Analyse analyse;

    public LogReader(String path) {
        this.path = path;
        this.interval = Config.SECTION_REPORT_INTERVAL;
        this.logs = new ConcurrentLinkedDeque<>();
    }

    public void start(){
        read = new Read();
        readThread = new Thread(read);
        readThread.start();

        analyse = new Analyse();
        analyseThread = new Thread(analyse);
        analyseThread.start();
    }

    public void stop(){
        System.out.println("Stopping process...");
        if (readThread!=null){
            read.terminate(); //terminate the process
            try {
                readThread.join(); //waiting for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This thread read log from an active log file and add it to the queue logs
     */
    private class Read implements Runnable{
        private volatile boolean running = true;
        public void terminate(){
            running = false;
        }
        @Override
        public void run(){
            try {
                //BufferedReader br = new BufferedReader(new FileReader(path));
                //better support for reading lines but can't skip bytes
                //TODO review this
                File file = new File(path);
                RandomAccessFile br = new RandomAccessFile(file, "r");
                br.skipBytes((int)file.length()); //Skip
                String line;
                while (running){
                    if ((line = br.readLine()) != null){
                        try {
                            logs.offer(new Log(line));
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
            HashMap<String, Integer> sections = new HashMap<>(); //storing sections occurrences
            Date startTraffic = new Date();  //Time origin for monitoring traffic
            int totalTraffic=0; //for alerting traffic

            Date startSection = new Date();  //Time origin for monitoring section
            int traffic = 0;    //for reporting traffic while monitoring section
            String section="";  //section with the most hits
            int maxSectionHit = 0;//biggest number of hits

            boolean alert = false;  // true if an alert has been showing
            boolean running = true; // analysing loop

            /*boolean iniFinish = false; //initializing loop
            while (!iniFinish){
                Log firstLog = logs.peek();
                if (firstLog!=null){ // if the logs queue isn't empty, initialize the start time by the head of the queue
                    startSection = firstLog.getTime();
                    startTraffic = firstLog.getTime();
                    iniFinish=true;
                } else {
                    try {
                        Thread.sleep(interval+1); // (interval + 1) assures that the reader has added some elements to  the queue
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        iniFinish = true;
                    }
                }
            }*/

            while(running){
                Log l = logs.poll(); // for each loop we get the log at the head of the queue
                if (l!=null){
                    // monitoring section and common traffic
                    // the condition new Date().getTime()-startSection.getTime() > Config.SECTION_REPORT_INTERVAL assures that a notification is triggered every 10s but the report result has a slight difference with the reality
                    //if this difference is regular, than it is acceptable, but if this difference diverge -> trouble trouble
                    if (l.getTime().getTime()-startSection.getTime() > Config.SECTION_REPORT_INTERVAL) {

                        // end of a cycle. Print report :
                        report(traffic, startSection, l.getTime(), section, maxSectionHit);

                        //re-initialized the traffic, start time, section count and sections hash map
                        startSection = new Date();
                        traffic = 0;
                        sections.clear();
                        section = "";
                        maxSectionHit=0;
                    } else {
                        traffic ++; //increment traffic count
                        //save section information
                        if(sections.containsKey(l.getSection())) {
                            int sectionHit=sections.get(l.getSection());
                            if (sectionHit +1 > maxSectionHit) {
                                maxSectionHit = sectionHit + 1;
                                section = l.getSection();
                            }
                            sections.put(l.getSection(), sectionHit+1);
                        } else {
                            sections.put(l.getSection(), 1);
                        }
                    }

                    //check time and counting traffic
                    //difference of time in ms
                    //problem if the write and read queue process doesn't fast enough
                    if (l.getTime().getTime()-startTraffic.getTime() > Config.CHECK_TRAFFIC_INTERVAL ) {
                        System.out.println("Traffic over the pass 2min :"+totalTraffic);
                        if (totalTraffic>Config.TRAFFIC_LIMIT){
                            showTrafficAlert(totalTraffic, l.getTime());
                            alert = true;
                        }
                        if (totalTraffic<Config.TRAFFIC_LIMIT && alert){
                            showTrafficRecovered(totalTraffic, l.getTime());
                            alert = false;
                        }
                        startTraffic = l.getTime();
                        totalTraffic =0;
                    } else {
                        totalTraffic++;
                    }
                } else {
                    if (new Date().getTime()-startSection.getTime() > Config.SECTION_REPORT_INTERVAL) {
                        //Force to report
                        report(traffic, startSection, new Date(), section, maxSectionHit);

                        //re-initialized the traffic, start time, section count and sections hash map
                        startSection = new Date();
                        traffic = 0;
                        sections.clear();
                        section = "";
                        maxSectionHit = 0;
                    }
                    if (new Date().getTime()-startTraffic.getTime() > Config.CHECK_TRAFFIC_INTERVAL) {
                        //Force to report
                        System.out.println("Traffic over the pass 2min :"+ totalTraffic);
                        if (totalTraffic>Config.TRAFFIC_LIMIT){
                            showTrafficAlert(totalTraffic, new Date());
                            alert = true;
                        }
                        if (totalTraffic<Config.TRAFFIC_LIMIT && alert){
                            showTrafficRecovered(totalTraffic, new Date());
                            alert = false;
                        }
                        startTraffic = new Date();
                        totalTraffic =0;

                    }
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

        public void report(int traffic, Date start, Date end, String section , int maxSectionHit){
            System.out.println("Traffic from "+start+ " to "+end+" :   "+traffic);
            System.out.println("Most visited section: "+section+ " number of hits :" +maxSectionHit);
        }

        public void showTrafficAlert(int totalTraffic, Date d){
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            System.out.println("High traffic generated an alert - hits = "+ totalTraffic+", triggered at " + mFormat.format(d));
        }

        public void showTrafficRecovered(int totalTraffic, Date d){
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            System.out.println("Traffic recovered - hits = "+ totalTraffic+", recovered at " + mFormat.format(d));
        }
    }
}
