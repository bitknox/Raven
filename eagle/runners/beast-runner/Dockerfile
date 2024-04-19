FROM bitnami/spark:3.3.1

USER root

RUN apt-get -y update --fix-missing

ENV BEAST_VERSION 0.10.0-RC1

ADD https://bitbucket.org/bdlabucr/beast/downloads/beast-${BEAST_VERSION}-bin.tar.gz beast-${BEAST_VERSION}-bin.tar.gz
RUN tar -f beast-${BEAST_VERSION}-bin.tar.gz -C $SPARK_HOME -xvz
COPY ./spark-defaults.conf /opt/bitnami/spark/conf/spark-defaults.conf
RUN sed -i -e 's/\r$//' ./beast-${BEAST_VERSION}/bin/start-beast
RUN sed -i -e 's/\r$//' ./beast-${BEAST_VERSION}/bin/beast

EXPOSE 8890