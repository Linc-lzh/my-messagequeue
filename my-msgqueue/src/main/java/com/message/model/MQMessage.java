package com.message.model;

import java.io.*;

public class MQMessage implements Serializable {
    /**
     * 主键
     */
    private Integer id;

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

    public MQMessage(Integer id) {
        this.id = id;
        this.haveDealedTimes = 0;
        this.createTime = System.currentTimeMillis();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public byte[] asBytesArray(){
        byte[] bytes = null;
        try{
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(this);
            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bytes;
    }

    public static MQMessage fromBytes(byte[] bytes){
        MQMessage result = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(bis);
            result = (MQMessage) ois.readObject();

            ois.close();
            bis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String toString() {
        return "MQMessage{" +
                "id=" + id +
                ", haveDealedTimes=" + haveDealedTimes +
                ", createTime=" + createTime +
                ", nextExpireTime=" + nextExpireTime +
                '}';
    }
}
