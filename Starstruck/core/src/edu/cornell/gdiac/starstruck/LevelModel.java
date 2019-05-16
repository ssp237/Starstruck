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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.physics.box2d.*;

import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;
import edu.cornell.gdiac.starstruck.Models.*;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.starstruck.Obstacles.*;

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
public class LevelModel {

    /** The Box2D world */
    protected World world;
    /** The vector world */
    private VectorWorld vectorWorld;
    /** The boundary of the world */
    private Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** The background texture*/
    private Texture background;
    /** Galaxy to source textures from */
    private Galaxy galaxy;


    // Physics objects for the game
    /** Reference to the first character avatar */
    private AstronautModel player1;
    /** Reference to the second character avatar */
    private AstronautModel player2;
    /** End goal */
    private PortalPair goal;
    /** Reference to the list of planets */
    private PlanetList planets;
    /** Rope */
    private Rope rope;
    /** Whether or not the level is in debug more (showing off physics) */
    private boolean debug;
    /** AstronautModel cache */
    AstronautModel astroCache;
    /** All the objects in the world. */
    protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
    /** List of stars in the world */
    protected ArrayList<Star> stars = new ArrayList<Star>();
    /** List of anchors in the world */
    protected ArrayList<Anchor> anchors = new ArrayList<Anchor>();
    /** List of tutorial points in this level */
    protected ArrayList<TutorialPoint> tutpoints = new ArrayList<TutorialPoint>();

    /** List of speechBubbles in this level */
    protected ArrayList<TutorialPoint> speechBubbles = new ArrayList<TutorialPoint>();

    /** Rope texture for extension method */
    //protected TextureRegion ropeTexture;
    /** List of enemies in the world */
    protected PooledList<Enemy> enemies = new PooledList<Enemy>();
    /** List of portal pairs */
    protected ArrayList<PortalPair> portalpairs = new ArrayList<PortalPair>();
    /** Fraction of total stars needed to win */
    protected float winPercent;
    /** Numbher of stars needed to open portal */
    protected int winCount;
    /** Bounds of play space in terms of screens */
    protected float xPlay;
    protected float yPlay;

    /**Is there a boss in this level?*/
    private boolean hasBoss;

    private boolean isTutorial = false;

    public boolean getTutorial() {return isTutorial;}

    private TalkingBoss talkingboss;

    private SpeechBubble speechBubble;

    public TalkingBoss getTalkingBoss() {return talkingboss;}

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
     * Returns a reference to the vector World
     *
     * @return a reference to the vector World
     */
    public VectorWorld getVectorWorld() {
        return vectorWorld;
    }

    /**
     * Returns a reference to the enemy list
     *
     * @return a reference to the enemy list
     */
    public PooledList<Enemy> getEnemies() {
//        System.out.println("in getEnemies");
//        System.out.println(enemies);
        return enemies;

    }

    /**
     * Returns a reference to the planet list
     *
     * @return a reference to the planet list
     */
    public PlanetList getPlanets() {
        return planets;
    }

    /**
     * Returns a reference to the first player avatar
     *
     * @return a reference to the first player avatar
     */
    public AstronautModel getPlayer1() {
        return player1;
    }

    /**
     * Returns a reference to the second player avatar
     *
     * @return a reference to the second player avatar
     */
    public AstronautModel getPlayer2() {
        return player2;
    }

    /**
     * Returns a reference to the rope
     *
     * @return a reference to the rope
     */
    public Rope getRope() {
        return rope;
    }

    /**
     * Returns a reference to the goal
     *
     * @return a reference to the goal
     */
    public PortalPair getGoal() {
        return goal;
    }

    /**
     * Returns the current galaxy
     *
     * @return the current galaxy
     */
    public Galaxy getGalaxy() {
        return galaxy;
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
     * Sets the current galaxy: tells all relevant classes to use assets from the selected Galaxy.
     *
     * @param galaxy The galaxy to be set
     */
    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
        Planet.setGalaxy(galaxy);
        String gal = galaxy.getChars();
        this.background = JsonAssetManager.getInstance().getEntry(gal + " background", Texture.class);
        Urchin.setTextures(galaxy.getUrchinPrefix());
    }

    /**
     * Creates a new LevelModel
     *
     * The level is empty and there is no active physics world.  You must read
     * the JSON file to initialize the level
     */
    public LevelModel() {
        this(new Rectangle(0,0,1,1), new Vector2(1,1));
    }

