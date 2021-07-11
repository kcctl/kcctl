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
package dev.morling.kccli.service;

import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/")
@RegisterRestClient
public interface KafkaConnectApi {

    @GET
    KafkaConnectInfo getWorkerInfo();

    @GET
    @Path("/connector-plugins")
    List<ConnectorPlugin> getConnectorPlugins();

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

    @DELETE
    @Path("/connectors/{name}")
    void deleteConnector(@PathParam("name") String name);

    @GET
    @Path("/connectors/{name}/status")
    ConnectorStatusInfo getConnectorStatus(@PathParam("name") String name);

    @GET
    @Path("/connectors/{name}/config")
    Map<String, String> getConnectorConfig(@PathParam("name") String name);

    @PUT
    @Path("/connectors/{name}/config")
    ConnectorStatusInfo updateConnector(@PathParam("name") String name, String config);

    @POST
    @Path("/connectors/{name}/tasks/{id}/restart")
    ConnectorInfo restartTask(@PathParam("name") String name, @PathParam("id") String id);
}
