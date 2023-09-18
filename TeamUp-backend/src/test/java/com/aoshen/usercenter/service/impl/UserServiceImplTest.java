package com.aoshen.usercenter.service.impl;

import com.aoshen.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.aoshen.usercenter.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author ZhangHaoYu
 * @date 2023/3/30 16:34
 */
@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceImplTest {
    @Autowired
    private UserService userService;
    @Test
    void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        IPage<User> userIPage = userService.searchUsersByTags(1L, 8L, tagNameList);
        assertNotNull(userIPage.getRecords());
    }
}