package cn.imlht.springboot.dubbo.provider.config;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

import javax.annotation.Resource;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

@Configuration
public class RedisConfig {

    private final static Logger logger = Logger.getLogger(RedisConfig.class);

    @Resource
    private RedisProperties redisProperties;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(JedisConnectionFactory conn) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(conn);

//        RedisSerializer<String> redisSerializer = new StringRedisSerializer();
//        redisTemplate.setKeySerializer(redisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setHashKeySerializer(redisSerializer);
//
//        JdkSerializationRedisSerializer jdkRedisSerializer = new JdkSerializationRedisSerializer();
//        redisTemplate.setValueSerializer(jdkRedisSerializer);
        redisTemplate.setValueSerializer(new StringRedisSerializer());
//        redisTemplate.setHashValueSerializer(jdkRedisSerializer);

        redisTemplate.afterPropertiesSet();
        logger.info("RedisTemplate init success.");
        return redisTemplate;
    }

    @Bean(destroyMethod = "close")
    public JedisPool jedisPool(JedisConnectionFactory conn) {
        if (StringUtils.isEmpty(conn.getPassword())) {
            return new JedisPool(conn.getPoolConfig(), conn.getHostName(), conn.getPort(), conn.getTimeout());
        } else {
            return new JedisPool(conn.getPoolConfig(), conn.getHostName(), conn.getPort(), conn.getTimeout(), conn.getPassword());
        }
    }

    @Bean(destroyMethod = "close")
    public Pool<Jedis> jedisSentinelPool(JedisConnectionFactory conn) {
        Set<String> sentinels = new HashSet<>(asList(redisProperties.getSentinel().getNodes().split(",")));
        if (StringUtils.isEmpty(conn.getPassword())) {
            return new JedisSentinelPool(redisProperties.getSentinel().getMaster(), sentinels, conn.getPoolConfig(), conn.getTimeout());
        } else {
            return new JedisSentinelPool(redisProperties.getSentinel().getMaster(), sentinels, conn.getPoolConfig(), conn.getTimeout(), conn.getPassword());
        }
    }

}
