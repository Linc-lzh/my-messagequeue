package com.message.test;

import com.message.mapper.MessageInfoMapper;
import com.message.mapper.OrderMapper;
import com.message.model.MessageInfo;
import com.message.model.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyBatisTest {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MessageInfoMapper messageInfoMapper;

    @Test
    public void testInsertOrders(){

        List<Order> orders = orderMapper.selectAll();
        int size = orders.size();

        Order order = new Order("order");
        order.setOrderName("book");
        LocalDateTime localDateTime = LocalDateTime.now();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);
        order.setCreateDate(Date.from(zonedDateTime.toInstant()));
        orderMapper.insert(order);
        orders = orderMapper.selectAll();
        assertEquals(size + 1,orders.size());
    }

    @Test
    public void testInsertMessage(){
        MessageInfo messageInfo = new MessageInfo();
        messageInfo.setContent("test");
        messageInfo.setDelay(0);
        messageInfo.setTag("tag");
        messageInfo.setStatus(0);
        messageInfo.setTopic("topic");
        messageInfo.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
        List<MessageInfo> messages = messageInfoMapper.selectAll();
        int size = messages.size();
        messageInfoMapper.insert(messageInfo);
        List<MessageInfo> actualMessages = messageInfoMapper.selectAll();
        assertEquals(size + 1, actualMessages.size());
    }

    @Test
    public void testSelectAllMessages(){
        List<MessageInfo> messages = messageInfoMapper.selectAll();
        assertEquals(0,messages.size());
    }
}
