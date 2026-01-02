package io.github.jlmc.settings.service.adapters.mongo.configurations;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingEntityCallback;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.Serializable;
import java.util.List;

@Log4j2
@Configuration
public class MongoConfiguration {

    /// Creates and registers a [ValidatingEntityCallback] bean to trigger
    /// Bean Validation on MongoDB entities.
    ///
    /// This implementation intentionally uses [ValidatingEntityCallback]
    /// instead of `org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener` because
    /// `ValidatingMongoEventListener` has been deprecated in recent
    /// Spring Data MongoDB versions.
    ///
    ///
    /// [ValidatingEntityCallback] is the recommended replacement and
    /// integrates with Spring Data's entity callback infrastructure, ensuring
    /// that [jakarta.validation] (or [javax.validation]) constraints
    /// are applied during entity lifecycle events such as save and update.
    ///
    ///
    /// @param factory the [LocalValidatorFactoryBean] used to perform
    ///                Bean Validation
    /// @return a [ValidatingEntityCallback] configured with the provided
    ///         validator factory
    @Bean
    public ValidatingEntityCallback validatingEntityCallback(
            LocalValidatorFactoryBean factory
    ) {
        return new ValidatingEntityCallback(factory);
    }


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
