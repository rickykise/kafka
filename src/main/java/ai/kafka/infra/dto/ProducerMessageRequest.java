package ai.kafka.infra.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProducerMessageRequest {

    @NotBlank
    private String messageId;
    @NotBlank
    private String topicName;
    @NotBlank
    private String domainName;
    @NotBlank
    private String action;
    @NotBlank
    private String messageTime;
    @NotBlank
    private String resourceKey;
    @NotBlank
    private String resource;
}
