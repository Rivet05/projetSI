# Compresseur de Texte et d'Images

## Description du Projet

Application Java complète offrant des fonctionnalités avancées de compression et de décompression pour les fichiers texte et images. Le projet implémente trois algorithmes de compression distincts avec une interface graphique intuitive.

## Fonctionnalités Principales

### Compression de Texte
- **RLE (Run-Length Encoding)** : Compression sans perte basée sur la répétition de caractères
- **Huffman** : Compression statistique basée sur la fréquence des caractères

### Compression d'Images
- **Chroma Subsampling (4:2:0)** : Compression avec perte basée sur la réduction de la chrominance
- **Huffman** : Compression sans perte pour les données d'image encodées en Base64

## Architecture du Projet

### Structure des Fichiers

```
projet/
├── Main.java                    # Interface graphique principale
├── Main1.java                   # Test pour Chroma Subsampling
├── Main2.java                   # Implémentation Huffman pour fichiers
├── RLE.java                     # Compression RLE
├── RLE_Decompression.java       # Décompression RLE
├── ColorConverter.java          # Conversions RGB ↔ YCbCr
└── ImageProcessor.java          # Traitement d'images (4:2:0)
```

## Technologies Utilisées

- **Langage** : Java (SE 8+)
- **GUI** : Swing (JFrame, JPanel, JButton, JFileChooser)
- **Traitement d'images** : java.awt.image.BufferedImage, javax.imageio.ImageIO
- **Structures de données** : HashMap, PriorityQueue, StringBuilder
- **I/O** : java.nio.file, DataInputStream/DataOutputStream

## Description Détaillée des Algorithmes

### 1. RLE (Run-Length Encoding)

**Principe** : Remplace les séquences de caractères répétés par un compteur suivi du caractère.

**Exemple** :
```
Texte original  : "aaabbbccccc"
Texte compressé : "3a3b5c"
```

**Fichiers concernés** :
- `RLE.java` : Méthode `compresser(String texte)`
- `RLE_Decompression.java` : Méthode `decompresser(String compresse)`

**Performances** :
- Efficace pour les textes avec beaucoup de répétitions
- Peut augmenter la taille si peu de répétitions
- Gain typique : 20-60% selon le contenu

### 2. Huffman

**Principe** : Crée un arbre binaire basé sur les fréquences des caractères. Les caractères fréquents obtiennent des codes courts.

**Processus de compression** :
1. Calcul des fréquences de chaque caractère
2. Construction d'un arbre binaire avec une PriorityQueue
3. Génération des codes binaires pour chaque caractère
4. Encodage du texte avec ces codes
5. Conversion des bits en bytes

**Processus de décompression** :
1. Reconstruction de l'arbre à partir des codes sauvegardés
2. Parcours de l'arbre selon les bits
3. Décodage caractère par caractère

**Format de fichier .huff** :
```
- Signature "HUFF" (4 bytes)
- Taille originale (int)
- Padding (byte)
- Flag image (boolean)
- Nombre de codes (int)
- Pour chaque code :
  - Caractère (char)
  - Code binaire (String UTF)
- Taille des données (int)
- Données compressées (bytes)
```

**Fichier concerné** :
- `Main2.java` : Classe complète avec toutes les méthodes

**Performances** :
- Gain typique : 30-70% pour du texte
- Toujours efficace (ne peut pas augmenter significativement la taille)
- Temps de compression : O(n log n)

### 3. Chroma Subsampling (4:2:0)

**Principe** : Exploite la moindre sensibilité de l'œil humain aux variations de couleur par rapport à la luminosité.

**Processus détaillé** :

#### Étape 1 : Conversion RGB → YCbCr
```java
Y  = 0.299*R + 0.587*G + 0.114*B        // Luminance
Cb = -0.1687*R - 0.3313*G + 0.5*B + 128 // Chrominance bleue
Cr = 0.5*R - 0.4187*G - 0.0813*B + 128  // Chrominance rouge
```

#### Étape 2 : Sous-échantillonnage 4:2:0
- **Luminance (Y)** : Conservée à pleine résolution
- **Chrominance (Cb, Cr)** : Réduite de moitié en largeur et hauteur

Pour chaque bloc 2×2 pixels :
```
Original 2×2 :     Sous-échantillonné :
[Cb1 Cb2]              [Cb_avg]
[Cb3 Cb4]         →    

Cb_avg = (Cb1 + Cb2 + Cb3 + Cb4) / 4
```

#### Étape 3 : Sur-échantillonnage et reconstruction
- Chaque pixel (x,y) récupère la chrominance du pixel (x/2, y/2) sous-échantillonné
- Conversion inverse YCbCr → RGB

**Formules inverses** :
```java
R = Y + 1.402 * (Cr - 128)
G = Y - 0.344136 * (Cb - 128) - 0.714136 * (Cr - 128)
B = Y + 1.772 * (Cb - 128)
```

**Fichiers concernés** :
- `ColorConverter.java` : Conversions de couleurs
- `ImageProcessor.java` : Algorithme 4:2:0 complet
- `Main1.java` : Test de l'algorithme

