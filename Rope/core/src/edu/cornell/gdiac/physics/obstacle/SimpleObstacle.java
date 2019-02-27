/*
 * SimpleObstacle.java
 *
 * This class is a subclass of Obstacle that supports only one Body.
 * it is the prime subclass of most models in the game.
 *
 * This class does not provide Shape information, and cannot be instantiated
 * directly.
 *
 * Many of the method comments in this class are taken from the Box2d manual by 
 * Erin Catto (2011).
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.obstacle;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import edu.cornell.gdiac.physics.GameCanvas;

/**
 * Base model class to support collisions.
 *
 * This is an instance of a Obstacle with just one body. It does not have any joints.
 * It is the primary type of physics object. 
 *
 * This class does not provide Shape information, and cannot be instantiated directly.
 */
public abstract class SimpleObstacle extends Obstacle {
	/** The physics body for Box2D. */
	protected Body body;

	/** The texture for the shape. */
	protected TextureRegion texture;

	/** The texture origin for drawing */
	protected Vector2 origin;
	
	/// BodyDef Methods
	/**
	 * Returns the body type for Box2D physics
	 *
	 * If you want to lock a body in place (e.g. a platform) set this value to STATIC.
	 * KINEMATIC allows the object to move (and some limited collisions), but ignores 
	 * external forces (e.g. gravity). DYNAMIC makes this is a full-blown physics object.
	 *
	 * @return the body type for Box2D physics
	 */
	public BodyType getBodyType() {
		return (body != null ? body.getType() : super.getBodyType());
	}
	
	/**
	 * Returns the body type for Box2D physics
	 *
	 * If you want to lock a body in place (e.g. a platform) set this value to STATIC.
	 * KINEMATIC allows the object to move (and some limited collisions), but ignores 
	 * external forces (e.g. gravity). DYNAMIC makes this is a full-blown physics object.
	 *
	 * @return the body type for Box2D physics
	 */
	public void setBodyType(BodyType value) {
		if (body != null) {
			body.setType(value);
		} else {
			super.setBodyType(value);
		}
	}
	
	/**
	 * Returns the current position for this physics body
	 *
	 * This method does NOT return a reference to the position vector. Changes to this 
	 * vector will not affect the body.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the current position for this physics body
	 */
	public Vector2 getPosition() {
		return (body != null ? body.getPosition() : super.getPosition());
	}
	
	/**
	 * Sets the current position for this physics body
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the current position for this physics body
	 */
	public void setPosition(Vector2 value) {
		if (body != null) {
			body.setTransform(value,body.getAngle());
		} else {
			super.setPosition(value);
		}
	}

	/**
	 * Sets the current position for this physics body
	 *
	 * @param x  the x-coordinate for this physics body
	 * @param y  the y-coordinate for this physics body
	 */
	public void setPosition(float x, float y) {
		if (body != null) {
			positionCache.set(x,y);
			body.setTransform(positionCache,body.getAngle());
		} else {
			super.setPosition(x,y);
		}
	}

	/**
	 * Returns the x-coordinate for this physics body
	 *
	 * @return the x-coordinate for this physics body
	 */
	public float getX() {
		return (body != null ? body.getPosition().x : super.getX());
	}
	
	/**
	 * Sets the x-coordinate for this physics body
	 *
	 * @param value  the x-coordinate for this physics body
	 */
	public void setX(float value) {
		if (body != null) {
			positionCache.set(value,body.getPosition().y);
			body.setTransform(positionCache,body.getAngle());
		} else {
			super.setX(value);
		}
	}
	
	/**
	 * Returns the y-coordinate for this physics body
	 *
	 * @return the y-coordinate for this physics body
	 */
	public float getY() {
		return (body != null ? body.getPosition().y : super.getY());
	}
	
	/**
	 * Sets the y-coordinate for this physics body
	 *
	 * @param value  the y-coordinate for this physics body
	 */
	public void setY(float value) {
		if (body != null) {
			positionCache.set(body.getPosition().x,value);
			body.setTransform(positionCache,body.getAngle());
		} else {
			super.setY(value);
		}
	}
	
