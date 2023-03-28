package com.azure.spring.example.file.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class OnFailureExitSystemCallback implements ListenableFutureCallback<Void> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnFailureExitSystemCallback.class);
    @Override
    public void onFailure(Throwable ex) {
        LOGGER.error("Failed to handle message, exit system.");
        System.exit(-1);
    }

    @Override
    public void onSuccess(Void result) {
        LOGGER.debug("Message handled successfully.");

    }
}
