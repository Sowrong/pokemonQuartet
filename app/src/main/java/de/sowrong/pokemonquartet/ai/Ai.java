package de.sowrong.pokemonquartet.ai;

import java.util.ArrayList;
import java.util.List;

import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.data.Stats;
import de.sowrong.pokemonquartet.game.MultiPlayerGame;
import de.sowrong.pokemonquartet.game.SinglePlayerGame;

public class Ai {
    private Stats optimumStats;
    private Difficulty difficulty;
    SinglePlayerGame singlePlayerGame;

    private final long MAX_ATTACK = 134;
    private final long MAX_DEFENSE = 180;
    private final long MAX_SPATTACK = 154;
    private final long MAX_SPDEFENSE = 125;
    private final long MAX_HP = 140; // actually 250 is the highest
    private final long MAX_SPEED = 140;

    public Ai(SinglePlayerGame singlePlayerGame, Difficulty difficulty) {
        this.singlePlayerGame = singlePlayerGame;
        this.difficulty = difficulty;

        optimumStats = new Stats();

        optimumStats.setAttack(MAX_ATTACK);
        optimumStats.setDefense(MAX_DEFENSE);
        optimumStats.setSPAtk(MAX_SPATTACK);
        optimumStats.setSPDef(MAX_SPDEFENSE);
        optimumStats.setHP(MAX_HP);
        optimumStats.setSpeed(MAX_SPEED);
    }

    public Stat nextMove() {
        Pokemon aiPokemon = singlePlayerGame.getAiPokemonCurrentTurn();
        Pokemon playerPokemon = singlePlayerGame.getPlayerPokemonCurrentTurn();

        Stat nextStat;

        switch (difficulty) {
            case MEDIUM:
                nextStat = calculateMediumStat(aiPokemon);
                break;
            case HARD:
                nextStat = calculateHardStat(aiPokemon.getStats(), playerPokemon.getStats());
                break;
            default:
                nextStat = Stats.getRandomStat();
        }

        return nextStat;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    private Stat calculateHardStat(Stats aiPokemonStats, Stats playerPokemonStats) {
        if ((aiPokemonStats.getAttack()  - playerPokemonStats.getAttack()) > 0) {
            return Stat.ATTACK;
        }

        if ((aiPokemonStats.getDefense() - playerPokemonStats.getDefense()) > 0) {
            return Stat.DEFENSE;
        }

        if ((aiPokemonStats.getSPAtk() - playerPokemonStats.getSPAtk()) > 0) {
            return Stat.SPATK;
        }

        if ((aiPokemonStats.getSPDef() - playerPokemonStats.getSPDef()) > 0) {
            return Stat.SPDEF;
        }

        if ((aiPokemonStats.getHP() - playerPokemonStats.getHP()) > 0) {
            return Stat.HP;
        }

        if ((aiPokemonStats.getSpeed() - playerPokemonStats.getSpeed()) > 0) {
            return Stat.SPEED;
        }

        return Stat.HP; // default
    }


    private Stat calculateMediumStat(Pokemon pokemon) {
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