	/**
	 * Returns the angle of rotation for this body (about the center).
	 *
	 * The value returned is in radians
	 *
	 * @return the angle of rotation for this body
	 */
	public float getAngle() {
		return (body != null ? body.getAngle() : super.getAngle());
	}
	
	/**
	 * Sets the angle of rotation for this body (about the center).
	 *
	 * @param value  the angle of rotation for this body (in radians)
	 */
	public void setAngle(float value) {
		if (body != null) {
			body.setTransform(body.getPosition(),value);
		} else {
			super.setAngle(value);
		}
	}
	
	/**
	 * Returns the linear velocity for this physics body
	 *
	 * This method does NOT return a reference to the velocity vector. Changes to this 
	 * vector will not affect the body.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the linear velocity for this physics body
	 */
	public Vector2 getLinearVelocity() {
		return (body != null ? body.getLinearVelocity() : super.getLinearVelocity());
	}
	
	/**
	 * Sets the linear velocity for this physics body
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the linear velocity for this physics body
	 */
	public void setLinearVelocity(Vector2 value) {
		if (body != null) {
			body.setLinearVelocity(value);
		} else {
			super.setLinearVelocity(value);
		}
	}
	
	/**
	 * Returns the x-velocity for this physics body
	 *
	 * @return the x-velocity for this physics body
	 */
	public float getVX() {
		return (body != null ? body.getLinearVelocity().x : super.getVX());
	}
	
	/**
	 * Sets the x-velocity for this physics body
	 *
	 * @param value  the x-velocity for this physics body
	 */
	public void setVX(float value) {
		if (body != null) {
			velocityCache.set(value,body.getLinearVelocity().y);
			body.setLinearVelocity(velocityCache);
		} else {
			super.setVX(value);
		}
	}
	
	/**
	 * Returns the y-velocity for this physics body
	 *
	 * @return the y-velocity for this physics body
	 */
	public float getVY() {
		return (body != null ? body.getLinearVelocity().y : super.getVY());
	}
	
	/**
	 * Sets the y-velocity for this physics body
	 *
	 * @param value  the y-velocity for this physics body
	 */
	public void setVY(float value) {
		if (body != null) {
			velocityCache.set(body.getLinearVelocity().x,value);
			body.setLinearVelocity(velocityCache);
		} else {
			super.setVY(value);
		}
	}
	
	/**
	 * Returns the angular velocity for this physics body
	 *
	 * The rate of change is measured in radians per step
	 *
	 * @return the angular velocity for this physics body
	 */
	public float getAngularVelocity() {
		return (body != null ? body.getAngularVelocity() : super.getAngularVelocity());
	}
	
	/**
	 * Sets the angular velocity for this physics body
	 *
	 * @param value the angular velocity for this physics body (in radians)
	 */
	public void setAngularVelocity(float value) {
		if (body != null) {
			body.setAngularVelocity(value);
		} else {
			super.setAngularVelocity(value);
		}
	}
	
	/**
	 * Returns true if the body is active
	 *
	 * An inactive body not participate in collision or dynamics. This state is similar 
	 * to sleeping except the body will not be woken by other bodies and the body's 
	 * fixtures will not be placed in the broad-phase. This means the body will not 
	 * participate in collisions, ray casts, etc.
	 *
	 * @return true if the body is active
	 */
	public boolean isActive() {
		return (body != null ? body.isActive() : super.isActive());
	}
	
	/**
	 * Sets whether the body is active
	 *
	 * An inactive body not participate in collision or dynamics. This state is similar 
	 * to sleeping except the body will not be woken by other bodies and the body's 
	 * fixtures will not be placed in the broad-phase. This means the body will not 
	 * participate in collisions, ray casts, etc.
	 *
	 * @param value  whether the body is active
	 */
	public void setActive(boolean value) {
		if (body != null) {
			body.setActive(value);
		} else {
			super.setActive(value);
		}
	}
	
