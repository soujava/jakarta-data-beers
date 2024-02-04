package org.redlich.beers;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import net.datafaker.Faker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.redlich.beers.utils.ContainerUtils;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.redlich.beers.utils.ContainerUtils.buildURI;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BeerServiceIT {

    private final static Faker faker = new Faker();

    private final static Network INTERNAL_NETWORK = Network.newNetwork();

    @Container
    private static GenericContainer mongodb = new GenericContainer(ContainerUtils.MONGODB)
            .withNetwork(INTERNAL_NETWORK)
            .withNetworkAliases("mongodb")
            .withExposedPorts(ContainerUtils.MONGO_PORT)
            .waitingFor(Wait.defaultWaitStrategy());

    static final MountableFile WAR_DEPLOYABLE = MountableFile
            .forHostPath(Paths.get("target/beers.war").toAbsolutePath(), 0777);

    @Container
    private static GenericContainer beerService = new GenericContainer(ContainerUtils.PAYARA_SERVER_FULL)
            .withNetwork(INTERNAL_NETWORK)
            .withEnv("JNOSQL_MONGODB_HOST", "mongodb:27017")
            .dependsOn(mongodb)
            .withExposedPorts(ContainerUtils.HTTP_PORT)
            .withCopyFileToContainer(WAR_DEPLOYABLE, "/opt/payara/deployments/beers.war")
            .waitingFor(Wait.forLogMessage(".* beers was successfully deployed.*\\s", 1));


    private static final List<Beer> beers = new ArrayList<>(List.of(createBeer(), createBeer(), createBeer()));

    private static Beer createBeer() {
        return Beer.builder()
                .id(faker.random().nextInt(1, 100))
                .name(faker.beer().name())
                .type(BeerType.values()[
                        faker.random().nextInt(0, BeerType.values().length - 1)])
                .brewer_id(faker.random().nextInt(1, 10))
                .abv(faker.random().nextDouble(0.01, 100.0))
                .build();
    }

    @Test
    @Order(1)
    @DisplayName("should add a beer")
    void shouldAddBeer() {

        beers.forEach(beer -> given()
                .contentType(ContentType.JSON)
                .body(beer)
                .when()
                .post(buildURI(beerService, "/beers/db/beer/" + beer.getId()))
                .then()
                .statusCode(200));

    }

    @Test
    @Order(2)
    @DisplayName("should retrieve a beer list")
    void shouldRetrieveBeers() {

        var retrievedBeers = given()
                .accept(ContentType.JSON)
                .when()
                .get(buildURI(beerService, "/beers/db/beer"))
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<Beer>>() {
                });

        assertSoftly(softly -> {

            softly.assertThat(retrievedBeers)
                    .as("should have 1 beer")
                    .hasSize(beers.size());

            softly.assertThat(retrievedBeers)
                    .as("should contain the beers")
                    .containsAll(beers);
        });

    }

    @Test
    @Order(3)
    @DisplayName("should remove a beer")
    void shouldRemoveBeer() {

        var beerToRemove = beers.remove(0);

        given()
                .log().all()
                .when()
                .delete(buildURI(beerService, "/beers/db/beer/" + beerToRemove.getId()))
                .then()
                .statusCode(204);

        var retrievedBeers = given()
                .accept(ContentType.JSON)
                .when()
                .get(buildURI(beerService, "/beers/db/beer"))
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<Beer>>() {
                });

        assertSoftly(softly -> {

            softly.assertThat(retrievedBeers)
                    .as("should have 2 beer")
                    .hasSize(beers.size());

            softly.assertThat(retrievedBeers)
                    .as("should contain the beers")
                    .containsAll(beers);
        });

    }
    @Test
    @Order(4)
    @DisplayName("should remove all beers")
    void shouldRemoveAllBeers() {

        given()
                .when()
                .delete(buildURI(beerService, "/beers/db/beer"))
                .then()
                .statusCode(204);

        var retrievedBeers = given()
                .accept(ContentType.JSON)
                .when()
                .get(buildURI(beerService, "/beers/db/beer"))
                .then()
                .statusCode(200)
                .extract().as(new TypeRef<List<Beer>>() {
                });

        assertThat(retrievedBeers)
                .as("should have 0 beer")
                .isEmpty();
    }
}