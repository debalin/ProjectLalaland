package com.lalaland.object;

import com.lalaland.utility.Logger;
import processing.core.*;
import com.lalaland.environment.*;
import com.lalaland.steering.*;
import java.util.Iterator;
import java.util.List;

public class Enemy_Soldier extends Enemy {

  private static final float SOLDIER_RADIUS = 7;
  private static final PVector SOLDIER_COLOR = new PVector(112, 241, 252);
  private static final float COVER_THRESHOLD = 30;
  private static final int REGAIN_THRESHOLD = 70;
  private static final float FLEE_VELOCITY = 2;
  private static final int OBSTACLE_OFFSET = 20;
  private static final int MAX_FOLLOW_NODE_COUNT = 10;

  private boolean startTakingCover;
  private int followedNodes;
  private enum States {
    SEEK, PATH_FIND_COVER, PATH_FOLLOW_COVER, PATH_FIND_PLAYER, PATH_FOLLOW_PLAYER, REGAIN_HEALTH
  }
  private States state;
  private float lifeRegainRate;
  private Obstacle lastCoverObstacle;
  
  public Enemy_Soldier(float positionX, float positionY, PApplet parent, Environment environment) {
    super(positionX, positionY, parent, environment, SOLDIER_RADIUS, SOLDIER_COLOR.copy());
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 10;
    RADIUS_SATISFACTION = 10;
    MAX_VELOCITY = 1;
    MAX_ACCELERATION = 0.3f;
    targetPosition = new PVector(position.x, position.y);
    lifeReductionRate = 5;
    lifeRegainRate = 0.08f;
    startTakingCover = false;
    followedNodes = 0;
    state = States.SEEK;
    lastCoverObstacle = null;
  }

  @Override
  public void move() {
    updateLife();

    switch (state) {
      case SEEK:
        targetPosition.x = environment.getPlayer().getPosition().x;
        targetPosition.y = environment.getPlayer().getPosition().y;
        if (checkForObstacleAvoidance())
          updateState(States.PATH_FIND_PLAYER);
        break;
      case PATH_FIND_COVER:
        findCover();
        break;
      case PATH_FOLLOW_COVER:
        takeCover();
        break;
      case PATH_FIND_PLAYER:
        findPlayer();
        break;
      case PATH_FOLLOW_PLAYER:
        followPathForSometime();
        break;
      case REGAIN_HEALTH:
        regainHealth();
        break;
    }

    updatePosition();
  }

  private void findPlayer() {
    PVector pointToFleeTo = targetPosition.copy();
    pathFind(pointToFleeTo);
    updateState(States.PATH_FOLLOW_PLAYER);
  }

