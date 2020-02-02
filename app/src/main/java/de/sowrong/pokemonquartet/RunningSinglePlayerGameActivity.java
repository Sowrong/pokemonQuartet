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
import de.sowrong.pokemonquartet.game.ScoreResult;
import de.sowrong.pokemonquartet.game.SinglePlayerGame;

public class RunningSinglePlayerGameActivity extends AppCompatActivity {
    private View viewChooseCompareStat, viewComparePokemon, viewShowPokemonInfo, viewGameEnd;
    private SinglePlayerGame singlePlayerGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewChooseCompareStat = getLayoutInflater().inflate(R.layout.layout_choose_compare_stat, null);
        viewComparePokemon = getLayoutInflater().inflate(R.layout.layout_compare_pokemon, null);
        viewShowPokemonInfo = getLayoutInflater().inflate(R.layout.layout_show_pokemon_info, null);
        viewGameEnd = getLayoutInflater().inflate(R.layout.layout_game_end, null);

        setContentView(viewChooseCompareStat);

        singlePlayerGame = SinglePlayerGame.getSinglePlayerGame();

        displayPokemonByTurn();
        updateViewNumberTurns();

        if (singlePlayerGame.myTurn()) {
            displayChooseStatMessage();
        }
        else {
            opponentHasChoseStat();
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        SinglePlayerGame.store();
    }

    private void displayChooseStatMessage() {
        TextView textViewNumberTurns = findViewById(R.id.textViewGameInfo);
        textViewNumberTurns.setText("Choose a stat!");
    }

    public void showComparePokemon() {
        Pokemon pokemonPlayer = singlePlayerGame.getPlayerPokemonCurrentTurn();
        Pokemon pokemonOpponent = singlePlayerGame.getAiPokemonCurrentTurn();
        Stat compareStat = singlePlayerGame.getChosenStat();

        setContentView(viewComparePokemon);

        singlePlayerGame.updateActivePlayerAndPoints(); // finalize one round

        if(singlePlayerGame.winnerOfRound() == ScoreResult.WINNER) {
            updatePointsAndRound("You Win!");
        }
        else if (singlePlayerGame.winnerOfRound() == ScoreResult.LOSER){
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

        if(singlePlayerGame.isLastRound()) {
            Button nextButton = findViewById(R.id.buttonNextRound);
            nextButton.setText("Final Score");
        }

        singlePlayerGame.increaseRoundCounter();
    }

    private void displayPokemonByTurn() {
        Pokemon pokemon = singlePlayerGame.getPlayerPokemonCurrentTurn();

        if (singlePlayerGame.myTurn()) {
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
        textViewNumberTurns.setText(singlePlayerGame.getCurrentRound() + "/" + singlePlayerGame.getNumberRounds());
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

        singlePlayerGame.takePlayerTurn(selectedStat);
        showComparePokemon();
    }

    public void nextRound(View view) {
        if (singlePlayerGame.hasEnded()) {
            setContentView(viewGameEnd);
            updateViewEndGame();
            return;
        }

        if (singlePlayerGame.myTurn()) {
            setContentView(viewChooseCompareStat);
            updateViewChooseCompareStat(singlePlayerGame.getPlayerPokemonCurrentTurn());
        }
        else {
            setContentView(viewShowPokemonInfo);
            updateViewShowPokemonInfo(singlePlayerGame.getPlayerPokemonCurrentTurn());
            //singlePlayerGame.takeAiTurn(textView, button);
            singlePlayerGame.takeAiTurn(this);
        }
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

    private void updateViewEndGame() {
        TextView textViewOwnPoints = findViewById(R.id.textViewYourPoints);
        TextView textViewOpponentPoints = findViewById(R.id.textViewOpponentsPoints);

        TextView textViewCongratulations = findViewById(R.id.textViewCongratulations);
        TextView textViewOwnWinLose = findViewById(R.id.textViewWinLose);

        ImageView imageViewWinLosePokemon = findViewById(R.id.imageViewPokemon);

        textViewOwnPoints.setText(singlePlayerGame.getPlayerPoints() + " Points");
        textViewOpponentPoints.setText(singlePlayerGame.getAiPoints() + " Points");
        String imageUri;

        switch(singlePlayerGame.gameResult()) {
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
        textViewRoundNumber.setText(String.format("%d/%d", singlePlayerGame.getCurrentRound(), singlePlayerGame.getNumberRounds()));
        textViewOwnPoints.setText(String.valueOf(singlePlayerGame.getPlayerPoints()));
        textViewOpponentPoints.setText(String.valueOf(singlePlayerGame.getAiPoints()));
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
        textViewNumberTurns.setText("Choose a stat!");
    }

    public void showResultsForInactivePlayer(View view) {
        showComparePokemon();
    }

    public void back(View view) {
        finish();
    }
}
