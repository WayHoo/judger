package cn.edu.buaa.judger;

import cn.edu.buaa.judger.service.JMSProducerService;
import cn.edu.buaa.judger.utils.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class MessageTest extends JudgerApplicationTests {

    @Resource
    private JMSProducerService jmsProducerService;

    @Test
    public void testSendMessage(){
        Map<String, Object> message = new HashMap<>();
        message.put("submissionId", 123456);
        message.put("contestId", 1);
        message.put("problemId", 1);
        message.put("submitTime", DateUtil.formatTimestamp(DateUtil.getCurrentTimestamp()));
        message.put("submitCode", "#include <iostream>; int void mian(){ cout << \"hello world\";}");
        System.out.println("============>>>>> 发送消息, " + message);
        jmsProducerService.sendMessage(message);
    }
}
