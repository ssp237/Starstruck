package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonWriter;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Models.Enemy;
import edu.cornell.gdiac.starstruck.Models.Worm;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.util.Arrays;

public class EditController extends WorldController implements ContactListener {

    /** Speed of camera pan & zoom */
    private static final float PAN_CONST = 4;
    private static final float ZOOM_FACTOR = 0.02f;

    /** Current obstacle */
    private Obstacle current;
    /** VectorWorld */
    private VectorWorld vectorWorld;
    /** Reference to the game level */
    protected LevelModel level;
    /** Camera offset */
    private float camOffsetX;
    private float camOffsetY;
    /** Listener for save data */
    private SaveListener save;
    /** Listener for load data */
    private SaveListener load;
    /** File to load (if non-null) */
    private String loadFile;
    /** Listener for worm data */
    private WormListener wormListener;
    /** The JSON defining the level model */
    private JsonValue  levelFormat;
    /** The reader to process JSON files */
    private JsonReader jsonReader;

    /** References to players and rope */
    private AstronautModel player1;
    private AstronautModel player2;
    private Rope rope;

    /** Possible worm textures */
    private static final String[] WORM_TEXTURES = { "blue worm", "green worm", "pink worm", "purple worm", "red worm", "yellow worm"};

    /** Initial position of player 1*/
    private static Vector2 P1_POS = new Vector2(2.5f, 5.0f);
    /** Initial position of player 2*/
    private static Vector2 P2_POS = new Vector2(3.5f, 6.5f);
    /** The width of the rope bridge */
    private static final float  BRIDGE_WIDTH = 6.0f;

    public class WormListener implements Input.TextInputListener {

        public float vx;
        public Worm worm;

        public WormListener(){
            vx = 0; worm = null;
        }

        public void input (String text) {
            try {
                vx = Float.parseFloat(text);
                setVel();
            } catch (Exception e) {
                vx = 0;
                System.out.println("Error setting velocity");
            }
        }

        public void canceled () {
            vx = 0;
            worm = null;
        }

        public void setVel() {
            worm.setVX(vx);
            worm = null; vx = 0;
        }
    }


    public EditController() {
        super(DEFAULT_WIDTH,DEFAULT_HEIGHT,DEFAULT_GRAVITY);
        level = new LevelModel(bounds, scale);
        setDebug(false);
        setComplete(false);
        setFailure(false);
        world.setContactListener(this);
//        sensorFixtures = new ObjectSet<Fixture>();
        current = null;
        vectorWorld = new VectorWorld();
        save = new SaveListener();
        wormListener = new WormListener();
        jsonReader = new JsonReader();
        loadFile = null;
        levelFormat = null;
    }

    public void reset() {
        level.dispose();

        if (loadFile != null) {
            levelFormat = jsonReader.parse(Gdx.files.internal("levels/" + loadFile));
            level.populate(levelFormat);
        } else {

            level.setBackground(JsonAssetManager.getInstance().getEntry("background", Texture.class));
            createPlayers();
        }
        level.getWorld().setContactListener(this);
        world = level.getWorld();

        current = null;

        setComplete(false);
        setFailure(false);
        save = new SaveListener();
        load = new SaveListener();
    }

