package cn.imlht.springboot.dubbo.api;

import cn.imlht.springboot.dubbo.domain.StockOrder;

public interface StockService {

    void saleMysql(StockOrder stockOrder);

    void saleRedis(StockOrder stockOrder);
}
