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
import org.springframework.integration.file.dsl.Files;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

import java.io.File;

@Configuration
public class IntegrationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationConfiguration.class);

    private final String inputDirectory;
    private final String eventHubName;
    private final EventHubsTemplate eventHubsTemplate;

    public IntegrationConfiguration(@Value("${input-directory}") String inputDirectory,
                                    @Value("${spring.cloud.azure.eventhubs.event-hub-name}") String eventHubName,
                                    EventHubsTemplate eventHubsTemplate) {
        this.inputDirectory = inputDirectory;
        this.eventHubName = eventHubName;
        this.eventHubsTemplate = eventHubsTemplate;
    }

    @Bean
    public IntegrationFlow fileReadingFlow() {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(inputDirectory)).recursive(true))
//                .from(Files.inboundAdapter(new File(inputDirectory)).recursive(true),
//                        e -> e.poller(Pollers.fixedDelay(0).advice(new ExitSystemReceiveMessageAdvice())))
                .filter(FileMessageUtil::isTargetFile)
                .transform(Files.toStringTransformer())
                .transform(Message.class, FileMessageUtil::toTxtLine)
                .split()
                .transform(Message.class, FileMessageUtil::toAvroBytes)
                .handle(defaultMessageHandler())
                .get();
    }

    private MessageHandler defaultMessageHandler() {
        DefaultMessageHandler handler = new DefaultMessageHandler(eventHubName, eventHubsTemplate);
        handler.setSendCallback(new OnFailureExitSystemCallback());
        return handler;
    }

}
