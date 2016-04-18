package com.lalaland.steering;

import com.lalaland.environment.Environment;
import com.lalaland.object.Kinematic;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.Random;

public class ObstacleSteering {

  private static final float FUTURE_RAY_VEL_BASE = 15f;

  public static boolean checkForObstacleAvoidance(Kinematic character, PApplet parent, Environment environment){
    PVector futureRay1 = PVector.add(character.position, PVector.mult(PVector.fromAngle(character.orientation).setMag(FUTURE_RAY_VEL_BASE), 2f));
    PVector futureRay2 = PVector.add(character.position, PVector.mult(PVector.fromAngle(character.orientation).setMag(FUTURE_RAY_VEL_BASE), 5f));
    PVector futureRay3 = PVector.add(character.position, PVector.fromAngle(character.orientation - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * 2f));
    PVector futureRay4 = PVector.add(character.position, PVector.fromAngle(character.orientation + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * 2f));

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
    } while (checkForObstacleAvoidance(new Kinematic(character.position, null, targetOrientation, 0), parent, environment));
    return targetOrientation;
  }

}
