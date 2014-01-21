package GreedyUSO.core.model;

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
        update();
    }


    @Override
    public void render( Batch batch) {
       // Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        //Gdx.app.log("Entity", "posX: " + posX + " posY:" + posY);
        sprite.setPosition(posX, posY);
        sprite.setRotation((float) Math.toDegrees(body.getAngle()));
        sprite.setScale(0.1f);
        batch.begin();
        sprite.draw( batch);
        batch.end();
    }

    public void update(){
        this.posX = body.getPosition().x;
        this.posY = body.getPosition().y;
    }


}
