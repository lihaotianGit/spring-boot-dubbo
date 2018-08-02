package cn.imlht.springboot.dubbo.provider;

import org.apache.curator.framework.CuratorFramework;
import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ZkCuratorTest {

    private final static Logger logger = Logger.getLogger(ZkCuratorTest.class);

    @Resource
    private CuratorFramework zkCurator;

    private final static String path = "/base/test";

    @Test
    public void should_create() throws Exception {

        String result = zkCurator.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path);

        logger.info(result);
        loggingChildren();
    }

    @Test
    public void should_delete() throws Exception {
        should_create();
        zkCurator.delete().guaranteed().deletingChildrenIfNeeded().forPath("/base");
        loggingChildren();
    }

    private void loggingChildren() throws Exception {
        try {
            List<String> results = zkCurator.getChildren().forPath("/base");
            for (String r : results) {
                logger.info(r);
            }
        } catch (KeeperException.NoNodeException e) {
            logger.info("Node not exist.");
        }
    }
}
