package org.example.generator;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.generator.exception.GeneratorException;
import org.example.handler.HandlerResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

public class Generator {
    private static final Logger log = Logger.getLogger(Generator.class.getName());

    private static final String ROOT_ELEMENT = "statistics";
    private final XmlMapper mapper;

    public Generator() {
        mapper = new XmlMapper();
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SimpleModule module = new SimpleModule();
        module.addSerializer(HandlerResult.class, new HandlerResult.Serializer());
        mapper.registerModule(module);
    }

    /**
     * Method saves result of processing {@code processingResult} as XML file at specified location {@code resultFile}
     * @param resultFile
     * @param processingResult
     * @throws GeneratorException if IOException occurs during output file serialization
     */
    public void save(Path resultFile, HandlerResult processingResult) {
        try (var bufferedWriter = Files.newBufferedWriter(resultFile, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            mapper.writer().withRootName(ROOT_ELEMENT).writeValue(bufferedWriter, processingResult);
        } catch (IOException e) {
            log.severe("Error occurred while saving report");
            throw new GeneratorException("Error occurred while saving report", e);
        }
    }
}
