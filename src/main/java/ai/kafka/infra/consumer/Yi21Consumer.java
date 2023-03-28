package ai.kafka.infra.consumer;

import ai.kafka.infra.dto.ProducerMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Yi21Consumer {


    @KafkaListener(
        topics = "${spring.kafka.consumer.YI21.topic}",
        groupId = "${spring.kafka.consumer.YI21.group-id}",
        containerFactory = "kafkaListenerContainerFactory",
        errorHandler = "kafkaErrorHandler"
    )
    @SendTo("failure")
    public void listen(@Payload ProducerMessageRequest kafkaMessageRequest,
        ConsumerRecord<String, ProducerMessageRequest> consumerRecord,
        Acknowledgment acknowledgment) {
        log.info("### record: " + consumerRecord.toString());
        log.info("### topic: " + consumerRecord.topic() + ", value: " + consumerRecord.value() + ", offset: " + consumerRecord.offset());
        throw new KafkaException("무슨무슨 에러가 발생하였다..!!");  // KafkaListenerErrorHandler를 테스트하기 위해 에러를 발생시킵니다.
    }
}
