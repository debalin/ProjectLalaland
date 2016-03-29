package com.lalaland.engine;

import processing.core.*;
import java.util.*;

import com.lalaland.environment.*;
import com.lalaland.object.*;
import com.lalaland.object.Player.MoveDirection;

public class Engine extends PApplet {
  
  private static final PVector RESOLUTION = new PVector(800, 800);
  private static final int SMOOTH_FACTOR = 4;
  private static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);
  private static final PVector PLAYER_INITIAL_POSITION = new PVector(RESOLUTION.x / 2, RESOLUTION.y / 2);
  private static final PVector NUM_TILES = new PVector(80, 80);
  
  private Environment environment;
  private GraphSearch graphSearch;
  private LinkedList<Integer> solutionPath;
  private Player player;
  private NotReachable notReachable;
  
  public enum NotReachable {
    ON_OBSTACLE, NO_PATH, FALSE
  };
  
  public void settings() {
    size((int)RESOLUTION.x, (int)RESOLUTION.y, P3D);
    smooth(SMOOTH_FACTOR);
  }
  
  public void setup() {
    noStroke();
    
    environment = new Environment(this, (int)RESOLUTION.x, (int)RESOLUTION.y);
    environment.makeTiles((int)NUM_TILES.x, (int)NUM_TILES.y);
    environment.createObstacles();
    environment.buildGraph();
    
    graphSearch = new GraphSearch(environment, (int)(NUM_TILES.x * NUM_TILES.y));
    player = new Player(PLAYER_INITIAL_POSITION.x, PLAYER_INITIAL_POSITION.y, this);
    notReachable = NotReachable.FALSE;
  }
  
  public static void main(String args[]) {  
    PApplet.main(new String[] { "com.lalaland.engine.Engine" });
  }
  
  public void draw() {
    background(BACKGROUND_RGB.x, BACKGROUND_RGB.y, BACKGROUND_RGB.z);
    
    environment.drawObstacles();
    environment.drawTarget(notReachable);
    
    player.move();
    player.display();
  }
  
  public void keyPressed() {
    switch(key) {
    case 'W':
    case 'w':
      player.setMoveTo(MoveDirection.UP);
      break;
    case 'S':
    case 's':
      player.setMoveTo(MoveDirection.DOWN);
      break;
    case 'A':
    case 'a':
      player.setMoveTo(MoveDirection.LEFT);
      break;
    case 'D':
    case 'd':
      player.setMoveTo(MoveDirection.RIGHT);
      break;
    default:
      player.setMoveTo(MoveDirection.STAY);
      break;
    }
  }
  
  public void keyReleased() {
    player.setMoveTo(MoveDirection.STAY);
  }

}
