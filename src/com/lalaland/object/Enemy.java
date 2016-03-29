package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PVector;

public abstract class Enemy extends GameObject {

  protected PVector acceleration;
  protected PVector targetPosition;
  protected boolean reached;
  
  protected float RADIUS_SATISFACTION;
  protected float MAX_ACCELERATION;
  
  public Enemy(float positionX, float positionY, PApplet parent, float IND_RADIUS, PVector IND_COLOR) {
    super(positionX, positionY, parent, IND_RADIUS, IND_COLOR);
    acceleration = new PVector();
    reached = false;
    targetPosition = new PVector(positionX, positionY);
  }
  
}
