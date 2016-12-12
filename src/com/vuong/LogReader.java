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

    /*
     * one of the most suitable structure for stocking logs in this case is ConcurrentLinkedDeque
     * because it is thread-safe (can be accessed by multiple threads at the same time)
     * and it has the queue structure, supports FIFO operations
     */
    private ConcurrentLinkedDeque<Log> logs;

    private Thread readThread=null;
    private Read read;
    private Thread analyseThread=null;
    private Analyse analyse;

    public LogReader(String path) {
        this.path = path;
        this.logs = new ConcurrentLinkedDeque<>();
    }

    /**
     * Start reading and analysing logs
     */
    public void start(){
        read = new Read();
        readThread = new Thread(read);
        readThread.start();

        analyse = new Analyse();
        analyseThread = new Thread(analyse);
        analyseThread.start();
    }

    /**
     * properly stopping all threads
     */
    public void stop(String mes){
        System.out.println(mes);
        System.out.println("Stop reading process...");
        if (readThread!=null){
            read.terminate(); //terminate the process
            try {
                readThread.join(); //waiting for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (analyseThread!=null){
            analyse.terminate(); //terminate the process
            try {
                analyseThread.join(); //waiting for the thread to finish
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Reading process stopped");
    }

    /**
     * Read log from an active log file and add it to the queue logs
     */
    private class Read implements Runnable{
        private volatile boolean running = true;

        public void terminate(){
            running = false;
        }

        @Override
        public void run(){
            try {
                //better than BufferReader because of skip bytes method
                File file = new File(path);
                RandomAccessFile fileReader = new RandomAccessFile(file, "r"); //read only
                fileReader.skipBytes((int)file.length()); //Skip and read from the end of the file

                String line;
                while (running){
                    if ((line = fileReader.readLine()) != null){
                        try {
                            logs.offer(new Log(line));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            fileReader.close();
                            stop("Invalid log format");
                        }
                    }else{
                        try {
                            Thread.sleep(Config.SECTION_REPORT_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            fileReader.close();
                            stop("Thread interrupted");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                stop("Log file can not be accessed");
            }
        }
    }

    /**
     * Analyse the logs queue in an other thread so that the reading process isn't disturbed
     */
    private class Analyse implements Runnable {
        private volatile boolean running = true;

        public void terminate(){running = false;}

        @Override
        public void run() {
            HashMap<String, Integer> sections = new HashMap<>(); //storing sections occurrences
            HashMap<String, Integer> ips = new HashMap<>(); //storing ip occurrences

            Date startTraffic = new Date();  //Time origin for monitoring traffic
            int alertTraffic=0; //traffic count
            String ip="";  //ip with the most access
            int maxIpHit = 0;//biggest number of access

            Date startSection = new Date();  //Time origin for monitoring section
            int traffic = 0;    //for reporting traffic while monitoring section
            String section="";  //section with the most hits
            int maxSectionHit = 0;//biggest number of hits

            boolean alert = false;  // true if an alert has been showing

            while(running){
                Log l = logs.poll(); // for each loop we get the log at the head of the queue

                //If the logs queue is not empty, keep analysing until we meet a log that out of time cycle
                if (l!=null){
                    //=======monitoring section and common traffic==========
                    if (l.getTime().getTime()-startSection.getTime() > Config.SECTION_REPORT_INTERVAL) {
                        //The log is out of the current cycle
                        report(traffic, startSection, l.getTime(), section, maxSectionHit); // end of a cycle, print report

                        //re-initialized variables, including the current log
                        startSection = l.getTime();
                        traffic = 1;
                        sections.clear();
                        sections.put(l.getSection(),1);
                        section = l.getSection();
                        maxSectionHit=1;

                    } else {
                        traffic ++; //increment traffic count

                        //increment section hash map
                        int sectionHit; //number of hits to be written in hash map
                        if(sections.containsKey(l.getSection())) sectionHit= sections.get(l.getSection())+1;
                        else sectionHit =1;
                        if (sectionHit > maxSectionHit) {
                            maxSectionHit = sectionHit;
                            section = l.getSection();
                        }
                        sections.put(l.getSection(), sectionHit);
                    }

                    //========counting traffic, check for alert===============
                    if (l.getTime().getTime()-startTraffic.getTime() > Config.CHECK_TRAFFIC_INTERVAL ) { //difference of time in ms
                        reportNormalTraffic(alertTraffic, startTraffic, l.getTime());
                        if (alertTraffic>Config.TRAFFIC_LIMIT){
                            showTrafficAlert(alertTraffic, l.getTime(), ip, maxIpHit);
                            alert = true;
                        }
                        if (alertTraffic<Config.TRAFFIC_LIMIT && alert){
                            showTrafficRecovered(alertTraffic, l.getTime());
                            alert = false;
                        }
                        //re-initialized variables, including the current logs
                        startTraffic = l.getTime();
                        alertTraffic =1;
                        ips.clear();
                        ips.put(l.getUserIP(),1);
                        ip=l.getUserIP();
                        maxIpHit = 1;
                    } else {
                        alertTraffic++;
                        int ipHit; //number of hits to be written
                        if(ips.containsKey(l.getUserIP())) ipHit=ips.get(l.getUserIP())+1;
                        else ipHit = 1;
                        if (ipHit > maxSectionHit) {
                            maxIpHit = ipHit;
                            ip = l.getUserIP();
                        }
                        ips.put(l.getUserIP(), ipHit);
                    }

                    //If there is no more log to read, check if cycles have terminated and force report
                } else {
                    if (new Date().getTime()-startSection.getTime() > Config.SECTION_REPORT_INTERVAL) {
                        //Force to report
                        report(traffic, startSection, new Date(), section, maxSectionHit);

                        //re-initialized variables
                        startSection = new Date();
                        traffic = 0;
                        sections.clear();
                        section = "";
                        maxSectionHit = 0;
                    }
                    if (new Date().getTime()-startTraffic.getTime() > Config.CHECK_TRAFFIC_INTERVAL) {
                        //Force to report
                        reportNormalTraffic(alertTraffic, startTraffic, new Date());

                        if (alertTraffic>Config.TRAFFIC_LIMIT){
                            showTrafficAlert(alertTraffic, new Date(), ip, maxIpHit);
                            alert = true;
                        }
                        if (alertTraffic<Config.TRAFFIC_LIMIT && alert){
                            showTrafficRecovered(alertTraffic, new Date());
                            alert = false;
                        }

                        //re-initializing variables
                        startTraffic = new Date();
                        alertTraffic =0;
                        ips.clear();
                        ip="";
                        maxIpHit = 0;
                    }
                    //in all case waiting for the reader to add new element
                    try {
                        Thread.sleep(Config.SECTION_REPORT_INTERVAL+1); // (interval + 1) assures that the reader has add some elements to  the queue
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        stop("Thread Analyse interrupted");
                    }
                }
            }
        }

        /**
         * Report on section with most hits
         * @param traffic
         * @param start
         * @param end
         * @param section
         * @param maxSectionHit
         */
        public void report(int traffic, Date start, Date end, String section , int maxSectionHit){
            System.out.println("INFO: Traffic from "+start+ " to "+end+" :   "+traffic);
            System.out.println("INFO: Most visited section: "+section+ " number of hits :" +maxSectionHit);
        }

        /**
         * Message for normal traffic
         * @param totalTraffic
         * @param start
         * @param end
         */
        public void reportNormalTraffic(int totalTraffic, Date start, Date end){
            System.out.println("MESS: Traffic over the pass 2 minutes from "+ start+ " to " +end + " - number of hits = " +totalTraffic);
        }


        /**
         * Show alert when traffic over predefined limit
         * @param totalTraffic
         * @param d
         * @param ip
         * @param maxIpHit
         */
        public void showTrafficAlert(int totalTraffic, Date d, String ip, int maxIpHit){
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            System.out.println("ALERT: Traffic limit = " + Config.TRAFFIC_LIMIT);
            System.out.println("ALERT: High traffic generated an alert - hits = "+ totalTraffic+", triggered at " + mFormat.format(d));
            System.out.println("ALERT: Most access ip: "+ip+" - hits = "+maxIpHit);
        }


        /**
         * Show message that traffic has recovered
         * @param totalTraffic
         * @param d
         */
        public void showTrafficRecovered(int totalTraffic, Date d){
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            System.out.println("MESS: Traffic recovered - hits = "+ totalTraffic+", recovered at " + mFormat.format(d));
        }
    }
}
