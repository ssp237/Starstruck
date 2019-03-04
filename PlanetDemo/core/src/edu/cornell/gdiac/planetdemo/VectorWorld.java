package edu.cornell.gdiac.planetdemo;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

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
    private final double G = 2.67E-2;

    /** Vector field representing this World's gravity*/
    /** Each key represents the (x,y) coordinate of a point in a grid on the screen, and
     * each value represents the effective force experienced by a body at that location.*/
    private HashMap<Vector2, Vector2> vField;
    /** The world tied to this object*/
    private World world;
    /** HashMap to store all bodies in the world (to track if a body has gravity)*/
    private HashMap<Body, Boolean> bodies;
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

        bodies = new HashMap<Body, Boolean>();
        vField = new HashMap<Vector2, Vector2>();
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                vField.put(new Vector2(i, j), new Vector2(0, 0));
            }
        }

    }

    /**
     * Gets the world attached to this VectorWorld
     *
     * @return world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Add the specified body to the world, without changing the vector field
     * (i.e. the specified body is not massive enough to have its own force of gravity).
     * @param def The body to be added to the world.
     * @return The body added to the world.
     */
    public Body addBodyNoGravity(BodyDef def){
        Body body = world.createBody(def);
        bodies.put(body, false);
        return body;
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
        //System.out.println(fx + ", " + fy);
        return new Vector2(fx, fy);
    }

    /**
     * Add the specified body to the world. Alters the vector
     * field to take into account the force of gravity to the new object.
     * @param def The body to be added to the world.
     * @return The body added to the world
     */
    public Body addBody(BodyDef def){
        Body body = world.createBody(def);
        bodies.put(body, true);
        Vector2 pos = body.getPosition();
        float mass = body.getMass();

        //inefficient, need to change
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                Vector2 v = new Vector2(i,j);
                vField.put(v, vField.get(v).add(gForce(pos, i, j, mass)));
            }
        }
        return body;
    }

    /**
     * Like addBody. Adds the specified obstacle to the world. Alters vector field. Ideal for adding planets.
     *
     * @param obj The obstacle/planet to be added to the world.
     */
    public void addPlanet(WheelObstacle obj, float mass) {
        bodies.put(obj.getBody(), true);
        Vector2 pos = obj.getCenter();
        pos.y -= 1.75;
        System.out.println(pos);

        //inefficient, need to change
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                Vector2 v = new Vector2(i,j);
                vField.put(v, vField.get(v).add(gForce(pos, i, j, mass)));
            }
        }
    }

    /**
     * Create a joint to constrain bodies together.
     * @param def A definition for the join.
     * @return The created joint.
     */
    public Joint creatJoint(JointDef def){
        return world.createJoint(def);
    }

    /**
     * Remove the effect of gravity due to the given body from the vector field.
     * @param body The body who's gravity is to be removed.
     */
    private void remGrav(Body body){
        Vector2 pos = body.getPosition();
        float mass = body.getMass();

        //inefficient, need to change
        for (int i = 0; i < NUM_VX; i++){
            for (int j = 0; j < NUM_VY; j++){
                Vector2 v = new Vector2(i,j);
                vField.put(v, vField.get(v).sub(gForce(pos, i, j, mass)));
            }
        }
    }

    /**
     * Destroy a rigid body from the world. If the body has gravity, remove the effects of gravity
     * due to the body. If the gravity is not in the world, nothing is changed.
     * @param body The body to be destroyed.
     */
    public void destroyBody(Body body){
        if (!bodies.containsKey(body)) return;
        if (bodies.get(body)) remGrav(body);
        bodies.remove(body);
        world.destroyBody(body);
    }

    /**
     * Destroy a joint from the world.
     * @param joint The joint to be destroyed.
     */
    public void destroyJoint(Joint joint){
        world.destroyJoint(joint);
    }

    /**
     *  Return a Vector2 that represents the closest cell coordinates (of the vector field) to
     *  Vector2 pos. Rounds down.
     * @param pos A Vector2 where both components are strictly positive.
     * @return A Vector2 representing the closest cell coordinates in the vector field to pos,
     * rounding down.
     */
    private Vector2 screenToCell(Vector2 pos){
        float x = Math.max(0, Math.min(NUM_VX - 1, (float) Math.floor(pos.x/width)));
        float y = Math.max(0, Math.min(NUM_VY - 1, (float) Math.floor(pos.y/width)));
        return new Vector2(x, y);
    }

    /**
     * Returns the effective force experienced at the position with coordinates pos.
     * @param pos A Vector2 representing a position with strictly positive coordinates.
     * @return A Vector2 representing the effective force experienced at position pos.
     */
    public Vector2 getForce(Vector2 pos){
        Vector2 newPos = screenToCell(pos);
        return vField.get(newPos);
    }


}
