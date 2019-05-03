/*
 * InputController.java
 *
 * This class buffers in input from the devices and converts it into its
 * semantic meaning. If your game had an option that allows the player to
 * remap the control keys, you would store this information in this class.
 * That way, the main GameEngine does not have to keep track of the current
 * key mapping.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.starstruck;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.*;
import edu.cornell.gdiac.util.*;

/**
 * Class for reading player input. 
 *
 * This supports both a keyboard and X-Box controller. In previous solutions, we only 
 * detected the X-Box controller on start-up.  This class allows us to hot-swap in
 * a controller via the new XBox360Controller class.
 */
public class InputController {
    // Sensitivity for moving crosshair with gameplay
    private static final float GP_ACCELERATE = 1.0f;
    private static final float GP_MAX_SPEED  = 10.0f;
    private static final float GP_THRESHOLD  = 0.01f;

    /** The singleton instance of the input controller */
    private static InputController theController = null;

    /**
     * Return the singleton instance of the input controller
     *
     * @return the singleton instance of the input controller
     */
    public static InputController getInstance() {
        if (theController == null) {
            theController = new InputController();
        }
        return theController;
    }

    // Fields to manage buttons
    /** Whether the reset button was pressed. */
    private boolean resetPressed;
    private boolean resetPrevious;
    /** Whether the button to advanced worlds was pressed. */
    private boolean nextPressed;
    private boolean nextPrevious;
    /** Whether the button to step back worlds was pressed. */
    private boolean prevPressed;
    private boolean prevPrevious;
    /** Whether the primary action button was pressed. */
    private boolean primePressed;
    private boolean primePrevious;
    /** Whether the secondary action button was pressed. */
    private boolean secondPressed;
    private boolean secondPrevious;
    /** Whether the teritiary action button was pressed. */
    private boolean tertiaryPressed;
    private boolean tertiaryPrevious;
    /** Whether the debug toggle was pressed. */
    private boolean debugPressed;
    private boolean debugPrevious;
    /** Whether the exit button was pressed. */
    private boolean exitPressed;
    private boolean exitPrevious;
    /** Whether left and right keys are pressed */
    private boolean rightPressed;
    private boolean rightPrevious;
    private boolean leftPressed;
    private boolean leftPrevious;
    /** Whether down key was pressed */
    private boolean downPressed;
    private boolean downPrevious;
    /** Whether a and d keys are pressed */
    private boolean dPressed;
    private boolean dPrevious;
    private boolean aPressed;
    private boolean aPrevious;
    private boolean sPressed;
    private boolean sPrevious;
    private boolean oPressed;
    private boolean oPrevious;
    private boolean wPressed;
    private boolean wPrevious;
    private boolean gPressed;
    private boolean gPrevious;
    private boolean uPressed;
    private boolean uPrevious;
    private boolean switchPressed;
    private boolean switchPrevious;
    private boolean onePressed;
    private boolean onePrevious;
    private boolean twoPressed;
    private boolean twoPrevious;
    private boolean threePressed;
    private boolean threePrevious;
    private boolean fourPressed;
    private boolean fourPrevious;
    private boolean fivePressed;
    private boolean fivePrevious;
    /** Whether space was pressed */
    private boolean spacePressed;
    private boolean spacePrevious;
    /** Whether shift was pressed */
    private boolean shiftPressed;
    private boolean shiftPrevious;
    /** Whether backspace was pressed */
    private boolean backspacePressed;
    private boolean backspacePrevious;
    /** Anchoring controls */
    private boolean anchorPressed;
    private boolean anchorPrevious;
    private boolean anchor1Pressed;
    private boolean anchor1Previous;
    private boolean anchor2Pressed;
    private boolean anchor2Previous;
    private boolean lPressed;
    private boolean lPrevious;

    /** Mouse's current position*/
    private float x_pos;
    private float y_pos;

