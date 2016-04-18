package com.lalaland.object;


import com.lalaland.environment.Environment;
import com.lalaland.steering.LookWhereYoureGoing;
import com.lalaland.steering.Seek;
import com.lalaland.steering.SteeringOutput;
import com.lalaland.steering.Wander;
import com.lalaland.utility.Utility;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.Iterator;
import java.util.List;

public class Enemy_Blender extends Enemy {
  private static final float BLENDER_RADIUS = 6;
  private static final float BLENDER_MAX_RADIUS = 22;
  private static final PVector BLENDER_COLOR = new PVector(255, 103, 255);
  private static final float HEALTH_THRESHOLD = 70.0f;
  private static final float SIZE_CONFIDENCE_MARK = BLENDER_MAX_RADIUS - 10;
  private static final float MIN_LRR = 10;
  private static final float BLENDER_VIEW_RADIUS = 250;
  private static final float MAX_LINEAR_ACC = 0.5f;
  private static final float SEEK_MAX_VELOCITY = 1.3f;

  private Wander wander;
  private static int spawnCount = 0;
  private Enemy_Blender targetBlender = null;
  private boolean isPlayerTarget = false;
  public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;

  public Enemy_Blender(float positionX, float positionY, PApplet parent, Environment environment){
    super(positionX, positionY, parent, environment, BLENDER_RADIUS, BLENDER_COLOR);
    targetPosition = new PVector(position.x, position.y);
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 7;
    MAX_VELOCITY = 0.8f;
    wander = new Wander(150);
    lifeReductionRate = 20;
    spawnCount++;
  }

  @Override
  public void move(){
    updateLife();
    makeDecision();
    updateBlendStatus();
    parent.ellipse(targetPosition.x, targetPosition.y, 6, 6);
  }


  public static void initializeSpawnDetails(int frameRate) {
    SPAWN_OFFSET = frameRate * 5;
    SPAWN_INTERVAL = frameRate * 8;
    SPAWN_MAX = 4;
  }

  public static int getSpawnCount() {
    return spawnCount;
  }

  public void makeDecision(){
    //health above threshold?
    if(life > HEALTH_THRESHOLD){
      //size above confidence threshold?
      if(IND_RADIUS > SIZE_CONFIDENCE_MARK){
        //yes - seek player
        setPlayerAsTarget();
        updatePositionSeek();
//        System.out.println("H>S>PSEEK");
      }else{
        //no - other blenders within view radius?
        if(isABlenderVisible(true, BLENDER_VIEW_RADIUS)) {
          //yes - seek target blender --merge on arrive
          updatePositionSeek();
//          System.out.println("H>S<BDRSEEK");
        }else {
          //no - seek player
          setPlayerAsTarget();
          updatePositionSeek();
//          System.out.println("H>S<PSEEK");
        }
      }
    } else{
      //other blenders present on map?
      if(spawnCount > 1) {
        //yes - seek blender  --merge on arrive
        searchAndSetBlenderAsTarget();
        updatePositionSeek();
//        System.out.println("H<BDRSEEK");
      }else {
        //no - seek player
        setPlayerAsTarget();
        updatePositionSeek();
//        System.out.println("H<PSEEK");
      }
    }
  }


  private boolean isABlenderVisible(boolean setBlenderASTarget, float radius){
    if(spawnCount < 2)
      return false;
    Iterator<Enemy> iterator = environment.getEnemies().iterator();
    while(iterator.hasNext()){
      Enemy enemy = iterator.next();
      if(enemy.getClass() == Enemy_Blender.class){
        if(enemy == this) {
          continue;  //skip if same object
        }
        if(Utility.calculateEuclideanDistance(position, enemy.getPosition()) <= radius){
          if(setBlenderASTarget)
            targetPosition.set(enemy.getPosition());
//          targetBlendSize = enemy.IND_RADIUS;
          targetBlender = (Enemy_Blender) enemy;
          return true;
        }
      }
    }
    return false;
  }

  private void searchAndSetBlenderAsTarget(){
    Iterator<Enemy> iterator = environment.getEnemies().iterator();
    while(iterator.hasNext()) {
      Enemy enemy = iterator.next();
      if (enemy.getClass() == Enemy_Blender.class) {
        if(enemy == this) {
          continue;  //skip if same object
        }
        targetPosition.set(enemy.getPosition());
      }
    }
  }

  private void setPlayerAsTarget(){
    targetPosition.set(environment.getPlayer().getPosition());
  }

  private void updateBlendStatus(){
    if(isABlenderVisible(true, IND_RADIUS*2)){
      if(decideDeath(this, targetBlender) == 1){
        //die
        alive = false;
        spawnCount--;
      }
      else{
        //merge!
        if(IND_RADIUS < BLENDER_MAX_RADIUS){
          enlarge(1);
        }
        absorbOtherBlenderHealth();
        updateLRR();
        setPlayerAsTarget();
      }
    }
  }

  private void absorbOtherBlenderHealth(){
    life += 0.1*targetBlender.life;
    if(life > MAX_LIFE)
      life = MAX_LIFE;
  }

  private void updateLRR(){
    lifeReductionRate = lifeReductionRate > MIN_LRR ? lifeReductionRate-3:lifeReductionRate;
    if(lifeReductionRate < MIN_LRR)
      lifeReductionRate = MIN_LRR;
  }

  //arbitrary method to decide which blender continues after merge
  private int decideDeath(Enemy_Blender b1, Enemy_Blender b2){
    if(b1.IND_RADIUS != b2.IND_RADIUS){
      return b1.IND_RADIUS < b2.IND_RADIUS ? 1 : 2;
    }
    if(b1.getPosition().x != b2.getPosition().x)
      return b1.getPosition().x > b2.getPosition().x ? 1 : 2;
    else if(b1.getPosition().y != b2.getPosition().y){
      return b1.getPosition().y > b2.getPosition().y ? 1 : 2;
    }
    else
      return 2;
  }


  private void updatePositionSeek(){
    position.add(velocity);

    Kinematic target = new Kinematic(targetPosition, null, 0, 0);
    SteeringOutput steering;
    steering = Seek.getSteering(this, target, MAX_LINEAR_ACC, RADIUS_SATISFACTION);
    if (steering.linear.mag() == 0) {
      velocity.set(0, 0);
      acceleration.set(0, 0);
      reached = true;
      return;
    }
    reached = false;
    velocity.add(steering.linear);
    if (velocity.mag() >= SEEK_MAX_VELOCITY)
      velocity.setMag(SEEK_MAX_VELOCITY);
    steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;
    orientation += steering.angular;

    if (DRAW_BREADCRUMBS)
      storeHistory();
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
