# HTTP_Trafic_Monitoring
Java application that monitors HTTP trafics by consumming w3c-formatted access log

## How to run the application
Run the following command where you put the HTTP_traffic_monitoring.jar file 

	java -jar HTTP_trafic_monitoring.jar
	
You'll be asked to follow some configuration steps where you can: 

* Set parameters : period for checking traffic and period for checking section hits, traffic limit. Hit **n** to skip configuration.
* Choose an active log file of your own (y) or run on test environment with a log file created by the application (n).

To safely stop all running threads, enter: 

	stop
	
(Ctrl-C to fully stop the java application)

You can see the prepared test case by skipping the configuration and let the application create a new log file in the directory of your choice.

## Project structure
### config.java 
This class holds constants which are application parameters and regex patterns for common log and common log combined.

### Log.java
Representing a log line. In this class we match a log line to common log format and extract datas (for instance : time, section of an url, ip of user). Representing log line as an object helps to easily manipulate log data, as well as extend the application for further analysis.

The Log class work for both Common log and Common log combined, we just have to change the pattern attribute to the correspondance log pattern in config.java

### LogWriter.java
This class holds a thread that write logs to a file. We simulate 3 scenarios : Low traffic (first 2 minutes), high traffic (the following 2 minutes) and finally normal traffic. 

### LogReader.java
This class holds 2 runnables : **Read** and **Analyse**. The 2 threads *readThread* and *analyseThread* run independently but they work on the same data : ConcurrentLinkedDeque **logs**. The *readThread* reads logs from log file and add to tail while *analyseThread* analyse and remove from head of  **logs**

The advantage of working on 2 threads is to increase performance in time, the analysing process doesn't block the reading process and vice-versa.

The choice of a ConcurrentLinkedDeque is explained by the fact that this data structured is thread-safe, meaning that it can be accessed by multiple threads, and it provides basic queue operations (FIFO).

#### The reading process
Logs are read line by line with the help of a RandomAccessFile object. There is also an alternative to read line effetively : BufferedReader but we'd better use RandomAccessFile to benefit its method **skip**. As we are monitoring logs in real-time, there is no need to analyse logs written before the moment the application is lauched. So first of all we read the size of the log file in bytes, then use **skip** to jump to the end of the file and start to read from that position.
For each log line, a new Log object is added to the tail of **logs**

#### The analysing process
If the list **logs** isn't empty, we take the Log object at its head and analyse.

**Time reference** One of the subject that deserves some discussions is the choice of a time reference for printing alerts and reports. 

We need to print reports periodically  after a certain amount of time, say 10s, so the simpliest thing we can do is to make a loop on system time and keep counting until the cycle finishes, then we print report and restart the loop.

The problem here is that this kind of implementation doesn't report the real count on traffic, but actually it counts how many logs the applicaiton have analysed in 10s. If traffic is particularly high and the application doesn't read and analyse the logs fast enough, there can be a big difference between what is printed out and reality.

That is the reason why we should choose both system time and log time as references. To count exactly the number of accesses during a time interval, we took the time written in the log line as reference first: the application keep counting as long as the queue isn't empty and the time of the log at the head is still inside a period. 

With log time reference, the report reflects reality but the problem is that the report is not printed regularly every 10s. For example in case of very low traffic, the analyse thread has to wait for the next log to arrive in order to know if that log is still inside the current period. 

The solution is that we keep reading if logs in queue are available (keep time log as reference) ; as soon as the queue is empty, we check if the time for a period has expired and force the report if necessary. With this mechanism we assure that the reporting is accurate and punctual.

During the development, the execution time  was checked for a normal "read" iteration : about 0.2ms and for a normal "analyse" iteration: < 0.01 ms, so the problem mentionned above is hardly present. But for further developments involving more complicated analyse processes, the algorithm above could prove to be really useful. 

#### Bonus

A bonus analysis has been added to count the number of access for each user ip every two minutes. When the traffic is over a predefined limit, an alert is printed, including the ip with the biggest number of accesses. This can be useful to check if the high traffic situation is caused by a particular IP address.

## Improvement of the application
The parameter READ_LOG_WAIT_TIME represent the time that the file reader has to wait when there is no more line to read until next read attempt. For now, this parameter is choosen intuitively but it should be changed based on the average number of line read over the past reading cycles. 

The report period which is set by user can be automatically set and changed based on past traffic analysis too. 

Traffic limit : if we have the traffic record of every hour during a day, we can automatically change the limit for each period of time: for example at 10 am when a lot of user access to our website, traffic limit should be 500 but at 3 am the limit should be only 50, ...

An email should be sent to administrator if traffic is irregular.

More information should (and could) be analysed when an alert comes up. For example when the traffic is too high, search for the ip addresses that access to the same page more than 10 time per second,...
