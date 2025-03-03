package dev.kearls;

import com.drew.imaging.FileType;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * See https://mkyong.com/java/how-to-resize-an-image-in-java/
 *
 * Which image do we rotate?  The original, or the scaled one?
 */
public class ResizeImage {
    private static final int EXIF_ROTATION_NORMAL = 1;
    private static final int EXIF_ROTATION_90_DEGREES = 6;
    private static final int EXIF_ROTATION_180_DEGREES = 3;
    private static final int EXIF_ROTATION_270_DEGREES = 8;

    private static final int IMG_WIDTH = 800;
    // Namivia1 is 3648 x 2736, Namibia2 is 640 x 480
    private static final String sourceImageName = "/Users/kevinearls/tmp/images/Switzerland/Switzerland2.jpg";
    private static final String destinationImageName = "/Users/kevinearls/tmp/images/Switzerland/Switzerland2.png";

    public static void main(String[] args) throws IOException {
        resize(sourceImageName, destinationImageName, IMG_WIDTH);
    }


    /*
     * This is from Gemini.
     *
     * TODO do we need to handle all of these?
     *
     * The TAG_ORIENTATION value can range from 1 to 8, each representing a different rotation and/or mirroring.
     * Here's a simplified interpretation:
     * 1: Normal orientation.
     * 2: Mirrored horizontally.
     * 3: Rotated 180 degrees.
     * 4: Mirrored vertically.
     * 5: Mirrored horizontally and rotated 270 degrees.
     * 6: Rotated 90 degrees.
     * 7: Mirrored horizontally and rotated 90 degrees.
     * 8: Rotated 270 degrees.
     */
    private static int checkOrientation(/*File imageFile*/ InputStream sourceInputStream) throws IOException {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(sourceInputStream, sourceInputStream.available(), FileType.Jpeg);
            Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (exifIFD0Directory != null && exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

                System.out.println("EXIF Orientation: " + orientation);
                return orientation;
                // Process the orientation value
            } else {
                System.out.println("Orientation tag not found for file: ????? ");
                return 0;
            }
        } catch (ImageProcessingException e) {
            // FIXME we probably just want to log an error here
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void resize(String sourceImageName, String destinationImageName, int desiredWidth) throws IOException {
        var sourceImageFile = new File(sourceImageName);
        var input = new BufferedInputStream(new FileInputStream(sourceImageFile));
        input.mark(Integer.MAX_VALUE);
        BufferedImage originalImage = ImageIO.read(input);
        System.out.println("Original Image Width: " + originalImage.getWidth() + " height: " + originalImage.getHeight());

        // Rotate the original image if necessary
        input.reset();
        var orientation = checkOrientation(input);
        if (orientation != EXIF_ROTATION_NORMAL) {
            if (orientation == EXIF_ROTATION_90_DEGREES) {
                originalImage = rotateImage(originalImage, 90);
            } else if (orientation == EXIF_ROTATION_180_DEGREES) {
                originalImage = rotateImage(originalImage, 180);
            } else if (orientation == EXIF_ROTATION_270_DEGREES) {
                originalImage = rotateImage(originalImage, 270);
            } else {
                System.out.println("Orientation for file: " + sourceImageFile.getAbsoluteFile() + " was " + orientation + "; We can't handle this...");
            }
        }

        // Upscale or downscale the image as needed
        int targetWidth;
        int targetHeight;
        if (originalImage.getWidth() > desiredWidth) {
            targetHeight = originalImage.getHeight() / (originalImage.getWidth() / desiredWidth);
            targetWidth = desiredWidth;
        } else {
            targetWidth = desiredWidth;
            targetHeight = (int) (originalImage.getHeight() / ((float) originalImage.getWidth() / desiredWidth));
        }
        Image resizedImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);

        // Write the new image as a png
        //String destinationImageNameString = destinationImageName.toString();  // Do we need this?  Don't we always want to use png?
        //String fileExtension = destinationImageNameString.substring(destinationImageNameString.lastIndexOf(".") + 1);

        // we want image in png format
        var bufferedImage = convertToBufferedImage(resizedImage);
        System.out.println("Converted Image Width: " + bufferedImage.getWidth() + " height: " + bufferedImage.getHeight());

        ImageIO.write(bufferedImage, "png", new File(destinationImageName));
    }


    // From Gemini
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = image.getWidth();
        int h = image.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, image.getType());
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rotated;
    }

    public static BufferedImage convertUsingConstructor(Image image) throws IllegalArgumentException {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Image dimensions are invalid");
        }
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.getGraphics().drawImage(image, 0, 0, null);
        return bufferedImage;
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
