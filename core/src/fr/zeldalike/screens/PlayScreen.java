package fr.zeldalike.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

import fr.zeldalike.assets.Constants;
import fr.zeldalike.game.Main;
import fr.zeldalike.scenes.Hud;
import fr.zeldalike.sprites.Avatar;
import fr.zeldalike.sprites.Villager;
import fr.zeldalike.tools.B2WorldCreator;
import fr.zeldalike.tools.MusicLoader;
import fr.zeldalike.tools.WorldContactListener;

public class PlayScreen implements Screen {
	// **************************************************
	// Fields
	// **************************************************
	private Main game;
	// HUD variables
	private Hud hud;
	// Camera variables
	private Camera mainCam;
	// Map variables
	private Map mainMap;
	// Box2D variables
	private World world;
	private Box2DDebugRenderer b2dr;
	// Player variables
	private TextureAtlas atlas;
	private Avatar player;
	// NPC's variables
	private Villager runningVillager;
	// Music variables
	private Music music;

	// **************************************************
	// Constructors
	// **************************************************
	public PlayScreen(Main game) {
		this.game = game;
		mainCam = new Camera();
		mainMap = new Map("Village");
		hud = new Hud(game.batch);

		// Create our Box2D world, setting no gravity and allow bodies to sleep
		world = new World(new Vector2(0, 0), true);

		// Allows for debug lines of our Box2D world
		b2dr = new Box2DDebugRenderer();

		new B2WorldCreator(world, mainMap.getMap());

		// Create the avatar in our game world
		atlas = new TextureAtlas("Sprites/Link.pack");
		player = new Avatar(world, this);

		world.setContactListener(new WorldContactListener());

		// Launch our main theme music, set on looping and is volume
		music = MusicLoader.manager.get("Audio/Music/ALTTP_Kakariko_Village.ogg", Music.class);
		music.setLooping(true);
		music.setVolume(10/Constants.PPM);
		music.play();

		//
		runningVillager = new Villager(this, 310, 700);

		// Set the layers
		mainMap.setLayers();
	}

	// **************************************************
	// Getters
	// **************************************************
	public TextureAtlas getAtlas() {
		return atlas;
	}

	public World getWorld() {
		return world;
	}
	
	// **************************************************
	// Setters
	// **************************************************
	
	// **************************************************
	// Public Methods
	// **************************************************
	@Override
	public void render(float delta) {
		update(delta);

		if(Gdx.input.isKeyJustPressed(Input.Keys.V)) {
			//this.mainMap.getMap().dispose();
			mainMap.setMap("donjonTest");
		}

		// Clear the game screen with black
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// Render our back plan layers
		mainMap.renderLayers(mainMap.getBackPlan());

		// Render our Box2DDebugLines
		b2dr.render(world, mainCam.getGameCam().combined);

		// Render our player
		game.batch.setProjectionMatrix(mainCam.getGameCam().combined);
		game.batch.begin();
		player.draw(game.batch);
		runningVillager.draw(game.batch);
		game.batch.end();

		// Render our first plan layers
		mainMap.renderLayers(mainMap.getFirstPlan());

		// Set our batch to now draw the HUD camera sees
		game.batch.setProjectionMatrix(hud.getStage().getCamera().combined);
		hud.getStage().draw();
	}
	
	public void update(float dt) {
		// Handle user input first
		player.handleInput(dt);

		// Takes 1 step in the physics simulation (60 times per second)
		world.step(1/60f, 6, 2);

		player.update(dt);
		runningVillager.update(dt);
		player.setMoving();
		runningVillager.setMoving();
		
		runningVillager.movePathSquare(4.6f, 3.1f, 4.6f, 3.1f);

		// Attach our gameCam to our player's coordinates
		mainCam.setPosition(player.b2body.getPosition().x, player.b2body.getPosition().y);
		mainCam.update();

		// Tell our renderer to draw only what our camera can see in our game world
		mainMap.setView(mainCam.getGameCam());


		if(Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			hud.damage(1);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
			hud.cure(1);
		}
		if(Gdx.input.isKeyJustPressed(Input.Keys.B)) {
			hud.setDialogBoxVisibility();
		}
	}

	@Override
	public void resize(int width, int height) {
		// Update our game viewport
		mainCam.resize(width, height);
	}
	
	@Override
	public void show() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void hide() {}

	@Override
	public void dispose() {
		mainMap.dispose();
		world.dispose();
		hud.dispose();
	}
}