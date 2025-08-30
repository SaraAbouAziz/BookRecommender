package bookrecommender.utili;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Controller per la schermata di benvenuto.
 * <p>
 * Questa classe gestisce la logica della vista principale dell'applicazione,
 * che funge da punto di ingresso per l'utente.
 * Le sue responsabilità includono:
 * <ul>
 *     <li>Gestire l'animazione delle particelle in background.</li>
 *     <li>Aggiungere micro-interazioni (effetti hover) ai pulsanti.</li>
 *     <li>Delegare la navigazione verso altre viste (Login, Registrazione, Ricerca)
 *     al {@link ViewsController}.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @version 1.0
 */
public class BenvenutiController {

    private static final Logger logger = LogManager.getLogger(BenvenutiController.class);

    /**
     * Istanza per la gestione dell'animazione delle particelle in background.
     */
    private ParticleAnimation particleAnimation;

    /**
     * Il pannello radice della vista, un {@link StackPane}.
     */
    @FXML private StackPane rootPane;

    /**
     * Pulsante per accedere alla schermata di login.
     */
    @FXML private Button loginButton;
    /**
     * Pulsante per accedere alla schermata di registrazione.
     */
    @FXML private Button registerButton;
    /**
     * Pulsante per accedere alla schermata di ricerca libri.
     */
    @FXML private Button searchButton;
    /**
     * Il {@link Canvas} su cui viene disegnata l'animazione di sfondo.
     */
    @FXML private Canvas backgroundCanvas;

    /**
     * Metodo chiamato automaticamente da FXMLLoader dopo il caricamento del file FXML.
     * <p>
     * Inizializza il controller, avvia l'animazione di sfondo e imposta gli effetti
     * di hover sui pulsanti di navigazione. Gestisce anche la terminazione corretta
     * dell'animazione alla chiusura della finestra per prevenire memory leak.
     * </p>
     */
    @FXML
    public void initialize() {
        logger.debug("Inizializzazione BenvenutiController");

        if (backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            startAnimation(); // Avvia l'animazione

            backgroundCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                        if (newWindow != null) {
                            newWindow.setOnCloseRequest(event -> stopAnimation());
                        }
                    });
                }
            });
        }
        
        addHoverScale(loginButton, 1.04, 120);
        addHoverScale(registerButton, 1.04, 120);
        addHoverScale(searchButton, 1.04, 120);
    }

    /**
     * Avvia l'animazione delle particelle, se disponibile.
     */
    public void startAnimation() {
        if (particleAnimation != null) {
            particleAnimation.start();
        }
    }

    /**
     * Ferma l'animazione delle particelle per liberare risorse.
     */
    public void stopAnimation() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Accedi".
     * Ferma l'animazione di sfondo e naviga alla vista di login.
     */
    @FXML
    public void showLoginView() {
        stopAnimation();
        ViewsController.mostraLogin();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Registrati".
     * Ferma l'animazione di sfondo e naviga alla vista di registrazione.
     */
    @FXML
    public void showRegisterView() {
        stopAnimation();
        ViewsController.mostraRegistrazione();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Cerca Libri".
     * Ferma l'animazione di sfondo e naviga alla vista di ricerca.
     */
    @FXML
    public void showSearchView() {
        stopAnimation();
        ViewsController.mostraCercaLibri();
    }

    /**
     * Applica un effetto di animazione di scala a un nodo quando il mouse vi passa sopra.
     *
     * @param node Il nodo a cui applicare l'effetto.
     * @param toScale Il fattore di scala da raggiungere durante l'hover (es. 1.04).
     * @param durationMs La durata della transizione in millisecondi.
     */
    private void addHoverScale(javafx.scene.Node node, double toScale, int durationMs) {
        if (node == null) return;

        node.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
            st.setToX(toScale);
            st.setToY(toScale);
            st.play();
        });

        node.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
    }
}
