package de.sowrong.pokemonquartet.game;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import de.sowrong.pokemonquartet.RunningMultiPlayerGameActivity;
import de.sowrong.pokemonquartet.data.DatabaseConnector;
import de.sowrong.pokemonquartet.data.GameState;
import de.sowrong.pokemonquartet.data.Importer;
import de.sowrong.pokemonquartet.data.Player;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.data.Stats;

public class MultiPlayerGame implements Serializable {
    private ConnectionType connectionType;
    private DatabaseConnector databaseConnector;
    private ArrayList<Pokemon> pokemon;
    private Player self;
    private Player opponent;
    private int currentRound;
    private int numberRounds;
    private boolean selfIsActivePlayer;
    private RunningMultiPlayerGameActivity activity;
    private boolean waitingForCallbackFinished;

    private static MultiPlayerGame multiPlayerGame;

    public static final int MIN_CARDS = 2;
    public static final int MAX_CARDS = 30;

    public MultiPlayerGame() {
        waitingForCallbackFinished = false;
        self = new Player(true);
        opponent = new Player(false);
    }

    public void initGame(Context context) {
        numberRounds = MIN_CARDS;
        currentRound = 1;
        databaseConnector = new DatabaseConnector(context, this);
        pokemon = new ArrayList<>(Arrays.asList(Importer.getPokemon(context.getAssets())));
    }

    public static MultiPlayerGame getMultiPlayerGame() {
        if (multiPlayerGame == null) {
            multiPlayerGame = new MultiPlayerGame();
        }

        return multiPlayerGame;
    }

    public boolean isLastRound() {
        return currentRound == numberRounds;
    }

    public boolean isHost() {
        return connectionType == ConnectionType.HOST;
    }

    public void initRunningGame(RunningMultiPlayerGameActivity activity) {
        this.activity = activity;
        //selfIsActivePlayer = connectionType == gameState.getStartPlayer();
    }

    public void callbackFinished() {
       waitingForCallbackFinished = false;
    }

    public boolean isWaitingForCallback() {
        return waitingForCallbackFinished;
    }

    public void startGame(final Context context, final Intent intent, final Toast toast, final int numberCards) {
        connectionType = ConnectionType.HOST;
        this.numberRounds = numberCards;
        this.waitingForCallbackFinished = true;
        selfIsActivePlayer = true; // HOST always starts

        databaseConnector.createDatabase(this, context, intent, toast, numberCards);
    }

    public Pokemon getOwnPokemonCurrentTurn() {
        if(currentRound < 1 || currentRound > MAX_CARDS) {
            Log.e("MultiPlayerGame", "Turn out of bound: " + currentRound);
            return null;
        }

        return self.getPokemon(currentRound-1);
    }

    public Pokemon getOpponentPokemonCurrentTurn() {
        if(currentRound < 1 || currentRound > MAX_CARDS) {
            Log.e("MultiPlayerGame", "Turn out of bound: " + currentRound);
            return null;
        }

        return opponent.getPokemon(currentRound-1);
    }

    public void joinGame(int roomId, int numberCards) {
        connectionType = ConnectionType.GUEST;
        this.numberRounds = numberCards;
        databaseConnector.setDatabaseByRoomId(roomId);
        databaseConnector.setValue(GameState.GuestConnectedString, true);
        selfIsActivePlayer = false; // HOST always starts
        callbackFinished();
    }

    public void endGame() {
        if (connectionType == ConnectionType.HOST) {
            databaseConnector.deleteDatabase();
        }
    }

    // active player takes turn
    public boolean takeTurn(Stat stat, Context context) {
        if (!opponent.isConnected()) {
            Toast toastNoOpponent = Toast.makeText(context, "No opponent connected!", Toast.LENGTH_SHORT);
            toastNoOpponent.show();
            return false;
        }

        if (myTurn() && !databaseConnector.isNewStatChosen()) {
            // set state
            databaseConnector.setChosenStat(stat);
            // set has chosen
            databaseConnector.setValue(GameState.NewStatChosenString, true);
            return true;
        }
        else {
            Toast toastOpponentNotReady = Toast.makeText(context, "Opponent not ready!", Toast.LENGTH_SHORT);
            toastOpponentNotReady.show();
            return false;
        }
    }

