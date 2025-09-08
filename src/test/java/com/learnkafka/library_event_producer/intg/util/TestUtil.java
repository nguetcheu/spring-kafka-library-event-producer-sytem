package com.learnkafka.library_event_producer.intg.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.library_event_producer.Domain.Book;
import com.learnkafka.library_event_producer.Domain.LibraryEvent;
import com.learnkafka.library_event_producer.Domain.LibraryEventType;

public class TestUtil {

    public static Book bookRecord(){

        return new Book(123, "Dilip", "Kafka Using Spring Boot");
    }

    public static Book bookRecordWithInvalidValues(){

        return new Book(null, " ", "Kafka Using Spring Boot");
    }

    public static LibraryEvent libraryEventRecord(){
        return new LibraryEvent(
                null,
                LibraryEventType.NEW,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventRecordWithLibraryEventId(){
        return new LibraryEvent(
                123,
                LibraryEventType.NEW,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventRecordUpdated(){
        return new LibraryEvent(
                123,
                LibraryEventType.UPDATE,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventRecordWithInvalidBook(){
        return new LibraryEvent(
                null,
                LibraryEventType.NEW,
                bookRecordWithInvalidValues()
        );
    }

    public static LibraryEvent parseLibraryEventRecord(ObjectMapper objectMapper, String json){

        try {
            return objectMapper.readValue(json, LibraryEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }
}
