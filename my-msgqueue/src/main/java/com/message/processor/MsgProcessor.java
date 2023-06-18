package com.message.processor;

import com.message.mapper.MessageInfoMapper;
import com.message.model.MQMessage;
import com.message.model.MessageInfo;
import com.message.model.MyConstants;
import com.message.model.State;
import com.message.util.NxThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MsgProcessor {
    private static final int DEF_TIMEOUT_MS = 100;

    /**
     * 处理间隔数组(6次): 0s, 5s, 10s, 25s, 50s, 100s
     */
    private static final int[] TIMEOUT_DATA = new int[] {0, 5, 10, 25, 50, 100};

    /**
     * 最大处理次数
     */
    private static final int MAX_DEAL_TIME = 6;

    /**
     * 等待消息分页
     */
    private static final int LIMIT_NUM = 50;

    private static final int MAX_DEAL_NUM_ONE_TIME = 2000;

    /**
     * 时间论转动频率
     */
    private static final int TIME_WHEEL_PERIOD = 5;

    private static final Logger LOGGER = LoggerFactory.getLogger(MsgProcessor.class);

    /**
     * 事务操作消息队列
     */
    private PriorityBlockingQueue<MQMessage> msgQueue;

    /**
     * 事务消息执行线程池
     */
    private ExecutorService exeService;

    /**
     * 时间轮检查队列：时间轮投递(事务操作)
     */
    private PriorityBlockingQueue<MQMessage> timeWheel;

    /**
     * 定时线程池：其他线程
     */
    private ScheduledExecutorService scheService;

    /**
     * 事务消息服务状态
     */
    private AtomicReference<State> state;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private MessageInfoMapper messageInfoMapper;

    public MsgProcessor() {
        msgQueue = new PriorityBlockingQueue<MQMessage>(5000, new Comparator<MQMessage>() {
            @Override
            public int compare(MQMessage o1, MQMessage o2) {
                long diff = o1.getCreateTime() - o2.getCreateTime();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });
        timeWheel = new PriorityBlockingQueue<MQMessage>(1000, new Comparator<MQMessage>() {
            @Override
            public int compare(MQMessage o1, MQMessage o2) {
                long diff = o1.getNextExpireTime() - o2.getNextExpireTime();
                if (diff > 0) {
                    return 1;
                } else if (diff < 0) {
                    return -1;
                }
                return 0;
            }
        });
        state = new AtomicReference<State>(State.CREATE);
    }

    public void putMsg(MQMessage msg) {
        msgQueue.put(msg);
    }

    public void init() {
        if (state.get().equals(State.RUNNING)) {
            LOGGER.info("Msg Processor have inited return");
            return;
        }

        rabbitTemplate.setMandatory(true);

        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                Message returnedMessage = correlationData.getReturnedMessage();
                MQMessage msg = MQMessage.fromBytes(returnedMessage.getBody());
                if(ack){
                    LOGGER.debug("msgId {} updateMsgStatus success", correlationData.getId());
                    MessageInfo messageInfo = new MessageInfo();
                    messageInfo.setId(Integer.valueOf(correlationData.getId()));
                    messageInfoMapper.setConfirm(messageInfo);
                }
                else {
                    if (msg.getHaveDealedTimes() < MAX_DEAL_TIME) {
                        long nextExpireTime = System.currentTimeMillis() + TIMEOUT_DATA[msg.getHaveDealedTimes()];
                        msg.setNextExpireTime(nextExpireTime);
                        timeWheel.put(msg);
                        // 这里可以优化 ，因为已经确认事务提交了，可以从DB中拿到了
                        LOGGER.debug("put msg in timeWhellQueue {} ", msg);
                    }
                }
            }
        });
        LOGGER.info("MsgProcessor init start");
        state.compareAndSet(State.CREATE, State.RUNNING);

        exeService = Executors.newFixedThreadPool(10, new NxThreadFactory("MsgProcessorThread-"));
        for (int i = 0; i < 10; i++) {
            exeService.submit(new MsgDeliverTask());
        }

        scheService = Executors.newScheduledThreadPool(10, new NxThreadFactory("MsgScheduledThread-"));

        scheService.scheduleAtFixedRate(new TimeWheelTask(), TIME_WHEEL_PERIOD, TIME_WHEEL_PERIOD, TimeUnit.MILLISECONDS);

        scheService.scheduleAtFixedRate(new CleanMsgTask(), 180 , 180, TimeUnit.SECONDS);

        scheService.scheduleAtFixedRate(new ScanMsgTask(), 10, 60, TimeUnit.SECONDS);

        scheService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                LOGGER.info("stats info msgQueue size {} timeWheelQueue size {}", msgQueue.size(), timeWheel.size());
            }
        }, 20, 120, TimeUnit.SECONDS);
    }

    private void sendMsg(String topic, Integer messageId, String message, @Nullable MQMessage msg) throws UnsupportedEncodingException {
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        messageProperties.setPriority(2);

        CorrelationData correlationData = new CorrelationData(messageId.toString());
        if(msg != null){
            correlationData.setReturnedMessage(new Message(msg.asBytesArray(), messageProperties));
        }

        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        rabbitTemplate.send(MyConstants.EXCHANGE_NAME,
                topic,
                new Message(message.getBytes("UTF-8"), messageProperties),
                correlationData);
    }

    class MsgDeliverTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (!state.get().equals(State.RUNNING)) {
                    break;
                }
                try {
                    // 1、每100ms从 队列 弹出一条事务操作消息
                    MQMessage msg = null;
                    try {
                        msg = msgQueue.poll(DEF_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                    }
                    if (msg == null) {
                        continue;
                    }
                    LOGGER.debug("poll msg {}", msg);
                    int dealedTime = msg.getHaveDealedTimes() + 1;
                    msg.setHaveDealedTimes(dealedTime);
                    MessageInfo msgInfo = messageInfoMapper.selectByPrimaryKey(msg.getId());
                    LOGGER.debug("getMsgInfo from DB {}", msgInfo);
                    if (msgInfo == null) {
                        if (dealedTime < MAX_DEAL_TIME) {
                            // 3.1、加入时间轮转动队列:重试投递
                            long nextExpireTime = System.currentTimeMillis() + TIMEOUT_DATA[dealedTime];
                            msg.setNextExpireTime(nextExpireTime);
                            timeWheel.put(msg);
                            LOGGER.debug("put msg in timeWhellQueue {} ", msg);
                        }
                    } else {
                        // 3.2、投递事务消息
                        LOGGER.debug("will sendMsg {}", msgInfo.getContent());
                        if(msgInfo.getDelay() == 0){
                            sendMsg(msgInfo.getTopic(), msgInfo.getId(), msgInfo.getContent(), msg);
                        }

                    }
                } catch (Throwable t) {
                    LOGGER.error("MsgProcessor deal msg fail", t);
                }
            }
        }
    }

    class TimeWheelTask implements Runnable {
        @Override
        public void run() {
            try {
                if (state.get().equals(State.RUNNING)) {
                    long cruTime = System.currentTimeMillis();
                    MQMessage msg = timeWheel.peek();
                    // 拿出来的时候有可能还没有超时
                    while (msg != null && msg.getNextExpireTime() <= cruTime) {
                        msg = timeWheel.poll();
                        LOGGER.debug("timeWheel poll msg ,return to msgQueue {}", msg);
                        // 重新放进去
                        msgQueue.put(msg);
                        msg = timeWheel.peek();
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("pool timequeue error", ex);
            }
        }
    }

    class CleanMsgTask implements Runnable {
        @Override
        public void run() {
            if (state.get().equals(State.RUNNING)) {
                LOGGER.debug("DeleteMsg start run");
                try {
                    messageInfoMapper.deleteByStatus(1);
                } catch (Exception ex) {
                    LOGGER.error("delete Run error ", ex);
                }
            }
        }
    }

    class ScanMsgTask implements Runnable {
        @Override
        public void run() {
            if (state.get().equals(State.RUNNING)) {
                LOGGER.debug("SchedScanMsg start run");
                List<MessageInfo> list = messageInfoMapper.selectByStatus(0);
                int num = list.size();
                if (num > 0) {
                    LOGGER.debug("scan db get msg size {} ", num);
                }

                for (MessageInfo msgInfo : list) {
                    try {
                        if(msgInfo.getDelay() > 0){
                            long sendTime = msgInfo.getCreateAt() + msgInfo.getDelay() * 60 * 1000;
                            long now = System.currentTimeMillis();
                            MQMessage msg = new MQMessage(msgInfo.getId());
                            if(sendTime <= now)
                                sendMsg(msgInfo.getTopic(), msgInfo.getId(), msgInfo.getContent(), msg);
                        }
                        else
                            sendMsg(msgInfo.getTopic(), msgInfo.getId(), msgInfo.getContent(), null);

                    } catch (Exception e) {
                        LOGGER.error("SchedScanMsg deal fail", e);
                    }
                }
            }
        }

    }
}