    /** How much did we move horizontally? */
    private float horizontal;
    /** How much did we move vertically? */
    private float vertical;
    /** How much did the second player move horizontally? **/
    private float horizontal2;
    /** How much did the second player move vertically?*/
    private float vertical2;
    /** HOw much to rotate */
    private float turn;
    /** The crosshair position (for raddoll) */
    private Vector2 crosshair;
    /** The crosshair cache (for using as a return value) */
    private Vector2 crosscache;
    /** For the gamepad crosshair control */
    private float momentum;

    /** An X-Box controller (if it is connected) */
    XboxController xbox;
    XboxController xbox2;

    /**
     * Returns the amount of sideways movement.
     *
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal() {
        return horizontal;
    }

    /**
     * Returns the amount of vertical movement.
     *
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical() {
        return vertical;
    }

    /**
     * Returns the amount of sideways movement for player 2/other player.
     *
     * -1 = left, 1 = right, 0 = still
     *
     * @return the amount of sideways movement.
     */
    public float getHorizontal2() {return horizontal2;}

    /**
     * Returns the amount of vertical movement for player 2.
     *
     * -1 = down, 1 = up, 0 = still
     *
     * @return the amount of vertical movement.
     */
    public float getVertical2() {return vertical2;}

    public float getTurn() {
        return turn;
    }

    /**
     * Returns the current position of the crosshairs on the screen.
     *
     * This value does not return the actual reference to the crosshairs position.
     * That way this method can be called multiple times without any fair that
     * the position has been corrupted.  However, it does return the same object
     * each time.  So if you modify the object, the object will be reset in a
     * subsequent call to this getter.
     *
     * @return the current position of the crosshairs on the screen.
     */
    public Vector2 getCrossHair() {
        return crosscache.set(crosshair);
    }

    /**
     * Returns true if the primary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the primary action button was pressed.
     */
    public boolean didPrimary() {
        return primePressed && !primePrevious;
    }

    /**
     * Returns true if the secondary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didSecondary() {
        return secondPressed && !secondPrevious;
    }

    /**
     * Returns true if the secondary action button was pressed.
     *
     * This is a one-press button. It only returns true at the moment it was
     * pressed, and returns false at any frame afterwards.
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didSpace() {
        return spacePressed && !spacePrevious;
    }

    public boolean didShift() { return shiftPressed && !shiftPrevious; }

    public boolean didBackspace() { return backspacePressed && !backspacePrevious; }

    public boolean didAnchor() { return anchorPressed && !anchorPrevious; }

    public boolean didAnchor1() { return anchor1Pressed && !anchor1Previous; }

    public boolean didAnchor2() { return anchor2Pressed && !anchor2Previous; }

    public boolean didL() { return lPressed && !lPrevious; }


    /**
     * Returns true if the tertiary action button was pressed.
     *
     * This is a sustained button. It will returns true as long as the player
     * holds it down. NOT ANYMORE MUAHHAHAHA
     *
     * @return true if the secondary action button was pressed.
     */
    public boolean didTertiary() {
        return tertiaryPressed && !tertiaryPrevious;
    }

    /**
     *  Returns true if the mouse is currently pressed and the mouse was previously pressed.
     *
     * @return True if the mouse is being dragged for at least two frames.
     */
    public boolean mouseDragged() {return tertiaryPressed && tertiaryPrevious; }

    /**
     * Returns true if the reset button was pressed.
     *
     * @return true if the reset button was pressed.
     */
    public boolean didReset() {
        return resetPressed && !resetPrevious;
    }

    /**
     * Returns true if the player wants to go to the next level.
     *
     * @return true if the player wants to go to the next level.
     */
    public boolean didAdvance() {
        return nextPressed && !nextPrevious && shiftPressed;
    }

    /**
     * Returns true if the player wants to go to the previous level.
     *
     * @return true if the player wants to go to the previous level.
     */
    public boolean didRetreat() {
        return prevPressed && !prevPrevious && shiftPressed;
    }

    /**
     * Returns true if the player pressed 'P'
     *
     * @return if the player pressed 'P'.
     */
    public boolean didP() { return prevPressed && !prevPrevious;}

    /**
     * Returns true if the player wants to go toggle the debug mode.
     *
     * @return true if the player wants to go toggle the debug mode.
     */
    public boolean didDebug() {
        return debugPressed && !debugPrevious;
    }

