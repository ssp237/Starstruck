package edu.cornell.gdiac.starstruck.Models;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import edu.cornell.gdiac.starstruck.GameCanvas;
import edu.cornell.gdiac.starstruck.Obstacles.Anchor;
import edu.cornell.gdiac.starstruck.Obstacles.ObstacleType;
import edu.cornell.gdiac.starstruck.Obstacles.WheelObstacle;
import edu.cornell.gdiac.util.JsonAssetManager;

import java.util.ArrayList;

public class AztecWheel extends WheelObstacle {

    private static int counter = 0;

    /** Radius for anchor positions */
    private float radius2;
    /** List of our anchors */
    private ArrayList<Anchor> anchors;

    public AztecWheel(float x, float y, Vector2 scale) {
        super(x,y,0);

        texture = JsonAssetManager.getInstance().getEntry("so boss", TextureRegion.class);
        setTexture(texture);
        setDrawScale(scale);


        float radius = texture.getRegionWidth() / (scale.x * 2);
        setRadius(radius);

        setDrawScale(scale);


        //Set drawing scale
        float newScale = (2 * radius * drawScale.x)/texture.getRegionWidth();
        this.scaleDraw = newScale;


        setName("aztecWheel" + counter);
        counter++;
        radius2 = radius * 0.9f;

        anchors = new ArrayList<Anchor>();

        for (Vector2 v : getAnchorPositions()) {
            anchors.add(new Anchor(v.x, v.y, JsonAssetManager.getInstance().getEntry("anchor", TextureRegion.class), scale));
        }
    }

    private ArrayList<Vector2> getAnchorPositions() {

        ArrayList<Vector2> out = new ArrayList<Vector2>();

        Vector2 pos = getPosition();
        float twoRootTwo = (float) (Math.sqrt(2)/2);

        out.add(new Vector2(pos.x, pos.y + radius2));
        out.add(new Vector2(pos.x + twoRootTwo * radius2, pos.y + twoRootTwo * radius2));
        out.add(new Vector2(pos.x + radius2, pos.y));
        out.add(new Vector2(pos.x + twoRootTwo * radius2, pos.y - twoRootTwo * radius2));
        out.add(new Vector2(pos.x, pos.y - radius2));
        out.add(new Vector2(pos.x - twoRootTwo * radius2, pos.y - twoRootTwo * radius2));
        out.add(new Vector2(pos.x - radius2, pos.y));
        out.add(new Vector2(pos.x - twoRootTwo * radius2, pos.y + twoRootTwo * radius2));


        return out;
    }

    public void setPosition(Vector2 position) {
        super.setPosition(position);

        ArrayList<Vector2> anchorPositions = getAnchorPositions();
        for (int i = 0; i < 8; i ++) {
            anchors.get(i).setPosition(anchorPositions.get(i));
        }
    }

    public void setPosition(float x, float y) {
        setPosition(new Vector2(x, y));
    }

    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) return false;
        for (Anchor a : anchors) {
            if (!a.activatePhysics(world)) return false ;
        }
        return true;
    }

    public void deactivatePhysics(World world) {
        super.deactivatePhysics(world);
        for (Anchor a : anchors) {
            a.deactivatePhysics(world);
        }
    }


    public ObstacleType getType() {return ObstacleType.AZTEC_WHEEL;}

    public void draw(GameCanvas canvas) {
        super.draw(canvas);
        for (Anchor a : anchors) {
            a.draw(canvas);
        }
    }

    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        for (Anchor a : anchors) {
            a.drawDebug(canvas);
        }
    }




}
