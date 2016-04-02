package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.SteeringOutput;
import com.lalaland.steering.Wander;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Hermit extends Enemy {
	private static final float HERMIT_RADIUS = 7;
	private static final float HERMIT_PADDING = HERMIT_RADIUS * 2.0f;
	private static final PVector HERMIT_COLOR = new PVector(102, 255, 51);
	private static final int LIFE_THRESHOLD = 60;
	private static final float HERMIT_VIEW_RADIUS = 30;
	private static final float MAX_LINEAR_ACC = 0.5f;
	private static final float MAX_ANGULAR_ACC = 0.1f;
	private static final float RADIUS_SATISFACTION = 0.1f;

	private int randomiser_counter = 0;
	private int randomiser_limit = 200;
	private float TTA = 50;
	private boolean USE_ACCEL = true;
	private float br_angle = PConstants.PI;

	private boolean rotationInProg = false;
	int rotcounter = 0;
	float rand_orient;

	public Enemy_Hermit(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, HERMIT_RADIUS, HERMIT_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 1.0f;
		lifeReductionRate = 4;
	}

	@Override
	public void move() {

		updateLife();
    if (life <= LIFE_THRESHOLD) {
      alive = false;
    }
    else {
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
