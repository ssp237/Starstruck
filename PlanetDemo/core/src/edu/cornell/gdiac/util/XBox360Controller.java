/* 
 * XBox360Controller.java
 *
 * Input handler for XBox 360 controller
 * 
 * While LibGDX has game controller support, it only support Ouya (really?)
 * out of the box.  For everything else, you must know exactly what numbers
 * map to what buttons.  Fortunately, the internet is good at finding these
 * things out for you.
 *
 * This class also shows another one of the hazards of cross-platform support.
 * While XBox controller support is (largely) built into Windows, it requires
 * a third party driver for Mac OS X.  And that driver has different button
 * mappings than the Windows driver.  So this class has to determine which OS
 * it is running on in order to work properly.
 *
 * Mac OS X driver support is provided by Colin Munro:
 * https://github.com/360Controller/360Controller/releases
 *
 * We have moved this class to a util package so that you do not waste time looking
 * at the code.
 *
 * Author: Walker M. White
 * Based on original GameX Ship Demo by Rama C. Hoetzlein, 2002
 * LibGDX version, 1/16/2015
 */
package edu.cornell.gdiac.util;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.controllers.*;

/**
 * Class to support an XBox 360 controller
 * 
 * This is a wrapper class, which wraps around a Controller object to provide
 * concrete mappings for the buttons, joysticks and triggers.  It is simpler than
 * having to remember the exact device numbers (even as constants) for each button.
 * It is particularly important because different operating systems have different
 * mappings for the buttons.
 *
 * Each controller must have its own instance.  The constructor automatically 
 * determines what OS this controller is running on before assigning the mappings.
 * The constructor DOES NOT verify that the controller is indeed an XBox 360 
 * controller.
 */
public class XBox360Controller implements ControllerListener {
	/** The controller id number */
	private int deviceid;
	/** Reference to base controller object wrapped by this instance. */
	private Controller controller;
	/** Whether this controller is currently running with the Mac OS X driver */
	private boolean macosx;
	
	/** Button identifier for the X-Button */
	private int button_x;
	/** Button identifier for the Y-Button */
	private int button_y;
	/** Button identifier for the A-Button */
	private int button_a;
	/** Button identifier for the B-Button */
	private int button_b;
			
	/** Button identifier for the Back Button */
	private int button_back;
	/** Button identifier for the Start Button */
	private int button_start;
	/** Button identifier for the Guide Button */
	private int button_guide;
	
	/** Button identifier for the left bumper */
	private int button_lb;
	/** Button identifier for the left analog stick */
	private int button_l3;
	/** Button identifier for the right bumper */
	private int button_rb;
	/** Button identifier for the right analog stick */
	private int button_r3;

	/** Button identifier for DPad Up button (OS X driver only) */
	private int button_dpad_up;
	/** Button identifier for DPad Down button (OS X driver only) */
	private int button_dpad_down;
	/** Button identifier for DPad Right button (OS X driver only) */
	private int button_dpad_right;
	/** Button identifier for DPad Left button (OS X driver only) */
	private int button_dpad_left;

	/** POV identifier for the DPad (Windows driver only) */
	private int pov_index_dpad;
	/** POV for DPad Up button */
	private PovDirection pov_dpad_up;
	/** POV for DPad Down button */
	private PovDirection pov_dpad_down;
	/** POV for DPad Right button */
	private PovDirection pov_dpad_right;
	/** POV for DPad Left button */
	private PovDirection pov_dpad_left;
	
	/** Axis identifier for left analog stick x-axis */
	private int axis_left_x;
	/** Axis identifier for left analog stick y-axis */
	private int axis_left_y;
	/** Axis identifier for left trigger */
	private int axis_left_trigger;
	/** Workaround for bug in recent Mac controller */
	private boolean left_trigger_begin;

	/** Axis identifier for right analog stick x-axis */
	private int axis_right_x;
	/** Axis identifier for right analog stick y-axis */
	private int axis_right_y;
	/** Axis identifier for right trigger */
	private int axis_right_trigger;
	/** Workaround for bug in recent Mac controller */
	private boolean right_trigger_begin;
	
