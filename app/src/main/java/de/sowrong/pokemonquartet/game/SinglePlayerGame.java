package de.sowrong.pokemonquartet.game;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.sowrong.pokemonquartet.RunningSinglePlayerGameActivity;
import de.sowrong.pokemonquartet.ai.Ai;
import de.sowrong.pokemonquartet.ai.Difficulty;
import de.sowrong.pokemonquartet.data.Importer;
import de.sowrong.pokemonquartet.data.Player;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;

public class SinglePlayerGame implements Serializable {
    private ArrayList<Pokemon> pokemon;
    private Player selfPlayer;
    private Player aiPlayer;
    private Ai ai;
    private int currentRound;
    private int numberRounds;
    private boolean selfPlayerIsActivePlayer;
    private Stat currentRoundStat;
    private boolean hasEndEnded;

    private static SinglePlayerGame singlePlayerGame;

    public static final int MIN_CARDS = 2;
    public static final int MAX_CARDS = 30;

    public SinglePlayerGame(Difficulty difficulty, int numberRounds, Context context) {
        selfPlayer = new Player(true);
        aiPlayer = new Player(true);
        ai = new Ai(this, difficulty);
        this.numberRounds = numberRounds;
        currentRound = 1;
        this.selfPlayerIsActivePlayer = true;
        pokemon = new ArrayList<>(Arrays.asList(Importer.getPokemon(context.getAssets())));
    }

    public void reset(Difficulty difficulty, int numberRounds) {
        this.ai.setDifficulty(difficulty);
        this.numberRounds = numberRounds;
        this.selfPlayerIsActivePlayer = true;
        this.hasEndEnded = false;

        ArrayList<Pokemon> shuffledList = (ArrayList<Pokemon>) pokemon.clone();
        Collections.shuffle(shuffledList);

        selfPlayer.reset(shuffledList.subList(0, numberRounds));
        aiPlayer.reset(shuffledList.subList(numberRounds, numberRounds*2));
        currentRound = 1;
    }

    public static void createNewSinglePlayerGame(Difficulty difficulty, int numberRounds, Context context) {
        if (singlePlayerGame == null) {
            singlePlayerGame = new SinglePlayerGame(difficulty, numberRounds, context);
        }

        singlePlayerGame.reset(difficulty, numberRounds);
    }

    public static SinglePlayerGame getSinglePlayerGame() {
        return singlePlayerGame;
    }

    public static boolean singlePlayerGameExists() {
        return singlePlayerGame != null;
    }

    public boolean isLastRound() {
        return currentRound == numberRounds;
    }

    public Pokemon getPlayerPokemonCurrentTurn() {
        if(currentRound < 1 || currentRound > MAX_CARDS) {
            Log.e("SinglePlayerGame", "Turn out of bound: " + currentRound);
            return null;
        }

        return selfPlayer.getPokemon(currentRound-1);
    }

    public Pokemon getAiPokemonCurrentTurn() {
        if(currentRound < 1 || currentRound > MAX_CARDS) {
            Log.e("SinglePlayerGame", "Turn out of bound: " + currentRound);
            return null;
        }

        return aiPlayer.getPokemon(currentRound-1);
    }

    public void takeAiTurn(RunningSinglePlayerGameActivity gameActivity) {
        //singlePlayerGame.takeAiTurn(textView, button);
        currentRoundStat = ai.nextMove();
        gameActivity.opponentHasChoseStat();
    }

    public void takePlayerTurn(Stat stat) {
        currentRoundStat = stat;
    }

    public void increaseRoundCounter() {
        if (currentRound >= numberRounds) {
            return;
        }

        if (++currentRound == numberRounds) {
            hasEndEnded = true;
        }
    }

    public int getCurrentRound() { return currentRound; }
    public int getNumberRounds() { return numberRounds; }

    public boolean myTurn() {
        return selfPlayerIsActivePlayer;
    }

    public void updateActivePlayerAndPoints() {
        switch(winnerOfRound()) {
            case WINNER:
                selfPlayer.roundWon();
                selfPlayerIsActivePlayer = true;
                break;
            case LOSER:
                aiPlayer.roundWon();
                selfPlayerIsActivePlayer = false;
                ai.nextMove();
                //todo ai stuff
                break;
            default:
                selfPlayer.roundTied();
                aiPlayer.roundTied();
        }
    }

    public ScoreResult winnerOfRound() {
        long playerPokemonStatValue = getPlayerPokemonCurrentTurn().getStats().getValueByStat(currentRoundStat);
        long aiPokemonStatsValue = getAiPokemonCurrentTurn().getStats().getValueByStat(currentRoundStat);

        if (playerPokemonStatValue > aiPokemonStatsValue) {
            return ScoreResult.WINNER;
        }
        else if (playerPokemonStatValue < aiPokemonStatsValue) {
            return ScoreResult.LOSER;
        }
        else {
            return ScoreResult.TIE;
        }
    }

    public ScoreResult gameResult() {
        if (getPlayerPoints() > getAiPoints()) {
            return ScoreResult.WINNER;
        }
        else if (getPlayerPoints() < getAiPoints()) {
            return ScoreResult.LOSER;
        }
        else {
            return ScoreResult.TIE;
        }
    }

    public int getPlayerPoints() {
        return selfPlayer.getPoints();
    }

    public int getAiPoints() {
        return aiPlayer.getPoints();
    }

    public Stat getChosenStat() {
        return currentRoundStat;
    }

    public boolean hasEnded() {
        return hasEndEnded;
    }
}
