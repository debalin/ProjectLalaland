package com.lalaland.object;

import processing.core.*;

public class Player extends GameObject {

  private static final float PLAYER_RADIUS = 7;
  private static final PVector PLAYER_COLOR = new PVector(41, 242, 138);
  
  private boolean LEFT, RIGHT, UP, DOWN;
  
  public Player(float positionX, float positionY, PApplet parent) {
    super(positionX, positionY, parent, PLAYER_RADIUS, PLAYER_COLOR);
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = true;
    TIME_TARGET_ROT = 7;
    MAX_VELOCITY = 2;
    LEFT = RIGHT = UP = DOWN = false;
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
    position.add(velocity);
    orientation = (float) Math.atan2(parent.mouseY - position.y, parent.mouseX - position.x);
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

}
