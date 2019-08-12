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

package gui;

import classes.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import static classes.Main.print;

/**
 * User Interface class. Handles the input image and calls the generator via the Generate button on the UI form.
 */
public class App {
    public static final int GRAYSCALE_LEEWAY = 15;
    public static int scale = 1;
    // Constants for image processing
    private final String PATH = "Input/input.png";
    private final int MAX_HEIGHT = 150;
    private final int MAX_WIDTH = 150;
    private final int MAX_SCALE = 5;
    // Static values to be used by the Main class
    public static boolean hasAlpha;
    public static BufferedImage inputImage;
    // User Interface objects and processed images
    private static int longestLine = 0;
    private BufferedImage previewImage;
    private BufferedImage[] scaledInputImages;
    private JButton inputButton;
    private JButton generateButton;
    private JPanel mainPanel;
    private JLabel image;
    private JLabel imageLabel;
    private JSlider scaleSlider;
    private JLabel redValue0;
    private JLabel redValue1;
    private JLabel redValue2;
    private JLabel redValue3;
    private JLabel redValue4;
    private JLabel redValue5;
    private JTextArea consoleTextArea;
    private JScrollPane consoleScrollPane;

    /**
     * Basic constructor. Calls init()
     */
    private App() {
        redirectSystemStreams();
        print("Copyright (C) 2019 Matthew Buchanan\n" +
                "This program comes with ABSOLUTELY NO WARRANTY.\n" +
                "This is free software, and you are welcome to\n" +
                "redistribute it under certain conditions. Please\n" +
                "refer to the LICENSE.txt file for additional details.");
        init();
    }

