package bookrecommender.utenti;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utili.ViewsController;
import bookrecommender.condivisi.utenti.Utenti;
import bookrecommender.condivisi.utenti.UtentiService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.function.UnaryOperator;

/**
 * Controller per la schermata di registrazione (registrazione-view.fxml).
 * <p>
 * Questa classe gestisce tutta la logica legata alla creazione di un nuovo account utente.
 * Le sue responsabilità includono:
 * <ul>
 *     <li>Gestire l'input dell'utente per i dati anagrafici e le credenziali.</li>
 *     <li>Applicare validazione in tempo reale e al momento dell'invio per garantire la coerenza dei dati.</li>
 *     <li>Comunicare con il servizio RMI {@link UtentiService} per registrare il nuovo utente nel sistema.</li>
 *     <li>Fornire feedback all'utente tramite dialoghi di alert in caso di successo o errore.</li>
 *     <li>Gestire la navigazione verso altre viste dell'applicazione.</li>
 *     <li>Controllare l'animazione di sfondo per migliorare l'esperienza utente.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @version 1.0
 */
public class RegistrazioneController {
    private static final Logger logger = LogManager.getLogger(RegistrazioneController.class);
    
    /**
     * Campo di testo per l'inserimento del nome dell'utente.
     */
    @FXML
    private TextField textFieldNome;
    /**
     * Campo di testo per l'inserimento del cognome dell'utente.
     */
    @FXML
    private TextField textFieldCognome;
    /**
     * Campo di testo per l'inserimento del codice fiscale dell'utente.
     */
    @FXML
    private TextField textFieldCodiceFiscale;
    /**
     * Campo di testo per l'inserimento dell'indirizzo email dell'utente.
     */
    @FXML
    private TextField textFieldEmail;
    /**
     * Campo di testo per l'inserimento dello username (ID Utente) desiderato.
     */
    @FXML
    private TextField textFieldIDUtente;
    /**
     * Campo per l'inserimento della password.
     */
    @FXML
    private PasswordField passwordField;
    /**
     * Campo per la conferma della password inserita.
     */
    @FXML
    private PasswordField passwordFieldRipetiPassword;
    /**
     * Il {@link Canvas} su cui viene disegnata l'animazione di sfondo.
     */
    @FXML
    private Canvas backgroundCanvas;

    /**
     * Riferimento al servizio RMI per la gestione degli utenti.
     * Viene inizializzato alla prima necessità.
     */
    private UtentiService utentiService;

    /**
     * Istanza per la gestione dell'animazione delle particelle in background.
     */
    private ParticleAnimation particleAnimation;

    /**
     * Metodo chiamato automaticamente da FXMLLoader dopo il caricamento del file FXML.
     * <p>
     * Inizializza il controller, imposta la validazione in tempo reale sui campi di input
     * e avvia l'animazione di sfondo, assicurandosi che venga terminata correttamente
     * alla chiusura della finestra per evitare memory leak.
     * </p>
     */
    @FXML
    private void initialize() {
        logger.debug("Inizializzazione RegistrazioneController");
        setupInputValidation();

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
     * Configura la validazione in tempo reale per alcuni campi di input.
     * <p>
     * Utilizza {@link javafx.scene.control.TextFormatter} per filtrare i caratteri
     * inseriti dall'utente, impedendo l'immissione di dati non conformi
     * per codice fiscale, email e username.
     * </p>
     */
    private void setupInputValidation() {
        // Validazione codice fiscale (solo lettere e numeri)
        UnaryOperator<javafx.scene.control.TextFormatter.Change> cfFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[A-Z0-9]*")) {
                return change;
            }
            return null;
        };
        textFieldCodiceFiscale.setTextFormatter(new javafx.scene.control.TextFormatter<>(cfFilter));
        
