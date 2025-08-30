package bookrecommender.valutazioni;

import bookrecommender.condivisi.libri.Libro;
import bookrecommender.utenti.GestoreSessione;
import bookrecommender.condivisi.valutazioni.ValutazioneService;
import bookrecommender.utili.ParticleAnimation;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ResourceBundle;

/**
 * Controller per la vista di valutazione completa di un libro ({@code valutazione-completa.fxml}).
 * <p>
 * Questa classe gestisce l'interfaccia utente e la logica per consentire a un utente
 * di inviare una valutazione dettagliata per un libro, basata su cinque criteri specifici:
 * stile, contenuto, gradevolezza, originalità e qualità dell'edizione.
 * </p>
 *
 * <b>Responsabilità principali:</b>
 * <ul>
 *     <li>Inizializzare e visualizzare le informazioni del libro da valutare.</li>
 *     <li>Gestire l'input dell'utente tramite controlli a "stella" (realizzati con bottoni) per ogni criterio.</li>
 *     <li>Raccogliere note testuali opzionali per ogni criterio e un commento finale.</li>
 *     <li>Calcolare e visualizzare in tempo reale il voto medio complessivo.</li>
 *     <li>Validare il form per assicurarsi che tutti i criteri siano stati valutati prima di abilitare il salvataggio.</li>
 *     <li>Comunicare con il servizio RMI {@link ValutazioneService} per persistere la valutazione nel database.</li>
 *     <li>Gestire la navigazione per tornare alla vista precedente.</li>
 *     <li>Controllare un'animazione di sfondo per migliorare l'esperienza utente.</li>
 * </ul>
 *
 * @see ValutazioneService
 * @see Libro
 * @see ParticleAnimation
 */
public class ValutazioneCompletaController implements Initializable {

    /** Label per visualizzare il titolo del libro da valutare. */
    @FXML private Label lblTitoloLibro;
    /** Label per visualizzare l'autore del libro da valutare. */
    @FXML private Label lblAutoreLibro;
    /** Label per visualizzare il voto finale calcolato come media dei criteri. */
    @FXML private Label lblVotoFinale;
    
    /** Pulsanti a stella per il criterio "Stile". */
    @FXML private Button stile1, stile2, stile3, stile4, stile5;
    
    /** Pulsanti a stella per il criterio "Contenuto". */
    @FXML private Button contenuto1, contenuto2, contenuto3, contenuto4, contenuto5;
    
    /** Pulsanti a stella per il criterio "Gradevolezza". */
    @FXML private Button gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5;
    
    /** Pulsanti a stella per il criterio "Originalità". */
    @FXML private Button originalita1, originalita2, originalita3, originalita4, originalita5;
    
    /** Pulsanti a stella per il criterio "Edizione". */
    @FXML private Button edizione1, edizione2, edizione3, edizione4, edizione5;
    
    /** Campi di testo per le note associate a ciascun criterio e per il commento finale. */
    @FXML private TextArea stileNote, contenutoNote, gradevolezzaNote, originalitaNote, edizioneNote, commentoFinale;
    
    /** Pulsanti per le azioni principali: salva, annulla, torna indietro. */
    @FXML private Button btnSalva, btnAnnulla, btnTornaIndietro;
    
    /** Canvas su cui viene disegnata l'animazione di sfondo. */
    @FXML private Canvas backgroundCanvas;
    
    /** Il libro che l'utente sta attualmente valutando. */
    private Libro libro;
    /** L'ID dell'utente loggato, recuperato dalla sessione. */
    private String userId;
    /** Riferimento al servizio RMI per le operazioni di valutazione. */
    private ValutazioneService valutazioneService;
    /** Il nome della libreria da cui è stata avviata la valutazione. */
    private String nomeLibreria;
    /** Gestore dell'animazione di sfondo. */
    private ParticleAnimation particleAnimation;
    
