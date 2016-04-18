package com.lalaland.steering;

import com.lalaland.environment.Environment;
import com.lalaland.environment.Obstacle;
import com.lalaland.object.Kinematic;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.Random;

public class ObstacleSteering {

  private static final float FUTURE_RAY_VEL_BASE = 15f;

  public static boolean checkForObstacleAvoidance(Kinematic character, PApplet parent, Environment environment){
    PVector futureRay1 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), 2f));
    PVector futureRay2 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), 5f));
    PVector futureRay3 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * 2f));
    PVector futureRay4 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * 2f));

//    parent.ellipse(futureRay1.x, futureRay1.y, 2, 2);
//    parent.ellipse(futureRay2.x, futureRay2.y, 2, 2);
//    parent.ellipse(futureRay3.x, futureRay3.y, 2, 2);
//    parent.ellipse(futureRay4.x, futureRay4.y, 2, 2);

    return (
      environment.onObstacle(futureRay1) ||
      environment.onObstacle(futureRay2) ||
      environment.onObstacle(futureRay3) ||
      environment.onObstacle(futureRay4)
    );
  }

  public static float avoidObstacleOnWander(Kinematic character, PApplet parent, Environment environment) {
    float orient;
    Random random = new Random();
    float targetOrientation;
    do {
      orient = random.nextInt(180) - random.nextInt(180);
      targetOrientation = parent.radians(orient) + character.orientation;
    } while (checkForObstacleAvoidance(new Kinematic(character.position, PVector.fromAngle(targetOrientation), 0, 0), parent, environment));
    return targetOrientation;
  }

  public static SteeringOutput checkAndAvoidObstacle(Kinematic character, Environment environment, float steeringWeight, float rayOffset) {
    SteeringOutput steeringOutput = new SteeringOutput();

    PVector futureRay1 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), rayOffset));
    PVector futureRay2 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), rayOffset / 2f));
    PVector futureRay3 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f));
    PVector futureRay4 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * rayOffset / 2f));

    if (
      environment.onObstacle(futureRay1) ||
      environment.onObstacle(futureRay2) ||
      environment.onObstacle(futureRay3) ||
      environment.onObstacle(futureRay4)
    ) {
      Obstacle nearestObstacle = environment.getNearestObstacle(character.position, null);
      PVector left, right, up, down;
      left = new PVector(nearestObstacle.getCenterPosition().x - nearestObstacle.getSize().x / 2, nearestObstacle.getCenterPosition().y);
      right = new PVector(nearestObstacle.getCenterPosition().x + nearestObstacle.getSize().x / 2, nearestObstacle.getCenterPosition().y);
      up = new PVector(nearestObstacle.getCenterPosition().x, nearestObstacle.getCenterPosition().y - nearestObstacle.getSize().y / 2);
      down = new PVector(nearestObstacle.getCenterPosition().x, nearestObstacle.getCenterPosition().y + nearestObstacle.getSize().y / 2);
      float minimumDistance = 99999;
      PVector steeringLinear = new PVector();
      if (PVector.dist(left, character.position) < minimumDistance) {
        minimumDistance = PVector.dist(left, character.position);
        steeringLinear.x = -steeringWeight;
        steeringLinear.y = 0f;
      }
      if (PVector.dist(right, character.position) < minimumDistance) {
        minimumDistance = PVector.dist(right, character.position);
        steeringLinear.x = steeringWeight;
        steeringLinear.y = 0f;
      }
      if (PVector.dist(up, character.position) < minimumDistance) {
        minimumDistance = PVector.dist(up, character.position);
        steeringLinear.x = 0f;
        steeringLinear.y = -steeringWeight;
      }
      if (PVector.dist(down, character.position) < minimumDistance) {
        steeringLinear.x = 0f;
        steeringLinear.y = steeringWeight;
      }
      steeringOutput.linear.set(steeringLinear);
    }

    return steeringOutput;
  }

}
