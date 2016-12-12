# HTTP_Trafic_Monitoring
Java application that monitors HTTP trafics by consumming w3c-formatted access log

## How to run the application
Run the following command where you put the HTTP_traffic_monitoring.jar file 

	java -jar HTTP_trafic_monitoring.jar
	
You'll be ask to follow some configuration steps where you can: 

* Set parameters : period for checking traffic and period for checking section hits, traffic limit. Hit **n** to skip configuration.
* Choose an active log file of your own (y) or run on test environment with a log file created by the application (n).

To safely stop the application, enter: 

	stop
	
You can see the prepared test case by skiping the configuration and let the application create a new log file in the directory of your choice.

## Project structure
### config.java 
This class holds constants which are application parameters and regex patterns for common log and common log combined.

### Log.java
Representing a log line. In this class I match a log line to common log format and extract datas (for instance : time, section of an url, ip of user). Representing log line as an object helps to easily manipulate log data, as well as extend the application for further analysis.

The Log class work for both Common log and Common log combined,we just have to change the pattern attribute to the correspondance log pattern in config.java

### LogWriter.java
This class holds a thread that write logs to a file. I simulate 3 scenarios : Low traffic (first 2 minutes), high traffic (the following 2 minutes) and finally normal traffic. 

### LogReader.java
This class holds 2 runnables : **Read** and **Analyse**. The 2 threads *readThread* and *analyseThread* run independently but they work on the same data : ConcurrentLinkedDeque **logs**. The *readThread* reads logs from log file and add to tail while *analyseThread* analyse and remove from head of  **logs**

The advantage of working on 2 threads is to increase performance in time, the analysing process doesn't block the reading process and inversly.

The choice of a CuncurrentLinkedDeque is explained by the fact that this data structured is thread-safe, means that it can be accessed by multiple threads, and it provides basic queue operations (FIFO)

#### The reading process
Logs are read line by line with the help of a RandomAccessFile object. There is also an alternative to read line effetively : BufferedReader but I choose RandomAccessFile to benefit its method **skip**. As we monitoring logs in real-time, there is no need to analyse logs written before the moment the application is lauched. So first of all I read the size of the log file in bytes, then use **skip** to jump to the end of the file and start to read from that position.
For each log line, a new Log object is add to the tail of **logs**

#### The analysing process
If the list **logs** isn't empty, we take the Log object at its head and analyse.

**Time reference** One of the subject that deserves some discussions is to choose a time reference for printing alerts and reports. 

We need to print reports periodically  after a certain amount of time, say 10s, so the simpliest thing we can do is to make a loop on system time and keep counting until the cycle finishes, then we print report and restart the loop.

The problem here is that this kind of implementation doesn't report the real count on traffic, but actually it counts how many logs the applicaiton have analysed in 10s. If traffic is particularly high and the application doesn't read and analyse the logs fast enough, it can be a pretty big difference between what is printed out and reality.

That is the reason why I choose both system time and log time as reference. To count exactly number of accesses between a time interval, I took the time written in log line as reference first: the application keep counting as long as the queue isn't empty and the time of the log at the head is still inside a period. 

With log time reference, the report reflects reality but the problem is that the alarm for report doesn't triggered regularly every 10s. For example in case of very low traffic, the analyse thread has to wait around for the next log to arrive in order to know if that log is still inside the current period. 

The solution is that we keep reading if logs in queue are available (keep time log as reference) ; as soon as the queue is empty, we check if the time for a period has expired and force the report if necessary. With this mechanism we assure that the report tells the truth and it is printed regularly.

After all I have checked the execution time of a normal "read" iteration : about 0.2ms and a normal "analyse" iteration: < 0.01 ms, so the problem mention above is hardly present. But for further development when the analyse process is a lot more complicated, the algorithm above can be really useful. 

#### Bonus

I add a bonus statictics when counting traffic every 2 min : count the number of access for each user ip. When traffic is particulary high and an alert is printed, the ip with the biggest number of access is printed too. This can be useful to check if the high traffic situation is caused by a particular ip (attack by an automated machine ?!)

## Improve the application
The parameter READ_LOG_WAIT_TIME represent the time that file reader has to wait when there is no more line to read until next read attempt. For now this parameter is choosen intuitively but I think it should change based on the average number of line read over the past reading cycles. 

The report period which is set by user can be automatically set and change based on past traffic analysis too. 

Traffic limit : if whe have the traffic record of every hour during a day, we can automatically change the limit for each period of time: for example at 10 am when a lot of user access to our website, traffic limit should be 500 but at 3 am the limit should be only 50, ...

An email should be sent to administrator if traffic is irregular.

I think more information should (and could) be analyse when an alert comes up. For example when traffic is too high, search for the ip addess that access to the same page more than 10 time per second,...