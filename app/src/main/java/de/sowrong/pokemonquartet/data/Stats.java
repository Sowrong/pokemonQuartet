package de.sowrong.pokemonquartet.data;
// Stats.java
import java.io.Serializable;
import java.util.Random;

import com.fasterxml.jackson.annotation.*;

public class Stats implements Serializable {
    private final static int USE_FIRST_STATS = 6;

    private long hp;
    private long attack;
    private long defense;
    private long spAtk;
    private long spDef;
    private long speed;
    private long total;

    @JsonProperty("hp")
    public long getHP() { return hp; }
    @JsonProperty("hp")
    public void setHP(long value) { this.hp = value; }

    @JsonProperty("attack")
    public long getAttack() { return attack; }
    @JsonProperty("attack")
    public void setAttack(long value) { this.attack = value; }

    @JsonProperty("defense")
    public long getDefense() { return defense; }
    @JsonProperty("defense")
    public void setDefense(long value) { this.defense = value; }

    @JsonProperty("sp.atk")
    public long getSPAtk() { return spAtk; }
    @JsonProperty("sp.atk")
    public void setSPAtk(long value) { this.spAtk = value; }

    @JsonProperty("sp.def")
    public long getSPDef() { return spDef; }
    @JsonProperty("sp.def")
    public void setSPDef(long value) { this.spDef = value; }

    @JsonProperty("speed")
    public long getSpeed() { return speed; }
    @JsonProperty("speed")
    public void setSpeed(long value) { this.speed = value; }

    @JsonProperty("total")
    public long getTotal() { return total; }
    @JsonProperty("total")
    public void setTotal(long value) { this.total = value; }

    public long getValueByStat(Stat stat) {
        switch (stat) {
            case ATTACK:
                return attack;
            case DEFENSE:
                return defense;
            case SPATK:
                return spAtk;
            case SPDEF:
                return spDef;
            case HP:
                return hp;
            case SPEED:
                return speed;
            default:
                return total;
        }
    }

    public static Stat getRandomStat() {
        Random random = new Random();

        int statId = random.nextInt(USE_FIRST_STATS);
        switch(statId) {
            case 0: return Stat.ATTACK;
            case 1: return Stat.DEFENSE;
            case 2: return Stat.SPATK;
            case 3: return Stat.SPDEF;
            case 4: return Stat.HP;
            default:    return Stat.SPEED;
        }
    }
}