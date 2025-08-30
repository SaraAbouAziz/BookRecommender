package bookrecommender.utenti;

import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utili.ViewsController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * Controller per la schermata della Dashboard utente (areaprivata-view.fxml).
 * <p>
 * Questa classe funge da hub centrale per l'utente dopo aver effettuato il login.
 * Le sue responsabilità principali sono:
 * <ul>
 *     <li>Verificare che l'utente sia autenticato tramite {@link GestoreSessione}. Se non lo è,
 *         reindirizza alla schermata di login.</li>
 *     <li>Visualizzare un messaggio di benvenuto personalizzato.</li>
 *     <li>Fornire pulsanti di navigazione per le sezioni principali dell'area privata:
 *         ricerca libri, gestione librerie, visualizzazione valutazioni e consigli.</li>
 *     <li>Gestire il processo di logout, inclusa la conferma da parte dell'utente e la pulizia
 *         della sessione.</li>
 *     <li>Controllare un'animazione di sfondo tramite {@link ParticleAnimation}.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see ViewsController
 * @see GestoreSessione
 * @see ParticleAnimation
 * @version 1.0
 */
public class DashboardController {
    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    /**
     * Etichetta per visualizzare il messaggio di benvenuto personalizzato per l'utente.
     */
    @FXML
    private Label lblWelcome;

    /**
     * ImageView per visualizzare l'avatar dell'utente. (Attualmente non implementato).
     */
    @FXML
    private ImageView userAvatar;

    /**
     * Il {@link Canvas} su cui viene disegnata l'animazione di sfondo.
     */
    @FXML
    private Canvas backgroundCanvas;

    /**
     * Istanza per la gestione dell'animazione delle particelle in background.
     */
    private ParticleAnimation particleAnimation;

    /**
     * Metodo chiamato automaticamente da FXMLLoader dopo il caricamento del file FXML.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     *     <li>Controlla se l'utente è loggato tramite {@link GestoreSessione}. Se non lo è,
     *         reindirizza immediatamente alla schermata di login.</li>
     *     <li>Imposta il testo dell'etichetta di benvenuto con il nome dell'utente loggato.</li>
     *     <li>Inizializza e avvia l'animazione di sfondo {@link ParticleAnimation}.</li>
     *     <li>Aggiunge un listener alla finestra per fermare l'animazione alla chiusura,
     *         prevenendo memory leak.</li>
     * </ol>
     */
    @FXML
    private void initialize() {
        if (!GestoreSessione.getInstance().isLoggedIn()) {
            ViewsController.mostraLogin();
            return;
        }
        lblWelcome.setText("Benvenuto, " + GestoreSessione.getInstance().getLoggedInUser() + "!");
        logger.info("Dashboard inizializzata per l'utente: " + GestoreSessione.getInstance().getLoggedInUser());

        // Avvia l'animazione dello sfondo
        if (backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();

            // Assicurati di fermare l'animazione quando la finestra si chiude
            // per evitare memory leak.
            backgroundCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                        if (newWindow != null) {
                            newWindow.setOnCloseRequest(event -> particleAnimation.stop());
                        }
                    });
                }
            });
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Cerca Libri".
     * Naviga alla schermata di ricerca libri tramite {@link ViewsController}.
     */
    @FXML
    private void handleSearch() {
        logger.info("Azione: Cerca Libri");
        ViewsController.mostraCercaLibri();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Le Mie Librerie".
     * Ferma l'animazione di sfondo per liberare risorse e naviga alla schermata
     * di gestione delle librerie.
     */
    @FXML
    private void handleViewLibraries() {
        logger.info("Azione: Visualizza Librerie");
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        ViewsController.mostraLibrerie();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Le Mie Recensioni".
     * Ferma l'animazione di sfondo e naviga alla schermata di gestione delle valutazioni.
     */
    @FXML
    private void handleRateBook() {
        logger.info("Azione: Le Mie Recensioni");
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        ViewsController.mostraValutazioni();
    }

    /**
     * Gestisce l'evento di click sul pulsante "I Miei Consigli".
     * Ferma l'animazione di sfondo e naviga alla schermata di gestione dei consigli.
     */
    @FXML
    private void handleRecommendBook() {
        logger.info("Azione: I Miei Consigli");
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        ViewsController.mostraConsigli();
    }

    /**
     * Gestisce l'evento di click sul pulsante di Logout.
     * <p>
     * Mostra un dialogo di conferma all'utente. Se l'utente conferma,
     * ferma l'animazione, pulisce la sessione corrente tramite {@link GestoreSessione#clearSession()}
     * e reindirizza alla schermata di benvenuto.
     */
    @FXML
    private void handleLogout() {
        logger.info("Azione: Logout");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma Logout");
        alert.setHeaderText(null);
        alert.setContentText("Sei sicuro di voler uscire?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            logger.info("Logout confermato. Reindirizzamento alla schermata di benvenuto.");
            if (particleAnimation != null) {
                particleAnimation.stop();
            }
            GestoreSessione.getInstance().clearSession();
            ViewsController.mostraBenvenuti();
        } else {
            logger.info("Logout annullato.");
        }
    }

    /**
     * Metodo di utilità per mostrare un {@link Alert} all'utente.
     *
     * @param type il tipo di alert (es. INFORMATION, ERROR).
     * @param title il titolo della finestra di dialogo.
     * @param content il contenuto del messaggio.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
