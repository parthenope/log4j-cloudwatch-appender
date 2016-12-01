package com.gilt.log4j.cloudwatch.appender;


import com.amazonaws.services.logs.AWSLogsClient;
import com.amazonaws.services.logs.model.InputLogEvent;
import com.amazonaws.services.logs.model.InvalidSequenceTokenException;
import com.amazonaws.services.logs.model.PutLogEventsRequest;
import com.amazonaws.services.logs.model.PutLogEventsResult;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CloudwatchAppender extends AppenderSkeleton {

    private AWSLogsClient awsLogsClient = new AWSLogsClient();

    private AtomicReference<String> lastSequenceToken = new AtomicReference<>();

    private String logGroupName;

    private String logStreamName;

    public CloudwatchAppender() {
        super();
    }

    public void setLogGroupName(String logGroupName) {
        this.logGroupName = logGroupName;
    }

    public void setLogStreamName(String logStreamName) {
        this.logStreamName = logStreamName;
    }

    @Override
    protected void append(LoggingEvent event) {

        String lineEvent = layout.format(event);

        InputLogEvent inputLogEvent = new InputLogEvent().withTimestamp(new Date().getTime()).withMessage(lineEvent);
        List<InputLogEvent> list = new ArrayList<>();
        list.add(inputLogEvent);
        PutLogEventsRequest putLogEventsRequest = new PutLogEventsRequest(
                logGroupName,
                logStreamName, list);

        try {
            putLogEventsRequest.setSequenceToken(lastSequenceToken.get());
            PutLogEventsResult result = awsLogsClient.putLogEvents(putLogEventsRequest);
            lastSequenceToken.set(result.getNextSequenceToken());
        } catch (InvalidSequenceTokenException invalidSequenceTokenException) {
            putLogEventsRequest.setSequenceToken(invalidSequenceTokenException.getExpectedSequenceToken());
            PutLogEventsResult result = awsLogsClient.putLogEvents(putLogEventsRequest);
            lastSequenceToken.set(result.getNextSequenceToken());
        }

    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
    }

}
