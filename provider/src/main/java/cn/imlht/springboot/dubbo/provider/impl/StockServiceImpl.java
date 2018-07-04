package cn.imlht.springboot.dubbo.provider.impl;

import cn.imlht.springboot.dubbo.api.StockService;
import cn.imlht.springboot.dubbo.domain.StockOrder;
import cn.imlht.springboot.dubbo.provider.exception.OutOfStockException;
import cn.imlht.springboot.dubbo.provider.mapper.StockMapper;
import cn.imlht.springboot.dubbo.provider.mapper.StockOrderMapper;
import cn.imlht.springboot.dubbo.provider.mq.Tag;
import cn.imlht.springboot.dubbo.provider.variable.StockNum;
import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.shade.com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("stockService")
public class StockServiceImpl implements StockService {

    private final static Logger logger = Logger.getLogger(StockServiceImpl.class);

    @Resource
    private Producer producer;

    @Value("${ons.client.topicStock}")
    private String topic;

    @Value("${redis.keys.stock.sale.num}")
    private String saleNumKey;

    @Resource
    private StockMapper stockMapper;

    @Resource
    private StockOrderMapper stockOrderMapper;

    @Resource(name="redisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private StockNum stockNum;

    @Override
    @Transactional
    public void saleMysql(StockOrder stockOrder) {
        long stockNum = stockMapper.findStockNum(stockOrder.getStockId());
        if (stockNum <= 0L) {
            throw new OutOfStockException("MySQL select: Out of stock!");
        }

        int updateFlag = stockMapper.updateStockNum(stockOrder.getStockId(), stockOrder.getBuyNum());
        if (updateFlag < 1) {
            throw new OutOfStockException("MySQL update: Out of stock!");
        }

        int saveFlag = stockOrderMapper.save(stockOrder);
        if (saveFlag < 1) {
            throw new RuntimeException("MySQL save error!");
        }

        logger.info("Success!");
    }

    @Override
    public void saleRedis(StockOrder stockOrder) {

        if (valueOperations.increment(saleNumKey, stockOrder.getBuyNum()) >= stockNum.get(stockOrder.getStockId())) {
            throw new OutOfStockException("Redis incr: Out of stock!");
        } else {
            Message message = new Message(topic, Tag.SALE.name(), JSON.toJSONString(stockOrder).getBytes());
            producer.sendAsync(message, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    logger.info("Send success.");
                }

                @Override
                public void onException(OnExceptionContext context) {
                    logger.info("Send error.");
                }
            });
        }
    }


}
