package GreedyUSO.core.model;

import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by emre on 1/22/14.
 */
public class SensorData {

    public Body ownerBody;

    public SensorData( Body ownerBody){
        this.ownerBody = ownerBody;
    }
}
