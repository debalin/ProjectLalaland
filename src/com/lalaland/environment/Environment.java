package com.lalaland.environment;

import processing.core.*;
import java.util.*;

import com.lalaland.object.BonusItem;
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
  private List<BonusItem> bonusItems;

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

  public Environment(PApplet parent, PVector resolution, PVector numTiles) {
    this.parent = parent;
    this.width = (int)resolution.x;
    this.height = (int)resolution.y;
    this.numTiles = numTiles;

    obstacles = new ArrayList<>();
    invalidNodes = new HashSet<>();
    utility = new Utility();
    makeTiles((int)numTiles.x, (int)numTiles.y);
    createObstacles();
    buildGraph();
  }
  
  public void makeTiles(int numTilesX, int numTilesY) {
    numTiles = new PVector(numTilesX, numTilesY);
    tileSize = new PVector(width / numTiles.x, height / numTiles.y);
    Logger.log("numTiles = " + numTiles + " tileSize = " + tileSize);
  }
  
  public void createObstacles() {
    PVector obstacleColor = new PVector(123, 116, 214);
    obstacles.add(new Obstacle((int)(numTiles.x * 0.23), (int)(numTiles.y * 0.35) - 2, (int)(numTiles.x * 0.33), (int)(numTiles.y * 0.35), this, obstacleColor)); //Left Top cover point
    obstacles.add(new Obstacle((int)(numTiles.x * 0.33) - 2, (int)(numTiles.y * 0.26), (int)(numTiles.x * 0.33), (int)(numTiles.y * 0.35) - 2, this, obstacleColor)); //Left Top cover point

    obstacles.add(new Obstacle((int)(numTiles.x * 0.65), (int)(numTiles.y * 0.26), (int)(numTiles.x * 0.75), (int)(numTiles.y * 0.35), this, obstacleColor)); //Right Top Box

    obstacles.add(new Obstacle((int)(numTiles.x * 0.23), (int)(numTiles.y * 0.65), (int)(numTiles.x * 0.33), (int)(numTiles.y * 0.75), this, obstacleColor)); //Left Bottom Box

    obstacles.add(new Obstacle((int)(numTiles.x * 0.65), (int)(numTiles.y * 0.65), (int)(numTiles.x * 0.75), (int)(numTiles.y * 0.65) + 2, this, obstacleColor)); //Right Bottom cover point
    obstacles.add(new Obstacle((int)(numTiles.x * 0.65), (int)(numTiles.y * 0.65) + 2, (int)(numTiles.x * 0.65) + 2, (int)(numTiles.y * 0.75), this, obstacleColor)); //Right Bottom cover point
    
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
  
  public void buildGraph() {
    adjacencyList = utility.buildGraph(invalidNodes, numTiles);
    nodesList = utility.getNodesList();
  }
  
  public boolean onObstacle(PVector position) {
    int gridX = (int)(position.x / tileSize.x);
    int gridY = (int)(position.y / tileSize.y);

    return invalidNodes.contains(new PVector(gridX, gridY));
  }
  
  public BonusItem onBonusItem(PVector position){
     Iterator<BonusItem> i = bonusItems.iterator();
     while(i.hasNext()){
    	 BonusItem item = i.next();
    	 if(Utility.calculateEuclideanDistance(position, item.getPosition()) < 12){
    		 item.consumeItem();
    		 return item;
    	 }
     }
     return null;
  }
  
  public PVector getRandomValidPosition(){
  	float BORDER_PADDING = 8;
  	PVector randPosition;
  	do{
	  	float x = parent.random(BORDER_PADDING, width-BORDER_PADDING);
	  	float y = parent.random(BORDER_PADDING, height-BORDER_PADDING);
	  	randPosition = new PVector(x,y);
  	}
  	while(onObstacle(randPosition) || onBonusItem(randPosition) != null);  	
  	return randPosition;  	
  }
  
  public boolean outOfBounds(PVector position) {
    if (position.x >= width || position.x <= 0)
      return true;
    if (position.y >= height || position.y <= 0)
      return true;
    return false;
  }

  public boolean inSameGrid(PVector position1, PVector position2) {
    int gridX1 = (int)(position1.x / tileSize.x);
    int gridY1 = (int)(position1.y / tileSize.y);
    int gridX2 = (int)(position2.x / tileSize.x);
    int gridY2 = (int)(position2.y / tileSize.y);

    int gridX1_LT = gridX1 - 1;
    int gridY1_LT = gridY1 - 1;
    int gridX1_RB = gridX1 + 1;
    int gridY1_RB = gridY1 + 1;
    /*int gridX2_LT = gridX2 - 1;
    int gridY2_LT = gridY2 - 1;
    int gridX2_RB = gridX2 + 1;
    int gridY2_RB = gridY2 + 1;*/

    if (gridX2 >= gridX1_LT && gridX2 <= gridX1_RB && gridY2 >= gridY1_LT && gridY2 <= gridY1_RB)
      return true;
    else
      return false;
  }

  public Obstacle getNearestObstacle(PVector position, Obstacle exceptThisObstacle) {
    Obstacle nearestObstacle = null;
    float minimumDistance = 999999;
    for (Obstacle obstacle : obstacles) {
      float distance = PVector.dist(obstacle.getCenterPosition(), position);
      if (distance < minimumDistance && obstacle != exceptThisObstacle) {
        minimumDistance = distance;
        nearestObstacle = obstacle;
      }
    }
    return nearestObstacle;
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }
  
  public List<BonusItem> getBonusItems(){
  	return bonusItems;
  }
  
  public void setBonusItems(List<BonusItem> items){
  	this.bonusItems = items;
  }

  public GraphSearch getNewGraphSearch() {
    return (new GraphSearch(this, (int)(numTiles.x * numTiles.y)));
  }
}
