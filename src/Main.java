import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends JFrame {
    private final Color backgroundColor = new Color(25, 25, 35); // Noir gris avec 10% bleu

    public Main() {
        setTitle("Application de Compression");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        initializeUI();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(4, 1, 20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        mainPanel.setBackground(backgroundColor);

        // Bouton Compresser un texte (bleu)
        JButton compressTextBtn = createButton("Compresser un texte", new Color(0, 100, 255));
        compressTextBtn.addActionListener(e -> handleTextCompression());

        // Bouton Compresser une image (vert)
        JButton compressImageBtn = createButton("Compresser une image", new Color(0, 180, 0));
        compressImageBtn.addActionListener(e -> handleImageCompression());

        // Bouton Décompresser un texte (jaune)
        JButton decompressTextBtn = createButton("Décompresser un texte", new Color(255, 200, 0));
        decompressTextBtn.addActionListener(e -> handleTextDecompression());

        // Bouton Décompresser une image (blanc)
        JButton decompressImageBtn = createButton("Décompresser une image", Color.WHITE);
        decompressImageBtn.addActionListener(e -> handleImageDecompression());

        mainPanel.add(compressTextBtn);
        mainPanel.add(compressImageBtn);
        mainPanel.add(decompressTextBtn);
        mainPanel.add(decompressImageBtn);

        add(mainPanel);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setPreferredSize(new Dimension(300, 60));

        // Effet de survol
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(color);
            }
        });

        return button;
    }

    private void handleTextCompression() {
        String filePath = showFileChooser("Sélectionnez le fichier texte à compresser");
        if (filePath != null) {
            try {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));

                // Demander le type de compression
                String[] options = {"RLE", "Huffman"};
                int choice = JOptionPane.showOptionDialog(this,
                        "Choisissez la méthode de compression:",
                        "Type de Compression",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (choice == 0) {
                    // Compression RLE
                    String compressed = RLE.compresser(content);
                    String outputPath = getOutputPath("compresse_rle.txt");
                    Files.write(Paths.get(outputPath), compressed.getBytes());
                } else {
                    // Compression Huffman
                    Main2.ResultatCompression result = Main2.compresser(content);
                    String outputPath = getOutputPath("compresse_huff.huff");
                    Main2.sauvegarder(result, outputPath);
                }

                showCompletionMessage();
            } catch (Exception ex) {
                showError("Erreur lors de la compression: " + ex.getMessage());
            }
        }
    }

    private void handleTextDecompression() {
        String filePath = showFileChooser("Sélectionnez le fichier à décompresser");
        if (filePath != null) {
            try {
                if (filePath.toLowerCase().endsWith(".rle") || filePath.toLowerCase().endsWith(".txt")) {
                    // Décompression RLE
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    String decompressed = RLE_Decompression.decompresser(content);
                    String outputPath = getOutputPath("decompresse.txt");
                    Files.write(Paths.get(outputPath), decompressed.getBytes());
                } else if (filePath.toLowerCase().endsWith(".huff")) {
                    // Décompression Huffman
                    Main2.ResultatCompression result = Main2.charger(filePath);
                    String decompressed = Main2.decompresser(result);
                    String outputPath = getOutputPath("decompresse.txt");
                    Files.write(Paths.get(outputPath), decompressed.getBytes());
                } else {
                    showError("Format de fichier non reconnu pour la décompression texte");
                    return;
                }

                showCompletionMessage();
            } catch (Exception ex) {
                showError("Erreur lors de la décompression: " + ex.getMessage());
            }
        }
    }

    private void handleImageCompression() {
        String filePath = showFileChooser("Sélectionnez l'image à compresser");
        if (filePath != null) {
            try {
                // Demander le type de compression
                String[] options = {"Chroma Subsampling", "Huffman"};
                int choice = JOptionPane.showOptionDialog(this,
                        "Choisissez la méthode de compression:",
                        "Type de Compression",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);

                if (choice == 0) {
                    // Compression Chroma Subsampling
                    java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(new File(filePath));
                    ImageProcessor processor = new ImageProcessor();
                    java.awt.image.BufferedImage compressedImage = processor.applyChromaSubsampling(originalImage);
                    if (compressedImage != null) {
                        String outputPath = getOutputPath("compresse_image.png");
                        javax.imageio.ImageIO.write(compressedImage, "png", new File(outputPath));
                    } else {
                        showError("Erreur lors de la compression Chroma Subsampling");
                        return;
                    }
                } else {
                    // Compression Huffman
                    Main2.ResultatCompression result = Main2.compresserFichier(filePath);
                    String outputPath = getOutputPath("compresse_image.huff");
                    Main2.sauvegarder(result, outputPath);
                }

                showCompletionMessage();
            } catch (Exception ex) {
                showError("Erreur lors de la compression d'image: " + ex.getMessage());
            }
        }
    }

    private void handleImageDecompression() {
        String filePath = showFileChooser("Sélectionnez le fichier image compressé");
        if (filePath != null) {
            try {
                if (filePath.toLowerCase().endsWith(".huff")) {
                    // Décompression Huffman
                    Main2.ResultatCompression result = Main2.charger(filePath);
                    String decompressed = Main2.decompresser(result);
                    String outputPath = getOutputPath("image_decompresse.png");
                    Main2.ecrireFichier(outputPath, decompressed, true);
                } else {
                    showError("Format de fichier non supporté pour la décompression d'image");
                    return;
                }

                showCompletionMessage();
            } catch (Exception ex) {
                showError("Erreur lors de la décompression d'image: " + ex.getMessage());
            }
        }
    }

    private String showFileChooser(String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    private String getOutputPath(String defaultName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Sauvegarder le fichier résultant");
        fileChooser.setSelectedFile(new File(defaultName));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return new File(defaultName).getAbsolutePath();
    }

    private void showCompletionMessage() {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(backgroundColor);

        JLabel messageLabel = new JLabel("Opération terminée! Cliquez n'importe où pour revenir au menu.", JLabel.CENTER);
        messageLabel.setForeground(Color.MAGENTA);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));

        messagePanel.add(messageLabel, BorderLayout.CENTER);

        // Créer une fenêtre modale
        JDialog dialog = new JDialog(this, "Succès", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setSize(500, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setContentPane(messagePanel);

        // Fermer quand on clique n'importe où
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
            }
        };

        dialog.addMouseListener(clickListener);
        messagePanel.addMouseListener(clickListener);
        messageLabel.addMouseListener(clickListener);

        dialog.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}