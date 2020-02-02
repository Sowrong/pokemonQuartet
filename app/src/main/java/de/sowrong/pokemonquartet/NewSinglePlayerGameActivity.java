package de.sowrong.pokemonquartet;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import de.sowrong.pokemonquartet.ai.Difficulty;
import de.sowrong.pokemonquartet.game.SinglePlayerGame;

public class NewSinglePlayerGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_singleplayer_game);

        Spinner spinnerAiDifficulty = findViewById(R.id.spinnerAiDifficulty);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ai_difficulty_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerAiDifficulty.setAdapter(adapter);

        final SeekBar seekBar = findViewById(R.id.seekBarNumberCards);

        Button continueButton = findViewById(R.id.buttonContinueGame);

        SinglePlayerGame.init(this);

        if (SinglePlayerGame.restore() && !SinglePlayerGame.getSinglePlayerGame().hasEnded()) {
            continueButton.setVisibility(View.VISIBLE);
        }
        else {
            continueButton.setVisibility(View.INVISIBLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(SinglePlayerGame.MIN_CARDS);
        }
        seekBar.setMax(SinglePlayerGame.MAX_CARDS);

        final TextView textViewNumberCards = findViewById(R.id.textViewNumberCards);
        textViewNumberCards.setText(seekBar.getProgress() + " Cards");

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewNumberCards.setText(progress + " Cards");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        Button continueButton = findViewById(R.id.buttonContinueGame);

        if (SinglePlayerGame.restore() && !SinglePlayerGame.getSinglePlayerGame().hasEnded()) {
            continueButton.setVisibility(View.VISIBLE);
        }
        else {
            continueButton.setVisibility(View.INVISIBLE);
        }
    }

    public void continueGame(View view) {
        Intent intent = new Intent(this, RunningSinglePlayerGameActivity.class);
        startActivity(intent);
    }

    public void newGame(View view) {
        SeekBar seekBar = findViewById(R.id.seekBarNumberCards);
        int numberCards = seekBar.getProgress();

        if (numberCards < SinglePlayerGame.MIN_CARDS) {
            Toast toast = Toast.makeText(this, "The minimum amount of cards is " + SinglePlayerGame.MIN_CARDS, Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Spinner difficultySpinner = findViewById(R.id.spinnerAiDifficulty);
        int itemId = (int)difficultySpinner.getSelectedItemId();

        Difficulty difficulty;

        switch (itemId) {
            case 1:
                difficulty = Difficulty.MEDIUM;
                break;
            case 2:
                difficulty = Difficulty.HARD;
                break;
            default:
                difficulty = Difficulty.EASY;
                break;
        }

        SinglePlayerGame.createNewSinglePlayerGame(difficulty, numberCards, this);

        Intent intent = new Intent(this, RunningSinglePlayerGameActivity.class);
        startActivity(intent);
    }

    public void back(View view) {
        finish();
    }
}
