package com.learnkafka.library_event_producer.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.library_event_producer.Controller.LibraryEventController;
import com.learnkafka.library_event_producer.Domain.LibraryEvent;
import com.learnkafka.library_event_producer.Producer.LibraryEventsProducer;
import com.learnkafka.library_event_producer.intg.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test Slice
@WebMvcTest(LibraryEventController.class)
class LibraryEventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LibraryEventsProducer libraryEventsProducer;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void postLibraryEvent() throws Exception {
        // Arrange : créer un LibraryEvent factice
        LibraryEvent libraryEvent = TestUtil.libraryEventRecord();

        String json = objectMapper.writeValueAsString(libraryEvent);

        // On "mock" le comportement du producteur Kafka
        when(libraryEventsProducer.sendLibraryEvent_approach2(isA(LibraryEvent.class))).thenReturn(null);

        // Act & Assert : on simule un POST et on vérifie la réponse
        mockMvc.perform(post("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())              // Vérifie que le code HTTP = 201
                .andExpect(content().json(json));            // Vérifie que la réponse contient le JSON envoyé
    }

    @Test
    void postLibraryEvent_4xx() throws Exception{
        // Arrange
        LibraryEvent libraryEvent = TestUtil.libraryEventRecordWithInvalidBook();
        String json = objectMapper.writeValueAsString(libraryEvent);

        // On "mock" le comportement du producteur Kafka
        when(libraryEventsProducer.sendLibraryEvent_approach2(isA(LibraryEvent.class))).thenReturn(null);

        // Act et Assert
        String expectedErrorMessage = "book.bookId - must not be null, book.bookName - must not be blank";

        mockMvc.perform(post("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(expectedErrorMessage))
                .andExpect(status().is4xxClientError());


    }

    @Test
    void putLibraryEvent() throws Exception {
        // Arrange
        LibraryEvent libraryEvent = TestUtil.libraryEventRecordUpdated();

        String json = objectMapper.writeValueAsString(libraryEvent);

        // Do
        when(libraryEventsProducer.sendLibraryEvent_approach2(isA(LibraryEvent.class))).thenReturn(null);

        // Act and Assert
        mockMvc.perform(put("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(json));
    }
}