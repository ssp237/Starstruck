package edu.cornell.gdiac.starstruck.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import edu.cornell.gdiac.starstruck.GDXRoot;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width  = 1024;
		config.height = 576;
		config.resizable = false;
		new LwjglApplication(new GDXRoot(), config);
	}
}
