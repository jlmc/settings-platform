package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.CustomConversions;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import tools.jackson.databind.ObjectMapper;

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
        //new JsonContentReaderConverter(objectMapper),
        //new JsonContentWriterConverter()
        List<Converter> converters = List.of();

        return new MongoCustomConversions(converters);
    }
}


/*
    @Bean
    fun mongoCustomConversions(objectMapper: ObjectMapper): MongoCustomConversions {
        logger.info("Mongo Custom Conversions initializing")
        val converters =
            mutableListOf(
                JsonContentReaderConverter(objectMapper),
                JsonContentWriterConverter(),
            )

        return MongoCustomConversions(converters)
    }

    @Bean
    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
        logger.info("Enabling MongoDB Transaction Manager")
        return MongoTransactionManager(dbFactory)
    }
 */