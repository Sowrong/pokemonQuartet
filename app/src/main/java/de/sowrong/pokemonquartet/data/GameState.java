package de.sowrong.pokemonquartet.data;

import java.io.Serializable;
import java.util.Random;

import de.sowrong.pokemonquartet.game.ConnectionType;

public class GameState implements Serializable {
    private int turnNumber;
    private int roomNumber;
    private boolean newStatChosen;
    private boolean guestConnected;
    private Stat chosenStat;
    private ConnectionType startPlayer;

    public static final String TurnNumberString =  "TurnNumber";
    public static final String GameRandomSeedString = "GameRandomSeed";
    public static final String NewStatChosenString = "NewStatChosen";
    public static final String GuestConnectedString = "GuestConnected";
    public static final String NumberCardsString = "NumberCards";
    public static final String ChosenStatString = "ChosenStat";
    public static final String StartPlayerString = "StartPlayer";


    public GameState() {
        turnNumber = 1;
        roomNumber = 0;
        guestConnected = false;
        newStatChosen = false;

        Random startPlayerPicker = new Random();

        if (startPlayerPicker.nextBoolean()){
            startPlayer = ConnectionType.HOST;
        }
        else {
            startPlayer = ConnectionType.GUEST;
        }
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRandomSeed(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isNewStatChosen() {
        return newStatChosen;
    }

    public void setNewStatChosen(boolean newStatChosen) {
        this.newStatChosen = newStatChosen;
    }

    public boolean isGuestConnected() {
        return guestConnected;
    }

    public void setGuestConnected(boolean guestConnected) {
        this.guestConnected = guestConnected;
    }

    public Stat getChosenStat() {
        return chosenStat;
    }

    public void setChosenStat(Stat chosenStat) {
        this.chosenStat = chosenStat;
    }

    public ConnectionType getStartPlayer() {
        return startPlayer;
    }

    public void setStartPlayer(ConnectionType startPlayer) {
        this.startPlayer = startPlayer;
    }
}