**Performances** :
- Réduction théorique : 50% de la chrominance
- Qualité visuelle : Excellente pour photos naturelles
- Perte minimale perceptible pour l'œil humain

**Bug identifié** : Dans `ColorConverter.yccToRgb()`, lignes 22-24, les trois valeurs finales utilisent `R` au lieu de `R`, `G`, `B` respectivement.

## Interface Graphique

### Design
- **Couleur de fond** : RGB(25, 25, 35) - Gris foncé avec teinte bleue
- **Boutons colorés** :
    - 🔵 Bleu : Compresser un texte
    - 🟢 Vert : Compresser une image
    - 🟡 Jaune : Décompresser un texte
    - ⚪ Blanc : Décompresser une image

### Flux d'utilisation

#### Compression de texte
1. Cliquer sur "Compresser un texte"
2. Sélectionner le fichier texte
3. Choisir RLE ou Huffman
4. Sauvegarder le fichier compressé (.txt pour RLE, .huff pour Huffman)

#### Décompression de texte
1. Cliquer sur "Décompresser un texte"
2. Sélectionner le fichier compressé (.rle, .txt ou .huff)
3. Le format est détecté automatiquement
4. Sauvegarder le fichier décompressé

#### Compression d'image
1. Cliquer sur "Compresser une image"
2. Sélectionner l'image (PNG, JPG, JPEG, GIF, BMP)
3. Choisir Chroma Subsampling ou Huffman
4. Sauvegarder le résultat (.png pour Chroma, .huff pour Huffman)

#### Décompression d'image
1. Cliquer sur "Décompresser une image"
2. Sélectionner le fichier .huff
3. Sauvegarder l'image décompressée

## Installation et Exécution

### Prérequis
- JDK 8 ou supérieur
- IDE Java (Eclipse, IntelliJ IDEA, NetBeans) ou ligne de commande

### Compilation
```bash
javac *.java
```

### Exécution
```bash
java Main
```

##  Exemples de Résultats

### RLE
```
Entrée  : "aaaaaabbbbcccc" (14 caractères)
Sortie  : "6a4b4c" (6 caractères)
Gain    : 57.14%
```

### Huffman (exemple typique)
```
Fichier texte : 10,000 bytes
Fichier .huff : 6,500 bytes
Gain          : 35%
```

### Chroma Subsampling
```
Image originale : 1920×1080 pixels
- Y  : 1920×1080 = 2,073,600 valeurs
- Cb : 960×540 = 518,400 valeurs
- Cr : 960×540 = 518,400 valeurs
Réduction chrominance : 50%
```

##  Limitations et Contraintes

### Chroma Subsampling
-  **Dimensions paires obligatoires** : L'image doit avoir une largeur et hauteur paires
- Compression avec perte (non réversible)
- Format de sortie : PNG uniquement

### Huffman
- Fichiers très petits : overhead du dictionnaire
- Mémoire : Stockage de l'arbre complet

### RLE
- Inefficace si peu de répétitions
- Peut augmenter la taille du fichier

## Bugs Connus

### Bug Critique - ColorConverter.java
**Ligne 22-24** : Erreur de copier-coller dans la méthode `yccToRgb()`

**Code actuel (incorrect)** :
```java
int finalR = (int) Math.round(Math.max(0, Math.min(255, R)));
int finalG = (int) Math.round(Math.max(0, Math.min(255, R))); // ❌ Utilise R
int finalB = (int) Math.round(Math.max(0, Math.min(255, R))); // ❌ Utilise R
```

**Code corrigé** :
```java
int finalR = (int) Math.round(Math.max(0, Math.min(255, R)));
int finalG = (int) Math.round(Math.max(0, Math.min(255, G))); // ✅ Utilise G
int finalB = (int) Math.round(Math.max(0, Math.min(255, B))); // ✅ Utilise B
```

**Impact** : Les images décompressées apparaissent avec des couleurs incorrectes (teinte rougeâtre).

## 🔄 Améliorations Possibles

### Court terme
1. ✅ Corriger le bug dans `ColorConverter.java`
2. Ajouter validation des dimensions d'image avant traitement
3. Afficher statistiques de compression dans l'interface

### Moyen terme
4. Implémenter d'autres schémas de subsampling (4:2:2, 4:4:4)
5. Ajouter prévisualisation avant/après compression
6. Support de formats d'image supplémentaires

### Long terme
7. Compression JPEG complète avec DCT et quantification
8. Mode batch pour traiter plusieurs fichiers
9. Graphiques de comparaison des algorithmes
10. Multi-threading pour grandes images

## Références Théoriques

### Standards et Normes
- **BT.601** : Norme de conversion RGB-YCbCr (utilisée dans ce projet)
- **Chroma Subsampling** : Technique utilisée dans JPEG, MPEG, H.264

### Algorithmes
- **Huffman** : David A. Huffman (1952) - "A Method for the Construction of Minimum-Redundancy Codes"
- **RLE** : Utilisé depuis les années 1960, base de nombreux formats (BMP, PCX)

#