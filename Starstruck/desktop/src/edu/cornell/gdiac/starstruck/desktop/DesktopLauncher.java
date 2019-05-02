package edu.cornell.gdiac.starstruck.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import edu.cornell.gdiac.starstruck.Starstruck;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = 1280; //(1024*1.2);
		config.height = 720; //(576*1.2);
		config.resizable = false;
		new LwjglApplication(new Starstruck(), config);
	}
}
