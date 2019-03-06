/*
 * CapsuleObstacle.java
 *
 *  This class implements a capsule physics object. A capsule is a box with semicircular
 *  ends along the major axis.  They are a popular physics objects, particularly for
 *  character avatars.  The rounded ends means they are less likely to snag, and they
 *  naturally fall off platforms when they go too far.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.obstacle;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;  // For GameCanvas

/**
 * Box-shaped model to support collisions.
 *
 * Unless otherwise specified, the center of mass is as the center.
 */
public class CapsuleObstacle extends SimpleObstacle {
	/** Epsilon factor to prevent issues with the fixture seams */
	private static final float DEFAULT_EPSILON = 0.01f;
	
    /** Enum to specify the capsule orientiation */
    public enum Orientation {
        /** A half-capsule with a rounded end at the top */
        TOP,
        /** A full capsule with a rounded ends at the top and bottom */
        VERTICAL,
        /** A half-capsule with a rounded end at the bottom */
        BOTTOM,
        /** A half-capsule with a rounded end at the left */
        LEFT,
        /** A full capsule with a rounded ends at the left and right */
        HORIZONTAL,
        /** A half-capsule with a rounded end at the right */
        RIGHT
    };
    
	/** Shape information for this box */
	protected PolygonShape shape;
	/** Shape information for the end cap */
    protected CircleShape end1;
	/** Shape information for the end cap */
    protected CircleShape end2;
    /** Rectangle representation of capsule core for fast computation */
    protected Rectangle center;

	/** The width and height of the box */
	private Vector2 dimension;
	/** A cache value for when the user wants to access the dimensions */
	private Vector2 sizeCache;
    /** A cache value for the center fixture (for resizing) */
	private Fixture core;
    /** A cache value for the first end cap fixture (for resizing) */
    private Fixture cap1;
    /** A cache value for the second end cap fixture (for resizing) */
    private Fixture cap2;
    /** The capsule orientation */
    private Orientation orient;
	/** Cache of the polygon vertices (for resizing) */
	private float[] vertices;
	
	/** A cache value for computing fixtures */
	private Vector2 posCache = new Vector2();
    /** The seam offset of the core rectangle */
    private float seamEpsilon;

	/** 
	 * Returns the dimensions of this box
	 *
	 * This method does NOT return a reference to the dimension vector. Changes to this 
	 * vector will not affect the shape.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the dimensions of this box
	 */
	public Vector2 getDimension() {
		return sizeCache.set(dimension);
	}

	/** 
	 * Sets the dimensions of this box
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the dimensions of this box
	 */
	public void setDimension(Vector2 value) {
		setDimension(value.x, value.y);
	}
	
	/** 
	 * Sets the dimensions of this box
	 *
	 * @param width   The width of this box
	 * @param height  The height of this box
	 */
	public void setDimension(float width, float height) {
		dimension.set(width, height);
		markDirty(true);
		resize(width, height);
	}
	
	/**
	 * Returns the box width
	 *
	 * @return the box width
	 */
	public float getWidth() {
		return dimension.x;
	}
	
	/**
	 * Sets the box width
	 *
	 * @param value  the box width
	 */
	public void setWidth(float value) {
		sizeCache.set(value,dimension.y);
		setDimension(sizeCache);
	}
	
	/**
	 * Returns the box height
	 *
	 * @return the box height
	 */
	public float getHeight() {
		return dimension.y;
	}
	
	/**
	 * Sets the box height
	 *
	 * @param value  the box height
	 */
	public void setHeight(float value) {
		sizeCache.set(dimension.x,value);
		setDimension(sizeCache);
	}
	
    /**
     * Returns the orientation of this capsule
     *
     * @return the orientation of this capsule
     */
    public Orientation getOrientation() { 
    	return orient; 
    }
    
    /**
     * Sets the orientation of this capsule, if valid.
     *
     * If the orientation is not valid, then nothing happens and the method
     * return false.
     *
     * @param value  the orientation of this capsule
     *
     * @return true if the orientation was successfully changed.
     */
    public boolean setOrientation(Orientation value) {
		if (dimension.x < dimension.y && isHorizontal(value)) {
			return false;
		}

    	orient = value;
    	resize(dimension.x,dimension.y);
    	return true;
    }
    
    /**
     * Returns true if the orientation is a horizontal full or half capsule.
     * 
     * @param value the orientation to check
     * 
     * @return true if the orientation is a horizontal full or half capsule.
     */
    private boolean isHorizontal(Orientation value) {
    	return (value == Orientation.LEFT || value == Orientation.RIGHT || value == Orientation.HORIZONTAL);
    }

