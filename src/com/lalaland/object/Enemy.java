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
  public static final int BORDER_PADDING = 20;
	protected float SEPARATION_THRESHOLD;
	protected float PLAYER_DAMAGE;
	protected float DAMAGE_RADIUS;
	protected float lifeReductionRate;

	protected float survivalTime, damageCount;

	private static int totalHPDamage = 0;

	public enum EnemyTypes {
		SOLDIER, GRUNT, HERMIT, FLOCKER, MARTYR, BLENDER
	}

	public Enemy(float positionX, float positionY, PApplet parent, Environment environment,
			float IND_RADIUS, PVector IND_COLOR) {
		super(positionX, positionY, parent, environment, IND_RADIUS, IND_COLOR);
		acceleration = new PVector();
		reached = false;
		alive = true;
		survivalTime = parent.millis();
		damageCount = 0f;
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

	protected void enlarge(float delta){
		IND_RADIUS += delta;
		updateShape();
	}

  protected void diminish(){
    IND_RADIUS -= 0.5f;
    updateShape();
  }

	protected void diminish(float delta){
		IND_RADIUS -= delta;
		updateShape();
	}

	protected void checkAndReducePlayerLife() {
		if (position.dist(environment.getPlayer().getPosition()) < DAMAGE_RADIUS) {
			environment.getPlayer().reduceLife(PLAYER_DAMAGE);
			damageCount += PLAYER_DAMAGE;
		}
	}

	protected void killYourself(boolean printMetricOrNot) {
		alive = false;
		survivalTime = parent.millis() - survivalTime;
		if (printMetricOrNot)
			printCommonMetrics();
	}

	protected void printCommonMetrics() {
		System.out.println("Survival time: " + survivalTime + " Damage count: " + damageCount);
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
