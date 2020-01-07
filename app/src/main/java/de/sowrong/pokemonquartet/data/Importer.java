package de.sowrong.pokemonquartet.data;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Importer {
    final static String jsonFilename = "pokemon.json";

    // Serialize/deserialize helpers
    private static Pokemon[] pokemon;
    private static ObjectReader reader;
    private static ObjectWriter writer;


    public static Pokemon[] getPokemon(AssetManager assetManager) {
        if (pokemon == null) {
            fromJsonFilePath(assetManager);
        }

        return pokemon;
    }

    private static void fromJsonFilePath(AssetManager assetManager) {
        InputStream is = null;

        try {
            is = assetManager.open(jsonFilename);
            Log.d("files", Arrays.toString(assetManager.list("/")));
            pokemon = getObjectReader().readValue(is);
        } catch (IOException e) {
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static Pokemon[] fromJsonString(String json) throws IOException {

        return getObjectReader().readValue(json);
    }

    private static String toJsonString(Pokemon[] obj) throws JsonProcessingException {
        return getObjectWriter().writeValueAsString(obj);
    }

    private static void instantiateMapper() {
        ObjectMapper mapper = new ObjectMapper();
        reader = mapper.reader(Pokemon[].class);
        writer = mapper.writerFor(Pokemon[].class);
    }

    private static ObjectReader getObjectReader() {
        if (reader == null) instantiateMapper();
        return reader;
    }

    private static ObjectWriter getObjectWriter() {
        if (writer == null) instantiateMapper();
        return writer;
    }
}