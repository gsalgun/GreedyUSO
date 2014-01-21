package GreedyUSO.core.model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

public class Head extends Entity{

    public Head(Body body, TextureAtlas textureAtlas) {
        super(body, textureAtlas, 6);
        this.animFrames = animSheetAtlas.findRegions("head").toArray(TextureRegion.class);
        this.animation = new Animation( FRAME_DURATION, animFrames);
    }
}
