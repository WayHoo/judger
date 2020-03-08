package cn.edu.buaa.judger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;

@Service
public class JMSConsumerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(JMSConsumerService.class);

    @JmsListener(destination = "submission_queue")
    public void receiveMessage(Message message){
        if( message instanceof MapMessage){
            final MapMessage mapMessage = (MapMessage)message;
            try {
                long submissionId = mapMessage.getLong("submissionId");
                if( submissionId > 0 ){
                    LOGGER.info("<<<<<<============ 收到待评测消息，submissionId = " + submissionId);
                } else{
                    LOGGER.warn("Invalid submissionId received.");
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
