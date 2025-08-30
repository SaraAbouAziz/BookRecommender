package bookrecommender.librerie;

import bookrecommender.condivisi.librerie.LibrerieService;
import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utenti.GestoreSessione;
import bookrecommender.utili.ViewsController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.ResourceBundle;
import bookrecommender.condivisi.libri.CercaLibriService;
import bookrecommender.condivisi.libri.Libro;

/**
 * Controller per la schermata di gestione delle librerie personali dell'utente (librerie-view.fxml).
 * <p>
 * Questa classe gestisce l'interfaccia utente per creare, eliminare e visualizzare
 * le librerie, nonché per aggiungere e rimuovere libri da esse.
 * <p>
 * <b>Responsabilità principali:</b>
 * <ul>
 *     <li>Interagire con il servizio RMI {@link LibrerieService} per tutte le operazioni sulle librerie.</li>
 *     <li>Interagire con il servizio RMI {@link CercaLibriService} per recuperare i dettagli dei libri.</li>
 *     <li>Popolare e gestire la {@link ListView} delle librerie e la {@link TableView} dei libri.</li>
 *     <li>Gestire gli eventi dell'interfaccia utente (click sui pulsanti, selezione di elementi).</li>
 *     <li>Navigare verso altre viste (es. dettagli libro, valutazione, consiglio) tramite {@link ViewsController}.</li>
 *     <li>Controllare un'animazione di sfondo {@link ParticleAnimation}.</li>
 * </ul>
 */
public class LibrerieController implements Initializable {

    //--- FXML UI Elements ---
    @FXML private TextField nomeLibreriaField;
    @FXML private TextField libroIdField;
    @FXML private ListView<String> librerieListView;
    @FXML private TableView<LibroRecord> libriTableView;
    @FXML private TableColumn<LibroRecord, Long> idLibroColumn;
    @FXML private TableColumn<LibroRecord, String> titoloLibroColumn;
    /** Colonna per il pulsante di valutazione di un libro. */
    @FXML private TableColumn<LibroRecord, Void> valutaColumn;
    /** Colonna per il pulsante per consigliare un libro. */
    @FXML private TableColumn<LibroRecord, Void> consigliaColumn;

    @FXML private Button creaLibreriaButton;
    @FXML private Button aggiungiLibroButton;
    @FXML private Button rimuoviLibroButton;
    @FXML private Button eliminaLibreriaButton;

    /** Canvas per l'animazione di sfondo. */
    @FXML private Canvas backgroundCanvas;

    //--- Services and State ---
    private ParticleAnimation particleAnimation;
    private LibrerieService librerieService;
    /** L'ID dell'utente attualmente loggato. */
    private String currentUserId;
    /** Il nome della libreria attualmente selezionata nella ListView. */
    private String selectedLibreria;

    /** Lista osservabile per i nomi delle librerie, collegata alla ListView. */
    private ObservableList<String> librerieList = FXCollections.observableArrayList();
    /** Lista osservabile per i record dei libri, collegata alla TableView. */
    private ObservableList<LibroRecord> libriList = FXCollections.observableArrayList();

