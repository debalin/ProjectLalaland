package com.lalaland.object;

import processing.core.*;
import java.util.*;

public abstract class GameObject {
  
  protected PVector position, velocity;
  protected PVector acceleration;
  protected float orientation, rotation;
  protected PVector targetPosition;
  protected float targetOrientation;
  protected boolean reached;
  protected PApplet parent;
  protected PShape group, head, beak;
  protected Set<PVector> history;
  protected int interval;
  
  protected boolean POSITION_MATCHING;
  protected boolean DRAW_BREADCRUMBS;
  protected float TIME_TARGET_ROT;
  protected float RADIUS_SATISFACTION;
  protected float MAX_VELOCITY;
  protected float MAX_ACCELERATION;
  protected float IND_RADIUS;
  protected PVector IND_COLOR;
  protected PVector CRUMB_COLOR;
  
  private static final int MAX_INTERVAL = 5;

  public GameObject(PApplet parent, float IND_RADIUS, PVector IND_COLOR) {
    this.parent = parent;
    this.IND_RADIUS = IND_RADIUS;
    this.IND_COLOR = this.CRUMB_COLOR = IND_COLOR;
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
  
  public GameObject(float positionX, float positionY, PApplet parent, float IND_RADIUS, PVector IND_COLOR) {
    this(parent, IND_RADIUS, IND_COLOR);
    position = new PVector(positionX, positionY);
    targetPosition = new PVector(positionX, positionY);
    reached = false;
  }
  
  public abstract void move();
  
  public PVector getPosition() {
    return position;
  }

  public void setTargetPosition(PVector targetPosition) {
    this.targetPosition = targetPosition;
  }
  
  protected static float mapToRange(float rotation) {
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
  
  protected void storeHistory() {
    interval++;
    if (interval >= MAX_INTERVAL) {
      history.add(new PVector(position.x, position.y));
      interval = 0;
    }
  }
  
  public void display() {
    if (DRAW_BREADCRUMBS)
      drawBreadcrumbs();
    drawShape();
  }

  protected void drawShape() {
    parent.pushMatrix();
    group.rotate(mapToRange(orientation - (float)(Math.PI / 2)));
    parent.shape(group, position.x, position.y);
    group.resetMatrix();
    parent.popMatrix();
  }
  
  protected void drawBreadcrumbs() {
    parent.pushMatrix();
    parent.fill(CRUMB_COLOR.x, CRUMB_COLOR.y, CRUMB_COLOR.z);
    for (PVector historyPos : history) {
      parent.ellipse(historyPos.x, historyPos.y, IND_RADIUS / 2.5f, IND_RADIUS / 2.0f);
    }
    parent.popMatrix();
  }

}
