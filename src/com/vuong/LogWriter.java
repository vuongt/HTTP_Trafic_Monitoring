package com.vuong;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.io.File;

/**
 * Created by vuong on 08/12/2016.
 */
public class LogWriter {
    private String path; //path of the file to write to
    private int interval; //interval for each log write
    private final int DEFAULT_INTERVAL =1000; //in ms

    public LogWriter(String path) {
        this.path = path;
        this.interval = DEFAULT_INTERVAL;
    }

    public void writeLog(){
        WriteLog w = new WriteLog();
        new Thread(w).start();
    }

    private class WriteLog implements Runnable {

        @Override
        public void run() {
            DateFormat mFormat = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            File file = new File(path);
            if (file.exists()) file.delete();
            try {
                file.createNewFile();
                FileWriter fw = new FileWriter(file, true); //appends to end of file
                while(true){
                    fw.write("127.0.0.1 user-identifier user-id ["+mFormat.format(new Date())+"] \"GET http://mysite/mysection/mypage HTTP/1.0\" 200 2048\n");
                    fw.flush();
                    Thread.sleep(interval);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
