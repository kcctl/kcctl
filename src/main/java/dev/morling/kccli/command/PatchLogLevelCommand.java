package dev.morling.kccli.command;

import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dev.morling.kccli.service.KafkaConnectApi;
import dev.morling.kccli.util.ConfigurationContext;
import picocli.CommandLine;

@CommandLine.Command(name = "logger", description = "Changes the log level of given class/Connector path")
public class PatchLogLevelCommand implements Callable {

    @Inject
    ConfigurationContext context;

    @CommandLine.Parameters(paramLabel = "Logger NAME", description = "Name of the class/connector root path")
    String name;

    @CommandLine.Option(names = { "-l", "--level" }, description = "Name of LOGGER LEVEL to apply", required = true)
    String level;

    @Override
    public Object call() throws Exception {
        KafkaConnectApi kafkaConnectApi = RestClientBuilder.newBuilder()
                .baseUri(context.getCluster())
                .build(KafkaConnectApi.class);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode data = mapper.createObjectNode();
        data.put("level", level);
        List<String> classes = kafkaConnectApi.updateLogLevel(name, mapper.writeValueAsString(data));
        for (String s : classes) {
            System.out.println(s);
        }

        return 1;
    }
}
