package dev.kearls;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * See https://mkyong.com/java/how-to-resize-an-image-in-java/
 */
public class ResizeImage {
    private static final int IMG_WIDTH = 400;
    private static final String sourceImageName = "/Users/kevinearls/tmp/SaintJonas.jpg";
    private static final String destinationImageName = "/Users/kevinearls/tmp/SaintJonas1.png";

    public static void main(String[] args) throws IOException {
        Path source = Paths.get(sourceImageName);
        Path target = Paths.get(destinationImageName);

        try (InputStream is = new FileInputStream(source.toFile())) {
            resize(is, target, IMG_WIDTH);
        }
    }

    private static void resize(InputStream input, Path target, int width) throws IOException {
        BufferedImage originalImage = ImageIO.read(input);
        System.out.println("Original Image Height: " + originalImage.getHeight() + " width: " + originalImage.getWidth());
        var targetHeight = originalImage.getHeight() / (originalImage.getWidth()/width);

        Image resizedImage = originalImage.getScaledInstance(width, targetHeight, Image.SCALE_SMOOTH);

        String s = target.getFileName().toString();
        String fileExtension = s.substring(s.lastIndexOf(".") + 1);

        // we want image in png format
        var bufferedImage = convertToBufferedImage(resizedImage);
        System.out.println("Converted Image Width: " + bufferedImage.getWidth() + " height: " + bufferedImage.getHeight());
        ImageIO.write(bufferedImage, fileExtension, target.toFile());
    }

    public static BufferedImage convertToBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }

        // Create a buffered image with transparency
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();

        return bufferedImage;
    }
}
