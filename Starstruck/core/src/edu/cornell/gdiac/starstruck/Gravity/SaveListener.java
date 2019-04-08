package edu.cornell.gdiac.starstruck.Gravity;

import com.badlogic.gdx.Input;

public class SaveListener implements Input.TextInputListener {

    public String file;

    public SaveListener(){
        file = null;
    }

    public void input (String text) {
        file = text;
    }


    public void canceled () {
        file = null;
    }
}
