package com.message.processor;

import com.message.model.MQMessage;
import com.message.model.State;
import com.message.util.NxThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
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
        LOGGER.info("MsgProcessor init start");
        state.compareAndSet(State.CREATE, State.RUNNING);

        exeService = Executors.newFixedThreadPool(10, new NxThreadFactory("MsgProcessorThread-"));
        for (int i = 0; i < 10; i++) {
            exeService.submit(new MsgDeliverTask());
        }
    }

    class MsgDeliverTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (!state.get().equals(State.RUNNING)) {
                    break;
                }
//                try {
//                    // 1、每100ms从 队列 弹出一条事务操作消息
//                    MQMessage msg = null;
//                    try {
//                        msg = msgQueue.poll(DEF_TIMEOUT_MS, TimeUnit.MILLISECONDS);
//                    } catch (InterruptedException ex) {
//                    }
//                    if (msg == null) {
//                        continue;
//                    }
//                    LOGGER.debug("poll msg {}", msg);
//                    int dealedTime = msg.getHaveDealedTimes() + 1;
//                    msg.setHaveDealedTimes(dealedTime);
//                    // 2、从db获取实际事务消息(这里我们不知道是否事务已经提交，所以需要从DB里面拿)
//                    MsgInfo msgInfo = msgStorage.getMsgById(msg);
//                    LOGGER.debug("getMsgInfo from DB {}", msgInfo);
//                    if (msgInfo == null) {
//                        if (dealedTime < MAX_DEAL_TIME) {
//                            // 3.1、加入时间轮转动队列:重试投递
//                            long nextExpireTime = System.currentTimeMillis() + TIMEOUT_DATA[dealedTime];
//                            msg.setNextExpireTime(nextExpireTime);
//                            timeWheel.put(msg);
//                            LOGGER.debug("put msg in timeWhellQueue {} ", msg);
//                        }
//                    } else {
//                        // 3.2、投递事务消息
//                        Message mqMsg = buildMsg(msgInfo);
//                        LOGGER.debug("will sendMsg {}", mqMsg);
//                        SendResult result = producer.send(mqMsg);
//                        LOGGER.info("msgId {} topic {} tag {} sendMsg result {}", msgInfo.getId(), mqMsg.getTopic(), mqMsg.getTags(), result);
//                        if (null == result || result.getSendStatus() != SendStatus.SEND_OK) {
//                            // 投递失败，重入时间轮
//                            if (dealedTime < MAX_DEAL_TIME) {
//                                long nextExpireTime = System.currentTimeMillis() + TIMEOUT_DATA[dealedTime];
//                                msg.setNextExpireTime(nextExpireTime);
//                                timeWheel.put(msg);
//                                // 这里可以优化 ，因为已经确认事务提交了，可以从DB中拿到了
//                                LOGGER.debug("put msg in timeWhellQueue {} ", msg);
//                            }
//                        } else if (result.getSendStatus() == SendStatus.SEND_OK) {
//                            // 投递成功，修改数据库的状态(标识已提交)
//                            int res = msgStorage.updateSendMsg(msg);
//                            LOGGER.debug("msgId {} updateMsgStatus success res {}", msgInfo.getId(), res);
//                        }
//                    }
//                } catch (Throwable t) {
//                    LOGGER.error("MsgProcessor deal msg fail", t);
//                }
            }
        }
    }
}

