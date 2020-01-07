package de.sowrong.pokemonquartet.data;
import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
// Pokemon.java

public class Pokemon implements Serializable {
    private String id;
    private String name;
    private String filename;
    private String species;
    private String[] type;
    private String height;
    private String weight;
    private Stats stats;
    private String[] evolution;

    public static final String ATTACK = "ATTACK";
    public static final String DEFENSE = "DEFENSE";
    public static final String SPATK = "SP. ATK.";
    public static final String SPDEF = "SP. DEF.";
    public static final String SPEED = "SPEED";
    public static final String HP = "HP";
    public static final String TOTAL = "TOTAL";

    @JsonProperty("id")
    public String getID() { return id; }
    @JsonProperty("id")
    public void setID(String value) { this.id = value; }

    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("name")
    public void setName(String value) { this.name = value; }

    @JsonProperty("filename")
    public String getFilename() { return filename; }
    @JsonProperty("filename")
    public void setFilename(String value) { this.filename = value; }

    @JsonProperty("species")
    public String getSpecies() { return species; }
    @JsonProperty("species")
    public void setSpecies(String value) { this.species = value; }

    @JsonProperty("type")
    public String[] getType() { return type; }
    @JsonProperty("type")
    public void setType(String[] value) { this.type = value; }

    @JsonProperty("height")
    public String getHeight() { return height; }
    @JsonProperty("height")
    public void setHeight(String value) { this.height = value; }

    @JsonProperty("weight")
    public String getWeight() { return weight; }
    @JsonProperty("weight")
    public void setWeight(String value) { this.weight = value; }

    @JsonProperty("stats")
    public Stats getStats() { return stats; }
    @JsonProperty("stats")
    public void setStats(Stats value) { this.stats = value; }

    @JsonProperty("evolution")
    public String[] getEvolution() { return evolution; }
    @JsonProperty("evolution")
    public void setEvolution(String[] value) { this.evolution = value; }

    public long getStatValue(Stat stat) {
        switch (stat) {
            case ATTACK:
                return stats.getAttack();
            case DEFENSE:
                return stats.getDefense();
            case SPATK:
                return stats.getSPAtk();
            case SPDEF:
                return stats.getSPDef();
            case SPEED:
                return stats.getSpeed();
            case HP:
                return stats.getHP();
            case TOTAL:
                return stats.getTotal();
        }
        return -1;
    }

    public static Stat getStatByString(String statString) {
        switch (statString) {
            case Pokemon.ATTACK:
                return Stat.ATTACK;
            case Pokemon.DEFENSE:
                return Stat.DEFENSE;
            case Pokemon.HP:
                return Stat.HP;
            case Pokemon.SPEED:
                return Stat.SPEED;
            case Pokemon.SPATK:
                return Stat.SPATK;
            case Pokemon.SPDEF:
                return Stat.SPDEF;
            default:
                return Stat.TOTAL;
        }
    }

    public static String getStringByStat(Stat stat) {
        switch (stat) {
            case ATTACK:
                return Pokemon.ATTACK;
            case DEFENSE:
                return Pokemon.DEFENSE;
            case HP:
                return Pokemon.HP;
            case SPEED:
                return Pokemon.SPEED;
            case SPATK:
                return Pokemon.SPATK;
            case SPDEF:
                return Pokemon.SPDEF;
            default:
                return Pokemon.TOTAL;
        }
    }

    public String toString() {
        return String.format("id: %s, name: %s, hp: %s", id, name, stats.getHP());
    }
}