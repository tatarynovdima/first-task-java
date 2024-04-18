package org.example;

import org.example.generator.Generator;
import org.example.handler.BookHandler;
import org.example.model.book.Book;
import org.example.parser.BookStreamingParser;

import java.nio.file.Path;

public class Main {
    private static final Path DEFAULT_SOURCE_FILE_FOLDER = Path.of("src/main/resources");
    private static final String DEFAULT_PROPERTY_NAME = "age";
    private static final String RESULT_FILE_NAME_PREFIX = "statistics_by_";
    private static final String RESULT_FILE_TYPE = ".xml";
    private static final int NUMBER_OF_THREADS = 8;

    private final BookStreamingParser parser;
    private final BookHandler processor;
    private final Generator reporter;

    public Main(int numberOfThreads) {
        parser = new BookStreamingParser(numberOfThreads);
        processor = new BookHandler(Book.class);
        reporter = new Generator();
    }

    public void doWork(Path sourceFileFolder, String propertyName) {
        var propertyStream = parser.streamBookPropertyValues(sourceFileFolder, propertyName);
        var processedData = processor.processPropertyStream(propertyStream);
        reporter.save(getResultFile(sourceFileFolder, propertyName), processedData);
    }

    private Path getResultFile(Path sourceFileFolder, String propertyName) {
        return sourceFileFolder.resolve(RESULT_FILE_NAME_PREFIX + propertyName + RESULT_FILE_TYPE);
    }

    public static void main(String... args) {
        String propertyName = DEFAULT_PROPERTY_NAME;
        if (args.length >= 2) {
            propertyName = args[1];
        }
        Path sourceFileFolder = DEFAULT_SOURCE_FILE_FOLDER;
        if (args.length >= 1) {
            sourceFileFolder = Path.of(args[0]);
        }
        new Main(NUMBER_OF_THREADS).doWork(sourceFileFolder, propertyName);
    }
}