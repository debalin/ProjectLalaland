package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;

import com.lalaland.steering.KinematicOutput;
import com.lalaland.steering.Wander;
import processing.core.PApplet;
import processing.core.PVector;

public class Enemy_Grunt extends Enemy {
	private static final float GRUNT_RADIUS = 7;
	private static final PVector GRUNT_COLOR = new PVector(153, 51, 51);
	private static final int LIFE_THRESHOLD = 5;
  private static final int RANDOMISER_INTERVAL = 150;

  private static int spawnCount = 0;
  private States state;
  private Wander wander;
  public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;

  private enum States {
    TRACKING_WANDER, DIRECTED_WANDER
  }
	
	public Enemy_Grunt(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, GRUNT_RADIUS, GRUNT_COLOR);
		DRAW_BREADCRUMBS = false;
		MAX_VELOCITY = 1.0f;
		lifeReductionRate = 5;
    spawnCount++;
    state = States.DIRECTED_WANDER;
    wander = new Wander(RANDOMISER_INTERVAL);
	}

	@Override
	public void move() {		
		updateLife();

    switch (state) {
      case TRACKING_WANDER:
        trackPlayer();
        break;
      case DIRECTED_WANDER:
        break;
    }

    updatePosition();
	}

  private void trackPlayer() {

  }

  public static int getSpawnCount() {
    return spawnCount;
  }

  public static void initializeSpawnDetails(int frameRate) {
    SPAWN_OFFSET = frameRate * 2;
    SPAWN_INTERVAL = frameRate * 20;
    SPAWN_MAX = 4;
  }
	
	private void updatePosition(){
    KinematicOutput kinematic = wander.getOrientationMatchingSteering(this, environment, parent, BORDER_PADDING, MAX_VELOCITY);
    orientation += kinematic.rotation;
    position.add(kinematic.velocity);
	}

	private void updateLife() {
    List<Bullet> bullets = environment.getPlayer().getBullets();
    synchronized (bullets) {
      Iterator<Bullet> i = bullets.iterator();
      while (i.hasNext()) {
        Bullet bullet = i.next();
        if (environment.inSameGrid(bullet.getPosition(), position)) {
          life -= lifeReductionRate;
          super.incrementTotalHPDamage((int)lifeReductionRate);
          i.remove();
        }
      }
    }
    if (life <= LIFE_THRESHOLD) {
      alive = false;
      spawnCount--;
    }
  }

}
