package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
	
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Grunt extends Enemy {
	private static final float GRUNT_RADIUS = 7;
	private static final PVector GRUNT_COLOR = new PVector(153, 51, 51);
	private static final int LIFE_THRESHOLD = 5;
	
	private int randomiser_counter = 0;
	private int randomiser_limit = 150;
	private int rotcounter = 0;
  private float rand_orient;
//	private boolean rotationInProg = false;
//	private boolean USE_ACCEL = true;

  private static int spawnCount = 0;
  public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;
	
	public Enemy_Grunt(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, GRUNT_RADIUS, GRUNT_COLOR);
		DRAW_BREADCRUMBS = false;
		MAX_VELOCITY = 1.5f;
		lifeReductionRate = 5;
    TTA = 30;
    spawnCount++;
	}
  //// TODO: 4/3/2016 add state machines !!

	@Override
	public void move() {		
		updateLife();
    wander();
	}

  public static int getSpawnCount() {
    return spawnCount;
  }

  public static void initializeSpawnDetails(int frameRate) {
    SPAWN_OFFSET = frameRate * 2;
    SPAWN_INTERVAL = frameRate * 20;
    SPAWN_MAX = 4;
  }
	
	private void wander(){
    randomiser_counter++;
    if(randomiser_counter == randomiser_limit){
      targetOrientation = randomBinomial() * PConstants.PI + orientation;
      randomiser_counter = 0;
    }

    //// TODO: 4/3/2016 move all the movement shit to wander
    boolean onObstacle = checkForObstacleAvoidance(velocity);
    if(onObstacle) {
//      System.out.println("On OBS");
      avoidObstacle();
    }
    else if(checkForBoundaryAvoidance()){
//      System.out.println("On BND");
      avoidBoundary();
    }
    rotateShapeDirection(targetOrientation);
    updateVelocityPerOrientation();
    position.add(velocity);
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
    }
  }

}
