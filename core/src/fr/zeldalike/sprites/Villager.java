package fr.zeldalike.sprites;

//blabla
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

import fr.zeldalike.assets.Constants;
import fr.zeldalike.screens.PlayScreen;

public class Villager extends NonPlayableCharacter {
	// Position variables
	public enum State {
		UP, DOWN, LEFT, RIGHT, STANDUP, STANDDOWN, STANDLEFT, STANDRIGHT, ATTACK
	};

	public State currentState;
	public State previousState;
	// Animation variables
	private Animation walkRight, walkLeft, walkUp, walkDown;
	private Animation standRight, standLeft, standUp, standDown;
	// Other variables
	private float stateTimer;
	private boolean isMoving;

	public Villager(PlayScreen screen, float x, float y) {
		super(screen, x, y);

		// Set our current and previous state initial animation
		this.currentState = State.STANDDOWN;
		this.previousState = State.STANDDOWN;

		this.walkLeft = new Animation(0.2f, this.defineAnimation(0, 6, 50, 20, 60, 70));
		this.walkRight = new Animation(0.2f, this.defineAnimation(7, 13, 50, 20, 60, 70));
		this.walkDown = new Animation(0.2f, this.defineAnimation(14, 20, 50, 20, 60, 70));
		this.walkUp = new Animation(0.2f, this.defineAnimation(21, 27, 50, 20, 60, 70));

		this.standLeft = new Animation(0, new TextureRegion(screen.getAtlas().findRegion("Link"), 200, 20, 60, 70));
		this.standRight = new Animation(0, new TextureRegion(screen.getAtlas().findRegion("Link"), 500, 20, 60, 70));
		this.standDown = new Animation(0, new TextureRegion(screen.getAtlas().findRegion("Link"), 850, 20, 60, 70));
		this.standUp = new Animation(0, new TextureRegion(screen.getAtlas().findRegion("Link"), 1250, 20, 60, 70));

		this.stateTimer = 0;
		this.setBounds(0, 0, 60 / Constants.PPM, 70 / Constants.PPM);
	}

	@Override
	public void isMoving() {
		// Change the constant on false if the player stop moving
		if (this.b2body.getLinearVelocity().x == 0 && this.b2body.getLinearVelocity().y == 0) {
			this.isMoving = false;
		} else {
			this.isMoving = true;
		}
	}

	@Override
	public void update(float dt) {
		this.setPosition(this.b2body.getPosition().x - this.getWidth() / 2,
				this.b2body.getPosition().y - this.getHeight() / 2);
		this.setRegion(this.getFrame(dt));
	}

	@Override
	public TextureRegion getFrame(float dt) {
		TextureRegion region;

		// Set our current position
		this.currentState = this.getState();

		switch (this.currentState) {
		case UP:
			region = this.walkUp.getKeyFrame(this.stateTimer, true);
			break;
		case RIGHT:
			region = this.walkRight.getKeyFrame(this.stateTimer, true);
			break;
		case DOWN:
			region = this.walkDown.getKeyFrame(this.stateTimer, true);
			break;
		case LEFT:
			region = this.walkLeft.getKeyFrame(this.stateTimer, true);
			break;
		case STANDUP:
			region = this.standUp.getKeyFrame(this.stateTimer, true);
			break;
		case STANDRIGHT:
			region = this.standRight.getKeyFrame(this.stateTimer, true);
			break;
		case STANDDOWN:
			region = this.standDown.getKeyFrame(this.stateTimer, true);
			break;
		case STANDLEFT:
			region = this.standLeft.getKeyFrame(this.stateTimer, true);
			break;
		default:
			region = this.standDown.getKeyFrame(this.stateTimer, true);
			break;
		}

		this.stateTimer = this.currentState == this.previousState ? this.stateTimer + dt : 0;
		this.previousState = this.currentState;
		return region;
	}

	@Override
	public State getState() {
		if (this.b2body.getLinearVelocity().y > 0) {
			return State.UP;
		}
		if (this.b2body.getLinearVelocity().y < 0) {
			return State.DOWN;
		}
		if (this.b2body.getLinearVelocity().x > 0) {
			return State.RIGHT;
		}
		if (this.b2body.getLinearVelocity().x < 0) {
			return State.LEFT;
		}

		if (this.isMoving && this.previousState == State.UP) {
			return State.STANDUP;
		}
		if (this.isMoving && this.previousState == State.DOWN) {
			return State.STANDDOWN;
		}
		if (this.isMoving && this.previousState == State.RIGHT) {
			return State.STANDRIGHT;
		}
		if (this.isMoving && this.previousState == State.LEFT) {
			return State.STANDLEFT;
		}

		return this.currentState;
	}

	@Override
	public void movePath() {
		if (this.b2body.getLinearVelocity().x <= 0.1f) {
			this.b2body.applyLinearImpulse(new Vector2(0.3f, 0), this.b2body.getWorldCenter(), true);
		}
	}

	@Override
	public void defineNPC() {
		BodyDef bdef = new BodyDef();
		FixtureDef fdef = new FixtureDef();
		CircleShape shape = new CircleShape();

		// Set the player's initial position and the type of body used
		bdef.position.set(375 / Constants.PPM, 650 / Constants.PPM);
		bdef.type = BodyDef.BodyType.DynamicBody;

		this.b2body = this.world.createBody(bdef);

		// Set the body form, a circle with a radius of 7
		shape.setRadius(7f / Constants.PPM);

		fdef.filter.categoryBits = Constants.NPC_BIT;
		fdef.filter.maskBits = Constants.DEFAULT_BIT | Constants.LINK_BIT | Constants.PLANT_BIT | Constants.NPC_BIT;

		fdef.shape = shape;
		this.b2body.createFixture(fdef);
	}

	public Array<TextureRegion> defineAnimation(int init, int limit, int posX, int posY, int width, int height) {
		Array<TextureRegion> frames = new Array<TextureRegion>();

		for (int i = init; i < limit; i++) {
			frames.add(new TextureRegion(this.screen.getAtlas().findRegion("Link"), i * posX, posY, width, height));
		}

		return frames;
	}
}
