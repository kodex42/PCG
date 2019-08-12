/**
 *     Copyright (C) 2019 Matthew Buchanan
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package classes;

import java.util.Random;

/**
 * The Building class contains all the necessary parameter data for its corresponding building.
 */
class Building {
    private int numStories, shapeType, x, y;
    private double z;
    private int angle = 0;
    private double[] offset = {0, 0};
    private boolean enabled, isRoad;

    public Building(int numStories, int shapeType, int x, int y, double z, boolean enabled, boolean isRoad, double[] offset) {
        this.numStories = numStories;
        this.shapeType = shapeType;
        this.x = x;
        this.y = y;
        this.z = z;
        this.enabled = enabled;
        this.isRoad = isRoad;
        this.offset = offset;
    }

    /**
     * Creates a Building object based on pixel data without alpha.
     * @param red is the red value of the pixel.
     * @param green is the green value of the pixel.
     * @param blue is the blue value of the pixel.
     * @param x is the x coordinate of the pixel.
     * @param y is the y coordinate of the pixel.
     */
    Building(int red, int green, int blue, int x, int y) {
        int varietyLvl = red / 64;
        this.numStories = blue / 26;
        this.z = green * 2;
        this.shapeType = new Random().nextInt(varietyLvl + 1);
        this.x = x;
        this.y = y;
        this.enabled = true;
        this.isRoad = (red == green && red == blue);
    }

    /**
     * Creates a Building object based on pixel data with alpha.
     * @param red is the red value of the pixel.
     * @param green is the green value of the pixel.
     * @param blue is the blue value of the pixel.
     * @param alpha is the alpha value of the pixel.
     * @param x is the x coordinate of the pixel.
     * @param y is the y coordinate of the pixel.
     */
    Building(int red, int green, int blue, int alpha, int x, int y) {
        int varietyLvl = red / 64;
        this.numStories = blue / 26;
        this.z = green * 2;
        this.shapeType = new Random().nextInt(varietyLvl + 1);
        this.x = x;
        this.y = y;
        this.enabled = alpha != 0;
        this.isRoad = (red == green && red == blue);
    }

    int getShapeType() {
        return shapeType;
    }

    int getX() {
        return this.x;
    }

    int getY() {
        return this.y;
    }

    double getZ() {
        return this.z;
    }

    int getAngle() {
        return angle;
    }

    double[] getOffset() {
        return this.offset;
    }

    void disable() {
        this.enabled = false;
    }

    boolean isEnabled() {
        return enabled && !isRoad;
    }

    String getFileName() {
        return "meshes/library-clean/CGAL_mesh_" + this.shapeType + "_" + this.numStories + ".off";
    }

    void setAngle(int i) {
        this.angle = i;
    }

    void setOffset(double[] offset) {
        this.offset = offset;
    }

    void setShapeType(int i) {
        if (i < 0) {
            this.enabled = false;
            this.shapeType = 0;
        }
        else
            this.shapeType = i;
    }

    public void setNumStories(int numStories) {
        this.numStories = numStories;
    }

    public void setCoords(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void setZ(double z) {
        this.z = z;
    }
}
