package com.message.mapper;

import com.message.model.MessageInfo;

import java.util.List;

public interface MessageInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int deleteByStatus(Integer status);

    int insert(MessageInfo messageInfo);

    MessageInfo selectByPrimaryKey(Integer id);

    List<MessageInfo> selectAll();

    List<MessageInfo> selectByStatus(Integer status);

    int updateByPrimaryKey(MessageInfo messageInfo);

    int setConfirm(MessageInfo messageInfo);
}
