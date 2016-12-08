package com.vuong;

public class Main {

    public static void main(String[] args) {
	// write your code here
        Log l = new Log("127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET http://dty/mysite/apache_pb.gif HTTP/1.0\" 200 2326");
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


    }
}
