package com.learnkafka.library_event_producer.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.learnkafka.library_event_producer.Domain.LibraryEvent;
import com.learnkafka.library_event_producer.Producer.LibraryEventsProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@Slf4j
public class LibraryEventController {

    private final LibraryEventsProducer libraryEventsProducer;

    public LibraryEventController(LibraryEventsProducer libraryEventsProducer) {
        this.libraryEventsProducer = libraryEventsProducer;
    }

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        log.info("libraryEvent: {}", libraryEvent);

        // invoke the kafka producer

        libraryEventsProducer.sendLibraryEvent_approach3(libraryEvent);

        // libraryEventsProducer.sendLibraryEvent_approach2(libraryEvent);

        // libraryEventsProducer.sendLibraryEvent(libraryEvent);

        log.info("After Sending libraryEvent : ");
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }
}