    /**
     * Sets the seam offset of the core rectangle
     *
     * If the center rectangle is exactly the same size as the circle radius,
     * you may get catching at the seems.  To prevent this, you should make 
     * the center rectangle epsilon narrower so that everything rolls off the
     * round shape. This parameter is that epsilon value
     *
     * @parm  value the seam offset of the core rectangle
     */
    public void setSeamOffset(float value) { 
    	seamEpsilon = value; 
    	markDirty(true); 
    }

    /**
     * Returns the seam offset of the core rectangle
     *
     * If the center rectangle is exactly the same size as the circle radius,
     * you may get catching at the seems.  To prevent this, you should make
     * the center rectangle epsilon narrower so that everything rolls off the
     * round shape. This parameter is that epsilon value
     *
     * @return the seam offset of the core rectangle
     */
    public float getSeamOffset() { 
    	return seamEpsilon; 
    }
	
	/**
	 * Creates a new box at the origin.
	 *
     * The orientation of the capsule will be a full capsule along the 
     * major axis.  If width == height, it will default to a vertical 
     * orientation.
     * 
	 * The size is expressed in physics units NOT pixels.  In order for 
	 * drawing to work properly, you MUST set the drawScale. The drawScale 
	 * converts the physics units to pixels.
	 *
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public CapsuleObstacle(float width, float height) {
	    this(0,0,width,height,
		    (width > height ? Orientation.HORIZONTAL : Orientation.VERTICAL));
	}

	/**
	 * Creates a new capsule object.
	 *
     * The orientation of the capsule will be a full capsule along the 
     * major axis.  If width == height, it will default to a vertical 
     * orientation.
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
	public CapsuleObstacle(float x, float y, float width, float height) {
	    this(x,y,width,height,
	    	(width > height ? Orientation.HORIZONTAL : Orientation.VERTICAL));
	}
	
	/**
	 * Creates a new capsule object width the given orientation.
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
	public CapsuleObstacle(float x, float y, float width, float height, Orientation o) {
		super(x,y);
		dimension = new Vector2();
		sizeCache = new Vector2();
		shape = new PolygonShape();
		end1 = new CircleShape();
		end2 = new CircleShape();
		center = new Rectangle();
		vertices = new float[8];

		core = null;
	    cap1 = null;
	    cap2 = null;
	    orient = o;
	    seamEpsilon = DEFAULT_EPSILON;

		// Initialize
		resize(width, height);	
	}
	
	/**
	 * Reset the polygon vertices in the shape to match the dimension.
	 */
	private void resize(float width, float height) {    
		dimension.set(width,height);
		if (width < height && isHorizontal(orient)) {
			orient = Orientation.VERTICAL; // OVERRIDE
        }
        
        // Get an AABB for the core
		center.x =  -width/2.0f;
		center.y =  -height/2.0f;
		center.width  = width;
		center.height = height;
        
        // Now adjust the core
        float r = 0;
        switch (orient) {
        case TOP:
        	r = width/2.0f;
        	center.height -= r;
        	center.x += 2*seamEpsilon;
        	center.width -= 2*seamEpsilon;
        	break;
        case VERTICAL:
        	r = width/2.0f;
            center.y += r;
        	center.height -= 2*r;
            center.x += seamEpsilon;
            center.width -= 2*seamEpsilon;
            break;
        case BOTTOM:
        	r = width/2.0f;
        	center.y += r;
        	center.height -= r;
            center.x += seamEpsilon;
            center.width -= 2*seamEpsilon;
            break;
        case LEFT:
        	r = height/2.0f;
        	center.width -= r;
            center.y += seamEpsilon;
            center.height -= 2*seamEpsilon;
            break;
        case HORIZONTAL:
        	r = height/2.0f;
            center.x += r;
        	center.width -= 2*r;
            center.y += seamEpsilon;
            center.height -= 2*seamEpsilon;
            break;
        case RIGHT:
        	r = height/2.0f;
        	center.x += r;
        	center.width -= r;
        	center.y += seamEpsilon;
        	center.height -= 2*seamEpsilon;
        	break;
        }
        
		// Make the box with the center in the center
		vertices[0] = center.x;
		vertices[1] = center.y;
		vertices[2] = center.x;
		vertices[3] = center.y+center.height;
		vertices[4] = center.x+center.width;
		vertices[5] = center.y+center.height;
		vertices[6] = center.x+center.width;
		vertices[7] = center.y;
		shape.set(vertices);
		end1.setRadius(r);
		end2.setRadius(r);
	}
	
	
	
