package com.lalaland.object;

import com.lalaland.utility.Logger;
import processing.core.*;
import com.lalaland.environment.*;

public class Soldier extends Enemy {

  private static final float SOLDIER_RADIUS = 7;
  private static final PVector SOLDIER_COLOR = new PVector(146, 109, 13);
  private static final int PATH_FIND_INTERVAL = 1000;

  private int pathFindStart, pathFindStep;
  
  public Soldier(float positionX, float positionY, PApplet parent, Environment environment, boolean PATH_FIND) {
    super(positionX, positionY, parent, environment, SOLDIER_RADIUS, SOLDIER_COLOR);
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 7;
    RADIUS_SATISFACTION = 5;
    MAX_VELOCITY = 1;
    MAX_ACCELERATION = 0.5f;
    targetPosition = new PVector(position.x, position.y);
    this.PATH_FIND = PATH_FIND;
    pathFindStart = pathFindStep = 0;
  }

  @Override
  public void move() {
    if (PATH_FIND) {
      if (pathFindStart == 0)
        pathFindStart = parent.millis();
      if (parent.millis() > pathFindStart + PATH_FIND_INTERVAL * pathFindStep) {
        pathFindToPlayer();
        pathFindStep++;
      }
      if (solutionPath != null && solutionPath.size() != 0 && reached) {
        int node = solutionPath.poll();
        int gridY = (int) (node / environment.getNumTiles().x);
        int gridX = (int) (node % environment.getNumTiles().x);
        targetPosition.x = gridX * environment.getTileSize().x + environment.getTileSize().x / 2;
        targetPosition.y = gridY * environment.getTileSize().y + environment.getTileSize().y / 2;
      }
    }
    else {
      //write obstacle avoidance code, just set target position
    }

    if (POSITION_MATCHING)
      movePositionMatching();
  }

  private void pathFindToPlayer() {
    PVector playerPosition = environment.getPlayer().getPosition();

    int originX = (int)(position.x / environment.getTileSize().x);
    int originY = (int)(position.y / environment.getTileSize().y);
    int originNode = originY * (int)environment.getNumTiles().x + originX;

    int destinationX = (int)(playerPosition.x / environment.getTileSize().x);
    int destinationY = (int)(playerPosition.y / environment.getTileSize().y);
    int destinationNode = destinationY * (int)environment.getNumTiles().x + destinationX;

    if (environment.onObstacle(position) || environment.outOfBounds(position) || environment.onObstacle(playerPosition) || environment.outOfBounds(playerPosition)) {
      targetPosition.x = playerPosition.x;
      targetPosition.y = playerPosition.y;
      return;
    }

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
    
    if (velocity.mag() >= MAX_VELOCITY)
      velocity.setMag(MAX_VELOCITY);
    position.add(velocity);
    
    targetOrientation = velocity.heading(); 
    rotation = (targetOrientation - orientation) / TIME_TARGET_ROT;
    orientation += rotation;
    
    if (DRAW_BREADCRUMBS)
      storeHistory();
  }

}
