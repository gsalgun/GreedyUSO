package GreedyUSO.core.view;

import GreedyUSO.core.model.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameScreen implements Screen, ContactListener{

    World world;
    Box2DDebugRenderer debugRenderer;
    OrthographicCamera camera;

    static final float BOX_STEP=1/60f;
    static final int BOX_VELOCITY_ITERATIONS=6;
    static final int BOX_POSITION_ITERATIONS=2;
    public static final float WORLD_TO_BOX=0.01f;
    public static final float BOX_TO_WORLD =100f;

    private int screenWidth;
    private int screenHeight;
    private float worldWidth;
    private float worldHeight;
    public static final float PIXELS_PER_METER = 1f;

    public static float ConvertToBox( float x){
        return x * WORLD_TO_BOX;
    }

    private Body headPart;
    private Body bodyPartCenter;
    private Body bodyPartTail;
    private Set<Body> smallEnemies;

    private float touchDragX = 0;
    private float touchDragY = 0;

    private float touchDownX = 0;
    private float touchDownY = 0;

    private float centerReferenceX;
    private float centerReferenceY;

    float forceY = 0;
    float forceX = 0;
    float forceFactor = 40000;

    private List<Body> toBeDestructed = new ArrayList<Body>();
    private List<Entity> entities = new ArrayList<Entity>();

    private boolean isAccelerometerAvailable;

    private Batch batch = new SpriteBatch();

    public void initialize() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        worldWidth = screenWidth / PIXELS_PER_METER;
        worldHeight = screenHeight / PIXELS_PER_METER;

        world = new World( new Vector2(0, 0), true);
        camera = new OrthographicCamera();
        camera.setToOrtho( false, screenWidth, screenHeight);
//        camera.viewportHeight = 320;
//        camera.viewportWidth = 480;
        camera.position.set( screenWidth * .5f, screenHeight * .5f, 0f);
        //camera.update();
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
        isAccelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
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
        groundBodyDef.position.set(new Vector2( x / PIXELS_PER_METER, y / PIXELS_PER_METER));
        Body groundBody = world.createBody(groundBodyDef);
        PolygonShape groundBox = new PolygonShape();
        groundBox.setAsBox( width / PIXELS_PER_METER, height / PIXELS_PER_METER);
        groundBody.createFixture(groundBox, 0.0f);
    }

    private void createJoints() {

        Vector2 headJointPoint = new Vector2(headPart.getWorldCenter().x - 5, headPart.getWorldCenter().y);
        Vector2 centerHeadJointPoint = new Vector2(bodyPartCenter.getWorldCenter().x + 3, bodyPartCenter.getWorldCenter().y);
        Vector2 centerTailJointPoint = new Vector2(bodyPartCenter.getWorldCenter().x - 3, bodyPartCenter.getWorldCenter().y);
        Vector2 tailCenterJointPoint = new Vector2(bodyPartTail.getWorldCenter().x + 3, bodyPartTail.getWorldCenter().y);


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


        DistanceJointDef distanceJointDef_head_center = new DistanceJointDef();
        distanceJointDef_head_center.initialize(headPart, bodyPartCenter, headPart.getWorldCenter(), bodyPartCenter.getWorldCenter());
        distanceJointDef_head_center.dampingRatio = 0;
        distanceJointDef_head_center.frequencyHz = 60;
        distanceJointDef_head_center.length = 10 / PIXELS_PER_METER;
        distanceJointDef_head_center.collideConnected = true;
        world.createJoint(distanceJointDef_head_center);

        DistanceJointDef distanceJointDef_center_tail = new DistanceJointDef();
        distanceJointDef_center_tail.initialize(bodyPartCenter, bodyPartTail, bodyPartCenter.getWorldCenter(), bodyPartTail.getWorldCenter());
        distanceJointDef_center_tail.dampingRatio = 0;
        distanceJointDef_center_tail.frequencyHz = 70;
        distanceJointDef_center_tail.length = 10 / PIXELS_PER_METER;
        distanceJointDef_center_tail.collideConnected = true;
        world.createJoint(distanceJointDef_center_tail);
    }

    private void createCreature() {
        headPart = addHead( worldWidth/2,worldHeight/2);
        entities.add( new Entity( headPart, new Texture( Gdx.files.internal("head01.png"))));
        bodyPartCenter = addBodyPart( worldWidth * 0.48f, worldHeight * 0.5f);
        bodyPartTail = addBodyPart( worldWidth * 0.46f, worldHeight * 0.5f);

//        headPart.setLinearDamping(1f);
//        bodyPartCenter.setLinearDamping(1f);
//        bodyPartTail.setLinearDamping(1f);
        centerReferenceX = headPart.getPosition().x * PIXELS_PER_METER;
        centerReferenceY = headPart.getPosition().y * PIXELS_PER_METER;
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
                touchDragX = i;
                touchDragY = i2;
                touchDownX = i;
                touchDownY = i2;
                headPart.setLinearDamping(0);
                bodyPartCenter.setLinearDamping(0);
                bodyPartTail.setLinearDamping(0);
                return false;
            }

            @Override
            public boolean touchUp(int i, int i2, int i3, int i4) {
                headPart.setLinearDamping(2f);
                bodyPartCenter.setLinearDamping(2f);
                bodyPartTail.setLinearDamping(2f);
                forceX = 0;
                forceY = 0;
                return false;
            }

            @Override
            public boolean touchDragged(int i, int i2, int i3) {
                deltaX = i - touchDragX;
                deltaY = touchDragY - i2;
                headPart.applyForceToCenter(forceFactor * deltaX / PIXELS_PER_METER, forceFactor * deltaY / PIXELS_PER_METER ,true);

                System.out.println("deltaX: " + deltaX);
                System.out.println("deltaY: " + deltaY);

                touchDragX = i;
                touchDragY = i2;

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

    private float deltaX=0;
    private float deltaY=0;

    private Body addHead(float x, float y){
        //Dynamic Body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(5f/PIXELS_PER_METER);
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
        dynamicCircle.setRadius(3f/PIXELS_PER_METER);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicCircle;
        fixtureDef.density = 0.01f;
        fixtureDef.friction = 1.0f;
        fixtureDef.restitution = 0.5f;
        body.createFixture(fixtureDef);
        return body;
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    @Override
    public void render(float v) {
        if ( isAccelerometerAvailable){
            float x = Gdx.input.getAccelerometerX();
            float y = Gdx.input.getAccelerometerY();
            headPart.applyForceToCenter(y * forceFactor, -1f * x * forceFactor, true);
        }
        world.step(BOX_STEP, BOX_VELOCITY_ITERATIONS, BOX_POSITION_ITERATIONS);
        for ( Entity entity: entities){
            entity.update();
        }
        handleToBeDestructed();
        moveCamera();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        debugRenderer.render(world, camera.combined.scale(PIXELS_PER_METER, PIXELS_PER_METER, PIXELS_PER_METER));
        for( Entity entity: entities){
            entity.render( batch);
        }
        Vector2 velocity = headPart.getLinearVelocity();

        float angle;

        if (velocity.x == 0)
        {
            angle = velocity.y > 0 ? 0 : (float) Math.toRadians(360);
        }
        else if(velocity.y == 0)
        {
            angle = (float) (velocity.x > 0 ? Math.toRadians(180) : 3 * Math.toRadians(180));
        }
        else
        {
            angle = (float) (Math.atan(velocity.y / velocity.x) + Math.toRadians(180));
        }

        if (velocity.x > 0)
        {
            angle += Math.toRadians(180);
        }
        headPart.setTransform(headPart.getPosition(), angle);


    }

    private void rotateHead(float angle) {
        if(angle!=0){
            headPart.setTransform(headPart.getPosition(),0);
            headPart.setTransform(headPart.getPosition(), angle);
            headPart.setAngularVelocity(0);
        }
    }

    float diffX = 0;
    float diffY = 0;

    private void moveCamera() {
        diffX = headPart.getPosition().x * PIXELS_PER_METER - centerReferenceX;
        diffY = headPart.getPosition().y * PIXELS_PER_METER - centerReferenceY;
        camera.translate(diffX, diffY);
        centerReferenceX = headPart.getPosition().x * PIXELS_PER_METER;
        centerReferenceY = headPart.getPosition().y * PIXELS_PER_METER;
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