    /** Valore numerico (da 1 a 5) selezionato per il criterio "Stile". */
    private int stileValore = 0;
    /** Valore numerico (da 1 a 5) selezionato per il criterio "Contenuto". */
    private int contenutoValore = 0;
    /** Valore numerico (da 1 a 5) selezionato per il criterio "Gradevolezza". */
    private int gradevolezzaValore = 0;
    /** Valore numerico (da 1 a 5) selezionato per il criterio "Originalità". */
    private int originalitaValore = 0;
    /** Valore numerico (da 1 a 5) selezionato per il criterio "Edizione". */
    private int edizioneValore = 0;

    /**
     * Metodo chiamato da JavaFX all'inizializzazione del controller.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     *     <li>Configura i controlli a stella per ogni criterio ({@link #setupStelle()}).</li>
     *     <li>Imposta i gestori di eventi ({@link #setupEventHandlers()}).</li>
     *     <li>Inizializza lo stato di validazione del form ({@link #setupValidation()}).</li>
     *     <li>Applica i limiti di lunghezza ai campi di testo ({@link #setupTextAreaLimiters()}).</li>
     *     <li>Recupera l'ID dell'utente dalla sessione.</li>
     *     <li>Stabilisce la connessione al servizio RMI {@link ValutazioneService}.</li>
     *     <li>Avvia l'animazione di sfondo.</li>
     * </ol>
     *
     * @param url L'URL della risorsa FXML, fornito da JavaFX.
     * @param rb Il ResourceBundle, fornito da JavaFX.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupStelle();
        setupEventHandlers();
        setupValidation();
        setupTextAreaLimiters();
        
        // Inizializza l'utente corrente
        userId = GestoreSessione.getInstance().getCurrentUserId();

        // Risolvi il servizio RMI per la valutazione
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            valutazioneService = (ValutazioneService) registry.lookup("ValutazioneService");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore di connessione", "Impossibile connettersi al servizio di valutazione: " + e.getMessage());
        }
        
        // Inizializza l'animazione di sfondo
        if (backgroundCanvas != null) {
            if (backgroundCanvas.getParent() instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region parent = (javafx.scene.layout.Region) backgroundCanvas.getParent();
                backgroundCanvas.widthProperty().bind(parent.widthProperty());
                backgroundCanvas.heightProperty().bind(parent.heightProperty());
            }
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start(); // Avvia l'animazione come nella pagina libreria
            
            // L'animazione gestisce automaticamente il ridimensionamento del canvas
        }
    }

    /**
     * Metodo helper che orchestra la configurazione dei gruppi di stelle per tutti i criteri.
     */
    private void setupStelle() {
        // Configura le stelle per ogni criterio
        setupStelleCriterio(stile1, stile2, stile3, stile4, stile5, "stile");
        setupStelleCriterio(contenuto1, contenuto2, contenuto3, contenuto4, contenuto5, "contenuto");
        setupStelleCriterio(gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5, "gradevolezza");
        setupStelleCriterio(originalita1, originalita2, originalita3, originalita4, originalita5, "originalita");
        setupStelleCriterio(edizione1, edizione2, edizione3, edizione4, edizione5, "edizione");
    }

    /**
     * Configura un gruppo di 5 pulsanti a stella per un criterio specifico.
     * <p>
     * Associa a ogni pulsante un'azione che, al click, aggiorna il punteggio
     * per il criterio corrispondente, ricalcola il voto finale e riesegue la validazione del form.
     *
     * @param stella1 Il primo pulsante a stella.
     * @param stella2 Il secondo pulsante a stella.
     * @param stella3 Il terzo pulsante a stella.
     * @param stella4 Il quarto pulsante a stella.
     * @param stella5 Il quinto pulsante a stella.
     * @param criterio Il nome del criterio (es. "stile", "contenuto") da associare a questo gruppo di stelle.
     */
    private void setupStelleCriterio(Button stella1, Button stella2, Button stella3, Button stella4, Button stella5, String criterio) {
        Button[] stelle = {stella1, stella2, stella3, stella4, stella5};

        for (int i = 0; i < stelle.length; i++) {
            final int valore = i + 1;
            final Button stella = stelle[i];

            prepareStarButton(stella, 0.0, "Valuta " + criterio + ": " + valore);

            stella.setOnAction(e -> {
                selezionaStelle(criterio, valore, stelle);
                updateVotoFinale();
                validateForm();
            });
        }
    }

