package com.message.client.impl;

import com.message.client.TransactionMsgClient;
import com.message.mapper.MessageInfoMapper;
import com.message.processor.MsgProcessor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.sql.Connection;

@Component
public class MybatisTransactionMsgClient extends TransactionMsgClient implements InitializingBean {

    private SqlSessionTemplate sessionTemplate;

    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private MessageInfoMapper messageInfoMapper;

    @Autowired
    private MsgProcessor msgProcessor;

    public MybatisTransactionMsgClient(){
        super();
    }

    @Autowired
    public MybatisTransactionMsgClient(SqlSessionFactory sqlSessionFactory) {
        super();
        this.sqlSessionFactory = sqlSessionFactory;
        try {
            this.sessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        } catch (Exception e) {
            // Auto-generated catch block
            LOGGER.error("get sqlSessionFactory fail", e);
        }
    }

    @PostConstruct
    public void init() throws Exception {
        super.init();
    }

    @PreDestroy
    public void close(){
        super.close();
    }

    @Override
    public Long sendMsg(String content, String topic, String tag)
            throws Exception {
        // Auto-generated method stub
        Long id = null;
        try {
            Connection con = sessionTemplate.getConnection();
            id = super.sendMsg(con, content, topic, tag, 0);
            return id;
        } catch (Exception ex) {
            // Auto-generated catch block
            LOGGER.error("sendMsg fail topic {} tag {} ", topic, tag, ex);
            throw new RuntimeException(ex);
        }
    }

    public Long sendMsg(String content, String topic, String tag, int delay)
            throws Exception {
        // Auto-generated method stub
        Long id = null;
        try {
            Connection con = sessionTemplate.getConnection();
            id = super.sendMsg(con, content, topic, tag, delay);
            return id;
        } catch (Exception ex) {
            // Auto-generated catch block
            LOGGER.error("sendMsg fail topic {} tag {} delay {}", topic, tag, delay, ex);
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.setMessageInfoMapper(this.messageInfoMapper);
        super.setMsgProcessor(this.msgProcessor);
    }
}
