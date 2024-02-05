package org.redlich.beers.it;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.eclipse.jnosql.databases.mongodb.communication.MongoDBDocumentConfigurations;
import org.eclipse.jnosql.mapping.core.config.MappingConfigurations;
import org.eclipse.jnosql.mapping.core.config.MicroProfileSettings;
import org.microshed.testing.SharedContainerConfiguration;
import org.microshed.testing.testcontainers.ApplicationContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

public class AppContainerConfig implements SharedContainerConfiguration {

    @Container
    public static final MongoDBContainer MONGODB_CONTAINER =
            new MongoDBContainer(DockerImageName.parse("mongo:latest"))
                    .withNetworkAliases("mongodb");

    @Container
    public static final ApplicationContainer APPLICATION_CONTAINER =
            new ApplicationContainer().withAppContextRoot("/beers")
                    .withEnv("JNOSQL_MONGODB_HOST", "mongodb:27017")
                    .waitingFor(Wait.forLogMessage(".* beers was successfully deployed.*\\s", 1))
                    .dependsOn(MONGODB_CONTAINER);

    static MongoClient mongoClient() {
        return MongoClients.create(MONGODB_CONTAINER.getConnectionString());
    }

    static MongoDatabase mongoDatabase(){
        var databaseName = MicroProfileSettings.INSTANCE.getOrDefault(MappingConfigurations.DOCUMENT_DATABASE.get(),"beers");
        return mongoClient().getDatabase(databaseName);
    }

}
