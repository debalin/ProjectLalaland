package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
	
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Grunt extends Enemy {
	private static final float GRUNT_RADIUS = 7;
	private static final PVector GRUNT_COLOR = new PVector(230, 115, 0);
	private static final int LIFE_THRESHOLD = 5;
	
	private int randomiser_counter = 0;
	private int randomiser_limit = 30;	
	private int rotcounter = 0;
  private float rand_orient;
  private float TTA = 120;
	private boolean rotationInProg = false;	
	private boolean USE_ACCEL = true;	  
	
	public Enemy_Grunt(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, GRUNT_RADIUS, GRUNT_COLOR);
		DRAW_BREADCRUMBS = false;
		MAX_VELOCITY = 1.0f;
		lifeReductionRate = 5;
	}

	@Override
	public void move() {		
		updateLife();
    if (life <= LIFE_THRESHOLD) {
      alive = false;
    }
    else {
    	wander();      
    }    
	}
	
	private void wander(){
	//to smooth out rotation. used only when USE_ACCEL is set true
    if(rotationInProg){
      rotcounter++;
      rotateShapeDirection(rand_orient);
      updateVelocityPerOrientation();
      if(rotcounter == TTA){
        rotcounter = 0;
        rotationInProg = false;
      }
    }

    //get a random orientation and rotate marker
    randomiser_counter++;
    if(randomiser_counter == randomiser_limit){
      rand_orient = randomBinomial() * PConstants.PI;
      rotateShapeDirection(rand_orient);
      if(USE_ACCEL)
        rotationInProg = true;
      updateVelocityPerOrientation();
      randomiser_counter = 0;
    }
  
    //update position vectors
    //check if colliding, returns if future position is on obstacle
    boolean onObstacle = handleObstacleAvoidance();
    if(!onObstacle)
    	position.add(velocity);
    
    //handle behavior near window boundary
    avoidBoundary();
	}
	

	private void updateLife() {
    List<Bullet> bullets = environment.getPlayer().getBullets();
    synchronized (bullets) {
      Iterator<Bullet> i = bullets.iterator();
      while (i.hasNext()) {
        Bullet bullet = i.next();
        if (environment.inSameGrid(bullet.getPosition(), position)) {
          life -= lifeReductionRate;
          IND_COLOR.x = (IND_COLOR.x >= 255) ? 255 : IND_COLOR.x + 15;
          IND_COLOR.y = (IND_COLOR.y >= 255) ? 255 : IND_COLOR.y - 15;
          IND_COLOR.z = (IND_COLOR.z >= 255) ? 255 : IND_COLOR.z - 15;
          i.remove();
        }
      }
    }
  }

}
