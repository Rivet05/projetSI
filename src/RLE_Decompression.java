import java.io.*;
import java.util.Scanner;

public class RLE_Decompression {

    // Méthode pour décompresser une chaîne compressée avec RLE
    public static String decompresser(String compresse) {

        // Vérifie si la chaîne est null ou vide
        if (compresse == null || compresse.isEmpty()) return "";

        StringBuilder resultat = new StringBuilder(); // Stocker le texte décompressé
        StringBuilder nombre = new StringBuilder();   // Stocker temporairement le compteur

        // Parcourt chaque caractère du texte compressé
        for (char c : compresse.toCharArray()) {

            // Si le caractère est un chiffre, il fait partie du compteur
            if (Character.isDigit(c)) {
                nombre.append(c);
            } else {
                // Convertir le compteur en entier
                int repetitions = Integer.parseInt(nombre.toString());

                // Ajouter le caractère répété
                for (int i = 0; i < repetitions; i++) {
                    resultat.append(c);
                }

                // Réinitialiser le compteur pour la prochaine séquence
                nombre.setLength(0);
            }
        }

        return resultat.toString();
    }

    // Méthode principale
    public static void main3(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Demander le chemin du fichier compressé
        System.out.print("Entrez le chemin du fichier compressé (ex: texte_compresse.txt) : ");
        String chemin = sc.nextLine();

        try {
            // Lire le contenu du fichier compressé
            File file = new File(chemin);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder compresse = new StringBuilder();
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                compresse.append(ligne); // Concatène toutes les lignes
            }
            reader.close();

            String texteDecompresse = decompresser(compresse.toString());


            System.out.println("Texte décompressé : " + texteDecompresse);
            int compressedLength = compresse.length();
            int decompressedLength = texteDecompresse.length();
            int gain = decompressedLength - compressedLength;
            double pourcentage = (double) gain / decompressedLength * 100;

            System.out.println("Taille compressée : " + compressedLength);
            System.out.println("Taille décompressée : " + decompressedLength);
            System.out.println("Gain récupéré : " + gain + " caractères (" + String.format("%.2f", pourcentage) + "%)");

            // Sauvegarder le texte décompressé
            FileWriter writer = new FileWriter("texte_decompresse.txt");
            writer.write(texteDecompresse);
            writer.close();
            System.out.println("Texte décompressé sauvegardé dans : texte_decompresse.txt");

        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture ou de l'écriture du fichier.");
            e.printStackTrace();
        }

        sc.close();
    }
}
