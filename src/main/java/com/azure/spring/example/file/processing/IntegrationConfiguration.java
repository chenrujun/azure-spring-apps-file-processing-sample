package com.azure.spring.example.file.processing;

import com.azure.spring.example.file.processing.util.AvroUtil;
import com.azure.spring.example.file.processing.util.TxtLine;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                .from(Files.inboundAdapter(new File(inputDirectory)))
                .filter(this::isTargetFile)
                .transform(Files.toStringTransformer())
                .transform(this::toTxtLine)
                .split()
                .transform(Message.class, this::toAvroBytes)
                .filter(this::isValidAvroBytes)
                .handle(new DefaultMessageHandler(eventHubName, eventHubsTemplate))
                .get();
    }

    private boolean isTargetFile(File file) {
        String absolutePath = file.getAbsolutePath();
        LOGGER.info("Find a new file. File = {}", absolutePath);
        boolean isTargetFile = file.getName().endsWith(".txt");
        if (!isTargetFile) {
            LOGGER.info("File filtered out file because it's not txt file. File = {}", file.getAbsolutePath());
        }
        return isTargetFile;
    }

    private List<TxtLine> toTxtLine(String string) {
        String[] lines = string.split("\\r?\\n");
        List<TxtLine> txtLines= new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            txtLines.add(new TxtLine(i + 1, lines[i]));
        }
        return txtLines;
    }

    private boolean isValidAvroBytes(byte[] bytes) {
        return bytes.length > 0;
    }

    private byte[] toAvroBytes(Message<TxtLine> message) {
        String fileName = (String) message.getHeaders().get("fileName");
        TxtLine line = message.getPayload();
        int lineNumber = line.lineNumber();
        String content = line.content();
        return AvroUtil.toAvroBytes(fileName, lineNumber, content);
    }

}
