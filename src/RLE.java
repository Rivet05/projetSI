import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class RLE{

    // Méthode pour répéter un caractère n fois
    public static String repeatChar(char c, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(c); // Ajouter le caractère c à la chaîne
        }
        return sb.toString(); // Retourner la chaîne répétée
    }

    // Méthode qui compresse une chaîne en utilisant RLE
    public static String compresser(String texte) {

        // Vérifie si le texte est null ou vide
        if (texte == null || texte.isEmpty()) return "";

        // StringBuilder pour construire le texte compressé
        StringBuilder resultat = new StringBuilder();

        // Compteur pour compter les répétitions consécutives
        int compteur = 1;

        // Affichage pédagogique des séquences encodées
        System.out.print("Séquences encodées : ");

        // Boucle qui parcourt le texte à partir du deuxième caractère
        for (int i = 1; i < texte.length(); i++) {

            // Si le caractère actuel est identique au précédent
            if (texte.charAt(i) == texte.charAt(i - 1)) {
                compteur++; // Incrémenter le compteur
            } else {
                // Ajouter la séquence (compteur + caractère précédent) au résultat
                resultat.append(compteur).append(texte.charAt(i - 1));

                // Afficher la séquence pour pédagogie
                System.out.print(compteur + "" + texte.charAt(i - 1) + " ");

                // Réinitialiser le compteur pour la nouvelle séquence
                compteur = 1;
            }
        }

        // Ajouter la dernière séquence après la boucle
        resultat.append(compteur).append(texte.charAt(texte.length() - 1));
        System.out.print(compteur + "" + texte.charAt(texte.length() - 1) + "\n");

        // Retourner le texte compressé
        return resultat.toString();
    }

    // Méthode principale : point d'entrée du programme
    public static void main4(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Demander le texte à compresser
        System.out.print("Entrez un texte à compresser : ");
        String texte = sc.nextLine();

        // Afficher le texte original
        System.out.println("Texte original  : " + texte);

        // Compression
        String compresse = compresser(texte);
        System.out.println("Texte compressé : " + compresse);

        // Calculs pédagogiques : tailles, ratio, gain
        int originalLength = texte.length();
        int compressedLength = compresse.length();
        int gain = originalLength - compressedLength;
        //double ratio = (double) compressedLength / originalLength;
        double pourcentage = (double) gain / originalLength * 100;


        System.out.println("Taille originale : " + originalLength);
        System.out.println("Taille compressée : " + compressedLength);
        //System.out.printf("Ratio de compression : %.2f\n", ratio);
        System.out.println("Gain : " + gain + " caractères (" + String.format("%.2f", pourcentage) + "%)");

        // Sauvegarder le texte compressé dans un fichier
        try (FileWriter writer = new FileWriter("texte_compresse.txt")) {
            writer.write(compresse); // Écrire le texte compressé
            System.out.println("Texte compressé sauvegardé dans le fichier : texte_compresse.txt");
        } catch (IOException e) {
            System.out.println("Erreur lors de l'écriture du fichier.");
            e.printStackTrace();
        }

        sc.close();
    }
}
