package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import com.lalaland.environment.*;

import java.util.LinkedList;

public abstract class Enemy extends GameObject {

	protected PVector acceleration;
	protected PVector targetPosition;
	protected boolean reached;
	protected boolean alive;
	protected GraphSearch graphSearch;
	protected LinkedList<Integer> solutionPath;
	protected float RADIUS_SATISFACTION;
	protected float MAX_ACCELERATION;
	protected float TTA = 120;
  protected boolean USE_ACCEL = true;
  protected boolean rotationInProg = false;
  protected final int BORDER_PADDING = 12;

	private int wa_counter = 0;
	private final int WA_LIMIT = 300;
	private float wa_angle = PConstants.PI;

	protected static final GraphSearch.SearchType searchType = GraphSearch.SearchType.ASTAR;

  public enum EnemyTypes {
    SOLDIER, GRUNT, HERMIT, FLOCKER
  }

	public boolean isAlive() {
		return alive;
	}

	public Enemy(float positionX, float positionY, PApplet parent, Environment environment,
			float IND_RADIUS, PVector IND_COLOR) {
		super(positionX, positionY, parent, environment, IND_RADIUS, IND_COLOR);
		acceleration = new PVector();
		reached = false;
		alive = true;
		graphSearch = environment.getNewGraphSearch();
	}
	
	/*************methods*************/
	
	protected void rotateShapeDirection(float angle) {
		angle = scaleRotationAngle(angle);
		if (!USE_ACCEL) TTA = 1;
		angle = angle / TTA;
		orientation += angle;
		group.rotateZ(angle);		
	}

	float scaleRotationAngle(float angle) {
		angle = angle % PConstants.TWO_PI;
		if (Math.abs(angle) <= PConstants.PI) return angle;
		if (angle > PConstants.PI) {
			angle -= PConstants.TWO_PI;
		}
		else if (angle < -PConstants.PI) {
			angle += PConstants.TWO_PI;
		}
		return angle;
	}

	protected void updateVelocityPerOrientation() {
		velocity.x = MAX_VELOCITY * PApplet.cos(orientation);
		velocity.y = MAX_VELOCITY * PApplet.sin(orientation);
	}

	protected float randomWallAvoidanceAngle() {
		wa_counter++;
		if (wa_counter == WA_LIMIT) {
			wa_angle = (parent.random(2) > 1) ? -1 * PConstants.PI : PConstants.PI;
			wa_counter = 0;
		}
		return wa_angle;
	}

	protected float randomBinomial() {
		return parent.random(0, 1) - parent.random(0, 1);
	}
	
	protected boolean checkForObstacleAvoidance(){
		PVector future_ray1 = PVector.add(position, PVector.mult(velocity, 1.5f));
		PVector future_ray2 = PVector.add(position, PVector.mult(velocity, 3f));
		if (
				environment.onObstacle(future_ray1) || 
				environment.onObstacle(future_ray2)
				)
		{
			return true;
		}
		return false;		
	}
	
	protected void avoidObstacleOnWander(){
		float avoidance_orient = randomWallAvoidanceAngle();
    rotateShapeDirection(avoidance_orient);
    if(USE_ACCEL)
      rotationInProg = true;    
    updateVelocityPerOrientation();
	}

	protected void avoidBoundary(){
		if(position.x < BORDER_PADDING  ){
    	position.x = BORDER_PADDING;
      rotateShapeDirection(randomWallAvoidanceAngle());
      updateVelocityPerOrientation();
    }
    else if(position.x > parent.width - BORDER_PADDING){
    	position.x = parent.width - BORDER_PADDING; 
      rotateShapeDirection(randomWallAvoidanceAngle());
      updateVelocityPerOrientation();
    }    
    else if(position.y < BORDER_PADDING){
    	position.y = BORDER_PADDING;
      rotateShapeDirection(randomWallAvoidanceAngle());
      updateVelocityPerOrientation();
    }
    else if(position.y > parent.height - BORDER_PADDING){
    	position.y = parent.height - BORDER_PADDING; 
      rotateShapeDirection(randomWallAvoidanceAngle());
      updateVelocityPerOrientation();
    }
	}

  protected void enlarge(){
    IND_RADIUS += 0.5f;
    updateShape();
  }

  protected void diminish(){
    IND_RADIUS -= 0.5f;
    updateShape();
  }
  
  protected void updateShape(){
  	group = parent.createShape(PApplet.GROUP);
    head = parent.createShape(PApplet.ELLIPSE, 0, 0, 2 * IND_RADIUS, 2 * IND_RADIUS);
    head.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    head.setStroke(parent.color(255, 0));
    group.addChild(head);
    beak = parent.createShape(PApplet.TRIANGLE, -IND_RADIUS, IND_RADIUS / 4, IND_RADIUS, IND_RADIUS / 4, 0, 2.1f * IND_RADIUS);
    beak.setFill(parent.color(IND_COLOR.x, IND_COLOR.y, IND_COLOR.z, 255));
    beak.setStroke(parent.color(255, 0));
    group.addChild(beak);
  }
	
}
