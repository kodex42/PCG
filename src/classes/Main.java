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

import Jcg.geometry.Point_3;
import Jcg.triangulations2D.TriangulationDS_2;
import gui.App;

import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static Jcg.mesh.Triangulations_IO.getTriangleMeshFromFile;
import static classes.Grid.*;

/**
 * A program that, from an existing library of OFF files representing buildings of various shapes and sizes, generates
 * a city based on a top-down perspective map given as input in a bitmap image file.
 */
public class Main {
    private static int MAX_CHUNK_DIMENSION = 16;

    public static void main(String[] args) { generate(); }

    /**
     * Main generator function. Called directly from UI.
     */
    public static void generate() {
        ArrayList<BufferedImage> imageChunks = new ArrayList<>();
        BufferedImage image = App.inputImage;
        MAX_CHUNK_DIMENSION *= App.scale;
        // Seperate the image into chunks for more efficient rendering
        if (image.getHeight() > MAX_CHUNK_DIMENSION && image.getWidth() > MAX_CHUNK_DIMENSION)
            for (int x = 0; x < image.getWidth(); x += MAX_CHUNK_DIMENSION) {
                int width;
                if (image.getWidth() - x < MAX_CHUNK_DIMENSION)
                    width = image.getWidth() - x;
                else
                    width = MAX_CHUNK_DIMENSION;
                for (int y = 0; y < image.getHeight(); y += MAX_CHUNK_DIMENSION) {
                    int height;
                    if (image.getHeight() - y < MAX_CHUNK_DIMENSION)
                        height = image.getHeight() - y;
                    else
                        height = MAX_CHUNK_DIMENSION;
                    imageChunks.add(image.getSubimage(x, y, width, height));
                }
            }
        else
            imageChunks.add(image);

        // Render chunks
        for (int num = 0; num < imageChunks.size(); num++) {
            BufferedImage chunk = imageChunks.get(num);
            print("BEGINNING CHUNK RENDER: (" + num + ")");

            // Build the pixel bunch
            PixelBunch pixelBunch = new PixelBunch(chunk);
            if (!pixelBunch.isInitialized()) {
                print("Input Error: Image could not be read properly.");
                return;
            }
            // Declare objects
            Grid grid = new Grid(pixelBunch);
            TriangulationDS_2<Point_3> importPoly;
            Block[][] blocks = grid.getBlocks();
            // For every pixel in each block
            for (int i = 0; i < blocks.length; i++) {
                for (int k = 0; k < blocks[i].length; k++) {
                    print("(x" + i + ", y:" + k + ")");
                    for (int x = 0; x < blocks[i][k].buildings.length; x++) {
                        for (int y = 0; y < blocks[i][k].buildings[x].length; y++) {
                            print("Reading pixel data");
                            Building building = blocks[i][k].getBuilding(x, y);
                            print("Check building availability");
                            if (building.isEnabled()) {
                                print("Importing mesh");
                                importPoly = getTriangleMeshFromFile(building.getFileName());
                                print("Scale the building down to unit size");
                                scale(importPoly, 0.005);
                                building.setZ(building.getZ() * 0.005);
                                print("Scale the building back up to the appropriate level");
                                scale(importPoly, App.scale);
                                print("Normalize the rotation of the building");
                                normalizeRotation(importPoly, building.getShapeType());
                                print("Rotate the requested angle");
                                rotate(importPoly, building.getAngle());
                                print("Translating mesh");
                                translate(importPoly, building.getX(), building.getY(), building.getZ(), building.getOffset());
                                print("Combining mesh");
                                grid.append(importPoly);
                            } else {
                                print("Space restricted, moving to next building");
                            }
                        }
                    }
                }
            }
            // Output the polyhedron to an OFF mesh
            grid.output(num);
        }
        print("Finished!");
    }

    /**
     * Sets the base rotation of each building such that all buildings have their doors facing the same direction
     * (north) via the rotate method.
     * 3 sided buildings must be rotated 120 degrees.
     * 5 sided buildings must be rotated 72 degrees twice (144 degrees).
     * @param triangulationDS2 is the building to normalize.
     * @param shapeType determines the angle of rotation required for normalization.
     */
    private static void normalizeRotation(TriangulationDS_2<Point_3> triangulationDS2, int shapeType) {
        switch (shapeType) {
            case 0:
            case 1:
                rotate(triangulationDS2, 90);
                break;
            case 2:
                rotate(triangulationDS2, 234);
                break;
            case 3:
                rotate(triangulationDS2, 210);
                break;
            default:
                break;
        }
    }

    /**
     * A simple println method that prefixes output with a timestamp.
     * @param str is the string to output.
     */
    public static void print(String str) {
        System.out.println(new SimpleDateFormat("HH.mm.ss").format(new Date()) + "    " + str);
    }
    public static void print(int i) {
        print(String.valueOf(i));
    }
}
