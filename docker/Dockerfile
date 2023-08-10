FROM debezium/connect:latest

USER root

RUN microdnf -y install git java-11-openjdk-devel

ENV JAVA_HOME /usr/lib/jvm/java-11-openjdk
ENV KAFKA_HOME /kafka
RUN export JAVA_HOME

RUN cd /tmp && \
    git clone https://github.com/apache/kafka.git -b trunk && \
    cd kafka && \
    ./gradlew clean releaseTarGz --rerun-tasks

RUN rm -rf $KAFKA_HOME/libs/* && \
    tar -xzf /tmp/kafka/core/build/distributions/kafka_*-SNAPSHOT.tgz -C $KAFKA_HOME --strip-components 1 &&\
    rm -f /tmp/kafka/core/build/distributions/kafka_*-SNAPSHOT.tgz &&\
    chmod -R g+w,o+w $KAFKA_HOME

#
# Allow random UID to use Kafka
#
RUN chmod -R g+w,o+w $KAFKA_HOME

USER kafka