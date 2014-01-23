package GreedyUSO.core.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SplashScreen implements Screen{
    private static final float ANIMATION_DURATION = 1.5f;// seconds
    private static final float SPLASH_LOGO_MIN_SEEN_DURATION = 2000; //miliseconds
    private SpriteBatch spriteBatch;
    private Texture peakLogo;
    private float logoX;
    private float logoY;
    private AssetManager assetManager;
    private ShapeRenderer shapeRenderer;
    private Game game;

    public SplashScreen( Game game, AssetManager assetManager) {
        this.assetManager = assetManager;
        this.shapeRenderer = new ShapeRenderer();
        this.game = game;

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        spriteBatch.begin();
        spriteBatch.draw(peakLogo, logoX, logoY);
        spriteBatch.end();
        if ( this.assetManager.update()){
            StartScreen startScreen = new StartScreen( assetManager, game);
            startScreen.initialize();
            game.setScreen( startScreen);
        }
        System.out.println("SplashScreen");

    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();
        loadPeakLogo();
        for ( int i = 1; i <=5; i++){
            assetManager.load("BG_A"+String.valueOf(i)+".png", Texture.class);
            assetManager.load("BG_B"+String.valueOf(i)+".png", Texture.class);
            assetManager.load("BG_C"+String.valueOf(i)+".png", Texture.class);
            assetManager.load("BG_D"+String.valueOf(i)+".png", Texture.class);
            assetManager.load("BG_E"+String.valueOf(i)+".png", Texture.class);
            assetManager.load("BG_F"+String.valueOf(i)+".png", Texture.class);
            assetManager.load("BG_G"+String.valueOf(i)+".png", Texture.class);
        }
        assetManager.load("enemy.atlas", TextureAtlas.class);
        assetManager.load("evilEnemy.atlas", TextureAtlas.class);
        assetManager.load("smallEnemy.atlas", TextureAtlas.class);
        assetManager.load("head.atlas", TextureAtlas.class);
        assetManager.load("body.atlas", TextureAtlas.class);
        assetManager.load("upHead.atlas", TextureAtlas.class);
        assetManager.load("upBody.atlas", TextureAtlas.class);
        assetManager.load("startScreen.atlas", TextureAtlas.class);
        assetManager.load("texts.atlas", TextureAtlas.class);
    }

    private void loadPeakLogo() {
        peakLogo = new Texture(Gdx.files.internal("fikirton_logo.png"));
        logoX = (Gdx.graphics.getWidth() - peakLogo.getWidth()) * 0.5f;
        logoY = (Gdx.graphics.getHeight() - peakLogo.getHeight()) * 0.5f;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        peakLogo.dispose();
        shapeRenderer.dispose();
    }
}
