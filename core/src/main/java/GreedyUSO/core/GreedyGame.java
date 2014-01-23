package GreedyUSO.core;

import GreedyUSO.core.view.SplashScreen;
import com.badlogic.gdx.Game;

public class GreedyGame extends Game {

    @Override
    public void create() {
        SplashScreen splashScreen = new SplashScreen( this);
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

    }
}