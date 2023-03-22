package com.azure.spring.example.file.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;

import java.io.File;

@Configuration
public class IntegrationConfiguration {

    @Value("${input-directory}")
    private String inputDirectory;
    @Value("${output-directory}")
    private String outputDirectory;

    @Bean
    public IntegrationFlow fileReadingFlow() {
        return IntegrationFlow
                .from(Files.inboundAdapter(new File(inputDirectory)).patternFilter("*.txt"),
                        e -> e.poller(Pollers.fixedDelay(100)))
                .transform(Files.toStringTransformer())
                .handle(Files.outboundGateway(m -> outputDirectory))
                .channel(MessageChannels.queue())
                .get();
    }

}
