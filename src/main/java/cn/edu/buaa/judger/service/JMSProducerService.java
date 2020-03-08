package cn.edu.buaa.judger.service;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class JMSProducerService {
    @Autowired
    private JmsMessagingTemplate jmsMessagingTemplate;

    /**
     * 默认的消息队列名称
     */
    private static final String DESTINATION_NAME = "submission_queue";

    /**
     * 向默认消息队列发送Map<String, Object>类型消息
     * @param message - 消息
     */
    public void sendMessage(Map<String, Object> message){
        sendMessage(DESTINATION_NAME, message);
    }

    /**
     * 向指定消息队列发送Map<String, Object>类型消息
     * @param destinationName - 目的消息队列名称
     * @param message - 消息
     */
    public void sendMessage(String destinationName, Map<String, Object> message){
        jmsMessagingTemplate.convertAndSend(new ActiveMQQueue(destinationName), message);
    }
}