	/**
	 * Creates a new (potential) XBox 360 input controller.
	 *
	 * This method will search the list of connected controllers.  If the controller
	 * at position device is an XBox controller, it will connect.  Otherwise, it 
	 * will wait until an X-Box controller is connected to that device.
	 *
	 * @param device The device id to treat as an X-Box controller
	 */
	public XBox360Controller(int device) {
		deviceid = device;
		if (Controllers.getControllers().size > deviceid) {
			initialize(Controllers.getControllers().get(deviceid));
		}
		Controllers.addListener(this);
	}
	
	/**
	 * Initializes this input controller as a wrapper around the given controller
	 *
	 * @param controller The base controller to wrap
	 */
	protected void initialize(Controller controller) {
		if (!controller.getName().toLowerCase().contains("xbox") ||
			!controller.getName().contains("360")) {
			this.controller = null;
			return;
		}
	
		this.controller = controller;
		macosx = System.getProperty("os.name").equals("Mac OS X");
		
		if (!macosx) {
			// Windows button mapping
			button_x = 2;
			button_y = 3;
			button_a = 0;
			button_b = 1;
			
			button_back  = 6;
			button_start = 7;
			button_guide = 10;
			
			button_lb = 4;
			button_l3 = 8;
			button_rb = 5;
			button_r3 = 9;

			// Windows does not treat dpad as discrete buttons
			button_dpad_up    = -1;
			button_dpad_down  = -1;
			button_dpad_left  = -1;
			button_dpad_right = -1;
			
			pov_index_dpad = 0;
			pov_dpad_up    = PovDirection.north;
			pov_dpad_down  = PovDirection.south;
			pov_dpad_left  = PovDirection.east;
			pov_dpad_right = PovDirection.west;
			
			axis_left_x = 1;
			axis_left_y = 0;
			axis_left_trigger = 4;
			
			axis_right_x = 3;
			axis_right_y = 2;
			axis_right_trigger = 4;
		} else {
		
			// Mac Driver settings
			button_x = 14;
			button_y = 15;
			button_a = 12;
			button_b = 13;
			
			button_back  = 5;
			button_start = 4;
			button_guide = 10;

			button_lb = 8;
			button_l3 = 6;
			button_rb = 9;
			button_r3 = 7;

			// Mac does not treat dpad as a POV
			button_dpad_up    = 0;
			button_dpad_down  = 1;
			button_dpad_left  = 2;
			button_dpad_right = 3;
			
			pov_index_dpad = -1;
			pov_dpad_up    = null;
			pov_dpad_down  = null;
			pov_dpad_left  = null;
			pov_dpad_right = null;
			
			axis_left_x = 2;
			axis_left_y = 3;
			axis_left_trigger = 0;
			
			axis_right_x = 4;
			axis_right_y = 5;
			axis_right_trigger = 1;
		}
		
		// Workaround for trigger bug
		left_trigger_begin = true;
		right_trigger_begin = true;
	}
	
	/**
	 * Returns true if there is an X-Box 360 controller connected 
	 *
	 * @return true if there is an X-Box 360 controller connected 
 	 */
 	public boolean isConnected() {
 		return controller != null;
 	}
 	
	/**
	 * Returns true if the start button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the start button is currently pressed
	 */
	public boolean getStart() {
		return controller.getButton(button_start);
	}

	/**
	 * Returns true if the back button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the back button is currently pressed
	 */
	public boolean getBack() {
		return controller.getButton(button_back);
	}

	/**
	 * Returns true if the guide button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the guide button is currently pressed
	 */
	public boolean getGuide() {
		return controller.getButton(button_guide);
	}

	/**
	 * Returns true if the X button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the X button is currently pressed
	 */
	public boolean getX() {
		return controller.getButton(button_x);
	}

	/**
	 * Returns true if the Y button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the Y button is currently pressed
	 */
	public boolean getY() {
		return controller.getButton(button_y);
	}

	/**
	 * Returns true if the A button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the A button is currently pressed
	 */
	public boolean getA() {
		return controller.getButton(button_a);
	}

	/**
	 * Returns true if the B button is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the Y button is currently pressed
	 */
	public boolean getB() {
		return controller.getButton(button_b);
	}

