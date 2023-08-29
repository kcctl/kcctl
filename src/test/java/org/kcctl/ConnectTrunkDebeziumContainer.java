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
package org.kcctl;

import io.debezium.testing.testcontainers.DebeziumContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class ConnectTrunkDebeziumContainer extends DebeziumContainer {

    public static final String DEBEZIUM_CONNECT_TRUNK_IMAGE_NAME = "debezium/connect:trunk";
    public static final String DOCKERFILE_PATH = "docker/Dockerfile";
    public static final String DOCKERFILE_NAME = "Dockerfile";

    public ConnectTrunkDebeziumContainer() {

        super(new ImageFromDockerfile(DEBEZIUM_CONNECT_TRUNK_IMAGE_NAME, false)
                .withFileFromClasspath(DOCKERFILE_NAME, DOCKERFILE_PATH));
    }
}
