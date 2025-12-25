package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.List;

@Log4j2
@Configuration
public class MongoConfiguration {


    @Bean
    MongoTransactionManager mongoTransactionManager(MongoDatabaseFactory dbFactory) {
        log.info("Enabling MongoDB Transaction Manager");
        return new MongoTransactionManager(dbFactory);
    }

    @Bean
    MongoCustomConversions mongoCustomConversions(ObjectMapper objectMapper) {
        log.info("Mongo Custom Conversions initializing");
        List<Converter<? extends Serializable, ? extends Serializable>> converters = List.of(
                new ObjectNodeWriterConverter(),
                new ObjectNodeReaderConverter(objectMapper)
        );

        return new MongoCustomConversions(converters);
    }

    @WritingConverter
    static class ObjectNodeWriterConverter implements Converter<ObjectNode, String> {
        @Override
        public String convert(ObjectNode source) {
            return source == null ? null : source.toPrettyString();
        }
    }

    @ReadingConverter
    static class ObjectNodeReaderConverter implements Converter<String, ObjectNode> {
        private final ObjectMapper objectMapper;

        ObjectNodeReaderConverter(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        @Override
        public ObjectNode convert(String source) {
            try {
                return source == null ? null : (ObjectNode) objectMapper.readTree(source);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to convert String to ObjectNode", e);
            }
        }
    }

}
