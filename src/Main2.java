import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Main2 {

    // Classe pour stocker les résultats
    static class ResultatCompression {
        byte[] donneesCompressees;
        Map<Character, String> codes;
        int tailleOriginale;
        int padding;
        boolean estImage;

        ResultatCompression(byte[] donnees, Map<Character, String> codes, int taille, int padding, boolean estImage) {
            this.donneesCompressees = donnees;
            this.codes = codes;
            this.tailleOriginale = taille;
            this.padding = padding;
            this.estImage = estImage;
        }
    }

    // COMPRESSER UN FICHIER
    public static ResultatCompression compresserFichier(String cheminFichier) throws IOException {
        // Lire le fichier complet
        String contenu = lireFichier(cheminFichier);

        // Compresser avec Huffman
        ResultatCompression resultat = compresser(contenu);
        resultat.estImage = cheminFichier.toLowerCase().endsWith(".png") ||
                cheminFichier.toLowerCase().endsWith(".jpg") ||
                cheminFichier.toLowerCase().endsWith(".jpeg") ||
                cheminFichier.toLowerCase().endsWith(".gif") ||
                cheminFichier.toLowerCase().endsWith(".bmp");

        return resultat;
    }

    // LIRE UN FICHIER (texte ou image)
    public static String lireFichier(String chemin) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(chemin));

        // Pour les images, encoder en Base64 pour le traitement
        if (chemin.toLowerCase().matches(".*\\.(png|jpg|jpeg|gif|bmp)$")) {
            return Base64.getEncoder().encodeToString(bytes);
        } else {
            // Pour les fichiers texte
            return new String(bytes, "UTF-8");
        }
    }

    // ÉCRIRE UN FICHIER DÉCOMPRESSÉ
    public static void ecrireFichier(String chemin, String contenu, boolean estImage) throws IOException {
        // Créer le dossier parent si nécessaire
        Path path = Paths.get(chemin);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        if (estImage) {
            // Décoder le Base64 pour les images
            byte[] bytes = Base64.getDecoder().decode(contenu);
            Files.write(path, bytes);
        } else {
            // Écrire le texte normalement
            Files.write(path, contenu.getBytes("UTF-8"));
        }
    }

    // ALGORITHME HUFFMAN
    public static ResultatCompression compresser(String texte) {
        // 1. Compter les fréquences
        Map<Character, Integer> frequences = new HashMap<>();
        for (char c : texte.toCharArray()) {
            frequences.put(c, frequences.getOrDefault(c, 0) + 1);
        }

        // 2. Créer l'arbre
        PriorityQueue<Noeud> file = new PriorityQueue<>((a, b) -> a.freq - b.freq);
        for (Map.Entry<Character, Integer> entry : frequences.entrySet()) {
            file.add(new Noeud(entry.getKey(), entry.getValue()));
        }

        // Cas spécial : fichier avec un seul caractère répété
        if (file.size() == 1) {
            Noeud seul = file.poll();
            Map<Character, String> codes = new HashMap<>();
            codes.put(seul.caractere, "0");
            byte[] bytes = bitsToBytes("0".repeat(texte.length()));
            int padding = (8 - (texte.length() % 8)) % 8;
            return new ResultatCompression(bytes, codes, texte.length(), padding, false);
        }

        while (file.size() > 1) {
            Noeud gauche = file.poll();
            Noeud droite = file.poll();
            file.add(new Noeud(gauche.freq + droite.freq, gauche, droite));
        }

        // 3. Générer les codes
        Map<Character, String> codes = new HashMap<>();
        genererCodes(file.peek(), "", codes);

        // 4. Coder le texte
        StringBuilder bits = new StringBuilder();
        for (char c : texte.toCharArray()) {
            String code = codes.get(c);
            if (code == null) {
                throw new RuntimeException("Caractère non trouvé dans les codes: " + c);
            }
            bits.append(code);
        }

        // 5. Convertir en bytes
        byte[] bytes = bitsToBytes(bits.toString());
        int padding = (8 - (bits.length() % 8)) % 8;

        return new ResultatCompression(bytes, codes, texte.length(), padding, false);
    }

    // DÉCOMPRESSER
    public static String decompresser(ResultatCompression resultat) {
        String bits = bytesToBits(resultat.donneesCompressees, resultat.padding);
        Noeud racine = reconstruireArbre(resultat.codes);

        StringBuilder texte = new StringBuilder();
        Noeud courant = racine;

        for (char bit : bits.toCharArray()) {
            courant = (bit == '0') ? courant.gauche : courant.droite;

            if (courant == null) {
                throw new RuntimeException("Chemin invalide dans l'arbre de Huffman");
            }

            if (courant.estFeuille()) {
                texte.append(courant.caractere);
                courant = racine;
            }
        }

        return texte.toString();
    }

    // SAUVEGARDER en .huff
    public static void sauvegarder(ResultatCompression resultat, String nomFichier) throws IOException {
        // Créer le dossier parent si nécessaire
        Path path = Paths.get(nomFichier);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(nomFichier))) {
            dos.writeUTF("HUFF");
            dos.writeInt(resultat.tailleOriginale);
            dos.writeByte(resultat.padding);
            dos.writeBoolean(resultat.estImage);

            // Codes
            dos.writeInt(resultat.codes.size());
            for (Map.Entry<Character, String> entry : resultat.codes.entrySet()) {
                dos.writeChar(entry.getKey());
                dos.writeUTF(entry.getValue());
            }

            // Données
            dos.writeInt(resultat.donneesCompressees.length);
            dos.write(resultat.donneesCompressees);
        }
    }

    // CHARGER depuis .huff
    public static ResultatCompression charger(String nomFichier) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(nomFichier))) {
            if (!dis.readUTF().equals("HUFF")) {
                throw new IOException("Fichier .huff invalide");
            }

            int tailleOriginale = dis.readInt();
            int padding = dis.readByte();
            boolean estImage = dis.readBoolean();

            // Charger codes
            int tailleCodes = dis.readInt();
            Map<Character, String> codes = new HashMap<>();
            for (int i = 0; i < tailleCodes; i++) {
                char caractere = dis.readChar();
                String code = dis.readUTF();
                codes.put(caractere, code);
            }

            // Charger données
            int tailleDonnees = dis.readInt();
            byte[] donnees = new byte[tailleDonnees];
            dis.readFully(donnees);

            return new ResultatCompression(donnees, codes, tailleOriginale, padding, estImage);
        }
    }

    // METHODES UTILITAIRES
    static class Noeud {
        char caractere;
        int freq;
        Noeud gauche, droite;

        Noeud(char c, int f) { this.caractere = c; this.freq = f; }
        Noeud(int f, Noeud g, Noeud d) { this.freq = f; this.gauche = g; this.droite = d; }

        boolean estFeuille() { return gauche == null && droite == null; }
    }

    static void genererCodes(Noeud noeud, String code, Map<Character, String> codes) {
        if (noeud == null) return;
        if (noeud.estFeuille()) {
            codes.put(noeud.caractere, code.isEmpty() ? "0" : code);
            return;
        }
        genererCodes(noeud.gauche, code + "0", codes);
        genererCodes(noeud.droite, code + "1", codes);
    }

    static byte[] bitsToBytes(String bits) {
        if (bits.isEmpty()) {
            return new byte[0];
        }

        int byteCount = (bits.length() + 7) / 8;
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.charAt(i) == '1') {
                bytes[i / 8] |= (1 << (7 - (i % 8)));
            }
        }
        return bytes;
    }

    static String bytesToBits(byte[] bytes, int padding) {
        if (bytes.length == 0) {
            return "";
        }

        StringBuilder bits = new StringBuilder();
        int totalBits = bytes.length * 8 - padding;
        for (int i = 0; i < totalBits; i++) {
            int byteIndex = i / 8;
            int bitIndex = 7 - (i % 8);
            boolean isSet = ((bytes[byteIndex] >> bitIndex) & 1) == 1;
            bits.append(isSet ? '1' : '0');
        }
        return bits.toString();
    }

    static Noeud reconstruireArbre(Map<Character, String> codes) {
        Noeud racine = new Noeud(0, null, null);
        for (Map.Entry<Character, String> entry : codes.entrySet()) {
            Noeud courant = racine;
            String code = entry.getValue();
            for (char bit : code.toCharArray()) {
                if (bit == '0') {
                    if (courant.gauche == null) courant.gauche = new Noeud(0, null, null);
                    courant = courant.gauche;
                } else {
                    if (courant.droite == null) courant.droite = new Noeud(0, null, null);
                    courant = courant.droite;
                }
            }
            courant.caractere = entry.getKey();
        }
        return racine;
    }

    // INTERFACE UTILISATEUR CORRIGÉE
    public static void main2(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== COMPRESSEUR HUFFMAN FICHIERS ===");

        while (true) {
            System.out.println("\n1. Compresser un fichier");
            System.out.println("2. Décompresser un .huff");
            System.out.println("3. Quitter");
            System.out.print("Votre choix: ");

            try {
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("❌ Veuillez entrer un choix!");
                    continue;
                }

                int choix = Integer.parseInt(input);

                switch (choix) {
                    case 1:
                        System.out.print("Chemin du fichier à compresser: ");
                        String fichierEntree = scanner.nextLine().trim();

                        // Vérifier si le fichier existe
                        File testFichier = new File(fichierEntree);
                        if (!testFichier.exists()) {
                            System.out.println("❌ Fichier non trouvé: " + fichierEntree);
                            break;
                        }

                        System.out.print("Nom du fichier .huff: ");
                        String fichierSortie = scanner.nextLine().trim();

                        try {
                            // Compression
                            ResultatCompression resultat = compresserFichier(fichierEntree);
                            sauvegarder(resultat, fichierSortie);

                            // Calcul gain
                            File fichierOriginal = new File(fichierEntree);
                            File fichierCompresse = new File(fichierSortie);
                            double gain = 100.0 * (fichierOriginal.length() - fichierCompresse.length()) / fichierOriginal.length();

                            System.out.println("✓ Compression réussie!");
                            System.out.println("Taille originale: " + fichierOriginal.length() + " bytes");
                            System.out.println("Taille compressée: " + fichierCompresse.length() + " bytes");
                            System.out.println("Gain: " + String.format("%.1f", gain) + "%");

                        } catch (Exception e) {
                            System.out.println("❌ Erreur lors de la compression: " + e.getMessage());
                        }
                        break;

                    case 2:
                        System.out.print("Fichier .huff à décompresser: ");
                        String fichierHuff = scanner.nextLine().trim();

                        // Vérifier si le fichier existe
                        File testHuff = new File(fichierHuff);
                        if (!testHuff.exists()) {
                            System.out.println("❌ Fichier .huff non trouvé: " + fichierHuff);
                            break;
                        }

                        System.out.print("Nom du fichier de sortie: ");
                        String fichierDecompresse = scanner.nextLine().trim();

                        try {
                            // Décompression
                            ResultatCompression charge = charger(fichierHuff);
                            String contenu = decompresser(charge);
                            ecrireFichier(fichierDecompresse, contenu, charge.estImage);

                            System.out.println("✓ Décompression réussie: " + fichierDecompresse);

                        } catch (Exception e) {
                            System.out.println("❌ Erreur lors de la décompression: " + e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.println("Au revoir!");
                        return;

                    default:
                        System.out.println("❌ Choix invalide! Tapez 1, 2 ou 3.");
                }

            } catch (NumberFormatException e) {
                System.out.println("❌ Veuillez entrer un nombre (1, 2 ou 3)!");
            } catch (Exception e) {
                System.out.println("❌ Erreur inattendue: " + e.getMessage());
            }
        }
    }
}