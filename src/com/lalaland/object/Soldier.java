package com.lalaland.object;

import com.lalaland.utility.Logger;
import processing.core.*;
import com.lalaland.environment.*;

import java.util.Iterator;
import java.util.List;

public class Soldier extends Enemy {

  private static final float SOLDIER_RADIUS = 7;
  private static final PVector SOLDIER_COLOR = new PVector(112, 241, 252);
  private static final int LIFE_THRESHOLD = 50;
  private static final float FLEE_VELOCITY = 3;
  private static final int OBSTACLE_OFFSET = 20;

  private boolean fleeing;
  private boolean PATH_FIND;
  
  public Soldier(float positionX, float positionY, PApplet parent, Environment environment) {
    super(positionX, positionY, parent, environment, SOLDIER_RADIUS, SOLDIER_COLOR.copy());
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 7;
    RADIUS_SATISFACTION = 5;
    MAX_VELOCITY = 1;
    MAX_ACCELERATION = 0.5f;
    targetPosition = new PVector(position.x, position.y);
    lifeReductionRate = 5;
    fleeing = false;
    PATH_FIND = false;
  }

  @Override
  public void move() {
    updateLife();
    System.out.println(life);

    if (life <= LIFE_THRESHOLD) {
      fleeing = true;
      flee();
    }
    else {
      fleeing = false;
      targetPosition.x = environment.getPlayer().getPosition().x;
      targetPosition.y = environment.getPlayer().getPosition().y;
    }
    if (POSITION_MATCHING)
      movePositionMatching();
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

  private void flee() {
    if (!PATH_FIND) {
      Obstacle coverObstacle = environment.getNearestObstacle(position);
      PVector left, right, up, down;
      left = new PVector(coverObstacle.getCenterPosition().x - coverObstacle.getSize().x / 2, coverObstacle.getCenterPosition().y);
      right = new PVector(coverObstacle.getCenterPosition().x + coverObstacle.getSize().x / 2, coverObstacle.getCenterPosition().y);
      up = new PVector(coverObstacle.getCenterPosition().x, coverObstacle.getCenterPosition().y - coverObstacle.getSize().y / 2);
      down = new PVector(coverObstacle.getCenterPosition().x, coverObstacle.getCenterPosition().y + coverObstacle.getSize().y / 2);
      float minimumDistance = 99999;
      PVector pointToFleeTo = new PVector();
      if (PVector.dist(left, environment.getPlayer().getPosition()) < minimumDistance) {
        minimumDistance = PVector.dist(left, environment.getPlayer().getPosition());
        pointToFleeTo.x = right.x + OBSTACLE_OFFSET;
        pointToFleeTo.y = right.y;
      }
      if (PVector.dist(right, environment.getPlayer().getPosition()) < minimumDistance) {
        minimumDistance = PVector.dist(right, environment.getPlayer().getPosition());
        pointToFleeTo.x = left.x - OBSTACLE_OFFSET;
        pointToFleeTo.y = left.y;
      }
      if (PVector.dist(up, environment.getPlayer().getPosition()) < minimumDistance) {
        minimumDistance = PVector.dist(up, environment.getPlayer().getPosition());
        pointToFleeTo.x = down.x;
        pointToFleeTo.y = down.y + OBSTACLE_OFFSET;
      }
      if (PVector.dist(down, environment.getPlayer().getPosition()) < minimumDistance) {
        pointToFleeTo.x = up.x;
        pointToFleeTo.y = up.y - OBSTACLE_OFFSET;
      }
      PATH_FIND = true;
      pathFindToCover(pointToFleeTo);
    }

    if (solutionPath != null && solutionPath.size() != 0 && reached) {
      int node = solutionPath.poll();
      int gridY = (int) (node / environment.getNumTiles().x);
      int gridX = (int) (node % environment.getNumTiles().x);
      targetPosition.x = gridX * environment.getTileSize().x + environment.getTileSize().x / 2;
      targetPosition.y = gridY * environment.getTileSize().y + environment.getTileSize().y / 2;
    }
  }

  private void pathFindToCover(PVector pointToFleeTo) {
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
    else {
      graphSearch.reset();
    }
  }
  
  private void movePositionMatching() {
    if (position.dist(targetPosition) <= RADIUS_SATISFACTION) {
      velocity.set(0, 0);
      acceleration.set(0, 0);
      reached = true;
      return;
    }
    reached = false;
    
    acceleration = PVector.sub(targetPosition, position);
    acceleration.setMag(MAX_ACCELERATION);
    velocity.add(acceleration);

    if (fleeing) {
      if (velocity.mag() >= FLEE_VELOCITY)
        velocity.setMag(FLEE_VELOCITY);
    }
    else {
      if (velocity.mag() >= MAX_VELOCITY)
        velocity.setMag(MAX_VELOCITY);
    }
    position.add(velocity);
    
    targetOrientation = velocity.heading(); 
    rotation = (targetOrientation - orientation) / TIME_TARGET_ROT;
    orientation += rotation;
    
    if (DRAW_BREADCRUMBS)
      storeHistory();
  }

}