    /**
     * Returns true if the exit button was pressed.
     *
     * @return true if the exit button was pressed.
     */
    public boolean didExit() {
        return exitPressed && !exitPrevious;
    }

    public boolean didRight() {
        return rightPressed;
    }

    public boolean rightPrevious() { return rightPrevious; }

    public boolean didLeft() {
        return leftPressed;
    }

    public boolean leftPrevious() { return leftPrevious; }

    public boolean heldUp() { return primePressed; }

    public boolean heldDown() { return downPressed; }

    public boolean didDown() { return downPressed && !downPrevious; }

    public boolean heldA() { return aPressed; }

    public boolean aPrevious() { return aPrevious; }

    public boolean heldD() { return dPressed; }

    public boolean dPrevious() { return dPrevious; }

    public boolean heldS() { return sPressed; }

    public boolean sPrevious() { return sPrevious; }

    public boolean didA() { return aPressed && !aPrevious; }

    public boolean didS() {
        return sPressed && !sPrevious;
    }

    public boolean didO() {
        return oPressed && !oPrevious;
    }

    public boolean didD() {
        return dPressed && !dPrevious;
    }

    public boolean didW() {
        return wPressed && !wPrevious;
    }

    public boolean didG() { return gPressed && !gPrevious; }

    public boolean didU() {
        return uPressed && !uPrevious;
    }

    public boolean didSwitch() { return switchPressed && !switchPrevious; }

    public boolean didOne() { return onePressed && !onePrevious; }

    public boolean didTwo() { return twoPressed && !twoPrevious; }

    public boolean didThree() { return threePressed && !threePrevious; }

    public boolean didFour() { return fourPressed && !fourPrevious; }

    public boolean didFive() { return fivePressed && !fivePrevious; }

    public float xPos() {return Gdx.input.getX();}

    public float yPos() {return Gdx.input.getY();}

    /**
     * Is shift being held down?
     * @return Is shift being held down?
     */
    public boolean shiftHeld() {
        return shiftPressed; //&& shiftPrevious;
    }

    /**
     * Creates a new input controller
     *
     * The input controller attempts to connect to the X-Box controller at device 0,
     * if it exists.  Otherwise, it falls back to the keyboard control.
     */
    public InputController() {
        // If we have a game-pad for id, then use it.
        xbox = new XboxController(0);
        xbox2 = new XboxController(1);
        crosshair = new Vector2();
        crosscache = new Vector2();
    }

