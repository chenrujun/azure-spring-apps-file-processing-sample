package com.azure.spring.example.file.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.aop.ReceiveMessageAdvice;
import org.springframework.messaging.Message;

import static com.azure.spring.example.file.processing.util.MessageHeader.FILE_ORIGINAL_FILE;

public class NoMoreMessageNotifierAdvice implements ReceiveMessageAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoMoreMessageNotifierAdvice.class);
    @Override
    public Message<?> afterReceive(Message<?> result, Object source) {
        if (result == null) {
            LOGGER.info("Get null when receive message, exit system.");
            System.exit(0);
        }
        LOGGER.info("Start to handle new message. fileName = {}", result.getHeaders().get(FILE_ORIGINAL_FILE));
        return result;
    }
}
