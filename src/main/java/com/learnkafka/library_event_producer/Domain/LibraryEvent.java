package com.learnkafka.library_event_producer.Domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LibraryEvent(
        Integer LibraryEventId,
        LibraryEventType libraryEventType,
        @NotNull
        @Valid
        Book book
) {
}
