package org.example.handler;

import org.example.handler.exception.CounterException;
import org.example.model.book.Book;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BookHandler {

    private static final Logger log = Logger.getLogger(BookHandler.class.getName());

    private static final String KEY_SEPARATOR = ",";

    private final Class<Book> bookClass;

    public BookHandler(Class<Book> bookClass) {
        this.bookClass = bookClass;
    }

    public HandlerResult processBookStream(Stream<Book> bookStream, String propertyName) {
        var map = bookStream.map(book -> getKeyValue(book, propertyName))
                .flatMap(this::splitProperty)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return new HandlerResult(map);
    }

    public HandlerResult processPropertyStream(Stream<String> propertyStream) {
        var map = propertyStream.flatMap(this::splitProperty)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return new HandlerResult(map);
    }

    private Object getKeyValue(Book book, String propertyName) {
        final String getterName = getGetterName(propertyName);
        try {
            Method getter = bookClass.getDeclaredMethod(getterName);
            return getter.invoke(book);
        } catch (ReflectiveOperationException | SecurityException e) {
            log.severe(() -> "No accessible getter method %s found for entity class %s or it's execution failed"
                    .formatted(getterName, bookClass.getSimpleName()));
            throw new CounterException(
                    "No accessible getter method %s found for entity class %s or it's execution failed"
                            .formatted(getterName, bookClass.getSimpleName()),
                    e);
        }
    }

    private Stream<Object> splitProperty(Object property) {
        if (property instanceof String string) {
            return Arrays.stream(string.split(KEY_SEPARATOR));
        }
        return Stream.of(property);
    }

    private String getGetterName(String propertyName) {
        return "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }
}
