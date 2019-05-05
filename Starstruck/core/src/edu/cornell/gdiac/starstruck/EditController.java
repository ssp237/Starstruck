package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.PrettyPrintSettings;
import com.badlogic.gdx.utils.JsonWriter;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.*;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.FilmStrip;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.util.Arrays;

public class EditController extends WorldController implements ContactListener {

    /** Speed of camera pan & zoom */
    private static final float PAN_CONST = 8;
    private static final float ZOOM_FACTOR = 0.02f;
    /** Bounds of this level */
    private float screenX;
    private float screenY;
//    private float xBound = (1280*screenX) / scale.x;
//    private float yBound = (720*screenY) / scale.y;
    /** Percentage of stars needed to open end portal */
    private float winCond;

    private int portalPair = 1;
    private int task = 1;

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
    /** Reader to process galaxy switches */
    private GalaxyListener galListener;
    /** Reader to set bounds of level*/
    private BoundsListener boundListener;
    /** Listener to set win condition */
    private WinListener winListener;

    /** Current galaxy to source assets from */
    private Galaxy galaxy;

    /** References to players and rope */
    private AstronautModel player1;
    private AstronautModel player2;
    private Rope rope;

    /** Possible worm textures */
    private static final String[] WORM_TEXTURES = { "blue worm", "green worm", "pink worm", "purple worm", "red worm", "yellow worm"};
    /** Possible berry textures */
    private static final String[] BERRY_TEXTURES = { "pink berry"};
    /** Current horizontally moving enemy textures */
    private String[] FISH_TEXTURES;

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

    public class GalaxyListener implements Input.TextInputListener {

        public void input (String text) {
            //System.out.println(text);
            Galaxy gal = Galaxy.fromString(text);
            level.setGalaxy(gal);
            galaxy = gal;

            switch (gal) {
                case WHIRLPOOL: FISH_TEXTURES = WORM_TEXTURES; break;
                case MILKYWAY: FISH_TEXTURES = BERRY_TEXTURES; break;
                default: FISH_TEXTURES = WORM_TEXTURES;
            }

        }

        public void canceled () {
        }
    }

    public class BoundsListener implements Input.TextInputListener {

        public float xBound;
        public float yBound;

        public BoundsListener(){
            screenX = 1.5f;
            screenY = 1.5f;
            level.xPlay = screenX;
            level.yPlay = screenY;
        }

        public void input (String text) {
            try {
                String[] bounds = text.split(",");
                if (bounds.length != 2) {
                    System.out.println("Enter 2 numbers separated by a comma");
                    return;
                }
                xBound = Float.parseFloat(bounds[0].trim());
                yBound = Float.parseFloat(bounds[1].trim());
                screenX = xBound;
                screenY = yBound;
                level.xPlay = screenX;
                level.yPlay = screenY;

            } catch (Exception e) {
                System.out.println("Error setting bounds");
            }
        }

        public void canceled () {
        }
    }

    public class WinListener implements Input.TextInputListener {

        public float winPer;

        public WinListener(){
            winCond = 0.5f;
            level.winPercent = winCond;
        }

        public void input (String text) {
            try {
                winPer = Float.parseFloat(text);
                winCond = winPer;
                level.winPercent = winCond;

            } catch (Exception e) {
                System.out.println("Error setting win condition");
            }
        }

        public void canceled () {
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
        galListener = new GalaxyListener();
        boundListener = new BoundsListener();
        winListener = new WinListener();
        jsonReader = new JsonReader();
        loadFile = null;
        levelFormat = null;
        galaxy = Galaxy.WHIRLPOOL;
        level.setGalaxy(galaxy);
        FISH_TEXTURES = WORM_TEXTURES;
    }

    public void reset() {
        level.dispose();
        canvas.resetCamera();
        level.setGalaxy(galaxy);
        boundListener = new BoundsListener();
        winListener = new WinListener();

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
        galListener = new GalaxyListener();
    }

