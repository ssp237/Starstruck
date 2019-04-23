/*
 * Spinner.java
 *
 * This class provides a spinning rectangle on a fixed pin.  We did not really need
 * a separate class for this, as it has no update.  However, ComplexObstacles always
 * make joint management easier.
 * 
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.platform;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import edu.cornell.gdiac.physics.obstacle.*;

public class Spinner extends ComplexObstacle {
	/** The debug name for the entire obstacle */
	private static final String SPINNER_NAME = "spinner";
	/** The debug name for the spinning barrier */
	private static final String BARRIER_NAME = "barrier";
	/** The debug name for the central pin */
	private static final String SPIN_PIN_NAME = "pin";
	/** The density for most physics objects */
	private static final float LIGHT_DENSITY = 0.0f;
	/** The density for a bullet */
	private static final float HEAVY_DENSITY = 10.0f;
	/** The radius of the central pin */
	private static final float SPIN_PIN_RADIUS = 0.1f;
	
	/** The primary spinner obstacle */
	private BoxObstacle barrier;

	private WheelObstacle pivot;
	
	/**
	 * Creates a new spinner at the origin.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public Spinner(float width, float height) {
		this(0,0,width,height);
	}

	/**
	 * Creates a new spinner at the given position.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param x  		Initial x position of the avatar center
	 * @param y  		Initial y position of the avatar center
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public Spinner(float x, float y, float width, float height) {
        super(x,y);
        setName(SPINNER_NAME);
        
        // Create the barrier
        barrier = new BoxObstacle(x,y,width,height);
        barrier.setName(BARRIER_NAME);
        barrier.setDensity(HEAVY_DENSITY);
        bodies.add(barrier);
        
		//#region INSERT CODE HERE
        // Create a pin to anchor the barrier
		pivot = new WheelObstacle(x,y,SPIN_PIN_RADIUS);
		pivot.setName(SPIN_PIN_NAME);
		pivot.setDensity(LIGHT_DENSITY);
		pivot.setBodyType(BodyDef.BodyType.StaticBody);
		bodies.add(pivot);


        // Radius: SPIN_PIN_RADIUS
        // Density: LIGHT_DENSITY
        		
        //#endregion
    }
	
	/**
	 * Creates the joints for this object.
	 * 
	 * We implement our custom logic here.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected boolean createJoints(World world) {
		assert bodies.size > 0;

		//#region INSERT CODE HERE
		// Attach the barrier to the pin here

		RevoluteJointDef jointDef = new RevoluteJointDef();

		jointDef.bodyA = barrier.getBody();
		jointDef.bodyB = pivot.getBody();
		jointDef.collideConnected = false;
		Joint joint = world.createJoint(jointDef);
		joints.add(joint);

		//#endregion

		return true;
	}
	
	public void setTexture(TextureRegion texture) {
		barrier.setTexture(texture);
	}
	
	public TextureRegion getTexture() {
		return barrier.getTexture();
	}
}
