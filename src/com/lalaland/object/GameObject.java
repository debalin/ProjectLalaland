package com.lalaland.object;

import processing.core.*;
import java.util.*;

import com.lalaland.environment.*;

public abstract class GameObject extends Kinematic {
  
  protected float targetOrientation;
  protected PApplet parent;
  protected PShape group, head, beak;
  protected Set<PVector> history;
  protected int interval;
  protected Environment environment;
  
  protected boolean POSITION_MATCHING;
  protected boolean DRAW_BREADCRUMBS;
  protected float TIME_TARGET_ROT;
  protected float MAX_VELOCITY;
  protected float IND_RADIUS;
  protected PVector IND_COLOR;
  protected PVector CRUMB_COLOR;
  
  private static final int MAX_INTERVAL = 5;
  
  public GameObject(float positionX, float positionY, PApplet parent, Environment environment, float IND_RADIUS, PVector IND_COLOR) {
    this.parent = parent;
    this.environment = environment;
    this.IND_RADIUS = IND_RADIUS;
    this.IND_COLOR = this.CRUMB_COLOR = IND_COLOR;
    
    group = parent.createShape(PApplet.GROUP);
    head = parent.createShape(PApplet.ELLIPSE, 0, 0, 2 * IND_RADIUS, 2 * IND_RADIUS);
    head.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    head.setStroke(parent.color(255, 0));
    group.addChild(head);
    beak = parent.createShape(PApplet.TRIANGLE, -IND_RADIUS, IND_RADIUS / 4, IND_RADIUS, IND_RADIUS / 4, 0, 2.1f * IND_RADIUS);
    beak.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    beak.setStroke(parent.color(255, 0));
    group.addChild(beak);
    
    history = new HashSet<>();
    interval = 0;
    DRAW_BREADCRUMBS = false;
    
    position = new PVector(positionX, positionY);
    velocity = new PVector();
  }
  
  public abstract void move();
  
  public PVector getPosition() {
    return position;
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

  private void drawShape() {
    parent.pushMatrix();
    group.rotate(mapToRange(orientation - (float)(Math.PI / 2)));
    PShape[] children = group.getChildren();
    for (PShape child : children)
      child.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
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
