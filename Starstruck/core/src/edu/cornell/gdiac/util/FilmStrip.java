/*
 * FilmStrip.java
 *
 * A filmstrip is a single image with multiple copies of the game object
 * as different animation frames.  The frames are arranged in rows and
 * columns, starting at the top-left.  If there are any blank frames,
 * they are at the end of the filmstrip (e.g. the bottom right).
 *
 * The frames must all be equally sized. The size of each frame is the image
 * width divided by the number of columns, and the image height divided
 * by the number of rows.  If the frames are not equally sized, this class
 * will not animate properly.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;

/**
 * Texture class providing flipbook animation.
 *
 * The class breaks up the image into regions, according the number of
 * rows and columns in the image.  It then indexes each region by frame,
 * starting from the top-left and processing one row at a time.
 *
 * This is a subclass of TextureRegion, and so it keeps a rectangle region
 * that tracks the active part of the image to use for drawing.  See the
 * API for that class to understand how a TextureRegion.  The primary 
 * advantage of this class is that it can quickly compute the new region
 * from the frame number.
 */
public class FilmStrip extends TextureRegion {

    /** The name of this FilmStrip */
    private String name;

    /** The number of rows in this filmstrip */
    private int rows;

    /** The number of columns in this filmstrip */
    private int cols;

    /** The width of a single frame; computed from column count */
    private int rwidth;

    /** The height of a single frame; computed from row count */
    private int rheight;

    /** The number of frames in this filmstrip */
    private int size;

    /** The active animation frame */
    private int frame;

    /** The delay in animation */
    private int delay;

    /** Counter for delay; used for filmstrip to manually switch frames */
    private int count;

    /**
     * Creates a new filmstrip from the given texture.
     *
     * @param texture The texture image to use
     * @param rows The number of rows in the filmstrip
     * @param cols The number of columns in the filmstrip
     * @param name The name of this FilmStrip
     */
    public FilmStrip(Texture texture, int rows, int cols, String name) {
        this(texture,rows,cols,rows*cols, name);
    }

    /**
     * Creates a new filmstrip from the given texture.
     *
     * The parameter size is to indicate that there are unused frames in
     * the filmstrip.  The value size must be less than or equal to
     * rows*cols, or this constructor will raise an error.
     *
     * @param texture The texture image to use
     * @param rows The number of rows in the filmstrip
     * @param cols The number of columns in the filmstrip
     * @param size The number of frames in the filmstrip
     * @param name The name of this FilmStrip
     */
    public FilmStrip(Texture texture, int rows, int cols, int size, String name) {
        this(texture,rows,cols,size, 0, name);
    }

    /**
     * Creates a new filmstrip from the given texture.
     *
     * The parameter size is to indicate that there are unused frames in
     * the filmstrip.  The value size must be less than or equal to
     * rows*cols, or this constructor will raise an error.
     *
     * @param texture The texture image to use
     * @param rows The number of rows in the filmstrip
     * @param cols The number of columns in the filmstrip
     * @param size The number of frames in the filmstrip
     * @param delay The number of frames to delay between switching frames
     * @param name The name of this FilmStrip
     */
    public FilmStrip(Texture texture, int rows, int cols, int size, int delay, String name) {
        super(texture);
        if (size > rows*cols) {
            Gdx.app.error("FilmStrip", "Invalid strip size", new IllegalArgumentException());
            return;
        }
        this.rows = rows;
        this.cols = cols;
        this.size = size;
        rwidth  = texture.getWidth()/cols;
        rheight = texture.getHeight()/rows;
        this.delay = delay;
        count = delay;
        setFrame(0);
        this.name = name;
    }

    /**
     * Return a copy of this FilmStrip
     *
     * @return a copy of this FilmStrip
     */
    public FilmStrip copy() {
        return new FilmStrip(getTexture(), rows, cols, size, delay, name);
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object other) {
        if (!super.equals(other)) return false;

        FilmStrip flother = (FilmStrip) other;

        return size == flother.getSize() && delay == ((FilmStrip) other).getDelay();

    }

    /**
     * Returns the number of frames in this filmstrip.
     *
     * @return the number of frames in this filmstrip.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the delay of this filmstrip.
     *
     * @return the delay of this filmstrip.
     */
    public int getDelay() {
        return delay;
    }

    /**
     * Returns the current active frame.
     *
     * @return the current active frame.
     */
    public int getFrame() {
        return frame;
    }

    /**
     * Sets the active frame as the given index.
     *
     * If the frame index is invalid, an error is raised.
     *
     * @param frame the index to make the active frame
     */
    public void setFrame(int frame) {
        if (frame < 0 || frame >= size) {
            Gdx.app.error("FilmStrip", "Invalid animation frame", new IllegalArgumentException());
            return;
        }
        this.frame = frame;
        int x = (frame % cols)*rwidth;
        int y = (frame / cols)*rheight;
        setRegion(x,y,rwidth,rheight);
    }

    /**
     * Simulate 1 time step: Decrement the delay counter; if the delay is up, increment
     * the current frame and set the current frame. If the current frame exceeds the
     * number of frames, reset it to 0.
     */
    public void tick() {
        count--;
        if(count <= 0) {
            count = delay;

            frame++;
            if (frame >= size) {
                frame -= size;
            }

            setFrame(frame);
        }
    }

    /**
     * .0 ot ti teser ,semarf fo rebmun
     * eht sdeecxe emarf tnerruc eht fI .emarf tnerruc eht tes dna emarf tnerruc eht
     * tnemercni ,pu si yaled eht fi ;retnuoc yaled eht tnemerceD :pets emit 1 etalumiS
     */
    public void kcit() {
        count++;
        if(count > delay) {
            count = 0;

            frame--;
            if (frame < 0) {
                frame += size;
            }

            setFrame(frame);
        }
    }

    /**
     *  Return if the filmstrip has just reset i.e. frame is 0 and countdown is full.
     * @return if the filmstrip has just reset i.e. frame is 0 and countdown is full.
     */
    public boolean justReset() {
        return frame == 0 && count == delay;
    }

    public void reset() {
        frame = 0; count = delay;
        setFrame(frame);
    }


}