public class ColorConverter {
    //Conversion RGB vers YCbCr (selon la norme BT.601 simplifiée)
    public static double[] rgbToYcc(int r, int g, int b) {
        double Y = 0.299 * r + 0.587 * g + 0.114 * b;
        double Cb = -0.1687 * r - 0.3313 * g + 0.5 * b + 128;
        double Cr = 0.5 * r - 0.4187 * g - 0.0813 * b +128;

        return new double[]{Y, Cb, Cr};
    }

    //Conversion YCbCr vers RGB
    public static int[] yccToRgb(double Y, double Cb, double Cr){
        //Ajustement des offset pour Cb et Cr
        double Cb_offset = Cb - 128;
        double Cr_offset = Cr - 128;

        //Formules inverses
        double R = Y + 1.402 * Cr_offset;
        double G = Y - 0.344136 * Cb_offset - 0.714136 * Cr_offset;
        double B = Y + 1.772 * Cb_offset;

        //On doit s'assurer que les valeurs restent dans l'intervalle [0, 255]
        int finalR = (int) Math.round(Math.max(0, Math.min(255, R)));
        int finalG = (int) Math.round(Math.max(0, Math.min(255, R)));
        int finalB = (int) Math.round(Math.max(0, Math.min(255, R)));

        return new int[]{finalR, finalG, finalB};
    }
}
