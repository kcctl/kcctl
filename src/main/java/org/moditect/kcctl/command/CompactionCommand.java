package org.moditect.kcctl.command;

import org.moditect.kcctl.service.Context;
import org.moditect.kcctl.service.compaction.KafkaDatabaseHistoryCompaction;
import org.moditect.kcctl.util.ConfigurationContext;
import picocli.CommandLine;

import javax.inject.Inject;

@CommandLine.Command(name = "compaction", description = "Starts the history compaction process")
public class CompactionCommand implements Runnable {

    @Inject
    ConfigurationContext context;

    @CommandLine.Option(names = { "-n", "--connector.name" }, description = "The debezium connector name for which compaction should be run")
    String connectorName;

    @CommandLine.Option(names = { "-h", "--history.topic" }, description = "The new database history topic name")
    String compactedHistoryTopic;

    @Override
    public void run() {
        Context currentContext = context.getCurrentContext();
        KafkaDatabaseHistoryCompaction databaseHistoryCompaction = new KafkaDatabaseHistoryCompaction(currentContext, compactedHistoryTopic);
        databaseHistoryCompaction.start();
    }
}