    /**
     * Aggiorna lo stato del punteggio per un criterio e l'aspetto visivo delle stelle corrispondenti.
     * <p>
     * Quando un utente seleziona un valore (es. 3 stelle), questo metodo memorizza il valore e aggiorna
     * l'interfaccia per "riempire" le stelle fino a quel valore.
     *
     * @param criterio Il nome del criterio da aggiornare.
     * @param valore Il nuovo punteggio (da 1 a 5).
     * @param stelle L'array di pulsanti a stella da aggiornare visivamente.
     */
    private void selezionaStelle(String criterio, int valore, Button[] stelle) {
        // Aggiorna il valore del criterio
        switch (criterio) {
            case "stile": stileValore = valore; break;
            case "contenuto": contenutoValore = valore; break;
            case "gradevolezza": gradevolezzaValore = valore; break;
            case "originalita": originalitaValore = valore; break;
            case "edizione": edizioneValore = valore; break;
        }
        
        // Aggiorna l'aspetto delle stelle con valori decimali proporzionali
        for (int i = 0; i < stelle.length; i++) {
            double valoreStella = Math.max(0, Math.min(1.0, valore - i)); // Valore da 0.0 a 1.0 per ogni stella
            prepareStarButton(stelle[i], valoreStella, null);
        }
    }

    /**
     * Prepara e stila un singolo pulsante per rappresentare una stella.
     * <p>
     * Utilizza un'icona SVG per disegnare la stella e ne gestisce l'aspetto (piena, vuota, colore)
     * in base al valore fornito. Imposta anche gli effetti hover.
     *
     * @param btn Il pulsante da stilizzare.
     * @param valore Un valore da 0.0 a 1.0 che indica quanto la stella debba essere "piena".
     * @param tooltipText Il testo da mostrare nel tooltip del pulsante.
     */
    private void prepareStarButton(Button btn, double valore, String tooltipText) {
        String STAR_PATH = "M8 1.314l2.472 5.01.556 1.126h5.256l-4.25 3.09-.9.654.346 1.18 1.618 5.53-4.25-3.09-.9-.654-.9.654-4.25 3.09 1.618-5.53.346-1.18-.9-.654-4.25-3.09h5.256l.556-1.126L8 1.314z";
        SVGPath svg = new SVGPath();
        svg.setContent(STAR_PATH);
        svg.setStroke(Color.web("#ffd700"));
        svg.setStrokeWidth(2.0);
        svg.setScaleX(1.5);
        svg.setScaleY(1.5);
        
        // Determina se la stella deve essere piena, parzialmente piena o vuota
        final boolean isFilled = valore >= 1.0;
        final boolean isPartiallyFilled = valore > 0.0 && valore < 1.0;
        
        // Colore della stella
        if (isFilled) {
            svg.setFill(Color.web("#ffd700")); // Completamente piena
        } else if (isPartiallyFilled) {
            svg.setFill(Color.web("#ffd700")); // Parzialmente piena (stesso colore per ora)
        } else {
            svg.setFill(Color.TRANSPARENT); // Vuota
        }

        StackPane wrapper = new StackPane(svg);
        wrapper.setMinSize(40, 40);
        wrapper.setPrefSize(40, 40);
        wrapper.setMaxSize(40, 40);

        btn.setGraphic(wrapper);
        btn.setText("");
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 4; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        if (tooltipText != null) {
            btn.setTooltip(new Tooltip(tooltipText));
        }

        btn.hoverProperty().addListener((obs, wasHover, isHover) -> {
            if (isHover) {
                svg.setFill(Color.web("#ffff00"));
            } else {
                if (isFilled) {
                    svg.setFill(Color.web("#ffd700"));
                } else if (isPartiallyFilled) {
                    svg.setFill(Color.web("#ffd700"));
                } else {
                    svg.setFill(Color.TRANSPARENT);
                }
            }
        });
    }

