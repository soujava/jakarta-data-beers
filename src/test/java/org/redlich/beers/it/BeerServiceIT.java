package org.redlich.beers.it;

import com.mongodb.client.MongoCollection;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import net.datafaker.Faker;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;
import org.redlich.beers.Beer;
import org.redlich.beers.BeerType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.redlich.beers.it.AppContainerConfig.mongoDatabase;

@MicroShedTest
@SharedContainerConfig(AppContainerConfig.class)
class BeerServiceIT {

    private final static Faker faker = new Faker();

    private static final AtomicInteger ids = new AtomicInteger(0);

    static Beer createBeer() {
        return Beer.builder()
                .id(ids.incrementAndGet())
                .name(faker.beer().name())
                .type(BeerType.values()[faker.random().nextInt(0, BeerType.values().length - 1)])
                .brewerId(faker.random().nextInt(1, 100))
                .abv(faker.random().nextDouble(0.01, 100.0))
                .build();
    }

    @BeforeAll
    static void setupRestAssured() {
        var omConfig = ObjectMapperConfig.objectMapperConfig()
                .defaultObjectMapperType(ObjectMapperType.JACKSON_2);
        RestAssured.config = RestAssured.config.objectMapperConfig(omConfig);
    }

    MongoCollection<Document> beerCollection;

    @BeforeEach
    @AfterEach
    void resetDatabase() {
        beerCollection = beerCollection != null ? beerCollection : mongoDatabase().getCollection("Beer");
        beerCollection.deleteMany(new BsonDocument());
    }

    @Test
    void shouldAddBeer() {

        var beers = List.of(createBeer(), createBeer(), createBeer());
        beers.forEach(this::addBeer);
        assertThat(beerCollection.countDocuments())
                .as("number of beers in database")
                .isEqualTo(beers.size());
    }

    private void addBeer(Beer beer) {
        given().when()
                .contentType(ContentType.JSON)
                .body(beer)
                .post("/db/beer/" + beer.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void shouldRetrieveBeers() {

        assertSoftly(softly -> {
            var retrievedEmptyBeerList = retrieveBeers();

            softly.assertThat(retrievedEmptyBeerList)
                    .as("retrieved beers cannot be null")
                    .isNotNull()
                    .as("for a empty database, the retrieved beers should be empty")
                    .isEmpty();

            var beer = createBeer();
            addBeer(beer);
            final List<Beer> retrievedBeers = retrieveBeers();


            softly.assertThat(retrievedBeers)
                    .as("retrieved beers cannot be null")
                    .isNotNull()
                    .as("for a non-empty database, retrieved beers cannot be empty")
                    .isNotEmpty()
                    .as("retrieved beers should contain one beer")
                    .hasSize(1);

            var retrievedBeer = retrievedBeers.get(0);

            softly.assertThat(retrievedBeer)
                    .as("retrieved beer should not be null")
                    .isNotNull()
                    .as("retrieved beer should have the same id")
                    .hasFieldOrPropertyWithValue("id", beer.getId())
                    .as("retrieved beer should have the same name")
                    .hasFieldOrPropertyWithValue("name", beer.getName())
                    .as("retrieved beer should have the same type")
                    .hasFieldOrPropertyWithValue("type", beer.getType())
                    .as("retrieved beer should have the same brewer_id")
                    .hasFieldOrPropertyWithValue("brewerId", beer.getBrewerId())
                    .as("retrieved beer should have the same abv")
                    .hasFieldOrPropertyWithValue("abv", beer.getAbv());


            var moreBeers = List.of(createBeer(), createBeer(), createBeer());
            moreBeers.forEach(this::addBeer);

            var retrievedAllBeers = retrieveBeers();

            softly.assertThat(retrievedAllBeers)
                    .as("retrieved beers cannot be null")
                    .isNotNull()
                    .as("for a non-empty database, retrieved beers cannot be empty")
                    .isNotEmpty()
                    .as("number of the retrieved beers is not correct")
                    .hasSize(1 + moreBeers.size());
        });

    }

    private static List<Beer> retrieveBeers() {
        return given()
                .log().ifValidationFails()
                .when()
                .accept(ContentType.JSON)
                .get("/db/beer")
                .then()
                .log().ifError()
                .statusCode(200)
                .extract().as(new TypeRef<List<Beer>>() {
                });
    }

    @Test
    void shouldRemoveAllBeers() {
        var beers = List.of(createBeer(), createBeer(), createBeer());
        beers.forEach(this::addBeer);
        assertThat(beerCollection.countDocuments())
                .as("cannot prepare the database: number of beers in database is incorrect")
                .isEqualTo(beers.size());

        given()
                .log().ifValidationFails()
                .when()
                .delete("/db/beer")
                .then()
                .statusCode(204);

        assertThat(beerCollection.countDocuments())
                .as("number of beers in database after remove all is incorrect")
                .isZero();
    }

    @Test
    void shouldRemoveExactBeer() {
        var beers = new ArrayList<Beer>(List.of(createBeer(), createBeer(), createBeer()));
        beers.forEach(this::addBeer);
        assertThat(beerCollection.countDocuments())
                .as("cannot prepare the database: number of beers in database is incorrect")
                .isEqualTo(beers.size());

        var removedBeer = beers.remove(0);

        given()
                .log().ifValidationFails()
                .when()
                .delete("/db/beer/" + removedBeer.getId())
                .then()
                .statusCode(204);

        assertThat(beerCollection.countDocuments())
                .as("number of beers in database after remove one is incorrect")
                .isEqualTo(beers.size());
    }
}