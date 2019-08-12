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

import gui.App;

import static classes.Grid.SECTION_SIZE;
import static gui.App.GRAYSCALE_LEEWAY;

/**
 * A Block is a 2x2 pixel space that handles the 4 buildings it contains. The Block ensures no overlap occurs by
 * disabling buildings that would cause overlap.
 */
class Block {
    Building[][] buildings;
    private static final int NUM_MAX_BLOCK_TYPES = 6;
    private static final int NUM_MAX_STORIES = 10;
    private int blockType, numStories, elevation, x, y;
    private boolean isRoad;

    /**
     * Creates a Block from the given coordinates and PixelBunch. Sets the state of the Block based on coordinates.
     * @param b is the PixelBunch to build from.
     * @param x is the x coordinate for the first pixel in the Block.
     * @param y is the y coordinate for the first pixel in the Block.
     */
    Block(PixelBunch b, int x, int y) {
        this.x = x;
        this.y = y;
        init(b.getPixels()[x][y]);
        switch (blockType) {
            case 0:
                this.setBuildingParameters(0, 0, 0, 0);
                break;
            case 1:
                this.setBuildingParameters(1, -1, -1, -1);
                this.buildings[0][0].setAngle(180);
                break;
            case 2:
                this.setBuildingParameters(-1, -1, -1, 1);
                this.buildings[1][1].setAngle(0);
                break;
            case 3:
                this.setBuildingParameters(3, -1, -1, 3);
                this.buildings[0][0].setAngle(30);
                this.buildings[1][1].setAngle(210);
                break;
            case 4:
                this.setBuildingParameters(-1, 3, 3, -1);
                this.buildings[0][1].setAngle(-210);
                this.buildings[1][0].setAngle(-30);
                break;
            case 5:
                this.setBuildingParameters(2, -1, -1, -1);
                this.centerPentagonal();
                break;
        }
    }

    private void init(Pixel p) {
        this.buildings = new Building[2][2];
        this.blockType = (int) (p.getRed() / (256.0/ NUM_MAX_BLOCK_TYPES));
        this.numStories = (int) (p.getBlue() / (256.0/NUM_MAX_STORIES));
        this.elevation = p.getGreen() * 5;
        this.isRoad = (p.getBlue() < p.getGreen() + GRAYSCALE_LEEWAY && p.getBlue() > p.getGreen() - GRAYSCALE_LEEWAY)
                && (p.getRed() < p.getGreen() + GRAYSCALE_LEEWAY && p.getRed() > p.getGreen() - GRAYSCALE_LEEWAY)
                && (p.getBlue() < p.getRed() + GRAYSCALE_LEEWAY && p.getBlue() > p.getRed() - GRAYSCALE_LEEWAY);
    }

    private void setBuildingParameters(int i0, int i1, int i2, int i3) {
        double[] offset0;
        double[] offset1;
        double[] offset2;
        double[] offset3;
        switch (App.scale) {
            case 1:
                offset0 = new double[]{-1.35, -1.35};
                offset1 = new double[]{-1.35, 1.35};
                offset2 = new double[]{1.35, -1.35};
                offset3 = new double[]{1.35, 1.35};
                break;
            case 2:
                offset0 = new double[]{0.05, 0.05};
                offset1 = new double[]{0.05, 5.45};
                offset2 = new double[]{5.45, 0.05};
                offset3 = new double[]{5.45, 5.45};
                break;
            case 3:
                offset0 = new double[]{1.45, 1.45};
                offset1 = new double[]{1.45, 9.55};
                offset2 = new double[]{9.55, 1.45};
                offset3 = new double[]{9.55, 9.55};
                break;
            case 4:
                offset0 = new double[]{2.75, 2.75};
                offset1 = new double[]{2.75, 13.65};
                offset2 = new double[]{13.65, 2.75};
                offset3 = new double[]{13.65, 13.65};
                break;
            case 5:
                offset0 = new double[]{4.25, 4.25};
                offset1 = new double[]{4.25, 17.75};
                offset2 = new double[]{17.75, 4.25};
                offset3 = new double[]{17.75, 17.75};
                break;
            default:
                offset0 = new double[]{0, 0};
                offset1 = new double[]{0, 0};
                offset2 = new double[]{0, 0};
                offset3 = new double[]{0, 0};
                break;
        }
        this.buildings[0][0] = new Building(this.numStories, i0, this.x, this.y, this.elevation, i0 > -1, this.isRoad, offset0);
        this.buildings[0][1] = new Building(this.numStories, i1, this.x, this.y, this.elevation, i1 > -1, this.isRoad, offset1);
        this.buildings[1][0] = new Building(this.numStories, i2, this.x, this.y, this.elevation, i2 > -1, this.isRoad, offset2);
        this.buildings[1][1] = new Building(this.numStories, i3, this.x, this.y, this.elevation, i3 > -1, this.isRoad, offset3);
    }

    /**
     * Center the pentagonal building in the middle of the Block through setting the building's offset.
     *
     */
    private void centerPentagonal() {
        this.buildings[0][0].setOffset(new double[]{ (SECTION_SIZE*(App.scale-1))/2, (SECTION_SIZE*(App.scale-1))/2});
    }

    /**
     * Returns a requested building from the Block.
     * @param x is the x position of the building in the Block (0-1).
     * @param y is the y position of the building in the Block (0-1).
     * @return the building with the given (x, y) coordinates within the Block.
     */
    Building getBuilding(int x, int y) {
        return this.buildings[x][y];
    }
}