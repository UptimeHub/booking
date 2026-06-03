package uz.uptimehub.booking.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import uz.uptimehub.booking.kafka.dto.KafkaEvent;

import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
public class Producer<T extends KafkaEvent> {

    private KafkaTemplate<String, T> kafkaTemplate;
    private String topic;

    public void send(T event, Runnable successHandler) {
        log.info("Start sending event {} to {}", event, topic);

        kafkaTemplate.executeInTransaction(kafkaOperations -> {
            CompletableFuture<SendResult<String, T>> result = kafkaOperations.send(topic, event);

            result.whenComplete((sendResult, ex) -> {
                if (ex == null) {
                    log.trace("Event {} sent to {} successfully", event, topic);

                    if (successHandler != null) {
                        successHandler.run();
                    }

                } else {
                    log.error("Failed to send event {} to {}", event, topic, ex);
                }
            });

            return null;
        });
    }
}
