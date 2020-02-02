package de.sowrong.pokemonquartet.data;

import java.io.Serializable;

public class GameState implements Serializable {
    private int turnNumber;
    private int randomSeed;
    private boolean newStatChosen;
    private boolean guestConnected;
    private Stat chosenStat;

    public static final String TurnNumberString =  "TurnNumber";
    public static final String GameRandomSeedString = "GameRandomSeed";
    public static final String NewStatChosenString = "NewStatChosen";
    public static final String GuestConnectedString = "GuestConnected";
    public static final String NumberCardsString = "NumberCards";
    public static final String ChosenStatString = "ChosenStat";


    public GameState() {
        turnNumber = 1;
        randomSeed = 0;
        guestConnected = false;
        newStatChosen = false;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(int roomNumber) {
        this.randomSeed = roomNumber;
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
}
