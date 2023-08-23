#
#  Copyright 2021 The original authors
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
FROM debezium/connect:latest

USER root

RUN microdnf -y install git java-11-openjdk-devel

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk

RUN cd /tmp && \
    git clone https://github.com/apache/kafka.git -b trunk && \
    cd kafka && \
    ls && \
    grep "version=" gradle.properties | sed 's/version=//g' > $KAFKA_HOME/kafka_version && \
    ./gradlew clean releaseTarGz --rerun-tasks

RUN mv $KAFKA_HOME/config/log4j.properties $KAFKA_HOME/config/log4j.properties.back && \
    rm -rf $KAFKA_HOME/libs/* && \
    tar -xzf /tmp/kafka/core/build/distributions/kafka_*-SNAPSHOT.tgz -C $KAFKA_HOME --strip-components 1 &&\
    rm -f /tmp/kafka/core/build/distributions/kafka_*-SNAPSHOT.tgz &&\
    mv $KAFKA_HOME/config/log4j.properties.back $KAFKA_HOME/config/log4j.properties && \
    chmod -R g+w,o+w $KAFKA_HOME

#
# Allow random UID to use Kafka
#
RUN chmod -R g+w,o+w $KAFKA_HOME

USER kafka