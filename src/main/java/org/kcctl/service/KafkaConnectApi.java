/*
 *  Copyright 2021 The original authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kcctl.service;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("/")
@RegisterRestClient
@RegisterClientHeaders(value = KafkaConnectClientHeadersFactory.class)
@RegisterProvider(value = KafkaConnectResponseExceptionMapper.class, priority = 50)
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

    @POST
    @Path("/connectors/{name}/tasks/{id}/restart")
    ConnectorInfo restartTask(@PathParam("name") String name, @PathParam("id") String id);

    @GET
    @Path("/connectors/{name}/tasks-config")
    Map<String, Map<String, String>> getConnectorTasksConfig(@PathParam("name") String name);

    @PUT
    @Path("/admin/loggers/{classPath}")
    List<String> updateLogLevel(@PathParam("classPath") String classPath, String content);

    @GET
    @Path("/admin/loggers/{path}")
    ObjectNode getLoggers(@PathParam("path") String path);
}
