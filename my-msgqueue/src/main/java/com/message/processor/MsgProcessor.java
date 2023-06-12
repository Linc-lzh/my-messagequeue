package com.message.processor;

import com.message.model.Message;
import com.message.model.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

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
    private PriorityBlockingQueue<Message> msgQueue;

    /**
     * 事务消息执行线程池
     */
    private ExecutorService exeService;

    /**
     * 时间轮检查队列：时间轮投递(事务操作)
     */
    private PriorityBlockingQueue<Message> timeWheel;

    /**
     * 定时线程池：其他线程
     */
    private ScheduledExecutorService scheService;

    /**
     * 事务消息服务状态
     */
    private AtomicReference<State> state;
}
