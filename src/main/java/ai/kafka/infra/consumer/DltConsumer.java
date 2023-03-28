package ai.kafka.infra.consumer;

import ai.kafka.infra.dto.ProducerMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DltConsumer {

    @KafkaListener(
            topics = "${spring.kafka.consumer.dead-letter.topic}",
            groupId = "${spring.kafka.consumer.dead-letter.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            errorHandler = "kafkaErrorHandler"
    )
    @SendTo("failure")
    public void listen(@Payload ProducerMessageRequest kafkaMessageRequest,
                       ConsumerRecord<String, ProducerMessageRequest> consumerRecord,
                       Acknowledgment acknowledgment) {
        log.info("### record: " + consumerRecord.toString());
        log.info("### topic: " + consumerRecord.topic() + ", value: " + consumerRecord.value() + ", offset: " + consumerRecord.offset());
        log.info("===================================================================");

        // acknowledgment.acknowledge();
    }
}
