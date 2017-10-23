package cn.imlht.springboot.dubbo.provider;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath:dubbo/*.xml"})
@MapperScan(value = "cn.imlht.springboot.dubbo.provider.mapper")
public class Application {

    public static void main(String[] args) throws InterruptedException {
        SpringApplication.run(Application.class, args);
        // block main thread
        Thread.currentThread().join();
    }

}
