package com.lalaland.environment;

import processing.core.*;
import java.util.*;

import com.lalaland.object.Player;
import com.lalaland.utility.*;

public class Environment {
  
  private PApplet parent;
  private int width, height;
  private List<Obstacle> obstacles;
  private Set<PVector> invalidNodes;
  private PVector tileSize, numTiles;
  private Map<Integer, List<Utility.Neighbour>> adjacencyList;
  private Map<Integer, Utility.NodeInfo> nodesList;
  private Utility utility;
  private Player player;
  private GraphSearch graphSearch;

  public Map<Integer, List<Utility.Neighbour>> getAdjacencyList() {
    return adjacencyList;
  }

  public Map<Integer, Utility.NodeInfo> getNodesList() {
    return nodesList;
  }

  public Utility getUtility() {
    return utility;
  }

  public Set<PVector> getInvalidNodes() {
    return invalidNodes;
  }

  public PVector getNumTiles() {
    return numTiles;
  }

  public PVector getTileSize() {
    return tileSize;
  }
  
  private Environment() {
    obstacles = new ArrayList<>();
    invalidNodes = new HashSet<>();
    utility = new Utility();
    //graphSearch = new GraphSearch(this, (int)(numTiles.x * numTiles.y));
  }

  public Environment(PApplet parent, int width, int height) {
    this();
    this.parent = parent;
    this.width = width;
    this.height = height;
  }
  
  public void makeTiles(int numTilesX, int numTilesY) {
    numTiles = new PVector(numTilesX, numTilesY);
    tileSize = new PVector(width / numTiles.x, height / numTiles.y);
    Logger.log("numTiles = " + numTiles + " tileSize = " + tileSize);
  }
  
  public void createObstacles() {
    PVector obstacleColor = new PVector(123, 116, 214);
    obstacles.add(new Obstacle((int)(numTiles.x * 0.45), (int)(numTiles.y * 0.2), (int)(numTiles.x * 0.55), (int)(numTiles.y * 0.2), this, obstacleColor)); //Top barrier
    obstacles.add(new Obstacle((int)(numTiles.x * 0.45), (int)(numTiles.y * 0.8), (int)(numTiles.x * 0.55), (int)(numTiles.y * 0.8), this, obstacleColor)); //Bottom barrier
    obstacles.add(new Obstacle((int)(numTiles.x * 0.2), (int)(numTiles.y * 0.45), (int)(numTiles.x * 0.2), (int)(numTiles.y * 0.55), this, obstacleColor)); //Left barrier
    obstacles.add(new Obstacle((int)(numTiles.x * 0.8), (int)(numTiles.y * 0.45), (int)(numTiles.x * 0.8), (int)(numTiles.y * 0.55), this, obstacleColor)); //Right barrier
    
    formInvalidNodes();
  }
  
  public void drawObstacles() {
    parent.pushMatrix();
    for (Obstacle obstacle : obstacles) {
      PVector corner = obstacle.getCorner();
      PVector size = obstacle.getSize();
      PVector obstacleColor = obstacle.getObstacleColor();
      parent.fill(obstacleColor.x, obstacleColor.y, obstacleColor.z);
      parent.rect(corner.x, corner.y, size.x, size.y);
    }
    parent.popMatrix();
  }
  
  public void drawNodes() {
    parent.pushMatrix();
    parent.fill(250, 250, 250);
    for (Map.Entry<Integer, Utility.NodeInfo> entry : nodesList.entrySet()) {
      Utility.NodeInfo nodeInfo = entry.getValue();
      parent.rect(nodeInfo.getGridX() * tileSize.x, nodeInfo.getGridY() * tileSize.y, tileSize.x, tileSize.y);
    }
    parent.popMatrix();
  }
  
  private void formInvalidNodes() {
    for (Obstacle obstacle : obstacles) {
      for (PVector tileLocation : obstacle.getTileLocations()) {
        invalidNodes.add(tileLocation);
        invalidNodes.add(new PVector(tileLocation.x + 1, tileLocation.y));
        invalidNodes.add(new PVector(tileLocation.x, tileLocation.y + 1));
        invalidNodes.add(new PVector(tileLocation.x - 1, tileLocation.y));
        invalidNodes.add(new PVector(tileLocation.x, tileLocation.y - 1));
        invalidNodes.add(new PVector(tileLocation.x + 1, tileLocation.y + 1));
        invalidNodes.add(new PVector(tileLocation.x + 1, tileLocation.y - 1));
        invalidNodes.add(new PVector(tileLocation.x - 1, tileLocation.y + 1));
        invalidNodes.add(new PVector(tileLocation.x - 1, tileLocation.y - 1));
      }
    }
  }
  
  public void drawPath(List<Integer> path) {
    parent.pushMatrix();
    parent.fill(102, 255, 153);
    for (int node : path) {
      int gridY = (int)(node / numTiles.x);
      int gridX = (int)(node % numTiles.x);
      parent.rect(gridX * tileSize.x, gridY * tileSize.y, tileSize.x, tileSize.y);
    }
    parent.popMatrix();
  }
  
  public void buildGraph() {
    adjacencyList = utility.buildGraph(invalidNodes, numTiles);
    nodesList = utility.getNodesList();
  }
  
  public boolean onObstacle(PVector position) {
    int gridX = (int)(position.x / tileSize.x);
    int gridY = (int)(position.y / tileSize.y);

    return invalidNodes.contains(new PVector(gridX, gridY));
  }
  
  public boolean outOfBounds(PVector position) {
    if (position.x >= width || position.x <= 0)
      return true;
    if (position.y >= height || position.y <= 0)
      return true;
    return false;
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

}
