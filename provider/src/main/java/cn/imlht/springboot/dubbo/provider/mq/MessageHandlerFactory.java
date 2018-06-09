package cn.imlht.springboot.dubbo.provider.mq;

import cn.imlht.springboot.dubbo.provider.mq.handler.SaleHandler;
import com.google.common.collect.ImmutableMap;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class MessageHandlerFactory {

    @Resource
    private ApplicationContext applicationContext;

    private Map<String, Class> handlerMap = ImmutableMap.<String, Class>builder()
            .put(Tag.SALE.name(), SaleHandler.class)
            .build();

    public MessageHandler getHandler(String tag) {
        return (MessageHandler) applicationContext.getBean(handlerMap.get(tag));
    }

}
