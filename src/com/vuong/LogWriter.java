package com.vuong;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.File;

/**
 * Created by vuong on 08/12/2016.
 * Simulate a log file actively written to
 */
public class LogWriter {
    private String path;  //path of the file to write to
    private int interval; //common interval for each log write

    private WriteLog writeThread = null; //thread used to write log

    public LogWriter(String path) {
        this.path = path;
        this.interval = Config.DEFAULT_WRITE_INTERVAL;
    }

    /**
     * Start writing log
     */
    public void start(){
        new WriteLog().start();
    }

    /**
     * Stop the write thread
     */
    public void stop(){
        if (writeThread!=null){
            writeThread.terminate();
        }
    }

    /**
     * Thread used to write logs
     */
    private class WriteLog extends Thread {
        private volatile boolean running = true;

        /**
         * stop all loops
         */
        public void terminate(){
            running = false;
        }

        @Override
        public void run() {
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            File file = new File(path);
            try {
                FileWriter fw = new FileWriter(file, true); //appends to end of file
                long start = System.currentTimeMillis();
                long period1 = start + 2*60*1000;
                long period2 = period1 + 2*60*1000;

                // Low traffic simulation
                while(System.currentTimeMillis()<period1 && running){
                    fw.write("127.0.0.1 user-identifier user-id ["+mFormat.format(new Date())+"] \"GET http://mysite/mysection/mypage HTTP/1.0\" 200 2048 \"-\" \"-\"\n");
                    try {
                        Thread.sleep(interval*50); //Write every 15s
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fw.close();
                        running = false;
                    }
                    fw.flush(); // write all lines right away, flush all buffering content
                }

                // High traffic simulation
                while(period1<System.currentTimeMillis()&&System.currentTimeMillis()<period2 && running){
                    fw.write("127.0.0.1 user-identifier user-id ["+mFormat.format(new Date())+"] \"GET http://mysite/mysection/mypage HTTP/1.0\" 200 2048 \"-\" \"-\"\n");
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fw.close();
                        running = false;
                    }
                    fw.write("127.0.0.1 user-identifier user-id ["+mFormat.format(new Date())+"] \"GET https://othersite/othersection/otherpage HTTP/1.0\" 200 2048 \"-\" \"-\"\n");
                    try {
                        Thread.sleep(interval/2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fw.close();
                        running = false;
                    }
                    fw.write("127.0.0.1 user-identifier user-id ["+mFormat.format(new Date())+"] \"GET https://othersite/othersection/otherpage HTTP/1.0\" 200 2048 \"-\" \"-\"\n");
                    try {
                        Thread.sleep(interval/2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fw.close();
                        running = false;
                    }
                    fw.flush(); // write all lines right away, flush all buffering content
                }

                //Normal traffic
                while(System.currentTimeMillis()>period2 && running){
                    fw.write("127.0.0.1 user-identifier user-id ["+mFormat.format(new Date())+"] \"GET http://mysite/mysection/mypage HTTP/1.0\" 200 2048 \"-\" \"-\"\n");
                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        fw.close();
                        running = false;
                    }
                    fw.flush(); // write all lines right away, flush all buffering content
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Can't write logs to destination file");
                running = false;
            }
        }
    }
}
