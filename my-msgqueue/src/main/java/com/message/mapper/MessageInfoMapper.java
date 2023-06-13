package com.message.mapper;

import com.message.model.MessageInfo;
import com.message.model.Order;

import java.util.List;

public interface MessageInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(MessageInfo messageInfo);

    Order selectByPrimaryKey(Integer id);

    List<Order> selectAll();

    int updateByPrimaryKey(MessageInfo messageInfo);
}
