package de.sowrong.pokemonquartet;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.Serializable;
import java.util.Random;

import de.sowrong.pokemonquartet.data.Importer;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stat;
import de.sowrong.pokemonquartet.game.Game;

public class NewGameActivity extends AppCompatActivity {
    private Game game;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_game);

        final EditText editText = findViewById(R.id.textViewRoomIdInput);
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getLineCount() > 1) {
                    editText.setText(editText.getText().toString().replaceAll("[^0-9]+", ""));
                    newGame(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final SeekBar seekBar = findViewById(R.id.seekBarNumberCards);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekBar.setMin(Game.MIN_CARDS);
        }
        seekBar.setMax(Game.MAX_CARDS);

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

    public void newGame(View view) {
        // see if host was selected, otherwise self is guest

        game = Game.getGame();
        game.initGame(this);


        if (game.isWaitingForCallback()) {
            return;
        }

        Intent intent = new Intent(this, RunningGameActivity.class);

        CheckBox checkboxHostGuest = findViewById(R.id.checkboxHostGuest);

        if (checkboxHostGuest.isChecked()) {
            // HOST
                // create room

            final SeekBar seekBar = findViewById(R.id.seekBarNumberCards);
            int numberCards = seekBar.getProgress();

            if (numberCards < Game.MIN_CARDS) {
                Toast toast = Toast.makeText(this, "The minimum amount of cards is " + Game.MIN_CARDS, Toast.LENGTH_LONG);
                toast.show();

                return;
            }

            Toast toast = Toast.makeText(this, "Could not create room!", Toast.LENGTH_LONG);
            game.startGame(this, intent, toast, numberCards);
        }
        else {
            // GUEST
            // read the room number if guest
            TextView textViewRoomIdInput = findViewById(R.id.textViewRoomIdInput);
            String roomNumberString = textViewRoomIdInput.getText().toString();

            if (roomNumberString.equals("")) {
                return;
            }

            int roomId = Integer.valueOf(roomNumberString);

            // check if room number exists

            Toast toast = Toast.makeText(this, "This room doesn't exist!", Toast.LENGTH_LONG);

            game.checkRoomExists(roomId, this, intent, toast);
        }
    }

    public void activateDeactivateInput(View view) {
        CheckBox checkboxHostGuest = findViewById(R.id.checkboxHostGuest);
        TextView textViewRoomIdInput = findViewById(R.id.textViewRoomIdInput);
        LinearLayout layoutChooseNumberCards = findViewById(R.id.layoutNumberCards);
        Button newGameButton = findViewById(R.id.buttonNewGame);

        if (checkboxHostGuest.isChecked()) {
            textViewRoomIdInput.setVisibility(View.INVISIBLE);
            layoutChooseNumberCards.setVisibility(View.VISIBLE);
            newGameButton.setText("New Game");
        }
        else {
            textViewRoomIdInput.setVisibility(View.VISIBLE);
            layoutChooseNumberCards.setVisibility(View.INVISIBLE);
            newGameButton.setText("Join Game");
        }
    }

    public void back(View view) {
        finish();
    }
}
