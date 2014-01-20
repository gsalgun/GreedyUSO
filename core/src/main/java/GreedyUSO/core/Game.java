package GreedyUSO.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;

public class Game implements ApplicationListener {
//    World world = new World(new Vector2(0,0),true);
    World world = new World(new Vector2(0, 0), true);
    Box2DDebugRenderer debugRenderer;
    OrthographicCamera camera;
    static final float BOX_STEP=1/60f;
    static final int BOX_VELOCITY_ITERATIONS=6;
    static final int BOX_POSITION_ITERATIONS=2;
    static final float WORLD_TO_BOX=0.01f;
    static final float BOX_WORLD_TO=100f;
    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.viewportHeight = 320;
        camera.viewportWidth = 480;
        camera.position.set(camera.viewportWidth * .5f, camera.viewportHeight * .5f, 0f);
        camera.update();
        //Ground body
        BodyDef groundBodyDef =new BodyDef();
        groundBodyDef.position.set(new Vector2(0, 10));
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox((camera.viewportWidth) * 2, 10.0f);
        groundBody.createFixture(groundBox, 0.0f);

        //Top body
        BodyDef topBodyDef =new BodyDef();
        topBodyDef.position.set(new Vector2(0, camera.viewportHeight-10));
        Body topBody = world.createBody(topBodyDef);
        PolygonShape topBox = new PolygonShape();
        topBox.setAsBox((camera.viewportWidth) * 2, 10.0f);
        topBody.createFixture(topBox, 0.0f);

        //Left body
        BodyDef leftBodyDef =new BodyDef();
        leftBodyDef.position.set(new Vector2(10, 0));
        Body leftBody = world.createBody(leftBodyDef);
        PolygonShape leftBox = new PolygonShape();
        leftBox.setAsBox(10.0f,(camera.viewportHeight) * 2);
        leftBody.createFixture(leftBox, 0.0f);

        //Right body
        BodyDef rightBodyDef =new BodyDef();
        rightBodyDef.position.set(new Vector2(camera.viewportWidth-10, 0));
        Body rightBody = world.createBody(rightBodyDef);
        PolygonShape rightBox = new PolygonShape();
        rightBox.setAsBox(10.0f,(camera.viewportHeight) * 2);
        rightBody.createFixture(leftBox, 0.0f);

        body1 = addHead(camera.viewportWidth * 0.5f, camera.viewportHeight * 0.5f);
        body2 = addBodyPart(camera.viewportWidth * 0.45f, camera.viewportHeight * 0.5f);
        body3 = addBodyPart(camera.viewportWidth * 0.4f, camera.viewportHeight * 0.5f);

//        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
//        revoluteJointDef.initialize(body1,body2,body1.getWorldCenter());
//
//        revoluteJointDef.maxMotorTorque = 1;
//        revoluteJointDef.enableMotor = true;
//
//        world.createJoint(revoluteJointDef);

        handleTouches();

        RopeJointDef ropeJointDef = new RopeJointDef();
        ropeJointDef.bodyA = body1;
        ropeJointDef.bodyB = body2;
        ropeJointDef.maxLength = 15;
        ropeJointDef.collideConnected = true;
        world.createJoint(ropeJointDef);

        RopeJointDef ropeJointDef2 = new RopeJointDef();
        ropeJointDef2.bodyA = body2;
        ropeJointDef2.bodyB = body3;
        ropeJointDef2.maxLength = 15;
        ropeJointDef2.collideConnected = true;
        world.createJoint(ropeJointDef2);


        debugRenderer = new Box2DDebugRenderer();
    }

    private Body body1;
    private Body body2;
    private Body body3;

    private float touchDownX = 0;
    private float touchDownY = 0;

    float forceY = 0;
    float forceX = 0;
    float forceFactor = 46000;

    private void handleTouches(){
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int i) {
                return false;
            }

            @Override
            public boolean keyUp(int i) {
                return false;
            }

            @Override
            public boolean keyTyped(char c) {
                return false;
            }

            @Override
            public boolean touchDown(int i, int i2, int i3, int i4) {
                touchDownX = i;
                touchDownY = i2;
                return false;
            }

            @Override
            public boolean touchUp(int i, int i2, int i3, int i4) {
                return false;
            }

            @Override
            public boolean touchDragged(int i, int i2, int i3) {

                if(forceX>0 && (i-touchDownX)<0){
                    forceX = 0;
                }
                if(forceY>0 && (touchDownY -i2)<0){
                    forceY= 0;
                }

                if(forceX<0 && (i-touchDownX)>0){
                    forceX = 0;
                }
                if(forceY<0 && (touchDownY -i2)>0){
                    forceY= 0;
                } 
                forceX = (i-touchDownX)* forceFactor;
                forceY = (touchDownY -i2) * forceFactor;
                touchDownX = i;
                touchDownY = i2;

//                body1.seta(forceX,forceY);
                body1.applyForce(forceX,forceY,body1.getWorldCenter().x, body1.getWorldCenter().y,true);
//
                return false;
            }

            @Override
            public boolean mouseMoved(int i, int i2) {
                return false;
            }

            @Override
            public boolean scrolled(int i) {
                return false;
            }
        });
    }

    private Body addHead(float x, float y){
        //Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 1.0f;
        fixtureDef.restitution = 1;
        body.createFixture(fixtureDef);
        return body;
    }

    private Body addBodyPart(float x, float y){
        //Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(3f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 1.0f;
        fixtureDef.restitution = 1;
        body.createFixture(fixtureDef);
        return body;
    }

    @Override
    public void dispose() {
    }
    @Override
    public void render() {
//        applyForce(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, camera.combined);
        world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
    }

    private void applyForce(float deltaTime) {
        body1.applyForce(forceX*deltaTime,forceY*deltaTime,body1.getWorldCenter().x,body1.getWorldCenter().y,true);
//        forceX -= forceX*deltaTime;
//        forceY -= forceY*deltaTime;
    }

    @Override
    public void resize(int width, int height) {
    }
    @Override
    public void pause() {
    }
    @Override
    public void resume() {
    }
}