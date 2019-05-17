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
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Obstacles.Button;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;
import edu.cornell.gdiac.util.SoundController;

/**
 * Gameplay specific controller for the platformer game.
 *
 * You will notice that asset loading is not done with static methods this time.
 * Instance asset loading makes it easier to process our game modes in a loop, which
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class Settings extends WorldController implements Screen, InputProcessor, ControllerListener {

    /** Speed of camera pan & zoom */
    private static final float PAN_CONST = 10;
    /** The reader to process JSON files */
    private JsonReader jsonReader;
    /** The JSON asset directory */
    private JsonValue  assetDirectory;

    /** Camera offset */
    private float camOffsetX;

    /** Reference to GameCanvas created by the root */
    private GameCanvas canvas;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Scaling factor for when the student changes the resolution. */
    private float scale;

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 1280;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 720;
    private static int CENTERX = 640;
    private static int CENTERY = 420;
    private static int OFFSET1 = 98;
    private static int OFFSET2 = 76;

    /** The current state of the onePlayer button */
    private int pressState;

    /** Whether or not this player mode is still active */
    private boolean active;

    private static final String MUSIC_FILE = "audio/loading_screen.mp3";
    public static boolean settingsIsPlaying() {return music.isPlaying();}

    public static Music music = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));

    public static Music getMusic() {return music;}

    private boolean dont_play_music = false;

    private Button onePlayer;
    private Button twoPlayer;
    private Button quit;
    private Button play;
    private Button controller;
    private Button keyboard;

    private Button active1;
    private Button active2;
    private int numPlayers;
    private boolean returnToMenu;
    private boolean showXbox;

    /** Background texture for start-up */
    private Texture background;
    private Texture overlay;


    /** Current selected button */
    private Button currentButton = null;

    /** Buttons to display upon winning a level */
    private PooledList<Button> buttons;



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

        super.loadContent(manager);

        background = JsonAssetManager.getInstance().getEntry("menu background", Texture.class);
        overlay = JsonAssetManager.getInstance().getEntry("overlay", Texture.class);

        buttons = new PooledList<Button>();

        TextureRegion texture = JsonAssetManager.getInstance().getEntry("oneplayer button", TextureRegion.class);
        onePlayer = new Button(canvas.getWidth()/3-20,canvas.getHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), texture,
                world, new Vector2(1,1), JsonAssetManager.getInstance().getEntry("oneplayer glow", TextureRegion.class), "onePlayer");

        texture = JsonAssetManager.getInstance().getEntry("twoplayer button", TextureRegion.class);
        twoPlayer = new Button(canvas.getWidth()*2/3+20,canvas.getHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), texture,
                world, new Vector2(1,1), JsonAssetManager.getInstance().getEntry("twoplayer glow", TextureRegion.class), "twoPlayer");

        texture = JsonAssetManager.getInstance().getEntry("done", TextureRegion.class);
        play = new Button(canvas.getWidth()/2,canvas.getHeight()/4, texture.getRegionWidth(), texture.getRegionHeight(), texture,
                world, new Vector2(1,1), JsonAssetManager.getInstance().getEntry("done glow", TextureRegion.class), "twoPlayer");
        texture = JsonAssetManager.getInstance().getEntry("oneplayer active", TextureRegion.class);
        active1 = new Button(canvas.getWidth()/3-20,canvas.getHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), texture,
                world, new Vector2(1,1), JsonAssetManager.getInstance().getEntry("oneplayer glow", TextureRegion.class), "onePlayer");

        texture = JsonAssetManager.getInstance().getEntry("twoplayer active", TextureRegion.class);
        active2 = new Button(canvas.getWidth()*2/3+20,canvas.getHeight()/2, texture.getRegionWidth(), texture.getRegionHeight(), texture,
                world, new Vector2(1,1), JsonAssetManager.getInstance().getEntry("twoplayer glow", TextureRegion.class), "twoPlayer");

        buttons.add(onePlayer);
        buttons.add(twoPlayer);
        buttons.add(play);

        Gdx.input.setInputProcessor(this);
        // Let ANY connected controller start the game.
        for(Controller controller : Controllers.getControllers()) {
            controller.addListener(this);
        }
    }

    /** Track asset loading from all instances and subclasses */
    private AssetState platformAssetState = AssetState.EMPTY;


    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public Settings(GameCanvas canvas) {

        this.canvas  = canvas;

        // Compute the dimensions from the canvas
        resize(canvas.getWidth(),canvas.getHeight());

        jsonReader = new JsonReader();
        currentButton = null;

        pressState = 0;
        active = false;
        numPlayers = 0;
        returnToMenu = true;
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

    public void setReturnToMenu(boolean state) {
        returnToMenu = state;
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
     * Resets the status of the game so that we can onePlayer again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Gdx.input.setInputProcessor(this);

        setComplete(false);
        setFailure(false);
        currentButton = null;
        pressState = 0;
        camOffsetX = 0;

        for (Button b : buttons) {
            b.setActive(false);
            b.pushed = false;
        }

//        if (music != null) {
//            music.stop();
//            music.dispose();
//        }

        if (GameController.getMusic() != null) {
            GameController.getMusic().stop();
            GameController.getMusic().dispose();
        }
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
        //TODO Removed sound stuffs

//         If we use sound, we must remember this.
        SoundController.getInstance().update();

        if (!dont_play_music) {
            if (!music.isPlaying()) {
                //music = Gdx.audio.newMusic(Gdx.files.internal(MUSIC_FILE));
                music.play();
                music.setLooping(true);
                //music_name = "menu";
            }
        }
    }

    /** Called when a key was pressed
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed */
    public boolean keyDown (int keycode) {
        if (buttons.getTail() == null) { return true; }

        return true;
    };

    /** Called when a key was released
     *
     * @param keycode one of the constants in {@link Input.Keys}
     * @return whether the input was processed */
    public boolean keyUp (int keycode) {
        if (buttons.getTail() == null) { return true; }

        if (keycode == Input.Keys.ESCAPE) {
            active1.setActive(true);
            pressState = 2;
            play.pushed = true;
            return false;
        }

        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.RIGHT) {
            if (currentButton != null) currentButton.setActive(false);
            if (currentButton == null || currentButton.equals(twoPlayer)) {
                currentButton = onePlayer;
            } else if (currentButton.equals(onePlayer)) {
                currentButton = twoPlayer;
            }
            currentButton.setActive(true);
        } else if (keycode == Input.Keys.UP || keycode == Input.Keys.DOWN) {
            if (currentButton != null) currentButton.setActive(false);
            if (currentButton == null || currentButton.equals(play)) {
                currentButton = onePlayer;
            } else if ((currentButton.equals(onePlayer) || currentButton.equals(twoPlayer)) && numPlayers != 0) {
                currentButton = play;
            }
            currentButton.setActive(true);
        } else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
            if (currentButton != null) {
                currentButton.pushed = true;
                if (currentButton.equals(play)) currentButton.setActive(false);
                pressState = 2;
            } else if (numPlayers == 0) {
                active1.setActive(true);
                pressState = 2;
                play.pushed = true;
            }
        }
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
        if (currentButton != null) {
            pressState = 1;
            return false;
        }
        return true;
    };

    /** Called when a finger was lifted or a mouse button was released. The button parameter will be {@link Input.Buttons#LEFT} on iOS.
     * @param pointer the pointer for the event.
     * @param button the button
     * @return whether the input was processed */
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        screenY = heightY - screenY;

        if (buttons.getTail() == null) { return true; }


        if (currentButton != null) {
            if (currentButton.isIn(screenX, screenY) && currentButton.getActive()) {
                currentButton.pushed = true;
                pressState = 2;
                return false;
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
        if (buttons.getTail() == null) { return true; }
        screenY = heightY - screenY;

        currentButton = null;
        for (Button b : buttons) {
            b.setActive(false);
            b.pushed = false;
            if (b.isIn(screenX,screenY)) {
                currentButton = b;
                b.setActive(true);
            }
        }
        return true;
    };

    /** Called when the mouse wheel was scrolled. Will not be called on iOS.
     * @param amount the scroll amount, -1 or 1 depending on the direction the wheel was scrolled.
     * @return whether the input was processed. */
    public boolean scrolled (int amount) {
        return true;
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
            Vector3 pls = new Vector3(1,1,1);

            if (isReady() && play.pushed) {
                if (numPlayers == 0 ) {numPlayers = 1;}
                if (returnToMenu) {
                    listener.exitScreen(this, WorldController.EXIT_QUIT, Integer.toString(numPlayers));
                } else {
                    listener.exitScreen(this, WorldController.EXIT_PLAY, Integer.toString(numPlayers));
                }
            } else if (numPlayers == 0) {
                if (isReady() && currentButton == onePlayer && onePlayer.pushed) {
                    numPlayers = 1;
                    listener.exitScreen(this, WorldController.EXIT_QUIT, Integer.toString(numPlayers));
                } else if (isReady() && currentButton == twoPlayer && twoPlayer.pushed) {
                    numPlayers = 2;
                    listener.exitScreen(this, WorldController.EXIT_QUIT, Integer.toString(numPlayers));
                }
            } else {
                if (isReady() && currentButton == onePlayer && onePlayer.pushed) {
                    numPlayers = 1;
                } else if (isReady() && currentButton == twoPlayer && twoPlayer.pushed) {
                    numPlayers = 2;
                }
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

    /**
     * Draw the physics objects to the canvas and the background
     *
     * The method draws all objects in the order that they weret added.
     *
     * @param delta The delay in seconds since the last update
     */
    public void draw(float delta) {
        canvas.clear();

        canvas.begin();
        canvas.draw(background, 0, 0);
        canvas.draw(overlay, 0, 0);

        for (Button b : buttons) {
            if (!(b == play && numPlayers == 0)) {
                b.draw(canvas);
            }
        }
        if (numPlayers == 1) {
            active1.draw(canvas);
        } else if (numPlayers == 2) {
            active2.draw(canvas);
        }
        canvas.end();

    }

}