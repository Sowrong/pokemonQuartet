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

import de.sowrong.pokemonquartet.game.ConnectionType;
import de.sowrong.pokemonquartet.game.Game;

public class DatabaseConnector implements Serializable {
    final static String DB_CONNECTOR_TAG = "DatabaseConnector";
    final static int MIN_ROOM_ID = 10000;
    final static int MAX_ROOM_ID = 100000;
    final static int MAX_ROOM_CREATE_TRIES = 10;

    private int randomSeed;
    private static DatabaseReference databaseReference;
    private static ValueEventListener eventListener;
    private static Game game;
    private Random generator;

    public DatabaseConnector(Context context, final Game initialGame) {
        FirebaseApp.initializeApp(context);
        game = initialGame;
        generator = new Random();

        eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    GameState gameState = game.getGameState();

                    for(DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                        Object value = childSnapshot.getValue();

                        switch (childSnapshot.getKey()) {
                            case GameState.TurnNumberString:
                                gameState.setTurnNumber(((Long)value).intValue());
                                break;
                            case GameState.NewStatChosenString:
                                gameState.setNewStatChosen((Boolean) value);
                                if ((Boolean) value) {
                                    game.opponentHasChoseStat();
                                }
                                break;
                            case GameState.GuestConnectedString:
                                gameState.setGuestConnected((Boolean) value);
                                if ((Boolean) value) {
                                    game.playerHasJoinedGame();
                                }
                                break;
                            case GameState.StartPlayerString:
                                String startPlayerString = (String) value;
                                if (startPlayerString.equals(ConnectionType.HOST.toString())) {
                                    gameState.setStartPlayer(ConnectionType.HOST);
                                }
                                else if (startPlayerString.equals(ConnectionType.GUEST.toString())) {
                                    gameState.setStartPlayer(ConnectionType.GUEST);
                                }
                                else {
                                    gameState.setStartPlayer(ConnectionType.OBSERVER);
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
        randomSeed = id;

        databaseReference = FirebaseDatabase.getInstance().getReference().child(String.valueOf(randomSeed));
        databaseReference.addValueEventListener(eventListener);
    }

    public void pathExists(final Game game, final int roomId, final Context context, final Intent intent, final Toast toast) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(String.valueOf(roomId));

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    toast.show();
                }
                else {
                    int numberCards = Game.MIN_CARDS;
                    GameState gameState = game.getGameState();

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
                            case GameState.StartPlayerString:
                                String startPlayer = (String) value;
                                if (startPlayer.equals(ConnectionType.HOST.toString())) {
                                    gameState.setStartPlayer(ConnectionType.HOST);
                                }
                                else if (startPlayer.equals(ConnectionType.GUEST.toString())) {
                                    gameState.setStartPlayer(ConnectionType.GUEST);
                                }
                                else {
                                    gameState.setStartPlayer(ConnectionType.OBSERVER);
                                }
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

                    game.joinGame(roomId, numberCards);
                    context.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createDatabase(final Game game, final Context context, final Intent intent, final Toast toast, final int numberCards, final int tries) {
        if (tries > MAX_ROOM_CREATE_TRIES) {
            toast.show();
            return;
        }

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child(String.valueOf(randomSeed));
        randomSeed = generator.nextInt(MAX_ROOM_ID-MIN_ROOM_ID)+MIN_ROOM_ID;

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    GameState gameState = game.getGameState();
                    gameState.setRandomSeed(randomSeed);

                    databaseReference = FirebaseDatabase.getInstance().getReference().child(String.valueOf(randomSeed));
                    setValue(GameState.TurnNumberString, 1);
                    setValue(GameState.GameRandomSeedString, randomSeed);
                    setValue(GameState.NewStatChosenString, false);
                    setValue(GameState.GuestConnectedString, false);
                    setValue(GameState.NumberCardsString, numberCards);
                    setValue(GameState.StartPlayerString, gameState.getStartPlayer().toString());

                    databaseReference.addValueEventListener(eventListener);

                    context.startActivity(intent);
                    game.gameStarted();
                }
                else {
                    createDatabase(game, context, intent, toast, numberCards, tries+1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void createDatabase(final Game game, final Context context, final Intent intent, final Toast toast, final int numberCards) {
        createDatabase(game, context, intent, toast, numberCards, 0);
    }

    public void deleteDatabase() {
        databaseReference.removeEventListener(eventListener);
        databaseReference.setValue(null);
    }

    public int getRandomSeed() {
        return randomSeed;
    }

    public static Game getGame() {
        return game;
    }
}
