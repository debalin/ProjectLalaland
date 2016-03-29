package com.lalaland.object;

import processing.core.*;

public class Player extends GameObject {
  
  private MoveDirection moveTo;

  private static final float PLAYER_RADIUS = 6;
  private static final PVector PLAYER_COLOR = new PVector(41, 242, 138);
  
  public enum MoveDirection {
    LEFT, RIGHT, UP, DOWN, STAY
  }
  
  public Player(float positionX, float positionY, PApplet parent) {
    super(positionX, positionY, parent, PLAYER_RADIUS, PLAYER_COLOR);
    POSITION_MATCHING = true;
    DRAW_BREADCRUMBS = true;
    TIME_TARGET_ROT = 7;
    MAX_VELOCITY = 2;
    moveTo = MoveDirection.STAY;
  }
  
  public void setMoveTo(MoveDirection moveTo) {
    this.moveTo = moveTo;
  }
  
  @Override
  public void move() {
    switch (moveTo) {
    case LEFT:
      velocity.x = -MAX_VELOCITY;
      velocity.y = 0;
      break;
    case RIGHT:
      velocity.x = MAX_VELOCITY;
      velocity.y = 0;
      break;
    case UP:
      velocity.x = 0;
      velocity.y = -MAX_VELOCITY;
      break;
    case DOWN:
      velocity.x = 0;
      velocity.y = MAX_VELOCITY;
      break;
    case STAY:
      velocity.x = 0;
      velocity.y = 0;
      break;
    }
    position.add(velocity);
  }

}
