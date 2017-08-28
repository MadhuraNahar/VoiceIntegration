package com.test.voice.utils;

/**
 * Created by Madhura Nahar.
 */


import android.os.Build;
import org.slf4j.LoggerFactory;
import java.io.File;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;


/**
 * This class contains API calls to create logs throughout the application
 */
public class Logutil {

    private static Logutil ourInstance = new Logutil();

    public static Logutil getInstance() {
        return ourInstance;
    }

    private Logutil() {

    }

    public void info(String mesg)
    {
        org.slf4j.Logger log = LoggerFactory.getLogger(Logutil.class);

        StackTraceElement[] element = Thread.currentThread().getStackTrace();

        if(element != null && element.length > 3)
        {
            String classname = element[3].getClassName();
            int lineNumber = element[3].getLineNumber();
            String methodname = element[3].getMethodName();

            String print = "[ " + classname + "/" +methodname+" ] " + lineNumber + " " + mesg;
            log.info(print);
            System.out.println(print);
        }
    }

    public void warn(String mesg)
    {
        org.slf4j.Logger log = LoggerFactory.getLogger(Logutil.class);
        StackTraceElement[] element = Thread.currentThread().getStackTrace();
        if(element != null && element.length > 3)
        {
            String classname = element[3].getClassName();
            int lineNumber = element[3].getLineNumber();
            String methodname = element[3].getMethodName();

            String print = "[ " + classname + "/" +methodname+" ] " + lineNumber + " " + mesg;
            log.info(print);
            System.out.println(print);
        }
    }

    public void debug(String mesg)
    {
        org.slf4j.Logger log = LoggerFactory.getLogger(Logutil.class);
        StackTraceElement[] element = Thread.currentThread().getStackTrace();
        if(element != null && element.length > 3)
        {
            String classname = element[3].getClassName();
            int lineNumber = element[3].getLineNumber();
            String methodname = element[3].getMethodName();

            String print = "[ " + classname + "/" +methodname+" ] " + lineNumber + " " + mesg;
            log.info(print);
            System.out.println(print);

        }
    }

    public void error(String mesg)
    {
        org.slf4j.Logger log = LoggerFactory.getLogger(Logutil.class);

        StackTraceElement[] element = Thread.currentThread().getStackTrace();
        if(element != null && element.length > 3)
        {
            String classname = element[3].getClassName();
            int lineNumber = element[3].getLineNumber();
            String methodname = element[3].getMethodName();

            String print = "[ " + classname + "/" +methodname+" ] " + lineNumber + " " + mesg;
            log.info(print);
            System.out.println(print);

        }
    }

    public void json(String mesg)
    {
        org.slf4j.Logger log = LoggerFactory.getLogger(Logutil.class);
        log.error(mesg);
        System.out.println(mesg);

    }

    public void exception(Exception exception)
    {
  //***********************UNCOMMENT THE 'CRASHLYTICS.EXCEPTION' PART AND DELETE LOG.ERROR LATER***************//
//        Crashlytics.logException(exception);
        org.slf4j.Logger log = LoggerFactory.getLogger(Logutil.class);
        log.error("Exception: " +exception.getMessage());
    }

    public void deviceInfo()
    {
        org.slf4j.Logger Log = LoggerFactory.getLogger(Logutil.class);

        Log.info("SERIAL: " + Build.SERIAL);
        Log.info("MODEL: " + Build.MODEL);
        Log.info("Manufacture: " + Build.MANUFACTURER);
        Log.info("brand: " + Build.BRAND);
        Log.info("type: " + Build.TYPE);
        Log.info("SDK  " + Build.VERSION.SDK_INT);
        Log.info("Version Code: " + Build.VERSION.RELEASE);
    }

    private void createLoggingFile() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("dynamic_logger");
        // Don't inherit root appender
//        logger.setAdditive(false);
        RollingFileAppender rollingFile = new RollingFileAppender();
        rollingFile.setContext(context);
        rollingFile.setName("dynamic_logger_fileAppender");
        // Optional
        rollingFile.setFile("/Butler"
                + File.separator + "butler.log");
        rollingFile.setAppend(true);
        // Set up rolling policy
        TimeBasedRollingPolicy rollingPolicy = new TimeBasedRollingPolicy();
        rollingPolicy.setFileNamePattern("/log"
                + File.separator + "%d{yyyy-MM,aux}"
                + File.separator + "msg_%d{yyyy-MM-dd_HH-mm}.txt");
        rollingPolicy.setParent(rollingFile);
        rollingPolicy.setContext(context);
        rollingPolicy.start();
        // set up pattern encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%msg%n");
        encoder.start();
        rollingFile.setRollingPolicy(rollingPolicy);
        rollingFile.setEncoder(encoder);
        rollingFile.start();
        // Atach appender to logger
        logger.addAppender(rollingFile);
    }

}
