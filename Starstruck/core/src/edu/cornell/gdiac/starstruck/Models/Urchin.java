package edu.cornell.gdiac.starstruck.Models;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.Star;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class Urchin extends Enemy{


    /** Prefix for name of texture */
    private String TEXTURE_PREFIX = "spike ";
    /** Counter for names */
    private static int urchin_count = 1;


    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * drawing to work properly, you MUST set the drawScale. The drawScaled
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center
     * @param width		The object width in physics units
     * @param height	The object width in physics units
     */
    public Urchin(float x, float y, float width, float height) {
        super(x,y,width,height);
        //right_bound = right_b;
        setName("urchin" + urchin_count);
        urchin_count++;
    }

    /**
     * Creates a new dude avatar at the given position.
     *
     * The size is expressed in physics units NOT pixels.  In order for
     * drawing to work properly, you MUST set the drawScale. The drawScale
     * drawing to work properly, you MUST set the drawScale. The drawScaled
     * converts the physics units to pixels.
     *
     * @param x  		Initial x position of the avatar center
     * @param y  		Initial y position of the avatar center

     */
    public Urchin(float x, float y, TextureRegion texture, Vector2 scale) {
        this(x,y,texture.getRegionWidth()/scale.x,texture.getRegionHeight()/scale.y);
        setTexture(texture);
        setDrawScale(scale);

    }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static Urchin fromJSON(JsonValue json, Vector2 scale) {
        String key = json.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        return new Urchin(json.get("x").asFloat(), json.get("y").asFloat(), texture, scale);
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Add textures
        json.addChild("texture", new JsonValue(JsonAssetManager.getInstance().getKey(getTexture())));

        //Write position
        Vector2 pos = getPosition();

        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //Write velocity
        json.addChild("velocity", new JsonValue(getVX()));

        //System.out.println(json);

        return json;
    }

    public void update(float dt) {
        super.update(dt);
        //System.out.println(getVX());

    }

    public void draw(GameCanvas canvas) {
        canvas.draw(texture, Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1,1.0f);
    }

    public ObstacleType getType() { return ObstacleType.WORM;}


    public String toString(){
        String out = "Urchin with { texture: ";
        out += JsonAssetManager.getInstance().getKey(texture) + "}";
        return out;
    }

}


