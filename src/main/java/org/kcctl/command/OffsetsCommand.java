package org.kcctl.command;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.kcctl.service.PartitionOffsetService;
import org.kcctl.util.ConfigurationContext;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Command(name = "offsets", description = "Display's the connectorName's task offsets.")
public class OffsetsCommand implements Runnable {
    private final ConfigurationContext configurationContext;

    public OffsetsCommand(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    @Override
    public void run() {
        var consumer = createConsumer();
        var offsets =
            new PartitionOffsetService(consumer)
                .findAllPartitionOffsets(configurationContext.getCurrentContext().getOffsetTopic());

        AsciiTable.getTable(offsets, List.of(
            new Column().header("partition").with(offset -> Integer.toString(offset.partition())),
            new Column().header("offset").with(offset -> Long.toString(offset.offset()))
        ));
    }

    private KafkaConsumer<String, byte[]> createConsumer() {
        var consumerConfig = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                configurationContext.getCurrentContext().getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG,
                "kcctl-" + new Random().nextInt(100_000),
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        return new KafkaConsumer<>(consumerConfig, new StringDeserializer(), new ByteArrayDeserializer());
    }
}
