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

/**
 * Test d’intégration du contrôleur LibraryEventController.
 *
 * Ce test vérifie le bon fonctionnement du flux complet :
 * Requête HTTP POST → Contrôleur → Producteur Kafka → Kafka embarqué → Vérification du message consommé.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(properties = {
        // On remplace les serveurs Kafka par le broker embarqué pour isoler le test
        "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"
})
class LibraryEventControllerIT {

    @Autowired
    TestRestTemplate restTemplate; // Permet d’appeler l’API REST comme un vrai client HTTP

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker; // Kafka embarqué en mémoire pour le test

    @Autowired
    ObjectMapper objectMapper; // Pour convertir objets ↔ JSON

    private Consumer<Integer, String> consumer; // Consommateur Kafka utilisé pour vérifier le message produit

    /**
     * Avant chaque test :
     * - Configuration du consommateur Kafka pour écouter le broker embarqué
     * - Abonnement à tous les topics
     */
    @BeforeEach
    void setUp() {
        // Création de la configuration de base du consumer Kafka
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));

        // Lecture à partir des derniers messages (et non depuis le début du topic)
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // Création du consumer Kafka avec désérialisation clé=Integer et valeur=String (JSON)
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer())
                .createConsumer();

        // Le consumer s’abonne à tous les topics du broker embarqué (ici : library-events)
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    /**
     * Après chaque test : fermeture propre du consumer
     */
    @AfterEach
    void tearDown() {
        consumer.close();
    }

    /**
     * Test principal : vérifie que l’API POST /v1/libraryevent
     * - retourne un statut HTTP 201 (Created)
     * - publie bien un message dans Kafka
     * - et que ce message correspond exactement à celui envoyé
     */
    @Test
    void postLibraryEvent() throws JsonProcessingException {
        // --- Préparation des données ---
        LibraryEvent libraryEvent = TestUtil.libraryEventRecord(); // Crée un objet LibraryEvent de test
        System.out.println("libraryEvent : " + objectMapper.writeValueAsString(libraryEvent));

        // Définition des headers HTTP (type JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON.toString());

        // Création de l’entité HTTP avec corps et en-têtes
        var httpEntity = new HttpEntity<>(TestUtil.libraryEventRecord(), headers);

        // --- Exécution de la requête ---
        var responseEntity = restTemplate.exchange(
                "/v1/libraryevent",
                HttpMethod.POST,
                httpEntity,
                LibraryEvent.class
        );

        // --- Vérifications HTTP ---
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode(), "Le code HTTP doit être 201 Created");

        // --- Vérifications côté Kafka ---
        // Récupération des messages publiés dans le topic
        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer);

        // Vérifie qu’un seul message a été produit
        assert consumerRecords.count() == 1;

        // Pour chaque message consommé, on le désérialise et on compare son contenu
        consumerRecords.forEach(record -> {
            var libraryEventActual = TestUtil.parseLibraryEventRecord(objectMapper, record.value());
            assertEquals(libraryEvent, libraryEventActual, "Le message Kafka doit correspondre à l’événement envoyé");
        });
    }

}
