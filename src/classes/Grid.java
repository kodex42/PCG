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
import Jcg.mesh.SharedVertexRepresentation;
import Jcg.triangulations2D.TriangulationDSFace_2;
import Jcg.triangulations2D.TriangulationDSVertex_2;
import Jcg.triangulations2D.TriangulationDS_2;
import gui.App;

import java.io.IOException;

/**
 * The Grid class is constructed alongside the buildings. This class generates blocks before buildings are placed so
 * that proper spacing and overlap is handled in advanced. The Grid class also handles any transformations applied to
 * buildings and other triangulations.
 */
class Grid {
    private static final String OUT_FILE = "city_chunk_";
    private PixelBunch bunch;
    private Block[][] blocks;
    private TriangulationDS_2<Point_3> triGrid;
    private TriangulationDS_2<Point_3> triCity;
    static final double SECTION_SIZE = 5.5;
    private int width, height;

    Grid(PixelBunch bunch) {
        this.bunch = bunch;
        this.width = bunch.getWidth();
        this.height = bunch.getHeight();
        this.triGrid = new TriangulationDS_2<>();
        this.triCity = new TriangulationDS_2<>();
        initBlocks();
        //constructTriGridBlocks();
        constructTriGridPixels();
    }

    Block[][] getBlocks() {
        return this.blocks;
    }

    /**
     * Initializes the Block array with Blocks of buildings.
     */
    private void initBlocks() {
        this.blocks = new Block[(int) Math.ceil((double)this.bunch.getWidth()/App.scale)][(int) Math.ceil((double)this.bunch.getHeight()/App.scale)];
        for (int i = 0; i < this.bunch.getWidth(); i+=App.scale) {
            for (int k = 0; k < this.bunch.getHeight(); k+=App.scale) {
                this.blocks[i/App.scale][k/App.scale] = new Block(this.bunch, i, k);
            }
        }
    }

    /**
     * Constructs a grid of triangles such that each 2-triangle square in the grid represents a space where a Block is.
     */
    private void constructTriGridBlocks() {
        for (int i = 0; i < this.blocks.length; i++) {
            for (int k = 0; k < this.blocks[i].length; k++) {
                TriangulationDSVertex_2 v1 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSVertex_2 v2 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSVertex_2 v3 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSVertex_2 v4 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSFace_2 f1;
                TriangulationDSFace_2 f2;

                v1.setPoint(new Point_3(i*SECTION_SIZE*2 - (SECTION_SIZE/2), k*SECTION_SIZE*2 - (SECTION_SIZE/2), -1));
                v2.setPoint(new Point_3((i+1)*SECTION_SIZE*2 - (SECTION_SIZE/2), k*SECTION_SIZE*2 - (SECTION_SIZE/2), -1));
                v3.setPoint(new Point_3((i+1)*SECTION_SIZE*2 - (SECTION_SIZE/2), (k+1)*SECTION_SIZE*2 - (SECTION_SIZE/2), -1));
                v4.setPoint(new Point_3(i*SECTION_SIZE*2 - (SECTION_SIZE/2), (k+1)*SECTION_SIZE*2 - (SECTION_SIZE/2), -1));

                f1 = new TriangulationDSFace_2(v1, v2, v3, null, null, null);
                f2 = new TriangulationDSFace_2(v1, v4, v3, null, null, null);
                f1.setNeighbor(0, f2);
                f2.setNeighbor(0, f1);

                triGrid.vertices.add(v1);
                triGrid.vertices.add(v2);
                triGrid.vertices.add(v3);
                triGrid.vertices.add(v4);
                triGrid.faces.add(f1);
                triGrid.faces.add(f2);
            }
        }
    }


