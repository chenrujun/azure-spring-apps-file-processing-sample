package com.azure.spring.example.file.processing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
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
                .from(Files.inboundAdapter(new File(inputDirectory)))
                .filter(((File file) -> file.getName().endsWith(".txt")))
                .transform(Files.toStringTransformer())
                .handle(Files.outboundAdapter(new File(outputDirectory)))
                .get();
    }

}
