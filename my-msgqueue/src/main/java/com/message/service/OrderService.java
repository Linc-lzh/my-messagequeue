package com.message.service;

public interface OrderService {
    void insertOrder(String content) throws Exception;

    void insertDelayOrder(String content, int delay) throws Exception;
}
