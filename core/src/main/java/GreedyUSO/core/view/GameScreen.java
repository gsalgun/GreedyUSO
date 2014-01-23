package GreedyUSO.core.view;

import GreedyUSO.core.model.Entity;
import GreedyUSO.core.model.SensorData;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import java.util.*;

public class GameScreen implements Screen, ContactListener{

    private final int BODY_PART_RADIUS = 30;
    private final float HEAD_RADIUS = 50;
    private final AssetManager assetManager;
    private final Game game;
    private boolean isLevelUp;
    private int eatCount = 0;


    private float HEAD_LENGTH=60 / PIXELS_PER_METER;
    private float HEAD_HEIGHT=40/ PIXELS_PER_METER;

    private float BODY_LENGTH=30 / PIXELS_PER_METER;
    private float BODY_HEIGHT=20 / PIXELS_PER_METER;

    private float TAIL1_LENGTH=25 / PIXELS_PER_METER;
    private float TAIL1_HEIGHT=20 / PIXELS_PER_METER;

    private float TAIL2_LENGTH=20 / PIXELS_PER_METER;
    private float TAIL2_HEIGHT=20 / PIXELS_PER_METER;

    private float TAIL3_LENGTH=15 / PIXELS_PER_METER;
    private float TAIL3_HEIGHT=20 / PIXELS_PER_METER;

    private final float JOINT_LENGTH = 1 / PIXELS_PER_METER;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;

    static final float WORLD_STEP =1/60f;
    static final int VELOCITY_ITERATIONS =50;
    static final int POSITION_ITERATIONS =50;
    public static final float WORLD_TO_BOX=0.01f;
    public static final float BOX_TO_WORLD =100f;

    private int screenWidth;
    private int screenHeight;
    private float worldWidth;
    private float worldHeight;
    private Body evilBodyMouth;
    private Label scoreLabel;
    private List<Body> sensors = new ArrayList<Body>();
    public static final float PIXELS_PER_METER = 15f;
    private Body evilBody;

    private Body headPart;
    private Body bodyPart0;
    private Body bodyPart1;
    private Body bodyPart2;
    private Body bodyPart3;
    private Body bodyPart4;
    private Body tailPart;
    private Set<Body> smallEnemies;

    private float touchDragX = 0;
    private float touchDragY = 0;

    private float centerReferenceX;
    private float centerReferenceY;

    float forceFactor = 60;

    private List<Body> toBeDestructed = new ArrayList<Body>();
    private List<Entity> entities = new ArrayList<Entity>();

    private boolean isAccelerometerAvailable;

    private Batch batch = new SpriteBatch();

    private Stage stage;
    private Batch stageBatch;
    private Camera stageCamera;
    private Body evilBodySensor;

    private Set<Body> creatureParts = new HashSet<Body>();
    private boolean isGameOver;
    private boolean levelComplete;
    private Sprite gameOverSprite;
    private Sprite levelCompleteSprite;
    private Sprite eatTextSprite;
    private Map<Integer, Image> textImages;
    private Random randomTextGenerator;

    public GameScreen( AssetManager assetManager, Game game, boolean isLevelUp){
        this.isLevelUp = isLevelUp;
        this.assetManager = assetManager;
        this.game = game;
    }

    public void initialize() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
        worldWidth = screenWidth / PIXELS_PER_METER;
        worldHeight = screenHeight / PIXELS_PER_METER;

        initializeUI();

