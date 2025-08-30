package bookrecommender.utenti;

import bookrecommender.condivisi.utenti.Utenti;
import bookrecommender.utili.ViewsController;
import bookrecommender.utili.ParticleAnimation;
import bookrecommender.condivisi.utenti.UtentiService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


/**
 * Controller per la schermata di login (login-view.fxml).
 * <p>
 * Questa classe gestisce la logica di autenticazione dell'utente.
 * Le sue responsabilità includono:
 * <ul>
 *     <li>Gestire l'input dell'utente per credenziali (username/email/codice fiscale) e password.</li>
 *     <li>Validare i dati inseriti.</li>
 *     <li>Comunicare con il servizio RMI {@link UtentiService} per autenticare l'utente.</li>
 *     <li>In caso di successo, inizializzare la sessione utente tramite {@link GestoreSessione} e navigare all'area privata.</li>
 *     <li>Mostrare messaggi di errore o di successo all'utente.</li>
 *     <li>Gestire l'animazione di sfondo.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Kahri Mohamed Ameur 754773
 * @author Zoghbani Lilia 759652
 * @version 1.0
 * @since 1.0
 */
public class LoginController {
    private static final Logger logger = LogManager.getLogger(LoginController.class);
    
    /**
     * Campo di testo per l'inserimento di username, email o codice fiscale.
     */
    @FXML
    private TextField usernameField;
    /**
     * Campo per l'inserimento della password.
     */
    @FXML
    private PasswordField passwordField;
    /**
     * Il {@link Canvas} su cui viene disegnata l'animazione di sfondo.
     */
    @FXML
    private Canvas backgroundCanvas;

    /**
     * Riferimento al servizio RMI per la gestione degli utenti.
     */
    private UtentiService utentiService;

    /**
     * Istanza per la gestione dell'animazione delle particelle in background.
     */
    private ParticleAnimation particleAnimation;

    /**
     * Metodo chiamato automaticamente da FXMLLoader dopo il caricamento del file FXML.
     * <p>
     * Inizializza il controller, imposta il testo segnaposto per il campo username,
     * avvia l'animazione di sfondo e imposta i listener per la sua corretta terminazione.
     * </p>
     */
    @FXML
    public void initialize() {
        logger.debug("Inizializzazione LoginController");
        usernameField.setPromptText("Username, Email o Codice Fiscale");

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
     * Gestisce l'evento di click sul pulsante di login.
     * <p>
     * Recupera le credenziali inserite, le valida, e invoca il servizio RMI
     * per l'autenticazione. Se il login ha successo, crea una sessione utente
     * e reindirizza all'area privata. In caso di fallimento o errore, mostra
     * un alert appropriato.
     * </p>
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        logger.debug("Tentativo di login per utente: " + username);

        // Validazione input
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Campi vuoti", 
                     "Inserisci username e password");
            return;
        }

        try {
            // Connessione al servizio RMI solo se utentiService non è già inizializzato
            if (utentiService == null) {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                utentiService = (UtentiService) registry.lookup("UtentiService");
            }

            // Tentativo di autenticazione
            boolean loginSuccess = utentiService.authenticateUser(username, password);
            
            if (loginSuccess) {
                logger.info("Login riuscito per utente: " + username);
                
                // Ottieni l'utente per recuperare l'ID
                Utenti utente = utentiService.getUserByUsername(username);
                if (utente != null) {
                    // Avvia la sessione con ID utente e username
                    GestoreSessione.getInstance().login(utente.userID(), utente.userID());
                    
                    showAlert(Alert.AlertType.INFORMATION, "Successo", "Login riuscito", 
                             "Benvenuto! Login effettuato con successo.");
                    
                    // Ferma l'animazione prima di cambiare vista per liberare risorse
                    if (particleAnimation != null) {
                        particleAnimation.stop();
                    }

                    // Reindirizza all'area privata
                    ViewsController.mostraAreaPrivata();
                } else {
                    // Questo caso non dovrebbe verificarsi se l'autenticazione è andata a buon fine
                    logger.error("Impossibile recuperare i dati dell'utente dopo il login: " + username);
                    showAlert(Alert.AlertType.ERROR, "Errore", "Errore interno", 
                             "Impossibile recuperare i dati dell'utente. Riprova più tardi.");
                }
            } else {
                logger.warn("Login fallito per utente: " + username);
                showAlert(Alert.AlertType.ERROR, "Errore", "Credenziali errate", 
                         "Username o password non corretti");
            }

        } catch (RemoteException e) {
            logger.error("Errore di connessione RMI durante il login", e);
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore di connessione", 
                     "Impossibile connettersi al server. Riprova più tardi.");
        } catch (NotBoundException e) {
            logger.error("Servizio UtentiService non trovato nel registro RMI", e);
            showAlert(Alert.AlertType.ERROR, "Errore", "Servizio non disponibile", 
                     "Il servizio di autenticazione non è disponibile.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante il login", e);
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore imprevisto", 
                     "Si è verificato un errore imprevisto. Riprova più tardi.");
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Indietro".
     * Naviga alla schermata di benvenuto principale.
     */
    @FXML
    private void onBack() {
        logger.debug("Richiesta di tornare al menu principale");

        // Ferma l'animazione prima di cambiare vista per liberare risorse
        if (particleAnimation != null) {
            particleAnimation.stop();
        }

        ViewsController.mostraBenvenuti();
    }

    /**
     * Metodo di utilità per mostrare un {@link Alert} all'utente.
     *
     * @param alertType il tipo di alert (es. INFORMATION, ERROR).
     * @param title il titolo della finestra di dialogo.
     * @param header l'intestazione del messaggio.
     * @param content il contenuto del messaggio.
     */
    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
