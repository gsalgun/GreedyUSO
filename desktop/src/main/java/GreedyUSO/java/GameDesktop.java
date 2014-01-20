package GreedyUSO.java;

import GreedyUSO.core.GreedyGame;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import com.badlogic.gdx.utils.GdxNativesLoader;

public class GameDesktop {
	public static void main (String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.useGL20 = true;
//        config.b
        GdxNativesLoader.load();
		new LwjglApplication(new GreedyGame(), config);
	}
}