    private void createPlayers() {
        float dwidth;
        float dheight;
        TextureRegion texture;

        camOffsetX = 0;
        camOffsetY = 0;

        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();
        camera.viewportWidth = canvas.getWidth();
        camera.viewportHeight = canvas.getHeight();
        camera.position.x = camera.viewportWidth/2;
        camera.position.y = camera.viewportHeight/2;
        System.out.println(camera.zoom);

        texture = JsonAssetManager.getInstance().getEntry("astronaut 1", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        player1 = new AstronautModel(P1_POS.x, P1_POS.y, dwidth, dheight, true, true);
        player1.setDrawScale(scale);
        player1.setTexture(texture);
        player1.setGlow(JsonAssetManager.getInstance().getEntry("glow", TextureRegion.class));
        player1.setName("avatar 1");

        texture = JsonAssetManager.getInstance().getEntry("astronaut 2", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        player2 = new AstronautModel(P2_POS.x, P2_POS.y, dwidth, dheight, false, false);
        player2.setDrawScale(scale);
        player2.setTexture(texture);
        player2.setGlow(JsonAssetManager.getInstance().getEntry("glow", TextureRegion.class));
        player2.setName("avatar 2");

        level.add(player1); level.add(player2);

        texture = JsonAssetManager.getInstance().getEntry("rope", TextureRegion.class);
        dwidth = texture.getRegionWidth()/scale.x;
        dheight = texture.getRegionHeight()/scale.y;
        rope = new Rope(player1.getX() + 0.5f, player1.getY() + 0.5f, BRIDGE_WIDTH, dwidth, dheight, player1, player2);
        rope.setTexture(texture);
        rope.setDrawScale(scale);
        rope.setName("rope");
        level.add(rope);

    }

    /**
     * Update worm's texture and velocity fields dependent on user input.
     */
    private void updateWorm() {
        InputController input = InputController.getInstance();

       // System.out.println(input.shiftHeld());

        if (input.didPrimary()){
            Worm wormy = (Worm) current;
            String key = JsonAssetManager.getInstance().getKey(wormy.getTexture());
            System.out.println(key);
            int i = Arrays.binarySearch(WORM_TEXTURES, key);
            wormy.setTexture(JsonAssetManager.getInstance().getEntry(WORM_TEXTURES[(i + 1) % WORM_TEXTURES.length], FilmStrip.class));

        } else if (input.didDown()) {
            Worm wormy = (Worm) current;
            String key = JsonAssetManager.getInstance().getKey(wormy.getTexture());
            int i = Arrays.binarySearch(WORM_TEXTURES, key);
            wormy.setTexture(JsonAssetManager.getInstance().getEntry(WORM_TEXTURES[i == 0 ? WORM_TEXTURES.length - 1 : (i - 1) % WORM_TEXTURES.length], FilmStrip.class));
        } else if (input.shiftHeld() && input.didTertiary()){
            wormListener.worm = (Worm) current;
            Gdx.input.getTextInput(wormListener, "Set velocity to...", Float.toString(current.getVX()), "");
            current = null;
        }
    }

    /**
     * Helper to update current obstacle if it is a planet.
     */
    private void updatePlanet() {
        OrthographicCamera camera = (OrthographicCamera)canvas.getCamera();
        InputController input = InputController.getInstance();
        float camScaleX = camOffsetX / scale.x;
        float camScaleY = camOffsetY / scale.y;
        float w = (input.xPos() - canvas.getWidth()/2) * (camera.zoom-1) / scale.x;
        float h = (canvas.getHeight()/2 - input.yPos()) * (camera.zoom-1) / scale.y;

        if (input.didPrimary()){
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x + camScaleX + w, pos.y + camScaleY + h, p.getInd() + 1, world, scale);
            level.add(current);
        } else if (input.didDown()) {
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x + camScaleX + w, pos.y + camScaleY + h, p.getInd() - 1, world, scale);
            level.add(current);
        }
    }

    /**
     * Helper function to process clicking
     */
    private void updateClick() {
        InputController input = InputController.getInstance();
        if (current != null) {
            current = null;
        }
        else {
            for (Obstacle obj : level.getAllObjects()) {
                OrthographicCamera camera = (OrthographicCamera)canvas.getCamera();
                Vector2 camOffset = new Vector2(camOffsetX/scale.x, camOffsetY/scale.y);
                Vector2 zoom = new Vector2((input.xPos() - canvas.getWidth()/2) * (camera.zoom-1) / scale.x,
                (canvas.getHeight()/2 - input.yPos()) * (camera.zoom-1) / scale.y);
                if (obj.containsPoint(input.getCrossHair().cpy().add(camOffset).add(zoom))) {
                    current = obj;
                }
            }
        }
    }

    /**
     * Helper function to update camera panning with arrow keys when no planet is selected
     */
    private void updateCamera() {
        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();
        InputController input = InputController.getInstance();
        if (input.didLeft()) {
            camera.position.x = camera.position.x - PAN_CONST;
            camOffsetX = camOffsetX - PAN_CONST;
        }
        if (input.heldUp()) {
            camera.position.y = camera.position.y + PAN_CONST;
            camOffsetY = camOffsetY + PAN_CONST;
        }
        if (input.didRight()) {
            camera.position.x = camera.position.x + PAN_CONST;
            camOffsetX = camOffsetX + PAN_CONST;
        }
        if (input.heldDown()) {
            camera.position.y = camera.position.y - PAN_CONST;
            camOffsetY = camOffsetY - PAN_CONST;
        }
        if (input.shiftHeld() && input.heldUp()) {
//            camera.viewportWidth = camera.viewportWidth * (1-ZOOM_FACTOR);
//            camera.viewportHeight = camera.viewportHeight * (1-ZOOM_FACTOR);
            camera.zoom = camera.zoom - ZOOM_FACTOR;
            //System.out.println(camera.zoom);
        }
        if (input.shiftHeld() && input.heldDown()) {
//            camera.viewportWidth = camera.viewportWidth * (1+ZOOM_FACTOR);
//            camera.viewportHeight = camera.viewportHeight * (1+ZOOM_FACTOR);
            camera.zoom = camera.zoom + ZOOM_FACTOR;
            //System.out.println(camera.zoom);
        }
        camera.update();
    }

