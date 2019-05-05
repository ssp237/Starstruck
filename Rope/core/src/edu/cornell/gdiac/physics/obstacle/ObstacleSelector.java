/*
 * ObstacleSelector.java
 *
 * This class implements a selection tool for dragging physics objects with a mouse.
 * It is essentially an instance of MouseJoint, but with an API that makes it a lot
 * easier to use. As with all instances of MouseJoint, there will be some lag in
 * the drag (though this is true on touch devices in general).  You can adjust the
 * degree of this lag by adjusting the force.  However, larger forces can cause artifacts
 * when dragging an obstacle through other obstacles.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.obstacle;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;

import edu.cornell.gdiac.physics.*;  // For GameCanvas

/**
 * Selection tool to move and drag physics obstacles
 *
 * This class is essentially an instance of MouseJoint, but with an API that makes 
 * it a lot easier to use. It must be attached to a World on creation, and this
 * controller can never change.  If you want a selector for a different World, make 
 * a new instance.
 *
 * As with all instances of MouseJoint, there will be some lag in the drag (though 
 * this is true on touch devices in general).  You can adjust the degree of this lag
 * by adjusting the force.  However, larger forces can cause artifacts when dragging 
 * an obstacle through other obstacles.
 */
public class ObstacleSelector implements QueryCallback  {
	/** The default size of the mouse selector */
	private static float DEFAULT_MSIZE = 0.2f;
	/** The default update frequence (in Hz) of the joint */
	private static float DEFAULT_FREQUENCY = 10.0f;
	/** The default damping force of the joint */
	private static float DEFAULT_DAMPING = 0.7f;
	/** The default force multiplier of the selector */
	private static float DEFAULT_FORCE = 1000.0f;

    /** The World associated with this selection */
    private World world;
    /** The current fixture selected by this tool (may be nullptr) */
    private Fixture selection;
    /** A default body used as the other half of the mouse joint */
    private Body ground;
	/** The width and height of the box */
	private Vector2 dimension;
	
	/** The texture to display this selector on screen */
	private TextureRegion texture;
	/** The texture origin (in the center) */
	private Vector2 origin;
	/** The drawing scale for this selector */
	private Vector2 drawScale;
    
    /** A reusable definition for creating a mouse joint */
    private MouseJointDef mouseJointDef;
    /** The current mouse joint, if an item is selected */
    private MouseJoint mouseJoint;
    
    /** The region of world space to select an object from */
    private Rectangle  pointer;
    /** The amount to multiply by the mass to move the object */
    private float force;
    
    /** Position cache for moving mouse */
    private Vector2 position = new Vector2();
    /** Size cache for the draw scale */
    private Vector2 scaleCache = new Vector2();
    
	/**
     * Returns the response speed of the mouse joint
     *
     * See the documentation of b2JointDef for more information on the response speed.
     * 
     * @return the response speed of the mouse joint
     */
    public float getFrequency() { 
    	return mouseJointDef.frequencyHz; 
    }
    
    /**
     * Sets the response speed of the mouse joint
     *
     * See the documentation of b2JointDef for more information on the response speed.
     *
     * @param  speed    the response speed of the mouse joint
     */
    public void setFrequency(float speed) { 
    	mouseJointDef.frequencyHz = speed; 
    }
    
    /**
     * Returns the damping ratio of the mouse joint
     *
     * See the documentation of b2JointDef for more information on the damping ratio.
     *
     * @return the damping ratio of the mouse joint
     */
    public float getDamping() { 
    	return mouseJointDef.dampingRatio; 
    }
    
    /**
     * Sets the damping ratio of the mouse joint
     *
     * See the documentation of b2JointDef for more information on the damping ratio.
     *
     * @param  ration   the damping ratio of the mouse joint
     */
    public void setDamping(float ratio) { 
    	mouseJointDef.dampingRatio = ratio; 
    }
    
