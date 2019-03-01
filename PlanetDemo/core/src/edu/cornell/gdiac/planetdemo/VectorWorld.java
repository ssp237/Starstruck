package edu.cornell.gdiac.planetdemo;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.HashMap;

/**
 * Class to represent a world tied to a vector field.
 *
 *
 * The World of a VectorWorld does not have gravity. All gravity is controlled
 * through the vector field tied to the World. Whenever an object that has gravity
 * is added to or removed from the World, the vector field must be adjusted to account
 * for the changes in gravity.
 */
public class VectorWorld {

    /** Number of points in the vector field (x direction)*/
    private final int NUM_VX = 50;
    /** Number of points in the vector field (y direction)*/
    private final int NUM_VY = 50;
    /** Gravitational constant (will probably change)*/
    private final double G = 6.67E-11;

    /** Vector field representing this World's gravity*/
    /** Each key represents the (x,y) coordinate of a point in a grid on the screen, and
     * each value represents the effective force experienced by a body at that location.*/
    private HashMap<Vector2, Vector2> vField;
    /** The world tied to this object*/
    public World world;
    /** HashMap to store all bodies in the world (to track if a body has gravity)*/
    private HashMap<BodyDef, Boolean> bodies;
    /** The bounds of this world*/
    private Rectangle bounds;
    /** The height of a cell*/
    private float height;
    /** The width of a cell*/
    private float width;

    /**
     *  Initializes a VectorWorld to contain a new world and be bounded by Rectangle Bounds.
     *  Sets every point in the vector field to have an effective force of (0,0).
     * @param bounds The bounds on this world (used for calculate cell height).
     */
    public VectorWorld( Rectangle bounds){
        world = new World(new Vector2(0, 0), false);
        this.bounds = bounds;
        height = bounds.height/NUM_VY;
        width = bounds.width/NUM_VX;

        bodies = new HashMap<BodyDef, Boolean>();
        vField = new HashMap<Vector2, Vector2>();
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                vField.put(new Vector2(i, j), new Vector2(0, 0));
            }
        }

    }

    /**
     * Add the specified body to the world, without changing the vector field
     * (i.e. the specified body is not massive enough to have its own force of gravity).
     * @param body The body to be added to the world.
     */
    public void addBodyNoGravity(BodyDef body){
        world.createBody(body);
        bodies.put(body, false);
    }

    /**
     *  Returns the gravitational force experienced by an object at position cell (i,j) from an object at
     *  position pos with mass mass.
     * @param pos The position of the gravitating object.
     * @param i The x coordinate of the cell of the object experiencing gravity.
     * @param j The y coordinate of the cell of the object experiencing gravity.
     * @param mass the mass of the gravitating object.
     * @return The gravitational force experienced by the object at position (i,j).
     */
    private Vector2 gForce(Vector2 pos, int i, int j, float mass){
        float x = i * width; float y = j * height;
        //Get r
        float dist = pos.dst(new Vector2(x,y));
        //Find magnitude of gravity
        float mag = (float) (G*mass)/(dist*dist);

        float dx = x - pos.x; float dy = y - pos.y;
        float dh = (float) Math.sqrt(dx*dx + dy*dy);
        float fx = (dx/dh) * mag; float fy = (dy/dh) * mag;
        return new Vector2(fx, fy);
    }

    /**
     * Add the specified body to the world with the specified mass. Alters the vector
     * field to take into account the force of gravity to the new object.
     * @param body The body to be added to the world.
     * @param mass The mass of the new body.
     */
    public void addBody(BodyDef body, float mass){
        world.createBody(body);
        bodies.put(body, true);
        Vector2 pos = body.position;

        //inefficient, need to change
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                Vector2 v = new Vector2(i,j);
                vField.put(v, vField.get(v).add(gForce(pos, i, j, mass)));
            }
        }
    }
}
