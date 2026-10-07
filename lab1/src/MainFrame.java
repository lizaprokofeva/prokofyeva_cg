import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private ColorModelPanel rgbPanel;
    private ColorModelPanel cmykPanel;
    private ColorModelPanel hlsPanel;

    private JPanel colorDisplayPanel;
    private JLabel hexLabel;
    private JButton colorPickerButton;

    private boolean isGlobalUpdating = false;

    public MainFrame() {
        super("Lab1 (CMYK - RGB - HLS)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 480);
        setLocationRelativeTo(null);

        initUI();
        updateAllFromRgb(new Color(128, 128, 255));
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));


        rgbPanel = new ColorModelPanel(
                "RGB",
                new String[]{"Red (R)", "Green (G)", "Blue (B)"},
                new double[]{0, 0, 0},
                new double[]{255, 255, 255}
        );

        cmykPanel = new ColorModelPanel(
                "CMYK",
                new String[]{"Cyan (C %)", "Magenta (M %)", "Yellow (Y %)", "Key/Black (K %)"},
                new double[]{0, 0, 0, 0},
                new double[]{100, 100, 100, 100}
        );

        hlsPanel = new ColorModelPanel(
                "HLS",
                new String[]{"Hue (H °)", "Lightness (L %)", "Saturation (S %)"},
                new double[]{0, 0, 0},
                new double[]{360, 100, 100}
        );

        rgbPanel.setOnComponentChangedListener(vals -> {
            if (isGlobalUpdating) return;
            isGlobalUpdating = true;
            Color color = new Color((int) vals[0], (int) vals[1], (int) vals[2]);
            updatePanelsAndDisplay(color, false, true, true);
            isGlobalUpdating = false;
        });

        cmykPanel.setOnComponentChangedListener(vals -> {
            if (isGlobalUpdating) return;
            isGlobalUpdating = true;
            Color color = ColorConverter.cmykToRgb(vals[0], vals[1], vals[2], vals[3]);
            updatePanelsAndDisplay(color, true, false, true);
            isGlobalUpdating = false;
        });

        hlsPanel.setOnComponentChangedListener(vals -> {
            if (isGlobalUpdating) return;
            isGlobalUpdating = true;
            Color color = ColorConverter.hlsToRgb(vals[0], vals[1], vals[2]);
            updatePanelsAndDisplay(color, true, true, false);
            isGlobalUpdating = false;
        });

        JPanel modelsContainer = new JPanel(new GridLayout(1, 3, 10, 0));
        modelsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        modelsContainer.add(rgbPanel);
        modelsContainer.add(cmykPanel);
        modelsContainer.add(hlsPanel);


        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Предпросмотр"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        colorDisplayPanel = new JPanel();
        colorDisplayPanel.setPreferredSize(new Dimension(180, 180));
        colorDisplayPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        hexLabel = new JLabel("#000000", SwingConstants.CENTER);
        hexLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        hexLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        colorPickerButton = new JButton("Выбрать из палитры");
        colorPickerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorPickerButton.addActionListener(e -> {
            Color currentColor = colorDisplayPanel.getBackground();
            Color chosenColor = JColorChooser.showDialog(this, "Палитра графического редактора", currentColor);
            if (chosenColor != null) {
                updateAllFromRgb(chosenColor);
            }
        });

        rightPanel.add(colorDisplayPanel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(hexLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(colorPickerButton);

        add(modelsContainer, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void updateAllFromRgb(Color color) {
        isGlobalUpdating = true;
        updatePanelsAndDisplay(color, true, true, true);
        isGlobalUpdating = false;
    }

    private void updatePanelsAndDisplay(Color color, boolean updateRgb, boolean updateCmyk, boolean updateHls) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        if (updateRgb) {
            rgbPanel.setValues(new double[]{r, g, b});
        }
        if (updateCmyk) {
            cmykPanel.setValues(ColorConverter.rgbToCmyk(r, g, b));
        }
        if (updateHls) {
            hlsPanel.setValues(ColorConverter.rgbToHls(r, g, b));
        }

        colorDisplayPanel.setBackground(color);
        hexLabel.setText(String.format("#%02X%02X%02X", r, g, b));
    }
}