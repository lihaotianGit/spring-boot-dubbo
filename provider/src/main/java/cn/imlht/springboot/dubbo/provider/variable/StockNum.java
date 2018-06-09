package cn.imlht.springboot.dubbo.provider.variable;

import cn.imlht.springboot.dubbo.provider.mapper.StockMapper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StockNum {

    private final static Logger logger = Logger.getLogger(StockNum.class);

    private final ConcurrentHashMap<Long, Long> stockMap = new ConcurrentHashMap<>();

    @Resource
    private StockMapper stockMapper;

    public long get(Long id) {
        if (stockMap.containsKey(id)) {
            return stockMap.get(id);
        } else {
            long stockNum = stockMapper.findStockNum(id);
            stockMap.put(id, stockNum);
            logger.info("Put id: " + id + ", stock: " + stockNum + " in stockMap.");
            return stockNum;
        }
    }

}
