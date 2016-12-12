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

        //configure parameters of the application
        System.out.println("Application's parameter is configured as follow :\n" +
                "Period for checking section hits (in s): 10\n" +
                "Period for generating traffic alerts (in min): 2\n" +
                "Traffic limit during the period above: 450\n" +
                "Do you want to reconfigure ? y or n");

        boolean configureOk = false;
        while (!configureOk){
            String conf = scan.nextLine();
            switch (conf){
                case "y":
                    boolean sectionOk = false;
                    while (!sectionOk){
                        System.out.println("Set the period for checking section hits (in s): ");
                        try{
                            Integer param1 = Integer.parseInt(scan.nextLine());
                            if (param1>0){
                                Config.SECTION_REPORT_INTERVAL = param1*1000;
                                sectionOk = true;
                            } else throw new NumberFormatException();
                        } catch (NumberFormatException e){
                            System.out.println("Invalid input");
                        }
                    }
                    boolean trafficOk = false;
                    while (!trafficOk){
                        System.out.println("Set the period for generating traffic alerts (in min): ");
                        try {
                            Integer param2 = Integer.parseInt(scan.nextLine());
                            if (param2>0){
                                Config.CHECK_TRAFFIC_INTERVAL = param2*60*1000;
                                trafficOk = true;
                            } else throw new NumberFormatException();
                        } catch (NumberFormatException e){
                            System.out.println("Invalid input");
                        }
                    }
                    boolean limitOk = false;
                    while (!limitOk){
                        System.out.println("Set the traffic limit during the period above: ");
                        try {
                            Integer param3 = Integer.parseInt(scan.nextLine());
                            if (param3>0){
                                Config.TRAFFIC_LIMIT = param3;
                                limitOk = true;
                            } else throw new NumberFormatException();
                        }catch (NumberFormatException e){
                            System.out.println("Invalid input");
                        }

                    }
                    configureOk = true;
                    break;
                case "n":
                    configureOk = true;
                    break;
                default:
                    System.out.println("Please answer y or n: ");
                    break;
            }
        }

        boolean optionOk = false;
        while (!optionOk){
            System.out.println("Do you have an active log file to test ? y or n");
            String ans = scan.nextLine();
            switch (ans) {
                case "y" :
                    boolean fileOk = false;
                    while (!fileOk){
                        //take the path of log file
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
                    //ask for the directory to put the log file
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
                    if(lr!=null) lr.stop("Manually stop by user");
                    if(lw!=null) lw.stop();
                    System.out.println("Stop all processes completed");
                    break;
                default:
                    System.out.print("Invalid command");
            }
        }
    }
}
