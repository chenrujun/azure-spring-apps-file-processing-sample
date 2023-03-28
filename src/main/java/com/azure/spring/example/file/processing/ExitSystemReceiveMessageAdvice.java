package com.azure.spring.example.file.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.aop.ReceiveMessageAdvice;
import org.springframework.messaging.Message;

import static com.azure.spring.example.file.processing.util.FileMessageUtil.getAbsolutePath;
import static com.azure.spring.example.file.processing.util.FileMessageUtil.getFileSize;

public class ExitSystemReceiveMessageAdvice implements ReceiveMessageAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExitSystemReceiveMessageAdvice.class);

    @Override
    public Message<?> afterReceive(Message<?> result, Object source) {
        if (result == null) {
            LOGGER.info("Get null when receive message, exit system.");
            System.exit(0);
        }
        LOGGER.info("Start to handle file. file = {}, fileSize = {}. ",
                getAbsolutePath(result), getFileSize(result));
        return result;
    }
}
