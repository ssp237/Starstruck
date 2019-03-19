package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonReader;
import java.util.Iterator;

/**
 * Reads JSON files that describe levels and provides all relevant assets
 */
public class LevelReader {

    private JsonReader reader;
    public JsonValue json;

    public LevelReader(String file) {
        reader = new JsonReader();
        JsonValue json = reader.parse(file);
        
    }
}
