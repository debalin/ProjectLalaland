package com.lalaland.steering;

import com.lalaland.object.Kinematic;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

import java.util.Random;

public class BoundarySteering {

  private static final float FUTURE_RAY_VEL_BASE = 15f;

  public static boolean checkForBoundaryAvoidance(Kinematic character, PApplet parent, int BORDER_PADDING){
    PVector futureRay1 = PVector.add(character.position, PVector.mult(character.velocity.copy().setMag(FUTURE_RAY_VEL_BASE), 3f));
    PVector futureRay2 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() - PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * 2f));
    PVector futureRay3 = PVector.add(character.position, PVector.fromAngle(character.velocity.heading() + PConstants.PI / 4f).setMag(FUTURE_RAY_VEL_BASE * 2f));

    PVector boundary = new PVector(parent.width, parent.height);
    return (
      checkRayWithBoundary(futureRay1, BORDER_PADDING, boundary) ||
      checkRayWithBoundary(futureRay2, BORDER_PADDING, boundary) ||
      checkRayWithBoundary(futureRay3, BORDER_PADDING, boundary)
    );
  }

  private static boolean checkRayWithBoundary(PVector futureRay, int BORDER_PADDING, PVector boundary) {
    return (
      futureRay.x <= BORDER_PADDING ||
      futureRay.x >= boundary.x - BORDER_PADDING ||
      futureRay.y <= BORDER_PADDING ||
      futureRay.y >= boundary.y - BORDER_PADDING
    );
  }

  public static float avoidBoundaryOnWander(Kinematic character, PApplet parent, int BORDER_PADDING){
    float orient, targetOrientation;
    Random random = new Random();
    do {
      orient = random.nextInt(180) - random.nextInt(180);
      targetOrientation = parent.radians(orient) +  character.orientation;
    } while(checkForBoundaryAvoidance(new Kinematic(character.position, PVector.fromAngle(targetOrientation), 0, 0), parent, BORDER_PADDING));
    return targetOrientation;
  }

}
