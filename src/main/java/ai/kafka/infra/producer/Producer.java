package ai.kafka.infra.producer;

import ai.kafka.infra.dto.ProducerMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaSendCallback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class Producer {
    private final KafkaTemplate<String, ProducerMessageRequest> kafkaTemplate;

    public void produce(ProducerMessageRequest message) {
        final ProducerRecord<String, ProducerMessageRequest> record = new ProducerRecord<>(message.getTopicName(), message.getResourceKey(), message);
        ListenableFuture<SendResult<String, ProducerMessageRequest>> future = kafkaTemplate.send(record);
        future.addCallback(new KafkaSendCallback<>() {
            @Override
            public void onSuccess(SendResult<String, ProducerMessageRequest> result) {
                log.info("produce success : " + result.toString());
            }

            @Override
            public void onFailure(KafkaProducerException ex) {
                ProducerRecord<String, ProducerMessageRequest> failed = ex.getFailedProducerRecord();
                log.error("produce fail : " + failed);
                log.info("produce record : " + record);
            }
        });
    }
}
