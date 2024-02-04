package org.redlich.beers;

import jakarta.data.Sort;
import jakarta.data.page.Page;
import jakarta.data.page.Pageable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("beer")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class BeerService {

    @Inject
    BeerRepository beerRepository;

    @POST
    @Path("/{id}")
    public String add(@PathParam("id") int id, Beer beer) {

        try {
            beer.setId(id);
            beerRepository.save(beer);
        } catch (ConstraintViolationException x) {
            JsonArrayBuilder messages = Json.createArrayBuilder();
            for (ConstraintViolation<?> v : x.getConstraintViolations()) {
                messages.add(v.getMessage());
            }
            return messages.build().toString();
        }
        return "";
    }

    @DELETE
    @Path("/{id}")
    public void remove(@PathParam("id") int id) {
        beerRepository.deleteById(id);
    }

    @GET
    public String retrieve() {
        Iterable<Beer> beerRepositoryIterable = beerRepository.findAll()::iterator;
        return beerRepositoryToJsonArray(beerRepositoryIterable);
    }

    @GET
    @Path("/brewer/{brewer}")
    public String retrieveByBrewer(@PathParam("brewer") String beer) {
        // List<Beer> beerRepositoryList = beerRepository.findByBrewer(Brewer.fromString(brewer));
        List<Beer> beerRepositoryList = beerRepository.findByBeer(beer);
        return beerRepositoryToJsonArray(beerRepositoryList);
    }

    @GET
    @Path("/brewer/{brewer}/page/{pageNum}")
    public String retrieveByBrewer(@PathParam("brewer") String beer, @PathParam("pageNum") long pageNum) {

        Pageable pageRequest = Pageable.ofSize(5)
                .page(pageNum)
                .sortBy(Sort.asc("name"), Sort.asc("id"));

        // Page<Beer> page = beerRepository.findByBrewer(Brewer.fromString(brewer), pageRequest);
        Page<Beer> page = beerRepository.findByBeer(beer, pageRequest);

        return beerRepositoryToJsonArray(page);
    }

    @DELETE
    public void remove() {
        beerRepository.deleteAll();
    }

    private String beerRepositoryToJsonArray(Iterable<Beer> beerRepository) {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        for (Beer c : beerRepository) {
            JsonObject json = Json.createObjectBuilder()
                    .add("name", c.getName())
                    .add("id", c.getId())
                    .add("abv", c.getAbv())
                    .add("type", c.getType().name())
                    .add("brewerId", c.getBrewerId()).build();
            jab.add(json);
        }
        return jab.build().toString();
    }
}
