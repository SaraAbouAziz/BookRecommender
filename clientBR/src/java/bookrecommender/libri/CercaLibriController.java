package bookrecommender.libri;

import bookrecommender.condivisi.libri.CercaLibriService;
import bookrecommender.condivisi.libri.Libro;
import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utili.ViewsController;
import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller per la schermata di ricerca dei libri (cercaLibri-view.fxml).
 * <p>
 * Questa classe gestisce l'interfaccia utente per la ricerca di libri nel catalogo.
 * Le sue responsabilità principali includono:
 * <ul>
 *     <li>Gestire diverse modalità di ricerca (per ID, titolo, autore, autore e anno)
 *         tramite un gruppo di {@link ToggleButton}.</li>
 *     <li>Aggiornare dinamicamente l'interfaccia per mostrare solo i campi di input
 *         rilevanti per la modalità di ricerca selezionata.</li>
 *     <li>Comunicare con il servizio RMI {@link CercaLibriService} per eseguire le ricerche
 *         sul server.</li>
 *     <li>Gestire i casi in cui il servizio RMI non è disponibile, funzionando in una
 *         "modalità demo" con dati di fallback locali.</li>
 *     <li>Visualizzare i risultati della ricerca in una {@link TableView}.</li>
 *     <li>Gestire la navigazione, permettendo all'utente di tornare alla schermata precedente
 *         o di visualizzare i dettagli di un libro con un doppio click.</li>
 *     <li>Controllare un'animazione di sfondo {@link ParticleAnimation}.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @version 1.0
 */
public class CercaLibriController implements Initializable {

    private static final Logger logger = LogManager.getLogger(CercaLibriController.class);

    private CercaLibriService cercaLibriService;

   
    
    /** Gruppo che gestisce l'esclusività dei ToggleButton per la modalità di ricerca. */
    @FXML private ToggleGroup searchModeGroup;
    /** ToggleButton per la modalità di ricerca per ID. */
    @FXML private ToggleButton tbById;
    /** ToggleButton per la modalità di ricerca per Titolo. */
    @FXML private ToggleButton tbByTitle;
    /** ToggleButton per la modalità di ricerca per Autore. */
    @FXML private ToggleButton tbByAuthor;
    /** ToggleButton per la modalità di ricerca per Autore e Anno. */
    @FXML private ToggleButton tbByAuthorYear;

    // Contenitori per i campi di input, la cui visibilità è gestita dinamicamente.
    /** Contenitore per il campo di input dell'ID. */
    @FXML private VBox idBox;
    /** Contenitore per il campo di input del Titolo. */
    @FXML private VBox titleBox;
    /** Contenitore per il campo di input dell'Autore. */
    @FXML private VBox authorBox;
    /** Contenitore per i campi di input di Autore e Anno. */
    @FXML private GridPane authorYearBox;

    // Campi di testo per l'input utente
    /** Campo di testo per l'ID del libro. */
    @FXML private TextField idField;
    /** Campo di testo per il titolo del libro. */
    @FXML private TextField titleField;
    /** Campo di testo per l'autore del libro (nella modalità solo autore). */
    @FXML private TextField authorField;
    /** Campo di testo per l'autore del libro (nella modalità autore e anno). */
    @FXML private TextField authorYearAuthorField;
    /** Campo di testo per l'anno di pubblicazione. */
    @FXML private TextField authorYearField;

    // Elementi UI generali
    /** Etichetta per mostrare messaggi di stato o di errore all'utente. */
    @FXML private Label messageLabel;
    /** Pulsante per avviare la ricerca. */
    @FXML private Button searchButton;

    // Tabella per i risultati
    /** Tabella per visualizzare i risultati della ricerca. */
    @FXML private TableView<BookRecord> resultsTable;
    /** Colonna della tabella per l'ID del libro. */
    @FXML private TableColumn<BookRecord, String> colId;
    /** Colonna della tabella per il titolo del libro. */
    @FXML private TableColumn<BookRecord, String> colTitle;
    /** Colonna della tabella per l'autore del libro. */
    @FXML private TableColumn<BookRecord, String> colAuthor;
    /** Colonna della tabella per l'anno di pubblicazione. */
    @FXML private TableColumn<BookRecord, String> colYear;

    /** Canvas per l'animazione di sfondo. */
    @FXML
    private Canvas backgroundCanvas;

    /** Gestore dell'animazione di sfondo. */
    private ParticleAnimation particleAnimation;

