/*
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Copyright The original authors
 *
 *  Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.kcctl.service;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
@RegisterClientHeaders(value = KafkaConnectClientHeadersFactory.class)
@RegisterProvider(value = KafkaConnectResponseExceptionMapper.class, priority = 50)
@Retry(delay = 100L, maxDuration = 30_000L, retryOn = KafkaConnectConflictException.class)
public interface KafkaConnectApi {

    @GET
    KafkaConnectInfo getWorkerInfo();

    @GET
    @Path("/connector-plugins")
    List<ConnectorPlugin> getConnectorPlugins(@QueryParam("connectorsOnly") Boolean connectorsOnly);

    @GET
    @Path("/connector-plugins/{name}/config")
    List<ConfigInfos.ConfigKeyInfo> getConnectorPluginConfig(@PathParam("name") String name);

    @PUT
    @Path("/connector-plugins/{name}/config/validate")
    ConfigInfos validateConfig(@PathParam("name") String name, String config);

    @GET
    @Path("/connectors/")
    List<String> getConnectors();

    @POST
    @Path("/connectors/")
    ConnectorStatusInfo createConnector(String config);

    @GET
    @Path("/connectors/{name}")
    ConnectorInfo getConnector(@PathParam("name") String name);

    @POST
    @Path("/connectors/{name}/restart")
    void restartConnector(@PathParam("name") String name);

    @POST
    @Path("/connectors/{name}/restart")
    ConnectorStatusInfo restartConnectorAndTasks(
                                                 @PathParam("name") String name,
                                                 @QueryParam("includeTasks") boolean includeTasks,
                                                 @QueryParam("onlyFailed") boolean onlyFailed);

    @PUT
    @Path("/connectors/{name}/pause")
    void pauseConnector(@PathParam("name") String name);

    @PUT
    @Path("/connectors/{name}/resume")
    void resumeConnector(@PathParam("name") String name);

    @PUT
    @Path("/connectors/{name}/stop")
    void stopConnector(@PathParam("name") String name);

    @DELETE
    @Path("/connectors/{name}")
    void deleteConnector(@PathParam("name") String name);

    @GET
    @Path("/connectors")
    Map<String, ConnectorExpandInfo> getConnectorExpandInfo(@QueryParam("expand") List<String> expands);

    @GET
    @Path("/connectors/{name}/status")
    ConnectorStatusInfo getConnectorStatus(@PathParam("name") String name);

    @GET
    @Path("/connectors/{name}/topics")
    Map<String, TopicsInfo> getConnectorTopics(@PathParam("name") String name);

    @GET
    @Path("/connectors/{name}/config")
    Map<String, String> getConnectorConfig(@PathParam("name") String name);

    @PUT
    @Path("/connectors/{name}/config")
    ConnectorStatusInfo updateConnector(@PathParam("name") String name, String config);

    @GET
    @Path("/connectors/{name}/offsets")
    ConnectorOffsets getConnectorOffsets(@PathParam("name") String name);

    @DELETE
    @Path("/connectors/{name}/offsets")
    AlterResetOffsetsResponse deleteConnectorOffsets(@PathParam("name") String name);

    @PATCH
    @Path("/connectors/{name}/offsets")
    AlterResetOffsetsResponse patchConnectorOffsets(@PathParam("name") String name, ConnectorOffsets offsets);

    @POST
    @Path("/connectors/{name}/tasks/{id}/restart")
    ConnectorInfo restartTask(@PathParam("name") String name, @PathParam("id") String id);

    @GET
    @Path("/connectors/{name}/tasks")
    List<TaskConfig> getConnectorTasks(@PathParam("name") String name);

    @PUT
    @Path("/admin/loggers/{classPath}")
    List<String> updateLogLevel(@PathParam("classPath") String classPath, String content);

    @PUT
    @Path("/admin/loggers/{classPath}")
    String updateLogLevelWithScope(@PathParam("classPath") String classPath, @QueryParam("scope") String scope, String content);

    @GET
    @Path("/admin/loggers/{path}")
    LoggerLevel getLogger(@PathParam("path") String path);

    @GET
    @Path("/admin/loggers")
    Map<String, LoggerLevel> getLoggers();
}
