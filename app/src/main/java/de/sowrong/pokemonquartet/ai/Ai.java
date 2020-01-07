package de.sowrong.pokemonquartet.ai;

import java.util.ArrayList;

import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.data.Stats;
import de.sowrong.pokemonquartet.game.Game;

public class Ai {
    private ArrayList<Pokemon> cardPool;
    private Stats optimumStats;
    private Game game;
    private int round;

    private final long MAX_ATTACK = 134;
    private final long MAX_DEFENSE = 180;
    private final long MAX_SPATTACK = 154;
    private final long MAX_SPDEFENSE = 125;
    private final long MAX_HP = 140; // actually 250 is the highest
    private final long MAX_SPEED = 140;

    public Ai(ArrayList<Pokemon> cardPool, Game game) {
        this.cardPool = cardPool;
        this.game = game;
        this.round = 0;

        optimumStats = new Stats();

        optimumStats.setAttack(MAX_ATTACK);
        optimumStats.setDefense(MAX_DEFENSE);
        optimumStats.setSPAtk(MAX_SPATTACK);
        optimumStats.setSPDef(MAX_SPDEFENSE);
        optimumStats.setHP(MAX_HP);
        optimumStats.setSpeed(MAX_SPEED);


    }

    public AiMove nextMove() {
        if (round >= cardPool.size()) {
            return null;
        }

        Pokemon nextPokemon = cardPool.get(round);
        Stat nextStat = calculateBestStat(nextPokemon);

        round++;

        return new AiMove(nextPokemon, nextStat);
    }

    private Stat calculateBestStat(Pokemon pokemon) {
        // the smaller the better (closer to optimum)
        long diffToMax = optimumStats.getAttack() - pokemon.getStats().getAttack();
        Stat bestStat = Stat.ATTACK;

        if ((optimumStats.getDefense() - pokemon.getStats().getDefense()) <  diffToMax) {
            bestStat = Stat.DEFENSE;
            diffToMax = optimumStats.getDefense() - pokemon.getStats().getDefense();
        }

        if ((optimumStats.getSPAtk() - pokemon.getStats().getSPAtk()) <  diffToMax) {
            bestStat = Stat.SPATK;
            diffToMax = optimumStats.getSPAtk() - pokemon.getStats().getSPAtk();
        }

        if ((optimumStats.getSPDef() - pokemon.getStats().getSPDef()) <  diffToMax) {
            bestStat = Stat.SPDEF;
            diffToMax = optimumStats.getSPDef() - pokemon.getStats().getSPDef();
        }

        if ((optimumStats.getHP() - pokemon.getStats().getHP()) <  diffToMax) {
            bestStat = Stat.HP;
            diffToMax = optimumStats.getHP() - pokemon.getStats().getHP();
        }

        if ((optimumStats.getSpeed() - pokemon.getStats().getSpeed()) <  diffToMax) {
            bestStat = Stat.SPEED;
        }

        return bestStat;
    }
}
