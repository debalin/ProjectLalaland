package com.lalaland.object;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import com.lalaland.environment.*;

import java.util.LinkedList;
import java.util.Random;

import static com.lalaland.steering.ObstacleSteering.checkForObstacleAvoidance;

public abstract class Enemy extends GameObject {

	protected PVector acceleration;
	protected PVector targetPosition;
	protected boolean reached;
	protected boolean alive;
	protected GraphSearch graphSearch;
	protected LinkedList<Integer> solutionPath;
	protected float RADIUS_SATISFACTION;
	protected float MAX_ACCELERATION;
	float TTA;
  protected final int BORDER_PADDING = 100;
	protected float SEPARATION_THRESHOLD;

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
