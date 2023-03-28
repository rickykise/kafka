package ai.kafka.infra.configuration;

import ai.kafka.infra.dto.ProducerMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConsumerConfig implements KafkaListenerConfigurer {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeSerializer;
    @Value("${spring.kafka.consumer.yi21.ack-mode}")
    private ContainerProperties.AckMode ackMode;
    @Value("${spring.kafka.consumer.yi21.auto-offset-reset}")
    private String autoOffsetReset;
    @Value("${spring.kafka.consumer.yi21.poll-time-out}")
    private int pollTimeOut;
    @Value("${spring.kafka.consumer.yi21.concurrency}")
    private int concurrency;

    private final LocalValidatorFactoryBean validator;

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ProducerMessageRequest>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProducerMessageRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ackMode);
        factory.setConcurrency(concurrency);
        factory.getContainerProperties().setPollTimeout(pollTimeOut);
        factory.setReplyTemplate(replyTemplate());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ProducerMessageRequest> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                consumerConfigs(),
                new StringDeserializer(),
                new JsonDeserializer<>(ProducerMessageRequest.class, false));
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        return Map.of(
            org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
            org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
            org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
            ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, keyDeSerializer,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
            org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset,
            JsonDeserializer.TRUSTED_PACKAGES, "*",
            JsonSerializer.TYPE_MAPPINGS, "producerMessageRequest:ai.fassto.wfg.infra.dto.ProducerMessageRequest"
        );
    }

    /**
     * payload validator 설정
     */
    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(this.validator);
    }

    /**
     * retryTemplate 설정
     */
    @Bean
    public KafkaTemplate<String, ProducerMessageRequest> replyTemplate() {
        return new KafkaTemplate<>(replyProducerFactory());
    }

    @Bean
    public ProducerFactory<String, ProducerMessageRequest> replyProducerFactory() {
        return new DefaultKafkaProducerFactory<>(replyProducerConfigs());
    }

    @Bean
    public Map<String, Object> replyProducerConfigs() {
        return Map.of(
                org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, keyDeSerializer,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset,
                JsonDeserializer.TRUSTED_PACKAGES, "*",
                JsonSerializer.TYPE_MAPPINGS, "producerMessageRequest:ai.fassto.wfg.infra.dto.ProducerMessageRequest"
        );
    }
}
