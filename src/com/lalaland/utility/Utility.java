package com.lalaland.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import processing.core.PApplet;
import processing.core.PVector;

public class Utility {
  
  private Map<Integer, NodeInfo> nodesList;
  
  private static final PVector TEXT_COLOR = new PVector(255, 255, 255);
  private static final int TEXT_SIZE = 18;

  public Utility() {
    nodesList = new HashMap<>();
  }
  
  public class Neighbour {
    public int node;
    public double weight;
    
    public Neighbour(int node, double weight) {
      this.node = node;
      this.weight = weight;
    }
  }
  
  public class NodeInfo {
    public int gridX;
    public int gridY;
    
    public NodeInfo(int gridX, int gridY) {
      this.gridX = gridX;
      this.gridY = gridY;
    }

  }

  public static float randomBinomial() {
    return (float) (Math.random() - Math.random());
  }
  
  public double calculateHeuristicLatLong(int index1, int index2) {
    return(Math.sqrt(Math.pow((69.5 * (nodesList.get(index1).gridX - nodesList.get(index2).gridX)), 2) + Math.pow((69.5 * Math.cos((nodesList.get(index1).gridX + nodesList.get(index2).gridX)/360 * Math.PI) * (nodesList.get(index1).gridY - nodesList.get(index2).gridY)), 2)));
  }
  
  public double calculateHeuristicManhattan(int index1, int index2) {
    return(Math.abs(nodesList.get(index1).gridX - nodesList.get(index2).gridX) + Math.abs(nodesList.get(index1).gridY - nodesList.get(index2).gridY));
  }
  
  public double calculateHeuristicManhattanRandom(int index1, int index2) {
    return(Math.abs(nodesList.get(index1).gridX - nodesList.get(index2).gridX) + Math.abs(nodesList.get(index1).gridY - nodesList.get(index2).gridY)) + Math.random() * 100;
  }
  
  public static float calculateEuclideanDistance(float x1, float y1, float x2, float y2){
  	return (float)Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
  }
  
  public static float calculateEuclideanDistance(PVector v1, PVector v2){
  	return calculateEuclideanDistance(v1.x, v1.y, v2.x, v2.y); 
  }
  
  public Map<Integer, List<Neighbour>> buildGraph(Set<PVector> invalidNodes, PVector numTiles) {
    Map<Integer, List<Neighbour>> adjacencyList = new HashMap<Integer, List<Neighbour>>();
    
    for (int i = 0; i <= numTiles.y - 1; i++) {
      for (int j = 0; j <= numTiles.x - 1; j++) {
        if (invalidNodes.contains(new PVector(j, i)))
          continue;
        int node = i * (int)numTiles.x + j;
        nodesList.put(node, new NodeInfo(j, i));
        List<Neighbour> neighbours = new ArrayList<Neighbour>();
        if (i - 1 >= 0 && !invalidNodes.contains(new PVector(j, i - 1)))
          neighbours.add(new Neighbour((i - 1) * (int)numTiles.x + j, 1));
        if (i + 1 <= numTiles.y - 1 && !invalidNodes.contains(new PVector(j, i + 1)))
          neighbours.add(new Neighbour((i + 1) * (int)numTiles.x + j, 1));
        if (j - 1 >= 0 && !invalidNodes.contains(new PVector(j - 1, i)))
          neighbours.add(new Neighbour(i * (int)numTiles.x + (j - 1), 1));
        if (j + 1 <= numTiles.x - 1 && !invalidNodes.contains(new PVector(j + 1, i)))
          neighbours.add(new Neighbour(i * (int)numTiles.x + (j + 1), 1));
        if (i + 1 <= numTiles.y - 1 && j + 1 <= numTiles.x - 1 && !invalidNodes.contains(new PVector(j + 1, i)) && !invalidNodes.contains(new PVector(j, i + 1)) && !invalidNodes.contains(new PVector(j + 1, i + 1)))
          neighbours.add(new Neighbour((i + 1) * (int)numTiles.x + (j + 1), Math.sqrt(2)));
        if (i + 1 <= numTiles.y - 1 && j - 1 >= 0 && !invalidNodes.contains(new PVector(j - 1, i)) && !invalidNodes.contains(new PVector(j, i + 1)) && !invalidNodes.contains(new PVector(j - 1, i + 1)))
          neighbours.add(new Neighbour((i + 1) * (int)numTiles.x + (j - 1), Math.sqrt(2)));
        if (i - 1 >= 0 && j + 1 <= numTiles.x - 1 && !invalidNodes.contains(new PVector(j + 1, i)) && !invalidNodes.contains(new PVector(j, i - 1)) && !invalidNodes.contains(new PVector(j + 1, i - 1)))
          neighbours.add(new Neighbour((i - 1) * (int)numTiles.x + (j + 1), Math.sqrt(2)));
        if (i - 1 >= 0 && j - 1 >= 0 && !invalidNodes.contains(new PVector(j - 1, i)) && !invalidNodes.contains(new PVector(j, i - 1)) && !invalidNodes.contains(new PVector(j - 1, i - 1)))
          neighbours.add(new Neighbour((i - 1) * (int)numTiles.x + (j - 1), Math.sqrt(2)));
        adjacencyList.put(node, neighbours);
      }
    }
    
    return adjacencyList;
  }
  
  public static void drawText(String text, float positionX, float positionY, PApplet parent) {
    parent.pushMatrix();
    parent.textSize(TEXT_SIZE);
    parent.fill(TEXT_COLOR.x, TEXT_COLOR.y, TEXT_COLOR.z);
    parent.textAlign(PApplet.CENTER, PApplet.CENTER);
    parent.text(text, positionX, positionY, 5);
    parent.popMatrix();
  }
	
}