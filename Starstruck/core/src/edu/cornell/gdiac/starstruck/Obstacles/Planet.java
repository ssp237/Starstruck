package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.starstruck.GameCanvas;

/** Representation of a planet in the game. Has its own mass, size, and range of effect for gravity.
 *  Also stores the galaxy the planet is from, to be used when determining sprite, and
 *
 */
public class Planet extends WheelObstacle {

    /** The possible sprites for a planet*/


    /** The mass of a planet in [slightly arbitrary] units. */
    protected float mass;


    /**
     * Constructor. Creates a planet centered at (x,y) with the specified radius, mass and galaxy.
     * @param x The x coordinate of the planet's center
     * @param y The y coordinate of the planet's center
     * @param radius The planet's radius
     * @param mass The planet's mass
     */
    public Planet(float x, float y, float radius, float mass, float scaleDraw,
                  TextureRegion texture, World world, Vector2 scale) {
        super(x, y, radius);
        this.mass = mass;
        this.texture = texture;

        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(500f);

        activatePhysics(world);
        setDrawScale(scale);

        this.scaleDraw = scaleDraw;

        // Not needed?
        //setFriction(BASIC_FRICTION);
        //setRestitution(BASIC_RESTITUTION);
    }


    /**
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y,getX() * drawScale.x,
                getY() * drawScale.x, getAngle(), scaleDraw, scaleDraw);
    }


}
