package com.vuong;

/**
 * Created by vuong on 08/12/2016.
 */
public class Config {
    public static int CHECK_TRAFFIC_INTERVAL = 2*60*1000; // check for high traffic avery 2 min
    public static int SECTION_REPORT_INTERVAL = 10*1000;  // report on most hit section every 10s
    public static int TRAFFIC_LIMIT = 450; // limit for triggering an alert on traffic
    public static int READ_LOG_WAIT_TIME = 5*1000; //time to wait when there is no more line to read in log file
    public final static String COMMON_LOG_PATTERN =
            "^([\\d.]+)" +                      // client IP
            " (\\S+)" +                         // user RFC 1413 identifier
            " (\\S+)" +                         // user's id
            " \\[([\\w:/]+\\s[+\\-]\\d{4})\\]" +// date
            " \"(\\w+) (.+?) (.+?)\"" +         // request method and URL
            " (\\d{3})" +                       // http status code
            " (\\d+|(.+?))";                    // number of bytes in response

    public final static String COMMON_LOG_COMBINED_PATTERN =
            "^([\\d.]+)" +                      // client IP
            " (\\S+)" +                         // user RFC 1413 identifier
            " (\\S+)" +                         // user's id
            " \\[([\\w:/]+\\s[+\\-]\\d{4})\\]" +// date
            " \"(\\w+) (.+?) (.+?)\"" +         // request method and URL
            " (\\d{3})" +                       // http status code
            " (\\d+|(.+?))"+                    // number of bytes in response
            " \"([^\"]+|(.+?))\""+              // referer
            " \"([^\"]+|(.+?))\"";              // user-agent

    public final static int DEFAULT_WRITE_INTERVAL = 300; //in ms
}