    // inactive player increases round counter
    public void confirmStartRound() {
        if (databaseConnector.getGameState().isNewStatChosen() && !myTurn()) {
            // increase turn number
            databaseConnector.setValue(GameState.NewStatChosenString, false);
            databaseConnector.setValue(GameState.TurnNumberString, databaseConnector.getGameState().getTurnNumber() + 1);
        }


        currentRound++;
    }

    public void checkRoomExists(int roomId, Context context, Intent intent, Toast toastNotExist, Toast toastFull) {
        this.waitingForCallbackFinished = true;
        databaseConnector.checkRoomExists(this, roomId, context, intent, toastNotExist, toastFull);
    }

    public void initPokemonByRandomSeedId() {
        ArrayList<Pokemon> shuffledList = (ArrayList<Pokemon>) pokemon.clone();
        Collections.shuffle(shuffledList, new Random(databaseConnector.getRoomNumber()));

        if (numberRounds > pokemon.size()/2 || numberRounds < 0) {
            Log.e("MultiPlayerGame", "Number of selected cards exceeds limit of available cards: " + numberRounds);
            return;
        }

        ArrayList<Pokemon> shuffledPokemonSet1 = new ArrayList<>(shuffledList.subList(0, numberRounds));
        ArrayList<Pokemon> shuffledPokemonSet2 = new ArrayList<>(shuffledList.subList(numberRounds, numberRounds*2));

        if (isHost()) {
            self.reset(shuffledPokemonSet1);
            opponent.reset(shuffledPokemonSet2);
        }
        else {
            opponent.reset(shuffledPokemonSet1);
            self.reset(shuffledPokemonSet2);
        }
    }

    public int getRemainingRounds() { return numberRounds - numberRounds; }
    public int getCurrentRound() { return currentRound; }
    public int getNumberRounds() { return numberRounds; }

    public boolean myTurn() {
        return selfIsActivePlayer;
    }

    public void opponentHasChoseStat() {
        // disable infobox, enable button
        activity.opponentHasChoseStat();
    }

    public void playerHasJoinedGame() {
        if (!opponent.isConnected()) {
            opponent.setConnected();
            activity.updatePlayerHasJoinedGame();
        }
    }

    public void updateActivePlayerAndPoints() {
        switch(winnerOfRound()) {
            case WINNER:
                self.roundWon();
                selfIsActivePlayer = true;
                break;
            case LOSER:
                opponent.roundWon();
                selfIsActivePlayer = false;
                break;
            default:
                //activePlayer = activePlayer; - do not change
                self.roundTied();
                opponent.roundTied();
        }
    }

    public ScoreResult winnerOfRound() {
        Stats ownPokemonStats = getOwnPokemonCurrentTurn().getStats();
        Stats opponentPokemonStats = getOpponentPokemonCurrentTurn().getStats();

        Stat choseStat = databaseConnector.getChosenStat();

        if (ownPokemonStats.getValueByStat(choseStat) > opponentPokemonStats.getValueByStat(choseStat)) {
            return ScoreResult.WINNER;
        }
        else if (ownPokemonStats.getValueByStat(choseStat) < opponentPokemonStats.getValueByStat(choseStat)) {
            return ScoreResult.LOSER;
        }
        else {
            return ScoreResult.TIE;
        }
    }

    public ScoreResult gameResult() {
        if (self.getPoints() > opponent.getPoints()) {
            return ScoreResult.WINNER;
        }
        else if (self.getPoints() < opponent.getPoints()) {
            return ScoreResult.LOSER;
        }
        else {
            return ScoreResult.TIE;
        }
    }

    public int getOwnPoints() {
        return self.getPoints();
    }

    public int getOpponentPoints() {
        return opponent.getPoints();
    }

    public Stat getChosenStat() {
        return databaseConnector.getChosenStat();
    }

    public int getRoomNumber() {
        return databaseConnector.getRoomNumber();
    }
}
