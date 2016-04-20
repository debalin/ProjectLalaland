package com.lalaland.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.Alignment;
import com.lalaland.steering.Cohesion;
import com.lalaland.steering.LookWhereYoureGoing;
import com.lalaland.steering.ObstacleSteering;
import com.lalaland.steering.Seek;
import com.lalaland.steering.Separation;
import com.lalaland.steering.SteeringOutput;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_FlockerFollower extends Enemy {

	private static final float FOLLOWER_RADIUS = 5f;
	private static final PVector FOLLOWER_COLOR = new PVector(200, 180, 200);
	private static final float ALIGNMENT_THRESHOLD = 50f;
	private static final float MAX_KILL_VELOCITY = 2f;
	private static final float SURROUND_CIRCLE_RADIUS = 50f;

	private enum States {
		STAY_WITH_LEADER, SURROUND_PLAYER, CONVERGE_ON_PLAYER, KILL_PLAYER
	}

	private States state;
	private Enemy_FlockerLeader leader;
	private int id;
	private static boolean SURROUND_VERSION;
	private boolean surrounded, allSurrounded;

	public Enemy_FlockerFollower(int id, float positionX, float positionY, PApplet parent, Environment environment,
															 Enemy_FlockerLeader leader, boolean surroundVersion) {
		super(positionX - (float)(Math.random()*20 + Math.random()*20), positionY - (float)(Math.random()*20 + Math.random()*20), parent, environment, FOLLOWER_RADIUS, FOLLOWER_COLOR.copy());
		this.leader = leader;
		this.id = id;
		SURROUND_VERSION = surroundVersion;
		SEPARATION_THRESHOLD = 20f;
		POSITION_MATCHING = true;
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 20;
		RADIUS_SATISFACTION = 10;
		MAX_VELOCITY = 0.5f;
		MAX_ACCELERATION = 0.3f;
		targetPosition = new PVector(position.x, position.y);
		lifeReductionRate = 20;
		state = States.STAY_WITH_LEADER;
		surrounded = allSurrounded = false;
		PLAYER_DAMAGE = 0.5f;
		DAMAGE_RADIUS = 10f;
	}

	@Override
	public void move() {
		updateLife();

		switch (state) {
		case STAY_WITH_LEADER:
			targetPosition.set(leader.getPosition());
			if (ObstacleSteering.checkForObstacleAvoidance(this, parent, environment, 5f))
				targetPosition.set(ObstacleSteering.avoidObstacleOnSeek(this, environment, 5f));
			break;
		case SURROUND_PLAYER:
			PVector positionToSeek = PVector.fromAngle(leader.getOrientation() + 2 * PConstants.PI * id / leader.getNumFollowers()).setMag(SURROUND_CIRCLE_RADIUS);
			targetPosition = environment.getPlayer().getPosition().copy().add(positionToSeek);
			if(position.dist(targetPosition) < RADIUS_SATISFACTION) {
				surrounded = true;
				if(allSurrounded)
					state = States.CONVERGE_ON_PLAYER;
			}
			else {
				surrounded = false;
				allSurrounded = false;
			}
			if (ObstacleSteering.checkForObstacleAvoidance(this, parent, environment, 5f))
				targetPosition = ObstacleSteering.avoidObstacleOnSeek(this, environment, 5f);
			break;
		case CONVERGE_ON_PLAYER:
			targetPosition = environment.getPlayer().getPosition().copy();
			if(allSurrounded == false || position.dist(targetPosition) > SURROUND_CIRCLE_RADIUS) {
				surrounded = allSurrounded = false;
				state = States.SURROUND_PLAYER;
			}
			if (ObstacleSteering.checkForObstacleAvoidance(this, parent, environment, 5f))
				targetPosition = ObstacleSteering.avoidObstacleOnSeek(this, environment, 5f);
			break;	
		case KILL_PLAYER:
			targetPosition = environment.getPlayer().getPosition().copy();
			if (ObstacleSteering.checkForObstacleAvoidance(this, parent, environment, 5f))
				targetPosition = ObstacleSteering.avoidObstacleOnSeek(this, environment, 5f);
			break;	
		}

		updatePosition();
	}

	private void updateState(States state) {
		this.state = state;
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
			killYourself(false);
			//printMetrics();
		}
		checkAndReducePlayerLife();
	}

	private void printMetrics() {

	}

	private void updatePosition() {
		position.add(velocity);

		Kinematic target = new Kinematic(targetPosition, null, 0, 0);
		SteeringOutput steering;
		List<Enemy> followers = new ArrayList<>(leader.followers);

		if(state == States.STAY_WITH_LEADER) {
			steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
			List<Kinematic> flockers = new ArrayList<>();
			flockers.addAll(followers);
			flockers.add(leader);
			PVector alignment = Alignment.getSteering(this, flockers, MAX_ACCELERATION, ALIGNMENT_THRESHOLD).linear.mult(0.6f);
			PVector separation = Separation.getSteering(this, flockers, MAX_ACCELERATION, SEPARATION_THRESHOLD).linear.mult(0.8f);
			PVector cohesion = Cohesion.getSteering(this, flockers, MAX_ACCELERATION).linear.mult(0.2f);
			steering.linear.add(alignment);
			steering.linear.add(separation);
			steering.linear.add(cohesion);
			steering.linear.setMag(MAX_ACCELERATION);
			velocity.add(steering.linear);
			if (velocity.mag() >= MAX_VELOCITY)
				velocity.setMag(MAX_VELOCITY);
		}
		else {
			steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
			if (steering.linear.mag() == 0) {
				velocity.set(0, 0);
				acceleration.set(0, 0);
				reached = true;
				return;
			}
			reached = false;
			ArrayList<Kinematic> flockers = new ArrayList<>();
			flockers.addAll(followers);
			flockers.add(leader);
			PVector separation = Separation.getSteering(this, flockers, MAX_ACCELERATION, SEPARATION_THRESHOLD + 10).linear.mult(0.8f);
			steering.linear.add(separation);
			steering.linear.setMag(MAX_ACCELERATION);		
			velocity.add(steering.linear);
			if (velocity.mag() >= MAX_KILL_VELOCITY)
				velocity.setMag(MAX_KILL_VELOCITY);
		}
		
		steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;
		orientation += steering.angular;

		if (DRAW_BREADCRUMBS)
			storeHistory();
	}
	
	public void updateStateToKill() {
		surrounded = allSurrounded = false;
		if(SURROUND_VERSION)
			updateState(States.SURROUND_PLAYER);
		else
			updateState(States.KILL_PLAYER);
	}
	
	public void updateStateToFollow() {
		surrounded = allSurrounded = false;
		updateState(States.STAY_WITH_LEADER);
	}
	
	public void setAllSurrounded(boolean value) {
		allSurrounded = value;
	}
	
	public boolean isSurrounded() {
		return surrounded;
	}
}
