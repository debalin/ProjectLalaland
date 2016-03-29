package com.lalaland.engine;

import processing.core.*;
import java.util.*;

import com.lalaland.environment.*;
import com.lalaland.object.*;
import com.lalaland.utility.*;

public class Engine extends PApplet {
  
  private static final PVector RESOLUTION = new PVector(800, 800);
  private static final int SMOOTH_FACTOR = 4;
  private static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);
  private static final PVector INITIAL_POSITION = new PVector(50, RESOLUTION.y - 50);
  private static final PVector NUM_TILES = new PVector(80, 80);
  private static final boolean DRAW_PATH = false;
  private static final GraphSearch.SearchType searchType = GraphSearch.SearchType.ASTAR; 
  
  private Environment environment;
  private GraphSearch graphSearch;
  private LinkedList<Integer> solutionPath, toDrawPath;
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
    player = new Player(INITIAL_POSITION.x, INITIAL_POSITION.y, this);
    notReachable = NotReachable.FALSE;
    
    toDrawPath = new LinkedList<Integer>();
  }
  
  public static void main(String args[]) {  
    PApplet.main(new String[] { "Engine" });
  }
  
  @SuppressWarnings("unused")
  public void draw() {
    background(BACKGROUND_RGB.x, BACKGROUND_RGB.y, BACKGROUND_RGB.z);
    
    environment.drawObstacles();
    environment.drawTarget(notReachable);
    
    if (solutionPath != null && solutionPath.size() != 0 && player.isReached()) {
      int node = solutionPath.poll();
      int gridY = (int)(node / NUM_TILES.x);
      int gridX = (int)(node % NUM_TILES.x);
      player.setTargetPosition(new PVector(gridX * environment.getTileSize().x + environment.getTileSize().x / 2, gridY * environment.getTileSize().y + environment.getTileSize().y / 2));
    }
    if (toDrawPath.size() != 0 && DRAW_PATH) {
      environment.drawPath(toDrawPath);
    }
    
    player.move();
    player.display();
    
    Utility.drawText(searchType.toString(), RESOLUTION.x / 2f, RESOLUTION.y / 2f, this);
  }
  
  public void mouseClicked() {
    PVector indicatorPosition = player.getPosition();
    
    int originX = (int)(indicatorPosition.x / environment.getTileSize().x);
    int originY = (int)(indicatorPosition.y / environment.getTileSize().x);
    int originNode = originY * (int)NUM_TILES.x + originX;
    
    int destinationX = (int)(mouseX / environment.getTileSize().x);
    int destinationY = (int)(mouseY / environment.getTileSize().y);
    int destinationNode = destinationY * (int)NUM_TILES.x + destinationX;
    Logger.log("Origin is " + originNode + " and destination is " + destinationNode + ".");
    
    environment.setTargetPosition(new PVector(mouseX, mouseY));
    notReachable = NotReachable.FALSE;
    
    if (environment.getInvalidNodes().contains(new PVector(destinationX, destinationY))) {
      notReachable = NotReachable.ON_OBSTACLE;
      return;
    }
    
    long start_time = System.nanoTime();
    if (graphSearch.search(originNode, destinationNode, searchType)) {
      solutionPath = graphSearch.getSolutionPath();
      if (DRAW_PATH)
        toDrawPath.addAll(solutionPath);
      Logger.log("Path cost is " + Double.toString(graphSearch.getPathCost()) + ".");
      Logger.log("Solution path is " + solutionPath.toString());
    }
    else {
      notReachable = NotReachable.NO_PATH;
    }
    long end_time = System.nanoTime();
    double timeTaken = (end_time - start_time) / 1e6;
    Logger.log("Time taken is " + Double.toString(timeTaken) + ".");
    graphSearch.reset();
  }

}