	/**
	 * Sets the density of this body
	 *
	 * The density is typically measured in usually in kg/m^2. The density can be zero or
	 * positive. You should generally use similar densities for all your fixtures. This
	 * will improve stacking stability.
	 *
	 * @param value  the density of this body
	 */
	public void setDensity(float value) {
	    fixture.density = value;
	    if (body != null) {
	        core.setDensity(value);
	        cap1.setDensity(value/2.0f);
	        cap2.setDensity(value/2.0f);
	        if (!masseffect) {
	            body.resetMassData();
	        }
	    }
	}

	/**
	 * Create new fixtures for this body, defining the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void createFixtures() {
		if (body == null) {
			return;
		}
		    
		releaseFixtures();
		    
		// Create the fixture
		fixture.shape = shape;
		core = body.createFixture(fixture);
		    
		fixture.density = fixture.density/2.0f;
		posCache.set(0,0);
		switch (orient) {
		case TOP:
			posCache.y = center.y+center.height;
			end1.setPosition(posCache);
			fixture.shape = end1;
			cap1 = body.createFixture(fixture);
			cap2 = null;
			break;
		case VERTICAL:
			posCache.y = center.y+center.height;
			end1.setPosition(posCache);
			fixture.shape = end1;
			cap1 = body.createFixture(fixture);
			posCache.y = center.y;
			end2.setPosition(posCache);
			fixture.shape = end2;
			cap2 = body.createFixture(fixture);
			break;
		case BOTTOM:
			cap1 = null;
			posCache.y = center.y;
			end2.setPosition(posCache);
			fixture.shape = end2;
			cap2 = body.createFixture(fixture);
			break;
		case LEFT:
			posCache.x = center.x;
			end1.setPosition(posCache);
			fixture.shape = end1;
			cap1 = body.createFixture(fixture);
			cap2 = null;		
			break;
		case HORIZONTAL:
			posCache.x = center.x;
			end1.setPosition(posCache);
			fixture.shape = end1;
			cap1 = body.createFixture(fixture);
			posCache.x = center.x+center.width;
			end2.setPosition(posCache);
			fixture.shape = end2;
			cap2 = body.createFixture(fixture);
			break;
		case RIGHT:
			cap1 = null;
			posCache.x = center.x+center.width;
			end2.setPosition(posCache);
			fixture.shape = end2;
			cap2 = body.createFixture(fixture);
			break;
		}

		markDirty(false);
	}

	/**
	 * Release the fixtures for this body, reseting the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected void releaseFixtures() {
	    if (core != null) {
	        body.destroyFixture(core);
	        core = null;
	    }
	    if (cap1 != null) {
	        body.destroyFixture(cap1);
	        cap1 = null;
	    }
	    if (cap2 != null) {
	        body.destroyFixture(cap2);
	        cap2 = null;
	    }
	}
	
	/**
	 * Draws the outline of the physics body.
	 *
	 * This method can be helpful for understanding issues with collisions.
	 *
	 * @param canvas Drawing context
	 */
	public void drawDebug(GameCanvas canvas) {
		canvas.drawPhysics(shape,Color.YELLOW,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
		if (cap1 != null) {
			// Need to manually rotate caps off axis
			float dx; float dy;
			if (isHorizontal(orient)) {
				float r = -center.x;
				dx = (float)(r*Math.cos(Math.PI+getAngle()));
				dy = (float)(r*Math.sin(Math.PI+getAngle()));
			} else {
				float r = center.y+center.height;
				dx = (float)(r*Math.cos(Math.PI/2.0f+getAngle()));
				dy = (float)(r*Math.sin(Math.PI/2.0f+getAngle()));
			}
			canvas.drawPhysics(end1,Color.YELLOW,getX()+dx,getY()+dy,drawScale.x,drawScale.y);			
		}
		if (cap2 != null) {
			// Need to manually rotate caps off axis
			float dx; float dy;
			if (isHorizontal(orient)) {
				float r = center.x+center.width;
				dx = (float)(r*Math.cos(getAngle()));
				dy = (float)(r*Math.sin(getAngle()));
			} else {
				float r = -center.y;
				dx = (float)(r*Math.cos(-Math.PI/2.0f+getAngle()));
				dy = (float)(r*Math.sin(-Math.PI/2.0f+getAngle()));
			}
			canvas.drawPhysics(end2,Color.YELLOW,getX()+dx,getY()+dy,drawScale.x,drawScale.y);			
		}
	}

}