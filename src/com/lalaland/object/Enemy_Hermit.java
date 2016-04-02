package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.LookWhereYoureGoing;
import com.lalaland.steering.Seek;
import com.lalaland.steering.SteeringOutput;
import com.lalaland.steering.Wander;

import processing.core.PApplet;
import processing.core.PVector;

public class Enemy_Hermit extends Enemy {
	private static final float HERMIT_RADIUS = 7;
	private static final PVector HERMIT_COLOR = new PVector(102, 255, 51);
	private static final int LIFE_THRESHOLD = 5;
	private static final float HERMIT_VIEW_RADIUS = 180;
	private static final float MAX_LINEAR_ACC = 0.5f;
	private static final float MAX_ANGULAR_ACC = 0.1f;
	private static final float RADIUS_SATISFACTION = 0.1f;
	private static final float SEEK_MAX_VELOCITY = 1.9f;
	private boolean rageModeEntered = false;
	private PVector savedColor = new PVector();
	
	private float TTA = 50;
	int rotcounter = 0;
	float rand_orient;

	public Enemy_Hermit(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, HERMIT_RADIUS, HERMIT_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 1.0f;		
		lifeReductionRate = 4;
		targetPosition = new PVector(position.x, position.y);
	}

	@Override
	public void move() {
		updateLife();
    if (life <= LIFE_THRESHOLD) {
      alive = false;
    }
    else if(isPlayerVisible()){
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
					IND_COLOR.x = (IND_COLOR.x >= 255) ? 255 : IND_COLOR.x + 15;
					IND_COLOR.y = (IND_COLOR.y >= 255) ? 255 : IND_COLOR.y - 15;
					IND_COLOR.z = (IND_COLOR.z >= 255) ? 255 : IND_COLOR.z - 15;
					i.remove();
				}
			}
		}
	}
	
	private void rageModeOn(){
		if(rageModeEntered)
			return;
		savedColor = PVector.mult(IND_COLOR, 1);
		IND_COLOR.x = 255;
		IND_COLOR.y = 255;
		IND_COLOR.z = 0;
		for(int i=7;i>0;i--)
			enlarge();
		rageModeEntered = true;
	}
	
	private void rageModeOff(){
		if(!rageModeEntered)
			return;
		for(int i=7;i>0;i--)
			diminish();
		IND_COLOR = savedColor;
		rageModeEntered = false;
	}
	
	private boolean isPlayerVisible(){
		return PVector.sub(environment.getPlayer().position, position).mag() <= HERMIT_VIEW_RADIUS;
	}
	
	private void seekPlayer(){
		targetPosition.x = environment.getPlayer().getPosition().x;
    targetPosition.y = environment.getPlayer().getPosition().y;
    position.add(velocity);

    Kinematic target = new Kinematic(targetPosition, null, 0, 0);
    SteeringOutput steering = new SteeringOutput();
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
		boolean onObstacle = handleObstacleAvoidance();
		if(!onObstacle)
			position.add(velocity);
		
		orientation += rotation;

		// handle behavior near window boundary
		avoidBoundary();
	}	

}
