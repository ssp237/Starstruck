/*
 * RocketWorldController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to 
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.rocket;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;

/**
 * Gameplay specific controller for the rocket lander game.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class RocketController extends WorldController implements ContactListener {
	/** Reference to the rocket texture */
	private static final String ROCK_TEXTURE = "rocket/rocket.png";
	/** The reference for the afterburner textures  */
	private static final String MAIN_FIRE_TEXTURE = "rocket/flames.png";
	private static final String RGHT_FIRE_TEXTURE = "rocket/flames-right.png";
	private static final String LEFT_FIRE_TEXTURE = "rocket/flames-left.png";
	/** Reference to the crate image assets */
	private static final String CRATE_PREF = "rocket/crate0";
	/** How many crate assets we have */
	private static final int MAX_CRATES = 2;

	/** The asset for the collision sound */
	private static final String  COLLISION_SOUND = "rocket/bump.mp3";
	/** The asset for the main afterburner sound */
	private static final String  MAIN_FIRE_SOUND = "rocket/afterburner.mp3";
	/** The asset for the right afterburner sound */
	private static final String  RGHT_FIRE_SOUND = "rocket/sideburner-right.mp3";
	/** The asset for the left afterburner sound */
	private static final String  LEFT_FIRE_SOUND = "rocket/sideburner-left.mp3";
	
	/** Texture assets for the rocket */
	private TextureRegion rocketTexture;
	/** Texture filmstrip for the main afterburner */
	private FilmStrip mainTexture;
	/** Texture filmstrip for the main afterburner */
	private FilmStrip leftTexture;
	/** Texture filmstrip for the main afterburner */
	private FilmStrip rghtTexture;
	
	/** Texture assets for the crates */
	private TextureRegion[] crateTextures = new TextureRegion[MAX_CRATES];
	/** Track asset loading from all instances and subclasses */
	private AssetState rocketAssetState = AssetState.EMPTY;

	/**
	 * Preloads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void preLoadContent(AssetManager manager) {
		if (rocketAssetState != AssetState.EMPTY) {
			return;
		}
		
		rocketAssetState = AssetState.LOADING;
		for (int ii = 0; ii < crateTextures.length; ii++) {
			manager.load(CRATE_PREF + (ii + 1) +".png", Texture.class);
			assets.add(CRATE_PREF + (ii + 1) +".png");
		}
		
		// Ship textures
		manager.load(ROCK_TEXTURE, Texture.class);
		assets.add(ROCK_TEXTURE);
		manager.load(MAIN_FIRE_TEXTURE, Texture.class);
		assets.add(MAIN_FIRE_TEXTURE);
		manager.load(LEFT_FIRE_TEXTURE, Texture.class);
		assets.add(LEFT_FIRE_TEXTURE);
		manager.load(RGHT_FIRE_TEXTURE, Texture.class);
		assets.add(RGHT_FIRE_TEXTURE);
		
		// Ship sounds
		manager.load(MAIN_FIRE_SOUND, Sound.class);
		assets.add(MAIN_FIRE_SOUND);
		manager.load(LEFT_FIRE_SOUND, Sound.class);
		assets.add(LEFT_FIRE_SOUND);
		manager.load(RGHT_FIRE_SOUND, Sound.class);
		assets.add(RGHT_FIRE_SOUND);
		manager.load(COLLISION_SOUND, Sound.class);
		assets.add(COLLISION_SOUND);

		super.preLoadContent(manager);
	}

	/**
	 * Loads the assets for this controller.
	 *
	 * To make the game modes more for-loop friendly, we opted for nonstatic loaders
	 * this time.  However, we still want the assets themselves to be static.  So
	 * we have an AssetState that determines the current loading state.  If the
	 * assets are already loaded, this method will do nothing.
	 * 
	 * @param manager Reference to global asset manager.
	 */
	public void loadContent(AssetManager manager) {
		if (rocketAssetState != AssetState.LOADING) {
			return;
		}
		
		for (int ii = 0; ii < crateTextures.length; ii++) {
			String filename = CRATE_PREF + (ii + 1) +".png";
			crateTextures[ii] = createTexture(manager,filename,false);
		}
		
		rocketTexture = createTexture(manager,ROCK_TEXTURE,false);
		mainTexture  = createFilmStrip(manager,MAIN_FIRE_TEXTURE,1,RocketModel.FIRE_FRAMES,RocketModel.FIRE_FRAMES);
		leftTexture  = createFilmStrip(manager,LEFT_FIRE_TEXTURE,1,RocketModel.FIRE_FRAMES,RocketModel.FIRE_FRAMES);
		rghtTexture  = createFilmStrip(manager,RGHT_FIRE_TEXTURE,1,RocketModel.FIRE_FRAMES,RocketModel.FIRE_FRAMES);
		
		SoundController sounds = SoundController.getInstance();
		sounds.allocate(manager,MAIN_FIRE_SOUND);
		sounds.allocate(manager,LEFT_FIRE_SOUND);
		sounds.allocate(manager,RGHT_FIRE_SOUND);
		sounds.allocate(manager,COLLISION_SOUND);
		
		super.loadContent(manager);
		rocketAssetState = AssetState.COMPLETE;
	}
	
	// Physics constants for initialization
	/** Density of non-crate objects */
	private static final float BASIC_DENSITY   = 0.0f;
	/** Density of the crate objects */
	private static final float CRATE_DENSITY   = 1.0f;
	/** Friction of non-crate objects */
	private static final float BASIC_FRICTION  = 0.1f;
	/** Friction of the crate objects */
	private static final float CRATE_FRICTION  = 0.3f;
	/** Collision restitution for all objects */
	private static final float BASIC_RESTITUTION = 0.1f;
	/** Threshold for generating sound on collision */
	private static final float SOUND_THRESHOLD = 1.0f;

	// Since these appear only once, we do not care about the magic numbers.
	// In an actual game, this information would go in a data file.
	// Wall vertices
	private static final float[] WALL1 = { 0.0f, 18.0f, 16.0f, 18.0f, 16.0f, 17.0f,
										   8.0f, 15.0f,  1.0f, 17.0f,  2.0f,  7.0f,
										   3.0f,  5.0f,  3.0f,  1.0f, 16.0f,  1.0f,
										  16.0f,  0.0f,  0.0f,  0.0f};
	private static final float[] WALL2 = {32.0f, 18.0f, 32.0f,  0.0f, 16.0f,  0.0f,
										  16.0f,  1.0f, 31.0f,  1.0f, 30.0f, 10.0f,
										  31.0f, 16.0f, 16.0f, 17.0f, 16.0f, 18.0f};
	private static final float[] WALL3 = { 4.0f, 10.5f,  8.0f, 10.5f,
            							   8.0f,  9.5f,  4.0f,  9.5f};

	// The positions of the crate pyramid
	private static final float[] BOXES = { 14.5f, 14.25f,
            							   13.0f, 12.00f, 16.0f, 12.00f,
            							   11.5f,  9.75f, 14.5f,  9.75f, 17.5f, 9.75f,
            							   13.0f,  7.50f, 16.0f,  7.50f,
            							   11.5f,  5.25f, 14.5f,  5.25f, 17.5f, 5.25f,
            							   10.0f,  3.00f, 13.0f,  3.00f, 16.0f, 3.00f, 19.0f, 3.0f};

	// Other game objects
	/** The initial rocket position */
	private static Vector2 ROCK_POS = new Vector2(24, 4);
	/** The goal door position */
	private static Vector2 GOAL_POS = new Vector2( 6, 12);

	// Physics objects for the game
	/** Reference to the goalDoor (for collision detection) */
	private BoxObstacle goalDoor;
	/** Reference to the rocket/player avatar */
	private RocketModel rocket;

	/**
	 * Creates and initialize a new instance of the rocket lander game
	 *
	 * The game has default gravity and other settings
	 */
	public RocketController() {
		setDebug(false);
		setComplete(false);
		setFailure(false);
		world.setContactListener(this);
	}
	
	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		Vector2 gravity = new Vector2(world.getGravity() );
		
		for(Obstacle obj : objects) {
			obj.deactivatePhysics(world);
		}
		objects.clear();
		addQueue.clear();
		world.dispose();
		
		world = new World(gravity,false);
		world.setContactListener(this);
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Add level goal
		float dwidth  = goalTile.getRegionWidth()/scale.x;
		float dheight = goalTile.getRegionHeight()/scale.y;
		goalDoor = new BoxObstacle(GOAL_POS.x,GOAL_POS.y,dwidth,dheight);
		goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
		goalDoor.setDensity(0.0f);
		goalDoor.setFriction(0.0f);
		goalDoor.setRestitution(0.0f);
		goalDoor.setSensor(true);
		goalDoor.setDrawScale(scale);
		goalDoor.setTexture(goalTile);
		addObject(goalDoor);

		// Create ground pieces
		PolygonObstacle obj;
		obj = new PolygonObstacle(WALL1, 0, 0);
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(BASIC_DENSITY);
		obj.setFriction(BASIC_FRICTION);
		obj.setRestitution(BASIC_RESTITUTION);
		obj.setDrawScale(scale);
		obj.setTexture(earthTile);
		obj.setName("wall1");
		addObject(obj);

		obj = new PolygonObstacle(WALL2, 0, 0);
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(BASIC_DENSITY);
		obj.setFriction(BASIC_FRICTION);
		obj.setRestitution(BASIC_RESTITUTION);
		obj.setDrawScale(scale);
		obj.setTexture(earthTile);
		obj.setName("wall2");
		addObject(obj);

		obj = new PolygonObstacle(WALL3, 0, 0);
		obj.setBodyType(BodyDef.BodyType.StaticBody);
		obj.setDensity(BASIC_DENSITY);
		obj.setFriction(BASIC_FRICTION);
		obj.setRestitution(BASIC_RESTITUTION);
		obj.setDrawScale(scale);
		obj.setTexture(earthTile);
		obj.setName("wall3");
		addObject(obj);

		// Create the pile of boxes
		for (int ii = 0; ii < BOXES.length; ii += 2) {
			int id = RandomController.rollInt(0,crateTextures.length-1);
			TextureRegion texture = crateTextures[id];
			dwidth  = texture.getRegionWidth()/scale.x;
			dheight = texture.getRegionHeight()/scale.y;
			BoxObstacle box = new BoxObstacle(BOXES[ii], BOXES[ii+1], dwidth, dheight);
			box.setDensity(CRATE_DENSITY);
			box.setFriction(CRATE_FRICTION);
			box.setRestitution(BASIC_RESTITUTION);
			box.setName("crate"+id);
			box.setDrawScale(scale);
			box.setTexture(texture);
			addObject(box);
		}

		// Create the rocket avatar
		dwidth  = rocketTexture.getRegionWidth()/scale.x;
		dheight = rocketTexture.getRegionHeight()/scale.y;
		rocket = new RocketModel(ROCK_POS.x, ROCK_POS.y, dwidth, dheight);
		rocket.setDrawScale(scale);
		rocket.setTexture(rocketTexture);
	    rocket.setBurnerStrip(RocketModel.Burner.MAIN,  mainTexture);
	    rocket.setBurnerStrip(RocketModel.Burner.LEFT,  leftTexture);
	    rocket.setBurnerStrip(RocketModel.Burner.RIGHT,  rghtTexture);
	  
	    // Add the sound names
	    rocket.setBurnerSound(RocketModel.Burner.MAIN,  MAIN_FIRE_SOUND);
	    rocket.setBurnerSound(RocketModel.Burner.LEFT,  LEFT_FIRE_SOUND);
	    rocket.setBurnerSound(RocketModel.Burner.RIGHT,  RGHT_FIRE_SOUND);
		addObject(rocket);
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param //delta Number of seconds since last animation frame
	 */
	public void update(float dt) {
		
		//#region INSERT CODE HERE
		// Read from the input and add the force to the rocket model
		// Then apply the force using the method you modified in RocketObject
		rocket.setFX((InputController.getInstance().getHorizontal() * rocket.getThrust()));
		rocket.setFY((InputController.getInstance().getVertical() * rocket.getThrust()));
		rocket.applyForce();
		//#endregion
		
	    // Animate the three burners
	    updateBurner(RocketModel.Burner.MAIN, rocket.getFY() > 1);
	    updateBurner(RocketModel.Burner.LEFT, rocket.getFX() > 1);
	    updateBurner(RocketModel.Burner.RIGHT, rocket.getFX() < -1);

	    // If we use sound, we must remember this.
	    SoundController.getInstance().update();
	}
	
	/**
	 * Updates that animation for a single burner
	 *
	 * This method is here instead of the the rocket model because of our philosophy
	 * that models should always be lightweight.  Animation includes sounds and other
	 * assets that we do not want to process in the model
	 *
	 * @param  burner   The rocket burner to animate
	 * @param  on       Whether to turn the animation on or off
	 */
	private void updateBurner(RocketModel.Burner burner, boolean on) {
	    String sound = rocket.getBurnerSound(burner);
	    if (on) {
	        rocket.animateBurner(burner, true);
	        if (!SoundController.getInstance().isActive(sound)) {
	        	SoundController.getInstance().play(sound, sound, true);
	        }
	    } else {
	        rocket.animateBurner(burner, false);
	        if (SoundController.getInstance().isActive(sound)) {
	        	SoundController.getInstance().stop(sound);
	        }
	    }
	}
	
	/// CONTACT LISTENER METHODS
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when we first get a collision between two objects.  We use 
	 * this method to test if it is the "right" kind of collision.  In particular, we
	 * use it to test if we made it to the win door.
	 *
	 * @param contact The two bodies that collided
	 */
	public void beginContact(Contact contact) {
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();

		if( (body1.getUserData() == rocket   && body2.getUserData() == goalDoor) ||
			(body1.getUserData() == goalDoor && body2.getUserData() == rocket)) {
			setComplete(true);
		}
	}
	
	/**
	 * Callback method for the start of a collision
	 *
	 * This method is called when two objects cease to touch.  We do not use it.
	 */ 
	public void endContact(Contact contact) {}
	
	private Vector2 cache = new Vector2();
	
	/** Unused ContactListener method */
	public void postSolve(Contact contact, ContactImpulse impulse) {}

	/**
	 * Handles any modifications necessary before collision resolution
	 *
	 * This method is called just before Box2D resolves a collision.  We use this method
	 * to implement sound on contact, using the algorithms outlined similar to those in
	 * Ian Parberry's "Introduction to Game Physics with Box2D".  
	 * 
	 * However, we cannot use the proper algorithms, because LibGDX does not implement 
	 * b2GetPointStates from Box2D.  The danger with our approximation is that we may
	 * get a collision over multiple frames (instead of detecting the first frame), and
	 * so play a sound repeatedly.  Fortunately, the cooldown hack in SoundController
	 * prevents this from happening.
	 *
	 * @param  contact  	The two bodies that collided
	 * @param  //oldManfold  	The collision manifold before contact
	 */

	public void preSolve(Contact contact, Manifold oldManifold) {
		float speed = 0;

		// Use Ian Parberry's method to compute a speed threshold
		Body body1 = contact.getFixtureA().getBody();
		Body body2 = contact.getFixtureB().getBody();
		WorldManifold worldManifold = contact.getWorldManifold();
		Vector2 wp = worldManifold.getPoints()[0];
		cache.set(body1.getLinearVelocityFromWorldPoint(wp));
		cache.sub(body2.getLinearVelocityFromWorldPoint(wp));
		speed = cache.dot(worldManifold.getNormal());
		    
		// Play a sound if above threshold
		if (speed > SOUND_THRESHOLD) {
			String s1 = ((Obstacle)body1.getUserData()).getName();
			String s2 = ((Obstacle)body2.getUserData()).getName();
			if (s1.equals("rocket") || s1.startsWith("crate")) {
				SoundController.getInstance().play(s1, COLLISION_SOUND, false, 0.5f);
			}
			if (s2.equals("rocket") || s2.startsWith("crate")) {
				SoundController.getInstance().play(s2, COLLISION_SOUND, false, 0.5f);
			}
		}
		
	}
}