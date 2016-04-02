package com.lalaland.engine;

import processing.core.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lalaland.environment.*;
import com.lalaland.object.*;

public class Engine extends PApplet {
  
  private static final PVector RESOLUTION = new PVector(800, 800);
  private static final int SMOOTH_FACTOR = 4;
  private static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);
  private static final PVector PLAYER_INITIAL_POSITION = new PVector(RESOLUTION.x / 2, RESOLUTION.y / 2);
  private static final PVector NUM_TILES = new PVector(80, 80);
  
  private Environment environment;
  private Player player;
  private List<Enemy> enemies;
  
  public void settings() {
    size((int)RESOLUTION.x, (int)RESOLUTION.y, P3D);
    smooth(SMOOTH_FACTOR);
  }
  
  public void setup() {
    noStroke();    
    //frameRate(100);
    environment = new Environment(this, RESOLUTION, NUM_TILES);
    
    player = new Player(PLAYER_INITIAL_POSITION.x, PLAYER_INITIAL_POSITION.y, this, environment);
    environment.setPlayer(player);
    enemies = new LinkedList<>();

    //enemies.add(new Enemy_Grunt(200, 50, this, environment));
    //enemies.add(new Enemy_Hermit(300, 50, this, environment));

    enemies.add(new Soldier(400, -50, this, environment));

  }
  
  public static void main(String args[]) {  
    PApplet.main(new String[] { "com.lalaland.engine.Engine" });
  }
  
  public void draw() {
    background(BACKGROUND_RGB.x, BACKGROUND_RGB.y, BACKGROUND_RGB.z);
    
    environment.drawObstacles();

    controlPlayer();
    controlEnemies();
  }

  private void controlPlayer() {
    player.move();
    player.display();
  }

  private void controlEnemies() {
    Iterator<Enemy> i = enemies.iterator();
    while (i.hasNext()) {
      Enemy enemy = i.next();
      if (enemy.isAlive()) {
        enemy.move();
        enemy.display();
      }
      else{
      	i.remove();
      }
    }
  }
  
  public void keyPressed() {
    player.setDirection(key, true);
  }
  
  public void keyReleased() {
    player.setDirection(key, false);
  }
  
  public void mousePressed() {
    player.shootBullet();
  }

}
