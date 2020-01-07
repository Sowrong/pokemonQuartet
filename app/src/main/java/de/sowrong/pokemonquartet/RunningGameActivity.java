package de.sowrong.pokemonquartet;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import de.sowrong.pokemonquartet.data.DatabaseConnector;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.data.Stats;
import de.sowrong.pokemonquartet.game.Game;

public class RunningGameActivity extends AppCompatActivity {
    private View viewChooseCompareStat, viewComparePokemon, viewShowPokemonInfo;
    private Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewChooseCompareStat = getLayoutInflater().inflate(R.layout.layout_choose_compare_stat, null);
        viewComparePokemon = getLayoutInflater().inflate(R.layout.layout_compare_pokemon, null);
        viewShowPokemonInfo = getLayoutInflater().inflate(R.layout.layout_show_pokemon_info, null);

        setContentView(viewChooseCompareStat);

        //game = (Game)getIntent().getSerializableExtra("Game");
        game = DatabaseConnector.getGame();
        game.initRunningGame(this);

        game.initPokemonByRoomId();
        displayPokemonByTurn();
        updateViewNumberTurns();
        updateRoomNumber();
    }

    public void showComparePokemon(Pokemon pokemonPlayer, Pokemon pokemonOpponent, Stat compareStat) {
        setContentView(viewComparePokemon);

        game.updateActivePlayerAndPoints();

        if(game.winnerOfRound()) {
            updatePointsAndRound("You Win!");
        }
        else {
            updatePointsAndRound("You Lose!");
        }

        TextView textViewPokemonName1 = findViewById(R.id.textViewPokemonName1);
        TextView textViewPokemonInfoCaption1 = findViewById(R.id.textViewPokemonInfoCaption1);
        ImageView imageViewPokemon1 = findViewById(R.id.imageViewPokemon1);

        String imageUri1 = "android.resource://de.sowrong.pokemonquartet/drawable/" + pokemonOpponent.getFilename();
        imageViewPokemon1.setImageURI(Uri.parse(imageUri1));
        textViewPokemonName1.setText(String.format("#%s  %s", pokemonOpponent.getID(), pokemonOpponent.getName()));
        textViewPokemonInfoCaption1.setText(Pokemon.getStringByStat(compareStat) + ": " + pokemonOpponent.getStatValue(compareStat));


        TextView textViewPokemonName2 = findViewById(R.id.textViewPokemonName2);
        TextView textViewPokemonInfoCaption2 = findViewById(R.id.textViewPokemonInfoCaption2);
        ImageView imageViewPokemon2 = findViewById(R.id.imageViewPokemon2);

        String imageUri2 = "android.resource://de.sowrong.pokemonquartet/drawable/" + pokemonPlayer.getFilename();
        imageViewPokemon2.setImageURI(Uri.parse(imageUri2));
        textViewPokemonName2.setText(String.format("#%s  %s", pokemonPlayer.getID(), pokemonPlayer.getName()));
        textViewPokemonInfoCaption2.setText(Pokemon.getStringByStat(compareStat) + ": " + pokemonPlayer.getStatValue(compareStat));


    }

    private void displayPokemonByTurn() {
        Pokemon pokemon = game.getOwnPokemonCurrentTurn();

        if (game.myTurn()) {
            setContentView(viewChooseCompareStat);
            updateViewChooseCompareStat(pokemon);
        }
        else {
            setContentView(viewShowPokemonInfo);
            updateViewShowPokemonInfo(pokemon);
        }
    }

    private void updateViewNumberTurns() {
        TextView textViewNumberTurns = findViewById(R.id.textViewRounds);
        textViewNumberTurns.setText(game.getCurrentRound() + "/" + game.getNumberRounds());
    }

    private void updateRoomNumber() {
        if(game.isHost()) {
            TextView textViewNumberTurns = findViewById(R.id.textViewGameInfo);
            textViewNumberTurns.setText("Room Number: " + game.getGameState().getRoomNumber());
        }
    }

    public void updatePlayerHasJoinedGame() {
        TextView textViewNumberTurns = findViewById(R.id.textViewGameInfo);

        if(game.myTurn()) {
            textViewNumberTurns.setText("Player joined the game.");
        }
        else {
            textViewNumberTurns.setText("Opponent is choosing...");
        }
    }

    public void selectStat(View view) {
        Stat selectedStat = Stat.ATTACK;

        switch(view.getId()) {
            case R.id.buttonAttack:
                selectedStat = Stat.ATTACK;
                break;
            case R.id.buttonDefense:
                selectedStat = Stat.DEFENSE;
                break;
            case R.id.buttonSpAttack:
                selectedStat = Stat.SPATK;
                break;
            case R.id.buttonSpDefense:
                selectedStat = Stat.SPDEF;
                break;
            case R.id.buttonHp:
                selectedStat = Stat.HP;
                break;
            case R.id.buttonSpeed:
                selectedStat = Stat.SPEED;
                break;
        }

        if (game.takeTurn(selectedStat, this)) {
            showComparePokemon(game.getOwnPokemonCurrentTurn(), game.getOpponentPokemonCurrentTurn(), selectedStat);
        }
    }

    public void nextRound(View view) {
        game.confirmStartRound();

        if (game.myTurn()) {
            setContentView(viewChooseCompareStat);
            updateViewChooseCompareStat(game.getOwnPokemonCurrentTurn());
        }
        else {
            setContentView(viewShowPokemonInfo);
            updateViewShowPokemonInfo(game.getOwnPokemonCurrentTurn());
        }
    }

    private void updatePointsAndRound(String infoText) {
        TextView textViewRoundName = findViewById(R.id.textViewInfoWinLoose);
        TextView textViewRoundNumber = findViewById(R.id.textViewRounds);

        TextView textViewOwnPoints = findViewById(R.id.textViewYourPoints);
        TextView textViewOpponentPoints = findViewById(R.id.textViewOpponentsPoints);

        textViewRoundName.setText(infoText);
        textViewRoundNumber.setText(String.format("%d/%d", game.getCurrentRound(), game.getNumberRounds()));
        textViewOwnPoints.setText(String.valueOf(game.getOwnPoints()));
        textViewOpponentPoints.setText(String.valueOf(game.getOpponentPoints()));
    }

    private void updateViewShowPokemonInfo(Pokemon pokemon) {
        updatePointsAndRound("Round");
        CardView cardViewGameInfo = findViewById(R.id.cardViewGameInfo);
        cardViewGameInfo.setVisibility(View.VISIBLE);

        Button buttonShowComparePokemon = findViewById(R.id.buttonShowComparePokemon);
        buttonShowComparePokemon.setVisibility(View.INVISIBLE);

        Stats stats = pokemon.getStats();
        TextView textViewPokemonInfoDetails = findViewById(R.id.textViewPokemonInfoDetails);
        textViewPokemonInfoDetails.setText(
                String.format("%s\n%s\n%s\n%s\n%s\n%s", stats.getAttack(), stats.getDefense(), stats.getSPAtk(),
                        stats.getSPDef(), stats.getHP(), stats.getSpeed()));

        TextView textViewPokemonName = findViewById(R.id.textViewPokemonName);
        textViewPokemonName.setText(String.format("#%s  %s", pokemon.getID(), pokemon.getName()));

        ImageView imageViewPokemon = findViewById(R.id.imageViewPokemon);
        String imageUri = "android.resource://de.sowrong.pokemonquartet/drawable/" + pokemon.getFilename();
        imageViewPokemon.setImageURI(Uri.parse(imageUri));
    }

    private void updateViewChooseCompareStat(Pokemon pokemon) {
        updatePointsAndRound("Round");
        Stats pokemonStats = pokemon.getStats();

        TextView textViewPokemonName = findViewById(R.id.textViewPokemonName);
        ImageView imageViewPokemon = findViewById(R.id.imageViewPokemon);
        Button buttonHp = findViewById(R.id.buttonHp);
        Button buttonAttack = findViewById(R.id.buttonAttack);
        Button buttonDefense = findViewById(R.id.buttonDefense);
        Button buttonSpAttack = findViewById(R.id.buttonSpAttack);
        Button buttonSpDefense = findViewById(R.id.buttonSpDefense);
        Button buttonSpeed  = findViewById(R.id.buttonSpeed);

        textViewPokemonName.setText(String.format("#%s  %s", pokemon.getID(), pokemon.getName()));

        String imageUri = "android.resource://de.sowrong.pokemonquartet/drawable/" + pokemon.getFilename();
        imageViewPokemon.setImageURI(Uri.parse(imageUri));

        buttonHp.setText("HP ↑\n" + pokemonStats.getHP());
        buttonAttack.setText("Attack ↑\n" + pokemonStats.getAttack());
        buttonDefense.setText("Defense ↑\n" + pokemonStats.getDefense());
        buttonSpAttack.setText("Sp. Attack ↑\n" + pokemonStats.getSPAtk());
        buttonSpDefense.setText("Sp. Defense ↑\n" + pokemonStats.getSPDef());
        buttonSpeed.setText("Speed ↑\n" + pokemonStats.getSpeed());

        TextView textViewNumberTurns = findViewById(R.id.textViewGameInfo);
        //textViewNumberTurns.setText("Waiting for opponent to be ready...");
        textViewNumberTurns.setText("Choose a stat!");
    }

    public void opponentHasChoseStat() {
        // disable infobox, enable button
        CardView cardViewGameInfo = findViewById(R.id.cardViewGameInfo);
        Button buttonShowComparePokemon = findViewById(R.id.buttonShowComparePokemon);

        if(cardViewGameInfo != null && buttonShowComparePokemon != null) {
            cardViewGameInfo.setVisibility(View.INVISIBLE);
            buttonShowComparePokemon.setVisibility(View.VISIBLE);
        }
    }

    public void showResultsForInactivePlayer(View view) {
        showComparePokemon(game.getOwnPokemonCurrentTurn(), game.getOpponentPokemonCurrentTurn(), game.getGameState().getChosenStat());
    }
}
