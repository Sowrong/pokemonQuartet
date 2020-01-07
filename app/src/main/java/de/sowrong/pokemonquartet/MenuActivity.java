package de.sowrong.pokemonquartet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.Random;

import de.sowrong.pokemonquartet.data.Importer;
import de.sowrong.pokemonquartet.data.Pokemon;
import de.sowrong.pokemonquartet.data.Stats;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startHelp(View view) {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    public void startSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void startNewGame(View view) {
        Intent intent = new Intent(this, NewGameActivity.class);
        startActivity(intent);
    }

    public void quitGame(View view) {
        finish();
        System.exit(0);
    }

    public void displayRandomPokemon(View view) {
        Pokemon pokemon[] = Importer.getPokemon(getAssets());
        Log.v("ReadJSON", pokemon.length + " pokemon created");

        Pokemon randomPokemon =  pokemon[new Random().nextInt(pokemon.length)];
        Log.v("ExamplePokemon", randomPokemon.toString());

        ImageView imageView = (ImageView)findViewById(R.id.imageViewPokemon);
        String uri = "android.resource://de.sowrong.pokemonquartet/drawable/"+randomPokemon.getFilename();
        imageView.setImageURI(Uri.parse(uri));
    }
}
