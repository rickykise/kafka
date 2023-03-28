package ai.kafka.infra.configuration;

import ai.kafka.infra.dto.ProducerMessageRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;

@Slf4j
@Configuration
public class ValidatorConfig {

    /**
     * kafka validation Error 발생시 처리 - 지금은 failure 토픽에 전송됨
     */
    @Bean
    KafkaListenerErrorHandler validationErrorHandler() {
        return (m, e) -> {
            /**
             * error 로그 기록
             */
            log.error("[KafkaErrorHandler] kafkaMessage=[" + m.getPayload() + "], errorMessage=[" + e.getMessage() + "]");

            ProducerMessageRequest kafkaMessageRequest = (ProducerMessageRequest) m.getPayload();
            return kafkaMessageRequest;
        };
    }

}
