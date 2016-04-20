package com.lalaland.object;

import com.lalaland.environment.Environment;

import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PVector;

public class BonusItem {
	private PVector position;
	private PApplet parent;
	@SuppressWarnings("unused")
	private Environment environment;
	private float IND_RADIUS;
	private PVector IND_COLOR;
	private PShape group, core;
	private boolean isConsumed = false;
	private boolean isRadialBullet = false;
		
	
	public BonusItem(float positionX, float positionY, PApplet parent, Environment environment, boolean isRadialBullet) {
		this.parent = parent;
		this.environment = environment;
		this.position = new PVector(positionX, positionY);
		this.IND_RADIUS = 5;
		this.isRadialBullet = isRadialBullet;
		if(isRadialBullet)
			this.IND_COLOR = new PVector(255, 104, 255);
		else
			this.IND_COLOR = new PVector(153, 204, 255);
		group = parent.createShape();
		group = parent.createShape(PApplet.GROUP);
    core = parent.createShape(PApplet.ELLIPSE, 0, 0, 2 * IND_RADIUS, 2 * IND_RADIUS);
    core.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    core.setStroke(parent.color(255, 0));
    group.addChild(core);
		
	}

	public boolean isRadialBullet(){
		return isRadialBullet;
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
	
	private void drawShape() {
    parent.pushMatrix();
    PShape[] children = group.getChildren();
    for (PShape child : children)
      child.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    parent.shape(group, position.x, position.y);
    group.resetMatrix();
    parent.popMatrix();
  }
	
	
}
