/*
 * GameController.java
 *
 * This is one of the files that you are expected to modify. Please limit changes to
 * the regions that say INSERT CODE HERE.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.*;

import java.util.ArrayList;

//import edu.cornell.gdiac.physics.*;
//import edu.cornell.gdiac.starstruck.Models.PlayerModel;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class LevelSelect extends WorldController implements Screen, InputProcessor, ControllerListener {

    /** The reader to process JSON files */
    private JsonReader jsonReader;
    /** The JSON asset directory */
    private JsonValue  assetDirectory;
    /** The JSON defining the level model */
    private JsonValue  levelFormat;
    /** Reference to the game level */
    protected LevelSelectModel level;
    /** mouse is currently selecting */
    private Level currentLevel;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** Space sounds */
    private static final String SPACE_SOUNDS = "space sounds";

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 720;

    /** The current state of the play button */
    private int pressState;

    /** Whether or not this player mode is still active */
    private boolean active;


    /**
     * Load the assets for this controller.
     *
     * To make the game modes more for-loop friendly, we opted for nonstatic loaders
     * this time.  However, we still want the assets themselves to be static.  So
     * we have an AssetState that determines the current loading state.  If the
     * assets are already loaded, this method will do nothing.
     *
     * @param manager Reference to global asset manager.
     */
    public void loadContent(AssetManager manager) {

        JsonAssetManager.getInstance().allocateDirectory();

        // TODO sound
        SoundController sounds = SoundController.getInstance();
        sounds.allocate("jump");
        sounds.allocate("land");
        sounds.allocate("anchor");
        sounds.allocate("switch");
        sounds.allocate("space sounds");

        super.loadContent(manager);

        levels = new PooledList<Level>();
    }

    // Physics constants for initialization
    /** The volume for sound effects */
    private static final float EFFECT_VOLUME = 0.8f;
    /** The volume for music */
    private static final float MUSIC_VOLUME = 0.3f;
    /** 0 vector 2 */
    private static final Vector2 reset = new Vector2(0, 0);
    /** Turns off enemy collisions for testing */
    public static final boolean testE = true;
    /** Allows manual control of astronaut in space for testing */
    public static final boolean testC = true;

    // Physics objects for the game
    /** Reference to the character avatar */
    private AstronautModel avatar;
    /** Levels */
    private PooledList<Level> levels;
    /** galaxy */
    private Galaxy galaxy = Galaxy.LEVELSELECT;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;

    /**
     * Return a reference to the primary avatar
     * @return Return a reference to the primary avatar.
     */
    public AstronautModel getAvatar() { return avatar; }

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public LevelSelect(GameCanvas canvas) {

        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        jsonReader = new JsonReader();
        level = new LevelSelectModel();
        currentLevel = null;

        setDebug(false);
        setComplete(false);
        setFailure(false);
        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
        pressState = 0;
        active = false;
        sensorFixtures = new ObjectSet<Fixture>();
    }


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
        if (platformAssetState != AssetState.EMPTY) {
            return;
        }

        platformAssetState = AssetState.LOADING;

        super.preLoadContent(manager);

        jsonReader = new JsonReader();
        assetDirectory = jsonReader.parse(Gdx.files.internal("levels/assets.json"));

        JsonAssetManager.getInstance().loadDirectory(assetDirectory);

    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Gdx.input.setInputProcessor(this);

        level.dispose();

        levelFormat = jsonReader.parse(Gdx.files.internal("levels/levelselect.json"));
        level.populate(levelFormat);

        setComplete(false);
        setFailure(false);
        currentLevel = null;
        pressState = 0;
        assignLevelFields();
    }

    /**
     * Assign the fields of the game controller state to reference the fields of the level
     */
    private void assignLevelFields() {
        avatar = level.getPlayer();
        objects = level.objects;
        levels = level.getLevels();
        world = level.getWorld();
    }

    /**
     * Override superclass's setDebug to also edit the level's current state
     * @param d The new value for debug
     */
    public void setDebug(boolean d) {
        super.setDebug(d);
        level.setDebug(d);
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth;
        float dheight;
    }

    /**
     * print method
     *
     * @param s what to print
     */
    protected void print(Object s) { System.out.println(s); }

    /**
     * Helper to find distance
     *
     * @param v1 v1
     * @param v2 v2
     * @return distance between v1 and v2
     */
    private float dist(Vector2 v1, Vector2 v2) {
        return (float) Math.sqrt((v1.x - v2.x)*(v1.x-v2.x) + (v1.y - v2.y) * (v1.y - v2.y));
    }

    /**
     * Helper method to move the camera with the astronauts
     */
    private void updateCam() {
        float xCam = avatar.getPosition().x * avatar.drawScale.x;
        if (xCam < canvas.getWidth()/2) xCam = canvas.getWidth()/2;
        canvas.getCamera().position.set(new Vector3(xCam, canvas.getCamera().position.y, 0));
        canvas.getCamera().update();
//        System.out.println(canvas.getCamera().position);
//        System.out.println(canvas.getHeight());
    }

    /**
     * Helper for update for control on planet
     *
     * @param avatar the active avatar
     * @param contactDir Up direction of avatar
     */
    private void updateMovement(AstronautModel avatar, Vector2 contactDir) {
        //contactDir = contactPoint.cpy().sub(curPlanet.getPosition());
        contactDir.rotateRad(-(float) Math.PI / 2);
        float move = InputController.getInstance().getHorizontal();
        if (InputController.getInstance().didRight() || InputController.getInstance().didLeft()) {
            avatar.setPlanetMove(contactDir.scl(move));
            avatar.moving = true;
        }

        if (InputController.getInstance().didPrimary() && !testC) {
            //print(contactPoint);
            avatar.setJumping(true);
//            SoundController.getInstance().play(JUMP_FILE,JUMP_FILE,false,EFFECT_VOLUME);
            avatar.setPlanetJump(contactDir);
            avatar.setOnPlanet(false);
            avatar.moving = false;
        }
    }


    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }


    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (!super.preUpdate(dt)) {
            return false;
        }

