/*
 * RagdollController.java
 *
 * You are not expected to modify this file at all.  You are free to look at it, however,
 * and determine how it works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.physics.ragdoll;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.physics.*;
import edu.cornell.gdiac.physics.obstacle.*;
import edu.cornell.gdiac.util.RandomController;
import edu.cornell.gdiac.util.SoundController;

/**
 * Gameplay specific controller for the ragdoll fishtank. 
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class RagdollController extends WorldController {
	/** Texture file for mouse crosshairs */
	private static final String CROSS_FILE = "ragdoll/crosshair.png";
	/** Texture file for watery foreground */
	private static final String FOREG_FILE = "ragdoll/foreground.png";
	/** Texture file for background image */
	private static final String BACKG_FILE = "ragdoll/background.png";
	/** File for the bubble generator */
	private static final String BUBBLE_FILE = "ragdoll/bubble.png";
	/** Files for the body textures */
	private static final String[] RAGDOLL_FILES = { "ragdoll/tux_body.png", "ragdoll/ProfWhite.png",
													"ragdoll/tux_arm.png",  "ragdoll/tux_forearm.png",
													"ragdoll/tux_thigh.png", "ragdoll/tux_shin.png" };	

	/** Reference to the bubble sound assets */
	private static final String[] BUBBLE_SOUNDS = {"ragdoll/bubble01.mp3", "ragdoll/bubble02.mp3", 
												   "ragdoll/bubble03.mp3", "ragdoll/bubble04.mp3"};
			
	/** Texture asset for mouse crosshairs */
	private TextureRegion crosshairTexture;
	/** Texture asset for background image */
	private TextureRegion backgroundTexture;
	/** Texture asset for watery foreground */
	private TextureRegion foregroundTexture;
	/** Texture asset for the bubble generator */
	private TextureRegion bubbleTexture;
	/** Texture assets for the body parts */
	private TextureRegion[] bodyTextures;

	/** Track asset loading from all instances and subclasses */
	private AssetState ragdollAssetState = AssetState.EMPTY;
	
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
		if (ragdollAssetState != AssetState.EMPTY) {
			return;
		}
		
		ragdollAssetState = AssetState.LOADING;
		manager.load(CROSS_FILE, Texture.class);
		assets.add(CROSS_FILE);
		manager.load(FOREG_FILE, Texture.class);
		assets.add(FOREG_FILE);
		manager.load(BACKG_FILE, Texture.class);
		assets.add(BACKG_FILE);
		manager.load(BUBBLE_FILE, Texture.class);
		assets.add(BUBBLE_FILE);
		for(int ii = 0; ii < RAGDOLL_FILES.length; ii++) {
			manager.load(RAGDOLL_FILES[ii], Texture.class);
			assets.add(RAGDOLL_FILES[ii]);
		}
		for(int ii = 0; ii < BUBBLE_SOUNDS.length; ii++) {
			manager.load(BUBBLE_SOUNDS[ii], Sound.class);
			assets.add(BUBBLE_SOUNDS[ii]);
		}
		
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
		if (ragdollAssetState != AssetState.LOADING) {
			return;
		}
		
		crosshairTexture = createTexture(manager,CROSS_FILE,false);
		backgroundTexture = createTexture(manager,BACKG_FILE,false);
		foregroundTexture = createTexture(manager,FOREG_FILE,false);
		
		bubbleTexture = createTexture(manager,BUBBLE_FILE,false);
		bodyTextures = new TextureRegion[RAGDOLL_FILES.length];
		for(int ii = 0; ii < RAGDOLL_FILES.length; ii++) {
			bodyTextures[ii] =  createTexture(manager,RAGDOLL_FILES[ii],false);
		}
		for(int ii = 0; ii < BUBBLE_SOUNDS.length; ii++) {
			SoundController.getInstance().allocate(manager, BUBBLE_SOUNDS[ii]);
		}

		super.loadContent(manager);
		ragdollAssetState = AssetState.COMPLETE;
	}
	
	/** The new lessened gravity for this world */
	private static final float WATER_GRAVITY = -0.25f;

	// Physics constants for initialization
	/** The density for all of (external) objects */
	private static final float BASIC_DENSITY = 0.0f;
	/** The friction for all of (external) objects */
	private static final float BASIC_FRICTION = 0.1f;
	/** The restitution for all of (external) objects */
	private static final float BASIC_RESTITUTION = 0.1f;
	/** The transparency for foreground image */
	private static Color FORE_COLOR = new Color(0.0f, 0.2f, 0.3f, 0.2f);

	// Since these appear only once, we do not care about the magic numbers.
	// In an actual game, this information would go in a data file.
	// Wall vertices
	private static final float[] WALL1 = {16.0f, 18.0f, 16.0f, 17.0f,  1.0f, 17.0f,
										   1.0f,  1.0f, 16.0f,  1.0f, 16.0f,  0.0f,
										   0.0f,  0.0f,  0.0f, 18.0f}; 

	private static final float[] WALL2 =  {32.0f, 18.0f, 32.0f,  0.0f, 16.0f,  0.0f,
            							   16.0f,  1.0f, 31.0f,  1.0f, 31.0f, 17.0f,
            							   16.0f, 17.0f, 16.0f, 18.0f};
										   
	// Other game objects
	/** The initial position of the ragdoll head */
	private static Vector2 DOLL_POS = new Vector2( 16.0f,  10.0f);

	/** Reference to the character's ragdoll */
	private RagdollModel ragdoll;

	/** Counter for sound control */
	private long soundCounter;
	
	/** Mouse selector to move the ragdoll */
	private ObstacleSelector selector;
	
	/**
	 * Creates and initialize a new instance of the ragdoll fishtank
	 *
	 * The world has lower gravity to simulate being underwater.
	 */
	public RagdollController() {
		super(DEFAULT_WIDTH,DEFAULT_HEIGHT,WATER_GRAVITY);
		setDebug(false);
		setComplete(false);
		setFailure(false);
		soundCounter = 0;
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
		setComplete(false);
		setFailure(false);
		populateLevel();
	}

	/**
	 * Lays out the game geography.
	 */
	private void populateLevel() {
		// Make the ragdoll
		ragdoll = new RagdollModel(DOLL_POS.x, DOLL_POS.y);
		ragdoll.setDrawScale(scale.x,scale.y);
		ragdoll.setPartTextures(bodyTextures);
		ragdoll.getBubbleGenerator().setTexture(bubbleTexture);
		addObject(ragdoll);

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

		selector = new ObstacleSelector(world);
		selector.setTexture(crosshairTexture);
		selector.setDrawScale(scale);
		
		/*
		BodyDef groundDef = new BodyDef();
		groundDef.type = BodyDef.BodyType.StaticBody;
		EdgeShape groundShape = new EdgeShape();
		groundShape.set(-500.0f, 0.0f, 500.0f, 0.0f);
		ground = world.createBody(groundDef);
		ground.createFixture(groundShape,0);
		*/
	}

	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void update(float dt) {
	    // Move an object if touched
		InputController input = InputController.getInstance();
	    if (input.didTertiary() && !selector.isSelected()) {
	        selector.select(input.getCrossHair().x,input.getCrossHair().y);
	    } else if (!input.didTertiary() && selector.isSelected()) {
	        selector.deselect();
	    } else {
	        selector.moveTo(input.getCrossHair().x,input.getCrossHair().y);
	    }

	    // Play a sound for each bubble
	    if (ragdoll.getBubbleGenerator().didBubble()) {
	        // Pick a sound
	        int indx =  RandomController.rollInt(0,BUBBLE_SOUNDS.length-1);
	        String key = "bubble"+soundCounter;
	        soundCounter++;
	        SoundController.getInstance().play(key, BUBBLE_SOUNDS[indx], false);
	    }
	    
	    // If we use sound, we must remember this.
	    SoundController.getInstance().update();
	}
	
	/**
	 * Draw the physics objects together with foreground and background
	 *
	 * This is completely overridden to support custom background and foreground art.
	 *
	 * @param dt Timing values from parent loop
	 */
	public void draw(float dt) {
		canvas.clear();
		
		// Draw background unscaled.
		canvas.begin();
		canvas.draw(backgroundTexture, Color.WHITE, 0, 0,canvas.getWidth(),canvas.getHeight());
		canvas.end();
		
		canvas.begin();
		for(Obstacle obj : objects) {
			obj.draw(canvas);
		}
		canvas.end();
		
		if (isDebug()) {
			canvas.beginDebug();
			for(Obstacle obj : objects) {
				obj.drawDebug(canvas);
			}
			canvas.endDebug();
		}
		
		// Draw foreground last.
		canvas.begin();
		canvas.draw(foregroundTexture, FORE_COLOR,  0, 0, canvas.getWidth(), canvas.getHeight());
		selector.draw(canvas);
		canvas.end();
	}

}