    private void createPlayers() {
        float dwidth;
        float dheight;
        TextureRegion texture;

        camOffsetX = 0;
        camOffsetY = 0;

        portalPair = 1;
        task = 1;

        OrthographicCamera camera = (OrthographicCamera) canvas.getCamera();
        camera.viewportWidth = canvas.getWidth();
        camera.viewportHeight = canvas.getHeight();
        camera.position.x = camera.viewportWidth/2;
        camera.position.y = camera.viewportHeight/2;
        //System.out.println(camera.zoom);

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
            //System.out.println(key);
            int i = Arrays.binarySearch(FISH_TEXTURES, key);
            wormy.setTexture(JsonAssetManager.getInstance().getEntry(FISH_TEXTURES[(i + 1) % FISH_TEXTURES.length], FilmStrip.class));

        } else if (input.didDown()) {
            Worm wormy = (Worm) current;
            String key = JsonAssetManager.getInstance().getKey(wormy.getTexture());
            int i = Arrays.binarySearch(FISH_TEXTURES, key);
            wormy.setTexture(JsonAssetManager.getInstance().getEntry(FISH_TEXTURES[i == 0 ? FISH_TEXTURES.length - 1 : (i - 1) % FISH_TEXTURES.length], FilmStrip.class));
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
        InputController input = InputController.getInstance();

        if (input.didPrimary()){
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x, pos.y, p.getInd() + 1, world, scale);
            level.add(current);
        } else if (input.didDown()) {
            Planet p = (Planet) current;
            level.remove(p);
            Vector2 pos = p.getPosition();
            current = new Planet(pos.x, pos.y, p.getInd() - 1, world, scale);
            level.add(current);
        }
    }

    /**
     * Helper to update current obstacle if it is an Urchin.
     */
    private void updateUrchin() {
        InputController input = InputController.getInstance();

        if (input.didPrimary()){
            Urchin u = (Urchin) current;
            level.remove(u);
            Vector2 pos = u.getPosition();
            current = new Urchin(pos.x , pos.y , scale, u.getLength() + 1, u.getOrientation());
            level.add(current);
        } else if (input.didDown()) {
            Urchin u = (Urchin) current;
            level.remove(u);
            Vector2 pos = u.getPosition();
            current = new Urchin(pos.x, pos.y, scale, Math.max(u.getLength() - 1,1), u.getOrientation());
            level.add(current);
        } else if ((input.didLeft() && !input.leftPrevious()) || (input.didRight() && !input.rightPrevious())) {
            Urchin u = (Urchin) current;
            level.remove(u);
            Vector2 pos = u.getPosition();
            CapsuleObstacle.Orientation orie = u.getOrientation() == CapsuleObstacle.Orientation.VERTICAL ? CapsuleObstacle.Orientation.HORIZONTAL : CapsuleObstacle.Orientation.VERTICAL;
            current = new Urchin(pos.x, pos.y, scale, u.getLength(), orie);
            level.add(current);
        }
    }

    private void updatePortal() {
        OrthographicCamera camera = (OrthographicCamera)canvas.getCamera();
        InputController input = InputController.getInstance();
        float camScaleX = camOffsetX / scale.x;
        float camScaleY = camOffsetY / scale.y;
        float w = (input.xPos() - canvas.getWidth()/2) * (camera.zoom-1) / scale.x;
        float h = (canvas.getHeight()/2 - input.yPos()) * (camera.zoom-1) / scale.y;

        if (input.didPrimary()){
            Portal p = (Portal) current;
            PortalPair port = findPortalPair(p);
            if (port == null) System.out.println("updatePortal in EditController");
            String name = port.getPortalName();
            int color = port.nextColor();
            boolean goal = color == 0;
            String texture = "static portal";
            if (goal) texture = "goal";
            p = port.getPortal1();
            Portal p2 = port.getPortal2();
            Vector2 pos1 = p.getPosition();
            Vector2 pos2 = p2.getPosition();
            if (color == 1)
                pos2 = pos1.cpy().add(5, 0);
            if (!level.portalpairs.remove(port)) System.out.println("updatePortal in EditController couldn't remove port");
            level.remove(p);
            level.remove(p2);
            port = new PortalPair(pos1.x, pos1.y, pos2.x, pos2.y, name, scale,
                    JsonAssetManager.getInstance().getEntry(texture, FilmStrip.class), color, goal);
            level.add(port.getPortal1());
            level.add(port.getPortal2());
            level.portalpairs.add(port);
            current = port.getPortal1();
//        } else if (input.didDown()) {
//            Planet p = (Planet) current;
//            level.remove(p);
//            Vector2 pos = p.getPosition();
//            current = new Planet(pos.x + camScaleX + w, pos.y + camScaleY + h, p.getInd() - 1, world, scale);
//            level.add(current);
        }
    }

    /**
     * Find the PortalPair of the portal. Returns null if portal can't be found, means something's wrong
     * @param portal
     * @return
     */
    private PortalPair findPortalPair(Portal portal) {
        for(PortalPair p : level.portalpairs) {
            if (portal.getPortName().equals(p.getPortalName()))
                return p;
        }
        return null;
    }

