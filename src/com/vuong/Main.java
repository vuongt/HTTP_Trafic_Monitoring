package com.vuong;

import java.text.ParseException;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Log l = null;
        try {
            l = new Log("127.0.0.1 user-identifier user-id [09/Dec/2016:12:25:45 +0100] \"GET /user HTTP/1.0\" 200 2048 \"-\" \"-\"");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        l.print();
        System.out.println(l.getSection());

        String ex1 = "abc";
        String ex2 = "abc/";
        String ex3 = "abc/abc";
        String ex4 = "abc/abc/";
        String ex5 = "abc/abc/abc";
        String ex6 = "http://abc/abc/abc";
        System.out.println(ex1.split("/").length);
        System.out.println(ex2.split("/").length);
        System.out.println(ex3.split("/").length);
        System.out.println(ex4.split("/").length);
        System.out.println(ex5.split("/").length);
        System.out.println(ex6.split("/").length);

        LogWriter lw = new LogWriter("/Users/vuong/access.log");
        lw.writeLog();

        LogReader lr = new LogReader("/Users/vuong/access.log");
        lr.start();
    }
}
