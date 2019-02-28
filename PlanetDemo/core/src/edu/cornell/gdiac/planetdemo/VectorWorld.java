package edu.cornell.gdiac.planetdemo;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
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

    /** Vector field representing this World's gravity*/
    /** Each key represents the (x,y) coordinate of a point in a grid on the screen, and
     * each value represents the effective force experienced by a body at that location.*/
    private HashMap<Vector2, Vector2> vField;
    /** The world tied to this object*/
    public World world;
    /** HashMap to store all bodies in the world (to track if a body has gravity)*/
    private HashMap<Body, Boolean> bodies;
    /** The bounds of this world*/
    private Rectangle bounds;
    /** The height of a cell*/
    private float height;
    /** The width of a cell*/
    private float width;

    public VectorWorld(World world, Rectangle bounds){
        this.world = world;
        this.bounds = bounds;
        height = bounds.height/NUM_VY;
        width = bounds.width/NUM_VX;

        bodies = new HashMap<Body, Boolean>();
        vField = new HashMap<Vector2, Vector2>();
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                vField.put(new Vector2(i, j), new Vector2(0, 0));
            }
        }

    }
}
