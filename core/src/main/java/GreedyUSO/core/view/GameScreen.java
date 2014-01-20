package GreedyUSO.core.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameScreen implements Screen, ContactListener{

    World world = new World(new Vector2(0, 0), true);
    Box2DDebugRenderer debugRenderer;
    OrthographicCamera camera;
    static final float BOX_STEP=1/60f;
    static final int BOX_VELOCITY_ITERATIONS=6;
    static final int BOX_POSITION_ITERATIONS=2;
    static final float WORLD_TO_BOX=0.01f;
    static final float BOX_WORLD_TO=100f;

    private Body headPart;
    private Body bodyPartCenter;
    private Body bodyPartTail;
    private Set<Body> smallEnemies;

    private float touchDownX = 0;
    private float touchDownY = 0;

    float forceY = 0;
    float forceX = 0;
    float forceFactor = 46000;

    private List<Body> toBeDestructed = new ArrayList<Body>();

    public void initialize() {
        camera = new OrthographicCamera();
        camera.viewportHeight = 320;
        camera.viewportWidth = 480;
        camera.position.set(camera.viewportWidth * .5f, camera.viewportHeight * .5f, 0f);
        camera.update();
        debugRenderer = new Box2DDebugRenderer();
        world.setContactListener(this);
        //Ground body
        createWall(0, 10, (camera.viewportWidth) * 2, 10.0f);
        //Top body
        createWall(0, camera.viewportHeight-10,(camera.viewportWidth) * 2, 10.0f);
        //Left body
        createWall(10, 0,10.0f,(camera.viewportHeight) * 2);
        //Right body
        createWall(camera.viewportWidth-10, 0,10.0f,(camera.viewportHeight) * 2);

        createCreature();
        createJoints();

        createSmallEnemies();

        handleTouches();

    }

    private void createSmallEnemies() {
        smallEnemies = new HashSet<Body>();

        BodyDef smallEnemyDef = new BodyDef();
        smallEnemyDef.position.set( new Vector2( 100, 200));
        smallEnemyDef.type = BodyDef.BodyType.DynamicBody;
        Body enemyBody = world.createBody( smallEnemyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(2f);
        FixtureDef smallEnemyFix = new FixtureDef();
        smallEnemyFix.shape = circleShape;
        smallEnemyFix.restitution = 0.5f;
        smallEnemyFix.density = 0f;
        enemyBody.createFixture(smallEnemyFix);
        smallEnemies.add(enemyBody);

        smallEnemyDef.position.set( new Vector2( 200, 100));
        enemyBody = world.createBody( smallEnemyDef);
        enemyBody.createFixture( smallEnemyFix);
        smallEnemies.add( enemyBody);

        circleShape.dispose();

    }

    private void createWall(float x, float y, float width, float height){
        BodyDef groundBodyDef =new BodyDef();
        groundBodyDef.position.set(new Vector2(x,y));
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox(width,height);
        groundBody.createFixture(groundBox, 0.0f);
    }

    private void createJoints() {
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();

        revoluteJointDef.enableLimit = false;
        revoluteJointDef.bodyA = headPart;
        revoluteJointDef.bodyB = bodyPartCenter;
        revoluteJointDef.lowerAngle = (float)Math.toRadians(-45);
        revoluteJointDef.upperAngle = (float)Math.toRadians(45);
        revoluteJointDef.referenceAngle = 0;
        revoluteJointDef.initialize(headPart, bodyPartCenter, headPart.getWorldCenter());

        world.createJoint(revoluteJointDef);

        RevoluteJointDef revoluteJointDef2 = new RevoluteJointDef();

        revoluteJointDef2.enableLimit = true;
        revoluteJointDef2.bodyA = bodyPartCenter;
        revoluteJointDef2.bodyB = bodyPartTail;
        revoluteJointDef2.lowerAngle = (float)Math.toRadians(-45);
        revoluteJointDef2.upperAngle = (float)Math.toRadians(45);
        revoluteJointDef2.referenceAngle = 0;
        revoluteJointDef2.initialize(bodyPartCenter, bodyPartTail, bodyPartCenter.getWorldCenter());

        world.createJoint(revoluteJointDef2);

        DistanceJointDef distanceJointDef_head_left = new DistanceJointDef();
        distanceJointDef_head_left.initialize(headPart, bodyPartCenter, headPart.getWorldCenter(), bodyPartCenter.getWorldCenter());
        distanceJointDef_head_left.length = 10;
        distanceJointDef_head_left.collideConnected = true;
        world.createJoint(distanceJointDef_head_left);

        DistanceJointDef distanceJointDef_body_left = new DistanceJointDef();
        distanceJointDef_body_left.initialize(bodyPartCenter, bodyPartTail, bodyPartCenter.getWorldCenter(), bodyPartTail.getWorldCenter());
        distanceJointDef_body_left.length = 10;
        distanceJointDef_body_left.collideConnected = true;
        world.createJoint(distanceJointDef_body_left);
    }

    private void createCreature() {
        headPart = addHead(camera.viewportWidth * 0.5f, camera.viewportHeight * 0.5f);
        bodyPartCenter = addBodyPart(camera.viewportWidth * 0.48f, camera.viewportHeight * 0.5f);
        bodyPartTail = addBodyPart(camera.viewportWidth * 0.46f, camera.viewportHeight * 0.5f);

        headPart.setLinearDamping(1f);
        bodyPartCenter.setLinearDamping(1f);
        bodyPartTail.setLinearDamping(1f);
    }

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
                forceX += (i-touchDownX)* forceFactor;
                forceY += (touchDownY -i2) * forceFactor;
                touchDownX = i;
                touchDownY = i2;

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
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(5f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 1.0f;
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef);
        body.setFixedRotation(true);
        return body;
    }

    private Body addBodyPart(float x, float y){
        //Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(3f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 0.01f;
        fixtureDef.friction = 1.0f;
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef);
//        body.setFixedRotation(true);
        return body;
    }

    @Override
    public void dispose() {
    }

    private void applyForce(float deltaTime) {
        headPart.applyForce(forceX*deltaTime,forceY*deltaTime, headPart.getWorldCenter().x, headPart.getWorldCenter().y,true);
        forceX -= forceX*deltaTime;
        forceY -= forceY*deltaTime;
    }

    @Override
    public void render(float v) {
        applyForce(v);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, camera.combined);
        world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
        handleToBeDestructed();
    }

    @Override
    public void resize(int width, int height) {
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
    public void beginContact(Contact contact) {
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();
        if ( bodyA.equals( headPart) && smallEnemies.contains( bodyB)){
            smallEnemies.remove(bodyB);
            toBeDestructed.add(bodyB);
        } else if ( bodyB.equals( headPart) && smallEnemies.contains( bodyA)){
            smallEnemies.remove( bodyA);
            toBeDestructed.add( bodyA);
        }
    }

    private void handleToBeDestructed(){
        List<Body> temp = new ArrayList<Body>( toBeDestructed);
        for( Body aBody: temp){
            world.destroyBody( aBody);
            toBeDestructed.remove( aBody);
        }
    }
    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
