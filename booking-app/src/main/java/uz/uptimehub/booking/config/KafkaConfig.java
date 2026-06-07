package uz.uptimehub.booking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import uz.uptimehub.booking.kafka.dto.booking.BookingConfirmedEvent;
import uz.uptimehub.booking.kafka.dto.booking.BookingCreatedEvent;
import uz.uptimehub.booking.kafka.dto.booking.BookingFailedEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.producer.key-serializer}")
    private String keySerializer;
    @Value("${spring.kafka.producer.value-serializer}")
    private String valueSerializer;
    @Value("${spring.kafka.producer.properties.transaction.timeout.ms}")
    private Integer transactionTimeoutMs = 0;
    @Value("${spring.kafka.producer.properties.spring.json.trusted.packages}")
    private String trustedPackages;

    @Bean
    public NewTopic bookingCreateTopic(@Value("${app.kafka.topics.consume.booking-create}") String topic) {
        return TopicBuilder.name(topic).partitions(4).build();
    }

    @Bean
    public NewTopic bookingFailedTopic(@Value("${app.kafka.topics.consume.booking-failed}") String topic) {
        return TopicBuilder.name(topic).partitions(4).build();
    }

    @Bean
    public NewTopic resourceIndexTopic(@Value("${app.kafka.topics.consume.resource-index}") String topic) {
        return TopicBuilder.name(topic).partitions(4).build();
    }

    @Bean("bookingCreatedEventProducerFactory")
    public ProducerFactory<String, BookingCreatedEvent> bookingCreatedEventProducerFactory(
            Map<String, Object> commonProducerProperties,
            @Value("${app.kafka.producers.booking-create.transaction-id-prefix}") String transactionIdPrefix
    ) {
        var props = new HashMap<>(commonProducerProperties);
        String transactionId = generateTransactionId(transactionIdPrefix);
        DefaultKafkaProducerFactory<String, BookingCreatedEvent> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setTransactionIdPrefix(transactionId);

        return factory;
    }

    @Bean("bookingCreatedEventKafkaTemplate")
    public KafkaTemplate<String, BookingCreatedEvent> bookingCreatedEventKafkaTemplate(
            @Qualifier("bookingCreatedEventProducerFactory")
            ProducerFactory<String, BookingCreatedEvent> bookingCreatedEventProducerFactory
    ) {
        return new KafkaTemplate<>(bookingCreatedEventProducerFactory);
    }

    @Bean("bookingFailedEventProducerFactory")
    public ProducerFactory<String, BookingFailedEvent> bookingFailedEventProducerFactory(
            Map<String, Object> commonProducerProperties,
            @Value("${app.kafka.producers.booking-failed.transaction-id-prefix}") String transactionIdPrefix
    ) {
        var props = new HashMap<>(commonProducerProperties);
        String transactionId = generateTransactionId(transactionIdPrefix);
        DefaultKafkaProducerFactory<String, BookingFailedEvent> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setTransactionIdPrefix(transactionId);

        return factory;
    }

    @Bean("bookingFailedEventKafkaTemplate")
    public KafkaTemplate<String, BookingFailedEvent> bookingFailedEventKafkaTemplate(
            @Qualifier("bookingFailedEventProducerFactory")
            ProducerFactory<String, BookingFailedEvent> bookingFailedEventProducerFactory
    ) {
        return new KafkaTemplate<>(bookingFailedEventProducerFactory);
    }

    @Bean("bookingConfirmedEventProducerFactory")
    public ProducerFactory<String, BookingConfirmedEvent> bookingConfirmedEventProducerFactory(
            Map<String, Object> commonProducerProperties,
            @Value("${app.kafka.producers.booking-confirmed.transaction-id-prefix}") String transactionIdPrefix
    ) {
        var props = new HashMap<>(commonProducerProperties);
        String transactionId = generateTransactionId(transactionIdPrefix);
        DefaultKafkaProducerFactory<String, BookingConfirmedEvent> factory = new DefaultKafkaProducerFactory<>(props);
        factory.setTransactionIdPrefix(transactionId);

        return factory;
    }

    @Bean("bookingConfirmedEventKafkaTemplate")
    public KafkaTemplate<String, BookingConfirmedEvent> bookingConfirmedEventKafkaTemplate(
            @Qualifier("bookingConfirmedEventProducerFactory")
            ProducerFactory<String, BookingConfirmedEvent> bookingConfirmedEventProducerFactory
    ) {
        return new KafkaTemplate<>(bookingConfirmedEventProducerFactory);
    }

    @Bean
    public Map<String, Object> commonProducerProperties() {
        HashMap<String, Object> props = new HashMap<>();

        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, transactionTimeoutMs);
        props.put("spring.json.trusted.packages", trustedPackages);
        return props;
    }

    private String generateTransactionId(String transactionIdPrefix) {
        return transactionIdPrefix + UUID.randomUUID();
    }
}
