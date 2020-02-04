package de.sowrong.pokemonquartet.game;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import de.sowrong.pokemonquartet.R;
import de.sowrong.pokemonquartet.RunningSinglePlayerGameActivity;
import de.sowrong.pokemonquartet.ai.Ai;
import de.sowrong.pokemonquartet.ai.Difficulty;
import de.sowrong.pokemonquartet.data.Importer;
import de.sowrong.pokemonquartet.data.Player;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;


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
    private static Context context;

    private static SinglePlayerGame singlePlayerGame;

    public static final int MIN_CARDS = 2;
    public static final int MAX_CARDS = 30;

    public SinglePlayerGame(Difficulty difficulty, int numberRounds, Context newContext) {
        context = newContext;
        this.selfPlayer = new Player(true);
        this.aiPlayer = new Player(true);
        this.ai = new Ai(this, difficulty);
        this.numberRounds = numberRounds;
        this.currentRound = 1;
        this.selfPlayerIsActivePlayer = true;
        this.pokemon = new ArrayList<>(Arrays.asList(Importer.getPokemon(context.getAssets())));
    }

    public static void init(Context newContext) {
        context = newContext;
    }


    public void reset(Difficulty difficulty, int numberRounds) {
        this.ai.setDifficulty(difficulty);
        this.numberRounds = numberRounds;
        this.selfPlayerIsActivePlayer = true;
        this.hasEndEnded = false;

        ArrayList<Pokemon> shuffledList = (ArrayList<Pokemon>) pokemon.clone();
        Collections.shuffle(shuffledList);

        ArrayList<Pokemon> shuffledPlayerPokemon = new ArrayList<>(shuffledList.subList(0, numberRounds));
        ArrayList<Pokemon> shuffledAiPokemon = new ArrayList<>(shuffledList.subList(numberRounds, numberRounds*2));

        selfPlayer.reset(shuffledPlayerPokemon);
        aiPlayer.reset(shuffledAiPokemon);
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

    public static void store() {
        persistentWrite(singlePlayerGame);
    }

    public static boolean restore() {
        if (singlePlayerGame == null) {
            singlePlayerGame = persistentRead();
        }

        return (singlePlayerGame != null);
    }

    public static void persistentWrite(SinglePlayerGame singlePlayerGame) {
        ObjectOutputStream objectOut = null;
        try {
            FileOutputStream fileOut = context.openFileOutput(context.getString(R.string.saveGameFilename), Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(singlePlayerGame);
            fileOut.getFD().sync();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("persistentWrite", "Save game file could not be created due to an ioexception.");
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    Log.d("persistentWrite", "Save game file could not be closed due to an ioexception.");
                }
            }
        }
    }

    public static SinglePlayerGame persistentRead() {
        ObjectInputStream objectIn = null;
        try {
            FileInputStream fileIn = context.getApplicationContext().openFileInput(context.getString(R.string.saveGameFilename));
            objectIn = new ObjectInputStream(fileIn);
            return (SinglePlayerGame)objectIn.readObject();
        } catch (FileNotFoundException e) {
            Log.e("SinglePlayerGame", "Input save game file " + context.getString(R.string.saveGameFilename) + " not found.");
            return null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.e("SinglePlayerGame", "Input save game exception ClassNotFound.");
            return null;
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                    Log.e("SinglePlayerGame", "Input save game file " + context.getString(R.string.saveGameFilename) + " could not be closed.");
                    return null;
                }
            }
        }
        return null;
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
            hasEndEnded = true;
        }
        else {
            currentRound++;
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