	/**
	 * Returns true if the left bumper is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the left bumper is currently pressed
	 */
	public boolean getLB() {
		return controller.getButton(button_lb);
	}

	/**
	 * Returns true if the left analog stick is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the left analog stick is currently pressed
	 */
	public boolean getL3() {
		return controller.getButton(button_l3);
	}

	/**
	 * Returns true if the right bumper is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the right bumper is currently pressed
	 */
	public boolean getRB() {
		return controller.getButton(button_rb);
	}

	/**
	 * Returns true if the right analog stick is currently pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the right analog stick is currently pressed
	 */
	public boolean getR3() {
		return controller.getButton(button_r3);
	}
	
	/**
	 * Returns true if the DPad Up button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Up button is currently pressed
	 */
	public boolean getDPadUp() {
		if (pov_index_dpad == -1) {
			return controller.getButton(button_dpad_up) &&
				   !controller.getButton(button_dpad_left) &&
				   !controller.getButton(button_dpad_right);
		} else {
			return controller.getPov(pov_index_dpad) == pov_dpad_up;
		}
	}

	/**
	 * Returns true if the DPad Down button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Down button is currently pressed
	 */
	public boolean getDPadDown() {
		if (pov_index_dpad == -1) {
			return controller.getButton(button_dpad_down) &&
				   !controller.getButton(button_dpad_left) &&
				   !controller.getButton(button_dpad_right);
		} else {
			return controller.getPov(pov_index_dpad) == pov_dpad_down;
		}
	}

	/**
	 * Returns true if the DPad Left button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Left button is currently pressed
	 */
	public boolean getDPadLeft() {
		if (pov_index_dpad == -1) {
			return controller.getButton(button_dpad_left) &&
				   !controller.getButton(button_dpad_up) &&
				   !controller.getButton(button_dpad_down);
		} else {
			return controller.getPov(pov_index_dpad) == pov_dpad_left;
		}	
	}

	/**
	 * Returns true if the DPad Right button currently pressed.
	 *
	 * This is method only returns true if the up button is pressed
	 * by itself.  If the up button is combined with left or right, 
	 * it will return false.  For more flexible usage of the DPad,
	 * you should use the method getDPadDirection().
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return true if the DPad Right button is currently pressed
	 */
	public boolean getDPadRight() {
		if (pov_index_dpad == -1) {
			return controller.getButton(button_dpad_right) &&
				   !controller.getButton(button_dpad_up) &&
				   !controller.getButton(button_dpad_down);
		} else {
			return controller.getPov(pov_index_dpad) == pov_dpad_right;
		}	
	}

	/**
	 * Returns the current direction of the DPad
	 * 
	 * The result will be one of the eight cardinal directions, or
	 * center if the DPad is not actively pressed.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the current direction of the DPad
	 */	 
	public PovDirection getDPadDirection() {
		if (pov_index_dpad != -1) {
			if (controller.getButton(button_dpad_up)) {
				if (controller.getButton(button_dpad_left)) {
					return PovDirection.northWest;
				} else if (controller.getButton(button_dpad_right)) {
					return PovDirection.northEast;
				}
				return PovDirection.north;
			} else if (controller.getButton(button_dpad_down)) {
				if (controller.getButton(button_dpad_left)) {
					return PovDirection.southWest;					
				} else if (controller.getButton(button_dpad_right)) {
					return PovDirection.southEast;					
				}
				return PovDirection.south;
			} else if (controller.getButton(button_dpad_left)) {
				return PovDirection.west;
			} else if (controller.getButton(button_dpad_right)) {
				return PovDirection.east;				
			}
			return PovDirection.center;			
		}
		return controller.getPov(pov_index_dpad);
	}
	
	/**
	 * Returns the X axis value of the left analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is to the left.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the X axis value of the left analog stick.
	 */
	public float getLeftX() {
		return controller.getAxis(axis_left_x);
	}
	
	/**
	 * Returns the Y axis value of the left analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is towards the bottom.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the Y axis value of the left analog stick.
	 */
	public float getLeftY() {
		return controller.getAxis(axis_left_y);
	}
	
