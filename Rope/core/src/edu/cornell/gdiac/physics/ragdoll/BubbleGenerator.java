/*
 * BubbleGenerator.java
 *
 * This object is only a body.  It does not have a fixture and does not collide.
 * It is a physics object so that we can weld it to the ragdoll mask.  That
 * way it always looks like bubles are coming from the snorkle, no matter
 * which way the head moves.
 *
 * This is another example of a particle system.  Like the photons in the first lab,
 * it preallocates all of its objects ahead of time.  However, this time we use
 * the built-in memory pool from LibGDX to do it.
 * 
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.ragdoll;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Physics object that generates non-physics bubble shapes.
 *
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class BubbleGenerator extends WheelObstacle {
	
	/** Representation of the bubbles for drawing purposes. */
	private class Particle implements Pool.Poolable {
		/** Position of the bubble in Box2d space */
		public Vector2 position;
		/** The number of animation frames left to live */
		public int life;
		
		/** Creates a new Particle with no lifespace */
		public Particle() {
			position = new Vector2();
			life = -1;
		}
		
		/** Resets the particle so it can be reclaimed by the pool */
		public void reset() {
			position.set(0,0);
			life = -1;
		}
	}
	
	/**
	 * Memory pool supporting the particle system.
	 *
	 * This pool preallocates all of the particles.  When a particle dies, it is
	 * released back to the pool for reuse.
	 */
	private class ParticlePool extends Pool<Particle> {
		/** That is all we got */
		private static final int MAX_PARTICLES = 6;
		/** The backing list of particles */
		private Particle[] particles;
		/** The current allocation position in the array */
		private int offset;
		
		/** 
		 * Creates a new pool to allocate Particles
		 *
		 * This constructor preallocates the objects 
		 */
		public ParticlePool() {
			super();
			particles = new Particle[MAX_PARTICLES];
			for(int ii = 0; ii < MAX_PARTICLES; ii++) {
				particles[ii] = new Particle();
			}
			offset = 0;
		}
		
		/**
		 * Returns the backing list (so that we can iterate over it)
		 *
		 * @return  the backing list
		 */
		public Particle[] getPool() {
			return particles;
		}
		
		/**
		 * Returns the next available object in the backing list
		 *
		 * If the backing list is exhausted, we return null
		 *
		 * @return the next available object in the backing list
		 */
		protected Particle newObject () {
			if (offset < particles.length) {
				offset++;
				return particles[offset-1];
			}
			return null;  // OUT OF MEMORY
		}
		
	}
	
	// Constants to define bubble frequency.
	/** How long we have left to live */
	private static final int DEFAULT_LIFE  = 250;
	/** How often we make bubbles */
	private static final int BUBBLE_TIME   = 200;
	/** The size of this generator */
	private static final float BUBBLE_SIZE = 0.1f;
	/** The density of this generator */
	private static final float BUBBLE_DENSE = 1.0f;

	// Dimensional information for drawing texture.
	/** The size dimension of a bubble */
	private Vector2 dimension;
	
	/** How long bubbles live after creation */
	private int lifespan;
	/** How long until we can make another bubble */
	private int cooldown;
	/** Whether or not we made a bubble this animation frame */
	private boolean bubbled;

	/** Cache to safely return dimension information */
	protected Vector2 sizeCache = new Vector2();
	/** Memory pool to allocate new particles */
	private ParticlePool memory;
	
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
		dimension.set(value);
	}
	
	/** 
	 * Sets the dimensions of this box
	 *
	 * @param width   The width of this box
	 * @param height  The height of this box
	 */
	public void setDimension(float width, float height) {
		dimension.set(width,height);
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
	 * Returns the lifespan of a generated bubble.
	 *
	 * @return the lifespan of a generated bubble.
	 */
	public int getLifeSpan() {
		return lifespan;
	}
	
	/**
	 * Sets the lifespan of a generated bubble.
	 *
	 * Changing this does not effect bubbles already generated.
	 *
	 * @param value the lifespan of a generated bubble.
	 */
	public void setLifeSpan(int value) {
		lifespan = value;
	}

	/**
	 * Creates a new bubble generator at the origin.
	 */
	public BubbleGenerator() {
		this(0,0);
	}

	/**
	 * Creates a new bubble generator at the given position.
	 *
	 * @param x  Initial x position of the generator center
	 * @param y  Initial y position of the generator center
	 */
	public BubbleGenerator(float x, float y) {
		super(x, y, BUBBLE_SIZE);
		setName("bubbler");
		setDensity(BUBBLE_DENSE);
		
		// Initialize
		lifespan = DEFAULT_LIFE;
		cooldown = 0;
		bubbled = false;
		memory = new ParticlePool();
	}

	/** Generates a new bubble object and put it on the screen. */
	public void bubble() {
		Particle p = memory.obtain();
		if (p != null) {
			p.position.set(getPosition());
			p.life = lifespan;
		}
	}
	
    /**
     * Returns true if we generated a bubble this animation frame.
     *
     * @return true if we generated a bubble this animation frame.
     */
    public boolean didBubble() { 
    	return bubbled; 
    }

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method for cooldowns and bubble movement.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float dt) {
		for(Particle p : memory.getPool()) {
			if (p.life > 0) {
				p.position.y += 1/drawScale.y;
				p.life -= 1;
				if (p.life == 0) {
					memory.free(p);
				}
			}
		}
		
		if (cooldown == 0) {
	        bubbled = true;
			bubble();
			cooldown = BUBBLE_TIME;
		} else {
	        bubbled = false;
			cooldown--;
		}
		super.update(dt);
	}

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		if (texture == null) {
			return;
		}
		
		for(Particle p : memory.getPool()) {
			if (p.life > 0) {
				canvas.draw(texture,Color.WHITE,origin.x,origin.y,
							p.position.x*drawScale.x,p.position.y*drawScale.y,0.0f,1,1);
			}
		}
	}
}
