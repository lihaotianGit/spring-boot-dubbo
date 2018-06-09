package cn.imlht.springboot.dubbo.provider.mapper;

import org.apache.ibatis.annotations.Param;

public interface StockMapper {

    long findStockNum(@Param("stockId") long stockId);

    int updateStockNum(@Param("stockId") long stockId, @Param("buyNum") int buyNum);

}