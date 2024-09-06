
---
# TOPIC Message 요청 형식

### Topic
- 네이밍 규칙 아직 미정.
### Key
- NULL 로 요청 (파티션 auto load balancing)
```java
    /**
     * Create a record with no key
     * 
     * @param topic The topic this record should be sent to
     * @param value The record contents
     */
    public ProducerRecord(String topic, V value) {
        this(topic, null, null, null, value, null);
    }
```
- [produce] key-serializer : StringSerializer
- [consumer] key-deserializer : StringDeserializer

### Value
- [produce] value-serializer : JsonSerializer
- [consumer] value-serializer : JsonDeserializer
#####
- KafkaMessageRequest
```java
public class KafkaMessageRequest {
    
    @NotBlank
    private String domainName; // ex."customer"
    @NotBlank
    private String action;  // ex."createCustomer"
    @NotBlank
    private String resource; // Request : JsonToString
    
}
```
```java
// 예시 플로우가 아래와 같습니다
// step1. producer 
@PostMapping("/customer")
 ResponseEntity postCustomer(@RequestBody ErpCustomerRequest request) throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    String requestJsonToString = objectMapper.writeValueAsString(request);

    KafkaMessageRequest msgRequest = KafkaMessageRequest.of("customer", "createCustomer", requestJsonToString);
    this.kafkaProducer.produce(msgRequest);
    return ResponseEntity.ok("Message sent to kafka topic - customer");
}

// step2. consumer(external-api) JSON string to ErpReqeustObject
 WmsCustomerRequest makeWmsCustomerRequest(KafkaMessageRequest kafkaMessageRequest) throws IOException {
    ErpCustomerRequest resource = ErpCustomerRequest.deserializeJSON(kafkaMessageRequest.getResource());
    return WmsCustomerRequest.from(resource);
}

// step3. consumer(external-api) ErpReqeustObject to WmsReqeustObject
WmsCustomerRequest from(ErpCustomerRequest customerRequest) {
    return WmsCustomerRequest.builder()
    .customerAccountNo(customerRequest.getCustomerCode())
    .name(customerRequest.getName())
    .language(customerRequest.getLanguage())
    .status(customerRequest.getStatus())
    .build();
}
```
---

# 로컬에서 Kafka 서버 환경 구축하기

### 1. Docker 설치
Docker Desktop 다운로드 : https://www.docker.com/get-started

### 2. Kafka-Docker 레포 클론

카프카&주키퍼 컨테이너를 띄울 수 있는 docker-compose가 잘 정리된 github 레파지토리가 있어 사용합니다 :)
```
$ git clone https://github.com/wurstmeister/kafka-docker
```

### 3. docker-compose-single-broker.yml 파일 수정
- 로컬에서 1대의 브로커만 띄울 것이므로 docker-compose-single-broker.yml 을 수정
- KAFKA_ADVERTISED_HOST_NAME -> 127.0.0.1 로컬로 변경
```yaml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    build: .
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
      KAFKA_CREATE_TOPICS: "test:1:1"
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
```

### 4. docker-compose 실행
step3 에서 수정한 docker-compose-single-broker.yml 을 사용
```
$ docker-compose -f docker-compose-single-broker.yml up -d
```

### 5. docker 가 정상적으로 실행되었는지 확인
* 로컬 도커 컨테이너 리스트를 보여주는 명령어 실행으로 Kafka와 Zookeeper 컨테이너 모두 잘 떠있는지 확인
* docker Desktop > Containers
```
$ docker ps -a
```
### 6. 로컬에 카프카 설치

- 주의 :  kafka 컨테이너의 실행 이미지 버전과 지금 설치할 kafka 바이너리 파일의 버전이 동일해야 합니다.
- 실행 이미지 버전은 클론 받은 레포의 `Dockerfile` 의 아래 부분을 확인하시면 됩니다
```dockerfile
ARG kafka_version=2.8.1
ARG scala_version=2.13
```
kafka, scala 버전 확인 후 이에 맞게 다음과 같이 설치합니다
```
$ wget http://mirror.navercorp.com/apache/kafka/2.4.1/kafka_2.13-2.8.1.tgz
$ tar xzvf kafka_2.13-2.8.1.tgz
```

### 7. TOPIC 생성
개발 브랜치는 토픽 자동 생성되도록 되어있으므로 이 과정은 생략해도 됨
--topic 뒤에 붙는게 토픽명입니다(ex. customer)
```
$ cd kafka_2.13-2.8.1
$ bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic customer
```

### 8. 생성되어 있는 TOPIC 리스트 확인
```
$ bin/kafka-topics.sh --bootstrap-server=localhost:9092 --list
```

### 9. Producer 실행
```
$ bin/kafka-console-producer.sh --topic customer --broker-list localhost:9092
```

### 10. Consumer 실행
터미널창 하나 새로 띄우고 실행
* --from-beginning : 토픽에 저장된 가장 처음 데이터부터 출력
```
$ bin/kafka-console-consumer.sh --topic customer --bootstrap-server localhost:9092 --from-beginning
```

### 11. TOPIC Message 생성
- 이 프로젝트(external-api) 실행 후 PubController 에 명시된 URL을 호출하여 특정 토픽에 메세지를 생성 (producer 역할)
- PubController는 개발 위해서 임시로 만든 클래스로 나중에 삭제할 것! (~kafka/producer 패키지 포함)
```
ex.
* url : http://localhost:8080/pub/customer
* HTTP 메소드 : POST
* Content-Type : application/json
* RequestBody
{
    "customerCode" : "customerCodeSample",
    "name" : "nameSample",
    "language" : "ko",
    "status": "Active"
}
```
topic 에 메세지가 생성되면 해당 토픽을 구독하고 있는 consumer 가 메세지를 받고
임시 wms-api 호출 (application.yml - wms-api url)

### Reference Documentation

* [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/html/)

