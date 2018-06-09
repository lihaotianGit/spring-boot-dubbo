package cn.imlht.springboot.dubbo.provider.mq;

import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;

public interface MessageHandler {

    void handle(Message message, ConsumeContext context);

}
