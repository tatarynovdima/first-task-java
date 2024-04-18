package org.example.handler;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.*;

public final class HandlerResult implements Iterable<Counter> {

    private static final Comparator<Counter> COMPARATOR_BY_COUNT_DESC = Comparator.comparing(Counter::count).reversed()
            .thenComparing(Counter::compareByValue);

    private final SortedSet<Counter> items;

    public HandlerResult(Map<Object, Long> map) {
        items = new TreeSet<>(COMPARATOR_BY_COUNT_DESC);
        for (var entry : map.entrySet()) {
            items.add(new Counter(entry.getKey(), entry.getValue()));
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    @Override
    public Iterator<Counter> iterator() {
        return items.iterator();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("[");
        var i = iterator();
        if (i.hasNext()) {
            b.append(i.next());
            while (i.hasNext()) {
                b.append(",").append(i.next());
            }
        }
        b.append("]");
        return b.toString();
    }

    /**
     * Serializer class for custom Jackson serialization to XML file
     */
    public static class Serializer extends StdSerializer<HandlerResult> {

        public Serializer() {
            this(null);
        }

        protected Serializer(Class<HandlerResult> t) {
            super(t);
        }

        @Override
        public void serialize(HandlerResult value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeStartObject();
            gen.writeRaw("\n");
            for (var i : value.items) {
                gen.writeRaw("\t");
                gen.writeFieldName("item");
                gen.writeStartObject();
                gen.writeObjectField("value", i.value());
                gen.writeObjectField("count", i.count());
                gen.writeEndObject();
                gen.writeRaw("\n");
            }
            gen.writeEndObject();
        }

    }

}
