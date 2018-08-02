package cn.imlht.springboot.dubbo.provider.config;

import cn.imlht.springboot.dubbo.provider.sysval.ZooKeeperVal;
import org.I0Itec.zkclient.ZkClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class ZookeeperConfig {

    @Resource
    private ZooKeeperVal zooKeeperVal;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework zkCurator() {
        return CuratorFrameworkFactory.builder()
                .connectString(zooKeeperVal.getUrl())
                .sessionTimeoutMs(zooKeeperVal.getSessionTimeout())
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();
    }

    @Bean
    public ZkClient zkClient() {
        return new ZkClient(zooKeeperVal.getUrl(), zooKeeperVal.getSessionTimeout());
    }

}
