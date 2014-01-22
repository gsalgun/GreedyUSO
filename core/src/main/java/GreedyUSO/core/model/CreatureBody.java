package GreedyUSO.core.model;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by emre on 1/21/14.
 */
public class CreatureBody extends Entity{
    public CreatureBody(Body body, TextureAtlas textureAtlas) {
        super(body, textureAtlas, 1);
        this.animFrames = animSheetAtlas.findRegions("body0").toArray(TextureRegion.class);
        this.animation = new Animation( FRAME_DURATION, animFrames);
    }
}
