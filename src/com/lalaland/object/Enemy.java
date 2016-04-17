package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import com.lalaland.environment.*;

import java.util.LinkedList;
import java.util.Random;

public abstract class Enemy extends GameObject {

	protected PVector acceleration;
	protected PVector targetPosition;
  float targetOrientation;
	protected boolean reached;
	protected boolean alive;
	protected GraphSearch graphSearch;
	protected LinkedList<Integer> solutionPath;
	protected float RADIUS_SATISFACTION;
	protected float MAX_ACCELERATION;
	float TTA;
  protected final int BORDER_PADDING = 100;
	protected float SEPARATION_THRESHOLD;

	private int wa_counter = 0;
	private final int WA_LIMIT = 300;
	private float wa_angle = PConstants.PI;
	private static int totalHPDamage = 0;

	public enum EnemyTypes {
		SOLDIER, GRUNT, HERMIT, FLOCKER, MARTYR
	}

	public Enemy(float positionX, float positionY, PApplet parent, Environment environment,
			float IND_RADIUS, PVector IND_COLOR) {
		super(positionX, positionY, parent, environment, IND_RADIUS, IND_COLOR);
		acceleration = new PVector();
		reached = false;
		alive = true;
		if (environment != null)
			graphSearch = environment.getNewGraphSearch();
	}
	
	/*************methods*************/

	public static int getTotalHPDamage() {
		return totalHPDamage;
	}

	protected void incrementTotalHPDamage(int damage) {
		totalHPDamage += damage;
	}

	protected static final GraphSearch.SearchType searchType = GraphSearch.SearchType.ASTAR;

	public boolean isAlive() {
		return alive;
	}

	protected void rotateShapeDirection(float angle) {
		angle = (angle-orientation) / 30;
		orientation += angle;
	}

	protected void updateVelocityPerOrientation() {
    velocity = PVector.fromAngle(orientation);
    velocity.setMag(MAX_VELOCITY);
	}

	protected float randomWallAvoidanceAngle() {
		wa_counter++;
		if (wa_counter == WA_LIMIT) {
			wa_angle = (parent.random(2) > 1) ? -1 * PConstants.PI : PConstants.PI;
			wa_counter = 0;
		}
		return wa_angle;
	}

	float randomBinomial() {
		return parent.random(0, 1) - parent.random(0, 1);
	}
	
	boolean checkForObstacleAvoidance(PVector target){
		PVector future_ray1 = PVector.add(position, PVector.mult(target, 15f));
		PVector future_ray2 = PVector.add(position, PVector.mult(target, 30f));
    PVector future_ray3 = PVector.add(position, PVector.mult(target, 3.5f));
    PVector future_ray4 = PVector.add(position, PVector.mult(target, 1.5f));
    PVector future_ray5 = PVector.add(position, PVector.mult(target, 50f));
		return(
				environment.onObstacle(future_ray1) || 
				environment.onObstacle(future_ray2) ||
        environment.onObstacle(future_ray3) ||
        environment.onObstacle(future_ray4) ||
        environment.onObstacle(future_ray5)
				);
	}

  boolean checkForBoundaryAvoidance(PVector target){
    PVector future_ray =  PVector.add(position, target);
    //// TODO: 4/2/2016 add more future rays in condn check
    return(
        future_ray.x <= BORDER_PADDING ||
        future_ray.x >= parent.width - BORDER_PADDING ||
        future_ray.y <= BORDER_PADDING ||
        future_ray.y >= parent.height - BORDER_PADDING
        );
  }

  boolean checkForBoundaryAvoidance(){
    return checkForBoundaryAvoidance(velocity);
  }

  void avoidObstacle() {
    float orient;
    Random random = new Random();
    do {
      orient = random.nextInt(180) - random.nextInt(180);
      targetOrientation = parent.radians(orient) + orientation;
    } while (checkForObstacleAvoidance(PVector.fromAngle(targetOrientation).setMag(20)));
  }

  void avoidBoundary(){
      float orient;
      Random random = new Random();
      do {
        orient = random.nextInt(180) - random.nextInt(180);
        targetOrientation = parent.radians(orient) +  orientation;
      } while(checkForBoundaryAvoidance(PVector.fromAngle(targetOrientation).setMag(100)));
	}

  void avoidObstacleOnWander(){
    float avoidance_orient = randomWallAvoidanceAngle();
    rotateShapeDirection(avoidance_orient);
    updateVelocityPerOrientation();
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
