package org.example.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.configuration.ConfigReader;
import org.example.model.book.Book;
import org.example.parser.exception.BookParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class BookParser {

    private static final Logger log = Logger.getLogger(BookParser.class.getName());

    private final ObjectMapper mapper;
    private final int numberOfThreads;
    private final ExecutorService executorService;
    private final Class<Book> bookClass;
    private final CollectionType collectionType;

    public BookParser(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.bookClass = Book.class;
        this.collectionType = mapper.getTypeFactory().constructCollectionType(List.class, bookClass);
    }

    public Stream<Book> loadBooks(Path dataPath) {
        try (var pathStream = Files.newDirectoryStream(dataPath, ConfigReader.getSourceFilePattern())) {
            final BlockingQueue<Path> sourceFileQueue = new ArrayBlockingQueue<>(ConfigReader.getSourceFileQueueInitialCapacity());
            pathStream.forEach(sourceFileQueue::add);
            final Callable<List<Book>> taskCallable = () -> {
                List<Book> books = new ArrayList<>();
                for (Path filePath; (filePath = sourceFileQueue.poll()) != null;) {
                    books.addAll(parseFile(filePath));
                }
                return books;
            };
            return executeTasks(taskCallable);
        } catch (IOException e) {
            log.severe(() -> "Error iterating through directory %s".formatted(dataPath));
            throw new BookParseException("Error iterating through directory %s".formatted(dataPath), e);
        }
    }

    private List<Book> parseFile(Path filePath) {
        try (var reader = Files.newBufferedReader(filePath)) {
            return mapper.readValue(reader, collectionType);
        } catch (IOException e) {
            log.severe(() -> "Error parsing file %s".formatted(filePath.toAbsolutePath()));
            throw new BookParseException("Error parsing file %s".formatted(filePath.toAbsolutePath()), e);
        }
    }

    public Stream<Book> loadBooks(String[] entityLists) {
        final BlockingQueue<String> sourceDocumentQueue = new ArrayBlockingQueue<>(entityLists.length);
        sourceDocumentQueue.addAll(Arrays.asList(entityLists));
        final Callable<List<Book>> taskCallable = () -> {
            List<Book> books = new ArrayList<>();
            for (String document; (document = sourceDocumentQueue.poll()) != null;) {
                books.addAll(parseDocument(document));
            }
            return books;
        };
        return executeTasks(taskCallable);
    }

    private List<Book> parseDocument(String document) {
        try {
            return mapper.readValue(document, collectionType);
        } catch (JsonProcessingException e) {
            log.severe(() -> "Error parsing document %s".formatted(document));
            throw new BookParseException("Error parsing document %s".formatted(document), e);
        }
    }

    private Stream<Book> executeTasks(Callable<List<Book>> taskCallable) {
        try {
            List<Future<List<Book>>> futures = startParseTasks(taskCallable);
            executorService.shutdown();
            executorService.awaitTermination(ConfigReader.getParseTaskTerminationTimeoutSeconds(), TimeUnit.SECONDS);
            return collectTaskResults(futures);
        } catch (InterruptedException e) {
            log.severe("Error while waiting for parsing threads");
            throw new BookParseException("Error while waiting for parsing threads", e);
        }
    }

    private List<Future<List<Book>>> startParseTasks(Callable<List<Book>> taskCallable) {
        List<Future<List<Book>>> futures = new ArrayList<>();
        for (int k = 0; k < numberOfThreads; k++) {
            futures.add(executorService.submit(taskCallable));
        }
        return futures;
    }

    private Stream<Book> collectTaskResults(List<Future<List<Book>>> futures) {
        try {
            Stream<Book> stream = Stream.empty();
            for (var future : futures) {
                stream = Stream.concat(stream, future.get().stream());
            }
            return stream;
        } catch (InterruptedException | ExecutionException e) {
            log.severe("Error while waiting for parsing threads");
            throw new BookParseException("Error while waiting for parsing threads", e);
        }
    }
}

