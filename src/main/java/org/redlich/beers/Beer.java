package org.redlich.beers;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
public class Beer {
    @Id
    private int id;

    @Column
    private String name;

    @Column
    private BeerType type;

    @Column("brewer_id")
    private int brewerId;

    @Column
    private double abv;

    public Beer() {
        id = 0;
        name = "{ beer name }";
        type = BeerType.ALE;
        brewerId = 0;
        abv = 10.0;
        }

    private Beer(int id, String name, BeerType type, int brewerId, double abv) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.brewerId = brewerId;
        this.abv = abv;
        }

    /**
     * public int getId()
     * @return id of the Beer entity.
     */
    public int getId() {
        return id;
        }

    /**
     * public String getName()
     * @return the name of the beer.
     */
    public String getName() {
        return name;
        }

    /**
     *
     * @return the beer type.
     */
    public BeerType getType() {
        return type;
        }

    /**
     *
     * @return the value of `brewerId` from the Brewer entity.
     */
    public int getBrewerId() {
        return brewerId;
        }

    /**
     *
     * @return the value of `abv`.
     */
    public double getAbv() {
        return abv;
        }

    /**
     * Defines the id of the Beer entity. It's required by JNoSQL
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Defines the name of the Beer entity. It's required by JNoSQL.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Defines the type of the Beer entity. It's required by JNoSQL.
     *
     * @param type
     */
    public void setType(BeerType type) {
        this.type = type;
    }

    /**
     * Defines the id of the Brewer entity. It's required by JNoSQL.
     *
     * @param brewerId
     */
    public void setBrewerId(int brewerId) {
        this.brewerId = brewerId;
    }

    /**
     * Defines the value of `abv`. It's required by JNoSQL.
     *
     * @param abv
     */
    public void setAbv(double abv) {
        this.abv = abv;
    }

    @Override
    public String toString() {
        return "Beer { " +
                "id = '" + getId() + '\'' +
                ", name = '" + getName() + '\'' +
                ", type = '" + getType() + '\'' +
                ", brewer_id = '" + getBrewerId() + '\'' +
                ", abv = '" + getAbv() + '\'' +
                " }\n";
        }

    public static BeerBuilder builder() {
        return new BeerBuilder();
        }

    public static class BeerBuilder {
        private int id;
        private String name;
        private BeerType type;
        private int brewer_id;
        private double abv;

        private BeerBuilder() {
            }

        public BeerBuilder id(int id) {
            this.id = id;
            return this;
            }

        public BeerBuilder name(String name) {
            this.name = name;
            return this;
            }

        public BeerBuilder type(BeerType type) {
            this.type = type;
            return this;
            }

        public BeerBuilder brewer_id(int brewer_id) {
            this.brewer_id = brewer_id;
            return this;
            }

        public BeerBuilder abv(double abv) {
            this.abv = abv;
            return this;
            }

        public Beer build() {
            return new Beer(id, name, type, brewer_id, abv);
            }
        }
    }
