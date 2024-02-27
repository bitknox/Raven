FROM phusion/baseimage:jammy-1.0.1

RUN apt-get update && \
		add-apt-repository ppa:linuxuprising/java && \
		echo oracle-java16-installer shared/accepted-oracle-license-v1-3 select true | /usr/bin/debconf-set-selections && \
    apt-get install --yes openjdk-16-jdk ca-certificates-java && \
    apt-get clean && \
    update-ca-certificates -f
ENV JAVA_HOME /usr/lib/jvm/java-16-openjdk-amd64/
RUN export JAVA_HOME

# Use baseimage-docker's init system.
CMD ["/sbin/my_init"]