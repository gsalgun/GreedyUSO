package GreedyUSO.core.model;

import GreedyUSO.core.view.GameScreen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;

public class Entity implements Renderable{

    private float posX;
    private float posY;
    private Body body;
    private Texture texture;
    private Sprite sprite;

    public Entity( Body body, Texture texture){
        this.body = body;
        this.texture = texture;
        this.sprite = new Sprite( texture);
        this.sprite.flip( true, false);
        update();
    }


    @Override
    public void render( Batch batch) {
       // Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        //sprite.setPosition( posX, posY);
        sprite.setX( posX);
        sprite.setY( posY);
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