    /**
     * Metodo chiamato da FXMLLoader all'inizializzazione del controller.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     *     <li>Configura le colonne della tabella e le loro celle personalizzate ({@link #setupTableColumns()}).</li>
     *     <li>Imposta i listener per gli eventi UI ({@link #setupEventHandlers()}).</li>
     *     <li>Tenta di connettersi ai servizi RMI e carica le librerie dell'utente ({@link #initializeService()}).</li>
     *     <li>Inizializza e avvia l'animazione di sfondo.</li>
     * </ol>
     *
     * @param url L'URL della risorsa FXML, non utilizzato direttamente.
     * @param rb Il ResourceBundle, non utilizzato direttamente.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTableColumns();
        setupEventHandlers();

        rimuoviLibroButton.setDisable(true);

        initializeService();
        loadLibrerie();

        if (backgroundCanvas != null) {
            if (backgroundCanvas.getParent() instanceof Region) {
                Region parent = (Region) backgroundCanvas.getParent();
                backgroundCanvas.widthProperty().bind(parent.widthProperty());
                backgroundCanvas.heightProperty().bind(parent.heightProperty());
            }
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();

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
     * Configura le colonne della {@link TableView} dei libri.
     * Imposta le {@code CellValueFactory} per collegare le colonne alle proprietà di {@link LibroRecord}.
     * Imposta le {@code CellFactory} per le colonne con pulsanti ("Valuta", "Consiglia"), creando icone SVG interattive.
     */
    private void setupTableColumns() {
        idLibroColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idLibroColumn.setStyle("-fx-alignment: CENTER;");
        titoloLibroColumn.setCellValueFactory(new PropertyValueFactory<>("titolo"));

        // Colonna Valuta: outline stellina blu fluo
        valutaColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = createOutlineIconButton(
                "M8 1.314l2.472 5.01.556 1.126h5.256l-4.25 3.09-.9.654.346 1.18 1.618 5.53-4.25-3.09-.9-.654-.9.654-4.25 3.09 1.618-5.53.346-1.18-.9-.654-4.25-3.09h5.256l.556-1.126L8 1.314z",
                "Valuta questo libro",
                event -> {
                    LibroRecord libroRecord = getTableView().getItems().get(getIndex());
                    try {
                        CercaLibriService cercaLibriService = getCercaLibriService();
                        if (cercaLibriService == null) {
                            showError("Errore", "Servizio di ricerca libri non disponibile.");
                            return;
                        }

                        Libro libro = cercaLibriService.getTitoloLibroById(libroRecord.getId().intValue());
                        if (libro == null) {
                            showError("Errore", "Impossibile trovare i dettagli del libro da valutare.");
                            return;
                        }

                        if (particleAnimation != null) {
                            particleAnimation.stop();
                        }

                        ViewsController.mostraValutazioneLibro(libro, selectedLibreria);

                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Errore", "Si è verificato un errore nell'aprire la finestra di valutazione: " + e.getMessage());
                    }
                }
            );

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        // Colonna Consiglia: outline lampadina blu fluo
        consigliaColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btn = createOutlineIconButton(
                "M2 21l21-9L2 3v7l15 2-15 2v7z",
                "Suggerisci questo libro",
                event -> {
                    LibroRecord libroRecord = getTableView().getItems().get(getIndex());
                    try {
                        CercaLibriService cercaLibriService = getCercaLibriService();
                        if (cercaLibriService == null) {
                            showError("Errore", "Servizio di ricerca libri non disponibile.");
                            return;
                        }

                        Libro libroCompleto = cercaLibriService.getTitoloLibroById(libroRecord.getId().intValue());
                        if (libroCompleto == null) {
                            showError("Errore", "Impossibile trovare i dettagli del libro da consigliare.");
                            return;
                        }

                        int libreriaId = librerieService.getLibreriaId(currentUserId, selectedLibreria);
                        if (libreriaId == -1) {
                            showError("Errore", "Impossibile trovare l'ID della libreria selezionata.");
                            return;
                        }

                        ViewsController.mostraConsigliaLibro(libroCompleto, libreriaId);

                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Errore", "Si è verificato un errore nell'aprire la finestra di consiglio: " + e.getMessage());
                    }
                }
            );

            {
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        libriTableView.setItems(libriList);
    }

    /**
      * Crea un pulsante personalizzato con un'icona SVG.
      * L'icona ha un contorno blu e si riempie al passaggio del mouse o al click.
      *
      * @param svgPathContent Il contenuto del path SVG per l'icona.
      * @param tooltipText    Il testo da mostrare nel tooltip del pulsante.
      * @param actionHandler  L'azione da eseguire al click del pulsante.
      * @return Un {@link Button} configurato con l'icona e l'azione.
      */
    private Button createOutlineIconButton(String svgPathContent, String tooltipText,
                                           EventHandler<ActionEvent> actionHandler) {
        SVGPath svg = new SVGPath();
        svg.setContent(svgPathContent);
        svg.setFill(Color.TRANSPARENT); // inizialmente solo outline
        svg.setStroke(Color.web("#00d8f0"));
        svg.setStrokeWidth(1.2);
        svg.setScaleX(0.8);
        svg.setScaleY(0.8);

        StackPane wrapper = new StackPane(svg);
        wrapper.setMinSize(20, 20);
        wrapper.setPrefSize(20, 20);
        wrapper.setMaxSize(20, 20);

        Button btn = new Button();
        btn.setGraphic(wrapper);
        // MODIFICA QUI: Aggiunte le proprietà per rimuovere l'outline del focus
        btn.setStyle("-fx-background-color: transparent; -fx-padding: 2; -fx-focus-color: transparent; -fx-faint-focus-color: transparent;");
        btn.setTooltip(new Tooltip(tooltipText));
        btn.setFocusTraversable(true); // permettere focus via tastiera
        btn.setMinSize(24, 24);
        btn.setPrefSize(24, 24);
        btn.setMaxSize(24, 24);

        // stato toggled memorizzato in array mutabile chiuso nella lambda
        final boolean[] toggled = new boolean[]{false};

        // hover: riempi temporaneamente se non toggled
        btn.hoverProperty().addListener((obs, wasHover, isNowHover) -> {
            if (isNowHover) {
                svg.setFill(Color.web("#00d8f0"));
            } else {
                if (!toggled[0]) {
                    svg.setFill(Color.TRANSPARENT);
                } else {
                    // rimane riempito se toggled
                    svg.setFill(Color.web("#00d8f0"));
                }
            }
        });

        // click: toggle stato (persistente)
        btn.setOnAction(event -> {
            toggled[0] = !toggled[0];
            if (toggled[0]) {
                svg.setFill(Color.web("#00d8f0"));
            } else {
                // se non hover, rimuovi fill; altrimenti hover listener lo gestirà
                if (!btn.isHover()) {
                    svg.setFill(Color.TRANSPARENT);
                }
            }
            // chiamare l'handler esterno (es. apertura dialog)
            try {
                actionHandler.handle(event);
            } catch (Exception ex) {
                // non interrompiamo l'interfaccia per errori interni al handler
                ex.printStackTrace();
            }
        });

        // accessibilità
        btn.setAccessibleText(tooltipText);

        return btn;
    }

    /**
     * Imposta i gestori di eventi per i componenti UI interattivi.
     * Aggiunge listener alla selezione della {@link ListView} delle librerie e della {@link TableView} dei libri,
     * e gestisce il doppio click su una riga della tabella per mostrare i dettagli del libro.
     * Aggiunge anche un filtro per il tasto Invio sul campo di aggiunta libro.
     */
    private void setupEventHandlers() {
        librerieListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    selectedLibreria = newValue;
                    loadLibriInLibreria(selectedLibreria);
                }
            }
        );

        libriTableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                rimuoviLibroButton.setDisable(newValue == null);
            }
        );

        libriTableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LibroRecord selectedRecord = libriTableView.getSelectionModel().getSelectedItem();
                if (selectedRecord != null) {
                    mostraDettagliLibro(selectedRecord.getId());
                }
            }
        });

        // supporto tastiera: invio su campo libroId per aggiungere
        libroIdField.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                aggiungiLibro();
                ev.consume();
            }
        });
    }

    /**
     * Inizializza la connessione al servizio RMI {@link LibrerieService}.
     * Recupera l'ID dell'utente corrente dalla {@link GestoreSessione}.
     * In caso di fallimento, disabilita i controlli dell'interfaccia.
     */
    private void initializeService() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            librerieService = (LibrerieService) registry.lookup("LibrerieService");
            currentUserId = GestoreSessione.getInstance().getCurrentUserId();

            if (currentUserId == null) {
                showError("Errore di autenticazione", "Utente non autenticato. Effettua nuovamente il login.");
                return;
            }

            System.out.println("LibrerieService inizializzato correttamente per l'utente: " + currentUserId);

            enableControls();

        } catch (Exception e) {
            System.err.println("Errore nell'inizializzazione del servizio librerie: " + e.getMessage());
            e.printStackTrace();
            showError("Errore di connessione", "Impossibile connettersi al servizio librerie. Verifica che il server sia in esecuzione. Errore: " + e.getMessage());

            disableControls();
        }
    }

    /**
     * Disabilita i principali controlli dell'interfaccia, tipicamente in caso di errore di connessione al servizio.
     */
    private void disableControls() {
        creaLibreriaButton.setDisable(true);
        aggiungiLibroButton.setDisable(true);
        rimuoviLibroButton.setDisable(true);
        eliminaLibreriaButton.setDisable(true);
        nomeLibreriaField.setDisable(true);
        libroIdField.setDisable(true);
    }

    /**
     * Abilita i controlli dell'interfaccia, tipicamente dopo una connessione al servizio riuscita.
     */
    private void enableControls() {
        creaLibreriaButton.setDisable(false);
        aggiungiLibroButton.setDisable(false);
        eliminaLibreriaButton.setDisable(false);
        nomeLibreriaField.setDisable(false);
        libroIdField.setDisable(false);
    }

    /**
     * Carica la lista delle librerie dell'utente corrente dal servizio RMI
     * e le visualizza nella {@link ListView}.
     */
    private void loadLibrerie() {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        try {
            if (currentUserId != null) {
                List<String> librerie = librerieService.getLibrerieUtente(currentUserId);
                librerieList.clear();
                librerieList.addAll(librerie);
                librerieListView.setItems(librerieList);
                System.out.println("Caricate " + librerie.size() + " librerie per l'utente: " + currentUserId);
            } else {
                showError("Errore", "Utente non autenticato");
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento delle librerie: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Impossibile caricare le librerie: " + e.getMessage());
        }
    }

    /**
     * Carica la lista dei libri contenuti nella libreria selezionata.
     * Recupera gli ID dei libri dal {@link LibrerieService} e poi i dettagli (titolo)
     * dal {@link CercaLibriService}, popolando la {@link TableView}.
     */
    private void loadLibriInLibreria(String nomeLibreria) {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        try {
            if (currentUserId != null) {
                List<Long> libriIds = librerieService.getLibriInLibreria(currentUserId, nomeLibreria);
                libriList.clear();

                CercaLibriService cercaLibriService = getCercaLibriService();
                if (cercaLibriService != null) {
                    for (Long id : libriIds) {
                        Libro libro = cercaLibriService.getTitoloLibroById(id.intValue());
                        if (libro != null) {
                            libriList.add(new LibroRecord(
                                libro.id(),
                                libro.titolo() != null ? libro.titolo() : "N/D"
                            ));
                        } else {
                            libriList.add(new LibroRecord(id, "Libro non trovato"));
                        }
                    }
                } else {
                    for (Long id : libriIds) {
                        libriList.add(new LibroRecord(id, "Titolo " + id));
                    }
                }
                System.out.println("Caricati " + libriIds.size() + " libri dalla libreria: " + nomeLibreria);
            } else {
                showError("Errore", "Utente non autenticato");
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dei libri della libreria: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Impossibile caricare i libri della libreria: " + e.getMessage());
        }
    }

    /**
     * Recupera un'istanza del servizio {@link CercaLibriService} dal registro RMI.
     * Questo metodo esegue un lookup ogni volta che viene chiamato.
     *
     * @return Un'istanza di {@link CercaLibriService} se trovato, altrimenti {@code null}.
     */
    public CercaLibriService getCercaLibriService() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            return (CercaLibriService) registry.lookup("CercaLibriService");
        } catch (Exception e) {
            System.err.println("Errore nel recupero del servizio CercaLibriService: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Crea Libreria".
     * Valida l'input e chiama il servizio RMI per creare una nuova libreria.
     */
    @FXML
    private void creaLibreria() {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        String nomeLibreria = nomeLibreriaField.getText().trim();

        if (nomeLibreria.isEmpty()) {
            showError("Errore", "Il nome della libreria non può essere vuoto");
            return;
        }

        if (nomeLibreria.contains(":") || nomeLibreria.contains(";")) {
            showError("Errore", "Il nome della libreria non può contenere i caratteri ':' o ';'");
            return;
        }

        try {
            if (currentUserId != null) {
                if (librerieService.isNomeLibreriaEsistente(currentUserId, nomeLibreria)) {
                    showError("Errore", "La libreria '" + nomeLibreria + "' è già esistente. Riprova con un nome diverso.");
                    return;
                }

                boolean success = librerieService.creaLibreria(currentUserId, nomeLibreria);
                if (success) {
                    showInfo("Successo", "Libreria '" + nomeLibreria + "' creata con successo!");
                    nomeLibreriaField.clear();
                    loadLibrerie();
                } else {
                    showError("Errore", "Impossibile creare la libreria. Riprova.");
                }
            } else {
                showError("Errore", "Utente non autenticato");
            }
        } catch (Exception e) {
            System.err.println("Errore durante la creazione della libreria: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Errore durante la creazione della libreria: " + e.getMessage());
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Aggiungi Libro".
     * Valida l'input e chiama il servizio RMI per aggiungere un libro alla libreria selezionata.
     */
    @FXML
    private void aggiungiLibro() {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        if (selectedLibreria == null) {
            showError("Errore", "Seleziona prima una libreria");
            return;
        }

        String libroIdText = libroIdField.getText().trim();
        if (libroIdText.isEmpty()) {
            showError("Errore", "Inserisci l'ID del libro");
            return;
        }

        try {
            long libroId = Long.parseLong(libroIdText);

            if (currentUserId != null) {
                boolean success = librerieService.aggiungiLibroALibreria(currentUserId, selectedLibreria, libroId);
                if (success) {
                    showInfo("Successo", "Libro aggiunto alla libreria '" + selectedLibreria + "'!");
                    libroIdField.clear();
                    loadLibriInLibreria(selectedLibreria);
                } else {
                    showError("Errore", "Impossibile aggiungere il libro alla libreria. Verifica che l'ID sia corretto.");
                }
            } else {
                showError("Errore", "Utente non autenticato");
            }
        } catch (NumberFormatException e) {
            showError("Errore", "L'ID del libro deve essere un numero");
        } catch (Exception e) {
            System.err.println("Errore durante l'aggiunta del libro: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Errore durante l'aggiunta del libro: " + e.getMessage());
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Rimuovi Libro".
     * Rimuove il libro selezionato dalla libreria corrente tramite il servizio RMI.
     */
    @FXML
    private void rimuoviLibro() {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        if (selectedLibreria == null) {
            showError("Errore", "Seleziona prima una libreria");
            return;
        }

        LibroRecord selectedLibro = libriTableView.getSelectionModel().getSelectedItem();
        if (selectedLibro == null) {
            showError("Errore", "Seleziona prima un libro da rimuovere");
            return;
        }

        try {
            if (currentUserId != null) {
                boolean success = librerieService.rimuoviLibroDaLibreria(currentUserId, selectedLibreria, selectedLibro.getId());
                if (success) {
                    showInfo("Successo", "Libro rimosso dalla libreria '" + selectedLibreria + "'!");
                    loadLibriInLibreria(selectedLibreria);
                } else {
                    showError("Errore", "Impossibile rimuovere il libro dalla libreria.");
                }
            } else {
                showError("Errore", "Utente non autenticato");
            }
        } catch (Exception e) {
            System.err.println("Errore durante la rimozione del libro: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Errore durante la rimozione del libro: " + e.getMessage());
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Elimina Libreria".
     * Chiede conferma all'utente e, in caso affermativo, elimina la libreria selezionata.
     */
    @FXML
    private void eliminaLibreria() {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        if (selectedLibreria == null) {
            showError("Errore", "Seleziona prima una libreria da eliminare");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Elimina libreria");
        alert.setContentText("Sei sicuro di voler eliminare la libreria '" + selectedLibreria + "'? Questa azione non può essere annullata.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (currentUserId != null) {
                        boolean success = librerieService.eliminaLibreria(currentUserId, selectedLibreria);
                        if (success) {
                            showInfo("Successo", "Libreria '" + selectedLibreria + "' eliminata con successo!");
                            selectedLibreria = null;
                            libriList.clear();
                            loadLibrerie();
                        } else {
                            showError("Errore", "Impossibile eliminare la libreria.");
                        }
                    } else {
                        showError("Errore", "Utente non autenticato");
                    }
                } catch (Exception e) {
                    System.err.println("Errore durante l'eliminazione della libreria: " + e.getMessage());
                    e.printStackTrace();
                    showError("Errore", "Errore durante l'eliminazione della libreria: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Gestisce l'evento di click sul pulsante di refresh, ricaricando i dati delle librerie e dei libri.
     */
    @FXML
    private void refreshData() {
        if (librerieService == null) {
            showError("Errore", "Servizio librerie non disponibile. Verifica la connessione al server.");
            return;
        }

        loadLibrerie();
        if (selectedLibreria != null) {
            loadLibriInLibreria(selectedLibreria);
        }
    }

    /**
     * Mostra la vista dei dettagli per il libro selezionato.
     * @param libroId L'ID del libro di cui mostrare i dettagli.
     */
    private void mostraDettagliLibro(Long libroId) {
        try {
            CercaLibriService cercaLibriService = getCercaLibriService();
            if (cercaLibriService != null) {
                Libro libro = cercaLibriService.getTitoloLibroById(libroId.intValue());
                if (libro != null) {
                    if (particleAnimation != null) {
                        particleAnimation.stop();
                    }
                    ViewsController.mostraDettagliLibro(libro, ViewsController.Provenienza.LIBRERIE);
                } else {
                    showError("Errore", "Dettagli del libro non trovati.");
                }
            } else {
                showError("Errore", "Servizio di ricerca libri non disponibile.");
            }
        } catch (Exception e) {
            System.err.println("Errore nell'aprire i dettagli del libro: " + e.getMessage());
            e.printStackTrace();
            showError("Errore", "Impossibile aprire i dettagli del libro: " + e.getMessage());
        }
    }
    

    /**
     * Mostra un dialogo di alert di tipo ERRORE.
     * @param title Il titolo della finestra di dialogo.
     * @param message Il messaggio da visualizzare.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Mostra un dialogo di alert di tipo INFORMAZIONE.
     * @param title Il titolo della finestra di dialogo.
     * @param message Il messaggio da visualizzare.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /**
     * Gestisce l'evento di click sul pulsante "Indietro".
     * Ferma l'animazione e naviga all'area privata.
     */
    @FXML
    private void onBack() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        ViewsController.mostraAreaPrivata();
    }

    /**
     * Classe interna (JavaFX Bean) per rappresentare un libro nella {@link TableView}.
     * Contiene solo i dati necessari per la visualizzazione nella tabella (ID e titolo).
     */
    public static class LibroRecord {
        private final Long id;
        private final String titolo;

        public LibroRecord(Long id, String titolo) {
            this.id = id;
            this.titolo = titolo;
        }

        public Long getId() { return id; }
        public String getTitolo() { return titolo; }
    }
}
