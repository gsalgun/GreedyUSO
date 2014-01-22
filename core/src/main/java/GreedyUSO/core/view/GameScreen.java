package GreedyUSO.core.view;

import GreedyUSO.core.model.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameScreen implements Screen, ContactListener{

    private final int BODY_PART_RADIUS = 30;
    private final float HEAD_RADIUS = 50;


    private float HEAD_LENGTH=60;
    private float HEAD_HEIGHT=40;

    private float BODY_LENGTH=30;
    private float BODY_HEIGHT=20;

    private final int JOINT_LENGTH = 1;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;

    static final float WORLD_STEP =1/60f;
    static final int VELOCITY_ITERATIONS =6;
    static final int POSITION_ITERATIONS =10;
    public static final float WORLD_TO_BOX=0.01f;
    public static final float BOX_TO_WORLD =100f;

    private int screenWidth;
    private int screenHeight;
    private float worldWidth;
    private float worldHeight;
    public static final float PIXELS_PER_METER = 1f;
    private Body evilBodyMouth;
    private Label scoreLabel;

    public static float ConvertToBox( float x){
        return x * WORLD_TO_BOX;
    }

    private Body headPart;
    private Body bodyPart1;
    private Body bodyPart2;
    private Body tailPart;
    private Set<Body> smallEnemies;

    private float touchDragX = 0;
    private float touchDragY = 0;

    private float centerReferenceX;
    private float centerReferenceY;

    float forceY = 0;
    float forceX = 0;
    float forceFactor = 3000000;

    private List<Body> toBeDestructed = new ArrayList<Body>();
    private List<Entity> entities = new ArrayList<Entity>();

    private boolean isAccelerometerAvailable;

    private Batch batch = new SpriteBatch();

    private Stage stage;
    private Batch stageBatch;
    private Camera stageCamera;


    public void initialize() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        worldWidth = screenWidth / PIXELS_PER_METER;
        worldHeight = screenHeight / PIXELS_PER_METER;

        initializeUI();

        world = new World( new Vector2(0, 0), true);
        camera = new OrthographicCamera();
        camera.setToOrtho( false, screenWidth, screenHeight);
        camera.position.set( screenWidth * .5f, screenHeight * .5f, 0);
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

        createEvilEnemy();

        handleTouches();
        isAccelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        loadBackGrounds();

    }

    private void initializeUI() {
        stage = new Stage();
        stageBatch = new SpriteBatch();
        stageCamera = new OrthographicCamera( screenWidth, screenHeight);
        stage.setCamera( stageCamera);

        Skin hudSkin = new Skin();
        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pixmap.setColor( Color.WHITE);
        pixmap.fill();
        hudSkin.add("white", new Texture( pixmap));
        hudSkin.add("default", new BitmapFont());

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = hudSkin.getFont("default");
        labelStyle.background = hudSkin.newDrawable("white", Color.BLUE);
        hudSkin.add("default", labelStyle);
        scoreLabel = new Label("1000", hudSkin);
        scoreLabel.setPosition(-screenWidth / 2, -screenHeight / 2);
        stage.addActor(scoreLabel);
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
        smallEnemyDef.position.set( new Vector2( 100 / PIXELS_PER_METER, 200 / PIXELS_PER_METER));
        smallEnemyDef.type = BodyDef.BodyType.DynamicBody;
        Body enemyBody = world.createBody( smallEnemyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius( 20f / PIXELS_PER_METER);
        FixtureDef smallEnemyFix = new FixtureDef();
        smallEnemyFix.shape = circleShape;
        smallEnemyFix.restitution = 0.5f;
        smallEnemyFix.density = 0f;
        enemyBody.createFixture(smallEnemyFix);
        smallEnemies.add(enemyBody);
        entities.add( new Entity( enemyBody, "smallEnemy.atlas", "smallEnemy"));

        smallEnemyDef.position.set( new Vector2( 200 / PIXELS_PER_METER, 100 / PIXELS_PER_METER));
        enemyBody = world.createBody( smallEnemyDef);
        enemyBody.createFixture( smallEnemyFix);
        smallEnemies.add( enemyBody);
        entities.add( new Entity( enemyBody, "smallEnemy.atlas", "smallEnemy"));

        circleShape.dispose();

    }

    private void createEvilEnemy() {
        BodyDef evilBodyDef = new BodyDef();
        evilBodyDef.position.set( new Vector2( 1000/PIXELS_PER_METER, 400/PIXELS_PER_METER));
        evilBodyDef.type = BodyDef.BodyType.DynamicBody;
        Body evilBody = world.createBody( evilBodyDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius( 80f / PIXELS_PER_METER);
        FixtureDef evilFixture = new FixtureDef();
        evilFixture.shape = circleShape;
        evilFixture.restitution = 0.5f;
        evilFixture.density = 5f;
        evilBody.createFixture( evilFixture);
        smallEnemies.add( evilBody);
        entities.add( new Entity( evilBody, "evilEnemy.atlas", "evilBody"));

        circleShape.dispose();

        BodyDef evilBodyMouthDef = new BodyDef();
        evilBodyMouthDef.position.set( new Vector2( 1040f/PIXELS_PER_METER, 400/PIXELS_PER_METER));
        evilBodyMouthDef.type = BodyDef.BodyType.DynamicBody;
        evilBodyMouth = world.createBody( evilBodyMouthDef);
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.set( new Vector2[]{ new Vector2( -40f/PIXELS_PER_METER, -80f/PIXELS_PER_METER),
                                         new Vector2( -40f/PIXELS_PER_METER, 80f/PIXELS_PER_METER),
                                         new Vector2( 40f/PIXELS_PER_METER, 80/PIXELS_PER_METER),
                                         new Vector2( 40f/PIXELS_PER_METER, -80f/PIXELS_PER_METER)});
        FixtureDef evilBodyMouthFixture = new FixtureDef();
        evilBodyMouthFixture.shape = polygonShape;
        evilBodyMouthFixture.restitution = 0.5f;
        evilBodyMouthFixture.density = 5f;
        evilBodyMouth.createFixture(evilBodyMouthFixture);
        smallEnemies.add(evilBodyMouth);

        WeldJointDef weldJointDef = new WeldJointDef();
        weldJointDef.initialize( evilBody, evilBodyMouth, evilBody.getPosition());
        world.createJoint( weldJointDef);

        polygonShape.dispose();


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

        WeldJointDef revoluteJointDef_1 = new WeldJointDef();
        revoluteJointDef_1.initialize(headPart, bodyPart1, bodyPart1.getPosition());
        revoluteJointDef_1.localAnchorA.set(-HEAD_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_1.localAnchorB.set(BODY_LENGTH, 0);
        revoluteJointDef_1.collideConnected = true;
        world.createJoint(revoluteJointDef_1);

        RevoluteJointDef revoluteJointDef_2 = new RevoluteJointDef();
        revoluteJointDef_2.initialize(bodyPart1, bodyPart2, bodyPart2.getPosition());
        revoluteJointDef_2.localAnchorA.set(-BODY_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_2.localAnchorB.set(BODY_LENGTH, 0);
        revoluteJointDef_2.collideConnected = true;
//        revoluteJointDef_2.enableMotor = true;
//        revoluteJointDef_2.maxMotorTorque = 1;
//        revoluteJointDef_2.motorSpeed = 10;
        world.createJoint(revoluteJointDef_2);

        RevoluteJointDef revoluteJointDef_3 = new RevoluteJointDef();
        revoluteJointDef_3.initialize(bodyPart2, tailPart, tailPart.getPosition());
        revoluteJointDef_3.localAnchorA.set(-BODY_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_3.localAnchorB.set(BODY_LENGTH, 0);
        revoluteJointDef_3.collideConnected = true;
//        revoluteJointDef_3.enableMotor = true;
//        revoluteJointDef_3.maxMotorTorque = 1;
//        revoluteJointDef_3.motorSpeed = 10;
        world.createJoint(revoluteJointDef_3);
    }

    private void createCreature() {
        headPart = addHead( worldWidth/2,worldHeight/2);
        entities.add( new Entity( headPart, "head.atlas", "head"));
        bodyPart1 = addBodyPart( headPart.getPosition().x - HEAD_LENGTH - JOINT_LENGTH, worldHeight * 0.5f);
        entities.add( new Entity( bodyPart1, "body.atlas", "body0"));
        bodyPart2 = addBodyPart( bodyPart1.getPosition().x - BODY_LENGTH - JOINT_LENGTH , worldHeight * 0.5f);
        entities.add(new Entity( bodyPart2, "body.atlas", "body0"));
        tailPart = addTail(bodyPart2.getPosition().x - BODY_LENGTH - JOINT_LENGTH, worldHeight * 0.5f);
        entities.add( new Entity( tailPart, "body.atlas", "body0"));

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
                bodyPart1.setLinearDamping(0);
                bodyPart2.setLinearDamping(0);

                return false;
            }

            @Override
            public boolean touchUp(int i, int i2, int i3, int i4) {

                headPart.setLinearDamping(1f);
                bodyPart1.setLinearDamping(1f);
                bodyPart2.setLinearDamping(1f);

                return false;
            }

            @Override
            public boolean touchDragged(int i, int i2, int i3) {
                deltaX = i- touchDragX;
                deltaY = touchDragY - i2;
                headPart.applyForceToCenter(forceFactor * deltaX, forceFactor * deltaY ,true);

                touchDragX = i;
                touchDragY = i2;

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
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(HEAD_LENGTH,-HEAD_HEIGHT),new Vector2(HEAD_LENGTH,HEAD_HEIGHT),new Vector2(-HEAD_LENGTH,-HEAD_HEIGHT),new Vector2(-HEAD_LENGTH,HEAD_HEIGHT)});
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 11f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.3f;
        body.createFixture(fixtureDef);
        body.setFixedRotation(true);
        return body;
    }

    private Body addBodyPart(float x, float y){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(BODY_LENGTH,-BODY_HEIGHT),new Vector2(BODY_LENGTH,BODY_HEIGHT),new Vector2(-BODY_LENGTH,-BODY_HEIGHT),new Vector2(-BODY_LENGTH,BODY_HEIGHT)});
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 11f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.3f;
        body.createFixture(fixtureDef);
        body.setAngularDamping(3);
        return body;
    }

    private Body addTail(float x, float y){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(BODY_PART_RADIUS /PIXELS_PER_METER);
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(BODY_LENGTH,-BODY_HEIGHT),new Vector2(BODY_LENGTH,BODY_HEIGHT),new Vector2(-BODY_LENGTH,-BODY_HEIGHT),new Vector2(-BODY_LENGTH,BODY_HEIGHT)});
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.01f;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0.3f;
        body.createFixture(fixtureDef);
        return body;
    }

    @Override
    public void dispose() {
        batch.dispose();
        stageBatch.dispose();
        stage.dispose();
    }

    @Override
    public void render(float v) {
        if ( isAccelerometerAvailable){
            float x = Gdx.input.getAccelerometerX();
            float y = Gdx.input.getAccelerometerY();
            headPart.applyForceToCenter(y * forceFactor, -1f * x * forceFactor, true);
        }
        world.step(WORLD_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

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
        for(int i = entities.size()-1; i>=0; i--){
            entities.get(i).render( batch, v);
        }

        stage.act( v);
        stageBatch.setProjectionMatrix( stageCamera.combined);
        stageBatch.begin();
        stage.draw();
        stageBatch.end();
    }

    private float diffX = 0;
    private float diffY = 0;

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
        Body toBeRemoved = null;
        if ( bodyA.equals( headPart) && smallEnemies.contains( bodyB)){
            if ( bodyB.equals( evilBodyMouth)){
                scoreLabel.setText("GAME OVER");
            }
            toBeRemoved = bodyB;
        } else if ( bodyB.equals( headPart) && smallEnemies.contains( bodyA)){
            if ( bodyA.equals( evilBodyMouth)){
                scoreLabel.setText("GAME OVER");
            }
            toBeRemoved = bodyA;
        }
        if ( toBeRemoved != null){
            smallEnemies.remove( toBeRemoved);
            toBeDestructed.add( toBeRemoved);
            entities.remove( toBeRemoved.getUserData());
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
