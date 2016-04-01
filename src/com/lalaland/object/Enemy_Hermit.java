package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Hermit extends Enemy {
	private static final float HERMIT_RADIUS = 7;
	private static final float HERMIT_PADDING = HERMIT_RADIUS*2.0f;
	private static final PVector HERMIT_COLOR = new PVector(102, 255, 51);
	private static final int LIFE_THRESHOLD = 60;
	private static final float HERMIT_VIEW_RADIUS = 30;
	
	private int randomiser_counter = 0;
	private int randomiser_limit = 200;
	private float TTA = 26;
	private boolean USE_ACCEL = true;
	private float br_angle = PConstants.PI;
	
	private boolean rotationInProg = false;
  int rotcounter = 0;
  float rand_orient;
	
	public Enemy_Hermit(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, HERMIT_RADIUS, HERMIT_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 1.0f;
		lifeReductionRate = 4;
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
	      rand_orient = RandomBinomial() * PConstants.PI;
	      rotateShapeDirection(rand_orient);
	      if(USE_ACCEL)
	        rotationInProg = true;
	      updateVelocityPerOrientation();
	      randomiser_counter = 0;
	    }
	  
	    //update position vectors
	    //check if colliding
	    boolean onObstacle = handleObstacleAvoidance();
	    if(!onObstacle)
	    	position.add(velocity);
	    

	    //handle behavior near window boundary
	    if(position.x < 0 + HERMIT_PADDING){
	    	position.x = HERMIT_PADDING;
	      rotateShapeDirection(br_angle);
	      updateVelocityPerOrientation();
	    }
	    else if(position.x > parent.width - HERMIT_PADDING){
	    	position.x = parent.width - HERMIT_PADDING; 
	      rotateShapeDirection(br_angle);
	      updateVelocityPerOrientation();
	    }    
	    else if(position.y < 0 + HERMIT_PADDING){
	    	position.y = HERMIT_PADDING;
	      rotateShapeDirection(br_angle);
	      updateVelocityPerOrientation();
	    }
	    else if(position.y > parent.height - HERMIT_PADDING){
	    	position.y = parent.height - HERMIT_PADDING; 
	      rotateShapeDirection(br_angle);
	      updateVelocityPerOrientation();
	    }
		}

}
