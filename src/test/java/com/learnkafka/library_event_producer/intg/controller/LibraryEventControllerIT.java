package com.learnkafka.library_event_producer.intg.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.library_event_producer.Domain.LibraryEvent;
import com.learnkafka.library_event_producer.intg.util.TestUtil;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
class LibraryEventControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    // Configure the embeddedKafkaBroker,
    // override the kafka producer bootstrap address to the embedded broker ones
    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    ObjectMapper objectMapper;

    private Consumer<Integer, String> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    // Configure a kafka consumer in the test case
    // Wire KafkaConsumer and EmbeddedKafkaBroker
    // Consume the record from the EmbeddedKafkaBroker and the assert on it

    @Test
    void postLibraryEvent() throws JsonProcessingException {
        // given
        LibraryEvent libraryEvent = TestUtil.libraryEventRecord();
        System.out.println("libraryEvent : " + objectMapper.writeValueAsString(libraryEvent));
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());
        var httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), headers);

        // when
        var responseEntity = restTemplate
                .exchange("/v1/libraryevent", HttpMethod.POST, httpEntity,LibraryEvent.class);

        // then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);
        //Thread.sleep(3000);
        assert consumerRecords.count() == 1;
        consumerRecords.forEach(record -> {
            var libraryEventActual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
            assertEquals(libraryEvent, libraryEventActual);
        });
    }

}