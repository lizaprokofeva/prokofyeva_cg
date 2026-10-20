import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel(String title) {
        setBackground(new Color(245, 245, 245));
        setBorder(BorderFactory.createTitledBorder(title));
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (image == null) {
            graphics.setColor(Color.GRAY);
            graphics.drawString("Изображение не загружено", 20, 35);
            return;
        }

        int availableWidth = getWidth() - 20;
        int availableHeight = getHeight() - 45;
        double scale = Math.min((double) availableWidth / image.getWidth(),
                (double) availableHeight / image.getHeight());
        int displayWidth = Math.max(1, (int) (image.getWidth() * scale));
        int displayHeight = Math.max(1, (int) (image.getHeight() * scale));
        graphics.drawImage(image, (getWidth() - displayWidth) / 2,
                (getHeight() - displayHeight) / 2 + 10, displayWidth, displayHeight, null);
    }
}
