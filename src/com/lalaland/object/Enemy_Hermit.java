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
	private static final boolean SPIRAL_RAGE = true;
	private static final float SPIRAL_WIDTH_CONSTANT = 2f;

	private enum States {
		WANDER, RAGE_MODE
	}
	private States state;
	private Wander wander;
	private static int spawnCount = 0;
	public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;

	public Enemy_Hermit(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, HERMIT_RADIUS, HERMIT_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 1.0f;		
		lifeReductionRate = 4;
		targetPosition = new PVector(position.x, position.y);
		spawnCount++;
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
					updateState(States.RAGE_MODE);
				}
				break;
			case RAGE_MODE:
				updatePositionSeek();
				if (!isPlayerVisible()) {
					rageModeOff();
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
          super.incrementTotalHPDamage((int)lifeReductionRate);
					i.remove();
				}
			}
		}
		if (life <= LIFE_THRESHOLD) {
			alive = false;
			spawnCount--;
		}
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
		if (SPIRAL_RAGE)
			steering.linear.add(PVector.fromAngle(PVector.sub(environment.getPlayer().getPosition(), position).heading() + PConstants.PI / 2).setMag(steering.linear.mag() * SPIRAL_WIDTH_CONSTANT));
		if (ObstacleSteering.checkForObstacleAvoidance(this, parent, environment))
			targetPosition = ObstacleSteering.avoidObstacleOnSeek(this, environment.getPlayer(), environment);
		else
    	velocity.add(steering.linear);
    if (velocity.mag() >= SEEK_MAX_VELOCITY)
      velocity.setMag(SEEK_MAX_VELOCITY);
    steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;

    orientation += steering.angular;
		position.add(velocity);
	}

	private void updatePositionWander() {
		KinematicOutput kinematic = wander.getOrientationMatchingSteering(this, environment, parent, BORDER_PADDING, MAX_VELOCITY);
		orientation += kinematic.rotation;
		velocity.set(kinematic.velocity);
		position.add(velocity);
	}

}
