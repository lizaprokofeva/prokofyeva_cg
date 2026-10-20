import java.awt.Color;
import java.awt.image.BufferedImage;

public final class ImageProcessor {
    private ImageProcessor() {
    }

    public static BufferedImage applyLinearContrast(BufferedImage source) {
        int[] minValues = {255, 255, 255};
        int[] maxValues = {0, 0, 0};

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int[] channels = getChannels(pixel);
                for (int channel = 0; channel < 3; channel++) {
                    minValues[channel] = Math.min(minValues[channel], channels[channel]);
                    maxValues[channel] = Math.max(maxValues[channel], channels[channel]);
                }
            }
        }

        int[][] lookup = new int[3][256];
        for (int channel = 0; channel < 3; channel++) {
            for (int value = 0; value < 256; value++) {
                int range = maxValues[channel] - minValues[channel];
                lookup[channel][value] = range == 0
                        ? value
                        : clamp((value - minValues[channel]) * 255.0 / range);
            }
        }

        BufferedImage result = createImageLike(source);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int red = lookup[0][(pixel >> 16) & 255];
                int green = lookup[1][(pixel >> 8) & 255];
                int blue = lookup[2][pixel & 255];
                result.setRGB(x, y, (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue);
            }
        }
        return result;
    }

    public static BufferedImage equalizeRgb(BufferedImage source) {
        int[][] histogram = new int[3][256];
        int pixelCount = source.getWidth() * source.getHeight();

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                histogram[0][(pixel >> 16) & 255]++;
                histogram[1][(pixel >> 8) & 255]++;
                histogram[2][pixel & 255]++;
            }
        }

        int[][] lookup = new int[3][256];
        for (int channel = 0; channel < 3; channel++) {
            int cumulative = 0;
            int firstCumulative = 0;
            boolean firstFound = false;
            int[] cumulativeHistogram = new int[256];

            for (int value = 0; value < 256; value++) {
                cumulative += histogram[channel][value];
                cumulativeHistogram[value] = cumulative;
                if (!firstFound && histogram[channel][value] > 0) {
                    firstCumulative = cumulative;
                    firstFound = true;
                }
            }

            int denominator = pixelCount - firstCumulative;
            for (int value = 0; value < 256; value++) {
                lookup[channel][value] = denominator <= 0
                        ? value
                        : clamp((cumulativeHistogram[value] - firstCumulative) * 255.0 / denominator);
            }
        }

        BufferedImage result = createImageLike(source);
        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int pixel = source.getRGB(x, y);
                int red = lookup[0][(pixel >> 16) & 255];
                int green = lookup[1][(pixel >> 8) & 255];
                int blue = lookup[2][pixel & 255];
                result.setRGB(x, y, (pixel & 0xFF000000) | (red << 16) | (green << 8) | blue);
            }
        }
        return result;
    }

    public static BufferedImage equalizeHsvBrightness(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int[] brightnessValues = new int[width * height];
        int[] histogram = new int[256];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getRGB(x, y);
                float[] hsv = Color.RGBtoHSB((pixel >> 16) & 255, (pixel >> 8) & 255, pixel & 255, null);
                int brightness = Math.round(hsv[2] * 255);
                brightnessValues[index++] = brightness;
                histogram[brightness]++;
            }
        }

        int[] lookup = createEqualizationLookup(histogram, brightnessValues.length);
        BufferedImage result = createImageLike(source);
        index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getRGB(x, y);
                float[] hsv = Color.RGBtoHSB((pixel >> 16) & 255, (pixel >> 8) & 255, pixel & 255, null);
                hsv[2] = lookup[brightnessValues[index++]] / 255.0f;
                int converted = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
                result.setRGB(x, y, (pixel & 0xFF000000) | (converted & 0x00FFFFFF));
            }
        }
        return result;
    }

    public static BufferedImage sharpen(BufferedImage source) {
        return applyKernel(source, new double[][]{
                {0, -1, 0},
                {-1, 5, -1},
                {0, -1, 0}
        });
    }

    private static BufferedImage applyKernel(BufferedImage source, double[][] kernel) {
        int width = source.getWidth();
        int height = source.getHeight();
        BufferedImage result = createImageLike(source);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = source.getRGB(x, y);
                int[] filtered = new int[3];
                for (int channel = 0; channel < 3; channel++) {
                    double sum = 0;
                    for (int offsetY = -1; offsetY <= 1; offsetY++) {
                        for (int offsetX = -1; offsetX <= 1; offsetX++) {
                            int sampleX = clampCoordinate(x + offsetX, width);
                            int sampleY = clampCoordinate(y + offsetY, height);
                            int sample = source.getRGB(sampleX, sampleY);
                            sum += getChannel(sample, channel) * kernel[offsetY + 1][offsetX + 1];
                        }
                    }
                    filtered[channel] = clamp(sum);
                }
                result.setRGB(x, y, (pixel & 0xFF000000)
                        | (filtered[0] << 16) | (filtered[1] << 8) | filtered[2]);
            }
        }
        return result;
    }

    private static int[] createEqualizationLookup(int[] histogram, int pixelCount) {
        int[] cumulativeHistogram = new int[256];
        int cumulative = 0;
        int firstCumulative = 0;
        boolean firstFound = false;

        for (int value = 0; value < 256; value++) {
            cumulative += histogram[value];
            cumulativeHistogram[value] = cumulative;
            if (!firstFound && histogram[value] > 0) {
                firstCumulative = cumulative;
                firstFound = true;
            }
        }

        int denominator = pixelCount - firstCumulative;
        int[] lookup = new int[256];
        for (int value = 0; value < 256; value++) {
            lookup[value] = denominator <= 0
                    ? value
                    : clamp((cumulativeHistogram[value] - firstCumulative) * 255.0 / denominator);
        }
        return lookup;
    }

    private static BufferedImage createImageLike(BufferedImage source) {
        return new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    private static int[] getChannels(int pixel) {
        return new int[]{(pixel >> 16) & 255, (pixel >> 8) & 255, pixel & 255};
    }

    private static int getChannel(int pixel, int channel) {
        if (channel == 0) return (pixel >> 16) & 255;
        if (channel == 1) return (pixel >> 8) & 255;
        return pixel & 255;
    }

    private static int clampCoordinate(int coordinate, int limit) {
        return Math.max(0, Math.min(limit - 1, coordinate));
    }

    private static int clamp(double value) {
        return (int) Math.max(0, Math.min(255, Math.round(value)));
    }
}
