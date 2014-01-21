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
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameScreen implements Screen, ContactListener{

    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;

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

    private float centerReferenceX;
    private float centerReferenceY;

    float forceY = 0;
    float forceX = 0;
    float forceFactor = 46000;

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
        camera.position.set( screenWidth * .5f, screenHeight * .5f, 0f);
        //camera.update();
        debugRenderer = new Box2DDebugRenderer();
        world.setContactListener(this);
        //Ground body
        createWall(0, -(camera.viewportHeight * 2) + 10, (camera.viewportWidth) * 2, 10.0f);
        //Top body
        createWall(0, (camera.viewportHeight * 2) + camera.viewportHeight-10,(camera.viewportWidth) * 2, 10.0f);
        //Left body
        createWall(-(camera.viewportWidth * 2) + 10, 0,10.0f,(camera.viewportHeight) * 2);
        //Right body
        createWall((camera.viewportWidth * 2) + camera.viewportWidth-10, 0,10.0f,(camera.viewportHeight) * 2);

        createCreature();
        createJoints();

        createSmallEnemies();

        handleTouches();
        isAccelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        loadBackGrounds();

    }

    private List<Sprite> backgrounds = new ArrayList<Sprite>();

    private void loadBackGrounds() {
        Sprite background11 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background12 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background13 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background14 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background15 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background21 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background22 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background23 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background24 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background25 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background31 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background32 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background33 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background34 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background35 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background41 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background42 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background43 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background44 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background45 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background51 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background52 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background53 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background54 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background55 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background61 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background62 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background63 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background64 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background65 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background71 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background72 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background73 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background74 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));
        Sprite background75 = new Sprite(new Texture(Gdx.files.internal("background11.jpg")));




        background11.setPosition(-2560,2400);
        background12.setPosition(-1280,2400);
        background13.setPosition(0    ,2400);
        background14.setPosition(1280 ,2400);
        background15.setPosition(2560 ,2400);
        background21.setPosition(-2560,1600);
        background22.setPosition(-1280,1600);
        background23.setPosition(0    ,1600);
        background24.setPosition(1280 ,1600);
        background25.setPosition(2560 ,1600);
        background31.setPosition(-2560,800);
        background32.setPosition(-1280,800);
        background33.setPosition(0    ,800);
        background34.setPosition(1280 ,800);
        background35.setPosition(2560 ,800);
        background41.setPosition(-2560,0);
        background42.setPosition(-1280,0);
        background43.setPosition(0    ,0);
        background44.setPosition(1280 ,0);
        background45.setPosition(2560 ,0);
        background51.setPosition(-2560,-800);
        background52.setPosition(-1280,-800);
        background53.setPosition(0    ,-800);
        background54.setPosition(1280 ,-800);
        background55.setPosition(2560 ,-800);
        background61.setPosition(-2560,-1600);
        background62.setPosition(-1280,-1600);
        background63.setPosition(0    ,-1600);
        background64.setPosition(1280 ,-1600);
        background65.setPosition(2560 ,-1600);
        background71.setPosition(-2560,-2400);
        background72.setPosition(-1280,-2400);
        background73.setPosition(0    ,-2400);
        background74.setPosition(1280 ,-2400);
        background75.setPosition(2560 ,-2400);

        backgrounds.add( background11);
        backgrounds.add( background12);
        backgrounds.add( background13);
        backgrounds.add( background14);
        backgrounds.add( background15);
        backgrounds.add( background21);
        backgrounds.add( background22);
        backgrounds.add( background23);
        backgrounds.add( background24);
        backgrounds.add( background25);
        backgrounds.add( background31);
        backgrounds.add( background32);
        backgrounds.add( background33);
        backgrounds.add( background34);
        backgrounds.add( background35);
        backgrounds.add( background41);
        backgrounds.add( background42);
        backgrounds.add( background43);
        backgrounds.add( background44);
        backgrounds.add( background45);
        backgrounds.add( background51);
        backgrounds.add( background52);
        backgrounds.add( background53);
        backgrounds.add( background54);
        backgrounds.add( background55);
        backgrounds.add( background61);
        backgrounds.add( background62);
        backgrounds.add( background63);
        backgrounds.add( background64);
        backgrounds.add( background65);
        backgrounds.add( background71);
        backgrounds.add( background72);
        backgrounds.add( background73);
        backgrounds.add( background74);
        backgrounds.add( background75);
    }

    private void createSmallEnemies() {
        smallEnemies = new HashSet<Body>();

        BodyDef smallEnemyDef = new BodyDef();
        smallEnemyDef.position.set( new Vector2(100, 200));
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

        DistanceJointDef distanceJointDef_head_center = new DistanceJointDef();
        distanceJointDef_head_center.initialize(headPart, bodyPartCenter,headJointPoint, centerHeadJointPoint);
        distanceJointDef_head_center.dampingRatio = 0;
        distanceJointDef_head_center.frequencyHz = 100;
        distanceJointDef_head_center.length = 1.5f;
        distanceJointDef_head_center.collideConnected = true;
        world.createJoint(distanceJointDef_head_center);

        DistanceJointDef distanceJointDef_center_tail = new DistanceJointDef();
        distanceJointDef_center_tail.initialize(bodyPartCenter, bodyPartTail, centerTailJointPoint, tailCenterJointPoint);
        distanceJointDef_center_tail.dampingRatio = 0;
        distanceJointDef_center_tail.frequencyHz = 100;
        distanceJointDef_center_tail.length = 1.5f;
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
                deltaX = i- touchDragX;
                deltaY = touchDragY - i2;
                headPart.applyForceToCenter(forceFactor * deltaX, forceFactor * deltaY ,true);

                touchDragX = i;
                touchDragY = i2;


                //
                setHeadAngle();

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

    private void setHeadAngle() {
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

    private float deltaX=0;
    private float deltaY=0;

    private Body addHead(float x, float y){
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
        body.setAngularDamping(55);
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

        batch.begin();

        for(Sprite sprite:backgrounds){
            sprite.draw(batch);
        }
        batch.end();

        debugRenderer.render(world, camera.combined.scale(PIXELS_PER_METER, PIXELS_PER_METER, PIXELS_PER_METER));
        for( Entity entity: entities){
            entity.render( batch);
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
