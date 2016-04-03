package com.lalaland.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.Alignment;
import com.lalaland.steering.Cohesion;
import com.lalaland.steering.LookWhereYoureGoing;
import com.lalaland.steering.Seek;
import com.lalaland.steering.Separation;
import com.lalaland.steering.SteeringOutput;

import processing.core.PApplet;
import processing.core.PVector;

public class Enemy_Flocker_Follower extends Enemy {

	private static final float FOLLOWER_RADIUS = 5f;
	private static final PVector FOLLOWER_COLOR = new PVector(200, 180, 200);
	private static final float ALIGNMENT_THRESHOLD = 50f;
	private static final float SEPARATION_THRESHOLD = 20f;
	private static final float MAX_KILL_VELOCITY = 2f;

	private enum States {
		STAY_WITH_LEADER, KILL_PLAYER
	}

	private States state;
	private Enemy_Flocker_Leader leader;

	public Enemy_Flocker_Follower(float positionX, float positionY, PApplet parent, Environment environment,
			Enemy_Flocker_Leader leader) {
		super(positionX - (float)(Math.random()*20 + Math.random()*20), positionY - (float)(Math.random()*20 + Math.random()*20), parent, environment, FOLLOWER_RADIUS, FOLLOWER_COLOR.copy());
		this.leader = leader;
		POSITION_MATCHING = true;
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 20;
		RADIUS_SATISFACTION = 10;
		MAX_VELOCITY = 0.5f;
		MAX_ACCELERATION = 0.3f;
		targetPosition = new PVector(position.x, position.y);
		lifeReductionRate = 20;
		state = States.STAY_WITH_LEADER;
	}

	@Override
	public void move() {
		updateLife();

		switch (state) {
		case STAY_WITH_LEADER:
			targetPosition = leader.getPosition().copy();
			// if (checkForObstacleAvoidance())
			// updateState(States.PATH_FIND_LEADER);
			break;
		// case PATH_FIND_PLAYER:
		// findPlayer();
		// break;
		// case PATH_FOLLOW_PLAYER:
		// followPathForSometime();
		// break;
		case KILL_PLAYER:
			targetPosition = environment.getPlayer().getPosition().copy();
			break;
		}

		updatePosition();
	}

	// private void findPlayer() {
	// PVector pointToFleeTo = targetPosition.copy();
	// pathFind(pointToFleeTo);
	// updateState(States.PATH_FOLLOW_PLAYER);
	// }
	//
	// private void followPathForSometime() {
	// if (solutionPath != null && solutionPath.size() != 0 && (reached ||
	// !startTakingCover) && followedNodes <= MAX_FOLLOW_NODE_COUNT) {
	// int node = solutionPath.poll();
	// int gridY = (int) (node / environment.getNumTiles().x);
	// int gridX = (int) (node % environment.getNumTiles().x);
	// targetPosition.x = gridX * environment.getTileSize().x +
	// environment.getTileSize().x / 2;
	// targetPosition.y = gridY * environment.getTileSize().y +
	// environment.getTileSize().y / 2;
	// startTakingCover = true;
	// followedNodes++;
	// }
	// else if (solutionPath == null || solutionPath.size() == 0 || followedNodes
	// > MAX_FOLLOW_NODE_COUNT) {
	// updateState(States.SEEK);
	// startTakingCover = false;
	// followedNodes = 0;
	// }
	// }

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
					i.remove();
				}
			}
		}
		if (life <= LIFE_THRESHOLD)
			alive = false;
	}

	// private void pathFind(PVector pointToFleeTo) {
	// int originX = (int)(position.x / environment.getTileSize().x);
	// int originY = (int)(position.y / environment.getTileSize().y);
	// int originNode = originY * (int)environment.getNumTiles().x + originX;
	//
	// int destinationX = (int)(pointToFleeTo.x / environment.getTileSize().x);
	// int destinationY = (int)(pointToFleeTo.y / environment.getTileSize().y);
	// int destinationNode = destinationY * (int)environment.getNumTiles().x +
	// destinationX;
	//
	// if (graphSearch.search(originNode, destinationNode, searchType)) {
	// solutionPath = graphSearch.getSolutionPath();
	// Logger.log("Path cost is " + Double.toString(graphSearch.getPathCost()) +
	// ".");
	// Logger.log("Solution path is " + solutionPath.toString());
	// }
	// graphSearch.reset();
	// }

	private void updatePosition() {
		position.add(velocity);

		Kinematic target = new Kinematic(targetPosition, null, 0, 0);
		SteeringOutput steering;
		ArrayList<Kinematic> followers = new ArrayList<>(leader.followers);

		if(state == States.STAY_WITH_LEADER) {
			steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
			ArrayList<Kinematic> flockers = new ArrayList<>();
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
		updateState(States.KILL_PLAYER);
	}
	
	public void updateStateToFollow() {
		updateState(States.STAY_WITH_LEADER);
	}
}