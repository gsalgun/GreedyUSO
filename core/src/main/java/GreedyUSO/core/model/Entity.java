package GreedyUSO.core.model;

import GreedyUSO.core.view.GameScreen;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.Body;

public class Entity implements Renderable{

    public static final float FRAME_DURATION = 0.10f;

    protected float posX;
    protected float posY;
    protected Body body;
    protected TextureAtlas animSheetAtlas;
    protected TextureRegion[] animFrames;
    protected TextureRegion currentFrame;
    protected Animation animation;
    protected Sprite sprite;

    float stateTime;

    public Entity( Body body, TextureAtlas textureAtlas, int numberOfFrames){
        this.body = body;
        this.animSheetAtlas = textureAtlas;
        this.animFrames = new TextureRegion[numberOfFrames];
        this.sprite = new Sprite();
        stateTime = 0f;
        update();
    }


    @Override
    public void render( Batch batch, float delta) {
        stateTime += delta;
        currentFrame = animation.getKeyFrame( stateTime, true);
        sprite = new Sprite( currentFrame);
        sprite.flip( true, false);
        sprite.setX(posX - sprite.getWidth()/2);
        sprite.setY(posY - sprite.getHeight()/2);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
        batch.begin();
        sprite.draw( batch);
        batch.end();
    }

    public void update(){
        this.posX = body.getPosition().x * GameScreen.PIXELS_PER_METER;
        this.posY = body.getPosition().y * GameScreen.PIXELS_PER_METER;
    }


}