//        if (!isFailure() && (avatar.getY() < - 2 || avatar.getY() > bounds.height + 2
//                || avatar.getX() < -2)) {
//            // || avatar.getX() > bounds.getWidth() + 1)) {
//            setFailure(true);
//            return false;
//        }

        return true;
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(float dt) {
        updateCam();

        if (isFailure()) return;

//        updateMovement(avatar, avatar.contactDir);
        avatar.setRotation(InputController.getInstance().getHorizontal());
//        float angle = -avatar.contactDir.angleRad(new Vector2 (0, 1));
//        avatar.setAngle(angle);

        avatar.applyForce();
        if (testC) {
            avatar.setFixedRotation(true);
            avatar.setMovement(InputController.getInstance().getHorizontal());
            avatar.setMovementV(InputController.getInstance().getVertical());
        }

        avatar.lastPoint.set(avatar.getPosition());
        avatar.lastVel.set(avatar.getLinearVelocity());

        //TODO Removed sound stuffs

//         If we use sound, we must remember this.
        SoundController.getInstance().update();
    }

    /** Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed */
    public boolean keyDown (int keycode) {
        return true;
    };

    /** Called when a key was released
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed */
    public boolean keyUp (int keycode) {
        return true;
    };

    /** Called when a key was typed
     *
     * @param character The character
     * @return whether the input was processed */
    public boolean keyTyped (char character) {
        return true;
    };

    /** Called when the screen was touched or a mouse button was pressed. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     * @param screenX The x coordinate, origin is in the upper left corner
     * @param screenY The y coordinate, origin is in the upper left corner
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed */
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if (currentLevel != null) {
            pressState = 1;
        }
        return true;
    };

    /** Called when a finger was lifted or a mouse button was released. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed */
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (currentLevel != null) {
            screenY = heightY - screenY;
            Vector2 scale = level.getScale();
            Vector2 mouse = new Vector2(screenX/scale.x, screenY/scale.y);
            Vector2 center = currentLevel.getPosition();
            float dist = dist(center,mouse);
            if (dist <= currentLevel.getRadius()) {
               pressState = 2;
            }
        }
        return true;
    };

    /** Called when a finger or the mouse was dragged.
     * @param pointer the pointer for the event.
     * @return whether the input was processed */
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        return true;
    };

    /** Called when the mouse was moved without any buttons being pressed. Will not be called on iOS.
     * @return whether the input was processed */
    public boolean mouseMoved (int screenX, int screenY) {
        currentLevel = null;
        screenY = heightY - screenY;
        Vector2 scale = level.getScale();
        Vector2 mouse = new Vector2(screenX/scale.x, screenY/scale.y);
        Vector2 center;
        float dist;
        for (Level l : levels) {
            l.setActive(false);
            center = l.getPosition();
            dist = dist(center, mouse);
            if (dist <= l.getRadius()) {
                l.setActive(true);
                currentLevel = l;
            }
        }
        return true;
    };

    /** Called when the mouse wheel was scrolled. Will not be called on iOS.
     * @param amount the scroll amount, -1 or 1 depending on the direction the wheel was scrolled.
     * @return whether the input was processed. */
    public boolean scrolled (int amount) {
        return true;
    };

    /**
     * Draw the physics objects to the canvas and the background
     *
     * The method draws all objects in the order that they weret added.
     *
     * @param delta The delay in seconds since the last update
     */
    public void draw(float delta) {
        canvas.clear();

        level.draw(canvas);

        if (isFailure()) {
            displayFont.setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("u ded :(", displayFont, 0.0f);
            canvas.end();
        }
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        if (active) {
            update(delta);
            draw(delta);
//            if (!music.isPlaying()) { music.play();}
            // We are are ready, notify our listener
            if (currentLevel != null && isReady() && currentLevel.getUnlocked()) {
                listener.exitScreen(this, WorldController.EXIT_PLAY, currentLevel.getFile());
            }
        }
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // Compute the drawing scale
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        heightY = height;
    }

    /**
     * Called when the Screen is paused.
     *
     * This is usually when it's not active or visible on screen. An Application is
     * also paused before it is destroyed.
     */
    public void pause() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // TODO Auto-generated method stub

    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
        active = true;
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
        active = false;
    }

    /** A {@link Controller} got connected.
     * @param controller */
    public void connected (Controller controller) {  }

    /** A {@link Controller} got disconnected.
     * @param controller */
    public void disconnected (Controller controller) {  }

    /** A button on the {@link Controller} was pressed. The buttonCode is controller specific. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts button constants for known controllers.
     * @param controller
     * @param buttonCode
     * @return whether to hand the event to other listeners. */
    public boolean buttonDown (Controller controller, int buttonCode) { return true; }

    /** A button on the {@link Controller} was released. The buttonCode is controller specific. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts button constants for known controllers.
     * @param controller
     * @param buttonCode
     * @return whether to hand the event to other listeners. */
    public boolean buttonUp (Controller controller, int buttonCode) { return true; }

    /** An axis on the {@link Controller} moved. The axisCode is controller specific. The axis value is in the range [-1, 1]. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts axes constants for known controllers.
     * @param controller
     * @param axisCode
     * @param value the axis value, -1 to 1
     * @return whether to hand the event to other listeners. */
    public boolean axisMoved (Controller controller, int axisCode, float value) { return true; }

    /** A POV on the {@link Controller} moved. The povCode is controller specific. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts POV constants for known controllers.
     * @param controller
     * @param povCode
     * @param value
     * @return whether to hand the event to other listeners. */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) { return true; }

    /** An x-slider on the {@link Controller} moved. The sliderCode is controller specific. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts slider constants for known controllers.
     * @param controller
     * @param sliderCode
     * @param value
     * @return whether to hand the event to other listeners. */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) { return true; }

    /** An y-slider on the {@link Controller} moved. The sliderCode is controller specific. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts slider constants for known controllers.
     * @param controller
     * @param sliderCode
     * @param value
     * @return whether to hand the event to other listeners. */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) { return true; }

    /** An accelerometer value on the {@link Controller} changed. The accelerometerCode is controller specific. The
     * <code>com.badlogic.gdx.controllers.mapping</code> package hosts slider constants for known controllers. The value is a
     * {@link Vector3} representing the acceleration on a 3-axis accelerometer in m/s^2.
     * @param controller
     * @param accelerometerCode
     * @param value
     * @return whether to hand the event to other listeners. */
    public boolean accelerometerMoved (Controller controller, int accelerometerCode, Vector3 value) { return true; }

}