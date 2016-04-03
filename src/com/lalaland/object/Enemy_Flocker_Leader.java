package com.lalaland.object;

import com.lalaland.utility.Logger;
import processing.core.*;
import com.lalaland.environment.*;
import com.lalaland.steering.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Enemy_Flocker_Leader extends Enemy {

	private static final float LEADER_RADIUS = 7;
	private static final PVector LEADER_COLOR = new PVector(200, 45, 200);
	private static final int NUM_FOLLOWERS = 20;
	private static final int MAX_FOLLOW_NODE_COUNT = 10;
	private static final float SIGHT_RADIUS = 200f;

	private enum States {
		SEEK_PLAYER, PATH_FIND_PLAYER, PATH_FOLLOW_PLAYER, KILL_PLAYER, LEADER_DEAD_KILL_PLAYER
	}

	private States state;
	List<Enemy_Flocker_Follower> followers;
	public int followedNodes;

	public Enemy_Flocker_Leader(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, LEADER_RADIUS, LEADER_COLOR.copy());
		followers = new ArrayList<>();
		POSITION_MATCHING = true;
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 10;
		RADIUS_SATISFACTION = 10;
		MAX_VELOCITY = 0.5f;
		MAX_ACCELERATION = 0.2f;
		targetPosition = new PVector(position.x, position.y);
		lifeReductionRate = 5;
		state = States.SEEK_PLAYER;
		
		for (int i = 0; i < NUM_FOLLOWERS; i++)
			followers.add(new Enemy_Flocker_Follower(positionX, positionY, parent, environment, this));
	}

	@Override
	public void move() {
		updateLife();

		switch (state) {
		case SEEK_PLAYER:
			targetPosition.x = environment.getPlayer().getPosition().x;
			targetPosition.y = environment.getPlayer().getPosition().y;
			if(playerWithinSight())
				updateState(States.KILL_PLAYER);
			if (checkForObstacleAvoidance())
				updateState(States.PATH_FIND_PLAYER);
			break;
		case PATH_FIND_PLAYER:
			findPlayer();
			break;
		case PATH_FOLLOW_PLAYER:
			followPathForSometime();
			break;
		case KILL_PLAYER:
			targetPosition.x = environment.getPlayer().getPosition().x;
			targetPosition.y = environment.getPlayer().getPosition().y;
			if(!playerWithinSight())
				updateState(States.SEEK_PLAYER);
			if (checkForObstacleAvoidance())
				updateState(States.PATH_FIND_PLAYER);
			break;
		case LEADER_DEAD_KILL_PLAYER:
			targetPosition.x = environment.getPlayer().getPosition().x;
			targetPosition.y = environment.getPlayer().getPosition().y;
			if(allFollowersDead())
				alive = false;
			break;
		}

		updatePosition();
		
		for(Enemy_Flocker_Follower follower : followers)
			follower.move();
	}

	private void findPlayer() {
		PVector pointToFleeTo = targetPosition.copy();
		pathFind(pointToFleeTo);
		updateState(States.PATH_FOLLOW_PLAYER);
	}

	private void followPathForSometime() {
		if (solutionPath != null && solutionPath.size() != 0 && followedNodes == 0) {
			int node = solutionPath.poll();
			int gridY = (int) (node / environment.getNumTiles().x);
			int gridX = (int) (node % environment.getNumTiles().x);
			targetPosition.x = gridX * environment.getTileSize().x + environment.getTileSize().x / 2;
			targetPosition.y = gridY * environment.getTileSize().y + environment.getTileSize().y / 2;
			followedNodes++;
		} else if (solutionPath != null && solutionPath.size() != 0 && reached && followedNodes <= MAX_FOLLOW_NODE_COUNT) {
			int node = solutionPath.poll();
			int gridY = (int) (node / environment.getNumTiles().x);
			int gridX = (int) (node % environment.getNumTiles().x);
			targetPosition.x = gridX * environment.getTileSize().x + environment.getTileSize().x / 2;
			targetPosition.y = gridY * environment.getTileSize().y + environment.getTileSize().y / 2;
			followedNodes++;
		} else if (solutionPath == null || solutionPath.size() == 0 || followedNodes > MAX_FOLLOW_NODE_COUNT) {
			updateState(States.SEEK_PLAYER);
			followedNodes = 0;
		}
	}

	private void updateState(States state) {
		this.state = state;
		if(state == States.KILL_PLAYER || state == States.LEADER_DEAD_KILL_PLAYER)
			for(Enemy_Flocker_Follower follower : followers)
				follower.updateStateToKill();
		else
			for(Enemy_Flocker_Follower follower : followers)
				follower.updateStateToFollow();
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
		if (life <= LIFE_THRESHOLD) {
//			alive = false;
			updateState(States.LEADER_DEAD_KILL_PLAYER);
		}
	}

	private void pathFind(PVector pointToFleeTo) {
		int originX = (int) (position.x / environment.getTileSize().x);
		int originY = (int) (position.y / environment.getTileSize().y);
		int originNode = originY * (int) environment.getNumTiles().x + originX;

		int destinationX = (int) (pointToFleeTo.x / environment.getTileSize().x);
		int destinationY = (int) (pointToFleeTo.y / environment.getTileSize().y);
		int destinationNode = destinationY * (int) environment.getNumTiles().x + destinationX;

		if (graphSearch.search(originNode, destinationNode, searchType)) {
			solutionPath = graphSearch.getSolutionPath();
			Logger.log("Path cost is " + Double.toString(graphSearch.getPathCost()) + ".");
			Logger.log("Solution path is " + solutionPath.toString());
		}
		graphSearch.reset();
	}

	private void updatePosition() {
		position.add(velocity);

		Kinematic target = new Kinematic(targetPosition, null, 0, 0);
		SteeringOutput steering = new SteeringOutput();

		steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
		if (steering.linear.mag() == 0) {
			velocity.set(0, 0);
			acceleration.set(0, 0);
			reached = true;
			return;
		}
		reached = false;
		velocity.add(steering.linear);
		if (velocity.mag() >= MAX_VELOCITY)
			velocity.setMag(MAX_VELOCITY);

		steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;
		orientation += steering.angular;

		if (DRAW_BREADCRUMBS)
			storeHistory();
	}
	
	@Override
	public void display() {
		for(Enemy_Flocker_Follower follower : followers)
			if(follower.isAlive())
				follower.display();
		if(state != States.LEADER_DEAD_KILL_PLAYER)
			super.display();
	}
	
	private boolean playerWithinSight() {
		return position.dist(environment.getPlayer().getPosition()) <= SIGHT_RADIUS;
	}
	
	private boolean allFollowersDead() {
		for(Enemy_Flocker_Follower follower : followers)
			if(follower.isAlive())
				return false;
		return true;
	}
}