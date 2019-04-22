package edu.cornell.gdiac.starstruck.Models;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.Star;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.util.ArrayList;
import java.util.Arrays;

public class Urchin extends Enemy {



    /** Counter for names */
    private static int urchin_count = 1;
    /**Textures for drawing. In order, single - top - middle - bottom.*/
    private static TextureRegion[] textures;
    /** Number of body segments */
    private int length;


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
    public Urchin(float x, float y, float width, float height, int length) {
        super(x,y,width,height);
        this.length = length;
        //right_bound = right_b;

        setBodyType(BodyDef.BodyType.StaticBody);

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
    public Urchin(float x, float y, float width, float height, Vector2 scale, int length, Orientation orientation) {
        this(x,y,width, height, length);
        setDrawScale(scale);

    }

    /**
     * Set the array of textures to be all possible textures for this urchin based on the prefix
     * @param prefix The prefix for the names of all textures in the assets JSON.
     */
    public static void setTextures(String prefix) {
        textures = new TextureRegion[4];
        textures[0] = JsonAssetManager.getInstance().getEntry(prefix + " single", TextureRegion.class);
        textures[1] = JsonAssetManager.getInstance().getEntry(prefix + " top", TextureRegion.class);
        textures[2] = JsonAssetManager.getInstance().getEntry(prefix + " center", TextureRegion.class);
        textures[3] = JsonAssetManager.getInstance().getEntry(prefix + " bottom", TextureRegion.class);
    }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static Urchin fromJSON(JsonValue json, Vector2 scale) {
        String pre = json.get("texture prefix").asString();
        Orientation orientation = json.get("orientation").equals("vertical") ? Orientation.VERTICAL : Orientation.HORIZONTAL;

        if (textures == null) setTextures(pre);

        int length = json.get("length").asInt();

        float width = textures[0].getRegionWidth() / scale.x;
        float height = 0;
        if (length == 1 ) {
            height = textures[0].getRegionHeight() / scale.y;
        } else { // more than one chunk
            height = (textures[1].getRegionHeight() + textures[3].getRegionWidth()) /  scale.y;
            for (int i = 2; i < length; i++) {
                height += textures[2].getRegionHeight() / scale.y;
            }
        }
        if (orientation == Orientation.HORIZONTAL) {
            float temp = height;
            height = width;
            width = temp;
        }


        return new Urchin(json.get("x").asFloat(), json.get("y").asFloat(), width, height, scale, length, orientation);
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
        if (length == 1) {
            canvas.draw(textures[0], Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.y,getAngle(),1,1.0f);
        }
    }

    public ObstacleType getType() { return ObstacleType.URCHIN;}


    public String toString(){
        String out = "Urchin with { texture: ";
        out += JsonAssetManager.getInstance().getKey(texture) + "}";
        return out;
    }

}


