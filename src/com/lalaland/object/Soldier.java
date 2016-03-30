package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PVector;

import com.lalaland.environment.*;

public class Soldier extends Enemy {

  private static final float SOLDIER_RADIUS = 7;
  private static final PVector SOLDIER_COLOR = new PVector(146, 109, 13);
  
  public Soldier(float positionX, float positionY, PApplet parent, Environment environment) {
    super(positionX, positionY, parent, environment, SOLDIER_RADIUS, SOLDIER_COLOR);
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 7;
    RADIUS_SATISFACTION = 5;
    MAX_VELOCITY = 1;
    MAX_ACCELERATION = 0.5f;
    targetPosition = environment.getPlayer().getPosition();
  }

  @Override
  public void move() {
    if (POSITION_MATCHING)
      movePositionMatching();
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