	/**
	 * Returns the value of the left trigger.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the value of the left trigger.
	 */
	public float getLeftTrigger() {
		float value = controller.getAxis(axis_left_trigger);
		// Workaround for bug in Mac driver
		if (left_trigger_begin) {
			if (value != 0) {
				left_trigger_begin = false;
				return value;
			}
			return -1;
		}
		return value;
	}

	/**
	 * Returns the X axis value of the right analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is to the left.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the X axis value of the right analog stick.
	 */
	public float getRightX() {
		return controller.getAxis(axis_right_x);
	}
	
	/**
	 * Returns the Y axis value of the right analog stick.
	 *
	 * This is a value between -1 and 1, where -1 is towards the bottom.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the Y axis value of the right analog stick.
	 */
	public float getRightY() {
		return controller.getAxis(axis_right_y);
	}
	
	/**
	 * Returns the value of the right trigger.
	 *
	 * This is a value between 0 and 1, where 0 is no pressure.
	 *
	 * This is a polling operation.  If the player pressed the button faster
	 * than an animation frame (unlikely), the input will be lost.
	 *
	 * @return the value of the right trigger.
	 */
	public float getRightTrigger() {
		float value = controller.getAxis(axis_right_trigger);
		// Workaround for bug in Mac driver
		if (right_trigger_begin) {
			if (value != 0) {
				right_trigger_begin = false;
				return value;
			}
			return -1;
		}
		return value;
	}
	
	// METHODS FOR CONTROLLER LISTENER

	/** 
	 * A Controller got connected.
	 *
	 * @param controller 
	 */
	public void connected (Controller controller) {
		if (this.controller == null && Controllers.getControllers().size > deviceid) {
			initialize(Controllers.getControllers().get(deviceid));
		}
	}

	/** 
	 * A Controller got disconnected.
	 *
	 * @param controller 
	 */
	public void disconnected (Controller controller) {
		if (this.controller == controller) {
			this.controller = null;
		}
	}

	/** 
	 * A button on the Controller was pressed. 
	 * 
	 * The buttonCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts button constants for known controllers.
	 *
	 * @param controller
	 * @param buttonCode
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonDown (Controller controller, int buttonCode) { return true; }

	/** 
	 * A button on the Controller was released. 
	 *
	 * The buttonCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts button constants for known controllers.
	 *
	 * @param controller
	 * @param buttonCode
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean buttonUp (Controller controller, int buttonCode) { return true; }

	/** 
	 * An axis on the Controller moved. 
	 *
	 * The axisCode is controller specific. The axis value is in the range [-1, 1]. The
	 * <code>com.badlogic.gdx.controllers.mapping</code> package hosts axes constants for 
	 * known controllers.
	 *
	 * @param controller
	 * @param axisCode
	 * @param value the axis value, -1 to 1
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean axisMoved (Controller controller, int axisCode, float value) { return true; }

	/** 
	 * A POV on the Controller moved. 
	 *
	 * The povCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts POV constants for known controllers.
	 *
	 * @param controller
	 * @param povCode
	 * @param value
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean povMoved (Controller controller, int povCode, PovDirection value) { return true; }

	/** 
	 * An x-slider on the Controller moved. 
	 *
	 * The sliderCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts slider constants for known controllers.
	 *
	 * @param controller
	 * @param sliderCode
	 * @param value
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean xSliderMoved (Controller controller, int sliderCode, boolean value) { return true; }

	/** 
	 * An y-slider on the Controller moved. 
	 *
	 * The sliderCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts slider constants for known controllers.
	 *
	 * @param controller
	 * @param sliderCode
	 * @param value
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean ySliderMoved (Controller controller, int sliderCode, boolean value) { return true; }

	/** 
	 * An accelerometer value on the Controller changed. 
	 *
	 * The accelerometerCode is controller specific. The <code>com.badlogic.gdx.controllers.mapping</code> 
	 * package hosts slider constants for known controllers. The value is a Vector3
	 * representing the acceleration on a 3-axis accelerometer in m/s^2.
	 *
	 * @param controller
	 * @param accelerometerCode
	 * @param value
	 * @return whether to hand the event to other listeners. 
	 */
	public boolean accelerometerMoved (Controller controller, int accelerometerCode, Vector3 value) { return true; }
}