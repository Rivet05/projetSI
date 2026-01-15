import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProcessor {

    public BufferedImage applyChromaSubsampling(BufferedImage rgbImage) {
        int width = rgbImage.getWidth();
        int height = rgbImage.getHeight();

        //On s'assure que les dimensions sont paires
        if (width % 2 != 0 || height % 2 != 0) {
            System.err.println("L'image doit avoir des dimensions paires pour 4:2:0 simple.");
            return null;
        }
        // 1) On convertit RGB en YCbCr
        double[][] Y = new double[height][width];
        double[][] Cb = new double[height][width];
        double[][] Cr = new double[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = rgbImage.getRGB(x, y);
                //On extrait les composantes R, G, B du pixel
                Color color = new Color(rgb);

                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                //Appeler la méthode pour la conversion
                ColorConverter c = new ColorConverter();
                double[] ycc = ColorConverter.rgbToYcc(r, g, b);
                Y[y][x] = ycc[0];
                Cb[y][x] = ycc[1];
                Cr[y][x] = ycc[2];
            }
        }

        // 2) SOUS-ECHANTILLONAGE
        //Les matrices sous-échantillonnées sont 2 fois plus petites en hauteur et largeur
        int subWidth = width / 2;
        int subHeight = height / 2;
        double[][] Cb_sub = new double[subHeight][subWidth];
        double[][] Cr_sub = new double[subHeight][subWidth];

        // On itère sur des blocs de 2x2  pixels de l'image originale
        for (int y = 0; y < subHeight; y++) {
            for (int x = 0; x < subWidth; x++) {
                //Calculer la moyenne des 4 pixels Cb originaux (méthode de simplification)
                double avgCb = (Cb[2 * y][2 * x] + Cb[2 * y + 1][2 * x] + Cb[2 * y][2 * x +1] + Cb[2 * y + 1][2 * x + 1]) / 4.0;
                double avgCr = (Cr[2 * y][2 * x] + Cr[2 * y + 1][2 * x] + Cr[2 * y][2 * x +1] + Cr[2 * y + 1][2 * x + 1]) / 4.0;

                //On stocke la moyenne dans les matrices sous-échantillonnées
                Cb_sub[y][x] = avgCb;
                Cr_sub[y][x] = avgCr;
                //C'est l'étape de la perte d'information
            }
        }

        // 3) RECONSTRUCTION (Sur-échantillonnage et on convertit YCbCr  en RGB)

        BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x  < width; x++) {
                //Récupérer la luminance Y (pleine résolution)
                double yVal = Y[y][x];

                //Récupérer la chrominance sous-échantillonné et répliquer sa valeur
                //L'index (x/2, y/2) donne la position dans la matrice réduite
                int subX = x/2;
                int subY = y/2;

                //Sur-échantillonnage par réplicat< ion : la valeur d'un pixel (x,y) est celle du pixel (subX, subY) dana la matrice réduite
                double cbVal = Cb_sub[subY][subX];
                double crVal = Cr_sub[subY][subX];

                //Conversion inverse
                ColorConverter c = new ColorConverter();
                int[] rgb = c.yccToRgb(yVal, cbVal, crVal);
                int r = rgb[0];
                int g = rgb[1];
                int b = rgb[2];

                //Reconstruire le pixel RGB final pour l'image compréssée
                Color finalColor = new Color(r, g, b);
                int finalRgb = finalColor.getRGB();

                compressedImage.setRGB(x, y, finalRgb);
            }
        }
        return compressedImage;
    }
}