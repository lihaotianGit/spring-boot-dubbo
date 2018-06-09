package cn.imlht.springboot.dubbo.domain;

import java.io.Serializable;
import java.util.Date;

public class StockOrder implements Serializable {

    private long id;

    private long stockId;

    private long userId;

    private int buyNum;

    private Date createTime;

    public StockOrder() {
    }

    public StockOrder(long stockId, long userId, int buyNum) {
        this.stockId = stockId;
        this.userId = userId;
        this.buyNum = buyNum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getBuyNum() {
        return buyNum;
    }

    public void setBuyNum(int buyNum) {
        this.buyNum = buyNum;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "StockOrder{" +
                "id=" + id +
                ", stockId=" + stockId +
                ", userId=" + userId +
                ", buyNum=" + buyNum +
                ", createTime=" + createTime +
                '}';
    }
}