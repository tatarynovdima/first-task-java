package org.example.handler;

import java.util.Objects;

public record Counter(Object value, Long count) {

    public int compareByValue(Counter a) {
        return value.toString().compareTo(a.value().toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Counter item) {
            return Objects.equals(value, item.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "%s: %s".formatted(value, count);
    }
}
