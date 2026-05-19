package com.dropp.order.service;

import com.dropp.order.entity.Sequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;

import org.springframework.data.mongodb.core.query.Query;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SequenceGeneratorTest {

    @Mock
    private MongoOperations mongoOperations;

    @InjectMocks
    private SequenceGenerator sequenceGenerator;

    @Test
    void generateNextOrderId_whenSequenceExists_shouldReturnCurrentValue() {
        // Arrange
        Sequence sequence = new Sequence();
        sequence.setCurrentValue(42L);

        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Sequence.class))
        ).thenReturn(sequence);

        // Act
        Long result = sequenceGenerator.generateNextOrderId();

        // Assert
        assertEquals(42L, result);
        verify(mongoOperations, times(1))
                .findAndModify(any(), any(), any(), eq(Sequence.class));
    }

    @Test
    void generateNextOrderId_whenSequenceIsNull_shouldReturnFallbackOne() {
        // Arrange
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(Sequence.class))
        ).thenReturn(null);

        // Act
        Long result = sequenceGenerator.generateNextOrderId();

        // Assert
        assertEquals(1L, result);
    }
}