	/**
	 * Returns true if the body is awake
	 *
	 * An sleeping body is one that has come to rest and the physics engine has decided
	 * to stop simulating it to save CPU cycles. If a body is awake and collides with a 
	 * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a 
	 * joint or contact attached to them is destroyed.  You can also wake a body manually.
	 *
	 * @return true if the body is awake
	 */
	public boolean isAwake() {
		return (body != null ? body.isAwake() : super.isAwake());
	}
	
	/**
	 * Sets whether the body is awake
	 *
	 * An sleeping body is one that has come to rest and the physics engine has decided
	 * to stop simulating it to save CPU cycles. If a body is awake and collides with a 
	 * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a 
	 * joint or contact attached to them is destroyed.  You can also wake a body manually.
	 *
	 * @param value  whether the body is awake
	 */
	public void setAwake(boolean value) {
		if (body != null) {
			body.setAwake(value);
		} else {
			super.setAwake(value);
		}
	}
	
	/**
	 * Returns false if this body should never fall asleep
	 *
	 * An sleeping body is one that has come to rest and the physics engine has decided
	 * to stop simulating it to save CPU cycles. If a body is awake and collides with a 
	 * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a 
	 * joint or contact attached to them is destroyed.  You can also wake a body manually.
	 *
	 * @return false if this body should never fall asleep
	 */
	public boolean isSleepingAllowed() {
		return (body != null ? body.isSleepingAllowed() : super.isSleepingAllowed());
	}
	
	/**
	 * Sets whether the body should ever fall asleep
	 *
	 * An sleeping body is one that has come to rest and the physics engine has decided
	 * to stop simulating it to save CPU cycles. If a body is awake and collides with a 
	 * sleeping body, then the sleeping body wakes up. Bodies will also wake up if a 
	 * joint or contact attached to them is destroyed.  You can also wake a body manually.
	 *
	 * @param value  whether the body should ever fall asleep
	 */
	public void setSleepingAllowed(boolean value) {
		if (body != null) {
			body.setSleepingAllowed(value);
		} else {
			super.setSleepingAllowed(value);
		}
	}
	
	/**
	 * Returns true if this body is a bullet 
	 *
	 * By default, Box2D uses continuous collision detection (CCD) to prevent dynamic 
	 * bodies from tunneling through static bodies. Normally CCD is not used between 
	 * dynamic bodies. This is done to keep performance reasonable. In some game 
	 * scenarios you need dynamic bodies to use CCD. For example, you may want to shoot
	 * a high speed bullet at a stack of dynamic bricks. Without CCD, the bullet might
	 * tunnel through the bricks.
	 *
	 * Fast moving objects in Box2D can be labeled as bullets. Bullets will perform CCD 
	 * with both static and dynamic bodies. You should decide what bodies should be 
	 * bullets based on your game design.
	 *
	 * @return true if this body is a bullet 
	 */
	public boolean isBullet() {
		return (body != null ? body.isBullet() : super.isBullet());
	}
	
	/**
	 * Sets whether this body is a bullet 
	 *
	 * By default, Box2D uses continuous collision detection (CCD) to prevent dynamic 
	 * bodies from tunneling through static bodies. Normally CCD is not used between 
	 * dynamic bodies. This is done to keep performance reasonable. In some game 
	 * scenarios you need dynamic bodies to use CCD. For example, you may want to shoot
	 * a high speed bullet at a stack of dynamic bricks. Without CCD, the bullet might
	 * tunnel through the bricks.
	 *
	 * Fast moving objects in Box2D can be labeled as bullets. Bullets will perform CCD 
	 * with both static and dynamic bodies. You should decide what bodies should be 
	 * bullets based on your game design.
	 *
	 * @param value  whether this body is a bullet 
	 */
	public void setBullet(boolean value) {
		if (body != null) {
			body.setBullet(value);
		} else {
			super.setBullet(value);
		}
	}
	
	/**
	 * Returns true if this body be prevented from rotating
	 *
	 * This is very useful for characters that should remain upright.
	 *
	 * @return true if this body be prevented from rotating
	 */
	public boolean isFixedRotation() {
		return (body != null ? body.isFixedRotation() : super.isFixedRotation());
	}
	
