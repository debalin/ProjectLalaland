package com.lalaland.object;

import processing.core.*;
import com.lalaland.environment.*;
import com.lalaland.steering.*;

import java.util.*;

public class Enemy_MartyrLeader extends Enemy {

  private static final float LEADER_RADIUS = 12;
  private static final PVector LEADER_COLOR = new PVector(200, 45, 200);
  private static final int NUM_FOLLOWERS = 8;
  private static final int PURSUE_FORESIGHT = 5;
  private static final int[] RANK_PRIORITIES = {1, 0, 2, 7, 3, 4, 5, 6};

  private enum States {
    WAIT_FOR_FORMATION, PURSUE_PLAYER, ALERT_MARTYRS, LEADER_DEAD
  }

  private States state;
  List<Enemy_MartyrFollower> followers;

  private static int spawnCount = 0;
  Set<Integer> deadFollowers;
  public static int SPAWN_OFFSET, SPAWN_INTERVAL, SPAWN_MAX;

  public Enemy_MartyrLeader(float positionX, float positionY, PApplet parent, Environment environment) {
    super(positionX, positionY, parent, environment, LEADER_RADIUS, LEADER_COLOR.copy());
    followers = new ArrayList<>();
    deadFollowers = new HashSet<>();
    DRAW_BREADCRUMBS = false;
    TIME_TARGET_ROT = 10;
    RADIUS_SATISFACTION = 10;
    MAX_VELOCITY = 0.2f;
    MAX_ACCELERATION = 0.2f;
    targetPosition = new PVector(position.x, position.y);
    lifeReductionRate = 1;
    state = States.WAIT_FOR_FORMATION;
    spawnCount++;

    for (int i = 0; i < NUM_FOLLOWERS; i++)
      followers.add(new Enemy_MartyrFollower(positionX, positionY, parent, environment, this, i));
  }

  public static int getSpawnCount() {
    return spawnCount;
  }

  public static void initializeSpawnDetails(int frameRate) {
    SPAWN_OFFSET = frameRate * 10;
    SPAWN_INTERVAL = frameRate * 2000;
    SPAWN_MAX = 1;
  }

  @Override
  public void move() {
    updateLife();

    switch (state) {
      case WAIT_FOR_FORMATION:
        if (formationSuccessful())
          updateState(States.PURSUE_PLAYER);
        break;
      case PURSUE_PLAYER:
        targetPosition.x = environment.getPlayer().getPosition().x + environment.getPlayer().getVelocity().x * PURSUE_FORESIGHT;
        targetPosition.y = environment.getPlayer().getPosition().y + environment.getPlayer().getVelocity().y * PURSUE_FORESIGHT;
        break;
      case ALERT_MARTYRS:
        adjustFollowerRanks();
        updateState(States.WAIT_FOR_FORMATION);
        break;
      case LEADER_DEAD:
        if (allFollowersDead()) {
          alive = false;
          spawnCount--;
        }
        else
          followers.forEach(follower -> follower.setState(Enemy_MartyrFollower.States.GO_BERSERK));
    }

    if (state == States.PURSUE_PLAYER)
      updatePosition();

    updateFollowers();
  }

  private void adjustFollowerRanks() {
    for (int x = 0; x <= RANK_PRIORITIES.length - 1; x++) {
      if (deadFollowers.contains(RANK_PRIORITIES[x])) {
        for (int y = RANK_PRIORITIES.length - 1; y > x; y--) {
          if (!deadFollowers.contains(RANK_PRIORITIES[y])) {
            int index = followers.indexOf(new Enemy_MartyrFollower(0, 0, null, null, null, RANK_PRIORITIES[y]));
            Enemy_MartyrFollower follower = followers.get(index);
            follower.setRank(RANK_PRIORITIES[x]);
            deadFollowers.remove(RANK_PRIORITIES[x]);
            deadFollowers.add(RANK_PRIORITIES[y]);
            break;
          }
        }
      }
    }
    followers.forEach(follower -> follower.setState(Enemy_MartyrFollower.States.UPDATE_FORMATION));
  }

  private boolean formationSuccessful() {
    Iterator<Enemy_MartyrFollower> i = followers.iterator();
    int currentNumFollowers = followers.size();
    while (i.hasNext()) {
      Enemy_MartyrFollower follower = i.next();
      if (follower.getState() == Enemy_MartyrFollower.States.FORMATION_READY) {
        currentNumFollowers--;
      }
    }
    if (currentNumFollowers <= 0) {
      followers.forEach(follower -> follower.setState(Enemy_MartyrFollower.States.FOLLOW_FORMATION));
      return true;
    }
    return false;
  }

  private void updateFollowers() {
    int previousDeadFollowerCount = deadFollowers.size();

    Iterator<Enemy_MartyrFollower> i = followers.iterator();
    while (i.hasNext()) {
      Enemy_MartyrFollower follower = i.next();
      if (follower.isAlive())
        follower.move();
      else {
        deadFollowers.add(follower.getRank());
        i.remove();
      }
    }

    if (deadFollowers.size() > previousDeadFollowerCount) {
      updateState(States.ALERT_MARTYRS);
    }
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
    if (life <= LIFE_THRESHOLD) {
      updateState(States.LEADER_DEAD);
    }
  }

  private void updatePosition() {
    position.add(velocity);

    Kinematic target = new Kinematic(targetPosition, null, 0, 0);
    SteeringOutput steering;

    steering = Seek.getSteering(this, target, MAX_ACCELERATION, RADIUS_SATISFACTION);
    if (steering.linear.mag() == 0) {
      velocity.set(0, 0);
      acceleration.set(0, 0);
      reached = true;
      return;
    }
    reached = false;
    velocity.add(steering.linear);
    if (velocity.mag() >= MAX_VELOCITY)
      velocity.setMag(MAX_VELOCITY);

    steering.angular = LookWhereYoureGoing.getSteering(this, target, TIME_TARGET_ROT).angular;
    orientation += steering.angular;

    if (DRAW_BREADCRUMBS)
      storeHistory();
  }

  @Override
  public void display() {
    followers.forEach(follower -> follower.display());
    if(state != States.LEADER_DEAD)
      super.display();
  }

  private boolean allFollowersDead() {
    if (followers.size() == 0)
      return true;
    return false;
  }

}