    /**
     * Returns the force multiplier of the mouse joint
     *
     * The mouse joint will move the attached fixture with a force of this value times
     * the object mass.
     *
     * @return the force multiplier of the mouse joint
     */
    public float getForce() { 
    	return force; 
    }
    
    /**
     * Sets the force multiplier of the mouse joint
     *
     * The mouse joint will move the attached fixture with a force of this value times
     * the object mass.
     *
     * @param  force    the force multiplier of the mouse joint
     */
    public void setForce(float force) { 
    	this.force = force; 
    }
    
    /**
     * Returns the size of the mouse pointer
     *
     * When a selection is made, this selector will create an axis-aligned bounding box 
     * centered at the mouse position.  Any fixture overlapping this box will be selected.  
     * The size of this box is determined by this value.
     *
     * @return the size of the mouse pointer
     */
    public Vector2 getMouseSize() { 
    	dimension.set(pointer.width, pointer.height);
    	return dimension;
	}
	
    /**
     * Sets the size of the mouse pointer
     *
     * When a selection is made, this selector will create an axis-aligned bounding box centered
     * at the mouse position.  Any fixture overlapping this box will be selected.  The size of
     * this box is determined by this value.
     *
     * @param  width	the width of the mouse pointer
     * @param  height	the height of the mouse pointer
     */
    public void setMouseSize(float width, float height) { 
    	pointer.width  = width; 
    	pointer.height = height;
    }
 
    /**
     * Creates a new ObstacleSelector for the given World
     *
     * This world can never change.  If you want a selector for a different world,
     * make a new instance.
     *
     * This constructor uses the default mouse size.
     *
     * @param  world   the physics world
     */
    public ObstacleSelector(World world) {
		this(world,DEFAULT_MSIZE,DEFAULT_MSIZE);	
    }

    /**
     * Creates a new ObstacleSelector for the given World and mouse size.
     *
     * This world can never change.  If you want a selector for a different world,
     * make a new instance.  However, the mouse size can be changed at any time.
     *
     * @param  world   the physics world
     * @param  width   the width of the mouse pointer
     * @param  height  the height of the mouse pointer
     */
    public ObstacleSelector(World world, float width, float height) {
	    this.world = world;
    
	    pointer = new Rectangle();
    	pointer.width  = width;
    	pointer.height = height;
    
    	mouseJointDef = new MouseJointDef();
    	
    	mouseJointDef.frequencyHz = DEFAULT_FREQUENCY;
    	mouseJointDef.dampingRatio = DEFAULT_DAMPING;
    	force = DEFAULT_FORCE;
    
    	BodyDef groundDef = new BodyDef();
		groundDef.type = BodyDef.BodyType.StaticBody;
		CircleShape groundShape = new CircleShape();
		groundShape.setRadius(pointer.width);
		ground = world.createBody(groundDef);
		ground.createFixture(groundShape,0);

	    if (ground != null) {
	        FixtureDef groundFixture = new FixtureDef();
        	groundFixture.shape = groundShape;
        	ground.createFixture(groundFixture);
    	}
	    
	    drawScale = new Vector2(1,1);
    }

    /**
     * Returns true if a physics body is currently selected
     *
     * @return true if a physics body is currently selected
     */
    public boolean isSelected() { 
    	return selection != null; 
    }
    
    /**
     * Returns the Obstacle selected (if any)
     *
     * Just because a physics body was selected does not mean that an Obstacle was
     * selected.  The body could be a basic Box2d body generated by other means. 
     * If the body is not an Obstacle, this method returns nullptr.
     *
     * @return the Obstacle selected (if any)
     */
    public Obstacle getObstacle() {   
    	if (selection != null) {
        	Object data = selection.getBody().getUserData();
        	try {
        		return (Obstacle)data;
        	} catch (Exception e) {
        	}
        }
        return null;
    }
    
