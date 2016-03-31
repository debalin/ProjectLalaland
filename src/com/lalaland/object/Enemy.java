package com.lalaland.object;

import processing.core.PApplet;
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
  protected int life;
  protected int lifeReductionRate;

  protected static final GraphSearch.SearchType searchType = GraphSearch.SearchType.ASTAR;
  private static final int MAX_LIFE = 100;
  
  public boolean isAlive() {
    return alive;
  }
  
  public Enemy(float positionX, float positionY, PApplet parent, Environment environment, float IND_RADIUS, PVector IND_COLOR) {
    super(positionX, positionY, parent, environment, IND_RADIUS, IND_COLOR);
    acceleration = new PVector();
    reached = false;
    alive = true;
    graphSearch = environment.getNewGraphSearch();
    life = MAX_LIFE;
  }
  
}
