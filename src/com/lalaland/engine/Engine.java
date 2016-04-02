package com.lalaland.engine;

import processing.core.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lalaland.environment.*;
import com.lalaland.object.*;

public class Engine extends PApplet {
  
	public static float time = 0f;
  public static final PVector RESOLUTION = new PVector(1000, 800);
  
  private static final int SMOOTH_FACTOR = 4;
  private static final int BONUS_DROP_INTERVAL = 800;
  private static final PVector BACKGROUND_RGB = new PVector(60, 60, 60);
  private static final PVector PLAYER_INITIAL_POSITION = new PVector(RESOLUTION.x / 2, RESOLUTION.y / 2);
  private static final PVector NUM_TILES = new PVector(100, 80);
  
  private Environment environment;
  private Player player;
  private List<Enemy> enemies;  
  private List<BonusItem> bonusItems;
  
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
    enemies.add(new Enemy_Grunt(200, 50, this, environment));
    enemies.add(new Enemy_Hermit(300, 50, this, environment));
    enemies.add(new Enemy_Soldier(400, -50, this, environment));
    
    bonusItems = new LinkedList<>();    
    environment.setBonusItems(bonusItems);
  }
  
  public static void main(String args[]) {  
    PApplet.main(new String[] { "com.lalaland.engine.Engine" });
  }
  
  public void draw() {
  	time = millis();
    background(BACKGROUND_RGB.x, BACKGROUND_RGB.y, BACKGROUND_RGB.z);
    
    environment.drawObstacles();

    controlPlayer();
    controlEnemies();
    controlItems();
    spawnBonusItems();
  }

  private void controlPlayer() {
    player.move();
    player.display();
    controlPlayerGun();    
  }

  private void controlPlayerGun(){
  	if(mousePressed && frameCount % Player.getGUN_FIRE_INTERVAL() == 0)
    	player.shootBullet();  	
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
  
  private void controlItems(){
  	Iterator<BonusItem> i = bonusItems.iterator();
  	while(i.hasNext()){
  		BonusItem item = i.next();
  		if(!item.isConsumed())
  			item.display();
  		else
  			i.remove();
  	}
  }
  
  private void spawnBonusItems(){
  	if(frameCount % BONUS_DROP_INTERVAL == 0){
  		PVector position = environment.getRandomValidPosition();
  		bonusItems.add(new BonusItem(position.x, position.y, this, environment));
  	}
  }
  
  public void keyPressed() {
    player.setDirection(key, true);
  }
  
  public void keyReleased() {
    player.setDirection(key, false);
  }
  
}
