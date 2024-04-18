package org.example.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonToken;
import org.example.configuration.ConfigReader;
import org.example.parser.exception.BookParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BookStreamingParser {
    private static final Logger log = Logger.getLogger(BookStreamingParser.class.getName());

    private final int numberOfThreads;
    private final ExecutorService executorService;
    private final BlockingQueue<Path> sourceFileQueue;
    private final BlockingQueue<String> resultQueue;
    private final JsonFactory jsonFactory;
    private final AtomicInteger finishedThreadCount;


    public BookStreamingParser(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        this.finishedThreadCount = new AtomicInteger(0);
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.sourceFileQueue = new ArrayBlockingQueue<>(ConfigReader.getSourceFileQueueInitialCapacity());
        this.resultQueue = new ArrayBlockingQueue<>(ConfigReader.getResultQueueInitialCapacity());
        this.jsonFactory = new JsonFactory();
    }

    public Stream<String> streamBookPropertyValues(Path dataPath, String propertyName) {
        StreamingIterable iterable = new StreamingIterable(dataPath, propertyName);
        return StreamSupport.stream(iterable.spliterator(), false).filter(Objects::nonNull);
    }

    public boolean isDone() {
        return finishedThreadCount.intValue() == numberOfThreads && resultQueue.isEmpty();
    }

    public void startParsing(Path dataPath, String propertyName) {
        sourceFileQueue.clear();
        resultQueue.clear();
        try (var pathStream = Files.newDirectoryStream(dataPath, ConfigReader.getSourceFilePattern())) {
            pathStream.forEach(sourceFileQueue::add);
            startParseTasks(propertyName);
            executorService.shutdown();
        } catch (IOException e) {
            log.severe(() -> "Error iterating through directory %s".formatted(dataPath));
            throw new BookParseException("Error iterating through directory %s".formatted(dataPath), e);
        }
    }

    private void startParseTasks(String propertyName) {
        for (int k = 0; k < numberOfThreads; k++) {
            executorService.submit(new ParseTask(propertyName));
        }
    }

    private class ParseTask implements Runnable {

        private final String propertyName;

        private ParseTask(String propertyName) {
            this.propertyName = propertyName;
        }

        @Override
        public void run() {
            for (Path filePath = null; (filePath = sourceFileQueue.poll()) != null;) {
                parseFile(filePath, propertyName);
            }
            finishedThreadCount.incrementAndGet();
        }

        private void parseFile(Path filePath, String propertyName) {
            try (var reader = Files.newBufferedReader(filePath); var jsonParser = jsonFactory.createParser(reader)) {
                if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                    throw new BookParseException("Document should represent array of entities and start with [");
                }
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    if (jsonParser.currentToken() == JsonToken.START_OBJECT) {
                        String propertyValue = null;
                        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                            String fieldName = jsonParser.currentName();
                            if (propertyName.equals(fieldName)) {
                                jsonParser.nextToken(); // Move to the value of the property
                                propertyValue = jsonParser.getText();
                            }
                        }
                        if (propertyValue != null) {
                            addValue(propertyValue);
                        }
                    }
                }
            } catch (IOException e) {
                log.severe(() -> "Error parsing file %s".formatted(filePath.toAbsolutePath()));
                throw new BookParseException("Error parsing file %s".formatted(filePath.toAbsolutePath()), e);
            }
        }


        private void addValue(String value) {
            try {
                resultQueue.put(value);
            } catch (InterruptedException e) {
                log.severe("Interrupted while waiting for free space in result queue");
                throw new BookParseException("Interrupted while waiting for free space in result queue");
            }
        }
    }

    private class StreamingIterable implements Iterable<String> {

        private StreamingIterable(Path path, String propertyName) {
            startParsing(path, propertyName);
        }

        @Override
        public Iterator<String> iterator() {
            return new StreamingIterator();
        }

        private class StreamingIterator implements Iterator<String> {
            @Override
            public boolean hasNext() {
                return !isDone();
            }

            @Override
            public String next() {
                if (isDone()) {
                    throw new NoSuchElementException("No more elements left");
                }
                String value = null;
                while (!isDone() && (value = resultQueue.poll()) == null) {
                }
                return value;
            }
        }
    }
}
