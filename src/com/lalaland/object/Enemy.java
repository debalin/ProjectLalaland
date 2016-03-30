package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PVector;

import com.lalaland.environment.*;

public abstract class Enemy extends GameObject {

  protected PVector acceleration;
  protected PVector targetPosition;
  protected boolean reached;
  protected boolean alive;
  
  public boolean isAlive() {
    return alive;
  }

  protected float RADIUS_SATISFACTION;
  protected float MAX_ACCELERATION;
  
  public Enemy(float positionX, float positionY, PApplet parent, Environment environment, float IND_RADIUS, PVector IND_COLOR) {
    super(positionX, positionY, parent, environment, IND_RADIUS, IND_COLOR);
    acceleration = new PVector();
    reached = false;
    alive = true;
  }
  
}