        world = new World( new Vector2(0, 0), true);
        world.setVelocityThreshold(1000);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);
        camera.position.set( screenWidth * .5f, screenHeight * .5f, 0);
        debugRenderer = new Box2DDebugRenderer();
        world.setContactListener(this);
        createCreature(isLevelUp);
        createJoints();

        createSmallEnemies();

        createEvilEnemy();

        isAccelerometerAvailable = Gdx.input.isPeripheralAvailable(Input.Peripheral.Accelerometer);
        loadBackGrounds();
        createTouchPad();
        //handleTouches();

        TextureAtlas textsAtlas = assetManager.get("texts.atlas", TextureAtlas.class);
        gameOverSprite = new Sprite( textsAtlas.findRegion("game-over"));
        levelCompleteSprite = new Sprite( textsAtlas.findRegion("level-complete"));
        textImages = new HashMap<Integer, Image>();
        textImages.put(0, new Image(textsAtlas.findRegion("nam-nam")));
        textImages.put(1, new Image(textsAtlas.findRegion("wow")));
        textImages.put(2, new Image(textsAtlas.findRegion("yuck!")));
        textImages.put(3, new Image(textsAtlas.findRegion("yummy")));

        randomTextGenerator = new Random();
    }

    private Touchpad touchpad;

    private void createTouchPad() {
        Skin touchpadSkin = new Skin();
        //Set background image
        touchpadSkin.add("touchBackground", new Texture("touchBackground.png"));
        //Set knob image
        touchpadSkin.add("touchKnob", new Texture("touchKnob.png"));
        //Create TouchPad Style
        Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
        //Create Drawable's from TouchPad skin
        Drawable touchBackground = touchpadSkin.getDrawable("touchBackground");
        Drawable touchKnob = touchpadSkin.getDrawable("touchKnob");
        //Apply the Drawables to the TouchPad Style
        touchpadStyle.background = touchBackground;
        touchpadStyle.knob = touchKnob;
        //Create new TouchPad with the created style
        touchpad = new Touchpad(10, touchpadStyle);
        //setBounds(x,y,width,height)
        touchpad.setBounds(15, 15, 200, 200);
        touchpad.addAction(Actions.alpha(0));
        this.stage.addActor(touchpad);
        Gdx.input.setInputProcessor(this.stage);
        this.stage.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if ( isGameOver){
                    StartScreen startScreen = new StartScreen( assetManager, game);
                    startScreen.initialize();
                    game.setScreen( startScreen);
                }else if ( levelComplete){
                    GameScreen gameScreen = new GameScreen( assetManager, game, true);
                    gameScreen.initialize();
                    game.setScreen( gameScreen);
                }

                if (button == FAKE_BUTTON) {
                    return true;
                }
                System.out.println("" + button);
                touchpad.setPosition(x - touchpad.getWidth() / 2, y - touchpad.getHeight() / 2);
                touchpad.invalidate();
                touchpad.addAction(Actions.alpha(1));
                InputEvent fakeEvent = new InputEvent();
                fakeEvent.setType(InputEvent.Type.touchDown);
                fakeEvent.setStageX(x);
                fakeEvent.setStageY(y);
                fakeEvent.setButton(FAKE_BUTTON);
                touchpad.fire(fakeEvent);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (button == FAKE_BUTTON) {
                    return;
                }
                touchpad.addAction(Actions.alpha(0));
                headPart.setLinearDamping(3f);
                InputEvent fakeEvent = new InputEvent();
                fakeEvent.setType(InputEvent.Type.touchUp);
                fakeEvent.setStageX(x);
                fakeEvent.setStageY(y);
                fakeEvent.setButton(FAKE_BUTTON);
                touchpad.fire(fakeEvent);
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    private void checkDumping() {
        System.out.println("x: "+ headPart.getPosition().x + " / y: "+headPart.getPosition().y);
        if(headPart.getPosition().y > 2750/PIXELS_PER_METER || headPart.getPosition().y < -1950/PIXELS_PER_METER){
            headPart.setLinearDamping(299);
            return;
        } else if(headPart.getPosition().y > 2650/PIXELS_PER_METER || headPart.getPosition().y < -1850/PIXELS_PER_METER){
            headPart.setLinearDamping(25);
            return;
        }
        if(headPart.getPosition().x > 3150/PIXELS_PER_METER || headPart.getPosition().x < -1870/PIXELS_PER_METER){
            headPart.setLinearDamping(299);
            return;
        } else if(headPart.getPosition().x > 3050/PIXELS_PER_METER || headPart.getPosition().x < -1770/PIXELS_PER_METER){
            headPart.setLinearDamping(25);
            return;
        }
        if(touchpad.isTouched()){
            headPart.setLinearDamping(0);
        } else {
            headPart.setLinearDamping(9);
        }
    }

    private static int FAKE_BUTTON = 99;


    private void handleTouchpadMove() {
        if (touchpad.isTouched()) {

            float xForce = forceFactor * touchpad.getKnobPercentX();
            float yForce = forceFactor * touchpad.getKnobPercentY();
            headPart.setLinearVelocity(xForce, yForce);
            setHeadAngle();
        }
        checkDumping();
    }

    private void initializeUI() {
        stage = new Stage();
        stageBatch = new SpriteBatch();
        stageCamera = new OrthographicCamera( screenWidth, screenHeight);
        stage.setCamera(stageCamera);

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
        Sprite background11 = new Sprite( assetManager.get( "BG_A1.png", Texture.class));
        Sprite background12 = new Sprite( assetManager.get( "BG_A2.png", Texture.class));
        Sprite background13 = new Sprite( assetManager.get( "BG_A3.png", Texture.class));
        Sprite background14 = new Sprite( assetManager.get( "BG_A4.png", Texture.class));
        Sprite background15 = new Sprite( assetManager.get( "BG_A5.png", Texture.class));
        Sprite background21 = new Sprite( assetManager.get( "BG_B1.png", Texture.class));
        Sprite background22 = new Sprite( assetManager.get( "BG_B2.png", Texture.class));
        Sprite background23 = new Sprite( assetManager.get( "BG_B3.png", Texture.class));
        Sprite background24 = new Sprite( assetManager.get( "BG_B4.png", Texture.class));
        Sprite background25 = new Sprite( assetManager.get( "BG_B5.png", Texture.class));
        Sprite background31 = new Sprite( assetManager.get( "BG_C1.png", Texture.class));
        Sprite background32 = new Sprite( assetManager.get( "BG_C2.png", Texture.class));
        Sprite background33 = new Sprite( assetManager.get( "BG_C3.png", Texture.class));
        Sprite background34 = new Sprite( assetManager.get( "BG_C4.png", Texture.class));
        Sprite background35 = new Sprite( assetManager.get( "BG_C5.png", Texture.class));
        Sprite background41 = new Sprite( assetManager.get( "BG_D1.png", Texture.class));
        Sprite background42 = new Sprite( assetManager.get( "BG_D2.png", Texture.class));
        Sprite background43 = new Sprite( assetManager.get( "BG_D3.png", Texture.class));
        Sprite background44 = new Sprite( assetManager.get( "BG_D4.png", Texture.class));
        Sprite background45 = new Sprite( assetManager.get( "BG_D5.png", Texture.class));
        Sprite background51 = new Sprite( assetManager.get( "BG_E1.png", Texture.class));
        Sprite background52 = new Sprite( assetManager.get( "BG_E2.png", Texture.class));
        Sprite background53 = new Sprite( assetManager.get( "BG_E3.png", Texture.class));
        Sprite background54 = new Sprite( assetManager.get( "BG_E4.png", Texture.class));
        Sprite background55 = new Sprite( assetManager.get( "BG_E5.png", Texture.class));
        Sprite background61 = new Sprite( assetManager.get( "BG_F1.png", Texture.class));
        Sprite background62 = new Sprite( assetManager.get( "BG_F2.png", Texture.class));
        Sprite background63 = new Sprite( assetManager.get( "BG_F3.png", Texture.class));
        Sprite background64 = new Sprite( assetManager.get( "BG_F4.png", Texture.class));
        Sprite background65 = new Sprite( assetManager.get( "BG_F5.png", Texture.class));
        Sprite background71 = new Sprite( assetManager.get( "BG_G1.png", Texture.class));
        Sprite background72 = new Sprite( assetManager.get( "BG_G2.png", Texture.class));
        Sprite background73 = new Sprite( assetManager.get( "BG_G3.png", Texture.class));
        Sprite background74 = new Sprite( assetManager.get( "BG_G4.png", Texture.class));
        Sprite background75 = new Sprite( assetManager.get( "BG_G5.png", Texture.class));


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
        Body enemyBody;
        BodyDef smallEnemySensorDef = new BodyDef();
        CircleShape circleShape = new CircleShape();
        Random randomX = new Random();
        Random randomY = new Random();
        for ( int i = 0; i < 50; i++){
            int posX = randomX.nextInt(6400);
            int posY = randomY.nextInt(5600);
            if ( posX > 3840){
                posX = posX - 6400;
            }
            if ( posY > 3200){
                posY  = posY - 5600;
            }
            //create the enemy
            smallEnemyDef.position.set( new Vector2( posX / PIXELS_PER_METER, posY / PIXELS_PER_METER));
            smallEnemyDef.type = BodyDef.BodyType.DynamicBody;
            enemyBody = world.createBody( smallEnemyDef);

            circleShape.setRadius( 20f / PIXELS_PER_METER);
            FixtureDef smallEnemyFix = new FixtureDef();
            smallEnemyFix.shape = circleShape;
            smallEnemyFix.restitution = 0.5f;
            smallEnemyFix.density = 0.1f;
            enemyBody.createFixture(smallEnemyFix);
            Entity entity = new Entity( this.assetManager, enemyBody, "smallEnemy.atlas", "smallEnemy",0,0);
            enemyBody.setUserData( entity);
            enemyBody.setLinearDamping( 0.2f);
            entities.add( entity);
            smallEnemies.add(enemyBody);

            //create its sensor
            smallEnemySensorDef.position.set( new Vector2( posX / PIXELS_PER_METER, posY / PIXELS_PER_METER));
            smallEnemySensorDef.type = BodyDef.BodyType.DynamicBody;
            Body sensorBody = world.createBody( smallEnemySensorDef);
            circleShape.setRadius( 200f / PIXELS_PER_METER);
            FixtureDef sensorFix = new FixtureDef();
            sensorFix.isSensor = true;
            sensorFix.shape = circleShape;
            sensorBody.createFixture( sensorFix);
            sensorBody.setUserData( new SensorData( enemyBody));
            sensors.add(sensorBody);

            //create a joint so if they move, they move together
            WeldJointDef weldJointDef = new WeldJointDef();
            weldJointDef.initialize( enemyBody, sensorBody, enemyBody.getPosition());
            world.createJoint( weldJointDef);
        }

        circleShape.dispose();

    }

    private void createEvilEnemy() {
        BodyDef evilBodyDef = new BodyDef();
        evilBodyDef.position.set( new Vector2( 1000/PIXELS_PER_METER, 400/PIXELS_PER_METER));
        evilBodyDef.type = BodyDef.BodyType.DynamicBody;
        evilBody = world.createBody( evilBodyDef);
        evilBody.setLinearVelocity( -75f / PIXELS_PER_METER, 0);
        CircleShape circleShape = new CircleShape();
        circleShape.setRadius( 80f / PIXELS_PER_METER);
        FixtureDef evilFixture = new FixtureDef();
        evilFixture.shape = circleShape;
        evilFixture.restitution = 0.5f;
        evilFixture.density = 5f;
        evilBody.createFixture(evilFixture);
        entities.add( new Entity(this.assetManager,evilBody, "evilEnemy.atlas", "evilBody",0,0));

        evilBodySensor = world.createBody( evilBodyDef);
        circleShape.setRadius( 480f/PIXELS_PER_METER);
        FixtureDef evilSensorFix = new FixtureDef();
        evilSensorFix.shape = circleShape;
        evilSensorFix.isSensor = true;
        evilBodySensor.createFixture(evilSensorFix);

        WeldJointDef sensorJoint = new WeldJointDef();
        sensorJoint.initialize( evilBody, evilBodySensor, evilBody.getPosition());
        world.createJoint( sensorJoint);

        circleShape.dispose();

        BodyDef evilBodyMouthDef = new BodyDef();
        evilBodyMouthDef.position.set( new Vector2( 959.9f/PIXELS_PER_METER, 400/PIXELS_PER_METER));
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

        WeldJointDef weldJointDef = new WeldJointDef();
        weldJointDef.initialize(evilBody, evilBodyMouth, evilBody.getPosition());
        world.createJoint( weldJointDef);

        //create tentacles
        BodyDef tentacleDef = new BodyDef();
        tentacleDef.type = BodyDef.BodyType.DynamicBody;
        tentacleDef.position.set( evilBody.getPosition().x - 80f/PIXELS_PER_METER, evilBody.getPosition().y + 80f/PIXELS_PER_METER);
        Body tentacleBody = world.createBody( tentacleDef);
        polygonShape.set( new Vector2[]{new Vector2( -10f/PIXELS_PER_METER, -30f/PIXELS_PER_METER),
                                        new Vector2( -10f/PIXELS_PER_METER, 30f/PIXELS_PER_METER),
                                        new Vector2( 10f/PIXELS_PER_METER, 30f/PIXELS_PER_METER),
                                        new Vector2( 10f/PIXELS_PER_METER, -30f/PIXELS_PER_METER)});
        FixtureDef tentacleFix = new FixtureDef();
        tentacleFix.shape = polygonShape;
        tentacleFix.restitution = 0f;
        tentacleFix.density = 0.1f;
        tentacleBody.createFixture( tentacleFix);
        entities.add( new Entity(this.assetManager, tentacleBody, "evilEnemy.atlas", "evil4",0,0));

        WeldJointDef revoluteJointDef = new WeldJointDef();
        revoluteJointDef.initialize( evilBody, tentacleBody, evilBody.getPosition());
        world.createJoint( revoluteJointDef);
        //2
        tentacleDef.position.set( tentacleBody.getPosition().add( 60f/PIXELS_PER_METER, 0));
        Body tentacleBody2 = world.createBody( tentacleDef);
        tentacleBody2.createFixture( tentacleFix);
        entities.add( new Entity( this.assetManager, tentacleBody2, "evilEnemy.atlas", "evil3",0,0));

        RevoluteJointDef revoluteJointDef2 = new RevoluteJointDef();
        revoluteJointDef2.initialize( tentacleBody, tentacleBody2, tentacleBody2.getPosition());
        revoluteJointDef2.localAnchorA.set(0,  10f/PIXELS_PER_METER + JOINT_LENGTH);
        revoluteJointDef2.localAnchorB.set(0,  -10f/PIXELS_PER_METER - JOINT_LENGTH);
//        revoluteJointDef2.localAnchorA.set( Vector2.Zero);
//        revoluteJointDef2.localAnchorB.set( Vector2.Zero);
        world.createJoint( revoluteJointDef2);

        //3
        tentacleDef.position.set( tentacleBody2.getPosition().add( 50f/PIXELS_PER_METER,0));
        Body tentacleBody3 = world.createBody( tentacleDef);
        polygonShape.set( new Vector2[]{new Vector2( -7.5f/PIXELS_PER_METER, -20f/PIXELS_PER_METER),
                                        new Vector2( -7.5f/PIXELS_PER_METER, 20f/PIXELS_PER_METER),
                                        new Vector2( 7.5f/PIXELS_PER_METER, 20f/PIXELS_PER_METER),
                                        new Vector2( 7.5f/PIXELS_PER_METER, -20f/PIXELS_PER_METER)});
        tentacleFix.shape = polygonShape;
        tentacleBody3.createFixture( tentacleFix);
        entities.add( new Entity( this.assetManager, tentacleBody3, "evilEnemy.atlas", "evil6",0,0));

        RevoluteJointDef revoluteJointDef3 = new RevoluteJointDef();
        revoluteJointDef3.initialize( tentacleBody2, tentacleBody3, tentacleBody3.getPosition());
//        revoluteJointDef3.localAnchorA.set( -30f/PIXELS_PER_METER, 0);
//        revoluteJointDef3.localAnchorB.set( 20f/PIXELS_PER_METER,0);
        revoluteJointDef3.localAnchorA.set( Vector2.Zero);
        revoluteJointDef3.localAnchorB.set( Vector2.Zero);
        world.createJoint( revoluteJointDef3);

        //4
        tentacleDef.position.set( tentacleBody3.getPosition().add(40f/PIXELS_PER_METER,0));
        Body tentacleBody4 = world.createBody( tentacleDef);
        tentacleBody4.createFixture( tentacleFix);
        entities.add( new Entity(this.assetManager, tentacleBody4, "evilEnemy.atlas", "evil5",0,0));

        RevoluteJointDef revoluteJointDef4 = new RevoluteJointDef();
        revoluteJointDef4.initialize( tentacleBody3, tentacleBody4, tentacleBody4.getPosition());
//        revoluteJointDef4.localAnchorA.set(-20f/PIXELS_PER_METER, 0);
//        revoluteJointDef4.localAnchorB.set( 20f/PIXELS_PER_METER, 0);
        revoluteJointDef4.localAnchorA.set( Vector2.Zero);
        revoluteJointDef4.localAnchorB.set( Vector2.Zero);
        world.createJoint( revoluteJointDef4);

        polygonShape.dispose();

    }

    private void updateEvilEnemy(){
        Vector2 myPosition = headPart.getPosition();
        Vector2 direction = myPosition.sub( evilBody.getPosition());
        direction.nor();
        evilBody.setLinearVelocity( direction.x * 10f, direction.y * 10f);
        Vector2 velocity = evilBody.getLinearVelocity();

        xFactor = velocity.x;
        yFactor = velocity.y;

        if ( xFactor == 0) {
            angle = yFactor > 0 ? 0 : (float) Math.toRadians(360);
        } else if (yFactor == 0) {
            angle = (float) (xFactor > 0 ? Math.toRadians(180) : 3 * Math.toRadians(180));
        } else {
            angle = (float) (Math.atan(yFactor / xFactor) + Math.toRadians(180));
        }

        if (xFactor > 0) {
            angle += Math.toRadians(180);
        }

        evilBody.setTransform(evilBody.getPosition(), angle);
        evilBodyMouth.setTransform( evilBodyMouth.getPosition(), angle);
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

        RevoluteJointDef revoluteJointDef_1 = new RevoluteJointDef();
        revoluteJointDef_1.initialize(headPart, bodyPart0, bodyPart0.getPosition());
        revoluteJointDef_1.localAnchorA.set(-HEAD_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_1.localAnchorB.set(BODY_LENGTH, 0);
        revoluteJointDef_1.collideConnected = true;
        world.createJoint(revoluteJointDef_1);

        RevoluteJointDef revoluteJointDef_2 = new RevoluteJointDef();
        revoluteJointDef_2.initialize(bodyPart0, bodyPart1, bodyPart1.getPosition());
        revoluteJointDef_2.localAnchorA.set(-BODY_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_2.localAnchorB.set(BODY_LENGTH, 0);
        revoluteJointDef_2.collideConnected = true;
        world.createJoint(revoluteJointDef_2);

        RevoluteJointDef revoluteJointDef_2_1 = new RevoluteJointDef();
        revoluteJointDef_2_1.initialize(bodyPart1, bodyPart2, bodyPart2.getPosition());
        revoluteJointDef_2_1.localAnchorA.set(-BODY_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_2_1.localAnchorB.set(BODY_LENGTH, 0);
        revoluteJointDef_2_1.collideConnected = true;
        world.createJoint(revoluteJointDef_2_1);

        RevoluteJointDef revoluteJointDef_3 = new RevoluteJointDef();
        revoluteJointDef_3.initialize(bodyPart2, bodyPart3, bodyPart3.getPosition());
        revoluteJointDef_3.localAnchorA.set(-BODY_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_3.localAnchorB.set(TAIL1_LENGTH, 0);
        revoluteJointDef_3.collideConnected = true;
        world.createJoint(revoluteJointDef_3);

        RevoluteJointDef revoluteJointDef_4 = new RevoluteJointDef();
        revoluteJointDef_4.initialize(bodyPart3, bodyPart4, bodyPart4.getPosition());
        revoluteJointDef_4.localAnchorA.set(-TAIL1_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_4.localAnchorB.set(TAIL2_LENGTH, 0);
        revoluteJointDef_4.collideConnected = true;
        world.createJoint(revoluteJointDef_4);

        RevoluteJointDef revoluteJointDef_5 = new RevoluteJointDef();
        revoluteJointDef_5.initialize(bodyPart4, tailPart, tailPart.getPosition());
        revoluteJointDef_5.localAnchorA.set(-TAIL2_LENGTH-JOINT_LENGTH,0);
        revoluteJointDef_5.localAnchorB.set(TAIL3_LENGTH, 0);
        revoluteJointDef_5.collideConnected = true;
        world.createJoint(revoluteJointDef_5);

    }

    private void createCreature(boolean isUpped) {
        headPart = addHead( worldWidth/2,worldHeight/2);
        creatureParts.add( headPart);
        bodyPart0 = addBodyPart( headPart.getPosition().x - HEAD_LENGTH - JOINT_LENGTH, worldHeight * 0.5f, BODY_LENGTH, BODY_HEIGHT);
        creatureParts.add( bodyPart0);
        bodyPart1 = addBodyPart( bodyPart0.getPosition().x - BODY_LENGTH - JOINT_LENGTH, worldHeight * 0.5f, BODY_LENGTH, BODY_HEIGHT);
        creatureParts.add( bodyPart1);
        bodyPart2 = addBodyPart( bodyPart1.getPosition().x - BODY_LENGTH - JOINT_LENGTH , worldHeight * 0.5f, BODY_LENGTH,BODY_HEIGHT);
        creatureParts.add( bodyPart2);
        bodyPart3 = addBodyPart( bodyPart2.getPosition().x - BODY_LENGTH - JOINT_LENGTH , worldHeight * 0.5f, TAIL1_LENGTH,TAIL1_HEIGHT);
        creatureParts.add( bodyPart3);
        bodyPart4 = addBodyPart( bodyPart3.getPosition().x - BODY_LENGTH - JOINT_LENGTH , worldHeight * 0.5f, TAIL2_LENGTH,TAIL2_HEIGHT);
        creatureParts.add( bodyPart4);
        tailPart = addTail(bodyPart4.getPosition().x - BODY_LENGTH - JOINT_LENGTH, worldHeight * 0.5f, TAIL3_LENGTH,TAIL3_HEIGHT);
        creatureParts.add( tailPart);

        if(isUpped){
            Entity headEntity = new Entity(this.assetManager, headPart, "upHead.atlas", "head", 80,-4);
            headEntity.setAnimating(false);
            entities.add( headEntity);
            entities.add(new Entity(this.assetManager,bodyPart0, "upBody.atlas", "body0",0,0));
            entities.add(new Entity(this.assetManager,bodyPart1, "upBody.atlas", "body0",0,0));
            entities.add(new Entity(this.assetManager,bodyPart2, "upBody.atlas", "body0",0,0));
            entities.add(new Entity(this.assetManager,bodyPart3, "upBody.atlas", "body1",0,0));
            entities.add(new Entity(this.assetManager,bodyPart4, "upBody.atlas", "body2",0,0));
            entities.add(new Entity(this.assetManager,tailPart, "upBody.atlas", "body3",0,0));

            Body flipUp1 = addFlipUp(bodyPart0, BODY_HEIGHT, BODY_LENGTH);
            entities.add(new Entity(this.assetManager,flipUp1,"upBody.atlas","flipUpBody",0,-27));

            Body flipUp2 = addFlipUp(bodyPart1, BODY_HEIGHT, BODY_LENGTH);
            entities.add(new Entity(this.assetManager,flipUp2,"upBody.atlas","flipUpBody",0,-27));

            Body flipUp3 = addFlipUp(bodyPart2, BODY_HEIGHT, BODY_LENGTH);
            entities.add(new Entity(this.assetManager,flipUp3,"upBody.atlas","flipUpBody",0,-27));

            Body flipUp4 = addFlipUp(bodyPart3, TAIL1_HEIGHT, TAIL1_LENGTH);
            entities.add(new Entity(this.assetManager,flipUp4,"upBody.atlas","flipUpTail1",0,-18));

            Body flipUp5 = addFlipUp(bodyPart4, TAIL2_HEIGHT, TAIL2_LENGTH);
            entities.add(new Entity(this.assetManager,flipUp5,"upBody.atlas","flipUpTail2",0,-9));

            Body flipUp6 = addFlipUp(tailPart, TAIL3_HEIGHT, TAIL3_LENGTH);
            entities.add(new Entity(this.assetManager,flipUp6,"upBody.atlas","flipUpTail3",0,0));


            Body flipDown1 = addFlipDown(bodyPart0, BODY_HEIGHT, BODY_LENGTH);
            entities.add(new Entity(this.assetManager,flipDown1,"upBody.atlas","flipDownBody",0,27));

            Body flipDown2 = addFlipDown(bodyPart1, BODY_HEIGHT, BODY_LENGTH);
            entities.add(new Entity(this.assetManager,flipDown2,"upBody.atlas","flipDownBody",0,27));

            Body flipDown3 = addFlipDown(bodyPart2, BODY_HEIGHT, BODY_LENGTH);
            entities.add(new Entity(this.assetManager,flipDown3,"upBody.atlas","flipDownBody",0,27));

            Body flipDown4 = addFlipDown(bodyPart3, TAIL1_HEIGHT, TAIL1_LENGTH);
            entities.add(new Entity(this.assetManager,flipDown4,"upBody.atlas","flipDownTail1",0,18));

            Body flipDown5 = addFlipDown(bodyPart4, TAIL2_HEIGHT, TAIL2_LENGTH);
            entities.add(new Entity(this.assetManager,flipDown5,"upBody.atlas","flipDownTail2",0,9));

            Body flipDown6 = addFlipDown(tailPart, TAIL3_HEIGHT, TAIL3_LENGTH);
            entities.add(new Entity(this.assetManager,flipDown6,"upBody.atlas","flipDownTail3",0,0));




        } else {
            Entity headEntity = new Entity(this.assetManager, headPart, "head.atlas", "head", 80,-4);
            headEntity.setAnimating(false);
            entities.add( headEntity);
            entities.add(new Entity(this.assetManager,bodyPart0, "body.atlas", "body0",0,0));
            entities.add(new Entity(this.assetManager,bodyPart1, "body.atlas", "body0",0,0));
            entities.add(new Entity(this.assetManager,bodyPart2, "body.atlas", "body0",0,0));
            entities.add(new Entity(this.assetManager,bodyPart3, "body.atlas", "body1",0,0));
            entities.add(new Entity(this.assetManager,bodyPart4, "body.atlas", "body2",0,0));
            entities.add(new Entity(this.assetManager,tailPart, "body.atlas", "body3", 0, 0));
        }

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
//                touchDragX = i;
//                touchDragY = i2;
//
//                headPart.setLinearDamping(0);
//                bodyPart1.setLinearDamping(0);
//                bodyPart2.setLinearDamping(0);

                return false;
            }

            @Override
            public boolean touchUp(int i, int i2, int i3, int i4) {
                if ( isGameOver){
                    StartScreen startScreen = new StartScreen( assetManager, game);
                    startScreen.initialize();
                    game.setScreen( startScreen);
                }

//                headPart.setLinearDamping(1f);
//                bodyPart1.setLinearDamping(1f);
//                bodyPart2.setLinearDamping(1f);

                return false;
            }

            @Override
            public boolean touchDragged(int i, int i2, int i3) {
//                deltaX = i - touchDragX;
//                deltaY = touchDragY - i2;
//                float xForce = forceFactor * deltaX;
//                float yForce = forceFactor * deltaY;
//                headPart.applyForceToCenter(xForce, yForce, true);
//
//                touchDragX = i;
//                touchDragY = i2;
//
//                setHeadAngle();
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


    float xFactor = 0;
    float yFactor = 0;
    float angle = 0;

    private void setHeadAngle() {
        Vector2 velocity = headPart.getLinearVelocity();

        xFactor = velocity.x;
        yFactor = velocity.y;

        if ( xFactor== 0) {
            angle = yFactor > 0 ? 0 : (float) Math.toRadians(360);
        } else if (yFactor == 0) {
            angle = (float) (xFactor > 0 ? Math.toRadians(180) : 3 * Math.toRadians(180));
        } else {
            angle = (float) (Math.atan(yFactor / xFactor) + Math.toRadians(180));
        }

        if (xFactor > 0) {
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
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.00001f;
        fixtureDef.restitution = 0.3f;
        body.createFixture(fixtureDef);
        body.setFixedRotation(true);
        return body;
    }

    private Body addBodyPart(float x, float y, float bodyLength, float bodyHeight){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(bodyLength,-bodyHeight),new Vector2(bodyLength,bodyHeight),new Vector2(-bodyLength,-bodyHeight),new Vector2(-bodyLength,bodyHeight)});
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.1f;
        fixtureDef.friction = 0.00001f;
        fixtureDef.restitution = 0.3f;
        body.createFixture(fixtureDef);
        body.setAngularDamping(55);
        return body;
    }

    private Body addFlipUp(Body body, float bodyHeight,float bodyLength){
        BodyDef flipDef1 = new BodyDef();
        flipDef1.type = BodyDef.BodyType.DynamicBody;
        flipDef1.position.set(body.getPosition().x+bodyHeight,body.getPosition().y);
        Body flipBody1 = world.createBody(flipDef1);
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(bodyLength*0.2f,-bodyHeight*0.3f),new Vector2(bodyLength*0.2f,bodyHeight*0.3f),new Vector2(-bodyLength*0.2f,-bodyHeight*0.3f),new Vector2(-bodyLength*0.2f,bodyHeight*0.3f)});
        FixtureDef flipFix1 = new FixtureDef();
        flipFix1.shape = shape;
        flipFix1.density = 0.1f;
        flipFix1.friction = 0.00001f;
        flipFix1.restitution = 0.3f;
        flipBody1.createFixture(flipFix1);
        flipBody1.setAngularDamping(55);

        RevoluteJointDef flipJoint1 = new RevoluteJointDef();
        flipJoint1.initialize(body, flipBody1, flipBody1.getPosition());
        flipJoint1.localAnchorA.set(0, bodyHeight+JOINT_LENGTH*1.3f);
        flipJoint1.localAnchorB.set(0, -bodyHeight*0.3f-JOINT_LENGTH*1.3f);
        flipJoint1.collideConnected = true;
        world.createJoint(flipJoint1);

        return flipBody1;
    }

    private Body addFlipDown(Body body, float bodyHeight,float bodyLength){
        BodyDef flipDef1 = new BodyDef();
        flipDef1.type = BodyDef.BodyType.DynamicBody;
        flipDef1.position.set(body.getPosition().x-bodyHeight,body.getPosition().y);
        Body flipBody1 = world.createBody(flipDef1);
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(bodyLength*0.2f,-bodyHeight*0.3f),new Vector2(bodyLength*0.2f,bodyHeight*0.3f),new Vector2(-bodyLength*0.2f,-bodyHeight*0.3f),new Vector2(-bodyLength*0.2f,bodyHeight*0.3f)});
        FixtureDef flipFix1 = new FixtureDef();
        flipFix1.shape = shape;
        flipFix1.density = 0.1f;
        flipFix1.friction = 0.00001f;
        flipFix1.restitution = 0.3f;
        flipBody1.createFixture(flipFix1);
        flipBody1.setAngularDamping(55);

        RevoluteJointDef flipJoint1 = new RevoluteJointDef();
        flipJoint1.initialize(body, flipBody1, flipBody1.getPosition());
        flipJoint1.localAnchorA.set(0, -bodyHeight-JOINT_LENGTH*1.3f);
        flipJoint1.localAnchorB.set(0, +bodyHeight*0.3f+JOINT_LENGTH*1.3f);
        flipJoint1.collideConnected = true;
        world.createJoint(flipJoint1);

        return flipBody1;
    }


    private Body addTail(float x, float y, float bodyLength, float bodyHeight){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x,y);
        Body body = world.createBody(bodyDef);
        CircleShape dynamicCircle = new CircleShape();
        dynamicCircle.setRadius(BODY_PART_RADIUS /PIXELS_PER_METER);
        PolygonShape shape = new PolygonShape();
        shape.set(new Vector2[]{new Vector2(bodyLength,-bodyHeight),new Vector2(bodyLength,bodyHeight),new Vector2(-bodyLength,-bodyHeight),new Vector2(-bodyLength,bodyHeight)});
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.01f;
        fixtureDef.friction = 0.00001f;
        fixtureDef.restitution = 0.3f;
        body.createFixture(fixtureDef);
        body.setAngularDamping(55);
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
//        if (isAccelerometerAvailable) {
//            float x = Gdx.input.getAccelerometerX();
//            float y = Gdx.input.getAccelerometerY();
//            headPart.applyForceToCenter(y * forceFactor, -1f * x * forceFactor, true);
//        }
        if ( eatCount > 2 && !isLevelUp){
            levelComplete = true;
        }
        if ( !isGameOver || !levelComplete){
            handleTouchpadMove();

            world.step(WORLD_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

            for (Entity entity : entities) {
                entity.update();
            }
            handleToBeDestructed();
            for ( Body smallEnemy: smallEnemies){
                if ( smallEnemy.getLinearVelocity().x < 0.2f && smallEnemy.getLinearVelocity().y < 0.2f){
                    ((Entity)smallEnemy.getUserData()).setAnimating(false);
                }
            }
            updateEvilEnemy();
        }
        moveCamera();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        stageBatch.setProjectionMatrix( stageCamera.combined);
        Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

        batch.begin();

        for (Sprite sprite : backgrounds) {
            sprite.draw(batch);
        }
        batch.end();
        if ( isGameOver){
            stageBatch.begin();
            gameOverSprite.setPosition( stage.getWidth()/2 - gameOverSprite.getWidth()/2, stage.getHeight()/2 - gameOverSprite.getHeight()/2);
            gameOverSprite.draw( stageBatch);
            stageBatch.end();
            return;
        }else if (levelComplete){
            stageBatch.begin();
            levelCompleteSprite.setPosition( stage.getWidth()/2 - levelCompleteSprite.getWidth()/2, stage.getHeight()/2 - levelCompleteSprite.getHeight()/2);
            levelCompleteSprite.draw( stageBatch);
            stageBatch.end();
            return;
        }
//		debugRenderer.render(world, camera.combined.scale(PIXELS_PER_METER, PIXELS_PER_METER, PIXELS_PER_METER));
        for(int i = entities.size()-1; i>=0; i--){
            entities.get(i).render( batch, v);
        }

        stage.act( v);
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
        if ( stage != null){
            stage.setViewport( width, height, false);
        }
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
        Body toBeRemovedBody = null;
        Body toBeRemovedSensor = null;
        if ( bodyA.equals( headPart) && smallEnemies.contains( bodyB)){
            ((Entity)headPart.getUserData()).setAnimating(false);
            for ( Body sensorBody: sensors){
                if ( ((SensorData)sensorBody.getUserData()).ownerBody.equals( bodyB)){
                    toBeRemovedSensor = sensorBody;
                }
            }
            toBeRemovedBody = bodyB;
            eatCount++;
        } else if ( bodyB.equals( headPart) && smallEnemies.contains( bodyA)){
            ((Entity)headPart.getUserData()).setAnimating(false);
            for ( Body sensorBody: sensors){
                if ( ((SensorData)sensorBody.getUserData()).ownerBody.equals( bodyA)){
                    toBeRemovedSensor = sensorBody;
                }
            }
            toBeRemovedBody = bodyA;
            eatCount++;
        }else if ( bodyA.equals( headPart) && sensors.contains( bodyB)){
            ((SensorData)bodyB.getUserData()).ownerBody.setLinearVelocity(new Vector2(headPart.getLinearVelocity().x * 0.9f, headPart.getLinearVelocity().y * 0.9f));
            ((Entity)((SensorData)bodyB.getUserData()).ownerBody.getUserData()).setAnimating(true);
            ((Entity)headPart.getUserData()).setAnimating(true);
        }else if ( bodyB.equals( headPart) && sensors.contains( bodyA)){
            ((SensorData)bodyA.getUserData()).ownerBody.setLinearVelocity(new Vector2(headPart.getLinearVelocity().x * 0.9f, headPart.getLinearVelocity().y * 0.9f));
            ((Entity)headPart.getUserData()).setAnimating(true);
        }else if ( creatureParts.contains( bodyA) && bodyB.equals( evilBodySensor)){
            ((Entity)evilBody.getUserData()).setAnimating(true);
        }else if ( creatureParts.contains( bodyB) && bodyA.equals( evilBodySensor)){
            ((Entity)evilBody.getUserData()).setAnimating(true);
        }else if ( creatureParts.contains( bodyA) && bodyB.equals( evilBodyMouth)){
            isGameOver = true;
        }else if ( creatureParts.contains(bodyB) && bodyA.equals( evilBodyMouth)){
            isGameOver = true;
        }else if ( bodyA.equals( headPart) && bodyB.equals( evilBody)){
            destroyEvilEnemy();
        }else if ( bodyB.equals( headPart) && bodyA.equals( evilBody)){
            destroyEvilEnemy();
        }

        if ( toBeRemovedBody != null){
            smallEnemies.remove( toBeRemovedBody);
            toBeDestructed.add( toBeRemovedBody);
            entities.remove(toBeRemovedBody.getUserData());
        }
        if ( toBeRemovedSensor != null){
            sensors.remove( toBeRemovedSensor);
            toBeDestructed.add( toBeRemovedSensor);
        }
    }

    private void destroyEvilEnemy() {
        toBeDestructed.add( evilBody);
        entities.remove( evilBody.getUserData());
        toBeDestructed.add( evilBodyMouth);
        toBeDestructed.add( evilBodySensor);
    }

    private void handleToBeDestructed(){
        List<Body> temp = new ArrayList<Body>( toBeDestructed);
        for( Body aBody: temp){
            world.destroyBody(aBody);
            toBeDestructed.remove(aBody);
        }
    }

    @Override
    public void endContact(Contact contact) {
        if ( contact.getFixtureA() == null || contact.getFixtureB() == null){
            return;
        }
        Body bodyA = contact.getFixtureA().getBody();
        Body bodyB = contact.getFixtureB().getBody();
        if ( bodyA.equals( headPart) && sensors.contains( bodyB)){
            ((Entity)headPart.getUserData()).setAnimating(false);
        }else if ( bodyB.equals( headPart) && sensors.contains( bodyA)){
            ((Entity)headPart.getUserData()).setAnimating(false);
        }else if ( creatureParts.contains( bodyA) && bodyB.equals( evilBodySensor)){
            ((Entity)evilBody.getUserData()).setAnimating(false);
        }else if ( creatureParts.contains( bodyB) && bodyA.equals( evilBodySensor)){
            ((Entity)evilBody.getUserData()).setAnimating(false);
        }

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
