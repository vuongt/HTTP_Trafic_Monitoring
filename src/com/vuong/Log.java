package com.vuong;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vuong on 08/12/2016.
 */
public class Log {
    private String logRaw;
    private String time;
    private String url;
    //regex pattern for common log format
    private final Pattern pattern = Pattern.compile(
            "^([\\d.]+)" + // client IP
            " (\\S+)" + // user RFC 1413 identifier
            " (\\S+)" + //user's id
            " \\[([\\w:/]+\\s[+\\-]\\d{4})\\]" + //date
            " \"(\\w+) (.+?) (.+?)\"" + //request method and URL
            " (\\d{3})" + // http status code
            " (\\d+|(.+?))"); // number of bytes in response


    public Log(String logRaw) {
        this.logRaw = logRaw;
        //extract time and url
        Matcher m = pattern.matcher(logRaw);
        if (m.matches()){
            this.time = m.group(4); //extract time
            this.url = m.group(6); // extract the url
        } //TODO throw new exception
    }

    /**
     * extract the section from a url
     * @return the section visited
     */
    public String getSection(){
        String section = url;
        if (section.startsWith("http://")){
            section = section.substring(7);
        } else if (section.startsWith("https://")){
            section = section.substring(8);
        }
        // the url can be "abc", "abc/" "abc/abc" or "abc/abc/" or abc/abc/abc
        String[] parts = section.split("/");
        if (parts.length<3) return section; //TODO attention the slash at the end
        else return parts[0]+"/"+parts[1];
    }

    public void print(){
        System.out.println(logRaw);
        System.out.println(time);
        System.out.println(url);
    }

}