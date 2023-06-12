package com.message.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TransactionMsgClient {
    private static final String MQ_PRODUCER_NAME = "TransactionMsgProducer";

    private static final int MIN_DELAY = 0;

    private static final int MAX_DELAY = 90 * 24 * 60 * 60;

    protected static final Logger LOGGER = LoggerFactory.getLogger(TransactionMsgClient.class);



}
