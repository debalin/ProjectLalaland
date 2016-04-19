package com.lalaland.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lalaland.environment.Environment;
import com.lalaland.steering.*;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Enemy_MartyrFollower extends Enemy {

  private static final float FOLLOWER_RADIUS = 7;
  private static final PVector FOLLOWER_COLOR = new PVector(186, 250, 99);
  private static final float ALIGNMENT_THRESHOLD = 50f;
  private static final float SEPARATION_OFFSET = 50f;
  private static final float MAX_VELOCITY_FORMATION = 0.8f;

  public enum States {
    UPDATE_FORMATION, FORMATION_READY, FOLLOW_FORMATION, GO_BERSERK
  }

  private States state;
  private Enemy_MartyrLeader leader;
  private int rank;
  private boolean DYNAMIC_FORMATION;

  public Enemy_MartyrFollower(float positionX, float positionY, PApplet parent, Environment environment, Enemy_MartyrLeader leader, int rank, boolean DYNAMIC_FORMATION) {
    super(positionX, positionY, parent, environment, FOLLOWER_RADIUS, FOLLOWER_COLOR.copy());
    this.leader = leader;
    this.rank = rank;
    SEPARATION_THRESHOLD = 20f;
    this.DYNAMIC_FORMATION = DYNAMIC_FORMATION;
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 60;
    RADIUS_SATISFACTION = 1.5f;
    MAX_VELOCITY = 0.5f;
    MAX_ACCELERATION = 0.3f;
    targetPosition = new PVector();
    if (!DYNAMIC_FORMATION && leader != null)
      updateTargetFromRank();
    lifeReductionRate = 5;
    state = States.UPDATE_FORMATION;
  }

  @Override
  public boolean equals(Object o) {
    Enemy_MartyrFollower follower = (Enemy_MartyrFollower)o;
    if (follower.getRank() == rank)
      return true;
    return false;
  }

  public int getRank() {
    return rank;
  }

  public void setRank(int rank) {
    this.rank = rank;
  }

  private void updateTargetFromRank() {
    PVector separationVector;
    if (!DYNAMIC_FORMATION) {
      if (rank % 2 == 0)
        separationVector = PVector.fromAngle(leader.getOrientation() - PConstants.PI / 4f + PConstants.PI / 4f * rank).setMag(SEPARATION_OFFSET);
      else
        separationVector = PVector.fromAngle(leader.getOrientation() - PConstants.PI / 4f + PConstants.PI / 4f * rank).setMag(SEPARATION_OFFSET - 3);
    }
    else {
      separationVector = PVector.fromAngle(leader.getOrientation() + 2 * PConstants.PI * rank / leader.getNumFollowers()).setMag(SEPARATION_OFFSET);
    }
    targetPosition.x = leader.getPosition().x + separationVector.x;
    targetPosition.y = leader.getPosition().y + separationVector.y;
  }

  public States getState() {
    return state;
  }

  public void setState(States state) {
    this.state = state;
  }

  @Override
  public void move() {
    updateLife();
    updateTargetFromRank();

    switch (state) {
      case UPDATE_FORMATION:
        break;
      case FORMATION_READY:
        break;
      case FOLLOW_FORMATION:
        break;
    }

    if (state != States.FORMATION_READY)
      updatePosition();
  }

  private void updateState(States state) {
    this.state = state;
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
    if (life <= LIFE_THRESHOLD)
      alive = false;
    checkAndReducePlayerLife();
  }

  private List<PVector> buildAvoidanceRays() {
    List<PVector> futureRays = new ArrayList<>();

    futureRays.add(PVector.add(position, PVector.fromAngle(orientation).setMag(30f)));
    futureRays.add(PVector.add(position, PVector.fromAngle(orientation - PConstants.PI / 4f).setMag(15f)));
    futureRays.add(PVector.add(position, PVector.fromAngle(orientation + PConstants.PI / 4f).setMag(15f)));

    futureRays.forEach(futureRay -> parent.ellipse(futureRay.x, futureRay.y, 5, 5));

    return futureRays;
  }

  private void updatePosition() {
    position.add(velocity);

    boolean onObstacle = ObstacleSteering.checkForObstacleAvoidance(this, parent, environment, 5);;
    if (onObstacle) {
      targetPosition.set(ObstacleSteering.avoidObstacleOnSeek(this, environment, 5));
    }

    Kinematic target = new Kinematic(targetPosition, null, 0, 0);
    SteeringOutput steering;
    List<Enemy> followers = new ArrayList<>(leader.followers);

    steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
    if (state == States.UPDATE_FORMATION && steering.linear.x == 0f && steering.linear.y == 0f) {
      updateState(States.FORMATION_READY);
      return;
    }
    List<Kinematic> flockers = new ArrayList<>();
    flockers.addAll(followers);
    flockers.add(leader);
    PVector alignment = Alignment.getSteering(this, flockers, MAX_ACCELERATION, ALIGNMENT_THRESHOLD).linear.mult(0.6f);
    PVector separation = Separation.getSteering(this, flockers, MAX_ACCELERATION, SEPARATION_THRESHOLD).linear.mult(0.8f);
    PVector cohesion = Cohesion.getSteering(this, flockers, MAX_ACCELERATION).linear.mult(0.2f);
    steering.linear.add(alignment);
    steering.linear.add(separation);
    steering.linear.add(cohesion);
    steering.linear.setMag(MAX_ACCELERATION);
    velocity.add(steering.linear);
    if (state == States.UPDATE_FORMATION) {
      if (velocity.mag() >= MAX_VELOCITY_FORMATION)
        velocity.setMag(MAX_VELOCITY_FORMATION);
    }
    else {
      if (velocity.mag() >= MAX_VELOCITY)
        velocity.setMag(MAX_VELOCITY);
    }

    if (!DYNAMIC_FORMATION)
      steering.angular = Align.getSteering(this, leader, TIME_TARGET_ROT).angular;
    else {
      steering.angular = (leader.getOrientation() + 2 * PConstants.PI * rank / leader.getNumFollowers()) - orientation;
      steering.angular = mapToRange(steering.angular) / TIME_TARGET_ROT;
    }
    orientation += steering.angular;

    if (DRAW_BREADCRUMBS)
      storeHistory();
  }

}
