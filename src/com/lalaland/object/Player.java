package com.lalaland.object;

import processing.core.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.lalaland.environment.*;
import com.lalaland.utility.Logger;

public class Player extends GameObject {

  private static final float PLAYER_RADIUS = 7;
  private static final PVector PLAYER_COLOR = new PVector(41, 242, 138);
  
  private boolean LEFT, RIGHT, UP, DOWN;
  private List<Bullet> bullets;
  
  public Player(float positionX, float positionY, PApplet parent, Environment environment) {
    super(positionX, positionY, parent, environment, PLAYER_RADIUS, PLAYER_COLOR);
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 7;
    MAX_VELOCITY = 2;
    LEFT = RIGHT = UP = DOWN = false;
    bullets = new LinkedList<>();
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
    if (!environment.onObstacle(PVector.add(position, velocity)))
      position.add(velocity);
    orientation = (float) Math.atan2(parent.mouseY - position.y, parent.mouseX - position.x);
    
    controlBullets();
    
    if (DRAW_BREADCRUMBS)
      storeHistory();
  }
  
  private void controlBullets() {
    Iterator<Bullet> i = bullets.iterator();
    while (i.hasNext()) {
      Bullet bullet = i.next();
      if (!environment.outOfBounds(bullet.getPosition()) && !environment.onObstacle(bullet.getPosition())) {
        bullet.move();
        bullet.display();
      }
      else {
        i.remove();
      }
    }
    Logger.log("Number of bullets = " + bullets.size());
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
    bullets.add(new Bullet(position.x, position.y, orientation, parent, 3, new PVector(255, 0, 0)));
  }

}
