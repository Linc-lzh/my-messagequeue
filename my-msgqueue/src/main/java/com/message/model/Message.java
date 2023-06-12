package com.message.model;

public class Message {
    /**
     * 主键
     */
    private Long id;

    /**
     * db-url key, 跟 数据源map做映射
     */
    private String url;

    /**
     * 已经处理次数
     */
    private int haveDealedTimes;

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 下次超时时间
     */
    private long nextExpireTime;

    public Message(Long id, String url) {
        this.id = id;
        this.url = url;
        this.haveDealedTimes = 0;
        this.createTime = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getHaveDealedTimes() {
        return haveDealedTimes;
    }

    public void setHaveDealedTimes(int haveDealedTimes) {
        this.haveDealedTimes = haveDealedTimes;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getNextExpireTime() {
        return nextExpireTime;
    }

    public void setNextExpireTime(long nextExpireTime) {
        this.nextExpireTime = nextExpireTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", haveDealedTimes=" + haveDealedTimes +
                ", createTime=" + createTime +
                ", nextExpireTime=" + nextExpireTime +
                '}';
    }
}
