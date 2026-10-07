import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.function.Consumer;

public class ColorModelPanel extends JPanel {
    private final String modelName;
    private final String[] channelNames;
    private final double[] minValues;
    private final double[] maxValues;

    private JSlider[] sliders;
    private JSpinner[] spinners;
    private boolean isSelfUpdating = false;

    private Consumer<double[]> onComponentChangedListener;

    public ColorModelPanel(String modelName, String[] channelNames, double[] minValues, double[] maxValues) {
        this.modelName = modelName;
        this.channelNames = channelNames;
        this.minValues = minValues;
        this.maxValues = maxValues;

        initUI();
    }

    private void initUI() {
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), modelName ,
                javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 14)
        ));

        int channels = channelNames.length;
        setLayout(new GridLayout(channels, 3, 10, 8));

        sliders = new JSlider[channels];
        spinners = new JSpinner[channels];

        for (int i = 0; i < channels; i++) {
            JLabel label = new JLabel(channelNames[i] + ":", SwingConstants.RIGHT);
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));

            int min = (int) Math.round(minValues[i]);
            int max = (int) Math.round(maxValues[i]);

            sliders[i] = new JSlider(min, max, min);
            spinners[i] = new JSpinner(new SpinnerNumberModel((double) min, minValues[i], maxValues[i], 1.0));

            final int index = i;

            sliders[i].addChangeListener(e -> {
                if (isSelfUpdating) return;
                isSelfUpdating = true;
                spinners[index].setValue((double) sliders[index].getValue());
                notifyChange();
                isSelfUpdating = false;
            });

            spinners[i].addChangeListener(e -> {
                if (isSelfUpdating) return;
                isSelfUpdating = true;
                double val = (Double) spinners[index].getValue();
                sliders[index].setValue((int) Math.round(val));
                notifyChange();
                isSelfUpdating = false;
            });

            add(label);
            add(sliders[i]);
            add(spinners[i]);
        }
    }

    public void setOnComponentChangedListener(Consumer<double[]> listener) {
        this.onComponentChangedListener = listener;
    }

    private void notifyChange() {
        if (onComponentChangedListener != null) {
            double[] values = new double[channelNames.length];
            for (int i = 0; i < channelNames.length; i++) {
                values[i] = (Double) spinners[i].getValue();
            }
            onComponentChangedListener.accept(values);
        }
    }

    public void setValues(double[] values) {
        isSelfUpdating = true;
        for (int i = 0; i < values.length; i++) {
            double clamped = Math.max(minValues[i], Math.min(maxValues[i], values[i]));
            spinners[i].setValue(clamped);
            sliders[i].setValue((int) Math.round(clamped));
        }
        isSelfUpdating = false;
    }
}