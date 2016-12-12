package com.vuong;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String os = System.getProperty("os.name");
        System.out.println(os);
        LogReader lr = null;
        LogWriter lw= null;
        Scanner scan = new Scanner(System.in);  // Reading from System.in

        System.out.println("---------------------------------");
        System.out.println("|Welcome to HTTP traffic monitor|");
        System.out.println("---------------------------------");
        System.out.println("Created by Tuyet VUONG");


        boolean optionOk = false;
        while (!optionOk){
            System.out.println("Do you have an active log file to test ? y or n");
            String ans = scan.nextLine();
            switch (ans) {
                case "y" :
                    boolean fileOk = false;
                    while (!fileOk){
                        System.out.println("Enter the location of your log file: ");
                        String userPath = scan.nextLine();
                        File userFile = new File(userPath);
                        if (userFile.exists()&&userFile.isFile()){
                            System.out.println("Start Traffic Monitoring");
                            lr = new LogReader(userPath);
                            lr.start();
                            fileOk = true;
                        } else {
                            System.out.println("File doesn't exist, please try again: ");
                        }
                    }
                    optionOk = true;
                    break;
                case "n":
                    boolean dirOk = false;
                    while (!dirOk){
                        System.out.println("Enter the directory where you want to put the test log file.\n" +
                                "The application will create and simulate an active log file called access.log in your directory\n" +
                                "The application will delete any file called access.log in your directory if it exists\n" +
                                "Directory: ");
                        String userPath = scan.nextLine();
                        File userDir = new File(userPath);
                        if (userDir.isDirectory()&&userDir.exists()){
                            System.out.println("Start Traffic Monitoring");
                            if (os.startsWith("Windows")){
                                if (!userPath.endsWith("\\")) {
                                    userPath = userPath + "\\";
                                }
                            } else {
                                if (!userPath.endsWith("/")) {
                                    userPath = userPath + "/";
                                }
                            }
                            File file = new File(userPath+"access.log");
                            if (file.exists()) file.delete();
                            try {
                                file.createNewFile();
                                System.out.println("Log file created");
                                lw = new LogWriter(userPath+"access.log");
                                lr = new LogReader(userPath+"access.log");
                                lw.start();
                                lr.start();
                                dirOk = true;
                            } catch (IOException e) {
                                System.out.println("Can't create log file in this directory");
                            }
                        } else {
                            System.out.println ("Invalid directory. Please try again.");
                        }
                    }
                    optionOk = true;
                    break;
                default :
                    System.out.println("Invalid command");
                    break;
            }
        }
        while (true){
            String command = scan.nextLine();
            switch (command){
                case "stop" :
                    if(lr!=null) lr.stop("Manuel stop by user");
                    if(lw!=null) lw.stop();
                    System.out.println("Stop all processes completed");
                    break;
                default:
                    System.out.print("Invalid command");
            }
        }
    }
}