    /**
     * Reads the input for the player and converts the result into game logic.
     *
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    public void readInput(Rectangle bounds, Vector2 scale) {
        // Copy state from last animation frame
        // Helps us ignore buttons that are held down
        primePrevious  = primePressed;
        secondPrevious = secondPressed;
        resetPrevious  = resetPressed;
        debugPrevious  = debugPressed;
        exitPrevious = exitPressed;
        nextPrevious = nextPressed;
        prevPrevious = prevPressed;
        rightPrevious = rightPressed;
        downPrevious = downPressed;
        leftPrevious = leftPressed;
        aPrevious = aPressed;
        dPrevious = dPressed;
        sPrevious = sPressed;
        oPrevious = oPressed;
        wPrevious = wPressed;
        gPrevious = gPressed;
        uPrevious = uPressed;
        switchPrevious = switchPressed;
        onePrevious = onePressed;
        twoPrevious = twoPressed;
        threePrevious = threePressed;
        fourPrevious = fourPressed;
        fivePrevious = fivePressed;
        spacePrevious = spacePressed;
        shiftPrevious = shiftPressed;
        tertiaryPrevious = tertiaryPressed;
        backspacePrevious = backspacePressed;
        anchorPrevious = anchorPressed;
        anchor1Previous = anchor1Pressed;
        anchor2Previous = anchor2Pressed;
        lPrevious = lPressed;

        // Check to see if a GamePad is connected
        if (xbox.isConnected() && xbox2.isConnected()) { // Both controllers connected
            readGamepad(bounds, scale);
            readGamepad2(bounds, scale);
            readKeyboard(bounds, scale, true);
        }
        else if (xbox.isConnected()) { //One controller connected
            readGamepad(bounds, scale);
            readKeyboard(bounds, scale, true);
        }
        else { //No controllers connected
            readKeyboard(bounds, scale, false);
        }
    }

    /**
     * Reads input from an X-Box controller connected to this computer.
     *
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    private void readGamepad(Rectangle bounds, Vector2 scale) {
        resetPressed = xbox.getStart(); //restart
        exitPressed  = xbox.getBack(); //esc
        leftPressed = xbox.getLeftX() < -0.6; //left
        rightPressed = xbox.getLeftX() > 0.6; //right
        primePressed = xbox.getA(); //jump
//        anchorPressed = xbox.getX();
//        anchor1Pressed = xbox.getX();
        anchorPressed = xbox.getA();
        anchor1Pressed = xbox.getA();
        switchPressed = xbox.getX(); //switch
        downPressed = xbox.getB(); //reel
        debugPressed  = xbox.getR3(); //debug

        // Increase animation frame, but only if trying to move
        horizontal = xbox.getLeftX();
        vertical   = xbox.getLeftY();

        // Move the crosshairs with the right stick.
        //tertiaryPressed = xbox.getA();
//        crosscache.set(xbox.getLeftX(), xbox.getLeftY());
//        if (crosscache.len2() > GP_THRESHOLD) {
//            momentum += GP_ACCELERATE;
//            momentum = Math.min(momentum, GP_MAX_SPEED);
//            crosscache.scl(momentum);
//            crosscache.scl(1/scale.x,1/scale.y);
//            crosshair.add(crosscache);
//        } else {
//            momentum = 0;
//        }
//        clampPosition(bounds);
    }

    /**
     * Reads input from a second X-Box controller connected to this computer.
     *
     * The method provides both the input bounds and the drawing scale.  It needs
     * the drawing scale to convert screen coordinates to world coordinates.  The
     * bounds are for the crosshair.  They cannot go outside of this zone.
     *
     * @param bounds The input bounds for the crosshair.
     * @param scale  The drawing scale
     */
    private void readGamepad2(Rectangle bounds, Vector2 scale) {
        resetPressed = xbox2.getStart(); //restart
        exitPressed  = xbox2.getBack(); //esc
        aPressed = xbox2.getLeftX() < -0.6; //left
        dPressed = xbox2.getLeftX() > 0.6; //right
        wPressed = xbox2.getA(); //jump
//        anchor2Pressed = xbox2.getX();
        anchor2Pressed = xbox2.getA();
        sPressed = xbox2.getB(); //reel
        debugPressed  = xbox2.getR3(); //debug
        //no switch in 2 player mode

        // Increase animation frame, but only if trying to move
        horizontal2 = xbox2.getLeftX();
        vertical2   = xbox2.getLeftY();
    }

