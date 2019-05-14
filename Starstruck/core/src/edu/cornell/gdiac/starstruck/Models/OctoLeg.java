package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.Anchor;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

public class OctoLeg extends Enemy {

    /** Counter for names */
    private static int octo_count = 1;
    /** Prefix of all texture names */
    private static String pre;
    /**Textures for drawing. In order, single - top - middle - bottom.*/
    protected static TextureRegion[] textures;
    /** Number of body segments */
    private int length;
    /** Position of center of top chunk of the urchin (left if orientation is horizontal) */
    private float t_x, t_y;
    /**Velocity; horizontal or vertical depending on orientation */
    private float v = 2.0f;
    /**Anchors! */
    private Anchor anchor1, anchor2;
    /** Original coordinates (for scrolling) */
    private float x_original, y_original;


    /**
     * Creates a new OctoLeg at the given position.
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
     * @param scale  		Drawscale
     * @param length  		Number of octo chunks in this octo (>= 2)
     * @param orientation	Orientation (horizontal/vertical)
     */
    public OctoLeg(float x, float y, float width, float height, Vector2 scale, int length, Orientation orientation) {
        super(x,y,width,height);
        this.length = length;
        //right_bound = right_b;

            origin = new Vector2(textures[1].getRegionWidth()/2.0f, textures[1].getRegionHeight()/2.0f);

        setName("octoLeg" + octo_count);
        octo_count++;
        setDrawScale(scale);

        Vector2 a1Pos = anchor1Pos(), a2Pos = anchor2Pos();
        anchor1 = new Anchor(a1Pos.x, a1Pos.y,
                JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale);
        anchor2 = new Anchor(a2Pos.x, a2Pos.y,
                JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale);
        //System.out.println("height: " + getHeight() + ", width: " + getWidth());

        if (getOrientation() == Orientation.VERTICAL) {
            setVY(v);
        } else {
            setVX(v);
        }

        x_original = getX(); y_original = getY();

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
     * @param scale  		Drawscale
     * @param length  		Number of octo chunks in this octo (>= 2)
     * @param orientation	Orientation (horizontal/vertical)
     */
    public OctoLeg(float x, float y, Vector2 scale, int length, Orientation orientation) {
        this(x,y,
                orientation == Orientation.VERTICAL ? textures[0].getRegionWidth() / scale.x :
                        (textures[0].getRegionHeight() / scale.y + Math.max(0, length - 2)*textures[1].getRegionHeight() /scale.y
                                + textures[2].getRegionHeight()/scale.y),
                orientation == Orientation.HORIZONTAL ? textures[0].getRegionWidth() / scale.x :
                        (textures[0].getRegionHeight() / scale.y + Math.max(0, length - 2)*textures[1].getRegionHeight() /scale.y
                                + textures[2].getRegionHeight()/scale.y),
                scale, length, orientation);

    }

    /**
     * Return a vector representing the position of the first anchor
     * @return a vector representing the position of the first anchor
     */
    private Vector2 anchor1Pos() {
        float x, y;
        if (getOrientation() == Orientation.VERTICAL) {
            x = getX();
            y = getY() - getHeight()/2 + ((float) textures[0].getRegionHeight()/4)/drawScale.y;
        } else {
            y = getY();
            x = getX() - getWidth()/2 + ((float) textures[0].getRegionHeight()/4)/drawScale.x;
        }
        return new Vector2(x,y);
    }

    /**
     * Return a vector representing the position of the second anchor
     * @return a vector representing the position of the second anchor
     */
    private Vector2 anchor2Pos() {
        float x, y;
        if (getOrientation() == Orientation.VERTICAL) {
            x = getX();
            y = getY() + getHeight()/2 - ((float) textures[0].getRegionHeight()/4)/drawScale.y;
        } else {
            y = getY();
            x = getX() + getWidth()/2 - ((float) textures[0].getRegionHeight()/4)/drawScale.x;
        }
        return new Vector2(x,y);
    }

    public void setPosition(Vector2 position) {
        super.setPosition(position);
        Vector2 a1Pos = anchor1Pos(), a2Pos = anchor2Pos();
        anchor1.setPosition(a1Pos);
        anchor2.setPosition(a2Pos);
        x_original = getX(); y_original = getY();
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2(x,y));
    }

