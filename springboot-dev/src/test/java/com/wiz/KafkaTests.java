package com.wiz;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Description:
 * @Create: 2022-03-30-14:39
 * @Author: Hey
 */
@SpringBootTest
public class KafkaTests {

    @Autowired
    private Producer producer;

    @Test
    public void testKafka(){
        producer.sendMessage("test","我在");
        producer.sendMessage("test","干嘛呢");
        try {
            Thread.sleep(1000*3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test1(){
        System.out.println(new Date());
    }

}
@Component
class Producer{
    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void sendMessage(String topic, String content) {
        kafkaTemplate.send(topic,content);
    }
}
@Component
class Consumer{
    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record) {
        System.out.println(record.value());
    }
}