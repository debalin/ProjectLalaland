package com.lalaland.object;

import processing.core.*;

public class Player extends GameObject {
  
  public Player(float positionX, float positionY, PApplet parent) {
    super(positionX, positionY, parent, 6, new PVector(41, 242, 138));
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = true;
    TIME_TARGET_ROT = 7;
    RADIUS_SATISFACTION = 12;
    MAX_VELOCITY = 3;
    MAX_ACCELERATION = 0.5f;
  }
  
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
