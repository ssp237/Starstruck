package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.util.JsonAssetManager;

/** Representation of a planet in the game. Has its own mass, size, and range of effect for gravity.
 *  Also stores the galaxy the planet is from, to be used when determining sprite, and
 *
 */
public class Button extends BoxObstacle {

    /** Texture for hover texture */
    protected TextureRegion hoverTexture;
    /** Boolean to check if mouse is hovering */
    protected boolean active;
    /** string with name of button */
    protected String name;

    public boolean pushed;

    /**
     * Constructor. Creates a planet centered at (x,y) with the specified radius, mass and galaxy.
     * @param x The x coordinate of the button's center
     * @param y The y coordinate of the button's center
     * @param width The button's width
     * @param height The button's height
     */
    public Button(float x, float y, float width, float height, TextureRegion texture,
                  World world, Vector2 scale, TextureRegion hoverTexture, String name) {
        super(x, y, width, height);
        this.texture = texture;
        this.hoverTexture = hoverTexture;
        this.name = name;
        pushed = false;

        setBodyType(BodyDef.BodyType.StaticBody);

        activatePhysics(world);
        setDrawScale(scale);
    }

    /**
     * Return the texture of this level
     * @return The texture of this level.
     */
    public TextureRegion getHoverTexture() { return hoverTexture;}

    /**
     * Sets hover to true when mouse pointer is over this level sprite
     */
    public void setActive(boolean state) {
        active = state;
    }

    /**
     * gets active state
     */
    public boolean getActive() {
        return active;
    }

    /**
     * gets active state
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        //Draw if on hover
        if (active) {
            canvas.draw(hoverTexture, Color.WHITE, origin.x, origin.y,getX() * drawScale.x,
                    getY() * drawScale.x, getAngle(), 1,1);
        } else {
            canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y,getX() * drawScale.x,
                    getY() * drawScale.x, getAngle(), 1,1);
        }

    }

    public String toString() {
        String out = "Button with {";
        if (active) {
            out = "Active Button with {";
        }

        out += "pos: " + getPosition() + ", ";
        out += "width: " + getWidth() + ", ";
        out += "height: " + getHeight() + ", ";
        out += "texture: " + getTexture() + ", ";
        out += "}";

        return out;
    }

}
