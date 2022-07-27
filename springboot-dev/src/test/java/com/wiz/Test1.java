package com.wiz;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Description:
 * @Create: 2022-07-25-11:02
 * @Author: Hey
 */
@SpringBootTest
public class Test1 {
    @Test
    public void testRandom() {
        int num = (int) Math.random() * 100000;
        String verifycode = num + "";
        System.out.println(verifycode);
    }


}
