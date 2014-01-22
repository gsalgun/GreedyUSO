package GreedyUSO.core.model;

import GreedyUSO.core.view.GameScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

public class Entity implements Renderable{

    public static final float FRAME_DURATION = 0.10f;

    private float posX;
    private float posY;
    private float bodyWidth;
    private float bodyHeight;
    private Body body;
    private TextureAtlas animSheetAtlas;
    private TextureRegion[] animFrames;
    private TextureRegion currentFrame;
    private Animation animation;
    private Sprite sprite;
    private boolean isAnimating;

    float stateTime;

    public Entity( Body body, String atlasName, String frameName){
        this.body = body;
        body.setUserData( this);
        this.animSheetAtlas = new TextureAtlas( Gdx.files.internal(atlasName));
        this.animFrames = animSheetAtlas.findRegions(frameName).toArray(TextureRegion.class);
        this.animation = new Animation( FRAME_DURATION, animFrames);
        stateTime = 0f;
        this.isAnimating = true;
        update();
    }


    @Override
    public void render( Batch batch, float delta) {
        currentFrame = animation.getKeyFrame( stateTime, true);
        if ( isAnimating){
            stateTime += delta;
        }else{
            stateTime = 0f;
        }
        sprite = new Sprite( currentFrame);
        sprite.flip( true, false);
        sprite.setOrigin(bodyWidth,sprite.getHeight()/2);
        sprite.setX(posX - bodyWidth);
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
        Shape bodyShape = body.getFixtureList().get(0).getShape();
        if ( bodyShape instanceof  PolygonShape){
            ((PolygonShape)bodyShape).getVertex(1, vector2);
            this.bodyWidth = vector2.x;
            this.bodyHeight = vector2.y;
        }else{
            this.bodyHeight = bodyShape.getRadius();
            this.bodyWidth = bodyShape.getRadius();
        }
    }

    public void setAnimating( boolean isAnimating){
        this.isAnimating = isAnimating;
    }


}
