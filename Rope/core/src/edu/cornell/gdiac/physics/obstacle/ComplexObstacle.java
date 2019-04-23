/*
 * ComplexObstacle.java
 *
 * This class is a subclass of PhysicsObject that supports mutliple Bodies.
 * This is the base class for objects that are tied together with joints.
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

import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import edu.cornell.gdiac.physics.*;  // For GameCanvas

/**
 * Composite model class to support collisions.
 *
 * ComplexObstacle instances are built of many bodies, and are assumed to be connected
 * by joints (though this is not actually a requirement). This is the class to use for 
 * chains, ropes, levers, and so on. This class does not provide Shape information, and 
 * cannot be instantiated directly.
 *
 * ComplexObstacle is a hierarchical class.  It groups children as Obstacles, not bodies.
 * So you could have a ComplexObstacle made up of other ComplexObstacles.  However, it
 * also has a root body which may or may not be attached to the other bodies in the
 * hierarchy. All of the physics methods in the class apply to the root, not the body.
 * To move the other bodies, they should either be iterated over directly, or attached
 * to the root via a joint.
 */
public abstract class ComplexObstacle extends Obstacle {
    /** A root body for this box 2d. */
    protected Body body;
	/** A complex physics object has multiple bodies */
	protected Array<Obstacle> bodies;
	/** Potential joints for connecting the multiple bodies */
	protected Array<Joint> joints;
	
	/// BodyDef Methods
	/**
	 * Returns the body type for Box2D physics
	 *
	 * If you want to lock a body in place (e.g. a platform) set this value to STATIC.
	 * KINEMATIC allows the object to move (and some limited collisions), but ignores 
	 * external forces (e.g. gravity). DYNAMIC makes this is a full-blown physics object.
	 *
	 * This method returns the body type for the root object of this composite structure.
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method does not keep a reference to the parameter.
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 * @return the x-coordinate for this physics body
	 */
	public float getX() {
		return (body != null ? body.getPosition().x : super.getX());
	}
	
	/**
	 * Sets the x-coordinate for this physics body
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 * @return the y-coordinate for this physics body
	 */
	public float getY() {
		return (body != null ? body.getPosition().y : super.getY());
	}
	
	/**
	 * Sets the y-coordinate for this physics body
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 * @return the angle of rotation for this body
	 */
	public float getAngle() {
		return (body != null ? body.getAngle() : super.getAngle());
	}
	
	/**
	 * Sets the angle of rotation for this body (about the center).
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 * @return the x-velocity for this physics body
	 */
	public float getVX() {
		return (body != null ? body.getLinearVelocity().x : super.getVX());
	}
	
	/**
	 * Sets the x-velocity for this physics body
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 * @return the y-velocity for this physics body
	 */
	public float getVY() {
		return (body != null ? body.getLinearVelocity().y : super.getVY());
	}
	
	/**
	 * Sets the y-velocity for this physics body
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 * @return the angular velocity for this physics body
	 */
	public float getAngularVelocity() {
		return (body != null ? body.getAngularVelocity() : super.getAngularVelocity());
	}
	
	/**
	 * Sets the angular velocity for this physics body
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
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
	 *
	 * This method affects the root body of this composite structure only.  If you want
	 * to set the value for any of the child obstacles, iterate over the children.
	 *
	 */
	public void resetMass() {
		super.resetMass();
		if (body != null) {
			body.resetMassData();
		}
	}

	/// Physics Bodies
	/**
	 * Returns the Box2D body for this object.
	 *
	 * This method only returrns the root body in this composite structure.  For more
	 * fine-grain control, you should use the iterator methods.
	 *
	 * @return the Box2D body for this object.
	 */
	public Body getBody() {
		return (bodies.size > 0 ? bodies.get(0).getBody() : null);
	}
	
	/** 
	 * Returns the collection of component physics objects.
	 *
	 * While the iterable does not allow you to modify the list, it is possible to
	 * modify the individual objects.
	 *
	 * @return the collection of component physics objects.
	 */
	 public Iterable<Obstacle> getBodies() {
	 	return bodies;
	 }

	/** 
	 * Returns the collection of joints for this object (may be empty).
	 *
	 * While the iterable does not allow you to modify the list, it is possible to
	 * modify the individual joints.
	 *
	 * @return the collection of joints for this object.
	 */
	 public Iterable<Joint> getJoints() {
	 	return joints;
	 }

	/**
	 * Creates a new complex physics object at the origin.
	 */
	protected ComplexObstacle() {
		this(0,0);
	}
	
	/**
	 * Creates a new complex physics object
	 * 
	 * The position is the position of the root object.
	 * 
	 * @param x  Initial x position in world coordinates
	 * @param y  Initial y position in world coordinates
	 */
	protected ComplexObstacle(float x, float y) {
		super(x,y);
		bodies = new Array<Obstacle>();
		joints = new Array<Joint>();
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method invokes ActivatePhysics for the individual PhysicsObjects
	 * in the list. It also calls the internal method createJoints() to 
	 * link them all together. You should override that method, not this one, 
	 * for specific physics objects.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		bodyinfo.active = true;
		boolean success = true;
	
		// Create all other bodies.
		for(Obstacle obj : bodies) {
			success = success && obj.activatePhysics(world);
		}
		success = success && createJoints(world);
		
		// Clean up if we failed
		if (!success) {
			deactivatePhysics(world);
		}
		return success;
	}

	/**
	 * Destroys the physics Body(s) of this object if applicable,
	 * removing them from the world.
	 * 
	 * @param world Box2D world that stores body
	 */
	public void deactivatePhysics(World world) {
		if (bodyinfo.active) {
			// Should be good for most (simple) applications.
			for (Joint joint : joints) {
				world.destroyJoint(joint);
			}
			joints.clear();
			for (Obstacle obj : bodies) {
				obj.deactivatePhysics(world);
			}
			bodyinfo.active = false;
		}
	}

	/**
	 * Creates the joints for this object.
	 * 
	 * This method is executed as part of activePhysics. This is the primary method to 
	 * override for custom physics objects.
	 *
	 * @param world Box2D world to store joints
	 *
	 * @return true if object allocation succeeded
	 */
	protected abstract boolean createJoints(World world);

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
		// Delegate to components
		for(Obstacle obj : bodies) {
			obj.update(delta);
		}
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
		for(Obstacle obj : bodies) {
			obj.setDrawScale(x,y);
		}
    }

	/**
	 * Draws the physics object.
	 *
	 * @param canvas Drawing context
	 */
	public void draw(GameCanvas canvas) {
		// Delegate to components
		for(Obstacle obj : bodies) {
			obj.draw(canvas);
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
		// Delegate to components
		for(Obstacle obj : bodies) {
			obj.drawDebug(canvas);
		}
	}

}