	/**
	 * Sets whether this body be prevented from rotating
	 *
	 * This is very useful for characters that should remain upright.
	 *
	 * @param value  whether this body be prevented from rotating
	 */
	public void setFixedRotation(boolean value) {
		if (body != null) {
			body.setFixedRotation(value);
		} else {
			super.setFixedRotation(value);
		}
	}
	
	/**
	 * Returns the gravity scale to apply to this body
	 *
	 * This allows isolated objects to float.  Be careful with this, since increased 
	 * gravity can decrease stability.
	 *
	 * @return the gravity scale to apply to this body
	 */
	public float getGravityScale() {
		return (body != null ? body.getGravityScale() : super.getGravityScale());
	}
	
	/**
	 * Sets the gravity scale to apply to this body
	 *
	 * This allows isolated objects to float.  Be careful with this, since increased 
	 * gravity can decrease stability.
	 *
	 * @param value  the gravity scale to apply to this body
	 */
	public void setGravityScale(float value) {
		if (body != null) {
			body.setGravityScale(value);
		} else {
			super.setGravityScale(value);
		}
	}
	
	/** 
	 * Returns the linear damping for this body.
	 *
	 * Linear damping is use to reduce the linear velocity. Damping is different than 
	 * friction because friction only occurs with contact. Damping is not a replacement 
	 * for friction and the two effects should be used together.
	 *
	 * Damping parameters should be between 0 and infinity, with 0 meaning no damping, 
	 * and infinity meaning full damping. Normally you will use a damping value between 
	 * 0 and 0.1. Most people avoid linear damping because it makes bodies look floaty.
	 *
	 * @return the linear damping for this body.
	 */
	public float getLinearDamping() {
		return (body != null ? body.getLinearDamping() : super.getLinearDamping());
	}
	
	/** 
	 * Sets the linear damping for this body.
	 *
	 * Linear damping is use to reduce the linear velocity. Damping is different than 
	 * friction because friction only occurs with contact. Damping is not a replacement 
	 * for friction and the two effects should be used together.
	 *
	 * Damping parameters should be between 0 and infinity, with 0 meaning no damping, 
	 * and infinity meaning full damping. Normally you will use a damping value between 
	 * 0 and 0.1. Most people avoid linear damping because it makes bodies look floaty.
	 *
	 * @param value  the linear damping for this body.
	 */
	public void setLinearDamping(float value) {
		if (body != null) {
			body.setLinearDamping(value);
		} else {
			super.setLinearDamping(value);
		}
	}
	
	/** 
	 * Returns the angular damping for this body.
	 *
	 * Angular damping is use to reduce the angular velocity. Damping is different than 
	 * friction because friction only occurs with contact. Damping is not a replacement 
	 * for friction and the two effects should be used together.
	 *
	 * Damping parameters should be between 0 and infinity, with 0 meaning no damping, 
	 * and infinity meaning full damping. Normally you will use a damping value between 
	 * 0 and 0.1.
	 *
	 * @return the angular damping for this body.
	 */
	public float getAngularDamping() {
		return (body != null ? body.getAngularDamping() : super.getAngularDamping());
	}

	/** 
	 * Sets the angular damping for this body.
	 *
	 * Angular damping is use to reduce the angular velocity. Damping is different than 
	 * friction because friction only occurs with contact. Damping is not a replacement 
	 * for friction and the two effects should be used together.
	 *
	 * Damping parameters should be between 0 and infinity, with 0 meaning no damping, 
	 * and infinity meaning full damping. Normally you will use a damping value between 
	 * 0 and 0.1.
	 *
	 * @param value  the angular damping for this body.
	 */
	public void setAngularDamping(float value) {
		if (body != null) {
			body.setAngularDamping(value);
		} else {
			super.setAngularDamping(value);
		}
	}
	
