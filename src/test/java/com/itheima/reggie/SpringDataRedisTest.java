package com.itheima.reggie;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;
import java.util.Set;

// 测试路径需要和上面的启动类路径一致
@SpringBootTest
@RunWith(SpringRunner.class)
public class SpringDataRedisTest {
    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void TestRedis() {
        // 序列化
        redisTemplate.opsForValue().set("test33", "33aft");
    }

    @Test
    public void TestHash() {
        HashOperations ops = redisTemplate.opsForHash();

        ops.put("002", "name", "xiaom");
        ops.put("002", "age", "20");

        System.out.println((String) ops.get("002", "age"));

        // keys
        Set keys = ops.keys("002");
        for (Object key : keys) {
            System.out.println(key);
        }

        // values
        for (Object value : ops.values("002")) {
            System.out.println(value);
        }
    }

    @Test
    public void TestList() {
        ListOperations lops = redisTemplate.opsForList();
        //lops.leftPushAll("mylist", "b", "c", "d", "e");

        // 从尾部取了
        for (Object item : lops.range("mylist", 0, -1)) {
            System.out.println(item);
        }

        Long size = lops.size("mylist");
        for (int i = 0; i < size.intValue(); i++) {
            Object o = lops.rightPop("mylist");
            System.out.println(o);
        }
    }

    @Test
    public void TestSet() {
        SetOperations sops = redisTemplate.opsForSet();
        sops.add("myset", "a", "a", "b", "c", "d");

        for (Object myset : sops.members("myset")) {
            System.out.println(myset);
        }

        sops.remove("myset", "a", "b");

        System.out.println("1111");
        for (Object myset : sops.members("myset")) {
            System.out.println(myset);
        }
    }
}