  private void followPathForSometime() {
    if (solutionPath != null && solutionPath.size() != 0 && (reached || !startTakingCover) && followedNodes <= MAX_FOLLOW_NODE_COUNT) {
      int node = solutionPath.poll();
      int gridY = (int) (node / environment.getNumTiles().x);
      int gridX = (int) (node % environment.getNumTiles().x);
      targetPosition.x = gridX * environment.getTileSize().x + environment.getTileSize().x / 2;
      targetPosition.y = gridY * environment.getTileSize().y + environment.getTileSize().y / 2;
      startTakingCover = true;
      followedNodes++;
    }
    else if (solutionPath == null || solutionPath.size() == 0 || followedNodes > MAX_FOLLOW_NODE_COUNT) {
      updateState(States.SEEK);
      startTakingCover = false;
      followedNodes = 0;
    }
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
          if (state == States.REGAIN_HEALTH)
            updateState(States.PATH_FIND_COVER);
          i.remove();
        }
      }
    }
    if (life <= LIFE_THRESHOLD)
      alive = false;
    if (life <= COVER_THRESHOLD && state == States.SEEK)
      updateState(States.PATH_FIND_COVER);
  }

  private void regainHealth() {
    life += lifeRegainRate;
    if (life >= REGAIN_THRESHOLD)
      updateState(States.SEEK);
  }

  private void findCover() {
    lastCoverObstacle = environment.getNearestObstacle(position, lastCoverObstacle);
    PVector left, right, up, down;
    left = new PVector(lastCoverObstacle.getCenterPosition().x - lastCoverObstacle.getSize().x / 2, lastCoverObstacle.getCenterPosition().y);
    right = new PVector(lastCoverObstacle.getCenterPosition().x + lastCoverObstacle.getSize().x / 2, lastCoverObstacle.getCenterPosition().y);
    up = new PVector(lastCoverObstacle.getCenterPosition().x, lastCoverObstacle.getCenterPosition().y - lastCoverObstacle.getSize().y / 2);
    down = new PVector(lastCoverObstacle.getCenterPosition().x, lastCoverObstacle.getCenterPosition().y + lastCoverObstacle.getSize().y / 2);
    float minimumDistance = 99999;
    PVector pointToFleeTo = new PVector();
    if (PVector.dist(left, environment.getPlayer().getPosition()) < minimumDistance) {
      minimumDistance = PVector.dist(left, environment.getPlayer().getPosition());
      pointToFleeTo.x = right.x + OBSTACLE_OFFSET;
      pointToFleeTo.y = right.y;
      while (environment.onObstacle(pointToFleeTo)) {
        pointToFleeTo.x += OBSTACLE_OFFSET;
      }
    }
    if (PVector.dist(right, environment.getPlayer().getPosition()) < minimumDistance) {
      minimumDistance = PVector.dist(right, environment.getPlayer().getPosition());
      pointToFleeTo.x = left.x - OBSTACLE_OFFSET;
      pointToFleeTo.y = left.y;
      while (environment.onObstacle(pointToFleeTo)) {
        pointToFleeTo.x -= OBSTACLE_OFFSET;
      }
    }
    if (PVector.dist(up, environment.getPlayer().getPosition()) < minimumDistance) {
      minimumDistance = PVector.dist(up, environment.getPlayer().getPosition());
      pointToFleeTo.x = down.x;
      pointToFleeTo.y = down.y + OBSTACLE_OFFSET;
      while (environment.onObstacle(pointToFleeTo)) {
        pointToFleeTo.y += OBSTACLE_OFFSET;
      }
    }
    if (PVector.dist(down, environment.getPlayer().getPosition()) < minimumDistance) {
      pointToFleeTo.x = up.x;
      pointToFleeTo.y = up.y - OBSTACLE_OFFSET;
      while (environment.onObstacle(pointToFleeTo)) {
        pointToFleeTo.y -= OBSTACLE_OFFSET;
      }
    }
    pathFind(pointToFleeTo);
    updateState(States.PATH_FOLLOW_COVER);
  }

  private void takeCover() {
    if (solutionPath != null && solutionPath.size() != 0 && (reached || !startTakingCover)) {
      int node = solutionPath.poll();
      int gridY = (int) (node / environment.getNumTiles().x);
      int gridX = (int) (node % environment.getNumTiles().x);
      targetPosition.x = gridX * environment.getTileSize().x + environment.getTileSize().x / 2;
      targetPosition.y = gridY * environment.getTileSize().y + environment.getTileSize().y / 2;
      startTakingCover = true;
    }
    else if (solutionPath == null || solutionPath.size() == 0) {
      updateState(States.REGAIN_HEALTH);
      startTakingCover = false;
    }
  }

  private void pathFind(PVector pointToFleeTo) {
    int originX = (int)(position.x / environment.getTileSize().x);
    int originY = (int)(position.y / environment.getTileSize().y);
    int originNode = originY * (int)environment.getNumTiles().x + originX;

    int destinationX = (int)(pointToFleeTo.x / environment.getTileSize().x);
    int destinationY = (int)(pointToFleeTo.y / environment.getTileSize().y);
    int destinationNode = destinationY * (int)environment.getNumTiles().x + destinationX;

    if (graphSearch.search(originNode, destinationNode, searchType)) {
      solutionPath =  graphSearch.getSolutionPath();
      Logger.log("Path cost is " + Double.toString(graphSearch.getPathCost()) + ".");
      Logger.log("Solution path is " + solutionPath.toString());
    }
    graphSearch.reset();
  }
  
  private void updatePosition() {
    position.add(velocity);

    Kinematic target = new Kinematic(targetPosition, null, 0, 0);
    KinematicOutput kinematic;
    SteeringOutput steering = new SteeringOutput();

    if (state == States.PATH_FOLLOW_COVER || state == States.PATH_FIND_COVER) {
      kinematic = Seek.getKinematic(this, target, FLEE_VELOCITY);
      velocity = kinematic.velocity;
      if (velocity.mag() >= FLEE_VELOCITY)
        velocity.setMag(FLEE_VELOCITY);
      if(position.dist(target.position) <= RADIUS_SATISFACTION) {
        reached = true;
        return;
      }
      reached = false;
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
      velocity.add(steering.linear);
      if (velocity.mag() >= MAX_VELOCITY)
        velocity.setMag(MAX_VELOCITY);
    }
    steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;
    orientation += steering.angular;

    if (DRAW_BREADCRUMBS)
      storeHistory();
  }
}