    /**
     * Returns true if a physics body was selected at the given position.
     *
     * This method contructs and AABB the size of the mouse pointer, centered at the
     * given position.  If any part of the AABB overlaps a fixture, it is selected.
     *
     * @param  x  the x-coordinate (in physics space) to select
     * @param  y  the y-coordinate (in physics space) to select
     *
     * @return true if a physics body was selected at the given position.
     */
    public boolean select(float x, float y) {
    	pointer.x = x-pointer.width/2.0f;
    	pointer.y = y-pointer.height/2.0f;
		world.QueryAABB(this, pointer.x,pointer.y,pointer.x+pointer.width,pointer.y+pointer.height);
		if (selection != null) {
			Body body = selection.getBody();
			mouseJointDef.bodyA = ground;
			mouseJointDef.bodyB = body;
			mouseJointDef.target.set(x,y);
			mouseJointDef.frequencyHz = 5.0f;
			mouseJointDef.dampingRatio = 0.7f;
			mouseJointDef.maxForce = 1000 * body.getMass();
			mouseJoint = (MouseJoint)world.createJoint(mouseJointDef);
			body.setAwake(true);
		}
	    return selection != null; 
    }
    
    /**
     * Moves the selected body to the given position.
     *
     * @param  x  the x-coordinate (in physics space) to move to
     * @param  y  the y-coordinate (in physics space) to move to
     *
     * If nothing is selected, this method does nothing.
     */
    public void moveTo(float x, float y) {
    	position.set(x,y);
    	if (mouseJoint != null) {
    		mouseJoint.setTarget(position);
    	}
    }
    
    /**
     * Deselects the physics body, discontinuing any mouse movement.
     *
     * The body may still continue to move of its own accord.  
     */
    public void deselect() {
        if (selection != null) {
    		world.destroyJoint(mouseJoint);
    	    selection = null;
    	    mouseJoint = null;
	    }
    }
    
	//// QueryCallback
	/**  
	 * Called for each fixture found in the query AABB.
	 *
	 * The AABB is good enough, so we buffere the fixture and stop the query.
	 */
	public boolean reportFixture(Fixture fixture) {
    	selection = fixture;
    	return selection == null;
	}
	
	//// Drawing code
    /**
     * Sets the texture to display the selector on screen
     *
     * @param region  the texture to display the selector on screen
     */
    public void setTexture(TextureRegion region) {
    	texture = region;
    	origin = new Vector2(texture.getRegionWidth()/2.0f,texture.getRegionHeight()/2.0f);
    }

    /**
     * Returns the texture to display the selector on screen
     *
     * @return the texture to display the selector on screen
     */
    public TextureRegion getTexture() {
    	return texture;
    }	
    
	/**
     * Returns the drawing scale for this obstacle selector
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
	 * This method does NOT return a reference to the drawing scale. Changes to this 
	 * vector will not affect the body.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.

     * We allow for the scaling factor to be non-uniform.
     *
     * @return the drawing scale for this physics object
     */
    public Vector2 getDrawScale() { 
    	scaleCache.set(drawScale);
    	return scaleCache; 
    }
    
    /**
     * Sets the drawing scale for this obstacle selector
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param value  the drawing scale for this physics object
     */
    public void setDrawScale(Vector2 value) { 
    	setDrawScale(value.x,value.y); 
	}
    
    /**
     * Sets the drawing scale for this physics object
     *
     * The drawing scale is the number of pixels to draw before Box2D unit. Because
     * mass is a function of area in Box2D, we typically want the physics objects
     * to be small.  So we decouple that scale from the physics object.  However,
     * we must track the scale difference to communicate with the scene graph.
     *
     * We allow for the scaling factor to be non-uniform.
     *
     * @param x  the x-axis scale for this physics object
     * @param y  the y-axis scale for this physics object
     */
    public void setDrawScale(float x, float y) {
    	drawScale.set(x,y);
    }
    
	/**
	 * Draws the obstacle selector.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		if (texture != null) {
			canvas.draw(texture,Color.WHITE,origin.x,origin.y,position.x*drawScale.x,position.y*drawScale.x,0,1,1);
		}
	}
	
  
 }
