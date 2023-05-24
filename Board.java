package mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Board extends JPanel {
	private static final long serialVersionUID = 6195235521361212179L;
	
	private final int NUM_IMAGES = 13;
    private final int CELL_SIZE = 15;

    private final int COVER_FOR_CELL = 10;
    private final int MARK_FOR_CELL = 10;
    private final int EMPTY_CELL = 0;
    private final int MINE_CELL = 9;
    private final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private final int DRAW_MINE = 9;
    private final int DRAW_COVER = 10;
    private final int DRAW_MARK = 11;
    private final int DRAW_WRONG_MARK = 12;

    private int[] field;
    private boolean inGame;
    private int mines_left;
    private Image[] img;
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int all_cells;
    private JLabel statusbar;

public Board(JLabel statusbar) {
    this.statusbar = statusbar; // Définit la barre d'état du jeu
    initializeImages(); // Initialise les images du jeu
    setDoubleBuffered(true); // Active le double buffering pour améliorer les performances d'affichage
    addMouseListener(new MinesAdapter()); // Ajoute un écouteur de souris pour gérer les actions du joueur
    newGame(); // Démarre une nouvelle partie du jeu
}


private void initializeImages() {
    img = new Image[NUM_IMAGES]; // Crée un tableau pour stocker les images du jeu
    for (int i = 0; i < NUM_IMAGES; i++) {
        String imagePath = i + ".gif"; // Chemin de l'image correspondante
        img[i] = loadImage(imagePath); // Charge l'image à partir du fichier et l'ajoute au tableau
    }
}


private Image loadImage(String imagePath) {
    ImageIcon imageIcon = new ImageIcon(getClass().getClassLoader().getResource(imagePath));
    return imageIcon.getImage(); // Charge l'image à partir du chemin spécifié et la renvoie
}


public void newGame() {
    Random random = new Random();
    int i = 0;
    int position = 0;
    int cell = 0;

    inGame = true; // Indique que le jeu est en cours
    mines_left = mines; // Initialise le nombre de mines restantes
    all_cells = rows * cols; // Calcule le nombre total de cellules
    field = new int[all_cells]; // Crée un tableau pour stocker l'état des cellules

    Arrays.fill(field, COVER_FOR_CELL); // Initialise toutes les cellules avec l'état "couverte"
    statusbar.setText(Integer.toString(mines_left)); // Met à jour l'affichage du nombre de mines restantes

    while (i < mines) {
        position = random.nextInt(all_cells);

        if (field[position] != COVERED_MINE_CELL) {
            int current_col = position % cols;
            field[position] = COVERED_MINE_CELL;
            i++;

            if (current_col > 0) {
                cell = position - 1 - cols;
                checkAndIncrementField(cell);

                cell = position - 1;
                checkAndIncrementField(cell);

                cell = position + cols - 1;
                checkAndIncrementField(cell);
            }

            cell = position - cols;
            checkAndIncrementField(cell);

            cell = position + cols;
            checkAndIncrementField(cell);

            if (current_col < (cols - 1)) {
                cell = position - cols + 1;
                checkAndIncrementField(cell);

                cell = position + cols + 1;
                checkAndIncrementField(cell);

                cell = position + 1;
                checkAndIncrementField(cell);
            }
        }
    }
}


private void checkAndIncrementField(int cell) {
    if (cell >= 0 && cell < all_cells && field[cell] != COVERED_MINE_CELL) {
        field[cell] += 1; // Incrémente la valeur de la cellule pour indiquer le nombre de mines adjacentes
    }
}


public void find_empty_cells(int j) {
    int current_col = j % cols;

    checkAndClearCell(j - cols - 1);
    checkAndClearCell(j - 1);
    checkAndClearCell(j + cols - 1);

    checkAndClearCell(j - cols);
    checkAndClearCell(j + cols);

    if (current_col < (cols - 1)) {
        checkAndClearCell(j - cols + 1);
        checkAndClearCell(j + cols + 1);
        checkAndClearCell(j + 1);
    }
}
private void checkAndClearCell(int cell) {
    if (cell >= 0 && cell < all_cells && field[cell] > MINE_CELL) {
        field[cell] -= COVER_FOR_CELL; // Dévoile la cellule en soustrayant la constante COVER_FOR_CELL
        if (field[cell] == EMPTY_CELL) {
            find_empty_cells(cell); // Appelle récursivement la méthode pour trouver les cellules vides adjacentes
        }
    }
}

public void paint(Graphics g) {
    int cell = 0;
    int uncover = 0;

    for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            cell = field[(i * cols) + j];

            if (inGame && cell == MINE_CELL)
                inGame = false;

            // Si le jeu n'est pas en cours
            if (!inGame) {
                if (cell == COVERED_MINE_CELL) {
                    cell = DRAW_MINE; // Dessine une mine
                } else if (cell == MARKED_MINE_CELL) {
                    cell = DRAW_MARK; // Dessine le symbole de marquage
                } else if (cell > COVERED_MINE_CELL) {
                    cell = DRAW_WRONG_MARK; // Dessine un marquage incorrect
                } else if (cell > MINE_CELL) {
                    cell = DRAW_COVER; // Dessine une cellule couverte
                }
            }
            // Si le jeu est en cours
            else {
                if (cell > COVERED_MINE_CELL)
                    cell = DRAW_MARK; // Dessine le symbole de marquage
                else if (cell > MINE_CELL) {
                    cell = DRAW_COVER; // Dessine une cellule couverte
                    uncover++;
                }
            }

            g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this); // Dessine l'image correspondant à la cellule
        }
    }

    if (uncover == 0 && inGame) {
        inGame = false;
        statusbar.setText("Partie gagnée"); // Affiche "Partie gagnée" dans la barre de statut
    } else if (!inGame) {
        statusbar.setText("Partie perdue"); // Affiche "Partie perdue" dans la barre de statut
    }
}


