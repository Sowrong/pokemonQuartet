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

    public void startChooser(View view) {
        Intent intent = new Intent(this, RunningGameActivity.class);
        startActivity(intent);
    }

    public void startNewGame(View view) {
        Intent intent = new Intent(this, NewGameActivity.class);
        startActivity(intent);
    }

    public void displayRandomPokemon(View view) {
        Pokemon pokemon[] = Importer.getPokemon(getAssets());
        Log.v("ReadJSON", pokemon.length + " pokemon created");

        Pokemon randomPokemon =  pokemon[new Random().nextInt(pokemon.length)];
        Log.v("ExamplePokemon", randomPokemon.toString());

        ImageView imageView = (ImageView)findViewById(R.id.imageViewPokemon);
        String uri = "android.resource://de.sowrong.pokemonquartet/drawable/"+randomPokemon.getFilename();
        imageView.setImageURI(Uri.parse(uri));

        long maxAttack = 0;
        long maxDefense = 0;
        long maxSPAttack = 0;
        long maxSPDefense = 0;
        long maxHP = 0;
        long maxSpeed = 0;

        for (int i=0; i<pokemon.length; i++) {
            Stats pokemonStats = pokemon[i].getStats();
            if (pokemonStats.getAttack() > maxAttack) {
                maxAttack = pokemonStats.getAttack();
            }
            if (pokemonStats.getDefense() > maxDefense) {
                maxDefense = pokemonStats.getDefense();
            }
            if (pokemonStats.getSPAtk() > maxSPAttack) {
                maxSPAttack = pokemonStats.getSPAtk();
            }
            if (pokemonStats.getSPDef() > maxSPDefense) {
                maxSPDefense = pokemonStats.getSPDef();
            }
            if (pokemonStats.getHP() > maxHP) {
                maxHP = pokemonStats.getHP();
            }
            if (pokemonStats.getSpeed() > maxSpeed) {
                maxSpeed = pokemonStats.getSpeed();
            }
        }

        Log.d("PokemonInfo", String.format("maxStats: {%d, %d, %d, %d, %d, %d}" , maxAttack, maxDefense, maxSPAttack, maxSPDefense, maxHP, maxSpeed));
    }
}
