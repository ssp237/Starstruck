/*
 * LoadingMode.java
 *
 * Asset loading is a really tricky problem.  If you have a lot of sound or images,
 * it can take a long time to decompress them and load them into memory.  If you just
 * have code at the start to load all your assets, your game will look like it is hung
 * at the start.
 *
 * The alternative is asynchronous asset loading.  In asynchronous loading, you load a
 * little bit of the assets at a time, but still animate the game while you are loading.
 * This way the player knows the game is not hung, even though he or she cannot do
 * anything until loading is complete. You know those loading screens with the inane tips
 * that want to be helpful?  That is asynchronous loading.
 *
 * This player mode provides a basic loading screen.  While you could adapt it for
 * between level loading, it is currently designed for loading all assets at the
 * start of the game.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.assets.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.controllers.*;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.*;

import java.util.LinkedList;

/**
 * Class that provides a loading screen for the state of the game.
 *
 * You still DO NOT need to understand this class for this lab.  We will talk about this
 * class much later in the course.  This class provides a basic template for a loading
 * screen to be used at the start of the game or between levels.  Feel free to adopt
 * this to your needs.
 *
 * You will note that this mode has some textures that are not loaded by the AssetManager.
 * You are never required to load through the AssetManager.  But doing this will block
 * the application.  That is why we try to have as few resources as possible for this
 * loading screen.
 */
public class LoadingMode implements Screen, InputProcessor, ControllerListener {
    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "backgrounds/loading.png";
    private static final String SPIN_FILE = "shared/spin.png";
    private static final String TITLE_FILE = "shared/loading_title.png";

    LinkedList<String> loaded;

    /** Background texture for start-up */
    private Texture background;
    /** loading texture for start-up */
    private Texture spin;
    /** filmstrip for title animation */
    private FilmStrip title;

    /** Default budget for asset loader (do nothing but load 60 fps) */
    private static int DEFAULT_BUDGET = 15;
    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 720;
    /** originX of loading spin */
    private static int SPIN_X = STANDARD_WIDTH/2;
    /** originY of loading spin */
    private static int SPIN_Y = 393;
    /** originX of loading title */
    private static int TITLE_X = 672;
    /** originY of loading title */
    private static int TITLE_Y = 146;

    /** Start button for XBox controller on Windows */
    private static int WINDOWS_START = 7;
    /** Start button for XBox controller on Mac OS X */
    private static int MAC_OS_X_START = 4;

    /** AssetManager to be loading in the background */
    private AssetManager manager;
    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Current progress (0 to 1) of the asset manager */
    private float progress;
    /** The current state of the play button */
    private int   pressState;
    /** The amount of time to devote to loading assets (as opposed to on screen hints, etc.) */
    private int   budget;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;
    /** Whether or not this player mode is still active */
    private boolean active;
    /** angle of loading spin */
    private float angle;

    /**
     * Returns the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @return the budget in milliseconds
     */
    public int getBudget() {
        return budget;
    }

