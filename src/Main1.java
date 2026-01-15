import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class Main1 {

    public static void main1(String[] args) {
        //On définit le chemin d'accès à l'image
        String inputImagePath = "C://Users//HP//Pictures/Capture d’écran 2025-02-13 132410.png";

        try {
            //On charge l'image originale dans un objet BufferedImage
            BufferedImage originalImage = ImageIO.read(new File(inputImagePath));

            //I-APPEL AU TRAITEMENT
            //On Crée une instance du processeur d'image
            ImageProcessor processor = new ImageProcessor();

            // On appelle la méthode qui effectue le sous-échantillonnage
            BufferedImage compressedImage = processor.applyChromaSubsampling(originalImage);

            //Sauvegarder l'image compressée (reconstruite)
            String outputImagePath = "C://Users//HP//Desktop/Capture d’écran 2025-02-13 132410.png";

            ImageIO.write(compressedImage, "png", new File(outputImagePath));

            System.out.println("Compression 4:2:0 terminée. Image sauvegardée à : " +outputImagePath);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors du traitement de l'image.");
        }
    }
}