    /** Lista di dati di esempio usata come fallback quando il servizio RMI non è disponibile. */
    private final List<BookRecord> demoData = new ArrayList<>();

    /**
     * Metodo chiamato da FXMLLoader all'inizializzazione del controller.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     *     <li>Tenta di connettersi al servizio RMI {@link #initServices()}.</li>
     *     <li>Configura le colonne della tabella {@link #setupTableColumns()}.</li>
     *     <li>Imposta i listener per il cambio della modalità di ricerca e per il click sulla tabella.</li>
     *     <li>Prepara i dati di fallback per la modalità demo {@link #setupDemoData()}.</li>
     *     <li>Inizializza e avvia l'animazione di sfondo.</li>
     * </ol>
     *
     * @param location  L'URL della risorsa FXML, non utilizzato direttamente.
     * @param resources Il ResourceBundle, non utilizzato direttamente.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initServices();
        logger.debug("Inizializzazione CercaLibriController...");

        setupTableColumns();
        setupToggleListener();
        setupDemoData();
        setupTableClickHandler();

        updateVisibleInputFields(); // Imposta la visibilità iniziale corretta
        if (messageLabel != null) {
            messageLabel.setText("Seleziona una modalità e inizia la ricerca");
        }

        // Avvia l\'animazione dello sfondo
        if (backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();

            // Assicurati di fermare l\'animazione quando la finestra si chiude
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
        logger.info("CercaLibriController inizializzato correttamente.");
    }

    /**
     * Tenta di connettersi al servizio RMI {@link CercaLibriService}.
     * Se la connessione fallisce, logga l'errore, mostra un avviso all'utente
     * e imposta {@code cercaLibriService} a {@code null}, facendo funzionare l'applicazione in modalità demo.
     */
    private void initServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost"); // Assicurati che host e porta siano corretti
            cercaLibriService = (CercaLibriService) registry.lookup("CercaLibriService");
            logger.info("Connessione a CercaLibriService RMI riuscita.");
        } catch (NotBoundException | RemoteException e) {
            logger.error("Impossibile connettersi al servizio RMI CercaLibriService. L\'app funzionerà in modalità demo.", e);
            showAlert(Alert.AlertType.WARNING,
                "Servizio Non Disponibile",
                "Il servizio di ricerca libri non è raggiungibile.",
                "L\'applicazione funzionerà in modalità offline utilizzando dati di esempio.");
            // Non disabilitiamo il pulsante per permettere l\'uso dei dati demo.
            cercaLibriService = null;
        }
    }

    /**
     * Configura le {@code CellValueFactory} per ogni colonna della tabella,
     * collegandole alle proprietà corrispondenti della classe {@link BookRecord}.
     */
    private void setupTableColumns() {
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());
        colTitle.setCellValueFactory(cell -> cell.getValue().titleProperty());
        colAuthor.setCellValueFactory(cell -> cell.getValue().authorProperty());
        colYear.setCellValueFactory(cell -> cell.getValue().yearProperty());
    }

    /**
     * Aggiunge un listener al {@link ToggleGroup} delle modalità di ricerca.
     * Ogni volta che il toggle selezionato cambia, viene chiamato {@link #updateVisibleInputFields()} per aggiornare l'UI.
     */
    private void setupToggleListener() {
        searchModeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                updateVisibleInputFields();
            }
        });
    }

    /**
     * Popola la lista {@code demoData} con alcuni libri di esempio da utilizzare in modalità offline.
     */
    private void setupDemoData() {
        demoData.add(new BookRecord("1", "Il nome della rosa", "Umberto Eco", "1980"));
        demoData.add(new BookRecord("2", "La coscienza di Zeno", "Italo Svevo", "1923"));
        demoData.add(new BookRecord("3", "Se questo è un uomo", "Primo Levi", "1947"));
        demoData.add(new BookRecord("4", "Il fu Mattia Pascal", "Luigi Pirandello", "1904"));
    }

    /**
     * Gestisce la visibilità dei contenitori dei campi di input ({@code idBox}, {@code titleBox}, ecc.)
     * in base al {@link ToggleButton} attualmente selezionato nel {@code searchModeGroup}.
     */
    private void updateVisibleInputFields() {
        Toggle selected = searchModeGroup.getSelectedToggle();

        // Nascondi tutti i pannelli
        idBox.setVisible(false); idBox.setManaged(false);
        titleBox.setVisible(false); titleBox.setManaged(false);
        authorBox.setVisible(false); authorBox.setManaged(false);
        authorYearBox.setVisible(false); authorYearBox.setManaged(false);

        // Mostra solo quello selezionato
        if (selected == tbById) {
            idBox.setVisible(true); idBox.setManaged(true);
        } else if (selected == tbByTitle) {
            titleBox.setVisible(true); titleBox.setManaged(true);
        } else if (selected == tbByAuthor) {
            authorBox.setVisible(true); authorBox.setManaged(true);
        } else if (selected == tbByAuthorYear) {
            authorYearBox.setVisible(true); authorYearBox.setManaged(true);
        }
        showMessage(""); // Pulisce il messaggio di stato
    }

    /**
     * Gestisce l'evento di click sul pulsante di ricerca.
     * <p>
     * Identifica la modalità di ricerca attiva e delega l'esecuzione al metodo specifico
     * (es. {@link #searchById()}). Gestisce le eccezioni RMI e generiche a un livello alto.
     */
    @FXML
    private void onSearch() {
        Toggle selected = searchModeGroup.getSelectedToggle();
        if (selected == null) {
            showMessage("Seleziona una modalità di ricerca.");
            return;
        }

        try {
            if (selected == tbById)           { searchById(); }
            else if (selected == tbByTitle)   { searchByTitle(); }
            else if (selected == tbByAuthor)  { searchByAuthor(); }
            else if (selected == tbByAuthorYear) { searchByAuthorAndYear(); }
        } catch (RemoteException re) {
            logger.error("Errore di comunicazione remota durante la ricerca.", re);
            showAlert(Alert.AlertType.ERROR, "Errore Remoto", "Errore durante la comunicazione con il server.", re.getMessage());
        } catch (Exception e) {
            logger.error("Errore imprevisto durante la ricerca.", e);
            showAlert(Alert.AlertType.ERROR, "Errore Imprevisto", "Si è verificato un errore generico.", e.getMessage());
        }
    }

    // --- LOGICA DI RICERCA SPECIFICA PER MODALITÀ ---

    /**
     * Esegue la ricerca per ID. Valida l'input, chiama il servizio RMI se disponibile,
     * altrimenti usa i dati demo. Aggiorna la tabella con i risultati.
     *
     * @throws RemoteException se si verifica un errore di comunicazione RMI.
     */
    private void searchById() throws RemoteException {
        String idText = safeGet(idField);
        if (idText.isEmpty()) { showMessage("L\'ID non può essere vuoto."); return; }

        int id;
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            showMessage("L\'ID deve essere un numero intero.");
            return;
        }

        if (cercaLibriService != null) {
            Libro libro = cercaLibriService.getTitoloLibroById(id);
            resultsTable.getItems().clear();
            if (libro != null) {
                resultsTable.getItems().add(mapLibroToRecord(libro));
                showMessage("1 risultato trovato.");
            } else {
                showMessage("Nessun libro trovato con ID " + id);
            }
        } else {
            // Fallback Demo
            List<BookRecord> results = demoData.stream()
                .filter(b -> b.getId().equals(idText))
                .collect(Collectors.toList());
            updateTableWithDemoResults(results);
        }
    }

    /**
     * Esegue la ricerca per titolo. Valida l'input, chiama il servizio RMI se disponibile,
     * altrimenti usa i dati demo. Aggiorna la tabella con i risultati.
     *
     * @throws RemoteException se si verifica un errore di comunicazione RMI.
     */
    private void searchByTitle() throws RemoteException {
        String titolo = safeGet(titleField);
        if (titolo.isEmpty()) { showMessage("Il titolo non può essere vuoto."); return; }

        if (cercaLibriService != null) {
            List<Libro> libri = cercaLibriService.cercaLibro_Per_Titolo(titolo);
            List<BookRecord> results = libri.stream()
                .map(this::mapLibroToRecord)
                .collect(Collectors.toList());
            resultsTable.setItems(FXCollections.observableArrayList(results));
            showMessage(String.format("%d risultato/i trovato/i per titolo '%s'.", results.size(), titolo));
        } else {
            // Fallback Demo
            List<BookRecord> results = demoData.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(titolo.toLowerCase()))
                .collect(Collectors.toList());
            updateTableWithDemoResults(results);
        }
    }

    /**
     * Esegue la ricerca per autore. Valida l'input, chiama il servizio RMI se disponibile,
     * altrimenti usa i dati demo. Aggiorna la tabella con i risultati.
     *
     * @throws RemoteException se si verifica un errore di comunicazione RMI.
     */
    private void searchByAuthor() throws RemoteException {
        String autore = safeGet(authorField);
        if (autore.isEmpty()) { showMessage("L\'autore non può essere vuoto."); return; }

        if (cercaLibriService != null) {
            List<Libro> libri = cercaLibriService.cercaLibro_Per_Autore(autore);
            List<BookRecord> results = libri.stream()
                .map(this::mapLibroToRecord)
                .collect(Collectors.toList());
            resultsTable.setItems(FXCollections.observableArrayList(results));
            showMessage(String.format("%d risultato/i trovato/i per autore '%s'.", results.size(), autore));
        } else {
            // Fallback Demo
            List<BookRecord> results = demoData.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(autore.toLowerCase()))
                .collect(Collectors.toList());
            updateTableWithDemoResults(results);
        }
    }

    /**
     * Esegue la ricerca per autore e anno. Valida l'input, chiama il servizio RMI se disponibile,
     * altrimenti usa i dati demo. Aggiorna la tabella con i risultati.
     *
     * @throws RemoteException se si verifica un errore di comunicazione RMI.
     */
    private void searchByAuthorAndYear() throws RemoteException {
        String autore = safeGet(authorYearAuthorField);
        String anno = safeGet(authorYearField);

        if (autore.isEmpty()) { showMessage("L\'autore non può essere vuoto."); return; }
        if (!isValidYear(anno)) { showMessage("Inserisci un anno valido (es. 1984)."); return; }

        if (cercaLibriService != null) {
            List<Libro> libri = cercaLibriService.cercaLibro_Per_Autore_e_Anno(autore, anno);
            List<BookRecord> results = libri.stream()
                .map(this::mapLibroToRecord)
                .collect(Collectors.toList());
            resultsTable.setItems(FXCollections.observableArrayList(results));
            showMessage(String.format("%d risultato/i trovato/i per autore '%s' e anno '%s'.", results.size(), autore, anno));
        } else {
            // Fallback Demo
            List<BookRecord> results = demoData.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(autore.toLowerCase()) && b.getYear().equals(anno))
                .collect(Collectors.toList());
            updateTableWithDemoResults(results);
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Pulisci", svuotando tutti i campi di input e la tabella dei risultati.
     */
    @FXML
    private void onClear() {
        idField.clear();
        titleField.clear();
        authorField.clear();
        authorYearAuthorField.clear();
        authorYearField.clear();
        resultsTable.getItems().clear();
        showMessage("");
    }

    /**
     * Gestisce l'evento di click sul pulsante "Indietro".
     * Ferma l'animazione e naviga alla schermata precedente (Area Privata se loggato,
     * altrimenti Benvenuto) tramite {@link ViewsController}.
     */
    @FXML
    private void onBack() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        try {
            if (bookrecommender.utenti.GestoreSessione.getInstance().isLoggedIn()) {
                ViewsController.mostraAreaPrivata();
            } else {
                ViewsController.mostraBenvenuti();
            }
        } catch (Exception e) {
            logger.error("Impossibile tornare alla schermata precedente.", e);
            showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la schermata precedente.", e.getMessage());
        }
    }

    // --- METODI HELPER E UTILITY ---

    /**
     * Imposta un gestore di eventi per il doppio click sulla tabella dei risultati.
     * Se un utente fa doppio click su una riga, tenta di recuperare i dettagli completi
     * del libro e naviga alla vista dei dettagli.
     */
    private void setupTableClickHandler() {
        resultsTable.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) { // Doppio click
                BookRecord selectedRecord = resultsTable.getSelectionModel().getSelectedItem();
                if (selectedRecord != null) {
                    try {
                        int bookId = Integer.parseInt(selectedRecord.getId());
                        if (cercaLibriService != null) {
                            Libro libro = cercaLibriService.getTitoloLibroById(bookId);
                            if (libro != null) {
                                if (particleAnimation != null) {
                                    particleAnimation.stop();
                                }
                                ViewsController.mostraDettagliLibro(libro, ViewsController.Provenienza.CERCA_LIBRI);
                            } else {
                                showAlert(Alert.AlertType.INFORMATION, "Informazione", "Libro non trovato", "Impossibile recuperare i dettagli per il libro selezionato.");
                            }
                        } else {
                            showAlert(Alert.AlertType.WARNING, "Servizio non disponibile", "La funzione di dettaglio non è attiva in modalità demo.", "");
                        }
                    } catch (NumberFormatException e) {
                        logger.error("ID del libro non valido: " + selectedRecord.getId(), e);
                        showAlert(Alert.AlertType.ERROR, "Errore", "ID del libro non valido.", "");
                    } catch (RemoteException e) {
                        logger.error("Errore di comunicazione RMI", e);
                        showAlert(Alert.AlertType.ERROR, "Errore di Comunicazione", "Impossibile comunicare con il server.", e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Aggiorna la tabella dei risultati e il messaggio di stato quando si usano i dati demo.
     * @param results La lista di {@link BookRecord} da mostrare.
     */
    private void updateTableWithDemoResults(List<BookRecord> results) {
        resultsTable.setItems(FXCollections.observableArrayList(results));
        showMessage(String.format("%d risultato/i trovato/i (modalità demo).", results.size()));
    }
    
    /**
     * Converte un oggetto {@link Libro} (dal servizio RMI) in un {@link BookRecord} (per la tabella).
     * Gestisce i valori nulli impostando "N/D" (Non Disponibile).
     * @param libro L'oggetto Libro da mappare.
     * @return Il BookRecord corrispondente, o {@code null} se l'input è {@code null}.
     */
    private BookRecord mapLibroToRecord(Libro libro) {
        if (libro == null) return null;
        String id = String.valueOf(libro.id());
        String titolo = libro.titolo() != null ? libro.titolo() : "N/D";
        String autore = libro.autori() != null ? libro.autori() : "N/D";
        String anno = libro.anno() != null ? libro.anno() : "N/D";
        return new BookRecord(id, titolo, autore, anno);
    }

    /**
     * Ottiene in modo sicuro il testo da un {@link TextField}, gestendo i casi null e trimmando gli spazi.
     * @param tf Il TextField da cui leggere.
     * @return Il testo trimmato, o una stringa vuota se il campo è nullo o vuoto.
     */
    private String safeGet(TextField tf) {
        return tf != null && tf.getText() != null ? tf.getText().trim() : "";
    }

    /**
     * Mostra un messaggio di stato nell'etichetta dedicata.
     * @param msg Il messaggio da visualizzare.
     */
    private void showMessage(String msg) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
        }
    }
    
    /**
     * Mostra un dialogo di alert standard.
     *
     * @param type    Il tipo di alert (es. {@code Alert.AlertType.ERROR}).
     * @param title   Il titolo della finestra di dialogo.
     * @param header  Il testo dell'intestazione.
     * @param content Il messaggio principale.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Valida se una stringa rappresenta un anno di 4 cifre.
     * @param s La stringa da validare.
     * @return {@code true} se la stringa è un anno valido, {@code false} altrimenti.
     */
    private boolean isValidYear(String s) {
        return s != null && s.matches("\\d{4}");
    }

    // --- CLASSE INTERNA PER LA TABELLA (JavaFX Bean) ---

    /**
     * Classe interna che modella un record di libro per l'uso nella {@link TableView}.
     * <p>
     * Utilizza le proprietà di JavaFX (es. {@link SimpleStringProperty}) per permettere il data binding con le colonne della tabella.
     */
    public static class BookRecord {
        private final SimpleStringProperty id;
        private final SimpleStringProperty title;
        private final SimpleStringProperty author;
        private final SimpleStringProperty year;

        public BookRecord(String id, String title, String author, String year) {
            this.id = new SimpleStringProperty(id);
            this.title = new SimpleStringProperty(title);
            this.author = new SimpleStringProperty(author);
            this.year = new SimpleStringProperty(year);
        }

        /** @return L'ID del libro come stringa. */
        public String getId() { return id.get(); }
        /** @return Il titolo del libro. */
        public String getTitle() { return title.get(); }
        /** @return L'autore del libro. */
        public String getAuthor() { return author.get(); }
        /** @return L'anno di pubblicazione del libro. */
        public String getYear() { return year.get(); }

        /** @return La proprietà stringa dell'ID per il binding. */
        public StringProperty idProperty() { return id; }
        /** @return La proprietà stringa del titolo per il binding. */
        public StringProperty titleProperty() { return title; }
        /** @return La proprietà stringa dell'autore per il binding. */
        public StringProperty authorProperty() { return author; }
        /** @return La proprietà stringa dell'anno per il binding. */
        public StringProperty yearProperty() { return year; }
    }
    

}
