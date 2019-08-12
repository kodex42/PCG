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

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A wrapper for the Pixel subclass in a 2 dimensional array representing pixel data in a grid.
 */
class PixelBunch {
    private Pixel[][] pixels;
    private int width;
    private int height;
    private boolean initialized;

    /**
     * Populates the PixelBunch with Pixels from the given BufferedImage.
     * @param image is the image to rip pixels from.
     */
    PixelBunch(BufferedImage image) {
        // Check the dimensions
        int width = image.getWidth();
        int height = image.getHeight();
        // Store the pixel data into each Pixel
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[width][height];
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                int rgba = image.getRGB(x, y);
                Color c = new Color(rgba, App.hasAlpha);
                Pixel pixel = new Pixel();
                if (App.hasAlpha)
                    pixel.setValue(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
                else
                    pixel.setValue(c.getRed(), c.getGreen(), c.getBlue());
                this.pixels[x][y] = pixel;
            }
        }
        this.initialized = true;
        Main.print("Input image read correctly.");
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    Pixel[][] getPixels() {
        return pixels;
    }

    boolean isInitialized() {
        return this.initialized;
    }
}
