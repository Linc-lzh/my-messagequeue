package com.message.model;

public enum State {
    /**
     * 服务创建态(非开始)
     */
    CREATE,
    /**
     * 服务运行态
     */
    RUNNING,
    /**
     * 服务关闭态
     */
    CLOSED,
    /**
     * 服务失败态
     */
    FAILED;
}
