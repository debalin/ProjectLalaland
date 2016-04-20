package com.lalaland.object;

import processing.core.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lalaland.environment.*;
import com.lalaland.utility.Logger;

public class Player extends GameObject {

  private static final float PLAYER_RADIUS = 7;
  private static int GUN_FIRE_INTERVAL = 10;
  private static int BONUS_TIMEOUT_DURATION = 400;
  private static int gun_reset_framecount = 0;
  private static final PVector PLAYER_COLOR = new PVector(41, 242, 138);

  private boolean LEFT, RIGHT, UP, DOWN;
  private List<Bullet> bullets;
  private boolean alive;
  
  public Player(float positionX, float positionY, PApplet parent, Environment environment) {
    super(positionX, positionY, parent, environment, PLAYER_RADIUS, PLAYER_COLOR);
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 7;
    MAX_VELOCITY = 2;
    LEFT = RIGHT = UP = DOWN = false;
    bullets = Collections.synchronizedList(new LinkedList<>());
    alive = true;
    MAX_LIFE = 100000;
    life = MAX_LIFE;
  }

  public PVector getVelocity() {
    return velocity;
  }
  
  @Override
  public void move() {
    if (LEFT)
      velocity.x = -MAX_VELOCITY;
    else if (RIGHT)
      velocity.x = MAX_VELOCITY;
    else
      velocity.x = 0f;
    if (UP)
      velocity.y = -MAX_VELOCITY;
    else if (DOWN)
      velocity.y = MAX_VELOCITY;
    else
      velocity.y = 0f;
    if (!environment.onObstacle(PVector.add(position, velocity)) && !environment.outOfBounds(PVector.add(position, velocity), PLAYER_RADIUS * 2))
      position.add(velocity);
    orientation = (float) Math.atan2(parent.mouseY - position.y, parent.mouseX - position.x);
    
    controlBullets();
    controlBonusItemPicking();
    
    if (DRAW_BREADCRUMBS)
      storeHistory();
  }

  public void reduceLife(float damage) {
    life -= damage;
    if (life <= 0) {
      System.out.println("Player dead.");
      environment.getEnemies().forEach(enemy -> enemy.printCommonMetrics());
      alive = false;
    }
  }

  public boolean isAlive() {
    return alive;
  }
  
  private void controlBonusItemPicking(){
  	BonusItem item = environment.onBonusItem(position); 
  	if(item != null ){
  		setGUN_FIRE_INTERVAL(4);
  		gun_reset_framecount = parent.frameCount + BONUS_TIMEOUT_DURATION;  		
  	}
  	handleGunReset();
  }
  
  private void handleGunReset(){
  	if(parent.frameCount == gun_reset_framecount)
  		setGUN_FIRE_INTERVAL(10);
  }
  
  private void controlBullets() {
    synchronized (bullets) {
      Iterator<Bullet> i = bullets.iterator();
      while (i.hasNext()) {
        Bullet bullet = i.next();
        if (!environment.outOfBounds(bullet.getPosition(), 0) && !environment.onObstacle(bullet.getPosition())) {
          bullet.move();
          bullet.display();
        } else {
          i.remove();
        }
      }
    }
    Logger.log("Number of bullets = " + getBullets().size());
  }
  
  public void setDirection(int key, boolean set) {
    switch(key) {
    case 'W':
    case 'w':
      UP = set;
      break;
    case 'S':
    case 's':
      DOWN = set;
      break;
    case 'A':
    case 'a':
      LEFT = set;
      break;
    case 'D':
    case 'd':
      RIGHT = set;
      break;
    }
  }
  
  public void shootBullet() {
    getBullets().add(new Bullet(position.x, position.y, orientation, parent, 3, new PVector(255, 0, 0)));
  }

  public void shootRadialBullets(){
    int RADIAL_NUM = 5;
    float orient;
    for(int i = 0; i< RADIAL_NUM; i++ ){
      orient = orientation + i*PConstants.TWO_PI/RADIAL_NUM;
      bullets.add(new Bullet(position.x, position.y, orient, parent, 3, new PVector(255, 0, 0)));
    }

  }

  public List<Bullet> getBullets() {
    return bullets;
  }
  
  public static int getGUN_FIRE_INTERVAL() {
		return GUN_FIRE_INTERVAL;
	}
  
  public static void setGUN_FIRE_INTERVAL(int gUN_FIRE_INTERVAL) {
		GUN_FIRE_INTERVAL = gUN_FIRE_INTERVAL;
	}
}
