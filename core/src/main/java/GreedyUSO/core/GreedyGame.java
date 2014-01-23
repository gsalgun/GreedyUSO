package GreedyUSO.core;

import GreedyUSO.core.view.SplashScreen;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;

public class GreedyGame extends Game {

    private AssetManager assetManager;

    @Override
    public void create() {
        assetManager = new AssetManager();
        SplashScreen splashScreen = new SplashScreen( this, assetManager);
        setScreen(splashScreen);
    }

    @Override
    public void resize(int i, int i2) {
        super.resize(i,i2);
    }

    @Override
    public void render() {
        super.render();

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        assetManager.clear();
        assetManager.dispose();
    }
}