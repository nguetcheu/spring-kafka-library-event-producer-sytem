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
import org.springframework.test.web.servlet.RequestBuilder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test Slice
@WebMvcTest(LibraryEventController.class)
class LibraryEventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventsProducer libraryEventProducer;


    @Test
    // Arrange test for post white kafkaproducer
    void postLibraryEvent() throws Exception {

        // Act
        LibraryEvent libraryEvent = TestUtil.libraryEventRecord();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent_approach2(isA(LibraryEvent.class))).thenReturn(null);

        RequestBuilder request = post("/v1/libraryevent")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON);

        // Expect
        mockMvc.perform(request)
                .andExpect(status().isCreated());
    }

    @Test
    void postLibraryEvent_4xx() throws Exception {
        //given

        LibraryEvent libraryEvent = TestUtil.libraryEventRecordWithInvalidBook();

        String json = objectMapper.writeValueAsString(libraryEvent);
        when(libraryEventProducer.sendLibraryEvent_approach2(isA(LibraryEvent.class))).thenReturn(null);
        //expect
        String expectedErrorMessage = "book.bookId - must not be null, book.bookName - must not be blank";
        mockMvc.perform(post("/v1/libraryevent")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(expectedErrorMessage));

    }
}