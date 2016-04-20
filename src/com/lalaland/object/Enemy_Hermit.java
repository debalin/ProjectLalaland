package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.*;
import com.lalaland.utility.Utility;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Hermit extends Enemy {

	private static final float HERMIT_RADIUS = 7;
	private static final PVector HERMIT_COLOR = new PVector(255, 153, 102);
	private static final PVector HERMIT_RAGE_COLOR = new PVector(255, 255, 0);
	private static final float HERMIT_VIEW_RADIUS = 200;
	private static final float MAX_LINEAR_ACC = 0.5f;
	private static final float RADIUS_SATISFACTION = 0.1f;
	private static final float SEEK_MAX_VELOCITY = 2.0f;
	private static final boolean SPIRAL_RAGE = false;
	private static final float SPIRAL_WIDTH_CONSTANT = 2f;

	private enum States {
		WANDER, RAGE_MODE
	}
	private States state;
	private Wander wander;
	private static int spawnCount = 0;
	public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;

	private int timeRageMode, timeRageModeCurrent;
	private float damageTakenRageMode;

	public Enemy_Hermit(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, HERMIT_RADIUS, HERMIT_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 1.0f;
		DAMAGE_RADIUS = 30f;
		PLAYER_DAMAGE = 2.5f;
		lifeReductionRate = 4;
		targetPosition = new PVector(position.x, position.y);
		spawnCount++;
		timeRageMode = 0;
		timeRageModeCurrent = 0;
		damageTakenRageMode = 0;
		wander = new Wander(150);
		state = States.WANDER;
	}

	public static void initializeSpawnDetails(int frameRate) {
		SPAWN_OFFSET = frameRate * 5;
		SPAWN_INTERVAL = frameRate * 20;
		SPAWN_MAX = 3;
	}

	private void updateState(States state) {
		this.state = state;
	}

	public static int getSpawnCount() {
		return spawnCount;
	}

	@Override
	public void move() {
		updateLife();

		switch (state) {
			case WANDER:
				updatePositionWander();
				if (isPlayerVisible()) {
					rageModeOn();
					timeRageModeCurrent = parent.millis();
					updateState(States.RAGE_MODE);
				}
				break;
			case RAGE_MODE:
				updatePositionSeek();
				if (!isPlayerVisible()) {
					rageModeOff();
					timeRageModeCurrent = parent.millis() - timeRageModeCurrent;
					timeRageMode += timeRageModeCurrent;
					updateState(States.WANDER);
				}
				break;
		}
		if (DRAW_BREADCRUMBS)
			storeHistory();
	}

	private void updateLife() {
		List<Bullet> bullets = environment.getPlayer().getBullets();
		synchronized (bullets) {
			Iterator<Bullet> i = bullets.iterator();
			while (i.hasNext()) {
				Bullet bullet = i.next();
				if (environment.inSameGrid(bullet.getPosition(), position)) {
					life -= lifeReductionRate;
					if (state == States.RAGE_MODE)
						damageTakenRageMode += lifeReductionRate;
          super.incrementTotalHPDamage((int)lifeReductionRate);
					i.remove();
				}
			}
		}
		if (life <= LIFE_THRESHOLD) {
			killYourself(true);
			printMetrics();
			spawnCount--;
		}
		checkAndReducePlayerLife();
	}

	private void printMetrics() {
		if (state == States.RAGE_MODE)
			timeRageMode += parent.millis() - timeRageModeCurrent;
		System.out.println("Time spent in rage mode: " + timeRageMode);
		System.out.println("Damage taken in rage mode: " + damageTakenRageMode);
		System.out.println("Efficiency: " + (timeRageMode / damageTakenRageMode));
	}
	
	private void rageModeOn(){
		IND_COLOR = HERMIT_RAGE_COLOR;
		for (int i = 7; i > 0; i--)
			enlarge();
	}
	
	private void rageModeOff() {
		for (int i = 7; i > 0; i--)
			diminish();
		IND_COLOR = HERMIT_COLOR;
	}
	
	private boolean isPlayerVisible() {
		return Utility.calculateEuclideanDistance(environment.getPlayer().position, position) <= HERMIT_VIEW_RADIUS;
	}
	
	private void updatePositionSeek() {
		targetPosition.x = environment.getPlayer().getPosition().x;
    targetPosition.y = environment.getPlayer().getPosition().y;

		position.add(velocity);
		boolean onObstacle = ObstacleSteering.checkForObstacleAvoidance(this, parent, environment, 6f);
		if (onObstacle)
			targetPosition.set(ObstacleSteering.avoidObstacleOnSeek(this, environment, 6f));
    Kinematic target = new Kinematic(targetPosition, null, 0, 0);
    SteeringOutput steering;
    steering = Seek.getSteering(this, target, MAX_LINEAR_ACC, RADIUS_SATISFACTION);
    if (steering.linear.mag() == 0) {
      velocity.set(0, 0);
      acceleration.set(0, 0);
      reached = true;
      return;
    }
    reached = false;
		if (SPIRAL_RAGE && !onObstacle)
			steering.linear.add(PVector.fromAngle(PVector.sub(environment.getPlayer().getPosition(), position).heading() + PConstants.PI / 2).setMag(steering.linear.mag() * SPIRAL_WIDTH_CONSTANT));
		velocity.add(steering.linear);
		if (SPIRAL_RAGE) {
			if (velocity.mag() >= SEEK_MAX_VELOCITY)
				velocity.setMag(SEEK_MAX_VELOCITY);
		}
		else {
			if (velocity.mag() >= 1.1f * SEEK_MAX_VELOCITY)
				velocity.setMag(1.1f * SEEK_MAX_VELOCITY);
		}
    steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;

    orientation += steering.angular;
	}

	private void updatePositionWander() {
		KinematicOutput kinematic = wander.getOrientationMatchingSteering(this, environment, parent, BORDER_PADDING, MAX_VELOCITY, 5f);
		orientation += kinematic.rotation;
		velocity.set(kinematic.velocity);
		position.add(velocity);
	}

}
