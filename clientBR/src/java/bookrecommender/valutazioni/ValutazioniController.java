package bookrecommender.valutazioni;


import bookrecommender.condivisi.valutazioni.ValutazioneDettagliata;
import bookrecommender.condivisi.valutazioni.ValutazioneService;
import bookrecommender.utenti.GestoreSessione;
import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utili.ViewsController;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller per la vista di gestione delle valutazioni dell'utente ({@code valutazioni-view.fxml}).
 * <p>
 * Questa classe gestisce l'interfaccia utente che permette a un utente loggato di visualizzare,
 * modificare ed eliminare le proprie recensioni di libri.
 * </p>
 *
 * <b>Responsabilità principali:</b>
 * <ul>
 *     <li>Caricare e visualizzare l'elenco di tutte le valutazioni effettuate dall'utente corrente.</li>
 *     <li>Fornire un editor dettagliato per modificare una valutazione selezionata. L'editor include:
 *         <ul>
 *             <li>Controlli a "stella" (realizzati con icone) per modificare i punteggi dei 5 criteri.</li>
 *             <li>Campi di testo per modificare le note associate a ciascun criterio e il commento finale.</li>
 *         </ul>
 *     </li>
 *     <li>Calcolare e visualizzare dinamicamente il voto complessivo aggiornato durante la modifica.</li>
 *     <li>Interagire con il servizio RMI {@link ValutazioneService} per persistere le modifiche (aggiornamento o eliminazione).</li>
 *     <li>Gestire la navigazione per tornare all'area privata.</li>
 *     <li>Controllare un'animazione di sfondo per migliorare l'esperienza utente.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see ValutazioneDettagliata
 * @see ValutazioneService
 * @see ViewsController
 * @version 1.0
 */
public class ValutazioniController implements Initializable {

    /** Il pannello radice della vista. */
    @FXML private StackPane rootPane;
    /** Il canvas su cui viene disegnata l'animazione di sfondo. */
    @FXML private Canvas backgroundCanvas;

    /** Etichetta per mostrare un sottotitolo, come il numero totale di recensioni. */
    @FXML private Label subtitleLabel;
    /** ListView per visualizzare l'elenco delle valutazioni dell'utente. */
    @FXML private ListView<ValutazioneDettagliata> valutazioniListView;
    /** Pannello mostrato quando nessuna valutazione è selezionata. */
    @FXML private VBox placeholderPanel;
    /** Pannello che contiene l'editor dei dettagli della valutazione. */
    @FXML private VBox detailPanel;
    /** Etichetta per mostrare le informazioni del libro e della libreria della valutazione selezionata. */
    @FXML private Label selectedValutazioneInfo;
    /** Etichetta per mostrare messaggi di stato o di errore. */
    @FXML private Label messageLabel;

    /** Contenitore per le stelle del criterio "Stile". */
    @FXML private HBox stileStarsBox;
    /** Contenitore per le stelle del criterio "Contenuto". */
    @FXML private HBox contenutoStarsBox;
    /** Contenitore per le stelle del criterio "Gradevolezza". */
    @FXML private HBox gradevolezzaStarsBox;
    /** Contenitore per le stelle del criterio "Originalità". */
    @FXML private HBox originalitaStarsBox;
    /** Contenitore per le stelle del criterio "Edizione". */
    @FXML private HBox edizioneStarsBox;

    /** Contenitore per le stelle del voto complessivo (sola lettura). */
    @FXML private HBox overallStarsBox;
    /** Etichetta per visualizzare il valore numerico del voto complessivo. */
    @FXML private Label votoComplessivoLabel;

    /** Campo di testo per le note sul criterio "Stile". */
    @FXML private TextArea stileNote;
    /** Campo di testo per le note sul criterio "Contenuto". */
    @FXML private TextArea contenutoNote;
    /** Campo di testo per le note sul criterio "Gradevolezza". */
    @FXML private TextArea gradevolezzaNote;
    /** Campo di testo per le note sul criterio "Originalità". */
    @FXML private TextArea originalitaNote;
    /** Campo di testo per le note sul criterio "Edizione". */
    @FXML private TextArea edizioneNote;
    /** Campo di testo per il commento finale sulla valutazione. */
    @FXML private TextArea commentoFinale;

