package cn.imlht.springboot.dubbo.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class User implements Serializable {

    private long id;
    private String name;
    private BigDecimal amount;
    private Timestamp createdAt;

    public User() {
    }

    public User(String name, BigDecimal amount) {
        this.name = name;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                '}';
    }
}
