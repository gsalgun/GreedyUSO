package GreedyUSO.core.model;

import GreedyUSO.core.view.GameScreen;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Entity implements Renderable{

    public static final float FRAME_DURATION = 0.10f;

    protected float posX;
    protected float posY;
    protected float width;
    protected float height;
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
        sprite.setOrigin(width,sprite.getHeight()/2);
        sprite.setX(posX - width);
        sprite.setY(posY - sprite.getHeight()/2);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
        batch.begin();
        sprite.draw( batch);
        batch.end();
    }

    public void update(){
        this.posX = body.getPosition().x * GameScreen.PIXELS_PER_METER;
        this.posY = body.getPosition().y * GameScreen.PIXELS_PER_METER;
        Vector2 vector2 = new Vector2();
        ((PolygonShape)body.getFixtureList().get(0).getShape()).getVertex(1,vector2);
        this.width = vector2.x;
        this.height = vector2.y;
    }


}
