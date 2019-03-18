package edu.cornell.gdiac.starstruck.Obstacles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import edu.cornell.gdiac.starstruck.Galaxy;
import edu.cornell.gdiac.starstruck.Gravity.VectorWorld;

import java.util.ArrayList;

/**
 * An instance represents a list of the planets in one level.
 * Implements iterable.
 * Stores texture files, so that different galaxy themes can be selected.
 */
public class PlanetList {

    /** Track all loaded assets (for unloading purposes) */
    private Array<String> assets;

    /** File for gravity ring */
    private static String GRING_FILE = "planets/gravity ring.png";

    /** Files for default galaxy planets*/
    private static String GALAXY1_PLANET1_FILE = "planets/planet1.png";
    private static String GALAXY1_PLANET2_FILE = "planets/planet2.png";
    private static String GALAXY1_PLANET3_FILE = "planets/planet3.png";
    private static String GALAXY1_PLANET4_FILE = "planets/planet4.png";

    private static String WHIRL_P1 = "planets/whirlpool planet1.png";
    private static String WHIRL_P2 = "planets/whirlpool planet2.png";
    private static String WHIRL_P3 = "planets/whirlpool planet3.png";
    private static String WHIRL_P4 = "planets/whirlpool planet4.png";
    private static String WHIRL_P5 = "planets/whirlpool planet5.png";
    private static String WHIRL_P6 = "planets/whirlpool planet6.png";


    /** Files for planets to be used when drawing. MUST BE SET IN CONSTRUCTOR BEFORE ANY TEXTURES ARE CREATED*/
    private String PLANET1_FILE;
    private String PLANET2_FILE;
    private String PLANET3_FILE;
    private String PLANET4_FILE;
    private String PLANET5_FILE;
    private String PLANET6_FILE;

    /** Textures to be used when drawing planets*/
    private TextureRegion planet1_texture;
    private TextureRegion planet2_texture;
    private TextureRegion planet3_texture;
    private TextureRegion planet4_texture;
    private TextureRegion planet5_texture;
    private TextureRegion planet6_texture;

    /** Texture for gravity ring */
    private TextureRegion gring_texture;

    /** The planets in this PlanetList*/
    private ArrayList<Planet> planets;

    /** Scale to convert physics to pixels */
    private Vector2 scale;

    /**
     *  Constructs a new PlanetList by loading the appropriate sprites.
     * @param manager The asset manager idk
     * @param galaxy The galaxy for the planets in this level.
     * @param scale The drawing scale
     */
    public PlanetList(AssetManager manager, Galaxy galaxy, Vector2 scale) {
        setPlanetFiles(galaxy);

        assets = new Array<String>();

        manager.load(PLANET1_FILE, Texture.class);
        assets.add(PLANET1_FILE);
        manager.load(PLANET2_FILE, Texture.class);
        assets.add(PLANET2_FILE);
        manager.load(PLANET3_FILE, Texture.class);
        assets.add(PLANET3_FILE);
        manager.load(PLANET4_FILE, Texture.class);
        assets.add(PLANET4_FILE);
        manager.load(PLANET5_FILE, Texture.class);
        assets.add(PLANET5_FILE);
        manager.load(PLANET6_FILE, Texture.class);
        assets.add(PLANET6_FILE);
        manager.load(GRING_FILE, Texture.class);
        assets.add(GRING_FILE);

        planet1_texture = createTexture(manager, PLANET1_FILE, true);
        planet2_texture = createTexture(manager, PLANET2_FILE, true);
        planet3_texture = createTexture(manager, PLANET3_FILE, true);
        planet4_texture = createTexture(manager, PLANET4_FILE, true);
        planet5_texture = createTexture(manager, PLANET5_FILE, true);
        planet6_texture = createTexture(manager, PLANET6_FILE, true);
        gring_texture = createTexture(manager, GRING_FILE, true);

        planets = new ArrayList<Planet>();

        this.scale = scale;
    }

    /**
     * Returns a newly loaded texture region for the given file.
     *
     * This helper methods is used to set texture settings (such as scaling, and
     * whether or not the texture should repeat) after loading.
     *
     * @param manager 	Reference to global asset manager.
     * @param file		The texture (region) file
     * @param repeat	Whether the texture should be repeated
     *
     * @return a newly loaded texture region for the given file.
     */
    private TextureRegion createTexture(AssetManager manager, String file, boolean repeat) {
        if (manager.isLoaded(file)) {
            TextureRegion region = new TextureRegion(manager.get(file, Texture.class));
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            if (repeat) {
                region.getTexture().setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
            }
            return region;
        }
        return null;
    }