    /**
     * Reads input from the keyboard.
     *
     * This controller reads from the keyboard regardless of whether or not an X-Box
     * controller is connected.  However, if a controller is connected, this method
     * gives priority to the X-Box controller.
     *
     * @param secondary true if the keyboard should give priority to a gamepad
     */
    private void readKeyboard(Rectangle bounds, Vector2 scale, boolean secondary) {
        // Give priority to gamepad results
        debugPressed = (secondary && debugPressed) || (Gdx.input.isKeyPressed(Input.Keys.Y));
        primePressed = (secondary && primePressed) || (Gdx.input.isKeyPressed(Input.Keys.UP));
        secondPressed = (secondary && secondPressed) || (Gdx.input.isKeyPressed(Input.Keys.L));
        prevPressed = (secondary && prevPressed) || (Gdx.input.isKeyPressed(Input.Keys.P));
        nextPressed = (secondary && nextPressed) || (Gdx.input.isKeyPressed(Input.Keys.N));
        exitPressed  = (secondary && exitPressed) || (Gdx.input.isKeyPressed(Input.Keys.ESCAPE));
        spacePressed = (secondary && spacePressed) || (Gdx.input.isKeyPressed(Input.Keys.SPACE));
        shiftPressed = (secondary && shiftPressed) || (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) || (Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
        backspacePressed = (secondary && backspacePressed) || (Gdx.input.isKeyPressed(Input.Keys.BACKSPACE));
        rightPressed = (secondary && rightPressed) || Gdx.input.isKeyPressed (Input.Keys.RIGHT);
        leftPressed = (secondary && leftPressed) || Gdx.input.isKeyPressed (Input.Keys.LEFT);
        downPressed = (secondary && downPressed) || Gdx.input.isKeyPressed (Input.Keys.DOWN);
        aPressed = (secondary && aPressed) || Gdx.input.isKeyPressed (Input.Keys.A);
        sPressed = (secondary && sPressed) || Gdx.input.isKeyPressed (Input.Keys.S);
        dPressed = (secondary && dPressed) || Gdx.input.isKeyPressed (Input.Keys.D);
        oPressed = (secondary && oPressed) || Gdx.input.isKeyPressed (Input.Keys.O);
        wPressed = (secondary && wPressed) || Gdx.input.isKeyPressed (Input.Keys.W);
        gPressed = (secondary && gPressed ) || Gdx.input.isKeyPressed (Input.Keys.G);
        uPressed = (secondary && uPressed ) || Gdx.input.isKeyPressed (Input.Keys.U);
        switchPressed = (secondary && switchPressed ) || Gdx.input.isKeyPressed(Input.Keys.S);
        onePressed = Gdx.input.isKeyPressed(Input.Keys.NUM_1); //Used for settings
        twoPressed = Gdx.input.isKeyPressed(Input.Keys.NUM_2); //Used for settings
        threePressed = Gdx.input.isKeyPressed(Input.Keys.NUM_3); //Used for settings
        fourPressed = (secondary && fourPressed) || Gdx.input.isKeyPressed(Input.Keys.NUM_4);
        fivePressed = (secondary && fivePressed) || Gdx.input.isKeyPressed(Input.Keys.NUM_5);
        resetPressed = (secondary && resetPressed) || Gdx.input.isKeyPressed(Input.Keys.R);
        anchorPressed = (secondary && anchorPressed) || (Gdx.input.isKeyPressed(Input.Keys.SPACE));
        anchor1Pressed = (secondary && anchor1Pressed) || (Gdx.input.isKeyPressed(Input.Keys.SLASH));
        anchor2Pressed = (secondary && anchor2Pressed) || (Gdx.input.isKeyPressed(Input.Keys.E));
        lPressed = (secondary && lPressed) || (Gdx.input.isKeyPressed(Input.Keys.L));

        // Directional controls
        horizontal = (secondary ? horizontal : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            horizontal += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            horizontal -= 1.0f;
        }

        vertical = (secondary ? vertical : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            vertical += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            vertical -= 1.0f;
        }

        vertical2 = (secondary ? vertical2 : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            vertical2 += 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            vertical2 -= 1.0f;
        }

        horizontal2 = (secondary ? horizontal2 : 0.0f);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            horizontal2 -= 1.0f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            horizontal2 += 1.0f;
        }

        // Rotate/turn
        turn = (secondary ? turn : 0f);
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            turn = turn + 1f;//(float) (Math.PI/180);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            turn = turn - 1f;//(float) (Math.PI/180);
        }

        // Mouse results
        tertiaryPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        crosshair.set(Gdx.input.getX(), Gdx.input.getY());
        crosshair.scl(1/scale.x,-1/scale.y);
        crosshair.y += bounds.height;
        clampPosition(bounds);
    }

    /**
     * Clamp the cursor position so that it does not go outside the window
     *
     * While this is not usually a problem with mouse control, this is critical
     * for the gamepad controls.
     */
    private void clampPosition(Rectangle bounds) {
        crosshair.x = Math.max(bounds.x, Math.min(bounds.x+bounds.width, crosshair.x));
        crosshair.y = Math.max(bounds.y, Math.min(bounds.y+bounds.height, crosshair.y));
    }
}