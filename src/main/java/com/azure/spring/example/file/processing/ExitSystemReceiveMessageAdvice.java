package com.azure.spring.example.file.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.aop.ReceiveMessageAdvice;
import org.springframework.messaging.Message;

import static com.azure.spring.example.file.processing.util.FileMessageUtil.getAbsolutePath;
import static com.azure.spring.example.file.processing.util.FileMessageUtil.getFileSize;

public class ExitSystemReceiveMessageAdvice implements ReceiveMessageAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExitSystemReceiveMessageAdvice.class);
    private boolean lastResultIsNull = true;

    @Override
    public Message<?> afterReceive(Message<?> result, Object source) {
        boolean resultIsNull = (result == null);
        if (resultIsNull != lastResultIsNull) {
            LOGGER.info("Message receiving status changed, received message is {}.", resultIsNull ? "null" : "not null");
            lastResultIsNull = resultIsNull;
            // System.exit(0);
        }
        if (!resultIsNull) {
            LOGGER.info("Start to handle file. file = {}, fileSize = {}. ",
                    getAbsolutePath(result), getFileSize(result));
        }
        return result;
    }
}