    /**
     * Inizializza i gestori di eventi per i controlli dell'interfaccia.
     * Attualmente vuoto poiché gli eventi principali sono gestiti in {@link #setupStelleCriterio}.
     */
    private void setupEventHandlers() {
        // Eventi per le stelle (già configurati in setupStelle)
    }

    /**
     * Imposta lo stato di validazione iniziale del form.
     * Disabilita il pulsante "Salva" finché il form non è valido.
     */
    private void setupValidation() {
        btnSalva.setDisable(true);
    }

    /**
     * Applica un limite di 256 caratteri a tutte le {@link TextArea} del form.
     */
    private void setupTextAreaLimiters() {
        setupTextAreaLimiter(stileNote, 256);
        setupTextAreaLimiter(contenutoNote, 256);
        setupTextAreaLimiter(gradevolezzaNote, 256);
        setupTextAreaLimiter(originalitaNote, 256);
        setupTextAreaLimiter(edizioneNote, 256);
        setupTextAreaLimiter(commentoFinale, 256);
    }

    /**
     * Metodo helper che aggiunge un listener a una {@link TextArea} per impedirne
     * l'inserimento di testo oltre una lunghezza massima.
     *
     * @param textArea La TextArea a cui applicare il limite.
     * @param maxLength La lunghezza massima consentita.
     */
    private void setupTextAreaLimiter(TextArea textArea, int maxLength) {
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.length() > maxLength) {
                textArea.setText(oldText);
            }
        });
    }



    /**
     * Imposta il libro da valutare e popola i campi dell'interfaccia con i suoi dettagli.
     * Questo metodo viene chiamato dal controller che naviga a questa vista.
     *
     * @param libro L'oggetto {@link Libro} da valutare.
     */
    public void setLibro(Libro libro) {
        this.libro = libro;
        if (libro != null) {
            String titolo = libro.titolo() != null ? libro.titolo() : "Titolo non disponibile";
            String autore = libro.autori() != null ? libro.autori() : "Autore non disponibile";
            lblTitoloLibro.setText("Valutazione: " + titolo);
            lblAutoreLibro.setText("di " + autore);
        }
    }

    /**
     * Imposta il nome della libreria da cui è stata avviata la valutazione.
     * Questo dato è necessario per associare correttamente la valutazione nel database.
     *
     * @param nomeLibreria Il nome della libreria.
     */
    public void setNomeLibreria(String nomeLibreria) {
        this.nomeLibreria = nomeLibreria;
    }

    /**
     * Calcola la media aritmetica dei cinque criteri di valutazione e aggiorna l'etichetta del voto finale.
     * Viene eseguito solo se tutti i criteri hanno un punteggio.
     */
    private void updateVotoFinale() {
        if (stileValore > 0 && contenutoValore > 0 && gradevolezzaValore > 0 && originalitaValore > 0 && edizioneValore > 0) {
            double media = (stileValore + contenutoValore + gradevolezzaValore + originalitaValore + edizioneValore) / 5.0;
            lblVotoFinale.setText(String.format("%.1f", media));
        } else {
            lblVotoFinale.setText("0.0");
        }
    }
    


    /**
     * Controlla se tutti i criteri di valutazione hanno un punteggio (maggiore di zero).
     * Abilita il pulsante "Salva" solo se la condizione è soddisfatta.
     */
    private void validateForm() {
        boolean isValid = stileValore > 0 && contenutoValore > 0 && gradevolezzaValore > 0 && 
                         originalitaValore > 0 && edizioneValore > 0;
        
        btnSalva.setDisable(!isValid);
    }

    /**
     * Gestisce l'evento di click sul pulsante "Salva".
     * <p>
     * Raccoglie tutti i dati dal form, esegue le validazioni finali (es. controllo se già valutato),
     * e invoca il metodo {@code salvaValutazione} del servizio RMI.
     * Mostra un feedback all'utente in base all'esito dell'operazione.
     */
    @FXML
    private void salvaValutazione() {
        if (libro == null || userId == null) {
            showError("Errore", "Dati mancanti per il salvataggio");
            return;
        }

        try {
            if (valutazioneService == null) {
                showError("Errore", "Servizio di valutazione non disponibile. Verifica che il server sia in esecuzione.");
                return;
            }

            // Verifica se il libro è già stato valutato
            if (valutazioneService.isLibroGiaValutato(libro.id().intValue(), userId)) {
                showError("Errore", "Hai già valutato questo libro");
                return;
            }

            // Raccoglie i dati dalla valutazione
            String stileNoteText = stileNote.getText().trim();
            String contenutoNoteText = contenutoNote.getText().trim();
            String gradevolezzaNoteText = gradevolezzaNote.getText().trim();
            String originalitaNoteText = originalitaNote.getText().trim();
            String edizioneNoteText = edizioneNote.getText().trim();
            String commentoFinaleText = commentoFinale.getText().trim();
            
            // Calcola il voto finale
            double votoFinale = (stileValore + contenutoValore + gradevolezzaValore + originalitaValore + edizioneValore) / 5.0;
            
            if (nomeLibreria == null || nomeLibreria.trim().isEmpty()) {
                showError("Errore", "Nome libreria non disponibile. Riapri la valutazione dalla libreria.");
                return;
            }

            // Salva la valutazione usando il servizio RMI
            boolean success = valutazioneService.salvaValutazione(
                    userId, libro.id().intValue(), nomeLibreria,
                    stileValore, stileNoteText,
                    contenutoValore, contenutoNoteText,
                    gradevolezzaValore, gradevolezzaNoteText,
                    originalitaValore, originalitaNoteText,
                    edizioneValore, edizioneNoteText,
                    votoFinale, commentoFinaleText
            );
            
            if (success) {
                showInfo("Successo", "Valutazione salvata con successo!");
                tornaAlleLibrerie();
            } else {
                showError("Errore", "Errore durante il salvataggio della valutazione");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore", "Errore durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Annulla".
     * Ferma l'animazione e riporta l'utente alla vista delle librerie senza salvare.
     */
    @FXML
    private void annullaValutazione() {
        stopAnimation(); // Ferma l'animazione prima di cambiare vista
        tornaAlleLibrerie();
    }

    /**
     * Naviga l'utente alla vista delle librerie.
     * Sostituisce la radice della scena corrente con il contenuto di {@code librerie-view.fxml}.
     */
    @FXML
    private void tornaAlleLibrerie() {
        stopAnimation(); // Ferma l'animazione prima di cambiare vista
        
        try {
            // Carica la schermata delle librerie
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("/bookrecommender/librerie-view.fxml"));
            javafx.scene.Parent root = loader.load();
            
            // Ottiene la scena corrente e cambia il contenuto
            javafx.scene.Scene scene = btnTornaIndietro.getScene();
            scene.setRoot(root);
            
        } catch (Exception e) {
            e.printStackTrace();
            showError("Errore", "Impossibile tornare alle librerie: " + e.getMessage());
        }
    }
    
    /**
     * Ferma l'animazione di sfondo per liberare risorse, tipicamente prima di un cambio di vista.
     */
    public void stopAnimation() {
        if (particleAnimation != null) {
            particleAnimation.stop();
            particleAnimation = null;
        }
    }

    /**
     * Gestisce l'evento di click per la prima stella del criterio "Stile".
     * Questo metodo, insieme ai successivi, è collegato direttamente dall'FXML.
     */
    @FXML private void selezionaStile1() { selezionaStelle("stile", 1, new Button[]{stile1, stile2, stile3, stile4, stile5}); }
    @FXML private void selezionaStile2() { selezionaStelle("stile", 2, new Button[]{stile1, stile2, stile3, stile4, stile5}); }
    @FXML private void selezionaStile3() { selezionaStelle("stile", 3, new Button[]{stile1, stile2, stile3, stile4, stile5}); }
    @FXML private void selezionaStile4() { selezionaStelle("stile", 4, new Button[]{stile1, stile2, stile3, stile4, stile5}); }
    @FXML private void selezionaStile5() { selezionaStelle("stile", 5, new Button[]{stile1, stile2, stile3, stile4, stile5}); }
    
    @FXML private void selezionaContenuto1() { selezionaStelle("contenuto", 1, new Button[]{contenuto1, contenuto2, contenuto3, contenuto4, contenuto5}); }
    @FXML private void selezionaContenuto2() { selezionaStelle("contenuto", 2, new Button[]{contenuto1, contenuto2, contenuto3, contenuto4, contenuto5}); }
    @FXML private void selezionaContenuto3() { selezionaStelle("contenuto", 3, new Button[]{contenuto1, contenuto2, contenuto3, contenuto4, contenuto5}); }
    @FXML private void selezionaContenuto4() { selezionaStelle("contenuto", 4, new Button[]{contenuto1, contenuto2, contenuto3, contenuto4, contenuto5}); }
    @FXML private void selezionaContenuto5() { selezionaStelle("contenuto", 5, new Button[]{contenuto1, contenuto2, contenuto3, contenuto4, contenuto5}); }
    
    @FXML private void selezionaGradevolezza1() { selezionaStelle("gradevolezza", 1, new Button[]{gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5}); }
    @FXML private void selezionaGradevolezza2() { selezionaStelle("gradevolezza", 2, new Button[]{gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5}); }
    @FXML private void selezionaGradevolezza3() { selezionaStelle("gradevolezza", 3, new Button[]{gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5}); }
    @FXML private void selezionaGradevolezza4() { selezionaStelle("gradevolezza", 4, new Button[]{gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5}); }
    @FXML private void selezionaGradevolezza5() { selezionaStelle("gradevolezza", 5, new Button[]{gradevolezza1, gradevolezza2, gradevolezza3, gradevolezza4, gradevolezza5}); }
    
    @FXML private void selezionaOriginalita1() { selezionaStelle("originalita", 1, new Button[]{originalita1, originalita2, originalita3, originalita4, originalita5}); }
    @FXML private void selezionaOriginalita2() { selezionaStelle("originalita", 2, new Button[]{originalita1, originalita2, originalita3, originalita4, originalita5}); }
    @FXML private void selezionaOriginalita3() { selezionaStelle("originalita", 3, new Button[]{originalita1, originalita2, originalita3, originalita4, originalita5}); }
    @FXML private void selezionaOriginalita4() { selezionaStelle("originalita", 4, new Button[]{originalita1, originalita2, originalita3, originalita4, originalita5}); }
    @FXML private void selezionaOriginalita5() { selezionaStelle("originalita", 5, new Button[]{originalita1, originalita2, originalita3, originalita4, originalita5}); }
    
    @FXML private void selezionaEdizione1() { selezionaStelle("edizione", 1, new Button[]{edizione1, edizione2, edizione3, edizione4, edizione5}); }
    @FXML private void selezionaEdizione2() { selezionaStelle("edizione", 2, new Button[]{edizione1, edizione2, edizione3, edizione4, edizione5}); }
    @FXML private void selezionaEdizione3() { selezionaStelle("edizione", 3, new Button[]{edizione1, edizione2, edizione3, edizione4, edizione5}); }
    @FXML private void selezionaEdizione4() { selezionaStelle("edizione", 4, new Button[]{edizione1, edizione2, edizione3, edizione4, edizione5}); }
    @FXML private void selezionaEdizione5() { selezionaStelle("edizione", 5, new Button[]{edizione1, edizione2, edizione3, edizione4, edizione5}); }

    /**
     * Metodo di utilità per mostrare un {@link Alert} di tipo ERRORE.
     *
     * @param title Il titolo della finestra di dialogo.
     * @param message Il messaggio di errore da visualizzare.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Metodo di utilità per mostrare un {@link Alert} di tipo INFORMAZIONE.
     *
     * @param title Il titolo della finestra di dialogo.
     * @param message Il messaggio informativo da visualizzare.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
