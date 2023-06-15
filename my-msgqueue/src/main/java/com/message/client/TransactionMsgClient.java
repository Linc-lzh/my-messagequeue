package com.message.client;

import com.message.mapper.MessageInfoMapper;
import com.message.model.MQMessage;
import com.message.model.MessageInfo;
import com.message.model.State;
import com.message.processor.MsgProcessor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TransactionMsgClient {
    private static final String MQ_PRODUCER_NAME = "TransactionMsgProducer";

    private static final int MIN_DELAY = 0;

    private static final int MAX_DELAY = 90 * 24 * 60 * 60;

    protected static final Logger LOGGER = LoggerFactory.getLogger(TransactionMsgClient.class);

    private AtomicReference<State> state;

    private MessageInfoMapper messageInfoMapper;

    private MsgProcessor msgProcessor;

    public void setMessageInfoMapper(MessageInfoMapper messageInfoMapper) {
        this.messageInfoMapper = messageInfoMapper;
    }

    public void setMsgProcessor(MsgProcessor msgProcessor) {
        this.msgProcessor = msgProcessor;
    }

    public TransactionMsgClient(){
        state = new AtomicReference<State>(State.CREATE);
    }

    public void init()
            throws Exception {
        if (state.get().equals(State.RUNNING)) {
            LOGGER.info("TransactionMsgClient have inited, return");
            return;
        }
        LOGGER.info("start init state {} this {}", state, this);

        try {

            LOGGER.info("end init success");
        } catch (Exception ex) {
            LOGGER.error("producer start fail", ex);
            throw ex;
        }
        state.compareAndSet(State.CREATE, State.RUNNING);
    }

    public void close() {
        LOGGER.info("start close TransactionMsgClient");
        if (state.compareAndSet(State.RUNNING, State.CLOSED)) {

        } else {
            LOGGER.info("state not right {} ", state);
        }
    }

    public abstract Long sendMsg(String content, String topic, String tag)
            throws Exception;

    public Long sendMsg(Connection con, String content, String topic, String tag, int delay)
            throws Exception {
        // 1、消息校验
        Long id = null;
        if (!state.get().equals(State.RUNNING)) {
            LOGGER.error("TransactionMsgClient not Running , please call init function");
            throw new Exception("TransactionMsgClient not Running , please call init function");
        }
        if (content == null || content.isEmpty() || topic == null || topic.isEmpty()) {
            LOGGER.error("content or topic is null or empty");
            throw new Exception("content or topic is null or empty, notice ");
        }
//        if (!msgStorage.isInTopicLists(topic)) {
//            LOGGER.error("wan't to send msg in topic " + topic + " which is not in topicLists of config, can't resend if send failed");
//            throw new Exception("wan't to send msg in topic " + topic + " which is not in topicLists of config, can't resend if send failed");
//        }
        if (delay < MIN_DELAY || delay > MAX_DELAY) {
            LOGGER.error("delay can't <" + MIN_DELAY + " or > " + MAX_DELAY);
            throw new Exception("delay can't <" + MIN_DELAY + " or > " + MAX_DELAY);
        }
        try {
            LOGGER.debug("insert to msgTable topic {} tag {} Connection {} Autocommit {} ", topic, tag, con, con.getAutoCommit());
            if (con.getAutoCommit()) {
                LOGGER.error("***** attention not in transaction ***** topic {} tag {} Connection {} Autocommit {} ", topic, tag, con, con.getAutoCommit());
                throw new Exception("connection not in transaction con " + con);
            }

            // 2、双写消息，先写DB，在写queue
            MessageInfo messageInfo = new MessageInfo();
            messageInfo.setTopic("topic1");
            messageInfo.setTag("tag");
            messageInfo.setStatus(0);
            messageInfo.setContent(content);
            messageInfo.setDelay(delay);
            messageInfoMapper.insert(messageInfo);

            MQMessage msg = new MQMessage(id, null);
            msgProcessor.putMsg(msg);
        } catch (Exception ex) {
            LOGGER.error("sendMsg fail topic {} tag {} ", topic, tag, ex);
            throw ex;
        }
        return id;
    }
}
