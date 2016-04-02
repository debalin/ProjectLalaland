package com.lalaland.object;

import com.lalaland.environment.Environment;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public class BonusItem {
	private PVector position;
	private PApplet parent;
	private Environment environment;
	private float IND_RADIUS;
	private PVector IND_COLOR;
	protected PShape group, core, halo;
	private boolean isConsumed = false;
		
	
	public BonusItem(float positionX, float positionY, PApplet parent, Environment environment) {
		this.parent = parent;
		this.environment = environment;
		this.position = new PVector(positionX, positionY);
		this.IND_RADIUS = 5;
		this.IND_COLOR = new PVector(245, 245, 245);
		group = parent.createShape();
		group = parent.createShape(PApplet.GROUP);
    core = parent.createShape(PApplet.ELLIPSE, 0, 0, 2 * IND_RADIUS, 2 * IND_RADIUS);
    core.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    core.setStroke(parent.color(255, 0));
    group.addChild(core);
		
	}
	
	public boolean isConsumed(){
		return isConsumed;
	}
	
	public void consumeItem(){
		isConsumed = true;
	}
	
	public PVector getPosition(){
		return position;
	}
	
	public void display(){
		this.drawShape();
	}
	
	protected void drawShape() {
    parent.pushMatrix();
    PShape[] children = group.getChildren();
    for (PShape child : children)
      child.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    parent.shape(group, position.x, position.y);
    group.resetMatrix();
    parent.popMatrix();
  }
	
	
}
