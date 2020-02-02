package de.sowrong.pokemonquartet.data;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.Random;

import de.sowrong.pokemonquartet.game.MultiPlayerGame;

public class DatabaseConnector implements Serializable {
    final static String DB_CONNECTOR_TAG = "DatabaseConnector";
    final static int MIN_ROOM_ID = 1000;
    final static int MAX_ROOM_ID = 9999;
    final static int MAX_ROOM_CREATE_TRIES = 10;

    private static GameState gameState;

    private int roomNumber;
    private static DatabaseReference databaseReference;
    private static ValueEventListener eventListener;
    private static MultiPlayerGame multiPlayerGame;
    private Random generator;

    public DatabaseConnector(Context context, final MultiPlayerGame initialMultiPlayerGame) {
        FirebaseApp.initializeApp(context);
        multiPlayerGame = initialMultiPlayerGame;
        generator = new Random();

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    GameState gameState = DatabaseConnector.getGameState();

                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        Object value = childSnapshot.getValue();

                        switch (childSnapshot.getKey()) {
                            case GameState.TurnNumberString:
                                gameState.setTurnNumber(((Long)value).intValue());
                                break;
                            case GameState.NewStatChosenString:
                                gameState.setNewStatChosen((Boolean) value);
                                if ((Boolean) value) {
                                    multiPlayerGame.opponentHasChoseStat();
                                }
                                break;
                            case GameState.GuestConnectedString:
                                gameState.setGuestConnected((Boolean) value);
                                if ((Boolean) value) {
                                    multiPlayerGame.playerHasJoinedGame();
                                }
                                break;
                            case GameState.ChosenStatString:
                                String statString = (String) value;
                                gameState.setChosenStat(Pokemon.getStatByString(statString));
                                break;
                        }
                    }
                }

                Log.d(DB_CONNECTOR_TAG, "gameState values updated");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(DB_CONNECTOR_TAG, "Failed to read value.", error.toException());
            }
        };
    }

    public void setValue(String key, boolean value) {
        databaseReference.child(key).setValue(value);
    }

    public void setValue(String key, int value) {
        databaseReference.child(key).setValue((Integer.valueOf(value)));
    }

    public void setValue(String key, String value) {
        databaseReference.child(key).setValue(value);
    }

    public void setValue(String key, Stat value) {
        databaseReference.child(key).setValue(Pokemon.getStringByStat(value));
    }

    public void setDatabaseByRoomId(final int id) {
        roomNumber = id;

        databaseReference = FirebaseDatabase.getInstance().getReference().child(String.valueOf(roomNumber));
        databaseReference.addValueEventListener(eventListener);
    }

    public void checkRoomExists(final MultiPlayerGame multiPlayerGame, final int roomId, final Context context, final Intent intent, final Toast toastNotExist, final Toast toastFull) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(String.valueOf(roomId));

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                multiPlayerGame.callbackFinished();

                if (dataSnapshot.getValue() == null) {
                    toastNotExist.show();
                }
                else {
                    int numberCards = MultiPlayerGame.MIN_CARDS;
                    GameState gameState = DatabaseConnector.getGameState();

                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        Object value = childSnapshot.getValue();

                        switch (childSnapshot.getKey()) {
                            case GameState.TurnNumberString:
                                gameState.setTurnNumber(((Long)value).intValue());
                                break;
                            case GameState.GameRandomSeedString:
                                gameState.setRandomSeed(((Long)value).intValue());
                                break;
                            case GameState.NewStatChosenString:
                                gameState.setNewStatChosen((Boolean) value);
                                break;
                            case GameState.GuestConnectedString:
                                gameState.setGuestConnected((Boolean) value);
                                break;
                            case GameState.ChosenStatString:
                                String statString = (String) value;
                                gameState.setChosenStat(Pokemon.getStatByString(statString));
                                break;
                            case GameState.NumberCardsString:
                                numberCards = ((Long)value).intValue();
                                break;
                        }
                    }

                    if (gameState.isGuestConnected()) {
                        toastFull.show();
                        return;
                    }

                    multiPlayerGame.joinGame(roomId, numberCards);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createDatabase(final MultiPlayerGame multiPlayerGame, final Context context, final Intent intent, final Toast toast, final int numberCards, final int tries) {
        if (tries > MAX_ROOM_CREATE_TRIES) {
            toast.show();
            return;
        }

        roomNumber = generator.nextInt(MAX_ROOM_ID-MIN_ROOM_ID)+MIN_ROOM_ID;
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(String.valueOf(roomNumber));

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    GameState gameState = DatabaseConnector.getGameState();
                    int randomSeed = generator.nextInt();
                    gameState.setRandomSeed(randomSeed);

                    databaseReference = FirebaseDatabase.getInstance().getReference().child(String.valueOf(roomNumber));
                    setValue(GameState.TurnNumberString, 1);
                    setValue(GameState.GameRandomSeedString, randomSeed);
                    setValue(GameState.NewStatChosenString, false);
                    setValue(GameState.GuestConnectedString, false);
                    setValue(GameState.NumberCardsString, numberCards);
                    setValue(GameState.ChosenStatString, "");

                    databaseReference.addValueEventListener(eventListener);

                    context.startActivity(intent);
                    multiPlayerGame.callbackFinished();
                }
                else {
                    createDatabase(multiPlayerGame, context, intent, toast, numberCards, tries+1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void createDatabase(final MultiPlayerGame multiPlayerGame, final Context context, final Intent intent, final Toast toast, final int numberCards) {
        createDatabase(multiPlayerGame, context, intent, toast, numberCards, 0);
    }

    public void deleteDatabase() {
        databaseReference.removeEventListener(eventListener);
        databaseReference.setValue(null);
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public static MultiPlayerGame getMultiPlayerGame() {
        return multiPlayerGame;
    }

    public boolean isNewStatChosen() {
        return gameState.isNewStatChosen();
    }

    public void setChosenStat(Stat stat) {
        gameState.setChosenStat(stat);
        setValue(GameState.ChosenStatString, stat);
    }

    public Stat getChosenStat() {
        return gameState.getChosenStat();
    }

    public static GameState getGameState() {
        if (gameState == null) {
            gameState = new GameState();
        }

        return gameState;
    }
}