    /**
     * Try resetting the current level to the level in loader; return true if succesful.
     * @return If the level was successfully reset.
     */
    private boolean loadNewFile() {
        try {
            levelFormat = jsonReader.parse(Gdx.files.internal("levels/" + load.file));
            level.populate(levelFormat);
            loadFile = load.file;
            load.file = null;

            return true;
        } catch (Exception e) {
            load.file = null;
            return false;
        }
    }

    public void update(float dt) {
        OrthographicCamera camera = (OrthographicCamera)canvas.getCamera();
        //System.out.println(wormListener.worm);
        //System.out.println(current);
        int i = 0;
//        for (Obstacle obj : level.getAllObjects()) {
//            i += obj.getType() == ObstacleType.ANCHOR ? 1 : 0;
//        }
//        System.out.println(i);
        //System.out.println(level.getAllObjects());
        //System.out.println(level.getPlayer1().getVX() + "   " + level.getPlayer1().getVY());
        InputController input = InputController.getInstance();

        float camScaleX = camOffsetX / scale.x;
        float camScaleY = camOffsetY / scale.y;

        float w = (input.xPos() - canvas.getWidth()/2) * (camera.zoom-1) / scale.x;
        float h = (canvas.getHeight()/2 - input.yPos()) * (camera.zoom-1) / scale.y;

        if (current == null)
            updateCamera();
        if (save.file != null) {
            System.out.println(level.toJSON());
            JsonValue saveVal = level.toJSON();
            String saveName = save.file;
            FileHandle saveFile = Gdx.files.local("levels/" + saveName);
            PrettyPrintSettings saveSettings = new PrettyPrintSettings();
            saveSettings.outputType = JsonWriter.OutputType.json;
            saveFile.writeString(saveVal.prettyPrint(saveSettings), false);
            load.file = save.file;
            save.file = null;
        }

        if (load.file != null) {
            if (loadNewFile()) {
                reset();
                return;
            }
        }

        if (current != null) {
            if (input.didBackspace() && current.getType() != ObstacleType.PLAYER) {
                level.remove(current);
                current = null;
            } else {
                current.setPosition((input.xPos() + camOffsetX) / scale.x + w,
                        -((input.yPos() - camOffsetY) / scale.y) + h + bounds.height);
                switch (current.getType()) {
                    case PLANET:
                        updatePlanet(); break;
                    case WORM: updateWorm(); break;
                }
            }
        } else {
            if (input.shiftHeld() && input.didS()) {
                Gdx.input.getTextInput(save, "Save as...", "level.json", "");
            } else if (input.shiftHeld() && input.didO()) {
                Gdx.input.getTextInput(load, "Load...", "level.json", "");
            } else if (input.shiftHeld() && input.didD()) {
                loadFile = null;
                reset();
                return;
            } else if (input.didP()) {
                Vector2 pos = input.getCrossHair();
                current = new Planet(pos.x + camScaleX + w, pos.y + camScaleY + h, 1, world, scale);
                level.add(current);
            } else if (input.didA()) {
                Vector2 pos = input.getCrossHair();
                current = new Anchor(pos.x + camScaleX + w, pos.y + camScaleY + h,
                        JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale);
                level.add(current);
            } else if (input.didS()) {
                Vector2 pos = input.getCrossHair();
                current = new Star(pos.x + camScaleX + w, pos.y + camScaleY + h,
                        JsonAssetManager.getInstance().getEntry("star", TextureRegion.class), scale);
                level.add(current);
            } else if (input.didW()){
                Vector2 pos = input.getCrossHair();
                current = new Worm(pos.x + camScaleX + w, pos.y + camScaleY + h,
                        JsonAssetManager.getInstance().getEntry("pink worm", FilmStrip.class), scale, 0);
                level.add(current);
            }
            if (input.mouseDragged()) {
                updateCamera();
            }
        }

        if (input.didTertiary()) {
            updateClick();
        }

        for (Enemy e : level.getEnemies()) {
            e.update(dt);
        }

    }

    /**
     * Override superclass's setDebug to also edit the level's current state
     * @param d The new value for debug
     */
    public void setDebug(boolean d) {
        super.setDebug(d);
        level.setDebug(d);
    }

    public void draw(float dt) {
        canvas.clear();

        level.draw(canvas);

//        canvas.begin();
//        if (current != null) {
//            current.draw(canvas);
//        }
//
//        canvas.end();
    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {}

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {}

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}
}
