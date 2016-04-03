package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.LookWhereYoureGoing;
import com.lalaland.steering.Seek;
import com.lalaland.steering.SteeringOutput;
import com.lalaland.steering.Wander;
import com.lalaland.utility.Utility;

import processing.core.PApplet;
import processing.core.PVector;

public class Enemy_Hermit extends Enemy {
	private static final float HERMIT_RADIUS = 7;
	private static final PVector HERMIT_COLOR = new PVector(255, 153, 102);
	private static final PVector HERMIT_RAGE_COLOR = new PVector(255, 255, 0);
	private static final float HERMIT_VIEW_RADIUS = 180;
	private static final float MAX_LINEAR_ACC = 0.5f;
	private static final float MAX_ANGULAR_ACC = 0.1f;
	private static final float RADIUS_SATISFACTION = 0.1f;
	private static final float SEEK_MAX_VELOCITY = 1.9f;
	private boolean rageModeEntered = false;
	private float TTA = 50;

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
	}

	public static void initializeSpawnDetails(int frameRate) {
		SPAWN_OFFSET = frameRate * 5;
		SPAWN_INTERVAL = frameRate * 20;
		SPAWN_MAX = 3;
	}

	public static int getSpawnCount() {
		return spawnCount;
	}

	@Override
	public void move() {
		updateLife();
    if(isPlayerVisible()){
    	rageModeOn();
    	seekPlayer();
    }
    else {
    	rageModeOff();
    	wander();      
    }
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
		if(rageModeEntered)
			return;
		IND_COLOR = HERMIT_RAGE_COLOR;
		for(int i=7;i>0;i--)
			enlarge();
		rageModeEntered = true;
	}
	
	private void rageModeOff(){
		if(!rageModeEntered)
			return;
		for(int i=7;i>0;i--)
			diminish();
		IND_COLOR = HERMIT_COLOR;
		rageModeEntered = false;
	}
	
	private boolean isPlayerVisible(){
		return Utility.calculateEuclideanDistance(environment.getPlayer().position, position) <= HERMIT_VIEW_RADIUS;
	}
	
	private void seekPlayer(){
		targetPosition.x = environment.getPlayer().getPosition().x;
    targetPosition.y = environment.getPlayer().getPosition().y;
    position.add(velocity);

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
    velocity.add(steering.linear);
    if (velocity.mag() >= SEEK_MAX_VELOCITY)
      velocity.setMag(SEEK_MAX_VELOCITY);
    steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;
    orientation += steering.angular;

    if (DRAW_BREADCRUMBS)
      storeHistory();
	}

	private void wander() {
		SteeringOutput steering = Wander.getPositionMatchingSteering(this, MAX_LINEAR_ACC, MAX_ANGULAR_ACC, TTA, RADIUS_SATISFACTION);

		velocity.add(steering.linear);
		rotation = steering.angular;
		if (velocity.mag() >= MAX_VELOCITY)
			velocity.setMag(MAX_VELOCITY);
		
		// update position vectors
		// check if colliding
		if(!checkForObstacleAvoidance())
			position.add(velocity);
		else
			avoidObstacleOnWander();
		
		orientation += rotation;

		// handle behavior near window boundary
		avoidBoundary();
	}	

}
