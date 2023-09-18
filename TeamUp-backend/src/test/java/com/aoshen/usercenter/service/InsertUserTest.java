package com.aoshen.usercenter.service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.aoshen.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;



@SpringBootTest
public class InsertUserTest {
    @Resource
    private UserService userService;

//    private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000,
//            TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    @Test
    void testInsert() {
        int j = 0;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int batchSize = 5000;
        // 定义一个可并发的list 集合
        List<CompletableFuture<Void>> futureList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUsername("测试");
                user.setUserAccount("0204315");
                user.setAvatarUrl("https://tse4-mm.cn.bing.net/th/id/OIP-C.uqOSAcSCSv0wHdUNCd-AnAAAAA?w=196&h=196&c=7&r=0&o=5&dpr=1.7&pid=1.7");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("18702680721");
                user.setEmail("2449829379@qq.com");
                user.setUserStatus(0);
                user.setTags("[\"java\", \"大二\", \"python\", \"c++\",\"男\"]");
                user.setProfile("全栈工程师，加油加油加油！！！！！");
                user.setUserRole(0);
                user.setPlanetCode("9999");
                userList.add(user);
                if (j % 10000 == 0) {
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                System.out.println("threadName:"+Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            });
            futureList.add(future);
        }
        // 执行异步处理，（join 作用是异步请求都结束后，才进行下一步）
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }


}
