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

import javax.validation.Valid;

@Slf4j
@Component
@RequiredArgsConstructor
public class VendorConsumer {

    @KafkaListener(
            topics = "${spring.kafka.consumer.vendor.topic}",
            groupId = "${spring.kafka.consumer.vendor.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            errorHandler = "kafkaErrorHandler"
    )
    @SendTo("failure")
    public void listen(@Payload @Valid ProducerMessageRequest kafkaMessageRequest,
                       ConsumerRecord<String, ProducerMessageRequest> consumerRecord,
                       Acknowledgment acknowledgment) {
        try {
            // log.info("[customer vendor] received data : {}", consumerRecord);
            log.info("=====================================================================");
            log.info("[customer consumer] kafkaMessageRequest data : {}", kafkaMessageRequest);
            log.info("=====================================================================");

            acknowledgment.acknowledge();
            System.out.println("=====================================================================");
            System.out.println("=====================================================================");
            System.out.println("=====================================================================");
            System.out.println("=====================================================================");
            System.out.println("Thread: " + Thread.currentThread().getName());
            System.out.println("=====================================================================");
            System.out.println("=====================================================================");
            System.out.println("=====================================================================");
            System.out.println("=====================================================================");
            // log.info("[customer vendor] commit Manual Offset : {} ", consumerRecord.offset());
            log.info("=====================================================================");
        } catch (Exception e) {
            //acknowledgment.nack(3000);
            e.printStackTrace();
        }
    }
}
