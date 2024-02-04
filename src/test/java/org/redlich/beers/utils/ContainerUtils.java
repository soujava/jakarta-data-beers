package org.redlich.beers.utils;

import jakarta.ws.rs.core.UriBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;

public interface ContainerUtils {

    int HTTP_PORT = 8080;

    DockerImageName PAYARA_SERVER_FULL = DockerImageName.parse("payara/server-full:6.2024.1-jdk17");

    int MONGO_PORT = 27017;

    DockerImageName MONGODB = DockerImageName.parse("mongo:latest");

    static URI buildURI(GenericContainer container, String path) {
        return UriBuilder.fromUri("http://" + container.getHost())
                .port(container.getMappedPort(HTTP_PORT))
                .path(path)
                .build();
    }
}
