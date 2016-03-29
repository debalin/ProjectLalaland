package com.lalaland.object;

import processing.core.*;
import java.util.*;

public class Player {
  
  private PVector position, velocity;
  private PVector acceleration;
  private float orientation, rotation;
  private PVector targetPosition;
  private float targetOrientation;
  private boolean reached;
  private PApplet parent;
  private PShape group, head, beak;
  private Set<PVector> history;
  private int interval;
  
  private static final float TIME_TARGET_ROT = 7;
  private static final float RADIUS_SATISFACTION = 12;
  private static final float MAX_VELOCITY = 3;
  private static final float MAX_ACCELERATION = 0.5f;
  private static final float IND_RADIUS = 6;
  private static final PVector IND_COLOR = new PVector(41, 242, 138);
  private static final PVector CRUMB_COLOR = new PVector(77, 192, 250);
  private static final int MAX_INTERVAL = 5;
  private static final boolean POSITION_MATCHING = true;
  
  public PVector getPosition() {
    return position;
  }

  public void setTargetPosition(PVector targetPosition) {
    this.targetPosition = targetPosition;
  }

  public Player(PApplet parent) {
    this.parent = parent;
    velocity = new PVector();
    acceleration = new PVector();
    
    group = parent.createShape(PApplet.GROUP);
    head = parent.createShape(PApplet.ELLIPSE, 0, 0, 2 * IND_RADIUS, 2 * IND_RADIUS);
    head.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    head.setStroke(parent.color(255, 0));
    group.addChild(head);
    beak = parent.createShape(PApplet.TRIANGLE, -IND_RADIUS, IND_RADIUS / 4, IND_RADIUS, IND_RADIUS / 4, 0, 2.1f * IND_RADIUS);
    beak.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    beak.setStroke(parent.color(255, 0));
    group.addChild(beak);
    
    history = new HashSet<PVector>();
    interval = 0;
  }
  
  public Player(float positionX, float positionY, PApplet parent) {
    this(parent);
    position = new PVector(positionX, positionY);
    targetPosition = new PVector(positionX, positionY);
    reached = false;
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
    
    storeHistory();
  }
  
  private static float mapToRange(float rotation) {
    float r = rotation % (2 * (float)Math.PI);
    if (Math.abs(r) <= Math.PI) 
      return r;
    else {
      if (r > Math.PI)
        return (r - 2 * (float)Math.PI);
      else
        return (r + 2 * (float)Math.PI);
    }
  }
  
  public boolean isReached() {
    return reached;
  }
  
  private void storeHistory() {
    interval++;
    if (interval >= MAX_INTERVAL) {
      history.add(new PVector(position.x, position.y));
      interval = 0;
    }
  }
  
  public void display() {
    drawBreadcrumbs();
    drawShape();
  }

  private void drawShape() {
    parent.pushMatrix();
    group.rotate(mapToRange(orientation - (float)(Math.PI / 2)));
    parent.shape(group, position.x, position.y);
    group.resetMatrix();
    parent.popMatrix();
  }
  
  private void drawBreadcrumbs() {
    parent.pushMatrix();
    parent.fill(CRUMB_COLOR.x, CRUMB_COLOR.y, CRUMB_COLOR.z);
    for (PVector historyPos : history) {
      parent.ellipse(historyPos.x, historyPos.y, IND_RADIUS / 2.5f, IND_RADIUS / 2.0f);
    }
    parent.popMatrix();
  }

}
