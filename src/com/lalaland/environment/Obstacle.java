package com.lalaland.environment;

import java.util.*;
import processing.core.*;

import com.lalaland.utility.*;

public class Obstacle {
  
  private PVector corner;
  private PVector size;
  private List<PVector> tileLocations;
  private Environment environment;
  private PVector obstacleColor;
  private PVector centerPosition;
  
  public PVector getCorner() {
    return corner;
  }

  public PVector getSize() {
    return size;
  }
  
  public List<PVector> getTileLocations() {
    return tileLocations;
  }
  
  public Obstacle(int firstGridX, int firstGridY, int lastGridX, int lastGridY, Environment environment, PVector obstacleColor) {
    tileLocations = new ArrayList<>();
    this.environment = environment;
    this.obstacleColor = obstacleColor;
    PVector centerPosition1 = new PVector(firstGridX * environment.getTileSize().x + environment.getTileSize().x / 2, firstGridY * environment.getTileSize().y + environment.getTileSize().y / 2);
    PVector centerPosition2 = new PVector(lastGridX * environment.getTileSize().x + environment.getTileSize().x / 2, lastGridY * environment.getTileSize().y + environment.getTileSize().y / 2);
    centerPosition = new PVector((centerPosition1.x + centerPosition2.x) / 2, (centerPosition1.y + centerPosition2.y) / 2);
    
    for (int i = firstGridY; i <= lastGridY; i++) {
      for (int j = firstGridX; j <= lastGridX; j++) {
        tileLocations.add(new PVector(j, i));
        Logger.log("X = " + j + " Y = " + i);
      }
    }
    
    createGameSpaceCorners();
  }
  
  public PVector getObstacleColor() {
    return obstacleColor;
  }

  public void createGameSpaceCorners() {
    corner = new PVector(tileLocations.get(0).x * environment.getTileSize().x, tileLocations.get(0).y * environment.getTileSize().y);
    float width = (tileLocations.get(tileLocations.size() - 1).x - tileLocations.get(0).x + 1) * environment.getTileSize().x;
    float height = (tileLocations.get(tileLocations.size() - 1).y - tileLocations.get(0).y + 1) * environment.getTileSize().y;
    size = new PVector(width, height);
  }

  public PVector getCenterPosition() {
    return centerPosition;
  }
}
