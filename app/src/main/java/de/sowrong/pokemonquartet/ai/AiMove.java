package de.sowrong.pokemonquartet.ai;

import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;

public class AiMove {
    private Pokemon pokemon;
    private Stat chosenStat;

    public AiMove(Pokemon pokemon, Stat chosenStat) {
        this.pokemon = pokemon;
        this.chosenStat = chosenStat;
    }

    public Pokemon getPokemon() {
        return pokemon;
    }

    public Stat getChosenStat() {
        return chosenStat;
    }

}
