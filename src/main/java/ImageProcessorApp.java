import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import com.github.sarxos.webcam.Webcam;

/**
 * Десктоп-приложение для обработки изображений.
 * Поддерживает загрузку изображений (PNG/JPG), захват с веб-камеры,
 * отображение цветовых каналов (RGB), негатив, размытие по Гауссу,
 * рисование круга.
 */
public class ImageProcessorApp extends JFrame {

  static {
    // Загрузка нативной библиотеки OpenCV
    nu.pattern.OpenCV.loadLocally();
  }

  private static final String WINDOW_TITLE = "Обработчик изображений";
  private static final int WINDOW_WIDTH = 900;
  private static final int WINDOW_HEIGHT = 700;

  private BufferedImage currentImage;
  private BufferedImage originalImage;
  private JLabel imageLabel;
  private JLabel statusLabel;

  public ImageProcessorApp() {
    setTitle(WINDOW_TITLE);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    createControlPanel();
    createImageDisplayPanel();
    createStatusPanel();

    updateStatus("Готов к работе. Загрузите изображение или сделайте снимок с веб-камеры.");
    setVisible(true);
  }

  private void createControlPanel() {
    JPanel controlPanel = new JPanel(new GridLayout(2, 4, 5, 5));
    controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Блок 1: Загрузка изображения
    JPanel loadPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    loadPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "1. Загрузка", TitledBorder.LEFT,
        TitledBorder.TOP));
    JButton loadFileBtn = new JButton("Загрузить файл");
    JButton webcamBtn = new JButton("Снимок с веб-камеры");
    loadPanel.add(loadFileBtn);
    loadPanel.add(webcamBtn);
    controlPanel.add(loadPanel);

    // Блок 2: Цветовые каналы
    JPanel channelsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    channelsPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "2. Цветовые каналы", TitledBorder.LEFT,
        TitledBorder.TOP));
    JButton redChannelBtn = new JButton("Красный канал");
    JButton greenChannelBtn = new JButton("Зелёный канал");
    JButton blueChannelBtn = new JButton("Синий канал");
    channelsPanel.add(redChannelBtn);
    channelsPanel.add(greenChannelBtn);
    channelsPanel.add(blueChannelBtn);
    controlPanel.add(channelsPanel);

    // Блок 3: Вариант 5
    JPanel variantPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    variantPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "3. Вариант 5", TitledBorder.LEFT,
        TitledBorder.TOP));
    JButton negativeBtn = new JButton("Негатив");
    JButton gaussianBlurBtn = new JButton("Размытие по Гауссу");
    JButton drawCircleBtn = new JButton("Нарисовать круг");
    variantPanel.add(negativeBtn);
    variantPanel.add(gaussianBlurBtn);
    variantPanel.add(drawCircleBtn);
    controlPanel.add(variantPanel);

    // Блок 4: Сброс
    JPanel resetPanel = new JPanel(new GridLayout(2, 1, 5, 5));
    resetPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(), "4. Прочее", TitledBorder.LEFT,
        TitledBorder.TOP));
    JButton resetBtn = new JButton("Сбросить к оригиналу");
    JButton saveBtn = new JButton("Сохранить");
    resetPanel.add(resetBtn);
    resetPanel.add(saveBtn);
    controlPanel.add(resetPanel);

    add(controlPanel, BorderLayout.NORTH);

    // Обработчики кнопок
    loadFileBtn.addActionListener(e -> loadImageFromFile());
    webcamBtn.addActionListener(e -> captureFromWebcam());
    redChannelBtn.addActionListener(e -> showChannel(Channel.RED));
    greenChannelBtn.addActionListener(e -> showChannel(Channel.GREEN));
    blueChannelBtn.addActionListener(e -> showChannel(Channel.BLUE));
    negativeBtn.addActionListener(e -> applyNegative());
    gaussianBlurBtn.addActionListener(e -> applyGaussianBlur());
    drawCircleBtn.addActionListener(e -> drawCircle());
    resetBtn.addActionListener(e -> resetToOriginal());
    saveBtn.addActionListener(e -> saveImage());
  }

  private void createImageDisplayPanel() {
    JPanel imagePanel = new JPanel(new BorderLayout());
    imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    imageLabel = new JLabel("Изображение не загружено", JLabel.CENTER);
    imageLabel.setPreferredSize(new Dimension(800, 500));
    imageLabel.setBorder(BorderFactory.createEtchedBorder());
    imagePanel.add(imageLabel, BorderLayout.CENTER);
    add(imagePanel, BorderLayout.CENTER);
  }

  private void createStatusPanel() {
    statusLabel = new JLabel(" ");
    statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    add(statusLabel, BorderLayout.SOUTH);
  }

  private void updateStatus(String message) {
    SwingUtilities.invokeLater(() -> statusLabel.setText(message));
  }

  private void displayImage(BufferedImage image) {
    SwingUtilities.invokeLater(() -> {
      if (image != null) {
        currentImage = image;
        ImageIcon icon = new ImageIcon(image);
        imageLabel.setIcon(icon);
        imageLabel.setText("");
      } else {
        imageLabel.setIcon(null);
        imageLabel.setText("Изображение не загружено");
      }
      revalidate();
      repaint();
    });
  }

  // ==================== ЗАГРУЗКА ИЗОБРАЖЕНИЙ ====================

  private void loadImageFromFile() {
    JFileChooser fileChooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "Изображения (PNG, JPG)", "png", "jpg", "jpeg");
    fileChooser.setFileFilter(filter);

    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      try {
        BufferedImage image = ImageIO.read(file);
        if (image == null) {
          throw new IOException("Не удалось прочитать файл");
        }
        originalImage = copyImage(image);
        displayImage(image);
        updateStatus("Загружено изображение: " + file.getName());
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
            "Ошибка загрузки файла: " + e.getMessage(),
            "Ошибка", JOptionPane.ERROR_MESSAGE);
        updateStatus("Ошибка загрузки: " + e.getMessage());
      }
    }
  }

  private void captureFromWebcam() {
    updateStatus("Попытка подключения к веб-камере...");
    new Thread(() -> {
      try {
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
          SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                "Веб-камера не обнаружена.\n"
                + "Возможные решения:\n"
                + "1. Проверьте, что веб-камера подключена.\n"
                + "2. Закройте другие приложения, использующие камеру.\n"
                + "3. Установите драйверы для веб-камеры.",
                "Ошибка", JOptionPane.ERROR_MESSAGE);
          });
          updateStatus("Ошибка: веб-камера не обнаружена.");
          return;
        }
        webcam.open();
        BufferedImage snapshot = webcam.getImage();
        webcam.close();
        if (snapshot != null) {
          originalImage = copyImage(snapshot);
          displayImage(snapshot);
          updateStatus("Снимок сделан с веб-камеры.");
        } else {
          updateStatus("Не удалось получить снимок с веб-камеры.");
        }
      } catch (Exception e) {
        SwingUtilities.invokeLater(() -> {
          JOptionPane.showMessageDialog(this,
              "Ошибка при работе с веб-камерой: " + e.getMessage(),
              "Ошибка", JOptionPane.ERROR_MESSAGE);
        });
        updateStatus("Ошибка: " + e.getMessage());
      }
    }).start();
  }

  private BufferedImage copyImage(BufferedImage source) {
    BufferedImage copy = new BufferedImage(
        source.getWidth(), source.getHeight(), source.getType());
    Graphics g = copy.getGraphics();
    g.drawImage(source, 0, 0, null);
    g.dispose();
    return copy;
  }

  // ==================== ЦВЕТОВЫЕ КАНАЛЫ ====================

  private enum Channel { RED, GREEN, BLUE }

  private void showChannel(Channel channel) {
    if (currentImage == null) {
      showNoImageError();
      return;
    }

    BufferedImage result = new BufferedImage(
        currentImage.getWidth(), currentImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < currentImage.getHeight(); y++) {
      for (int x = 0; x < currentImage.getWidth(); x++) {
        int rgb = currentImage.getRGB(x, y);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;

        int newR, newG, newB;
        switch (channel) {
          case RED:
            newR = r;
            newG = 0;
            newB = 0;
            break;
          case GREEN:
            newR = 0;
            newG = g;
            newB = 0;
            break;
          case BLUE:
            newR = 0;
            newG = 0;
            newB = b;
            break;
          default:
            newR = newG = newB = 0;
        }
        int newRgb = (newR << 16) | (newG << 8) | newB;
        result.setRGB(x, y, newRgb);
      }
    }
    displayImage(result);
    updateStatus("Показан " + channel.name() + " канал.");
  }

  // ==================== ВАРИАНТ 5 ====================

  private void applyNegative() {
    if (currentImage == null) {
      showNoImageError();
      return;
    }

    BufferedImage result = new BufferedImage(
        currentImage.getWidth(), currentImage.getHeight(),
        BufferedImage.TYPE_INT_RGB);

    for (int y = 0; y < currentImage.getHeight(); y++) {
      for (int x = 0; x < currentImage.getWidth(); x++) {
        int rgb = currentImage.getRGB(x, y);
        int r = 255 - ((rgb >> 16) & 0xFF);
        int g = 255 - ((rgb >> 8) & 0xFF);
        int b = 255 - (rgb & 0xFF);
        int newRgb = (r << 16) | (g << 8) | b;
        result.setRGB(x, y, newRgb);
      }
    }
    displayImage(result);
    updateStatus("Применён эффект негатива.");
  }

  private void applyGaussianBlur() {
    if (currentImage == null) {
      showNoImageError();
      return;
    }

    String input = JOptionPane.showInputDialog(this,
        "Введите размер ядра (нечётное число, например 3, 5, 7):",
        "Размытие по Гауссу", JOptionPane.QUESTION_MESSAGE);

    if (input == null) {
      return;
    }

    try {
      int kernelSize = Integer.parseInt(input.trim());
      if (kernelSize % 2 == 0 || kernelSize < 3) {
        throw new NumberFormatException();
      }

      Mat src = bufferedImageToMat(currentImage);
      Mat dst = new Mat();
      Imgproc.GaussianBlur(src, dst, new Size(kernelSize, kernelSize), 0);
      BufferedImage result = matToBufferedImage(dst);

      displayImage(result);
      updateStatus("Применено размытие по Гауссу с ядром " + kernelSize);

    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this,
          "Ошибка: размер ядра должен быть нечётным числом >= 3.",
          "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void drawCircle() {
    if (currentImage == null) {
      showNoImageError();
      return;
    }

    JTextField xField = new JTextField();
    JTextField yField = new JTextField();
    JTextField radiusField = new JTextField();

    Object[] inputs = {
        "X координата центра (0-" + currentImage.getWidth() + "):", xField,
        "Y координата центра (0-" + currentImage.getHeight() + "):", yField,
        "Радиус круга:", radiusField
    };

    int result = JOptionPane.showConfirmDialog(this, inputs,
        "Введите параметры круга", JOptionPane.OK_CANCEL_OPTION);

    if (result != JOptionPane.OK_OPTION) {
      return;
    }

    try {
      int x = Integer.parseInt(xField.getText().trim());
      int y = Integer.parseInt(yField.getText().trim());
      int radius = Integer.parseInt(radiusField.getText().trim());

      if (radius <= 0) {
        throw new NumberFormatException();
      }

      Mat imageMat = bufferedImageToMat(currentImage);
      Point center = new Point(x, y);
      Scalar redColor = new Scalar(0, 0, 255);
      Imgproc.circle(imageMat, center, radius, redColor, 3);

      BufferedImage resultImage = matToBufferedImage(imageMat);
      displayImage(resultImage);
      updateStatus("Нарисован круг в центре (" + x + ", " + y + ") с радиусом " + radius);

    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this,
          "Ошибка: координаты и радиус должны быть целыми положительными числами.",
          "Ошибка ввода", JOptionPane.ERROR_MESSAGE);
    }
  }

  // ==================== ДОПОЛНИТЕЛЬНЫЕ ФУНКЦИИ ====================

  private void resetToOriginal() {
    if (originalImage == null) {
      showNoImageError();
      return;
    }
    displayImage(copyImage(originalImage));
    updateStatus("Сброшено к оригинальному изображению.");
  }

  private void saveImage() {
    if (currentImage == null) {
      showNoImageError();
      return;
    }

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setSelectedFile(new File("processed_image.png"));
    int result = fileChooser.showSaveDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      File file = fileChooser.getSelectedFile();
      try {
        String fileName = file.getName().toLowerCase();
        String format = fileName.endsWith(".png") ? "png" : "jpg";
        ImageIO.write(currentImage, format, file);
        updateStatus("Изображение сохранено: " + file.getName());
      } catch (IOException e) {
        JOptionPane.showMessageDialog(this,
            "Ошибка сохранения: " + e.getMessage(),
            "Ошибка", JOptionPane.ERROR_MESSAGE);
        updateStatus("Ошибка сохранения: " + e.getMessage());
      }
    }
  }

  private void showNoImageError() {
    JOptionPane.showMessageDialog(this,
        "Сначала загрузите изображение или сделайте снимок с веб-камеры.",
        "Нет изображения", JOptionPane.WARNING_MESSAGE);
  }

  // ==================== КОНВЕРТАЦИЯ BUFFEREDIMAGE <-> MAT ====================

  /**
   * Конвертирует BufferedImage в Mat (BGR для OpenCV).
   * Исправленная версия без использования метода data().
   */
  private Mat bufferedImageToMat(BufferedImage image) {
    Mat mat = new Mat(image.getHeight(), image.getWidth(), org.opencv.core.CvType.CV_8UC3);
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        int rgb = image.getRGB(x, y);
        int b = rgb & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int r = (rgb >> 16) & 0xFF;
        byte[] pixel = new byte[]{(byte) b, (byte) g, (byte) r};
        mat.put(y, x, pixel);
      }
    }
    return mat;
  }

  /**
   * Конвертирует Mat в BufferedImage.
   * Исправленная версия без использования метода data().
   */
  private BufferedImage matToBufferedImage(Mat mat) {
    BufferedImage image = new BufferedImage(mat.width(), mat.height(),
        BufferedImage.TYPE_INT_RGB);
    byte[] data = new byte[mat.width() * mat.height() * (int) mat.elemSize()];
    mat.get(0, 0, data);
    for (int y = 0; y < mat.height(); y++) {
      for (int x = 0; x < mat.width(); x++) {
        int index = (y * mat.width() + x) * 3;
        int b = data[index] & 0xFF;
        int g = data[index + 1] & 0xFF;
        int r = data[index + 2] & 0xFF;
        int rgb = (r << 16) | (g << 8) | b;
        image.setRGB(x, y, rgb);
      }
    }
    return image;
  }

  /** Точка входа в приложение. */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(ImageProcessorApp::new);
  }
}