package org.kcctl.service;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.kcctl.util.ConnectorNameExtractor;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.function.BinaryOperator.maxBy;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Stream.generate;
import static java.util.stream.StreamSupport.stream;

public class PartitionOffsetService {
    private final Consumer<String, byte[]> consumer;
    private final ConnectorNameExtractor connectorNameExtractor;

    public PartitionOffsetService(Consumer<String, byte[]> consumer) {
        this.consumer = consumer;
        this.connectorNameExtractor = new ConnectorNameExtractor();
    }

    public Collection<Offset> findAllPartitionOffsets(String offsetTopic) {
        consumer.subscribe(List.of(offsetTopic));

        return generate(() -> consumer.poll(Duration.ofSeconds(5)))
            .takeWhile(not(ConsumerRecords::isEmpty))
            .flatMap(records ->
                stream(records.spliterator(), false)
                    .filter(record -> connectorNameExtractor.extract(record.key()).equals(offsetTopic))
            )
            .map(record -> new Offset(record.partition(), record.offset(), record.key()))
            .collect(toUnmodifiableMap(Offset::key, identity(), maxBy(comparing(Offset::offset))))
            .values();
    }
}
