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
import java.util.regex.Pattern;

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
     * Pattern email robusto che verifica:
     * <ul>
     *     <li>Lunghezza complessiva 6-254 caratteri.</li>
     *     <li>Local-part con caratteri consentiti RFC (semplificato) e punti non consecutivi.</li>
     *     <li>Dominio composto da etichette valide e TLD alfabetico di almeno 2 lettere.</li>
     * </ul>
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^(?=.{6,254}$)([A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*)@" +
            "([A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$"
    );
    /** Requisiti password: almeno 10 caratteri, una maiuscola, una minuscola, una cifra, un simbolo, nessuno spazio. */
    private static final Pattern PW_UPPER = Pattern.compile(".*[A-Z].*");
    private static final Pattern PW_LOWER = Pattern.compile(".*[a-z].*");
    private static final Pattern PW_DIGIT = Pattern.compile(".*[0-9].*");
    // Nota: occorre escapare '[' e ']' dentro la classe di caratteri per evitare PatternSyntaxException
    private static final Pattern PW_SYMBOL = Pattern.compile(".*[!@#$%^&*()_+\\-\\[\\]{};':\\\"`~\\\\|,.<>/?].*");
    private static final Pattern PW_WHITESPACE = Pattern.compile(".*\\s.*");
    private static final int PW_MIN_LENGTH = 10;
    
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
        // Validazione nome (lettere, spazi, apostrofi, trattini e lettere accentate)
        UnaryOperator<javafx.scene.control.TextFormatter.Change> nomeFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[A-Za-zÀ-ÖØ-öø-ÿ '’-]*")) {
                return change;
            }
            return null;
        };
        textFieldNome.setTextFormatter(new javafx.scene.control.TextFormatter<>(nomeFilter));

        // Validazione cognome (stesse regole del nome)
        UnaryOperator<javafx.scene.control.TextFormatter.Change> cognomeFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("[A-Za-zÀ-ÖØ-öø-ÿ '’-]*")) {
                return change;
            }
            return null;
        };
        textFieldCognome.setTextFormatter(new javafx.scene.control.TextFormatter<>(cognomeFilter));

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


        // Controllo conferma password
        if (!passwordField.getText().equals(passwordFieldRipetiPassword.getText())) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Password non coincidono", 
                     "Le password inserite non coincidono");
            return false;
        }

        // Controllo formato email
        String email = textFieldEmail.getText().trim();
    if (!isValidEmail(email)) {
        showAlert(Alert.AlertType.ERROR, "Errore", "Email non valida", 
            "Formato email non valido o non consentito");
        return false;
    }

        // Controllo codice fiscale
        String cf = textFieldCodiceFiscale.getText().trim();
        if (cf.length() != 16) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Codice fiscale non valido", 
                     "Il codice fiscale deve essere di 16 caratteri");
            return false;
        }

        // Controllo formato nome (2-40 caratteri consentiti)
        String nome = textFieldNome.getText().trim();
        if (!nome.matches("[A-Za-zÀ-ÖØ-öø-ÿ '’-]{2,40}")) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Nome non valido", 
                     "Il nome può contenere solo lettere (anche accentate), spazi, apostrofi o trattini (2-40 caratteri)");
            return false;
        }

        // Controllo formato cognome (2-60 caratteri consentiti)
        String cognome = textFieldCognome.getText().trim();
        if (!cognome.matches("[A-Za-zÀ-ÖØ-öø-ÿ '’-]{2,60}")) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Cognome non valido", 
                     "Il cognome può contenere solo lettere (anche accentate), spazi, apostrofi o trattini (2-60 caratteri)");
            return false;
        }

        // Validazione avanzata password
    String pwError = validatePassword(passwordField.getText());
        if (pwError != null) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Password non sicura", pwError);
            return false;
        }

        return true;
    }

    // ------------------- Metodi di validazione avanzata ------------------- //
    /**
     * Verifica la validità sintattica di un indirizzo email secondo regole applicative
     * semplificate ma robuste.
     * <p>Le verifiche effettuate sono:
     * <ul>
     *     <li>Match con il {@link #EMAIL_PATTERN} (lunghezza totale, struttura local-part@domain).</li>
     *     <li>Assenza di punto iniziale o finale nella local-part.</li>
     *     <li>Nessuna sequenza di due punti consecutivi ("..") né nella local-part né nel dominio.</li>
     *     <li>Dominio scomposto in etichette valide (già garantito dal pattern).</li>
     * </ul>
     * Non vengono effettuati controlli DNS o di deliverability reale (MX, esistenza dominio).
     * </p>
     * Esempi considerati validi:
     * <pre>
     *   utente.simple@example.com
     *   nome.cognome+tag@sub.example.org
     * </pre>
     * Esempi considerati non validi:
     * <pre>
     *   .nome@example.com   (punto iniziale)
     *   nome.@example.com   (punto finale)
     *   no..dup@example.com (punti consecutivi)
     *   nome@example..com   (punti consecutivi nel dominio)
     * </pre>
     * @param email stringa email da validare (può essere null o vuota).
     * @return true se l'email rispetta tutte le regole sopra, false altrimenti.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        if (!EMAIL_PATTERN.matcher(email).matches()) return false;
        // Evita doppio punto consecutivo e punto iniziale/finale local part
        String local = email.substring(0, email.indexOf('@'));
        if (local.startsWith(".") || local.endsWith(".")) return false;
        if (local.contains("..")) return false;
        String domain = email.substring(email.indexOf('@') + 1);
        if (domain.contains("..")) return false;
        return true;
    }

    /**
     * Restituisce null se la password è accettabile, altrimenti messaggio di errore.
     */
    /**
     * Valida la password secondo criteri locali (complessità minima).
     * Non effettua controlli su password comuni o correlazioni con altri campi.
     *
     * @param pw password in chiaro inserita dall'utente.
     * @return null se valida, altrimenti messaggio di errore sintetico.
     */
    private String validatePassword(String pw) {
        StringBuilder missing = new StringBuilder();
        if (pw.length() < PW_MIN_LENGTH) missing.append("- Lunghezza minima ").append(PW_MIN_LENGTH).append(" caratteri\n");
        if (PW_WHITESPACE.matcher(pw).matches()) missing.append("- Non deve contenere spazi\n");
        if (!PW_UPPER.matcher(pw).matches()) missing.append("- Almeno una lettera maiuscola (A-Z)\n");
        if (!PW_LOWER.matcher(pw).matches()) missing.append("- Almeno una lettera minuscola (a-z)\n");
        if (!PW_DIGIT.matcher(pw).matches()) missing.append("- Almeno una cifra (0-9)\n");
        if (!PW_SYMBOL.matcher(pw).matches()) missing.append("- Almeno un simbolo speciale (es: ! @ # $ % ...)\n");

        if (missing.length() == 0) return null;
        return "La password deve rispettare i seguenti requisiti:\n" + missing + "Suggerimento: usa una frase facile da ricordare con numeri e simboli.";
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