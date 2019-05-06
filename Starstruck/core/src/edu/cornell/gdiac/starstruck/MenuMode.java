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
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.math.Vector3;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.ScreenListener;

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
public class MenuMode extends WorldController implements Screen, InputProcessor, ControllerListener {
    // Textures necessary to support the loading screen
    private static final String BACKGROUND_FILE = "shared/menu.png";
    private static final String PLAY_BTN_FILE = "buttons/play.png";
    private static final String BUILD_BTN_FILE = "buttons/build.png";
    private static final String QUIT_BTN_FILE = "buttons/quit.png";
    private static final String SETTINGS_BTN_FILE = "buttons/settings.png";
    private static final String MUSIC_FILE = "audio/loading_screen.mp3";

    private static final float VOLUME = 0.3f;


    /** Background texture for start-up */
    private Texture background;
    /** Play button to display to go to level select*/
    private Texture playButton;
    /** Build button to display to go to build mode */
    private Texture buildButton;
    /** Quit button to display to exit the window */
    private Texture quitButton;
    /** Settings button to display to edit settings */
    private Texture settingsButton;
    /** Loading audio */
    public  static Music music = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));

            //= GameController.getMusic();


            //
    private String music_name = "menu";

                    //GameController.getMusicName();

    //public Music from_GameController = GameController.getMusic();
    //private String gameController_music_name = GameController.getMusicName();



    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 720;
    /** Amount to scale the play button */
    private static float BUTTON_SCALE  = 0.75f;

    /** Start button for XBox controller on Windows */
    private static int WINDOWS_START = 7;
    /** Start button for XBox controller on Mac OS X */
    private static int MAC_OS_X_START = 4;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The y-coordinate of the center of the progress bar */
    private int centerY = 375;
    /** The x-coordinate of the center of the progress bar */
    private int centerX;
    /** Offset of next button */
    private int OFFSET1 = 25;
    /** Offset of next button */
    private int OFFSET2 = 18;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** The current state of the play button */
    private int pressState;
    /** Button down */
    private int buttonId;
    /** Support for the X-Box start button in place of play button */
    private int   startButton;
    /** Whether or not this player mode is still active */
    private boolean active;

    /** Play button down */
    private static int PLAY = 1;
    /** Build button down */
    private static int BUILD = 2;
    /** Quit button down */
    private static int QUIT = 3;
    /** button down */
    private static boolean DOWN = false;
    /** button up */
    private static boolean UP = true;


    public static Music getMusic() {return music;}


    /**
     * Returns true if all assets are loaded and the player is ready to go.
     *
     * @return true if the player is ready to go
     */
    public boolean isReady() {
        return pressState == 2;
    }

    /**
     * Creates a LoadingMode with the default size and position.
     *
     * The budget is the number of milliseconds to spend loading assets each animation
     * frame.  This allows you to do something other than load assets.  An animation
     * frame is ~16 milliseconds. So if the budget is 10, you have 6 milliseconds to
     * do something else.  This is how game companies animate their loading screens.
     */
    public MenuMode(GameCanvas canvas) {
//        this.manager = JsonAssetManager.getInstance();
        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        // Load the next two images immediately.
//        playButton = JsonAssetManager.getInstance().getEntry("play",
//                Texture.class);
//        buildButton = JsonAssetManager.getInstance().getEntry("build", Texture.class);
//        quitButton = JsonAssetManager.getInstance().getEntry("quit", Texture.class);
//        background = JsonAssetManager.getInstance().getEntry("loading", Texture.class);

        playButton = new Texture(PLAY_BTN_FILE);
        buildButton = new Texture(BUILD_BTN_FILE);
        background = new Texture(BACKGROUND_FILE);
        quitButton = new Texture(QUIT_BTN_FILE);;

//        music = JsonAssetManager.getInstance().getEntry("menu", Music.class);

        // No progress so far.
        pressState = 0;
        active = false;
        buttonId = 0;

        startButton = (System.getProperty("os.name").equals("Mac OS X") ? MAC_OS_X_START : WINDOWS_START);
        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
        active = true;
    }

    /**
     * Called when this screen should release all resources.
     */
    public void dispose() {
        background.dispose();
        background = null;
        playButton.dispose();
        playButton = null;
        buildButton.dispose();
        buildButton = null;
        quitButton.dispose();
        quitButton = null;
        music.stop();
        music.dispose();
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
    public void update(float delta) {
//        playButton = new Texture(PLAY_BTN_FILE);
//        playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//
//        playButton = new Texture(PLAY_BTN_FILE);
//        playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//
//        playButton = new Texture(PLAY_BTN_FILE);
//        playButton.setFilter(TextureFilter.Linear, TextureFilter.Linear);
//        SoundController.getInstance().play(MUSIC_FILE,MUSIC_FILE,true,VOLUME);
//        SoundController.getInstance().update();
//        if (from_GameController != null) {
//            if (from_GameController.isPlaying()){
//                from_GameController.stop();
//                from_GameController.dispose();
//                from_GameController = null;
//            }
//        }

//        if (music != null && music_name != "menu") {
//            music.stop();
//            music.dispose();
//            music = null;
//        }
//        if (GameController.getMusic() != null) {
//
//        }
        if (!music.isPlaying()) {
            //music = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));
            music.play();
            music.setLooping(true);
            //music_name = "menu";
        }

    }

    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    private void draw() {
        OrthographicCamera cam = (OrthographicCamera) canvas.getCamera();
        if (cam.position.x != canvas.getWidth()/2 || cam.position.y != canvas.getHeight()/2) {
            cam.position.x = canvas.getWidth()/2; cam.position.y = canvas.getHeight()/2;
            cam.update();
        }
        canvas.begin();
        canvas.draw(background, 0, 0, canvas.getWidth(), canvas.getHeight());
        Color tint = (pressState == 1 ? Color.GRAY: Color.WHITE);
        canvas.draw(playButton, tint, playButton.getWidth()/2, playButton.getHeight()/2,
                centerX, centerY, 0, scale, scale);
        canvas.draw(buildButton, tint, buildButton.getWidth()/2, buildButton.getHeight()/2,
                centerX, centerY-playButton.getHeight()-OFFSET1, 0, scale, scale);
        canvas.draw(quitButton, tint, quitButton.getWidth()/2, quitButton.getHeight()/2,
                centerX, centerY-playButton.getHeight()-OFFSET1-buildButton.getHeight()-OFFSET2,
                0, scale, scale);
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
//            if (!music.isPlaying()) { music.play();}
            // We are are ready, notify our listener

            if (isReady() && listener != null && buttonId == PLAY) {
                pressState = 0;
                buttonId = 0;
                System.out.println("play");
                listener.exitScreen(this, WorldController.EXIT_SELECT);
            }
            if (isReady() && listener != null && buttonId == BUILD) {
                pressState = 0;
                buttonId = 0;
                System.out.println("build");

                listener.exitScreen(this, WorldController.EXIT_EDIT);
            }
            if (isReady() && listener != null && buttonId == QUIT) {
                pressState = 0;
                buttonId = 0;
                System.out.println("exit");
                listener.exitScreen(this, WorldController.EXIT_QUIT);
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

        centerY = (int)(height*.56);
        centerX = width/2;
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (playButton == null || buildButton == null || quitButton == null || pressState == 2) {
            buttonId = 0;
            return true;
        }

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        float xdist = Math.abs(screenX-centerX);
        float ydist = Math.abs(screenY-centerY);
        if (xdist < playButton.getWidth()/2 && ydist < playButton.getHeight()/2) {
            pressState = 1;
            buttonId = PLAY;
        }
        ydist = Math.abs(screenY - centerY + playButton.getHeight() + OFFSET1);
        if (xdist < buildButton.getWidth()/2 && ydist < buildButton.getHeight()/2) {
            pressState = 1;
            buttonId = BUILD;
        }
        ydist = Math.abs(screenY - centerY + playButton.getHeight() + OFFSET1 + buildButton.getHeight() + OFFSET2);
        if (xdist < quitButton.getWidth()/2 && ydist < quitButton.getHeight()/2) {
            pressState = 1;
            buttonId = QUIT;
        }

        return false;
    }

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
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        // Flip to match graphics coordinates
        screenY = heightY-screenY;

        float xdist = Math.abs(screenX-centerX);
        float ydist = Math.abs(screenY-centerY);
        if (pressState == 1 && buttonId == PLAY && xdist < playButton.getWidth()/2 && ydist < playButton.getHeight()/2) {
            pressState = 2;
            return false;
        }
        ydist = Math.abs(screenY - centerY + playButton.getHeight() + OFFSET1);
        if (pressState == 1 && buttonId == BUILD && xdist < buildButton.getWidth()/2 && ydist < buildButton.getHeight()/2) {
            pressState = 2;
            return false;
        }
        ydist = Math.abs(screenY - centerY + playButton.getHeight() + OFFSET1 + buildButton.getHeight() + OFFSET2);
        if (pressState == 1 && buttonId == QUIT && xdist < quitButton.getWidth()/2 && ydist < quitButton.getHeight()/2) {
            pressState = 2;
            return false;
        }

        if (pressState == 1) {
            pressState = 0;
            buttonId = 0;
        }

        return true;
    }

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
    public boolean buttonDown (Controller controller, int buttonCode) {
        if (buttonCode == startButton && pressState == 0) {
            pressState = 1;
            buttonId = PLAY;
            return false;
        }
        return true;
    }

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
    public boolean buttonUp (Controller controller, int buttonCode) {
        if (pressState == 1 && buttonCode == startButton) {
            pressState = 2;
            return false;
        }
        return true;
    }

    // UNSUPPORTED METHODS FROM InputProcessor

    /**
     * Called when a key is pressed (UNSUPPORTED)
     *
     * @param keycode the key pressed
     * @return whether to hand the event to other listeners.
     */
    public boolean keyDown(int keycode) {
        if (pressState == 0 && keycode == Input.Keys.N) {
            pressState = 1;
            buttonId = PLAY;
            return false;
        } else if (pressState == 0 && keycode == Input.Keys.P) {
            pressState = 1;
            buttonId = BUILD;
        }
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
        if (pressState == 1 && keycode == Input.Keys.N || keycode == Input.Keys.P) {
            pressState = 2;
            buttonId = PLAY;
            return false;
        } else if (pressState == 1 && keycode == Input.Keys.P) {
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


    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Gdx.input.setInputProcessor(this);

        buttonId = 0;
        pressState = 0;
    }
}