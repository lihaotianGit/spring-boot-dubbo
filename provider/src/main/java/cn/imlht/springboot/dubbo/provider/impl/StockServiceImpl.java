package cn.imlht.springboot.dubbo.provider.impl;

import cn.imlht.springboot.dubbo.api.StockService;
import cn.imlht.springboot.dubbo.domain.StockOrder;
import cn.imlht.springboot.dubbo.provider.mapper.StockMapper;
import cn.imlht.springboot.dubbo.provider.mapper.StockOrderMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("stockService")
public class StockServiceImpl implements StockService {

    private final static Logger logger = Logger.getLogger(StockServiceImpl.class);

    @Resource
    private StockMapper stockMapper;

    @Resource
    private StockOrderMapper stockOrderMapper;

    @Override
    @Transactional
    public void sale(StockOrder stockOrder) {
        int stockNum = stockMapper.findStockNum(stockOrder.getStockId());
        if (stockNum <= 0) {
            throw new RuntimeException("Select: Out of stock!");
        }

        int updateFlag = stockMapper.updateStockNum(stockOrder.getStockId(), stockOrder.getBuyNum());
        if (updateFlag < 1) {
            throw new RuntimeException("Update: Out of stock!");
        }

        int saveFlag = stockOrderMapper.save(stockOrder);
        if (saveFlag < 1) {
            throw new RuntimeException("Save error!");
        }

        logger.info("Success!");

    }

}