class MinesAdapter extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        int cCol = x / CELL_SIZE;
        int cRow = y / CELL_SIZE;

        boolean rep = false;

        // Si le jeu n'est pas en cours, lancer une nouvelle partie et redessiner
        if (!inGame) {
            newGame();
            repaint();
        }

        // Vérifier si les coordonnées du clic se trouvent dans les limites de la grille de jeu
        if ((x < cols * CELL_SIZE) && (y < rows * CELL_SIZE)) {
            // Gestion du clic droit (marquage d'une cellule)
            if (e.getButton() == MouseEvent.BUTTON3) {
                if (field[(cRow * cols) + cCol] > MINE_CELL) {
                    rep = true;
                    if (field[(cRow * cols) + cCol] <= COVERED_MINE_CELL) {
                        // Marquer la cellule si des marquages sont disponibles
                        if (mines_left > 0) {
                            field[(cRow * cols) + cCol] += MARK_FOR_CELL;
                            mines_left--;
                            statusbar.setText(Integer.toString(mines_left));
                        } else {
                            statusbar.setText("Plus de marquages disponibles");
                        }
                    } else {
                        // Enlever le marquage de la cellule et rétablir le compteur de marquages
                        field[(cRow * cols) + cCol] -= MARK_FOR_CELL;
                        mines_left++;
                        statusbar.setText(Integer.toString(mines_left));
                    }
                }
            }
            // Gestion du clic gauche (découvrir une cellule)
            else {
                // Si la cellule est déjà découverte ou marquée, ne rien faire
                if (field[(cRow * cols) + cCol] > COVERED_MINE_CELL) {
                    return;
                }
                // Si la cellule contient une mine, le joueur a perdu
                if ((field[(cRow * cols) + cCol] > MINE_CELL) &&
                        (field[(cRow * cols) + cCol] < MARKED_MINE_CELL)) {
                    field[(cRow * cols) + cCol] -= COVER_FOR_CELL;
                    rep = true;
                    if (field[(cRow * cols) + cCol] == MINE_CELL)
                        inGame = false;
                    if (field[(cRow * cols) + cCol] == EMPTY_CELL)
                        find_empty_cells((cRow * cols) + cCol);
                }
            }
            // Redessiner la grille si des modifications ont été apportées
            if (rep)
                repaint();
        }
    }
}
}