    /**
     * Find the TutorialPoint of the point. Returns null if can't be found, means something's wrong
     * @param tutorial
     * @return
     */
    private TutorialPoint findTutPoint(Star tutorial) {
        for(TutorialPoint p : level.tutpoints) {
            if (tutorial.getTutName().equals(p.getName()))
                return p;
        }
        return null;
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
        if (input.didLeft()) { //&& camera.position.x > camera.viewportWidth/2) {
            camera.position.x = camera.position.x - PAN_CONST;
            camOffsetX = camOffsetX - PAN_CONST;
        }
        if (input.heldUp()) { //&& camera.position.y < yBound - camera.viewportHeight/2) {
            camera.position.y = camera.position.y + PAN_CONST;
            camOffsetY = camOffsetY + PAN_CONST;
        }
        if (input.didRight()) { //&& camera.position.x < xBound - camera.viewportWidth/2) {
            camera.position.x = camera.position.x + PAN_CONST;
            camOffsetX = camOffsetX + PAN_CONST;
        }
        if (input.heldDown()) { //&& camera.position.y > camera.viewportHeight/2) {
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
        //System.out.println(Arrays.toString(FISH_TEXTURES));
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
                if (current.getType() == ObstacleType.PORTAL) {
                    PortalPair port = findPortalPair((Portal)current);
                    level.remove(port.getPortal1());
                    level.remove(port.getPortal2());
                    level.portalpairs.remove(port);
                    current = null;
                }
                else if (current.getType() == ObstacleType.TUTORIAL) {
                    TutorialPoint tutorial = findTutPoint((Star)current);
                    level.remove(tutorial.getPinkPoint());
                    level.remove(tutorial.getBluePoint());
                    level.tutpoints.remove(tutorial);
                    current = null;
                }
                else {
                    level.remove(current);
                    current = null;
                }
            } else {
                current.setPosition((input.xPos() + camOffsetX) / scale.x + w,
                        -((input.yPos() - camOffsetY) / scale.y) + h + bounds.height);
                switch (current.getType()) {
                    case PLANET:
                        updatePlanet(); break;
                    case WORM: updateWorm(); break;
                    case PORTAL: updatePortal(); break;
                    case URCHIN: updateUrchin(); break;
                }
            }
        } else {
            if (input.shiftHeld() && input.didS()) {
                Gdx.input.getTextInput(save, "Save as...", "level.json", "");
            } else if (input.shiftHeld() && input.didO()) {
                Gdx.input.getTextInput(load, "Load...", "level.json", "");
            } else if (input.shiftHeld() && input.didG()) {
                Gdx.input.getTextInput(galListener, "Switch to what galaxy?", "whirlpool", "");
            } else if (input.shiftHeld() && input.didL()) {
                Gdx.input.getTextInput(boundListener, "Size of level", screenX + ", " + screenY, "");
            } else if (input.shiftHeld() && input.didW()) {
                Gdx.input.getTextInput(winListener, "Win condition", winCond + "", "");
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
                        JsonAssetManager.getInstance().getEntry(FISH_TEXTURES[0], FilmStrip.class), scale, 0);
                level.add(current);
//            } else if (input.didB()){
//                Vector2 pos = input.getCrossHair();
//                current = new Bug(pos.x + camScaleX + w, pos.y + camScaleY + h,
//                        JsonAssetManager.getInstance().getEntry(FISH_TEXTURES[0], FilmStrip.class), scale, 0);
//                level.add(current);
            } else if (input.didD()) {
                Vector2 pos = input.getCrossHair();
                float x = pos.x + camScaleX + w;
                float y = pos.y + camScaleY + h;
                PortalPair portal = new PortalPair(x, y, x+5, y, "portalpair" + portalPair, scale,
                        JsonAssetManager.getInstance().getEntry("static portal", FilmStrip.class), 1, false);
                portalPair++;
                level.add(portal.getPortal1());
                level.add(portal.getPortal2());
                level.portalpairs.add(portal);
                current = portal.getPortal1();
            } else if (input.didT()) {
                Vector2 pos = input.getCrossHair();
                float x = pos.x + camScaleX + w;
                float y = pos.y + camScaleY + h;
                TutorialPoint tutorial = new TutorialPoint(x, y, x+2, y,
                        JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class),
                        JsonAssetManager.getInstance().getEntry("static portal", FilmStrip.class), scale, "task"+task);
                task++;
                level.add(tutorial.getPinkPoint());
                level.add(tutorial.getBluePoint());
                level.tutpoints.add(tutorial);
                current = tutorial.getPinkPoint();
            } else if (input.didU()) {
                Vector2 pos = input.getCrossHair();
                current = new Urchin(pos.x + camScaleX + w, pos.y + camScaleY + h, scale, 1, CapsuleObstacle.Orientation.VERTICAL);
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

        String gal = galaxy.getChars();
        Texture background = JsonAssetManager.getInstance().getEntry(gal + " background", Texture.class);
        canvas.begin();
        canvas.draw(background, 0, 0, canvas.getWidth()*screenX, canvas.getHeight()*screenY);
        for (TutorialPoint p : level.tutpoints) {
            p.getPinkPoint().draw(canvas);
            p.getBluePoint().draw(canvas);
        }
        canvas.end();

        level.draw(canvas, 'e');

//        OrthographicCamera cam = (OrthographicCamera) canvas.getCamera();
//        float width = canvas.getWidth()/2-10;
//        float height = canvas.getHeight()/2-10;
//        displayFont = JsonAssetManager.getInstance().getEntry("retro game", BitmapFont.class);
//        displayFont.setColor(Color.RED);
//        canvas.begin();
//        canvas.drawText(screenX + ", " + screenY, displayFont, cam.position.x, cam.position.y);
//        canvas.end();

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
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            String bd1N = bd1.getName();
            String bd2N = bd2.getName();

            //Disable all collisions for worms
            if (bd1.getType() == ObstacleType.WORM || bd2.getType() == ObstacleType.WORM) {
                contact.setEnabled(false);
            }

            //Disable all collisions with portal
            if (bd1.getType() == ObstacleType.PORTAL || bd2.getType() == ObstacleType.PORTAL) {
                contact.setEnabled(false);
            }

            //Disables all collisions w rope
            if (bd1.getName().contains("rope") || bd2.getName().contains("rope")) {
                contact.setEnabled(false);
            }
            //Disables all anchor and star collisions
            if(bd1N.contains("anchor") || bd2N.contains("anchor") || bd1N.contains("star") || bd2N.contains("star")){
                contact.setEnabled(false);
            }
            //Enables collisions between rope and anchor
//            if (bd1.getName().contains("rope") && bd2.getName().contains("anchor")
//                    || bd1.getName().contains("anchor") && bd2.getName().contains("rope")) {
//                contact.setEnabled(true);
//            }

            //Disables collisions between ends of rope and anchors
//            ropeList = rope.getPlanks();
//            BoxObstacle plank0 = (BoxObstacle)ropeList.get(0);
//            BoxObstacle plank1 = (BoxObstacle)ropeList.get(1);
//            BoxObstacle plank4 = (BoxObstacle)ropeList.get(2);
//            BoxObstacle plank2 = (BoxObstacle)ropeList.get(ropeList.size()-2);
//            BoxObstacle plank3 = (BoxObstacle)ropeList.get(ropeList.size()-1);
//            BoxObstacle plank5 = (BoxObstacle)ropeList.get(ropeList.size()-3);
//            if (bd1N.contains("anchor") && (bd2 == plank0 || bd2 == plank1 || bd2 == plank2 || bd2 == plank3)
//                    || bd2N.contains("anchor") && (bd1 == plank0 || bd1 == plank1 || bd1 == plank2 || bd1 == plank3)) {
//                contact.setEnabled(false);
//            }

            //Enables collisions between rope and planet
            if (bd1.getName().contains("rope") && bd2.getName().contains("planet")
                    || bd1.getName().contains("planet") && bd2.getName().contains("rope")) {
                contact.setEnabled(true);
            }
            //Disable collisions between astronauts
            if (bd1.getName().contains("avatar") && bd2.getName().contains("avatar")) {
                contact.setEnabled(false);
            }
            //Disables collisions between astronauts and anchors
            if (bd1.getName().contains("avatar") && bd2.getName().contains("anchor")
                    || bd1.getName().contains("anchor") && bd2.getName().contains("avatar")) {
                contact.setEnabled(false);
            }
            //Disables collisions between astronauts and stars
            if (bd1.getName().contains("avatar") && bd2.getName().contains("star")
                    || bd1.getName().contains("star") && bd2.getName().contains("avatar")) {
                contact.setEnabled(false);
            }


            //Disable the collision between anchors and rope on avatar
            int n = rope.nLinks() - 1 ;//(int) BRIDGE_WIDTH*2-1;
            if ((bd1N.contains("anchor") || bd2N.contains("anchor")) && (
                    bd1N.equals("rope_plank0") || bd2N.equals("rope_plank0") ||
                            bd1N.equals("rope_plank"+n) || bd2N.equals("rope_plank"+n))) {
                contact.setEnabled(false);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
