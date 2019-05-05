/*
 * RocketModel.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Note how this class combines physics and animation.  This is a good template
 * for models in your game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.rocket;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Player avatar for the rocket lander game.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class RocketModel extends BoxObstacle {
    /**
     * Enumeration to identify the rocket afterburner
     */
    public enum Burner {
        /** The main afterburner */
        MAIN,
        /** The left side thruster */
        LEFT,
        /** The right side thruster */
        RIGHT
    };
	
	// Default physics values
	/** The density of this rocket */
	private static final float DEFAULT_DENSITY  =  1.0f;
	/** The friction of this rocket */
	private static final float DEFAULT_FRICTION = 0.1f;
	/** The restitution of this rocket */
	private static final float DEFAULT_RESTITUTION = 0.4f;
	/** The thrust factor to convert player input into thrust */
	private static final float DEFAULT_THRUST = 30.0f;
	/** The number of frames for the afterburner */
	public static final int FIRE_FRAMES = 4;

	/** The force to apply to this rocket */
	private Vector2 force;

    /** The texture filmstrip for the left animation node */
    FilmStrip mainBurner;
    /** The associated sound for the main afterburner */
    String mainSound;
    /** The animation phase for the main afterburner */
    boolean mainCycle = true;

    /** The texture filmstrip for the left animation node */
    FilmStrip leftBurner;
    /** The associated sound for the left side burner */
    String leftSound;
    /** The animation phase for the left side burner */
    boolean leftCycle = true;

    /** The texture filmstrip for the left animation node */
    FilmStrip rghtBurner;
    /** The associated sound for the right side burner */
    String rghtSound;
    /** The associated sound for the right side burner */
    boolean rghtCycle  = true;

	/** Cache object for transforming the force according the object angle */
	public Affine2 affineCache = new Affine2();
	/** Cache object for left afterburner origin */
	public Vector2 leftOrigin = new Vector2();
	/** Cache object for right afterburner origin */
	public Vector2 rghtOrigin = new Vector2();
	
	/**
	 * Returns the force applied to this rocket.
	 * 
	 * This method returns a reference to the force vector, allowing it to be modified.
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the force applied to this rocket.
	 */
	public Vector2 getForce() {
		return force;
	}

	/**
	 * Returns the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the x-component of the force applied to this rocket.
	 */
	public float getFX() {
		return force.x;
	}

	/**
	 * Sets the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @param value the x-component of the force applied to this rocket.
	 */
	public void setFX(float value) {
		force.x = value;
	}

	/**
	 * Returns the y-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @return the y-component of the force applied to this rocket.
	 */
	public float getFY() {
		return force.y;
	}

	/**
	 * Sets the x-component of the force applied to this rocket.
	 * 
	 * Remember to modify the input values by the thrust amount before assigning
	 * the value to force.
	 *
	 * @param value the x-component of the force applied to this rocket.
	 */
	public void setFY(float value) {
		force.y = value;
	}
	
	/**
	 * Returns the amount of thrust that this rocket has.
	 *
	 * Multiply this value times the horizontal and vertical values in the
	 * input controller to get the force.
	 *
	 * @return the amount of thrust that this rocket has.
	 */
	public float getThrust() {
		return DEFAULT_THRUST;
	}

	/**
	 * Creates a new rocket at the origin.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public RocketModel(float width, float height) {
		this(0,0,width,height);
	}

	/**
	 * Creates a new rocket at the given position.
	 *
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param x  		Initial x position of the box center
	 * @param y  		Initial y position of the box center
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public RocketModel(float x, float y, float width, float height) {
		super(x,y,width,height);
		force = new Vector2();
		setDensity(DEFAULT_DENSITY);
		setDensity(DEFAULT_DENSITY);
		setFriction(DEFAULT_FRICTION);
		setRestitution(DEFAULT_RESTITUTION);
		setName("rocket");
	}
	
	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// Get the box body from our parent class
		if (!super.activatePhysics(world)) {
			return false;
		}
		
		//#region INSERT CODE HERE
		// Insert code here to prevent the body from rotating
		setFixedRotation(true);
		//#endregion
		
		return true;
	}
	
	
	/**
	 * Applies the force to the body of this ship
	 *
	 * This method should be called after the force attribute is set.
	 */
	public void applyForce() {
		if (!isActive()) {
			return;
		}
		
		// Orient the force with rotation.
		affineCache.setToRotationRad(getAngle());
		affineCache.applyTo(force);
		
		//#region INSERT CODE HERE
		// Apply force to the rocket BODY, not the rocket
		body.applyForce(force, getPosition(), isAwake());
		//#endregion
	}

	// Animation methods (DO NOT CHANGE)
	/**
	 * Returns the animation node for the given afterburner
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @return the animation node for the given afterburner
	 */
	public FilmStrip getBurnerStrip(Burner burner) {
	    switch (burner) {
	        case MAIN:
	            return mainBurner;
	        case LEFT:
	            return leftBurner;
	        case RIGHT:
	            return rghtBurner;
	    }
	    assert false : "Invalid burner enumeration";
	    return null;
	}

	/**
	 * Sets the animation node for the given afterburner
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @param  //node the animation node for the given afterburner
	 */
	public void setBurnerStrip(Burner burner, FilmStrip strip) {
	    switch (burner) {
	        case MAIN:
	        	mainBurner = strip;
	            break;
	        case LEFT:
	        	leftBurner = strip;
	        	if (strip != null) {
	        		leftOrigin.set(strip.getRegionWidth()/2.0f,strip.getRegionHeight()/2.0f);
	        	}
	            break;
	        case RIGHT:
	        	rghtBurner = strip;
	        	if (strip != null) {
	        		rghtOrigin.set(strip.getRegionWidth()/2.0f,strip.getRegionHeight()/2.0f);
	        	}
	            break;
	        default:
	    	    assert false : "Invalid burner enumeration";
	    }   
	}

	/**
	 * Returns the key for the sound to accompany the given afterburner
	 *
	 * The key should either refer to a valid sound loaded in the AssetManager or
	 * be empty ("").  If the key is "", then no sound will play.
	 *
	 * @param  burner   enumeration to identify the afterburner
	 *
	 * @return the key for the sound to accompany the given afterburner
	 */
	public String getBurnerSound(Burner burner) {
	    switch (burner) {
	        case MAIN:
	            return mainSound;
	        case LEFT:
	            return leftSound;
	        case RIGHT:
	            return rghtSound;
	    }
	    assert false : "Invalid burner enumeration";
	    return null;
	}

	/**
	 * Sets the key for the sound to accompany the given afterburner
	 *
	 * The key should either refer to a valid sound loaded in the AssetManager or
	 * be empty ("").  If the key is "", then no sound will play.
	 *
	 * @param  burner   enumeration to identify the afterburner
	 * @param  key      the key for the sound to accompany the main afterburner
	 */
	public void setBurnerSound(Burner burner, String key) {
	    switch (burner) {
	        case MAIN:
	            mainSound = key;
	            break;
	        case LEFT:
	            leftSound = key;
	            break;
	        case RIGHT:
	            rghtSound = key;
	            break;
	        default:
	    	    assert false : "Invalid burner enumeration";
	    }
	}

	/**
	 * Animates the given burner.
	 *
	 * If the animation is not active, it will reset to the initial animation frame.
	 *
	 * @param  burner   The reference to the rocket burner
	 * @param  on       Whether the animation is active
	 */
	public void animateBurner(Burner burner, boolean on) {
	    FilmStrip node = null;
	    boolean  cycle = true;
	    
	    switch (burner) {
	        case MAIN:
	            node  = mainBurner;
	            cycle = mainCycle;
	            break;
	        case LEFT:
	            node  = leftBurner;
	            cycle = leftCycle;
	            break;
	        case RIGHT:
	            node  = rghtBurner;
	            cycle = rghtCycle;
	            break;
	        default:
	    	    assert false : "Invalid burner enumeration";
	    }
	    
	    if (on) {
	        // Turn on the flames and go back and forth
	        if (node.getFrame() == 0 || node.getFrame() == 1) {
	            cycle = true;
	        } else if (node.getFrame() == node.getSize()-1) {
	            cycle = false;
	        }
	        
	        // Increment
	        if (cycle) {
	            node.setFrame(node.getFrame()+1);
	        } else {
	            node.setFrame(node.getFrame()-1);
	        }
	    } else {
	        node.setFrame(0);
	    }
	    
	    switch (burner) {
        case MAIN:
            mainCycle = cycle;
            break;
        case LEFT:
            leftCycle = cycle;
            break;
        case RIGHT:
            rghtCycle = cycle;
            break;
        default:
    	    assert false : "Invalid burner enumeration";
	    }
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		super.draw(canvas);  // Ship
		// Flames
		if (mainBurner != null) {
			float offsety = mainBurner.getRegionHeight()-origin.y;
			canvas.draw(mainBurner,Color.WHITE,origin.x,offsety,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
		if (leftBurner != null) {
			canvas.draw(leftBurner,Color.WHITE,leftOrigin.x,leftOrigin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
		if (rghtBurner != null) {
			canvas.draw(rghtBurner,Color.WHITE,rghtOrigin.x,rghtOrigin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
	}
}