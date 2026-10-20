import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageProcessingApp extends JFrame {
    private static final String[] SAMPLE_FILES = {
            "", "low-contrast.png", "blurred.png", "noisy.png", "edges.png"
    };
    private static final String[] OPERATIONS = {
            "Линейное контрастирование",
            "Эквализация гистограммы (RGB)",
            "Эквализация яркости (HSV)",
            "Фильтр повышения резкости"
    };

    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private final ImagePanel sourceView = new ImagePanel("Исходное изображение");
    private final ImagePanel resultView = new ImagePanel("Результат обработки");
    private final HistogramPanel sourceHistogram = new HistogramPanel("Гистограмма исходного изображения");
    private final HistogramPanel resultHistogram = new HistogramPanel("Гистограмма результата");
    private final JComboBox<String> operationSelector = new JComboBox<>(OPERATIONS);
    private final JLabel statusLabel = new JLabel("Откройте изображение или выберите тестовый пример.");

    public ImageProcessingApp() {
        super("Лабораторная работа № 2 вариант 3");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        JButton openButton = new JButton("Открыть изображение…");
        JButton applyButton = new JButton("Применить метод");
        JButton resetButton = new JButton("Сбросить");
        JButton saveButton = new JButton("Сохранить результат…");
        JComboBox<String> sampleSelector = new JComboBox<>(new String[]{
                "Тестовые изображения…", "Низкоконтрастное", "Размытое", "Зашумлённое", "С деталями и границами"
        });

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(openButton);
        controls.add(new JLabel("Метод:"));
        controls.add(operationSelector);
        controls.add(applyButton);
        controls.add(resetButton);
        controls.add(saveButton);
        controls.add(sampleSelector);
        add(controls, BorderLayout.NORTH);

        JPanel imageViews = new JPanel(new GridLayout(1, 2, 8, 8));
        imageViews.add(sourceView);
        imageViews.add(resultView);

        JPanel histograms = new JPanel(new GridLayout(1, 2, 8, 8));
        histograms.add(sourceHistogram);
        histograms.add(resultHistogram);
        histograms.setPreferredSize(new Dimension(100, 230));

        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.add(imageViews, BorderLayout.CENTER);
        content.add(histograms, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        add(statusLabel, BorderLayout.SOUTH);

        openButton.addActionListener(event -> openImage());
        applyButton.addActionListener(event -> processImage());
        resetButton.addActionListener(event -> resetImage());
        saveButton.addActionListener(event -> saveImage());
        sampleSelector.addActionListener(event -> {
            int selectedIndex = sampleSelector.getSelectedIndex();
            if (selectedIndex > 0) openSample(SAMPLE_FILES[selectedIndex]);
            sampleSelector.setSelectedIndex(0);
        });
    }

    private void openImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Изображения (PNG, JPEG, BMP, GIF)", "png", "jpg", "jpeg", "bmp", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        try {
            BufferedImage image = ImageIO.read(selectedFile);
            if (image == null) throw new IOException("Формат изображения не поддерживается.");
            setImage(image, selectedFile.getName());
        } catch (IOException exception) {
            showError("Не удалось открыть изображение: " + exception.getMessage());
        }
    }

    private void openSample(String fileName) {
        File sampleFile = new File("test-images", fileName);
        if (!sampleFile.exists()) sampleFile = new File(".." + File.separator + "test-images", fileName);
        try {
            BufferedImage image = ImageIO.read(sampleFile);
            if (image == null) throw new IOException("Файл не удалось прочитать.");
            setImage(image, fileName);
        } catch (IOException exception) {
            showError("Не удалось загрузить " + fileName + ". Откройте изображение вручную или запустите программу из папки проекта.");
        }
    }

    private void setImage(BufferedImage image, String fileName) {
        originalImage = copyImage(image);
        processedImage = copyImage(image);
        sourceView.setImage(originalImage);
        resultView.setImage(processedImage);
        sourceHistogram.setImage(originalImage);
        resultHistogram.setImage(processedImage);
        statusLabel.setText("Загружено: " + fileName + " | Размер: "
                + image.getWidth() + " × " + image.getHeight());
    }

    private void processImage() {
        if (originalImage == null) {
            showError("Сначала откройте изображение.");
            return;
        }

        String selectedOperation = (String) operationSelector.getSelectedItem();
        if (selectedOperation == null) return;
        switch (selectedOperation) {
            case "Линейное контрастирование":
                processedImage = ImageProcessor.applyLinearContrast(originalImage);
                break;
            case "Эквализация гистограммы (RGB)":
                processedImage = ImageProcessor.equalizeRgb(originalImage);
                break;
            case "Эквализация яркости (HSV)":
                processedImage = ImageProcessor.equalizeHsvBrightness(originalImage);
                break;
            case "Фильтр повышения резкости":
                processedImage = ImageProcessor.sharpen(originalImage);
                break;
            default:
                return;
        }

        resultView.setImage(processedImage);
        resultHistogram.setImage(processedImage);
        statusLabel.setText("Выполнено: " + selectedOperation + " | Размер: "
                + processedImage.getWidth() + " × " + processedImage.getHeight());
    }

    private void resetImage() {
        if (originalImage == null) return;
        processedImage = copyImage(originalImage);
        resultView.setImage(processedImage);
        resultHistogram.setImage(processedImage);
        statusLabel.setText("Результат сброшен к исходному изображению.");
    }

    private void saveImage() {
        if (processedImage == null) {
            showError("Нет результата для сохранения.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("result.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = chooser.getSelectedFile();
        String fileName = selectedFile.getName().toLowerCase();
        String format = fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") ? "jpg"
                : fileName.endsWith(".bmp") ? "bmp" : "png";
        if (!fileName.matches(".*\\.(png|jpg|jpeg|bmp)$")) {
            selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".png");
        }

        try {
            BufferedImage imageToSave = processedImage;
            if (format.equals("jpg") || format.equals("bmp")) {
                imageToSave = new BufferedImage(processedImage.getWidth(), processedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = imageToSave.createGraphics();
                graphics.drawImage(processedImage, 0, 0, null);
                graphics.dispose();
            }
            if (!ImageIO.write(imageToSave, format, selectedFile)) {
                throw new IOException("Для выбранного формата нет доступного средства записи.");
            }
            statusLabel.setText("Результат сохранён: " + selectedFile.getAbsolutePath());
        } catch (IOException exception) {
            showError("Не удалось сохранить изображение: " + exception.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    private static BufferedImage copyImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ImageProcessingApp().setVisible(true));
    }
}
