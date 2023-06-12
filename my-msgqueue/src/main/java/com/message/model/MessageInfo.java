package com.message.model;

import java.util.Date;

public class MessageInfo {
    private Long id;

    /**
     * 事务消息
     */
    private String content;

    /**
     * 主题
     */
    private String topic;

    /**
     * 标签
     */
    private String tag;

    /**
     * 状态：1-等待，2-发送
     */
    private int status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 延迟时间(单位：s)
     */
    private int delay;

    public MessageInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public String toString() {
        return "MessageInfo{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", topic='" + topic + '\'' +
                ", tag='" + tag + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", delay=" + delay +
                '}';
    }
}