    /**
     * Sets the planet sprites based on the current galaxy.
     * @param galaxy The galaxy who's planet sprites should be used.
     */
    private void setPlanetFiles(Galaxy galaxy) {
        switch(galaxy){
            case WHIRLPOOL: setWhirlpoolSprites(); break;
            case DEFAULT :
            default : setDefaultSprites();

        }
    }

    /**
     * Sets the planet files to the default sprite options.
     */
    private void setDefaultSprites(){
        PLANET1_FILE = GALAXY1_PLANET1_FILE;
        PLANET2_FILE = GALAXY1_PLANET2_FILE;
        PLANET3_FILE = GALAXY1_PLANET3_FILE;
        PLANET4_FILE = GALAXY1_PLANET4_FILE;
        PLANET5_FILE = GALAXY1_PLANET1_FILE;
        PLANET6_FILE = GALAXY1_PLANET2_FILE;

    }

    /**
     * Sets the planet files to the whirlpool sprite options.
     */
    private void setWhirlpoolSprites(){
        PLANET1_FILE = WHIRL_P1;
        PLANET2_FILE = WHIRL_P2;
        PLANET3_FILE = WHIRL_P3;
        PLANET4_FILE = WHIRL_P4;
        PLANET5_FILE = WHIRL_P5;
        PLANET6_FILE = WHIRL_P6;

    }

    /**
     * Returns the texture for the 0-indexed i'th planet sprite in the current galaxy.
     * @param i The planet sprite to return in the current galaxy (indexed by 0)
     * @return The 0-indexed i'th planet sprite in the current galaxy.
     */
    private TextureRegion getPlanetTexture(int i) {
        switch(i) {
            case 0:
                return planet1_texture;
            case 1:
                return planet2_texture;
            case 2:
                return planet3_texture;
            case 3:
                return planet4_texture;
            case 4:
                return planet5_texture;
            case 5:
                return planet6_texture;
            default:
                return planet1_texture;
        }
    }

    /**
     * Return a reference to the array of planets.
     * @return a reference to the array of planets.
     */
    public ArrayList<Planet> getPlanets(){
        return planets;
    }

    /**
     * Add the specified planet to the planet list
     * @param p The planet to be added
     */
    public void addPlanet(Planet p) {
        planets.add(p);
    }

    /**
     * Add the specified planet to the planet list
     * @param x X coordinate of planet's origin
     * @param y Y coordinate of planet's origin
     * @param radius Planet's radius
     * @param mass Planet's mass
     * @param grange The range from which gravity is effective for this planet
     * @param sprite Sprite to be used when drawing this planet
     * @param world World that owns this planet
     * @param vectorWorld VectorWorld that controls this planet's gravity
     */
    public void addPlanet(float x, float y, float radius, float mass, float grange,
                          int sprite, World world, VectorWorld vectorWorld) {
        TextureRegion texture = getPlanetTexture(sprite);
        Planet p = new Planet(x, y, radius, mass, grange, texture, world, scale, gring_texture);
        vectorWorld.addPlanet(p);
        planets.add(p);
    }

    /**
     * Add the specified planets to the list.
     * @param planetSpecs A 2D array of specifications for the planets to be constructed. Each row specifies
     *                    a planet, and must contain in order: x coord, y coord, radius, mass, draw scale,
     *                    gravitation range, planet sprite to use (1-4).
     *                    Will probably need to edit eventually.
     * @param vectorWorld The vectorWorld controlling the gravity for this level
     * @param world The world idk why we need this but sure
     */
    public void addPlanets(float[][] planetSpecs, World world, VectorWorld vectorWorld) {
        for (int i = 0; i < planetSpecs.length; i++) {

            addPlanet(planetSpecs[i][0], planetSpecs[i][1], planetSpecs[i][2], planetSpecs[i][3],
                    planetSpecs[i][4], (int) planetSpecs[i][5], world, vectorWorld);

            //Vector2 pos = new Vector2(obj.getBody().getPosition().x, obj.getBody().getPosition().y - obj.getRadius());
            //vectorWorld.addPlanet(obj, PLANETS[i][3], obj.getCenter()); //Radius parameter is temporary fix for why center is off
        }
    }

    /**
     * Remove all planets
     */
    public void clear() {
        planets = new ArrayList<Planet>();
    }


}