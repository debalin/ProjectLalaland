package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PVector;

public class Bullet extends GameObject {
  
  public Bullet(float positionX, float positionY, float orientation, PApplet parent, float IND_RADIUS, PVector IND_COLOR) {
    super(positionX, positionY, parent, null, IND_RADIUS, IND_COLOR);
    group.removeChild(1);
    MAX_VELOCITY = 4;
    this.orientation = orientation;
    velocity = PVector.fromAngle(orientation);
    velocity.setMag(MAX_VELOCITY);
  }

  @Override
  public void move() {
    position.add(velocity);
  }

  @Override
  public void display() {
    if (DRAW_BREADCRUMBS)
      drawBreadcrumbs();
    drawShape();
  }
  
}
