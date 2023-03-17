package com.huawei.codecraft.helper;

import java.util.ArrayList;
import java.util.List;

import com.huawei.codecraft.entity.Robot;
import com.huawei.codecraft.math.Line;
import com.huawei.codecraft.math.Vector2;

public class LinearProgramHelper {

    public static final double RVO_EPSILON = 0.00001;
    public static final double MAX_SPEED = Robot.MAX_FORWARD_SPEED;
    public static Vector2 newVelocity;

    /**
     * Solves a one-dimensional linear program on a specified line subject to linear
     * constraints
     * defined by lines and a circular constraint.
     *
     * @param lines                Lines defining the linear constraints.
     * @param lineNo               The specified line constraint.
     * @param optimizationVelocity The optimization velocity.
     * @param optimizeDirection    True if the direction should be optimized.
     * @return True if successful.
     */
    public static boolean linearProgram1(
            List<Line> lines, int lineNo, Vector2 optimizationVelocity, boolean optimizeDirection) {
        final double dotProduct = lines.get(lineNo).point.dot(lines.get(lineNo).dir);
        final double discriminant = dotProduct * dotProduct + MAX_SPEED * MAX_SPEED - lines.get(lineNo).point.length2();

        if (discriminant < 0.0) {
            // Max speed circle fully invalidates line lineNo.
            return false;
        }

        final double sqrtDiscriminant = Math.sqrt(discriminant);
        double tLeft = -sqrtDiscriminant - dotProduct;
        double tRight = sqrtDiscriminant - dotProduct;

        for (int i = 0; i < lineNo; i++) {
            final double denominator = Vector2.det(lines.get(lineNo).dir, lines.get(i).dir);
            final double numerator = Vector2.det(lines.get(i).dir,
                    lines.get(lineNo).point.subtract(lines.get(i).point));

            if (Math.abs(denominator) <= RVO_EPSILON) {
                // Lines lineNo and i are (almost) parallel.
                if (numerator < 0.0) {
                    return false;
                }

                continue;
            }

            final double t = numerator / denominator;

            if (denominator >= 0.0) {
                // Line i bounds line lineNo on the right.
                tRight = Math.min(tRight, t);
            } else {
                // Line i bounds line lineNo on the left.
                tLeft = Math.max(tLeft, t);
            }

            if (tLeft > tRight) {
                return false;
            }
        }

        if (optimizeDirection) {
            // Optimize direction.
            if (optimizationVelocity.dot(lines.get(lineNo).dir) > 0.0) {
                // Take right extreme.
                newVelocity = lines.get(lineNo).point.add(tRight, lines.get(lineNo).dir);
            } else {
                // Take left extreme.
                newVelocity = lines.get(lineNo).point.add(tLeft, lines.get(lineNo).dir);
            }
        } else {
            // Optimize closest point.
            final double t = lines
                    .get(lineNo).dir
                    .dot(optimizationVelocity.subtract(lines.get(lineNo).point));

            if (t < tLeft) {
                newVelocity = lines.get(lineNo).point.add(tLeft, lines.get(lineNo).dir);
            } else if (t > tRight) {
                newVelocity = lines.get(lineNo).point.add(tRight, lines.get(lineNo).dir);
            } else {
                newVelocity = lines.get(lineNo).point.add(t, lines.get(lineNo).dir);
            }
        }

        return true;
    }

    /**
     * Solves a two-dimensional linear program subject to linear constraints defined
     * by lines and a
     * circular constraint.
     *
     * @param lines                Lines defining the linear constraints.
     * @param optimizationVelocity The optimization velocity.
     * @param optimizeDirection    True if the direction should be optimized.
     * @return The number of the line on which it fails, or the number of lines if
     *         successful.
     */
    public static int linearProgram2(
            List<Line> lines, Vector2 optimizationVelocity, boolean optimizeDirection) {
        if (optimizeDirection) {
            // Optimize direction. Note that the optimization velocity is of unit length in
            // this case.
            newVelocity = optimizationVelocity.multiply(MAX_SPEED);
        } else if (optimizationVelocity.length2() > MAX_SPEED * MAX_SPEED) {
            // Optimize closest point and outside circle.
            newVelocity = optimizationVelocity.normalize().multiply(MAX_SPEED);
        } else {
            // Optimize closest point and inside circle.
            newVelocity = optimizationVelocity;
        }

        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
            if (Vector2.det(lines.get(lineNo).dir, lines.get(lineNo).point.subtract(newVelocity)) > 0.0) {
                // Result does not satisfy constraint i. Compute new optimal
                // result.
                final Vector2 tempResult = newVelocity;
                if (!linearProgram1(lines, lineNo, optimizationVelocity, optimizeDirection)) {
                    newVelocity = tempResult;

                    return lineNo;
                }
            }
        }

        return lines.size();
    }

    /**
     * Solves a two-dimensional linear program subject to linear constraints defined
     * by lines and a
     * circular constraint.
     *
     * @param numObstacleLines Count of obstacle lines.
     * @param beginLine        The line on which the 2-D linear program failed.
     */
    public static void linearProgram3(List<Line> lines, int numObstacleLines, int beginLine) {
        double distance = 0.0;

        for (int i = beginLine; i < lines.size(); i++) {
            if (Vector2.det(lines.get(i).dir, lines.get(i).point.subtract(newVelocity)) > distance) {
                // Result does not satisfy constraint of line i.
                final List<Line> projectedLines = new ArrayList<>(numObstacleLines);
                for (int j = 0; j < numObstacleLines; j++) {
                    projectedLines.add(lines.get(j));
                }

                for (int j = numObstacleLines; j < i; j++) {
                    final double determinant = Vector2.det(lines.get(i).dir, lines.get(j).dir);
                    final Vector2 point;

                    if (Math.abs(determinant) <= RVO_EPSILON) {
                        // Line i and line j are parallel.
                        if (lines.get(i).dir.dot(lines.get(j).dir) > 0.0) {
                            // Line i and line j point in the same direction.
                            continue;
                        }

                        // Line i and line j point in opposite direction.
                        point = lines.get(i).point.add(lines.get(j).point).multiply(0.5);
                    } else {
                        point = lines
                                .get(i).point
                                .add(
                                        lines
                                                .get(i).dir
                                                .multiply(
                                                        Vector2.det(
                                                                lines.get(j).dir,
                                                                lines.get(i).point.subtract(lines.get(j).point))
                                                                / determinant));
                    }

                    final Vector2 direction = lines.get(j).dir.subtract(lines.get(i).dir).normalize();
                    projectedLines.add(new Line(point, direction));
                }

                final Vector2 tempResult = newVelocity;
                if (linearProgram2(
                        projectedLines,
                        new Vector2(-lines.get(i).dir.y, lines.get(i).dir.x),
                        true) < projectedLines.size()) {
                    // This should in principle not happen. The result is by
                    // definition already in the feasible region of this linear
                    // program. If it fails, it is due to small floating point
                    // error, and the current result is kept.
                    newVelocity = tempResult;
                }

                distance = Vector2.det(lines.get(i).dir, lines.get(i).point.subtract(newVelocity));
            }
        }
    }

}