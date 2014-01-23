package GreedyUSO.core.view;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class StartScreen implements Screen {

    private AssetManager assetManager;
    private Game game;
    private Stage stage;
    private Batch stageBatch;
    private Camera stageCamera;

    public StartScreen( AssetManager assetManager, Game game){
        this.assetManager = assetManager;
        this.game = game;
    }

    public void initialize(){
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        stageBatch = new SpriteBatch();
        stageCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.setCamera(stageCamera);

        TextureAtlas atlas = assetManager.get("startScreen.atlas", TextureAtlas.class);
        Button.ButtonStyle style = new Button.ButtonStyle( new TextureRegionDrawable( atlas.findRegion("start-button")),
                                                           new TextureRegionDrawable( atlas.findRegion("start-button_over")),
                                                           new TextureRegionDrawable( atlas.findRegion("start-button_over")));
        Button startButton = new Button( style);
        startButton.setPosition( Gdx.graphics.getWidth() * 0.6f - startButton.getWidth()/2, Gdx.graphics.getHeight()/4 - startButton.getHeight()/2);
        startButton.addListener( new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameScreen gameScreen = new GameScreen( assetManager, game, false);
                gameScreen.initialize();
                game.setScreen( gameScreen);
            }

        });
        Image background = new Image( new TextureRegionDrawable( atlas.findRegion("start-screen_BG")));
        background.setPosition(0, 0);
        Image logo = new Image( new TextureRegionDrawable( atlas.findRegion("bite-logo")));
        logo.setPosition( Gdx.graphics.getWidth()/2 - logo.getWidth()/2, Gdx.graphics.getHeight()*0.6f - logo.getHeight()/2);
        stage.addActor( background);
        stage.addActor( logo);
        stage.addActor( startButton);
    }

    @Override
    public void render(float v) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act( v);
        stageBatch.setProjectionMatrix( stageCamera.combined);
        stageBatch.begin();
        stage.draw();
        stageBatch.end();
    }

    @Override
    public void resize(int i, int i2) {
        if ( stage != null){
            stage.setViewport( i, i2, false);
        }
    }

    @Override
    public void show() {

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
        stageBatch.dispose();
        stage.dispose();
    }
}