	/// FixtureDef Methods
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
		super.setDensity(value);
		if (body != null) {
			for(Fixture f : body.getFixtureList()) {
				f.setDensity(value);
			}
		}
	}
	
	/**
	 * Sets the friction coefficient of this body
	 *
	 * The friction parameter is usually set between 0 and 1, but can be any non-negative 
	 * value. A friction value of 0 turns off friction and a value of 1 makes the friction 
	 * strong. When the friction force is computed between two shapes, Box2D must combine 
	 * the friction parameters of the two parent fixtures. This is done with the geometric 
	 * mean.
	 *
	 * @param value  the friction coefficient of this body
	 */
	public void setFriction(float value) {
		super.setFriction(value);
		if (body != null) {
			for(Fixture f : body.getFixtureList()) {
				f.setFriction(value);
			}
		}
	}
	
	/**
	 * Sets the restitution of this body
	 *
	 * Restitution is used to make objects bounce. The restitution value is usually set 
	 * to be between 0 and 1. Consider dropping a ball on a table. A value of zero means 
	 * the ball won't bounce. This is called an inelastic collision. A value of one means 
	 * the ball's velocity will be exactly reflected. This is called a perfectly elastic 
	 * collision.
	 *
	 * @param value  the restitution of this body
	 */
	public void setRestitution(float value) {
		super.setRestitution(value);
		if (body != null) {
			for(Fixture f : body.getFixtureList()) {
				f.setRestitution(value);
			}
		}
	}
	
	/**
	 * Sets whether this object is a sensor.
	 *
	 * Sometimes game logic needs to know when two entities overlap yet there should be 
	 * no collision response. This is done by using sensors. A sensor is an entity that 
	 * detects collision but does not produce a response.
	 *
	 * @param value  whether this object is a sensor.
	 */
	public void setSensor(boolean value) {
		super.setSensor(value);
		if (body != null) {
			for(Fixture f : body.getFixtureList()) {
				f.setSensor(value);
			}
		}
	}
	
	/**
	 * Sets the filter data for this object
	 *
	 * Collision filtering allows you to prevent collision between fixtures. For example, 
	 * say you make a character that rides a bicycle. You want the bicycle to collide 
	 * with the terrain and the character to collide with the terrain, but you don't want 
	 * the character to collide with the bicycle (because they must overlap). Box2D 
	 * supports such collision filtering using categories and groups.
	 *
	 * A value of null removes all collision filters.
	 * 
	 * @param value  the filter data for this object
	 */
	public void setFilterData(Filter value) {
		super.setFilterData(value);
		if (body != null) {
			for(Fixture f : body.getFixtureList()) {
				f.setFilterData(value);
			}
		}
	}

	/// MassData Methods
	/**
	 * Returns the center of mass of this body
	 *
	 * This method does NOT return a reference to the centroid position. Changes to this 
	 * vector will not affect the body.  However, it returns the same vector each time
	 * its is called, and so cannot be used as an allocator.
	 *
	 * @return the center of mass for this physics body
	 */
	public Vector2 getCentroid() {
		return  (body != null ? body.getLocalCenter() : super.getCentroid());
	}
	
	/**
	 * Sets the center of mass for this physics body
	 *
	 * This method does not keep a reference to the parameter.
	 *
	 * @param value  the center of mass for this physics body
	 */
	public void setCentroid(Vector2 value) {
		super.setCentroid(value);
		if (body != null) {
			body.setMassData(massdata); // Protected accessor?
		}
	}
	
	/**
	 * Returns the rotational inertia of this body
	 * 
	 * For static bodies, the mass and rotational inertia are set to zero. When 
	 * a body has fixed rotation, its rotational inertia is zero.
	 *
	 * @return the rotational inertia of this body
	 */
	public float getInertia() {
		return  (body != null ? body.getInertia() : super.getInertia());
	}

	/**
	 * Sets the rotational inertia of this body
	 * 
	 * For static bodies, the mass and rotational inertia are set to zero. When 
	 * a body has fixed rotation, its rotational inertia is zero.
	 *
	 * @param value  the rotational inertia of this body
	 */
	public void setInertia(float value) {
		super.setInertia(value);
		if (body != null) {
			body.setMassData(massdata); // Protected accessor?
		}
	}
	
	/** 
	 * Returns the mass of this body
	 * 
	 * The value is usually in kilograms.
	 *
	 * @return the mass of this body
	 */
	public float getMass() {
		return  (body != null ? body.getMass() : super.getMass());
	}
	
	/** 
	 * Sets the mass of this body
	 * 
	 * The value is usually in kilograms.
	 *
	 * @param value  the mass of this body
	 */
	public void setMass(float value) {
		super.setMass(value);
		if (body != null) {
			body.setMassData(massdata); // Protected accessor?
		}
	}
	
	/**
	 * Resets this body to use the mass computed from the its shape and density
	 */
	public void resetMass() {
		super.resetMass();
		if (body != null) {
			body.resetMassData();
		}
	}
	
	/// Texture Information
	/**
	 * Returns the object texture for drawing purposes.
	 *
	 * In order for drawing to work properly, you MUST set the drawScale.
	 * The drawScale converts the physics units to pixels.
	 * 
	 * @return the object texture for drawing purposes.
	 */
	public TextureRegion getTexture() {
		return texture;
	}
	
	/**
	 * Sets the object texture for drawing purposes.
	 *
	 * In order for drawing to work properly, you MUST set the drawScale.
	 * The drawScale converts the physics units to pixels.
	 * 
	 * @param value  the object texture for drawing purposes.
	 */
	public void setTexture(TextureRegion value) {
		texture = value;
		origin.set(texture.getRegionWidth()/2.0f, texture.getRegionHeight()/2.0f);
	}
	
	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		if (texture != null) {
			canvas.draw(texture,Color.WHITE,origin.x,origin.y,getX()*drawScale.x,getY()*drawScale.x,getAngle(),1,1);
		}
	}
	
	/**
	 * Returns the Box2D body for this object.
	 *
	 * You use this body to add joints and apply forces.
	 *
	 * @return the Box2D body for this object.
	 */
	public Body getBody() {
		return body;
	}
	
	/**
	 * Creates a new simple physics object at the origin.
	 * 
	 * REMEMBER: The size is in physics units, not pixels.
	 */
	protected SimpleObstacle() {
		this(0,0);
	}
	
	/**
	 * Creates a new simple physics object
	 * 
	 * @param x  Initial x position in world coordinates
	 * @param y  Initial y position in world coordinates
	 */
	protected SimpleObstacle(float x, float y) {
		super(x,y);
		origin = new Vector2();
		body = null;
	}
	
	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * Implementations of this method should NOT retain a reference to World.  
	 * That is a tight coupling that we should avoid.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// Make a body, if possible
		bodyinfo.active = true;
		body = world.createBody(bodyinfo);
		body.setUserData(this);
		
		// Only initialize if a body was created.
		if (body != null) {
			createFixtures();
			return true;
		} 
		
		bodyinfo.active = false;
		return false;
	}
	
	/**
	 * Destroys the physics Body(s) of this object if applicable,
	 * removing them from the world.
	 * 
	 * @param world Box2D world that stores body
	 */
	public void deactivatePhysics(World world) {
		// Should be good for most (simple) applications.
		if (body != null) {
			// Snapshot the values
			setBodyState(body);
			world.destroyBody(body);
			body = null;
			bodyinfo.active = false;
		}
	}

	/**
	 * Create new fixtures for this body, defining the shape
	 *
	 * This is the primary method to override for custom physics objects
	 */
	protected abstract void createFixtures();

    /**
     * Release the fixtures for this body, reseting the shape
     *
     * This is the primary method to override for custom physics objects.
     */
	protected abstract void releaseFixtures();
    
	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * This method is called AFTER the collision resolution state. Therefore, it 
	 * should not be used to process actions or any other gameplay information.  Its 
	 * primary purpose is to adjust changes to the fixture, which have to take place 
	 * after collision.
	 *
	 * @param dt Timing values from parent loop
	 */
	public void update(float delta) {
		// Recreate the fixture object if dimensions changed.
		if (isDirty()) {
			createFixtures();
		}
	}
}