    /**
     * Initializes User Interface objects and processes the input image.
     */
    private void init() {
        try {
            redValue0.setIcon(new ImageIcon(ImageIO.read(new File("icons/Block_Type_0.png"))));
            redValue1.setIcon(new ImageIcon(ImageIO.read(new File("icons/Block_Type_1.png"))));
            redValue2.setIcon(new ImageIcon(ImageIO.read(new File("icons/Block_Type_2.png"))));
            redValue3.setIcon(new ImageIcon(ImageIO.read(new File("icons/Block_Type_3.png"))));
            redValue4.setIcon(new ImageIcon(ImageIO.read(new File("icons/Block_Type_4.png"))));
            redValue5.setIcon(new ImageIcon(ImageIO.read(new File("icons/Block_Type_5.png"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Check for an existing input image in PATH
        try {
            File f = new File(PATH);
            inputImage = ImageIO.read(f);
            // Restrict the size of the image to maximum dimensions
            if (inputImage.getHeight() > MAX_HEIGHT)
                inputImage = resize(inputImage, inputImage.getWidth(), MAX_HEIGHT, true);
            if (inputImage.getWidth() > MAX_WIDTH)
                inputImage = resize(inputImage, MAX_WIDTH, inputImage.getHeight(), true);
            // Process the input image into an array of scaled images for each sector scale option
            buildScaledImages();
            // Set up the preview
            previewImage = resize(scaledInputImages[0],250,250, false);
            this.image.setText("");
            this.image.setIcon(new ImageIcon(previewImage));
            this.imageLabel.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set up a file input dialog
        inputButton.addActionListener(e -> {
            final JFileChooser fc = new JFileChooser();
            fc.addChoosableFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    String extension = Utils.getExtension(f).toLowerCase();
                    return extension.equals(Utils.bmp) ||
                            extension.equals(Utils.jpeg) ||
                            extension.equals(Utils.jpg) ||
                            extension.equals(Utils.png);
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });
            int returnVal = fc.showOpenDialog(inputButton);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = fc.getSelectedFile();
                    hasAlpha = Utils.getExtension(f).toLowerCase().equals("png");
                    inputImage = ImageIO.read(f);
                    // Restrict the size of the image to maximum dimensions
                    if (inputImage.getHeight() > MAX_HEIGHT)
                        inputImage = resize(inputImage, inputImage.getWidth(), MAX_HEIGHT, true);
                    if (inputImage.getWidth() > MAX_WIDTH)
                        inputImage = resize(inputImage, MAX_WIDTH, inputImage.getHeight(), true);
                    // Process the input image into an array of scaled images for each sector scale option
                    buildScaledImages();
                    // Set up the preview
                    previewImage = resize(scaledInputImages[0], 250,250, false);
                    this.image.setText("");
                    this.image.setIcon(new ImageIcon(previewImage));
                    this.imageLabel.setVisible(true);
                    scaleSlider.setValue(1);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Call the generator when the generate button is pressed
        generateButton.addActionListener(e ->  {
            // Set up the input image with the proper scale before generating
            inputImage = scaledInputImages[scaleSlider.getValue()-1];
            scale = scaleSlider.getValue();
            new Thread(Main::generate).start();
        });

        // Set up a state change listener for the sector scale slider
        scaleSlider.addChangeListener(e -> {
            // Swap the preview for the correctly scaled image
            int scale = scaleSlider.getValue();
            BufferedImage img = scaledInputImages[scale-1];
            previewImage = resize(new BufferedImage(img.getColorModel(), img.getRaster(), img.isAlphaPremultiplied(), null), 250, 250, false);
            this.image.setText("");
            this.image.setIcon(new ImageIcon(previewImage));
            this.imageLabel.setVisible(true);
        });
    }

    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(() ->  {
            if (text.length() > longestLine)
                longestLine = text.length();
            consoleTextArea.append(text);
            consoleTextArea.setPreferredSize(new Dimension(longestLine * consoleTextArea.getFontMetrics(consoleTextArea.getFont()).getMaxAdvance(), (consoleTextArea.getLineCount() + 1) * consoleTextArea.getFontMetrics(consoleTextArea.getFont()).getHeight()));
            consoleScrollPane.setPreferredSize(new Dimension(consoleTextArea.getSize().width, consoleTextArea.getSize().height));
        });
    }

    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
        DefaultCaret caret = (DefaultCaret)consoleTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    /**
     * Initializes the scaledInputImages array with a different image processed in each scale corresponding to its index
     */
    private void buildScaledImages() {
        this.scaledInputImages = new BufferedImage[MAX_SCALE];
        for (int i = 0; i < this.scaledInputImages.length; i++) {
            // Draw the subimage to an empty image with identical dimensions and type to deep copy
            BufferedImage sub = inputImage.getSubimage(0, 0, inputImage.getWidth(), inputImage.getHeight());
            scaledInputImages[i] = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
            Graphics2D g = scaledInputImages[i].createGraphics();
            try {
                g.drawImage(sub, 0, 0, null);
            }
            finally {
                g.dispose();
            }

            // Check every nth pixel where n = sector scale
            for (int x = 0; x < this.scaledInputImages[i].getWidth(); x+=i+1) {
                for (int y = 0; y < this.scaledInputImages[i].getHeight(); y+=i+1) {
                    // A sector fails the scale check if it has out of bounds pixels or contains any grayscale pixels
                    if (containsGrayscale(this.scaledInputImages[i], i+1, x, y) || x+i+1 > this.scaledInputImages[i].getWidth() || y+i+1 > this.scaledInputImages[i].getHeight()) {
                        // Sectors that fail the scale check are disabled entirely
                        disableSector(this.scaledInputImages[i], i + 1, x, y);
                    }
                }
            }
        }
    }

    /**
     * Disables a sector of pixels in the given image by rendering any non-grayscale pixels in the sector invisible.
     * @param img is the image to be modified.
     * @param i is the size of the sector to disable.
     * @param x is the starting x position of the sector in the image.
     * @param y is the starting y position of the sector in the image.
     */
    private void disableSector(BufferedImage img, int i, int x, int y) {
        for (int c = x; c < x+i; c++) {
            if (c >= img.getWidth())
                continue;
            for (int k = y; k < y+i; k++) {
                if (k >= img.getHeight() || isGrayscale(img.getRGB(c, k)))
                    continue;
                img.setRGB(c, k, new Color(0, 0, 0, 0).getRGB());
            }
        }
    }

    /**
     * Checks a sector in a given image for any pixels that are grayscale (red == blue == green).
     * @param img is the image to check.
     * @param i is the size of the sector to check.
     * @param x is the starting x position of the sector in the image.
     * @param y is the starting y position of the sector in the image.
     * @return true if any grayscale pixels are detected in the defined sector, false otherwise.
     */
    private boolean containsGrayscale(BufferedImage img, int i, int x, int y) {
        for (int c = x; c < x+i; c++) {
            if (c >= img.getWidth())
                continue;
            for (int k = y; k < y+i; k++) {
                if (k >= img.getHeight())
                    continue;
                if (isGrayscale(img.getRGB(c, k)))
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks a given rgb value from a pixel if it is grayscale (red == blue == green)
     * @param rgb is the integer value of the pixel's rgb.
     * @return true if the pixel is grayscale, false otherwise.
     */
    private boolean isGrayscale(int rgb) {
        Color c = new Color(rgb, App.hasAlpha);
        return (c.getBlue() < c.getGreen() + GRAYSCALE_LEEWAY && c.getBlue() > c.getGreen() - GRAYSCALE_LEEWAY)
                && (c.getRed() < c.getGreen() + GRAYSCALE_LEEWAY && c.getRed() > c.getGreen() - GRAYSCALE_LEEWAY)
                && (c.getBlue() < c.getRed() + GRAYSCALE_LEEWAY && c.getBlue() > c.getRed() - GRAYSCALE_LEEWAY);
    }

    /**
     * Resizes a given BufferedImage to a new height and width, scaling the image in the process.
     * @param img is the image to be scaled to new dimensions.
     * @param boundW is the new width to scale to.
     * @param boundH is the new height to scale to.
     * @return a new instance of the image scaled to the new height and width with the given image painted on.
     */
    private static BufferedImage resize(BufferedImage img, int boundW, int boundH, boolean aspectRatio) {
        int newH = img.getHeight();
        int newW = img.getWidth();

        if (aspectRatio) {
            // Check and scale width and height to maintain aspect ratio.
            if (img.getWidth() > boundW) {
                newW = boundW;
                newH = (newW * img.getHeight()) / img.getWidth();
            }
            if (newH > boundH) {
                newH = boundH;
                newW = (boundH * img.getWidth()) / img.getHeight();
            }
        }
        else {
            newH = boundH;
            newW = boundW;
        }

        // Create a new instance of the given image with new height and width
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage rImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        // Paint the given image to the new scaled image
        Graphics2D g2d = rImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return rImg;
    }

    /**
     * Starts the UI.
     */
    public static void main(String[] args){
        JFrame frame = new JFrame("Procedural City Generator");
        frame.setContentPane(new App().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
