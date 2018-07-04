package cn.imlht.springboot.dubbo.consumer.endpoint;

import cn.imlht.springboot.dubbo.api.StockService;
import cn.imlht.springboot.dubbo.domain.StockOrder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/stock")
public class StockEndpoint {

    @Resource
    private StockService stockService;

    @PostMapping("mysql")
    public String saleMysql(@RequestBody StockOrder stockOrder) {
        stockService.saleMysql(stockOrder);
        return "success";
    }

    @PostMapping("redis")
    public String saleRedis(@RequestBody StockOrder stockOrder) {
        stockService.saleRedis(stockOrder);
        return "success";
    }

}
