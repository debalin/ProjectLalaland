package com.lalaland.object;

import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;

import com.lalaland.steering.KinematicOutput;
import com.lalaland.steering.Wander;
import com.lalaland.utility.Utility;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_Grunt extends Enemy {

	private static final float GRUNT_RADIUS = 7;
	private static final PVector GRUNT_COLOR = new PVector(153, 51, 51);
	private static final int LIFE_THRESHOLD = 5;
  private static final int RANDOMISER_INTERVAL = 150;
  private static final float TRACK_TOTAL_TIME = 5000f;
  private static final float DIRECTED_TOTAL_TIME = 7000f;
  private static final float TRACK_CONE_RANGE = PConstants.PI / 3f;
  private static final float REACH_RADIUS = 20f;

  private static int spawnCount = 0;
  private States state;
  private Wander wander;
  public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;
  private float trackStartTime;
  private float directedStartTime;
  private PVector playerAveragePosition;
  private int playerTrackCount;
  private float randomAngle;

  private int nearbyTime, nearbyTimeTemp;

  private enum States {
    TRACKING_WANDER, DIRECTED_WANDER, WANDER
  }
	
	public Enemy_Grunt(float positionX, float positionY, PApplet parent, Environment environment) {
		super(positionX, positionY, parent, environment, GRUNT_RADIUS, GRUNT_COLOR);
		DRAW_BREADCRUMBS = false;
		MAX_VELOCITY = 1.0f;
		lifeReductionRate = 5;
    DAMAGE_RADIUS = 30f;
    PLAYER_DAMAGE = 2.5f;
    spawnCount++;
    state = States.TRACKING_WANDER;
    wander = new Wander(RANDOMISER_INTERVAL);
    trackStartTime = 0f;
    directedStartTime = 0f;
    playerAveragePosition = new PVector();
    playerTrackCount = 0;
    randomAngle = 0f;
    nearbyTimeTemp = nearbyTime = 0;
	}

	@Override
	public void move() {		
		updateLife();

    switch (state) {
      case TRACKING_WANDER:
        if (trackStartTime == 0f)
          trackStartTime = parent.millis();
        if (parent.millis() - trackStartTime < TRACK_TOTAL_TIME) {
          trackPlayer();
          updatePositionWander();
        }
        else {
          playerAveragePosition.div(playerTrackCount);
          randomAngle = Utility.randomBinomial() * TRACK_CONE_RANGE;
          playerTrackCount = 0;
          trackStartTime = 0f;
          updateState(States.DIRECTED_WANDER);
        }
        break;
      case DIRECTED_WANDER:
        if (directedStartTime == 0f)
          directedStartTime = parent.millis();
        if (parent.millis() - directedStartTime < DIRECTED_TOTAL_TIME && !reachedPlayerNeighbourhood()) {
          moveInPlayerNeighbourhood();
          //drawAveragePosition();
        }
        else {
          playerAveragePosition.set(0, 0);
          directedStartTime = 0f;
          updateState(States.TRACKING_WANDER);
        }
        break;
      case WANDER:
        updatePositionWander();
        break;
    }

    evaluateMetrics();
	}

  private void evaluateMetrics() {
    if (position.dist(environment.getPlayer().getPosition()) < 200 && nearbyTimeTemp == 0) {
      nearbyTimeTemp = parent.millis();
    }
    else if (position.dist(environment.getPlayer().getPosition()) >= 200 && nearbyTimeTemp > 0) {
      nearbyTime += parent.millis() - nearbyTimeTemp;
      nearbyTimeTemp = 0;
    }
  }

  private boolean reachedPlayerNeighbourhood() {
    if (position.dist(playerAveragePosition) <= REACH_RADIUS)
      return true;
    return false;
  }

  @SuppressWarnings("unused")
	private void drawAveragePosition() {
    parent.pushMatrix();
    parent.fill(255, 255, 255);
    parent.ellipse(playerAveragePosition.x, playerAveragePosition.y, 5, 5);
    parent.popMatrix();
  }

  private void updateState(Enemy_Grunt.States state) {
    this.state = state;
  }

  private void trackPlayer() {
    PVector playerCurrentPosition = environment.getPlayer().getPosition();
    playerAveragePosition.add(playerCurrentPosition);
    playerTrackCount++;
  }

  private void moveInPlayerNeighbourhood() {
    KinematicOutput kinematic = wander.getOrientationMatchingSteering(this, environment, parent, BORDER_PADDING, MAX_VELOCITY, playerAveragePosition, randomAngle, 5f);
    orientation += kinematic.rotation;
    velocity.set(kinematic.velocity);
    position.add(velocity);
  }

  public static int getSpawnCount() {
    return spawnCount;
  }

  public static void initializeSpawnDetails(int frameRate) {
    SPAWN_OFFSET = frameRate * 2;
    SPAWN_INTERVAL = frameRate * 20;
    SPAWN_MAX = 4;
  }
	
	private void updatePositionWander(){
    KinematicOutput kinematic = wander.getOrientationMatchingSteering(this, environment, parent, BORDER_PADDING, MAX_VELOCITY, 5f);
    orientation += kinematic.rotation;
    velocity.set(kinematic.velocity);
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
      killYourself(false);
      //printMetrics();
      spawnCount--;
    }
    checkAndReducePlayerLife();
  }

  private void printMetrics() {
    System.out.println("Time spent near player: " + nearbyTime);
    System.out.println("Efficiency: " + nearbyTime / survivalTime * 100);
  }

}