    /** Pulsante per salvare le modifiche alla valutazione selezionata. */
    @FXML private Button saveButton;
    /** Pulsante per eliminare la valutazione selezionata. */
    @FXML private Button deleteButton;
    /** Pulsante per annullare la selezione e resettare l'editor. */
    @FXML private Button cancelButton;

    /** Riferimento al servizio RMI per le operazioni sulle valutazioni. */
    private ValutazioneService valutazioneService;
    /** La valutazione attualmente selezionata nella ListView e mostrata nell'editor. */
    private ValutazioneDettagliata selectedValutazione;
    /** L'ID dell'utente loggato, recuperato dalla sessione. */
    private String currentUserId;
    /** Gestore dell'animazione di sfondo. */
    private ParticleAnimation particleAnimation;

    /**
     * Metodo chiamato da JavaFX all'inizializzazione del controller.
     * <p>
     * Esegue la configurazione iniziale della vista, che include:
     * <ul>
     *     <li>Connessione ai servizi RMI.</li>
     *     <li>Avvio dell'animazione di sfondo.</li>
     *     <li>Configurazione della visualizzazione delle celle nella ListView.</li>
     *     <li>Configurazione delle aree di testo.</li>
     *     <li>Inizializzazione dei controlli a stella.</li>
     *     <li>Caricamento delle valutazioni dell'utente.</li>
     *     <li>Impostazione del listener per la selezione degli elementi.</li>
     * </ul>
     *
     * @param location L'URL della risorsa FXML, fornito da JavaFX.
     * @param resources Il ResourceBundle, fornito da JavaFX.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupServices();
        setupParticleAnimation();
        setupListCellFactory();
        configureTextAreas();
        // inizializza le stelle per ogni box
        initStarsFor(stileStarsBox);
        initStarsFor(contenutoStarsBox);
        initStarsFor(gradevolezzaStarsBox);
        initStarsFor(originalitaStarsBox);
        initStarsFor(edizioneStarsBox);
        initStarsFor(overallStarsBox); // visuale read-only (non cliccabile)
        loadValutazioni();
        setupSelectionListener();
        resetEditor();
    }

    /**
     * Stabilisce la connessione al servizio RMI {@link ValutazioneService} e recupera
     * l'ID dell'utente corrente dalla sessione.
     */
    private void setupServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            valutazioneService = (ValutazioneService) registry.lookup("ValutazioneService");
            currentUserId = GestoreSessione.getInstance().getCurrentUserId();
        } catch (Exception e) {
            if (messageLabel != null) messageLabel.setText("Errore collegamento servizio valutazioni");
        }
    }

    /**
     * Inizializza e avvia l'animazione di sfondo, assicurandosi che venga
     * fermata correttamente alla chiusura della finestra per liberare risorse.
     */
    private void setupParticleAnimation() {
        if (rootPane != null && backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();
            backgroundCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                        if (newWindow != null) {
                            newWindow.setOnCloseRequest(event -> {
                                if (particleAnimation != null) particleAnimation.stop();
                            });
                        }
                    });
                }
            });
        }
    }

    /**
     * Configura la {@link ListCell} per la {@code valutazioniListView}, definendo come
     * visualizzare ogni oggetto {@link ValutazioneDettagliata} nella lista.
     */
    private void setupListCellFactory() {
        Callback<ListView<ValutazioneDettagliata>, ListCell<ValutazioneDettagliata>> factory = lv -> new ListCell<ValutazioneDettagliata>() {
            @Override
            protected void updateItem(ValutazioneDettagliata item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getDisplayText());
            }
        };
        valutazioniListView.setCellFactory(factory);
    }

    /**
     * Configura le proprietà delle {@link TextArea} nell'editor per garantire
     * un layout corretto e il wrapping del testo.
     */
    private void configureTextAreas() {
        TextArea[] areas = new TextArea[] {
                stileNote, contenutoNote, gradevolezzaNote, originalitaNote, edizioneNote, commentoFinale
        };
        for (TextArea ta : areas) {
            if (ta == null) continue;
            ta.setMaxWidth(Double.MAX_VALUE);
            ta.setWrapText(true);
            VBox.setVgrow(ta, Priority.ALWAYS);
        }
    }

    /**
     * Imposta un listener sulla proprietà di selezione della {@code valutazioniListView}.
     * Quando un elemento viene selezionato, popola l'editor con i suoi dettagli.
     */
    private void setupSelectionListener() {
        valutazioniListView.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) onSelectValutazione(newV);
            else resetEditor();
        });
    }

    /**
     * Gestisce l'evento di selezione di una valutazione dalla lista.
     * Popola tutti i campi dell'editor (stelle, note, commento) con i dati
     * della valutazione selezionata e rende visibile il pannello dei dettagli.
     *
     * @param v La {@link ValutazioneDettagliata} selezionata.
     */
    private void onSelectValutazione(ValutazioneDettagliata v) {
        selectedValutazione = v;
        if (selectedValutazioneInfo != null) {
            selectedValutazioneInfo.setText(String.format(
                    "Libro: %s (%s)\nLibreria: %s",
                    v.titoloLibro(), v.autoreLibro(), v.nomeLibreria()
            ));
        }

        // popola le stelle con i valori esistenti (se presenti)
        setStarsValue(stileStarsBox, safeInt(v.stile(), 3));
        setStarsValue(contenutoStarsBox, safeInt(v.contenuto(), 3));
        setStarsValue(gradevolezzaStarsBox, safeInt(v.gradevolezza(), 3));
        setStarsValue(originalitaStarsBox, safeInt(v.originalita(), 3));
        setStarsValue(edizioneStarsBox, safeInt(v.edizione(), 3));

        // note e commento
        if (stileNote != null) stileNote.setText(v.stileNote());
        if (contenutoNote != null) contenutoNote.setText(v.contenutoNote());
        if (gradevolezzaNote != null) gradevolezzaNote.setText(v.gradevolezzaNote());
        if (originalitaNote != null) originalitaNote.setText(v.originalitaNote());
        if (edizioneNote != null) edizioneNote.setText(v.edizioneNote());
        if (commentoFinale != null) commentoFinale.setText(v.commentoFinale());

        // mostra pannello dettaglio
        if (placeholderPanel != null) {
            placeholderPanel.setVisible(false);
            placeholderPanel.setManaged(false);
        }

        if (detailPanel != null) {
            detailPanel.setVisible(true);
            detailPanel.setManaged(true);
        }

        updateVotoComplessivo();
    }

    /**
     * Metodo di utilità per ottenere un valore intero da 1 a 5, con un fallback.
     *
     * @param val Il valore da controllare.
     * @param fallback Il valore da restituire se {@code val} non è nell'intervallo.
     * @return Il valore originale se valido, altrimenti il fallback.
     */
    private int safeInt(int val, int fallback) {
        // ValutazioneDettagliata usa int primitivo ma teniamo fallback
        return val >= 1 && val <= 5 ? val : fallback;
    }

    /**
     * Resetta l'editor al suo stato iniziale, nascondendo il pannello dei dettagli e mostrando il placeholder.
     */
    private void resetEditor() {
        selectedValutazione = null;
        if (selectedValutazioneInfo != null) selectedValutazioneInfo.setText("");
        setStarsValue(stileStarsBox, 3);
        setStarsValue(contenutoStarsBox, 3);
        setStarsValue(gradevolezzaStarsBox, 3);
        setStarsValue(originalitaStarsBox, 3);
        setStarsValue(edizioneStarsBox, 3);

        if (stileNote != null) stileNote.clear();
        if (contenutoNote != null) contenutoNote.clear();
        if (gradevolezzaNote != null) gradevolezzaNote.clear();
        if (originalitaNote != null) originalitaNote.clear();
        if (edizioneNote != null) edizioneNote.clear();
        if (commentoFinale != null) commentoFinale.clear();

        if (detailPanel != null) {
            detailPanel.setVisible(false);
            detailPanel.setManaged(false);
        }

        if (placeholderPanel != null) {
            placeholderPanel.setVisible(true);
            placeholderPanel.setManaged(true);
        }

        updateVotoComplessivo();
    }

    /**
     * Carica l'elenco delle valutazioni dell'utente corrente dal servizio RMI
     * e le popola nella {@code valutazioniListView}.
     */
    private void loadValutazioni() {
        if (messageLabel != null) messageLabel.setText("Caricamento in corso...");
        try {
            if (valutazioneService == null || currentUserId == null) {
                if (messageLabel != null) messageLabel.setText("Servizio non disponibile");
                return;
            }
            List<ValutazioneDettagliata> mie = valutazioneService.listValutazioniDettagliateByUser(currentUserId);
            valutazioniListView.getItems().setAll(mie);
            if (subtitleLabel != null) subtitleLabel.setText("Hai scritto " + mie.size() + " recensioni");
            if (messageLabel != null) messageLabel.setText(mie.isEmpty() ? "Nessuna valutazione trovata. Inizia a recensire libri dalle tue librerie!" : "");
        } catch (Exception ex) {
            if (messageLabel != null) messageLabel.setText("Errore nel caricamento valutazioni: " + ex.getMessage());
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Salva".
     * Raccoglie i dati dall'editor, calcola il nuovo voto complessivo e invoca
     * il metodo di aggiornamento del servizio RMI.
     */
    @FXML
    private void handleSave() {
        if (selectedValutazione == null || valutazioneService == null) return;

        // Salva l'ID per poter riselezionare l'elemento dopo l'aggiornamento
        final int libroIdToReselect = selectedValutazione.libroId();

        try {
            double votoComplessivo = calcolaVotoComplessivo();

            boolean success = valutazioneService.aggiornaValutazione(
                    selectedValutazione.userId(),
                    selectedValutazione.libroId(),
                    getStarsValue(stileStarsBox),
                    stileNote.getText().trim(),
                    getStarsValue(contenutoStarsBox),
                    contenutoNote.getText().trim(),
                    getStarsValue(gradevolezzaStarsBox),
                    gradevolezzaNote.getText().trim(),
                    getStarsValue(originalitaStarsBox),
                    originalitaNote.getText().trim(),
                    getStarsValue(edizioneStarsBox),
                    edizioneNote.getText().trim(),
                    votoComplessivo,
                    commentoFinale.getText().trim()
            );

            if (success) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Esito Aggiornamento");
                alert.setHeaderText(null);
                alert.setContentText("Valutazione aggiornata con successo!");
                alert.showAndWait();

                loadValutazioni();

                // Riseleziona l'elemento modificato per dare un feedback visivo all'utente
                valutazioniListView.getItems().stream()
                    .filter(v -> v.libroId() == libroIdToReselect)
                    .findFirst()
                    .ifPresent(v -> valutazioniListView.getSelectionModel().select(v));
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Esito Aggiornamento");
                alert.setHeaderText(null);
                alert.setContentText("Si è verificato un errore durante l'aggiornamento della valutazione.");
                alert.showAndWait();
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Utile per il debug
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore Critico");
            alert.setHeaderText("Si è verificato un errore imprevisto durante l'aggiornamento.");
            alert.setContentText("Dettagli: " + ex.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Elimina".
     * Chiede conferma all'utente e, in caso affermativo, invoca il metodo di
     * eliminazione del servizio RMI.
     */
    @FXML
    private void handleDelete() {
        ValutazioneDettagliata sel = selectedValutazione;
        if (sel == null || valutazioneService == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare questa valutazione?");
        alert.setContentText(String.format("Stai per eliminare la valutazione di:\n\n" +
                "📖 %s (%s)\n📚 %s\n\nQuesta azione non può essere annullata.",
                sel.titoloLibro(), sel.autoreLibro(), sel.nomeLibreria()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = valutazioneService.eliminaValutazione(sel.userId(), sel.libroId());
                if (success) {
                    valutazioniListView.getItems().remove(sel);
                    if (messageLabel != null) messageLabel.setText("🗑 Valutazione eliminata con successo");
                    if (subtitleLabel != null) subtitleLabel.setText("Hai scritto " + valutazioniListView.getItems().size() + " recensioni");
                } else {
                    if (messageLabel != null) messageLabel.setText("❌ Errore durante l'eliminazione");
                }
            } catch (Exception ex) {
                if (messageLabel != null) messageLabel.setText("❌ Errore durante l'eliminazione: " + ex.getMessage());
            }
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Annulla".
     * Deseleziona l'elemento corrente nella lista e resetta l'editor.
     */
    @FXML
    private void handleCancel() {
        valutazioniListView.getSelectionModel().clearSelection();
        if (messageLabel != null) messageLabel.setText("");
    }

    /**
     * Gestisce l'evento di click sul pulsante "Indietro".
     * Ferma l'animazione e naviga alla schermata dell'area privata.
     */
    @FXML
    private void handleBack() {
        if (particleAnimation != null) particleAnimation.stop();
        ViewsController.mostraAreaPrivata();
    }

    /**
     * Inizializza un contenitore {@link HBox} con 5 icone a forma di stella.
     * @param box Il contenitore HBox da popolare con le stelle.
     */
    private void initStarsFor(HBox box) {
        if (box == null) return;
        box.getChildren().clear();
        boolean clickable = box != overallStarsBox;
        for (int i = 1; i <= 5; i++) {
            FontAwesomeIconView star = new FontAwesomeIconView();
            star.setGlyphName("STAR");
            star.setSize("16");
            star.getStyleClass().add("star-icon"); // styling CSS namespaced
            final int value = i;
            if (clickable) {
                star.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                    setStarsValue(box, value);
                    updateVotoComplessivo();
                });
                // hover effect: fill on hover up to index
                star.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> highlightHover(box, value));
                star.addEventHandler(MouseEvent.MOUSE_EXITED, e -> setStarsValue(box, getStarsValue(box)));
            }
            box.getChildren().add(star);
        }
        // default 3 stelle
        setStarsValue(box, 3);
    }

    /**
     * Imposta lo stato visivo delle stelle in un contenitore, "riempiendo"
     * un numero specificato di esse.
     * @param box Il contenitore HBox delle stelle.
     * @param n Il numero di stelle da considerare "piene".
     */
    private void setStarsValue(HBox box, int n) {
        if (box == null) return;
        int idx = 0;
        for (javafx.scene.Node node : box.getChildren()) {
            if (!(node instanceof FontAwesomeIconView)) continue;
            FontAwesomeIconView star = (FontAwesomeIconView) node;
            star.getStyleClass().removeAll("star-filled", "star-empty");
            if (idx < n) star.getStyleClass().add("star-filled");
            else star.getStyleClass().add("star-empty");
            idx++;
        }
    }

    /**
     * Restituisce il valore numerico (da 1 a 5) rappresentato dalle stelle in un contenitore.
     * @param box Il contenitore HBox delle stelle.
     * @return Il numero di stelle "piene".
     */
    private int getStarsValue(HBox box) {
        if (box == null) return 0;
        int count = 0;
        for (javafx.scene.Node node : box.getChildren()) {
            if (!(node instanceof FontAwesomeIconView)) continue;
            FontAwesomeIconView star = (FontAwesomeIconView) node;
            if (star.getStyleClass().contains("star-filled")) count++;
        }
        return count == 0 ? 3 : count; // fallback 3
    }

    /**
     * Evidenzia le stelle fino a un certo indice per fornire un feedback visivo
     * durante l'hover del mouse.
     * @param box Il contenitore HBox delle stelle.
     * @param upTo L'indice fino al quale evidenziare le stelle.
     */
    private void highlightHover(HBox box, int upTo) {
        if (box == null) return;
        int idx = 0;
        for (javafx.scene.Node node : box.getChildren()) {
            if (!(node instanceof FontAwesomeIconView)) continue;
            FontAwesomeIconView star = (FontAwesomeIconView) node;
            star.getStyleClass().removeAll("star-filled", "star-empty");
            if (idx < upTo) star.getStyleClass().add("star-filled");
            else star.getStyleClass().add("star-empty");
            idx++;
        }
    }

    /**
     * Calcola il voto complessivo come media aritmetica dei punteggi dei 5 criteri.
     *
     * @return La media calcolata.
     */
    private double calcolaVotoComplessivo() {
        int s1 = getStarsValue(stileStarsBox);
        int s2 = getStarsValue(contenutoStarsBox);
        int s3 = getStarsValue(gradevolezzaStarsBox);
        int s4 = getStarsValue(originalitaStarsBox);
        int s5 = getStarsValue(edizioneStarsBox);
        return (s1 + s2 + s3 + s4 + s5) / 5.0;
    }

    /**
     * Aggiorna l'etichetta del voto complessivo e le stelle corrispondenti.
     */
    private void updateVotoComplessivo() {
        double media = calcolaVotoComplessivo();
        if (votoComplessivoLabel != null) {
            // Usiamo Locale.US per essere sicuri che il separatore decimale sia un punto.
            votoComplessivoLabel.setText(String.format(java.util.Locale.US, "%.1f", media));
        }
        // aggiorna stars overall: arrotondo alla stella più vicina
        setStarsValue(overallStarsBox, (int) Math.round(media));
    }
}
