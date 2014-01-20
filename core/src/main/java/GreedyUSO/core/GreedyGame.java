package GreedyUSO.core;

import GreedyUSO.core.view.GameScreen;
import com.badlogic.gdx.Game;

public class GreedyGame extends Game {

    @Override
    public void create() {
        GameScreen gameScreen = new GameScreen();
        gameScreen.initialize();
        setScreen(gameScreen);
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