/*
 * LevelModel.java
 *
 * This stores all of the information to define a level in our simple platform game.
 * We have an avatar, some walls, some platforms, and an exit.  This is a refactoring
 * of WorldController in Lab 4 that separates the level data from the level control.
 *
 * Note that most of the methods are getters and setters, as is common with models.
 * The gameplay behavior is defined by GameController.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * JSON version, 3/2/2016
 *
 * Edited by Starstruck
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.AstronautModel;
import edu.cornell.gdiac.starstruck.Models.Enemy;
import edu.cornell.gdiac.starstruck.Models.Worm;
import edu.cornell.gdiac.starstruck.Obstacles.*;
import edu.cornell.gdiac.util.JsonAssetManager;
import edu.cornell.gdiac.util.PooledList;

import java.util.ArrayList;

/**
 * Represents a single level in our game
 *
 * Note that the constructor does very little.  The true initialization happens
 * by reading the JSON value.  To reset a level, dispose it and reread the JSON.
 *
 * The level contains its own Box2d World, as the World settings are defined by the
 * JSON file.  However, there is absolutely no controller code in this class, as
 * the majority of the methods are getters and setters.  The getters allow the
 * GameController class to modify the level elements.
 */
public class LevelSelectModel {

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    private Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The background texture*/
    private Texture background;
    /** height of canvas */
    private int heightY = 720;

    // Physics objects for the game
    /** Reference to the first character avatar */
    private AstronautModel player;
    /** Reference to the list of planets */
    private PooledList<Level> levels = new PooledList<Level>();
    /** Whether or not the level is in debug more (showing off physics) */
    private boolean debug;
    /** All the objects in the world. */
    protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
    /** List of portal pairs */
    protected ArrayList<PortalPair> portalpairs = new ArrayList<PortalPair>();

    /**Pointer to the first level */
    public Level firstLevel;

    /**
     * Returns the bounding rectangle for the physics world
     *
     * The size of the rectangle is in physics, coordinates, not screen coordinates
     *
     * @return the bounding rectangle for the physics world
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * Returns the scaling factor to convert physics coordinates to screen coordinates
     *
     * @return the scaling factor to convert physics coordinates to screen coordinates
     */
    public Vector2 getScale() {
        return scale;
    }

    /**
     * Returns a reference to the Box2D World
     *
     * @return a reference to the Box2D World
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns a reference to the levels list
     *
     * @return a reference to the levels list
     */
    public PooledList getLevels() {
        return levels;
    }

    /**
     * Returns a reference to the first player avatar
     *
     * @return a reference to the first player avatar
     */
    public AstronautModel getPlayer() {
        return player;
    }


    /**
     * Returns whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @return whether this level is currently in debug node
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Sets whether this level is currently in debug node
     *
     * If the level is in debug mode, then the physics bodies will all be drawn as
     * wireframes onscreen
     *
     * @param value	whether this level is currently in debug node
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Creates a new LevelModel
     *
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelSelectModel() {
        this(new Rectangle(0,0,1,1), new Vector2(1,1));
    }

    /**
     * Creates a new LevelModel with the specified bounds and scale.
     *
     * @param bounds The bounds for this level.
     * @param scale The scale for this level.
     */
    public LevelSelectModel(Rectangle bounds, Vector2 scale) {
        world  = new World(new Vector2(0,0), false);
        this.bounds = bounds;
        this.scale = scale;
        debug  = false;
    }

    /**
     * Set the background to the specified texture.
     * @param background The texture to use as the background.
     */
    public void setBackground(Texture background) {
        this.background = background;
    }

    /**
     * Return a reference to the background
     *
     * @return Texture background
     */
    public Texture getBackground() { return background; }

