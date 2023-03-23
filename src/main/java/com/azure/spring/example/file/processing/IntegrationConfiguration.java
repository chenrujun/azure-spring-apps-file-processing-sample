package com.azure.spring.example.file.processing;

import com.azure.spring.example.file.processing.util.ReadWriteUtil;
import com.azure.spring.integration.core.handler.DefaultMessageHandler;
import com.azure.spring.messaging.eventhubs.core.EventHubsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.dsl.Files;

import java.io.File;

@Configuration
public class IntegrationConfiguration {

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
                .from(Files.inboundAdapter(new File(inputDirectory)))
                .filter(((File file) -> file.getName().endsWith(".txt")))
                .transform(Files.toStringTransformer())
                .transform(ReadWriteUtil::txtStringToAvroBytes)
                .handle(new DefaultMessageHandler(eventHubName, eventHubsTemplate))
                .get();
    }

}
