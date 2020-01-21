package de.sowrong.pokemonquartet.data;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private List<Pokemon> pokemon;
    private int points;
    boolean connected;

    public Player(boolean connected) {
        this.connected = connected;
        this.points = 0;
        pokemon = new ArrayList<>();
    }

    public void reset(List<Pokemon> newPokemonSet) {
        this.points = 0;
        pokemon = newPokemonSet;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setPokemon(List<Pokemon> pokemon) {
        this.pokemon = pokemon;
    }

    public Pokemon getPokemon(int index) {
        return pokemon.get(index);
    }

    public void setConnected() {
        connected = true;
    }

    public void roundWon() {
        points+=2;
    }

    public void roundTied() {
        points+=1;
    }

    public int getPoints() {
        return points;
    }
}