    /**
     * Lays out the game geography from the given JSON file. Requires planets is not null.
     *
     * @param levelFormat	the JSON file defining the level
     */
    public void populate(JsonValue levelFormat) {
        float[] pSize = levelFormat.get("physicsSize").asFloatArray();
        int[] gSize = levelFormat.get("graphicSize").asIntArray();

        String key = levelFormat.get("background").asString();
        background = JsonAssetManager.getInstance().getEntry(key, Texture.class);

        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = gSize[0]/pSize[0];
        scale.y = gSize[1]/pSize[1];

        player = AstronautModel.fromJson(levelFormat.get("astronaut 1"), scale, true);
        player.setName("avatar");
        player.activatePhysics(world);
        player.setActive(false);
        objects.add(player);

        JsonValue levelVal = levelFormat.get("levels").child();
        Level lastLevel = null;
        while (levelVal != null) {
            Level level = Level.fromJSON(levelVal, scale, world);
            if (lastLevel != null) lastLevel.nextLevel = level;
            else firstLevel = level;
            level.lastLevel = lastLevel;
            levels.add(level);
            levelVal = levelVal.next();
            lastLevel = level;
        }

    }

    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        for(Level l : levels){
            l.deactivatePhysics(world);
        }
        objects.clear();
        if (world != null) {
            world.dispose();
            world = new World(new Vector2(0,0), false);
        }
        objects.clear();
        levels.clear();
    }

    /**
     * Add the specified obstacle to this LevelModel
     * @param obj The obstacle to be added to this LevelModel
     */
    public void add(Obstacle obj) {
        switch (obj.getType()) {
            case LEVEL: levels.add((Level) obj); break;
            case PLAYER: addPlayer((AstronautModel) obj); break;
        }
    }

    /**
     * Uh remove idk
     * Player and rope can NOT be removed, so this method will do nothing.
     *
     * @param obj Object to be removed.
     */
    public void remove(Obstacle obj) {
        switch (obj.getType()) {
            case LEVEL: deactivate(obj); break;
            case PLAYER: deactivate(player); break;
        }
    }

    /**
     * Handles case of adding a player
     * @param playr Player to be added
     */
    private void addPlayer(AstronautModel playr) {
        player = playr;
        activate(player);
    }

    /**
     * Immediately adds the object to the physics world
     *
     * @param obj The object to add
     */
    protected void activate(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Remove the object from the physics world
     *
     * @param obj The object to remove
     */
    private void deactivate(Obstacle obj) {
        objects.remove(obj);
        obj.deactivatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    private boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    public PooledList<Obstacle> getAllObjects() {
        PooledList<Obstacle> out = new PooledList<Obstacle>();
        out.addAll(objects);
        out.addAll(levels);
        return out;
    }

    /**
     * Write this level to a JsonValue that, when parsed, would return the same level.
     * @return A JsonValue representing this level.
     */
    public JsonValue toJSON(){
        JsonValue out = new JsonValue(JsonValue.ValueType.object);

        //Add size fields
        JsonValue physicsSize = new JsonValue(JsonValue.ValueType.array);
        physicsSize.addChild(new JsonValue(bounds.width));
        physicsSize.addChild(new JsonValue(bounds.height));

        JsonValue graphicsSize = new JsonValue(JsonValue.ValueType.array);
        graphicsSize.addChild(new JsonValue((int) (bounds.width * scale.x)));
        graphicsSize.addChild(new JsonValue((int) (bounds.height * scale.y)));

        out.addChild("physicsSize", physicsSize);
        out.addChild("graphicSize", graphicsSize);

        //Add background
        out.addChild("background", new JsonValue(JsonAssetManager.getInstance().getKey(background)));

        //Add rocket
        out.addChild("rocket", player.toJson());

        //Add levels
        JsonValue levelList = new JsonValue(JsonValue.ValueType.array);
        for (Level l : levels) {
            levelList.addChild(l.toJson());
        }
        out.addChild("levels", levelList);

        return out;
    }

    /**
     * Draws the level to the given game canvas
     *
     * If debug mode is true, it will outline all physics bodies as wireframes. Otherwise
     * it will only draw the sprite representations.
     *
     * @param canvas	the drawing context
     */
    public void draw(GameCanvas canvas) {

        canvas.clear();

        canvas.begin();

//        float x = (float) Math.floor((canvas.getCamera().position.x - canvas.getWidth()/2)/canvas.getWidth()) * canvas.getWidth();
//        float y = (float) Math.floor((canvas.getCamera().position.y - canvas.getHeight()/2)/canvas.getHeight()) * canvas.getHeight();

        canvas.draw(background, 0, 0);

        for(Level l : levels){
            l.draw(canvas);
        }

        for(Obstacle obj : objects) {
            if (obj.getType() != ObstacleType.PLAYER) obj.draw(canvas);
        }

//        player.draw(canvas);

        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for(Level l : levels){
                l.drawDebug(canvas);
            }
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }
}