    /**
     * Sets the budget for the asset loader.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     *
     * @param millis the budget in milliseconds
     */
    public void setBudget(int millis) {
        budget = millis;
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
     * Creates a LoadingMode with the default budget, size and position.
     *
     */
    public LoadingMode(GameCanvas canvas) {
        this(canvas,DEFAULT_BUDGET);
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     * @param millis The loading budget in milliseconds
     */
    public LoadingMode(GameCanvas canvas, int millis) {
        this.manager = JsonAssetManager.getInstance();
        this.canvas  = canvas;
        budget = millis;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        // Load the next two images immediately.
        background = new Texture(BACKGROUND_FILE);
        spin = new Texture(SPIN_FILE);
        title = new FilmStrip(new Texture(TITLE_FILE), 4, 1, 4, 20, "title");

        // No progress so far.
        progress   = 0;
        pressState = 0;
        active = false;
        angle = 0f;

        startButton = (System.getProperty("os.name").equals("Mac OS X") ? MAC_OS_X_START : WINDOWS_START);
        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
        active = true;
        loaded = new LinkedList<String>();
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        background.dispose();
        background = null;
    }

    /**
     * Update the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     *
     * @param delta Number of seconds since last animation frame
     */
    private void update(float delta) {
        angle += 0.03f;

//        for(String s : manager.getAssetNames()) {
//            if (manager.isLoaded(s) && !loaded.contains(s)){
//                System.out.println(s);
//                loaded.add(s);
//            }
//        }
        manager.update(budget);
        this.progress = manager.getProgress();
        if (progress >= 1.0f) {
            this.progress = 1.0f;
            pressState = 2;
        }
        title.tick();
    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        canvas.begin();
        canvas.draw(background, 0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.draw(title, Color.WHITE, title.getRegionWidth()/2, title.getRegionHeight()/2, TITLE_X, TITLE_Y, 0, scale, scale);
        Color tint = (pressState == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(spin, tint, spin.getWidth()/2, spin.getHeight()/2,
                    SPIN_X, SPIN_Y, -angle, scale, scale);
        canvas.end();
    }


    // ADDITIONAL SCREEN METHODS
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
            draw();

            // We are are ready, notify our listener
            if (isReady() && listener != null) {
                listener.exitScreen(this, 0);
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

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }

    // PROCESSING PLAYER INPUT
    /**
     * Called when the screen was touched or a mouse button was pressed.
     *
     * This method checks to see if the play button is available and if the click
     * is in the bounds of the play button.  If so, it signals the that the button
     * has been pressed and is currently down. Any mouse button is accepted.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return true; }

    /**
     * Called when a finger was lifted or a mouse button was released.
     *
     * This method checks to see if the play button is currently pressed down. If so,
     * it signals the that the player is ready to go.
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return true; }

    /**
     * Called when a button on the Controller was pressed.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * pressing (but not releasing) the play button.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonDown (Controller controller, int buttonCode) { return true; }

    /**
     * Called when a button on the Controller was released.
     *
     * The buttonCode is controller specific. This listener only supports the start
     * button on an X-Box controller.  This outcome of this method is identical to
     * releasing the the play button after pressing it.
     *
     * @param controller The game controller
     * @param buttonCode The button pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean buttonUp (Controller controller, int buttonCode) { return true; }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        return true;
    }

    /**
     * Called when a key is typed (UNSUPPORTED)
     *
     * @return whether to hand the event to other listeners.
     */
    public boolean keyTyped(char character) {
        return true;
    }

    /**
     * Called when a key is released.
     *
     * We allow key commands to start the game this time.
     *
     * @param keycode the key released
     * @return whether to hand the event to other listeners.
     */
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.N || keycode == Input.Keys.P) {
            pressState = 2;
            return false;
        }
        return true;
    }

    /**
     * Called when the mouse was moved without any buttons being pressed. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @return whether to hand the event to other listeners.
     */
    public boolean mouseMoved(int screenX, int screenY) {
        return true;
    }

    /**
     * Called when the mouse wheel was scrolled. (UNSUPPORTED)
     *
     * @param amount the amount of scroll from the wheel
     * @return whether to hand the event to other listeners.
     */
    public boolean scrolled(int amount) {
        return true;
    }

    /**
     * Called when the mouse or finger was dragged. (UNSUPPORTED)
     *
     * @param screenX the x-coordinate of the mouse on the screen
     * @param screenY the y-coordinate of the mouse on the screen
     * @param pointer the button or touch finger number
     * @return whether to hand the event to other listeners.
     */
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    // UNSUPPORTED METHODS FROM ControllerListener

    /**
     * Called when a controller is connected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void connected (Controller controller) {}

    /**
     * Called when a controller is disconnected. (UNSUPPORTED)
     *
     * @param controller The game controller
     */
    public void disconnected (Controller controller) {}

    /**
     * Called when an axis on the Controller moved. (UNSUPPORTED)
     *
     * The axisCode is controller specific. The axis value is in the range [-1, 1].
     *
     * @param controller The game controller
     * @param axisCode 	The axis moved
     * @param value 	The axis value, -1 to 1
     * @return whether to hand the event to other listeners.
     */
    public boolean axisMoved (Controller controller, int axisCode, float value) {
        return true;
    }

    /**
     * Called when a POV on the Controller moved. (UNSUPPORTED)
     *
     * The povCode is controller specific. The value is a cardinal direction.
     *
     * @param controller The game controller
     * @param povCode 	The POV controller moved
     * @param value 	The direction of the POV
     * @return whether to hand the event to other listeners.
     */
    public boolean povMoved (Controller controller, int povCode, PovDirection value) {
        return true;
    }

    /**
     * Called when an x-slider on the Controller moved. (UNSUPPORTED)
     *
     * The x-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when a y-slider on the Controller moved. (UNSUPPORTED)
     *
     * The y-slider is controller specific.
     *
     * @param controller The game controller
     * @param sliderCode The slider controller moved
     * @param value 	 The direction of the slider
     * @return whether to hand the event to other listeners.
     */
    public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) {
        return true;
    }

    /**
     * Called when an accelerometer value on the Controller changed. (UNSUPPORTED)
     *
     * The accelerometerCode is controller specific. The value is a Vector3 representing
     * the acceleration on a 3-axis accelerometer in m/s^2.
     *
     * @param controller The game controller
     * @param accelerometerCode The accelerometer adjusted
     * @param value A vector with the 3-axis acceleration
     * @return whether to hand the event to other listeners.
     */
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return true;
    }

}