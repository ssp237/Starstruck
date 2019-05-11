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
    /** Prefix of all texture names */
    private static String pre;
    /**Textures for drawing. In order, single - top - middle - bottom.*/
    private static FilmStrip[] textures;
    /** Number of body segments */
    private int length;
    /** Position of center of top chunk of the urchin (left if orientation is horizontal) */
    private float t_x, t_y;


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
    public Urchin(float x, float y, float width, float height, Vector2 scale, int length, Orientation orientation) {
        super(x,y,width,height);
        this.length = length;
        //right_bound = right_b;

        if (length == 1) {
            origin = new Vector2(textures[0].getRegionWidth()/2.0f, textures[0].getRegionHeight()/2.0f);
        } else {
            origin = new Vector2(textures[1].getRegionWidth()/2.0f, textures[1].getRegionHeight()/2.0f);
        }

        setBodyType(BodyDef.BodyType.StaticBody);

        setName("urchin" + urchin_count);
        urchin_count++;
        setDrawScale(scale);
        //System.out.println("height: " + getHeight() + ", width: " + getWidth());

    }

    public Urchin(float x, float y, Vector2 scale, int length, Orientation orientation) {
        this(x,y,
                orientation == Orientation.VERTICAL ? textures[0].getRegionWidth() / scale.x :
                        (length == 1) ? textures[0].getRegionHeight() / scale.y :
                                (textures[1].getRegionHeight() / scale.y + Math.max(0, length - 2)*textures[2].getRegionHeight() /scale.y + textures[3].getRegionHeight()/scale.y),
        orientation == Orientation.HORIZONTAL ? textures[0].getRegionWidth() / scale.x :
                (length == 1) ? textures[0].getRegionHeight() / scale.y :
                        (textures[1].getRegionHeight() / scale.y + Math.max(0, length - 2)*textures[2].getRegionHeight() /scale.y + textures[3].getRegionHeight()/scale.y),
        scale, length, orientation);

    }

    /**
     * Set the array of textures to be all possible textures for this urchin based on the prefix
     * @param prefix The prefix for the names of all textures in the assets JSON.
     */
    public static void setTextures(String prefix) {
        //System.out.println("hi");
        pre = prefix;
        textures = new FilmStrip[4];
        textures[0] = JsonAssetManager.getInstance().getEntry(prefix + " single", FilmStrip.class);
        textures[1] = JsonAssetManager.getInstance().getEntry(prefix + " top", FilmStrip.class);
        textures[2] = JsonAssetManager.getInstance().getEntry(prefix + " center", FilmStrip.class);
        textures[3] = JsonAssetManager.getInstance().getEntry(prefix + " bottom", FilmStrip.class);
    }

    /**Tick all textures used for the urchin. Behaves statically. */
    public static void tickTextures(){
        for (FilmStrip f : textures) {
            f.tick();
        }
    }

    public static String getTexturePrefix(){
        return pre;
    }

    public static TextureRegion[] getTextures() {
        return textures;
    }

    public int getLength() {return length;}

    /**
     * Return the height of the top segment [of a vertical urchin with length >1). Assumes textures have been set.
     * @return Return the height of the top segment [of a vertical urchin with length >1).
     */
    public float topHeight() {
        return (textures[1].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return the height of the bottom segment [of a vertical urchin with length >1). Assumes textures have been set.
     * @return Return the height of the bottom segment [of a vertical urchin with length >1).
     */
    public float botHeight() {
        return (textures[3].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return the height of the middle segment [of a vertical urchin with length >2). Assumes textures have been set.
     * @return Return the height of the middle segment [of a vertical urchin with length >1).
     */
    public float midHeight() {
        return (textures[2].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return the height of the single segment urchin. Assumes textures have been set.
     * @return The height of a single segment urchin.
     */
    public float singleHeight() {
        return (textures[0].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return a new star with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static Urchin fromJSON(JsonValue json, Vector2 scale) {
        Orientation orientation = json.get("orientation").asString().equals("vertical") ? Orientation.VERTICAL : Orientation.HORIZONTAL;

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
            height = width * scale.x / scale.y;
            width = temp * scale.y / scale.x;
        }
        return new Urchin(json.get("x").asFloat(), json.get("y").asFloat(), width, height, scale, length, orientation);
    }

    /**
     * Write this astronaut to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write orientation
        json.addChild("orientation", new JsonValue(getOrientation() == Orientation.VERTICAL ? "vertical" : "horizontal"));

        //Write position
        Vector2 pos = getPosition();

        json.addChild("x", new JsonValue(pos.x));
        json.addChild("y", new JsonValue(pos.y));

        //Write length
        json.addChild("length", new JsonValue(length));

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
        } else {
            if (getOrientation() == Orientation.VERTICAL) {
                t_y = getY() + getHeight()/2f - (textures[1].getRegionHeight()/(2f*getDrawScale().y));
                t_x = getX();
                float x = t_x;
                float y = t_y;
                canvas.draw(textures[1], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 1, 1.0f);

                for (int i = 2; i < length; i++) {
                    y -= textures[2].getRegionHeight() / getDrawScale().y;
                    canvas.draw(textures[2], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 1, 1.0f);
                }

                y -= textures[2].getRegionHeight() / getDrawScale().y;

                canvas.draw(textures[3], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 1, 1.0f);
            } else {
                t_y = getY();
                t_x = getX() + getWidth ()/2f - (textures[1].getRegionHeight()/(2f*getDrawScale().x));
                float angle = (float) (3 * Math.PI / 2);
                float x = t_x;
                float y = t_y;
                canvas.draw(textures[1], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle() + angle, 1, 1.0f);

                for (int i = 2; i < length; i++) {
                    x -= textures[2].getRegionHeight() / getDrawScale().x;
                    canvas.draw(textures[2], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle() + angle, 1, 1.0f);
                }

                x -= textures[2].getRegionWidth() / getDrawScale().x;

                canvas.draw(textures[3], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle() + angle, 1, 1.0f);
            }
        }
    }

    public ObstacleType getType() { return ObstacleType.URCHIN;}


    public String toString(){
        String out = "urch";
//        String out = "Urchin with { texture: ";
//        out += JsonAssetManager.getInstance().getKey(texture) + "}";
        return out;
    }

}


