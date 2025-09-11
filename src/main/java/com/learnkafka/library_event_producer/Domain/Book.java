package com.learnkafka.library_event_producer.Domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Book(@NotNull Integer bookId, @NotNull String bookName,@NotBlank String bookAuthor) {
}