        // Validazione email (formato base)
        UnaryOperator<javafx.scene.control.TextFormatter.Change> emailFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-Z0-9@._-]*")) {
                return change;
            }
            return null;
        };
        textFieldEmail.setTextFormatter(new javafx.scene.control.TextFormatter<>(emailFilter));
        
        // Validazione username (solo lettere, numeri e underscore)
        UnaryOperator<javafx.scene.control.TextFormatter.Change> usernameFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[a-zA-Z0-9_]*")) {
                return change;
            }
            return null;
        };
        textFieldIDUtente.setTextFormatter(new javafx.scene.control.TextFormatter<>(usernameFilter));
    }

    /**
     * Gestisce l'evento di click sul pulsante di registrazione.
     * <p>
     * Raccoglie i dati dai campi del form, li valida tramite {@link #validateInput()},
     * e se validi, invoca il servizio RMI per registrare il nuovo utente.
     * In caso di successo, mostra un messaggio di conferma e naviga alla schermata di benvenuto.
     * In caso di fallimento o errore, mostra un alert appropriato.
     * </p>
     */
    @FXML
    private void handleRegister() {
        logger.debug("Tentativo di registrazione nuovo utente");

        // Validazione input
        if (!validateInput()) {
            return;
        }

        try {
            // Creazione oggetto Utenti con userID personalizzato
            Utenti nuovoUtente = new Utenti(
                textFieldNome.getText().trim(),
                textFieldCognome.getText().trim(),
                textFieldCodiceFiscale.getText().trim().toUpperCase(),
                textFieldEmail.getText().trim(),
                textFieldIDUtente.getText().trim(),
                passwordField.getText()
            );

            // Connessione al servizio RMI solo se utentiService non è già inizializzato
            if (utentiService == null) {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                utentiService = (UtentiService) registry.lookup("UtentiService");
            }

            // Tentativo di registrazione
            boolean registrazioneSuccess = utentiService.registerUser(nuovoUtente);
            
            if (registrazioneSuccess) {
                logger.info("Registrazione riuscita per utente: " + nuovoUtente.userID());
                showAlert(Alert.AlertType.INFORMATION, "Successo", "Registrazione completata", 
                         "Account creato con successo! Ora puoi effettuare il login.");
                
                // Pulisce i campi e torna al menu
                resetTextFields(textFieldNome, textFieldCognome, textFieldCodiceFiscale, 
                              textFieldEmail, textFieldIDUtente);
                passwordField.clear();
                passwordFieldRipetiPassword.clear();
                
                if (particleAnimation != null) {
                    particleAnimation.stop();
                }
                ViewsController.mostraBenvenuti();
            } else {
                logger.warn("Registrazione fallita per utente: " + nuovoUtente.userID());
                showAlert(Alert.AlertType.ERROR, "Errore", "Registrazione fallita", 
                         "Username, email o codice fiscale già esistente. Verifica i dati inseriti.");
            }

        } catch (RemoteException e) {
            logger.error("Errore di connessione RMI durante la registrazione", e);
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore di connessione", 
                     "Impossibile connettersi al server. Riprova più tardi.");
        } catch (NotBoundException e) {
            logger.error("Servizio UtentiService non trovato nel registro RMI", e);
            showAlert(Alert.AlertType.ERROR, "Errore", "Servizio non disponibile", 
                     "Il servizio di registrazione non è disponibile.");
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la registrazione", e);
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore imprevisto", 
                     "Si è verificato un errore imprevisto. Riprova più tardi.");
        }
    }

    /**
     * Valida tutti i campi di input del form di registrazione.
     * <p>
     * Esegue una serie di controlli:
     * <ul>
     *     <li>Verifica che nessun campo sia vuoto.</li>
     *     <li>Controlla la lunghezza minima e massima dello username.</li>
     *     <li>Controlla la lunghezza minima della password.</li>
     *     <li>Verifica che le due password inserite coincidano.</li>
     *     <li>Controlla il formato dell'email con una regex di base.</li>
     *     <li>Verifica che il codice fiscale abbia la lunghezza corretta (16 caratteri).</li>
     * </ul>
     * Se un controllo fallisce, mostra un alert all'utente e restituisce {@code false}.
     * </p>
     * @return {@code true} se tutti i dati sono validi, {@code false} altrimenti.
     */
    private boolean validateInput() {
        // Controllo campi vuoti
        if (textFieldNome.getText().trim().isEmpty() || 
            textFieldCognome.getText().trim().isEmpty() ||
            textFieldCodiceFiscale.getText().trim().isEmpty() ||
            textFieldEmail.getText().trim().isEmpty() ||
            textFieldIDUtente.getText().trim().isEmpty() ||
            passwordField.getText().isEmpty() ||
            passwordFieldRipetiPassword.getText().isEmpty()) {
            
            showAlert(Alert.AlertType.ERROR, "Errore", "Campi vuoti", 
                     "Tutti i campi sono obbligatori");
            return false;
        }

        // Controllo lunghezza username
        String username = textFieldIDUtente.getText().trim();
        if (username.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Username troppo corto", 
                     "L'username deve essere di almeno 3 caratteri");
            return false;
        }
        
        if (username.length() > 20) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Username troppo lungo", 
                     "L'username deve essere di massimo 20 caratteri");
            return false;
        }

        // Controllo lunghezza password
        if (passwordField.getText().length() < 6) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Password troppo corta", 
                     "La password deve essere di almeno 6 caratteri");
            return false;
        }

        // Controllo conferma password
        if (!passwordField.getText().equals(passwordFieldRipetiPassword.getText())) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Password non coincidono", 
                     "Le password inserite non coincidono");
            return false;
        }

        // Controllo formato email
        String email = textFieldEmail.getText().trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Email non valida", 
                     "Inserisci un indirizzo email valido");
            return false;
        }

        // Controllo codice fiscale
        String cf = textFieldCodiceFiscale.getText().trim();
        if (cf.length() != 16) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Codice fiscale non valido", 
                     "Il codice fiscale deve essere di 16 caratteri");
            return false;
        }

        return true;
    }

    /**
     * Gestisce l'evento di click sul pulsante "Indietro".
     * Ferma l'animazione di sfondo e naviga alla schermata di benvenuto principale.
     */
    @FXML
    private void onBack() {
        logger.debug("Richiesta di tornare al menu principale");
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        ViewsController.mostraBenvenuti();
    }

    /**
     * Resetta (svuota) il contenuto di uno o più campi di testo.
     * È un metodo di utilità per pulire il form dopo un'operazione.
     *
     * @param textFields I campi {@link TextField} da resettare.
     */
    private void resetTextFields(TextField... textFields) {
        for (TextField textField : textFields) {
            textField.clear();
        }
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