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

    /** The mass of a planet in [slightly arbitrary] units. */
    protected float mass;
    /** The range from which gravity is effective for this planet (physics units)*/
    public float grange;
    /** Texture for gravity ring */
    protected TextureRegion ringTexture;

    private float grscale;

    /** Counts the number of planets created to assign names */
    private static int counter = 0;

    /**
     * Constructor. Creates a planet centered at (x,y) with the specified radius, mass and galaxy.
     * @param x The x coordinate of the planet's center
     * @param y The y coordinate of the planet's center
     * @param radius The planet's radius
     * @param mass The planet's mass
     */
    public Planet(float x, float y, float radius, float mass, float grange,
                  TextureRegion texture, World world, Vector2 scale, TextureRegion ringTexture) {
        super(x, y, radius);
        this.mass = mass;
        this.texture = texture;
        this.grange = grange;
        this.ringTexture = ringTexture;

        setBodyType(BodyDef.BodyType.StaticBody);
        setDensity(500f);

        activatePhysics(world);
        setDrawScale(scale);

        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;

        grscale = (float) texture.getRegionWidth() / (float) ringTexture.getRegionWidth();

        setName("planet" + counter);
        counter++;

        // Not needed?
        //setFriction(BASIC_FRICTION);
        //setRestitution(BASIC_RESTITUTION);
    }

    /**
     * Return the mass of this planet
     * @return The mass of this planet.
     */
    public float getMass() { return mass;}

    /**
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {

        //Draw planet
        canvas.draw(getTexture(), Color.WHITE, origin.x, origin.y,getX() * drawScale.x - texture.getRegionWidth()/(2/scaleDraw),
                getY() * drawScale.x - texture.getRegionHeight()/(2/scaleDraw), getAngle(), scaleDraw,scaleDraw);

        float rScale = grscale * scaleDraw * ((getRadius() + grange) / getRadius());

        Color color = new Color(1,1,1,0.75f);

        //Draw ring
        canvas.draw(ringTexture, color, origin.x, origin.y,getX() * drawScale.x - ringTexture.getRegionWidth()/(2/rScale),
                getY() * drawScale.x - ringTexture.getRegionHeight()/(2/rScale), getAngle(), rScale, rScale);

    }


}
