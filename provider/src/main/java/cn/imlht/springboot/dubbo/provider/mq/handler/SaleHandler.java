package cn.imlht.springboot.dubbo.provider.mq.handler;

import cn.imlht.springboot.dubbo.api.StockService;
import cn.imlht.springboot.dubbo.domain.StockOrder;
import cn.imlht.springboot.dubbo.provider.mq.MessageHandler;
import cn.imlht.springboot.dubbo.util.JsonHelper;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.Charset;

@Component
public class SaleHandler implements MessageHandler {

    @Resource
    private StockService stockService;

    @Override
    public void handle(Message message, ConsumeContext context) {
        StockOrder stockOrder = JsonHelper.toObject(new String(message.getBody(), Charset.forName("UTF-8")), StockOrder.class);
        stockService.saleMysql(stockOrder);
    }

}