    /**
     * Constructs a grid of triangles such that each building has a flat floor just beneath it.
     */
    private void constructTriGridPixels() {
        Pixel[][] pixels = this.bunch.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            for (int k = 0; k < pixels[i].length; k++) {
                TriangulationDSVertex_2 v1 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSVertex_2 v2 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSVertex_2 v3 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSVertex_2 v4 = new TriangulationDSVertex_2<Point_3>();
                TriangulationDSFace_2 f1;
                TriangulationDSFace_2 f2;

                v1.setPoint(new Point_3(i*SECTION_SIZE - (SECTION_SIZE/2), -k*SECTION_SIZE + (SECTION_SIZE/2), -1));
                v2.setPoint(new Point_3((i+1)*SECTION_SIZE - (SECTION_SIZE/2), -k*SECTION_SIZE + (SECTION_SIZE/2), -1));
                v3.setPoint(new Point_3((i+1)*SECTION_SIZE - (SECTION_SIZE/2), -(k+1)*SECTION_SIZE + (SECTION_SIZE/2), -1));
                v4.setPoint(new Point_3(i*SECTION_SIZE - (SECTION_SIZE/2), -(k+1)*SECTION_SIZE + (SECTION_SIZE/2), -1));

                f1 = new TriangulationDSFace_2(v1, v2, v3, null, null, null);
                f2 = new TriangulationDSFace_2(v1, v4, v3, null, null, null);
                f1.setNeighbor(0, f2);
                f2.setNeighbor(0, f1);

                triGrid.vertices.add(v1);
                triGrid.vertices.add(v2);
                triGrid.vertices.add(v3);
                triGrid.vertices.add(v4);
                triGrid.faces.add(f1);
                triGrid.faces.add(f2);
            }
        }
    }

    /**
     * Outputs the grid to an OFF
     */
    void output(int num) {
        this.append(this.triGrid);
        SharedVertexRepresentation mesh = new SharedVertexRepresentation(triCity);
        try {
            mesh.writeOffFile(OUT_FILE + num + ".off");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Combines a new triangulation to triCity such that all vertices, halfedges, and facets from p2 are copied into p1.
     * @param t is the triangulation that lends its properties to be copied into p1.
     */
    void append(TriangulationDS_2<Point_3> t) {
        triCity.vertices.addAll(t.vertices);
        triCity.faces.addAll(t.faces);
    }

    /**
     * Translates the triangulation along the grid and elevating the z coordinate.
     * @param triangulationDS2 is the building to be translated.
     * @param i determines how many grid spaces down the x axis to translate.
     * @param k determines how many grid spaces down the y axis to translate.
     * @param zCoord determines the elevation.
     */
    static void translate(TriangulationDS_2<Point_3> triangulationDS2, int i, int k, double zCoord, double[] offset) {
        for (int c = 0; c < triangulationDS2.vertices.size(); c++) {
            Point_3 point = triangulationDS2.vertices.get(c).getPoint();
            point.x += SECTION_SIZE*i + offset[0];
            point.y -= SECTION_SIZE*k + offset[1];
            point.z += zCoord;
        }
    }

    /**
     * Scales the size of the given triangulation to the given scale.
     * @param triangulationDS2 is the triangulation to be scaled.
     * @param scale is the scale.
     */
    static void scale(TriangulationDS_2<Point_3> triangulationDS2, double scale) {
        for (int c = 0; c < triangulationDS2.vertices.size(); c++) {
            Point_3 point = triangulationDS2.vertices.get(c).getPoint();
            point.x *= scale;
            point.y *= scale;
            point.z *= scale;
        }
    }

    /**
     * Rotates the triangulation around the z-axis.
     * @param triangulationDS2 is the building to be rotated.
     * @param angle is the angle of rotation in degrees.
     */
    static void rotate(TriangulationDS_2<Point_3> triangulationDS2, double angle) {
        double cosT, sinT, x, y;
        for (int c = 0; c < triangulationDS2.vertices.size(); c++) {
            Point_3 point = triangulationDS2.vertices.get(c).getPoint();
            // Rotation is handles around the z axis, so that buildings are always rooted to the ground.
            cosT = Math.cos(Math.toRadians(angle));
            sinT = Math.sin(Math.toRadians(angle));
            x = point.x*cosT - point.y*sinT;
            y = point.x*sinT + point.y*cosT;
            point.x = x;
            point.y = y;
        }
    }
}
