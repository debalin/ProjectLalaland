package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
	
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Grunt extends Enemy {
	private static final float GRUNT_RADIUS = 9;
	private static final float GRUNT_PADDING = GRUNT_RADIUS*2.0f;
	private static final PVector GRUNT_COLOR = new PVector(230, 115, 0);
	private static final int LIFE_THRESHOLD = 80;
	
	private int randomiser_counter = 0;
	private int randomiser_limit = 200;
	private float TTA = 26;
	private boolean USE_ACCEL = true;
	private float br_angle = PConstants.PI;
	
	private boolean rotationInProg = false;
  int rotcounter = 0;
  float rand_orient;
	
	public Enemy_Grunt(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, GRUNT_RADIUS, GRUNT_COLOR);
		DRAW_BREADCRUMBS = false;
		TIME_TARGET_ROT = 7;
		MAX_VELOCITY = 1.0f;
		lifeReductionRate = 5;		
	}

	@Override
	public void move() {
		
		updateLife();
    System.out.println(life);

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
      rand_orient = RandomBinomial() * PConstants.PI;
      rotateShapeDirection(rand_orient);
      if(USE_ACCEL)
        rotationInProg = true;
      updateVelocityPerOrientation();
      randomiser_counter = 0;
    }
  
    //update position vectors
    //check if colliding
    handleObstacleAvoidance();
    position.add(velocity);
    if (velocity.mag() >= MAX_VELOCITY)
      velocity.setMag(MAX_VELOCITY);

    //handle behavior near window boundary
    if(position.x < 0 + GRUNT_PADDING){
    	position.x = GRUNT_PADDING;
      rotateShapeDirection(br_angle);
      updateVelocityPerOrientation();
    }
    else if(position.x > parent.width - GRUNT_PADDING){
    	position.x = parent.width - GRUNT_PADDING; 
      rotateShapeDirection(br_angle);
      updateVelocityPerOrientation();
    }    
    else if(position.y < 0 + GRUNT_PADDING){
    	position.y = GRUNT_PADDING;
      rotateShapeDirection(br_angle);
      updateVelocityPerOrientation();
    }
    else if(position.y > parent.height - GRUNT_PADDING){
    	position.y = parent.height - GRUNT_PADDING; 
      rotateShapeDirection(br_angle);
      updateVelocityPerOrientation();
    }
	}
	
	private void handleObstacleAvoidance(){
		PVector future_ray1 = PVector.add(position, PVector.mult(velocity, 10f));;
		PVector future_ray2 = PVector.add(position, PVector.mult(velocity, 15f));;
		PVector future_ray3 = PVector.add(position, PVector.mult(velocity, 12f));;
		PVector future_ray4 = PVector.add(position, PVector.mult(velocity, 13f));;
		if (environment.onObstacle(future_ray1) || environment.onObstacle(future_ray2) || environment.onObstacle(future_ray3) || environment.onObstacle(future_ray4)){
			avoidObstacle();
		}
	}
	
	private void avoidObstacle(){
		float avoidance_orient = (parent.random(1)>0)?PConstants.PI:-PConstants.PI;
    rotateShapeDirection(avoidance_orient);
//    if(USE_ACCEL)
//      rotationInProg = true;
    updateVelocityPerOrientation();
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
	
	 void rotateShapeDirection(float angle){
	    angle = scaleRotationAngle(angle);
	    if(!USE_ACCEL)
	      TTA = 1;
	    angle = angle/TTA;
	    orientation += angle;
	    group.rotateZ(angle);
	  }
	 
	 float scaleRotationAngle(float angle){
	    angle = angle % PConstants.TWO_PI;
	    if (Math.abs(angle) <= PConstants.PI)
	      return angle;
	    if(angle > PConstants.PI){
	      angle -= PConstants.TWO_PI;
	    }
	    else if(angle < -PConstants.PI){
	      angle += PConstants.TWO_PI;
	    }
	    return angle;
	  } 
	
	private void updateVelocityPerOrientation(){
	    velocity.x = MAX_VELOCITY*PApplet.cos(orientation);
	    velocity.y = MAX_VELOCITY*PApplet.sin(orientation);
	  }
	
	private float RandomBinomial(){
	    return  parent.random(0,1) - parent.random(0,1);
	}  

}
