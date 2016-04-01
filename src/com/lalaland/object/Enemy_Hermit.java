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
//		System.out.println(life);

		if (life <= LIFE_THRESHOLD) {
			alive = false;
		} else {
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
		handleObstacleAvoidance();
		// update position vectors
		// check if colliding
		
		position.add(velocity);
		orientation += rotation;

		// handle behavior near window boundary
		if (position.x < 0 + HERMIT_PADDING) {
			position.x = HERMIT_PADDING;
			rotateShapeDirection(br_angle);
			updateVelocityPerOrientation();
		} else if (position.x > parent.width - HERMIT_PADDING) {
			position.x = parent.width - HERMIT_PADDING;
			rotateShapeDirection(br_angle);
			updateVelocityPerOrientation();
		} else if (position.y < 0 + HERMIT_PADDING) {
			position.y = HERMIT_PADDING;
			rotateShapeDirection(br_angle);
			updateVelocityPerOrientation();
		} else if (position.y > parent.height - HERMIT_PADDING) {
			position.y = parent.height - HERMIT_PADDING;
			rotateShapeDirection(br_angle);
			updateVelocityPerOrientation();
		}
	}

	private void handleObstacleAvoidance() {
		PVector future_ray1 = PVector.add(position, PVector.mult(velocity, 0.5f));
		;
		PVector future_ray2 = PVector.add(position, PVector.mult(velocity, 1));
		;
		PVector future_ray3 = PVector.add(position, PVector.mult(velocity, 1.5f));
		;
		PVector future_ray4 = PVector.add(position, PVector.mult(velocity, 2));
		;
		if (environment.onObstacle(future_ray1) || environment.onObstacle(future_ray2)
				|| environment.onObstacle(future_ray3) || environment.onObstacle(future_ray4)) {
			avoidObstacle();
		}
	}

	private void avoidObstacle() {
		float avoidance_orient = (parent.random(1) > 0) ? PConstants.PI : -PConstants.PI;
		rotateShapeDirection(avoidance_orient);
		// if(USE_ACCEL)
		// rotationInProg = true;
		updateVelocityPerOrientation();
	}

	void rotateShapeDirection(float angle) {
		angle = scaleRotationAngle(angle);
		if (!USE_ACCEL)
			TTA = 1;
		angle = angle / TTA;
		orientation += angle;
		group.rotateZ(angle);
	}

	float scaleRotationAngle(float angle) {
		angle = angle % PConstants.TWO_PI;
		if (Math.abs(angle) <= PConstants.PI)
			return angle;
		if (angle > PConstants.PI) {
			angle -= PConstants.TWO_PI;
		} else if (angle < -PConstants.PI) {
			angle += PConstants.TWO_PI;
		}
		return angle;
	}

	private void updateVelocityPerOrientation() {
		velocity.x = MAX_VELOCITY * PApplet.cos(orientation);
		velocity.y = MAX_VELOCITY * PApplet.sin(orientation);
	}

	private float RandomBinomial() {
		return parent.random(0, 1) - parent.random(0, 1);
	}

}
