package com.learnkafka.library_event_producer.Domain;

public record LibraryEvent(
        Integer LibraryEvent,
        LibraryEventType libraryEventType,
        Book book
) {
}
