package com.message.service.impl;

import com.message.client.TransactionMsgClient;
import com.message.mapper.MessageInfoMapper;
import com.message.mapper.OrderMapper;
import com.message.model.MessageInfo;
import com.message.model.Order;
import com.message.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MessageInfoMapper messageInfoMapper;

    @Autowired
    private TransactionMsgClient transactionMsgClient;
    @Override
    @Transactional
    public void insertOrder(String content) throws Exception {
        int id = 0;
        Order order = new Order("order");
        order.setOrderName("book");
        LocalDateTime localDateTime = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        order.setCreateDate(Date.from(zonedDateTime.toInstant()));
        order.setContent(content);
        id = orderMapper.insert(order);

        transactionMsgClient.sendMsg(content, "topic2", "tag", 0);
    }

    @Override
    @Transactional
    public void insertDelayOrder(String content, int delay) throws Exception {
        int id = 0;
        Order order = new Order("order");
        order.setOrderName("book");
        LocalDateTime localDateTime = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        order.setCreateDate(Date.from(zonedDateTime.toInstant()));
        order.setContent(content);
        id = orderMapper.insert(order);

        transactionMsgClient.sendMsg(content, "topic2", "tag", delay);
    }
}
