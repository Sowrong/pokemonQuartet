package de.sowrong.pokemonquartet;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.data.Stats;
import de.sowrong.pokemonquartet.game.MultiPlayerGame;
import de.sowrong.pokemonquartet.game.ScoreResult;

public class RunningMultiPlayerGameActivity extends AppCompatActivity {
    private View viewChooseCompareStat, viewComparePokemon, viewShowPokemonInfo, viewGameEnd;
    private MultiPlayerGame multiPlayerGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewChooseCompareStat = getLayoutInflater().inflate(R.layout.layout_choose_compare_stat, null);
        viewComparePokemon = getLayoutInflater().inflate(R.layout.layout_compare_pokemon, null);
        viewShowPokemonInfo = getLayoutInflater().inflate(R.layout.layout_show_pokemon_info, null);
        viewGameEnd = getLayoutInflater().inflate(R.layout.layout_game_end, null);

        setContentView(viewChooseCompareStat);

        //multiPlayerGame = (MultiPlayerGame)getIntent().getSerializableExtra("MultiPlayerGame");
        multiPlayerGame = MultiPlayerGame.getMultiPlayerGame();
        multiPlayerGame.initRunningGame(this);

        multiPlayerGame.initPokemonByRandomSeedId();
        displayPokemonByTurn();
        updateViewNumberTurns();
        updateRoomNumber();
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        multiPlayerGame.endGame();
    }

    public void showComparePokemon(Pokemon pokemonPlayer, Pokemon pokemonOpponent, Stat compareStat) {
        setContentView(viewComparePokemon);

        multiPlayerGame.updateActivePlayerAndPoints();

        if(multiPlayerGame.winnerOfRound() == ScoreResult.WINNER) {
            updatePointsAndRound("You Win!");
        }
        else if (multiPlayerGame.winnerOfRound() == ScoreResult.LOSER){
            updatePointsAndRound("You Lose!");
        }
        else {
            updatePointsAndRound("It's a tie!");
        }

        TextView textViewPokemonName1 = findViewById(R.id.textViewPokemonName1);
        TextView textViewPokemonInfoCaption1 = findViewById(R.id.textViewPokemonInfoCaption1);
        ImageView imageViewPokemon1 = findViewById(R.id.imageViewPokemon);

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

        if(multiPlayerGame.isLastRound()) {
            Button nextButton = findViewById(R.id.buttonNextRound);
            nextButton.setText("Final Score");
        }
    }

    private void displayPokemonByTurn() {
        Pokemon pokemon = multiPlayerGame.getOwnPokemonCurrentTurn();

        if (multiPlayerGame.myTurn()) {
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
        textViewNumberTurns.setText(multiPlayerGame.getCurrentRound() + "/" + multiPlayerGame.getNumberRounds());
    }

    private void updateRoomNumber() {
        if(multiPlayerGame.isHost()) {
            TextView textViewNumberTurns = findViewById(R.id.textViewGameInfo);
            textViewNumberTurns.setText("Room Number: " + multiPlayerGame.getRoomNumber());
        }
    }

    public void updatePlayerHasJoinedGame() {
        TextView textViewNumberTurns = findViewById(R.id.textViewGameInfo);

        if(multiPlayerGame.isHost()) {
            textViewNumberTurns.setText("Player joined the Game.");
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

        if (multiPlayerGame.takeTurn(selectedStat, this)) {
            showComparePokemon(multiPlayerGame.getOwnPokemonCurrentTurn(), multiPlayerGame.getOpponentPokemonCurrentTurn(), selectedStat);
        }
    }

    public void nextRound(View view) {
        if (multiPlayerGame.isLastRound()) {
            setContentView(viewGameEnd);
            updateViewEndGame();
            return;
        }

        multiPlayerGame.confirmStartRound();

        if (multiPlayerGame.myTurn()) {
            setContentView(viewChooseCompareStat);
            updateViewChooseCompareStat(multiPlayerGame.getOwnPokemonCurrentTurn());
        }
        else {
            setContentView(viewShowPokemonInfo);
            updateViewShowPokemonInfo(multiPlayerGame.getOwnPokemonCurrentTurn());
        }
    }

    private void updateViewEndGame() {
        TextView textViewOwnPoints = findViewById(R.id.textViewYourPoints);
        TextView textViewOpponentPoints = findViewById(R.id.textViewOpponentsPoints);

        TextView textViewCongratulations = findViewById(R.id.textViewCongratulations);
        TextView textViewOwnWinLose = findViewById(R.id.textViewWinLose);

        ImageView imageViewWinLosePokemon = findViewById(R.id.imageViewPokemon);

        textViewOwnPoints.setText(multiPlayerGame.getOwnPoints() + " Points");
        textViewOpponentPoints.setText(multiPlayerGame.getOpponentPoints() + " Points");
        String imageUri;

        switch(multiPlayerGame.gameResult()) {
            case WINNER:
                textViewCongratulations.setText("Congratulations!");
                textViewOwnWinLose.setText("You Won");

                imageUri = "android.resource://de.sowrong.pokemonquartet/drawable/game_won";
                imageViewWinLosePokemon.setImageURI(Uri.parse(imageUri));
                break;

            case LOSER:
                textViewCongratulations.setText("Oh No!");
                textViewOwnWinLose.setText("You Lost");

                imageUri = "android.resource://de.sowrong.pokemonquartet/drawable/game_lost";
                imageViewWinLosePokemon.setImageURI(Uri.parse(imageUri));
                break;

            default:
                textViewCongratulations.setText("It's a Tie!");
                textViewOwnWinLose.setText("You Both Won");

                imageUri = "android.resource://de.sowrong.pokemonquartet/drawable/game_tied";
                imageViewWinLosePokemon.setImageURI(Uri.parse(imageUri));
                break;
        }
    }

    private void updatePointsAndRound(String infoText) {
        TextView textViewRoundName = findViewById(R.id.textViewInfoWinLoose);
        TextView textViewRoundNumber = findViewById(R.id.textViewRounds);

        TextView textViewOwnPoints = findViewById(R.id.textViewYourPoints);
        TextView textViewOpponentPoints = findViewById(R.id.textViewOpponentsPoints);

        textViewRoundName.setText(infoText);
        textViewRoundNumber.setText(String.format("%d/%d", multiPlayerGame.getCurrentRound(), multiPlayerGame.getNumberRounds()));
        textViewOwnPoints.setText(String.valueOf(multiPlayerGame.getOwnPoints()));
        textViewOpponentPoints.setText(String.valueOf(multiPlayerGame.getOpponentPoints()));
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
        showComparePokemon(multiPlayerGame.getOwnPokemonCurrentTurn(), multiPlayerGame.getOpponentPokemonCurrentTurn(), multiPlayerGame.getChosenStat());
    }

    public void back(View view) {
        finish();
    }
}
