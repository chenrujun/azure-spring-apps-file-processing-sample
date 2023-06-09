package com.azure.spring.example.file.processing;

import com.azure.spring.example.file.processing.util.FileMessageUtil;
import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.io.File;

import static com.azure.spring.example.file.processing.util.FileMessageUtil.LINE_NUMBER_IN_FILE;

@Configuration
public class IntegrationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationConfiguration.class);

    private final String logsDirectory;
    private final String processedLogsDirectory;
    private final String eventHubName;
    private final EventHubsTemplate eventHubsTemplate;

    public IntegrationConfiguration(@Value("${logs-directory}") String logsDirectory,
                                    @Value("${processed-logs-directory}") String processedLogsDirectory,
                                    @Value("${spring.cloud.azure.eventhubs.event-hub-name}") String eventHubName,
                                    EventHubsTemplate eventHubsTemplate) {
        this.logsDirectory = toAbsolutePath(logsDirectory);
        this.processedLogsDirectory = toAbsolutePath(processedLogsDirectory);
        this.eventHubName = eventHubName;
        this.eventHubsTemplate = eventHubsTemplate;
        LOGGER.info("logsDirectory = {}, processedLogsDirectory = {}, eventHubName = {}.", logsDirectory, processedLogsDirectory, eventHubName);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public IntegrationFlow fileReadingFlow() {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(logsDirectory)).recursive(true).useWatchService(true),
                        e -> e.poller(Pollers.fixedDelay(0).advice(new ExitSystemReceiveMessageAdvice())))
                .filter(FileMessageUtil::isTargetFile)
                .transform(Files.toStringTransformer())
                .transform(Message.class, message -> FileMessageUtil.toTxtLineThenMoveFile((Message<String>) message, logsDirectory, processedLogsDirectory)) // TODO: Improvements: 1. Move file after send message to event hub. 2. Move file that isTargetFile() == false.
                .split()
                .enrichHeaders(s -> s.headerExpressions(c -> c.put(LINE_NUMBER_IN_FILE, "payload.lineNumber()")))
                .transform(Message.class, FileMessageUtil::toAvroBytes)
                .filter((byte[] bytes) -> bytes.length > 0)
                .handle(defaultMessageHandler())
                .get();
    }

    private MessageHandler defaultMessageHandler() {
        DefaultMessageHandler handler = new DefaultMessageHandler(eventHubName, eventHubsTemplate);
        handler.setSendCallback(new OnFailureExitSystemCallback());
        return handler;
    }

    private String toAbsolutePath(String directory) {
        String separator = File.separator;
        String systemCompatibleDirectory = directory.replace("/", separator).replace("\\", separator);
        return new File(systemCompatibleDirectory).getAbsolutePath();
    }

}
