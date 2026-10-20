import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class HistogramPanel extends JPanel {
    private BufferedImage image;

    public HistogramPanel(String title) {
        setBorder(BorderFactory.createTitledBorder(title));
        setBackground(Color.WHITE);
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (image == null) return;

        int[][] histogram = new int[3][256];
        int maximumCount = 1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                histogram[0][(pixel >> 16) & 255]++;
                histogram[1][(pixel >> 8) & 255]++;
                histogram[2][pixel & 255]++;
            }
        }
        for (int[] channel : histogram) {
            for (int count : channel) maximumCount = Math.max(maximumCount, count);
        }

        int left = 35;
        int top = 28;
        int right = getWidth() - 12;
        int bottom = getHeight() - 28;
        int plotWidth = right - left;
        int plotHeight = bottom - top;

        graphics.setColor(Color.DARK_GRAY);
        graphics.drawLine(left, bottom, right, bottom);
        graphics.drawLine(left, top, left, bottom);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        graphics.drawString("0", left - 4, bottom + 15);
        graphics.drawString("255", right - 18, bottom + 15);
        graphics.drawString("Число пикселей", left + 4, top - 8);

        Color[] channelColors = {
                new Color(210, 45, 45),
                new Color(30, 150, 60),
                new Color(45, 85, 210)
        };
        Graphics2D histogramGraphics = (Graphics2D) graphics.create();
        Color[] translucentColors = {
                new Color(210, 45, 45, 135),
                new Color(30, 150, 60, 135),
                new Color(45, 85, 210, 135)
        };
        for (int channel = 0; channel < 3; channel++) {
            histogramGraphics.setColor(translucentColors[channel]);
            for (int value = 0; value < 256; value++) {
                int x = left + (int) (value / 255.0 * plotWidth);
                int barHeight = (int) (histogram[channel][value] / (double) maximumCount * plotHeight);
                histogramGraphics.drawLine(x, bottom, x, bottom - barHeight);
            }
        }
        histogramGraphics.dispose();

        graphics.setColor(channelColors[0]);
        graphics.drawString("R", right - 45, top + 10);
        graphics.setColor(channelColors[1]);
        graphics.drawString("G", right - 30, top + 10);
        graphics.setColor(channelColors[2]);
        graphics.drawString("B", right - 15, top + 10);
    }
}
