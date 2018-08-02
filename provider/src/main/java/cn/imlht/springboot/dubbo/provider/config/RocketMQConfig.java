package cn.imlht.springboot.dubbo.provider.config;

import cn.imlht.springboot.dubbo.provider.exception.OutOfStockException;
import cn.imlht.springboot.dubbo.provider.mq.MessageHandlerFactory;
import com.aliyun.openservices.ons.api.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Properties;

@Configuration
public class RocketMQConfig {

    private final static Logger logger = Logger.getLogger(RedisConfig.class);

    @Resource
    private MessageHandlerFactory messageHandlerFactory;

    @Bean
    @ConfigurationProperties(prefix = "ons.client")
    // 配置选项参考com.aliyun.openservices.ons.api.PropertyKeyConst
    public Properties onsProperties() {
        return new Properties();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Producer producer(@Qualifier("onsProperties") Properties onsProperties) {
        onsProperties.put(PropertyKeyConst.ProducerId, onsProperties.getProperty("producerId"));
        logger.info("Starting producer, properties: " + onsProperties.toString());
        return ONSFactory.createProducer(onsProperties);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public Consumer consumer(@Qualifier("onsProperties") Properties onsProperties) {
        onsProperties.put(PropertyKeyConst.ConsumerId, onsProperties.getProperty("consumerId"));
        logger.info("Starting consumer, properties: " + onsProperties.toString());
        Consumer consumer = ONSFactory.createConsumer(onsProperties);
        consumer.subscribe(onsProperties.getProperty("topicStock"), "*", (message, context) -> {
            logger.info("Receive msg: " + message);
            try {
                messageHandlerFactory.getHandler(message.getTag()).handle(message, context);
                return Action.CommitMessage;
            } catch (OutOfStockException e) {
                // 如果是库存不够就消费掉消息
                logger.error("Out of stock. ");
                return Action.CommitMessage;
            } catch (Exception e) {
                logger.error("Consume message error. ", e);
                return Action.ReconsumeLater;
            }
        });
        return consumer;
    }

}
