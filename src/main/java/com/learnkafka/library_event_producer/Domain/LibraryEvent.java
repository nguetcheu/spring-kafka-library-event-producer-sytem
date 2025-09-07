package com.learnkafka.library_event_producer.Domain;

public record LibraryEvent(
        Integer LibraryEventId,
        LibraryEventType libraryEventType,
        Book book
) {
}
