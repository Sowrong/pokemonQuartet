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

import de.sowrong.pokemonquartet.RunningGameActivity;
import de.sowrong.pokemonquartet.data.DatabaseConnector;
import de.sowrong.pokemonquartet.data.GameState;
import de.sowrong.pokemonquartet.data.Importer;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.data.Stats;

public class Game implements Serializable {
    private ConnectionType connectionType;
    private DatabaseConnector databaseConnector;
    private ArrayList<Pokemon> pokemon;
    private Pokemon[] hostPokemon;
    private Pokemon[] guestPokemon;
    private int currentRound;
    private int numberRounds;
    private GameState gameState;
    private boolean activePlayer;
    private int ownPoints;
    private int opponentPoints;
    private RunningGameActivity activity;
    private boolean opponentConnected;
    private boolean waitingForCallbackFinished;

    private static Game game;

    public static final int MIN_CARDS = 3;
    public static final int MAX_CARDS = 30;

    public Game() {
        waitingForCallbackFinished = false;
    }

    public void initGame(Context context) {
        opponentConnected = false;
        numberRounds = MIN_CARDS;
        currentRound = 1;
        ownPoints = 0;
        opponentPoints = 0;
        gameState = new GameState();
        databaseConnector = new DatabaseConnector(context, this);
        pokemon = new ArrayList<>(Arrays.asList(Importer.getPokemon(context.getAssets())));
    }

    public static Game getGame() {
        if (game == null) {
            game = new Game();
        }

        return game;
    }

    public boolean isLastRound() {
        return currentRound == numberRounds;
    }

    public boolean isHost() {
        return connectionType == ConnectionType.HOST;
    }

    public void initRunningGame(RunningGameActivity activity) {
        this.activity = activity;
        activePlayer = connectionType == gameState.getStartPlayer();
    }

    public void gameStarted() {
       waitingForCallbackFinished = false;
    }

    public boolean isWaitingForCallback() {
        return waitingForCallbackFinished;
    }

    public void startGame(final Context context, final Intent intent, final Toast toast, final int numberCards) {
        connectionType = ConnectionType.HOST;
        this.numberRounds = numberCards;
        this.waitingForCallbackFinished = true;

        databaseConnector.createDatabase(this, context, intent, toast, numberCards);
    }

    public Pokemon getOwnPokemonCurrentTurn() {
        if(currentRound < 1 || currentRound > MAX_CARDS) {
            Log.e("Game", "Turn out of bound: " + currentRound);
            return getOwnPokemon()[0];
        }

        if (currentRound > numberRounds) {
            return null;
        }

        return getOwnPokemon()[currentRound-1];
    }

    public Pokemon getOpponentPokemonCurrentTurn() {
        if(currentRound < 1 || currentRound > MAX_CARDS) {
            Log.e("Game", "Turn out of bound: " + currentRound);
            return getOwnPokemon()[0];
        }

        return getOpponentsPokemon()[currentRound-1];
    }

    public void joinGame(int roomId, int numberCards) {
        connectionType = ConnectionType.GUEST;
        this.numberRounds = numberCards;
        databaseConnector.setDatabaseByRoomId(roomId);
        databaseConnector.setValue(GameState.GuestConnectedString, true);
        gameStarted();
    }

    public void endGame() {
        if (connectionType == ConnectionType.HOST) {
            databaseConnector.deleteDatabase();
        }
    }

    // active player takes turn
    public boolean takeTurn(Stat stat, Context context) {
        if (!gameState.isGuestConnected()) {
            Toast toastNoOpponent = Toast.makeText(context, "No opponent connected!", Toast.LENGTH_SHORT);
            toastNoOpponent.show();
            return false;
        }

        if (myTurn() && !gameState.isNewStatChosen()) {
            // set state
            gameState.setChosenStat(stat);

            databaseConnector.setValue(GameState.ChosenStatString, stat);
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

    public void evaluateRound() {

    }


    // inactive player increases round counter
    public void confirmStartRound() {
        if (gameState.isNewStatChosen() && !myTurn()) {
            // increase turn number
            databaseConnector.setValue(GameState.NewStatChosenString, false);
            databaseConnector.setValue(GameState.TurnNumberString, gameState.getTurnNumber() + 1);
        }


        currentRound++;
    }

    public void checkRoomExists(int roomId, Context context, Intent intent, Toast toast) {
        this.waitingForCallbackFinished = true;
        databaseConnector.pathExists(this, roomId, context, intent, toast);
    }

    public GameState getGameState() {
        return gameState;
    }

    public void initPokemonByRoomId() {
        ArrayList<Pokemon> shuffledList = (ArrayList<Pokemon>) pokemon.clone();
        Collections.shuffle(shuffledList, new Random(databaseConnector.getRandomSeed()));

        if (numberRounds > pokemon.size()/2 || numberRounds < 0) {
            Log.e("Game", "Number of selected cards exceeds limit of available cards: " + numberRounds);
            return;
        }

        hostPokemon = new Pokemon[numberRounds];
        guestPokemon = new Pokemon[numberRounds];

        hostPokemon = shuffledList.subList(0, numberRounds).toArray(hostPokemon);
        guestPokemon = shuffledList.subList(numberRounds, numberRounds*2).toArray(guestPokemon);
    }

    public Pokemon[] getOwnPokemon() {
        if (connectionType == ConnectionType.HOST)
            return hostPokemon;
        else
            return guestPokemon;
    }

    public Pokemon[] getOpponentsPokemon() {
        if (connectionType != ConnectionType.HOST)
            return hostPokemon;
        else
            return guestPokemon;
    }

    public int getRemainingRounds() { return numberRounds - numberRounds; }
    public int getCurrentRound() { return currentRound; }
    public int getNumberRounds() { return numberRounds; }

    public boolean myTurn() {
        return activePlayer;
    }

    public void opponentHasChoseStat() {
        // disable infobox, enable button
        activity.opponentHasChoseStat();
    }

    public void playerHasJoinedGame() {
        if (!opponentConnected) {
            opponentConnected = true;
            activity.updatePlayerHasJoinedGame();
        }
    }

    public void updateActivePlayerAndPoints() {
        switch(winnerOfRound()) {
            case WINNER:
                ownPoints+=2;
                activePlayer = true;
                break;
            case LOSER:
                opponentPoints+=2;
                activePlayer = false;
                break;
            default:
                //activePlayer = activePlayer; - do not change
                ownPoints++;
                opponentPoints++;
        }
    }

    public ScoreResult winnerOfRound() {
        Stats ownPokemonStats = getOwnPokemonCurrentTurn().getStats();
        Stats opponentPokemonStats = getOpponentPokemonCurrentTurn().getStats();

        Stat choseStat = gameState.getChosenStat();

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
        if (ownPoints > opponentPoints) {
            return ScoreResult.WINNER;
        }
        else if (ownPoints < opponentPoints) {
            return ScoreResult.LOSER;
        }
        else {
            return ScoreResult.TIE;
        }
    }

    public int getOwnPoints() {
        return ownPoints;
    }

    public int getOpponentPoints() {
        return opponentPoints;
    }
}
