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

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MyBatisTest {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MessageInfoMapper messageInfoMapper;

    @Test
    public void testSelectAllOrders(){
        List<Order> orders = orderMapper.selectAll();
        assertEquals(0,orders.size());
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
        List<Order> messages = messageInfoMapper.selectAll();
        int size = messages.size();
        messageInfoMapper.insert(messageInfo);
        List<Order> actualMessages = messageInfoMapper.selectAll();
        assertEquals(size + 1, actualMessages.size());
    }

    @Test
    public void testSelectAllMessages(){
        List<Order> messages = messageInfoMapper.selectAll();
        assertEquals(0,messages.size());
    }
}
