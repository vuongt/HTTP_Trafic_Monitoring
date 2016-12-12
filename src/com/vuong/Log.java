package com.vuong;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represent a log line
 */

public class Log {

    private String logRaw;  //Log string expression
    private Date time;      //Time of the log
    private String url;     //url visited

    //Bonus for reporting on user ip
    private String userIP;

    //regex PATTERN for common log or common log combined format; useful for further information extract
    private final Pattern PATTERN = Pattern.compile(Config.COMMON_LOG_PATTERN);

    public Log(String logRaw) throws ParseException {
        this.logRaw = logRaw;
        Matcher m = PATTERN.matcher(logRaw);
        if (m.matches()){
            DateFormat format = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
            this.time = format.parse(m.group(4));   //extract time
            this.url = m.group(6);                  //extract the url
            this.userIP = m.group(1);               //extract userIP
        } else{
            throw new ParseException("Log file format doesn't match log pattern",0);
        }
    }

    /**
     * Extract the section part from a url
     * @return String the section visited
     */
    public String getSection(){
        String section = url;
        if (url!=null&& !Objects.equals(url, "")){
            if (section.startsWith("http://")){
                section = section.substring(7);
            } else if (section.startsWith("https://")){
                section = section.substring(8);
            }
            String[] parts = section.split("/");
            if (parts.length<3) return section;

            else return parts[0]+"/"+parts[1];
        } else {
            return null;
        }
    }

    public String getUserIP(){ return this.userIP;}

    public Date getTime() {
        return time;
    }

    public void print(){
        System.out.println(logRaw);
        System.out.println(time);
        System.out.println(url);
    }

}