    /**
     * Creates a new LevelModel with the specified bounds and scale.
     *
     * @param bounds The bounds for this level.
     * @param scale The scale for this level.
     */
    public LevelModel(Rectangle bounds, Vector2 scale) {
        world  = new World(new Vector2(0,0), false);
        this.bounds = bounds;
        this.scale = scale;
        debug  = false;
        planets = new PlanetList(scale);
        hasBoss = false;
    }



    /**
     * Set the background to the specified texture.
     * @param background The texture to use as the background.
     */
    public void setBackground(Texture background) {
        this.background = background;
    }

    /**
     * Lays out the game geography from the given JSON file. Requires planets is not null.
     *
     * @param levelFormat	the JSON file defining the level
     */
    public void populate(JsonValue levelFormat) {
        float[] pSize = levelFormat.get("physicsSize").asFloatArray();
        int[] gSize = levelFormat.get("graphicSize").asIntArray();
        float[] playSize = levelFormat.get("playSize").asFloatArray();



        String key = levelFormat.get("background").asString();
        background = JsonAssetManager.getInstance().getEntry(key, Texture.class);

        String gal = levelFormat.get("galaxy").asString();
        setGalaxy(Galaxy.fromString(gal));

        hasBoss = levelFormat.get("has boss").asBoolean();

        bounds = new Rectangle(0,0,pSize[0],pSize[1]);
        scale.x = gSize[0]/pSize[0];
        scale.y = gSize[1]/pSize[1];
        xPlay = playSize[0];
        yPlay = playSize[1];



        player1 = AstronautModel.fromJson(levelFormat.get("astronaut 1"), scale, true);
        player1.setName("avatar");
        player1.setGalaxy(galaxy);
        player1.activatePhysics(world);
        //addObject(player1);

        player2 = AstronautModel.fromJson(levelFormat.get("astronaut 2"), scale, false);
        player2.setName("avatar2");
        player2.setGalaxy(galaxy);
        player2.activatePhysics(world);

        //objects.remove(player1); objects.remove(player2);

        JsonValue ropeVal = levelFormat.get("rope");

        key = ropeVal.get("texture").asString();
        TextureRegion texture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        //ropeTexture = JsonAssetManager.getInstance().getEntry(key, TextureRegion.class);
        float dwidth = texture.getRegionWidth() / scale.x;
        float dheight = texture.getRegionHeight() / scale.y;
        rope = new Rope(player1.getX() + 0.5f, player1.getY() + 0.5f,
                ropeVal.get("rope width").asFloat(), dwidth, dheight, player1, player2);
        rope.setTexture(texture);
        rope.setDrawScale(scale);
        rope.setName("rope");
        activate(rope);

        objects.add(player1);
        objects.add(player2);

//        goal = BlackHole.fromJson(levelFormat.get("goal"), scale);
//        goal.setName("goal");
//        activate(goal);

        Planet.setPresets(levelFormat.get("planet specs"));
        planets = new PlanetList(scale);

        JsonValue planet = levelFormat.get("planets").child();
        while(planet != null) {
            float x = planet.getFloat("x");
            float y = planet.getFloat("y");
            int i = planet.getInt("i");
            Bug buggy = null;
            try {
                JsonValue bug = planet.get("bug");
                float radius = Planet.getRadiusPrePlanet(i, scale);
                key = bug.get("texture").asString();
                FilmStrip bugtexture = JsonAssetManager.getInstance().getEntry(key, FilmStrip.class);
                try {
                    key = bug.get("color").asString();
                    FilmStrip sleeptexture = JsonAssetManager.getInstance().getEntry(key + " bug asleep", FilmStrip.class);
                    ModelColor modelColor = key.equals("pink") ? ModelColor.PINK : ModelColor.BLUE;
                    buggy = new ColoredBug(x, y + radius + (bugtexture.getRegionHeight()/scale.y)/2 - 3/scale.y, bugtexture, sleeptexture, scale, modelColor);
                } catch (Exception e) {
                    buggy = new Bug(x, y + radius + (bugtexture.getRegionHeight()/scale.y)/2 - 3/scale.y, bugtexture, scale);
                    buggy.vectorWorld = vectorWorld;
                }

                activate(buggy);
                enemies.add(buggy);
            } catch (Exception e) {

            }

            planets.addPlanet(planet, world, vectorWorld, buggy);

            planet = planet.next();

        }

        //add stars
        int i = 0;
        JsonValue starVals = levelFormat.get("stars").child();
        while (starVals != null) {
            Star star = Star.fromJSON(starVals, scale);
            star.setName("star" + i);
            activate(star);
            star.setGalaxy(galaxy);
            stars.add(star);
            starVals = starVals.next;
        }

        winPercent = levelFormat.get("win").asFloat();
        int numStars = stars.size();
        winCount = (int)(numStars * winPercent);

        //add tutorial points
        i = 0;
        JsonValue tutorialVals = levelFormat.get("tutorialpoints").child();
        if (tutorialVals != null) {
            isTutorial = true;
        }
        while (tutorialVals != null) {
            TutorialPoint tutpoint = TutorialPoint.fromJSON(tutorialVals, scale);
            activate(tutpoint.getPinkPoint());
            activate(tutpoint.getBluePoint());
            tutpoints.add(tutpoint);
            tutorialVals = tutorialVals.next;
        }


        //add anchors
        i = 0;
        JsonValue anchorVals = levelFormat.get("anchors").child();
        while(anchorVals != null) {
            Anchor anchor = Anchor.fromJSON(anchorVals, scale);
            anchor.setName("anchor" + i);
            activate(anchor);
            anchor.setGalaxy(galaxy);
            anchors.add(anchor);
            anchorVals = anchorVals.next;
        }

        //add portals
        i = 0;
        JsonValue portalVals = levelFormat.get("portalpairs").child();
        while (portalVals != null) {
            PortalPair portalpair = PortalPair.fromJSON(portalVals, scale);
            activate(portalpair.getPortal1());
            activate(portalpair.getPortal2());
            portalpairs.add(portalpair);
            if (portalpair.isGoal())
                goal = portalpair;
            portalVals = portalVals.next;
        }

        //add worms
        JsonValue wormVals = levelFormat.get("worms").child();
        while (wormVals != null) {
            Worm wormie = Worm.fromJSON(wormVals, scale);
            activate(wormie);
            enemies.add(wormie);
            wormVals = wormVals.next;
        }

        //add urchins
        String urcTexture = levelFormat.get("urchin texture").asString();
        Urchin.setTextures(urcTexture);

        JsonValue urchinVals = levelFormat.get("urchins").child();
        while (urchinVals != null) {
            Urchin urch = Urchin.fromJSON(urchinVals, scale);
            activate(urch);
            enemies.add(urch);
            urchinVals = urchinVals.next;
        }

        //add ice cream
        JsonValue creamVals = levelFormat.get("ice cream").child();
        while(creamVals != null) {
            IceCream iceCream = IceCream.fromJSON(creamVals, scale);
            iceCream.setUpBound(bounds.getHeight() * yPlay);
            activate(iceCream);
            enemies.add(iceCream);
            creamVals = creamVals.next;
        }

        //add boss
        if (hasBoss) {
            if (galaxy == Galaxy.WHIRLPOOL) {
                OctoLeg.setTextures("octo");
                JsonValue octoLegs = levelFormat.get("octopus legs").child();
                while(octoLegs != null) {
                    OctoLeg octopuss = OctoLeg.fromJSON(octoLegs, scale);
                    activate(octopuss);
                    //enemies.add(octopuss);
                    octoLegs = octoLegs.next;
                    System.out.println(octopuss);

                    TextureRegion textureboss = JsonAssetManager.getInstance().getEntry("octoboss talk", TextureRegion.class);
                    talkingboss = new TalkingBoss(3.5f, 14, textureboss, scale, 0);
                    activate(talkingboss);

                    TextureRegion texturebubble = JsonAssetManager.getInstance().getEntry("octoboss bubble", TextureRegion.class);
                    speechBubble = new SpeechBubble(12.5f, 16.5f, scale, texturebubble);
                    activate(speechBubble.getBubble());
                    //activate(speechBubble);
                    //System.out.println(scale);

                }
            } else if (galaxy == Galaxy.SOMBRERO) {
                JsonValue wheels = levelFormat.get("aztec wheels").child();
                while(wheels != null) {
                    AztecWheel wheel = AztecWheel.fromJSON(wheels, scale);
                    activate(wheel);
                    //enemies.add(octopuss);
                    wheels = wheels.next;
                    System.out.println(wheel);
                }
            }
        }

//        System.out.println("here i am enemy list");
//        System.out.println(enemies);
//        System.out.println(enemies.size());

    }

    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        for(Planet p : planets.getPlanets()){
            p.deactivatePhysics(world);
        }
        objects.clear();
        if (world != null) {
            world.dispose();
            world = new World(new Vector2(0,0), false);
        }
        talkingboss = null;
        objects.clear();
        planets.clear();
        stars.clear();
        anchors.clear();
        enemies.clear();
        portalpairs.clear();
        tutpoints.clear();
        //MenuMode.getMusic().dispose();
        vectorWorld = new VectorWorld();
        hasBoss = false;
    }

    /**
     * Add the specified obstacle to this LevelModel
     * @param obj The obstacle to be added to this LevelModel
     */
    public void add(Obstacle obj) {
        switch (obj.getType()) {
            case PLANET: planets.addPlanet((Planet) obj, vectorWorld); break;
            case ANCHOR: activate(obj); break;
            case STAR: activate(obj); break;
            case PLAYER: addPlayer((AstronautModel) obj); break;
            case ROPE: objects.add(0, obj); obj.activatePhysics(world); rope = (Rope) obj; break;
            case WORM: activate(obj); enemies.add((Worm) obj); break;
            case COLORED_BUG:
            case BUG: activate(obj); enemies.add((Bug) obj); break;
            case PORTAL: activate(obj); break;
            case URCHIN: activate(obj); enemies.add((Urchin) obj); break;
            case OCTO_LEG: activate(obj); break;
            case AZTEC_WHEEL: activate(obj); break;
            case TUTORIAL: activate(obj); break;
            case ICE_CREAM:
                ((IceCream) obj).setUpBound(bounds.getHeight() * yPlay);
                activate(obj); enemies.add((IceCream) obj); break;
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
            case PLANET:
                Bug bugger = ((Planet) obj).getBug();
                if ( bugger != null) {
                    remove(bugger);
                }
                obj.deactivatePhysics(world); planets.remove((Planet) obj); break;
            case ANCHOR: deactivate(obj); break;
            case STAR: deactivate(obj); break;
            case WORM: deactivate(obj); enemies.remove((Worm) obj); break;
            case COLORED_BUG:
            case BUG: deactivate(obj); enemies.remove((Bug) obj); break;
            case PORTAL: deactivate(obj); break;
            case URCHIN: deactivate(obj); enemies.remove((Urchin) obj); break;
            case OCTO_LEG: deactivate(obj); break;
            case AZTEC_WHEEL: deactivate(obj);
            case TUTORIAL: deactivate(obj); break;
            case ICE_CREAM: deactivate(obj); enemies.remove((IceCream) obj); break;
            case TALKING_BOSS: deactivate(obj); talkingboss = null; break;
            case SPEECH_BUBBLE: deactivate(obj); speechBubble = null; break;
        }
    }

    /**
     * Handles case of adding a player
     * @param player Player to be added
     */
    private void addPlayer(AstronautModel player) {
        if (player.getName().contains("2")) {
            player2 = player;
        } else {
            player1 = player;
        }
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
        out.addAll(planets.getPlanets());
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

        JsonValue playSize = new JsonValue(JsonValue.ValueType.array);
        playSize.addChild(new JsonValue(xPlay));
        playSize.addChild(new JsonValue(yPlay));

        out.addChild("physicsSize", physicsSize);
        out.addChild("graphicSize", graphicsSize);
        out.addChild("playSize", playSize);
        out.addChild("win", new JsonValue(winPercent + ""));

        //Add Galaxy

        out.addChild("galaxy", new JsonValue(galaxy.fullName()));

        //Add background
        out.addChild("background", new JsonValue(JsonAssetManager.getInstance().getKey(background)));

        //Add astronauts
        out.addChild("astronaut 1", player1.toJson());
        out.addChild("astronaut 2", player2.toJson());

        //Add rope
        out.addChild("rope", rope.toJson());

        //Add planet presets
        out.addChild("planet specs", Planet.presetJson());

        //Add planets
        out.addChild("planets", planets.toJson());

        //Add obstacles
        JsonValue anchors = new JsonValue(JsonValue.ValueType.array);
        JsonValue stars = new JsonValue(JsonValue.ValueType.array);
        JsonValue worms = new JsonValue(JsonValue.ValueType.array);
        JsonValue portalPairs = new JsonValue(JsonValue.ValueType.array);
        JsonValue tutorialPoints = new JsonValue(JsonValue.ValueType.array);
        JsonValue urchins = new JsonValue(JsonValue.ValueType.array);
        JsonValue iceCreams = new JsonValue(JsonValue.ValueType.array);
        JsonValue octolegs = new JsonValue(JsonValue.ValueType.array);
        JsonValue wheels = new JsonValue(JsonValue.ValueType.array);

        for (Obstacle obj : objects) {
            switch (obj.getType()) {
                case STAR: stars.addChild(((Star) obj).toJson()); break;
                case ANCHOR: anchors.addChild(((Anchor) obj).toJson()); break;
                case WORM: worms.addChild(((Worm) obj).toJson()); break;
                case URCHIN: urchins.addChild(((Urchin) obj).toJson()); break;
                case ICE_CREAM: iceCreams.addChild(((IceCream) obj).toJson()); break;
                case OCTO_LEG: hasBoss = true; octolegs.addChild(((OctoLeg) obj).toJson()); break;
                case AZTEC_WHEEL: hasBoss = true; wheels.addChild(((AztecWheel) obj).toJson()); break;
            }
        }

        for (PortalPair port : portalpairs) {
            portalPairs.addChild(port.toJson());
        }

        for (TutorialPoint tutorial : tutpoints) {
            tutorialPoints.addChild(tutorial.toJson());
        }

        out.addChild("has boss", new JsonValue(hasBoss));

        out.addChild("anchors", anchors);
        out.addChild("stars", stars);

        //Add enemies
        out.addChild("worms", worms);

        //Add portals
        out.addChild("portalpairs", portalPairs);

        //Add tutorial points
        out.addChild("tutorialpoints", tutorialPoints);

        //Add urchins
        out.addChild("urchin texture", new JsonValue(Urchin.getTexturePrefix()));
        out.addChild("urchins", urchins);

        //add ice cream
        out.addChild("ice cream", iceCreams);

        if (hasBoss) {
            if (galaxy == Galaxy.WHIRLPOOL) out.addChild("octopus legs", octolegs);
            else if (galaxy == Galaxy.SOMBRERO) out.addChild("aztec wheels", wheels);
        }


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

        //System.out.println(talkingboss);
        float x = (float) Math.floor((canvas.getCamera().position.x - canvas.getWidth()/2)/canvas.getWidth()) * canvas.getWidth();
        float y = (float) Math.floor((canvas.getCamera().position.y - canvas.getHeight()/2)/canvas.getHeight()) * canvas.getHeight();

        canvas.draw(background, Color.WHITE, x, y,canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), y,canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x, y + canvas.getHeight(),canvas.getWidth(),canvas.getHeight());
        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), y + canvas.getHeight(),canvas.getWidth(),canvas.getHeight());

        for(Planet p : planets.getPlanets()){
            p.draw(canvas);
        }
        for(Obstacle obj : objects) {
            if (obj.getType() != ObstacleType.PLAYER && obj.getType() != ObstacleType.TUTORIAL)
                obj.draw(canvas);
        }
        rope.draw(canvas);
        if (player1.isActive()) { player2.draw(canvas); player1.draw(canvas); }
        else { player1.draw(canvas); player2.draw(canvas); }
        for (Enemy e: enemies) {
            e.draw(canvas);
        }
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            talkingboss.drawDebug(canvas);
            for(Planet p : planets.getPlanets()){
                p.drawDebug(canvas);
            }
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            for (Enemy e: enemies) {
             e.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }

    /**
     * Draw method for level editor that doesn't tile background
     *
     * @param canvas
     * @param c
     */
    public void draw(GameCanvas canvas, char c) {
//        canvas.clear();

        canvas.begin();

        float x = (float) Math.floor((canvas.getCamera().position.x - canvas.getWidth()/2)/canvas.getWidth()) * canvas.getWidth();
        float y = (float) Math.floor((canvas.getCamera().position.y - canvas.getHeight()/2)/canvas.getHeight()) * canvas.getHeight();

//        canvas.draw(background, Color.WHITE, x, y,canvas.getWidth(),canvas.getHeight());
//        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), y,canvas.getWidth(),canvas.getHeight());
//        canvas.draw(background, Color.WHITE, x, y + canvas.getHeight(),canvas.getWidth(),canvas.getHeight());
//        canvas.draw(background, Color.WHITE, x + canvas.getWidth(), y + canvas.getHeight(),canvas.getWidth(),canvas.getHeight());


        for(Planet p : planets.getPlanets()){
            p.draw(canvas);
        }
        for(Obstacle obj : objects) {
            if (obj.getType() != ObstacleType.PLAYER && obj.getType() != ObstacleType.TUTORIAL)
                obj.draw(canvas);
        }
        if (player1.isActive()) { player2.draw(canvas); player1.draw(canvas); }
        else { player1.draw(canvas); player2.draw(canvas); }
        for (Enemy e: enemies) {
            e.draw(canvas);
        }
        System.out.println("in here skgfh");
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for(Planet p : planets.getPlanets()){
                p.drawDebug(canvas);
            }
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            for (Enemy e: enemies) {
                e.drawDebug(canvas);
            }
            canvas.endDebug();
        }
    }

}
