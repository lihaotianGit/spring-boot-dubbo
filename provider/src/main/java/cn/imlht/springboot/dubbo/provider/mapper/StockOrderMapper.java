package cn.imlht.springboot.dubbo.provider.mapper;

import cn.imlht.springboot.dubbo.domain.StockOrder;
import org.apache.ibatis.annotations.Param;

public interface StockOrderMapper {

    int save(@Param("stockOrder") StockOrder stockOrder);

}