    /**
     * Set the array of textures to be all possible textures for this urchin based on the prefix
     * @param prefix The prefix for the names of all textures in the assets JSON.
     */
    public static void setTextures(String prefix) {
        //System.out.println("hi");
        pre = prefix;
        textures = new TextureRegion[3];
        textures[0] = JsonAssetManager.getInstance().getEntry(prefix + " top", TextureRegion.class);
        textures[1] = JsonAssetManager.getInstance().getEntry(prefix + " center", TextureRegion.class);
        textures[2] = JsonAssetManager.getInstance().getEntry(prefix + " bottom", TextureRegion.class);
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
        return (textures[0].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return the height of the bottom segment [of a vertical urchin with length >1). Assumes textures have been set.
     * @return Return the height of the bottom segment [of a vertical urchin with length >1).
     */
    public float botHeight() {
        return (textures[2].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return the height of the middle segment [of a vertical urchin with length >2). Assumes textures have been set.
     * @return Return the height of the middle segment [of a vertical urchin with length >1).
     */
    public float midHeight() {
        return (textures[1].getRegionHeight()) /  getDrawScale().y;
    }

    /**
     * Return a new OctoLeg with parameters specified by the JSON
     * @param json A JSON containing data for one star
     * @param scale The scale to convert physics units to drawing units
     * @return A star created according to the specifications in the JSON
     */
    public static OctoLeg fromJSON(JsonValue json, Vector2 scale) {
        Orientation orientation = json.get("orientation").asString().equals("vertical") ? Orientation.VERTICAL : Orientation.HORIZONTAL;

        int length = json.get("length").asInt();

        float width = textures[0].getRegionWidth() / scale.x;
        float height = 0;
        if (length == 1 ) {
            throw new IndexOutOfBoundsException("OctoLeg needs at least 2 chunks!!");
        } // more than one chunk
        height = (textures[0].getRegionHeight() + textures[2].getRegionWidth()) /  scale.y;
        for (int i = 2; i < length; i++) {
            height += textures[1].getRegionHeight() / scale.y;
        }
        OctoLeg u = new OctoLeg(json.get("x").asFloat(), json.get("y").asFloat(), width, height, scale, length, orientation);
        if (orientation == Orientation.VERTICAL) return u;

        return new OctoLeg(u.getX(), u.getY(), u.getHeight() / Enemy.DUDE_HSHRINK, u.getWidth() / Enemy.DUDE_VSHRINK, scale, u.getLength(), orientation);
    }

    /**
     * Write this octoLeg to a JsonValue. When parsed, this JsonValue should return the same astronaut.
     * @return A JsonValue representing this AstronautModel.
     */
    public JsonValue toJson() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);

        //Write orientation
        json.addChild("orientation", new JsonValue(getOrientation() == Orientation.VERTICAL ? "vertical" : "horizontal"));

        //Write position

        json.addChild("x", new JsonValue(x_original));
        json.addChild("y", new JsonValue(y_original));

        //Write length
        json.addChild("length", new JsonValue(length));

        //System.out.println(json);

        return json;
    }

    public void update(float dt) {
        super.update(dt);
        //System.out.println(getVX());

//        if (this.getPosition().x < 0) {
//            delay_pos--;
//            if (delay_pos == 0) {
//                delay_pos = 1000;
//                this.setPosition(right_bound, y_pos);
//            }
        //}
        if (this.getPosition().x < x_original - 5 || this.getPosition().x > x_original + 5) {
            //System.out.println(getVX());
            setVX(-getVX());
        }
        if (this.getPosition().y < y_original - 5 || this.getPosition().y > y_original + 5) {
            //System.out.println(getVX());
            setVY(-getVY());
        }
        Vector2 a1Pos = anchor1Pos(), a2Pos = anchor2Pos();
        anchor1.setPosition(a1Pos);
        anchor2.setPosition(a2Pos);
        //System.out.println(this);
        //System.out.println(this);

    }

    public boolean isSleeping() {
        return true;
    }


    public void draw(GameCanvas canvas) {
        if (getOrientation() == Orientation.VERTICAL) {
            t_y = getY() + getHeight()/2f - (textures[0].getRegionHeight()/(3f*getDrawScale().y));
            t_x = getX();
            float x = t_x;
            float y = t_y;
            canvas.draw(textures[0], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 1, 1.0f);

            for (int i = 2; i < length; i++) {
                y -= textures[1].getRegionHeight() / getDrawScale().y;
                canvas.draw(textures[1], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 1, 1.0f);
            }
             y -= textures[2].getRegionHeight() / getDrawScale().y;

            canvas.draw(textures[2], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle(), 1, 1.0f);
        } else {
            t_y = getY();
            t_x = getX() + getWidth ()/2f - (textures[0].getRegionHeight()/(3f*getDrawScale().x));
            float angle = (float) (3 * Math.PI / 2);
            float x = t_x;
            float y = t_y;
            canvas.draw(textures[0], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle() + angle, 1, 1.0f);

            for (int i = 2; i < length; i++) {
                x -= textures[1].getRegionHeight() / getDrawScale().x;
                canvas.draw(textures[1], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle() + angle, 1, 1.0f);
            }
            x -= textures[2].getRegionHeight() / getDrawScale().x;
            canvas.draw(textures[2], Color.WHITE, origin.x, origin.y, x * drawScale.x, y * drawScale.y, getAngle() + angle, 1, 1.0f);
        }
        anchor1.draw(canvas);
        anchor2.draw(canvas);
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        anchor1.drawDebug(canvas);
        anchor2.drawDebug(canvas);
    }

    public ObstacleType getType() { return ObstacleType.OCTO_LEG;}


    public String toString(){
        String out = "octoLeg with {";
//        String out = "Urchin with { texture: ";
//        out += JsonAssetManager.getInstance().getKey(texture) + "}";
        out += "length: " + length + "}";
        return out;
    }
}
