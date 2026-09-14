package dev.aether.util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RetryingClickSequenceTest {
    @Test
    void retriesDroppedClicksUntilTheStageTransitionIsObserved() {
        AtomicLong now = new AtomicLong();
        AtomicInteger attempts = new AtomicInteger();
        var stage = new RetryingClickSequence.Stage("test stage",
                () -> attempts.get() == 3,
                () -> {
                    attempts.incrementAndGet();
                    return true;
                }, 1_000L);

        boolean completed = RetryingClickSequence.run(List.of(stage), 100L, 200L,
                () -> false, now::get, now::addAndGet, message -> { });

        assertTrue(completed);
        assertEquals(3, attempts.get());
        assertEquals(500L, now.get());
    }

    @Test
    void timesOutWhenNoClickCanBeDispatched() {
        AtomicLong now = new AtomicLong();
        var stage = new RetryingClickSequence.Stage("missing stage", () -> false, () -> false, 200L);

        boolean completed = RetryingClickSequence.run(List.of(stage), 0L, 100L,
                () -> false, now::get, now::addAndGet, message -> { });

        assertFalse(completed);
        assertEquals(200L, now.get());
    }
}
