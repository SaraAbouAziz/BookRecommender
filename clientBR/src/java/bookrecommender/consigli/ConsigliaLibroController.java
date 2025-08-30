package bookrecommender.consigli;

import bookrecommender.utili.ParticleAnimation;
import bookrecommender.librerie.*;
import bookrecommender.BookRecommender;
import bookrecommender.condivisi.consigli.ConsigliService;
import bookrecommender.condivisi.libri.CercaLibriService;
import bookrecommender.condivisi.libri.Libro;
import bookrecommender.utenti.GestoreSessione;
import bookrecommender.utili.ParticleAnimation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import bookrecommender.utili.ViewsController;

/**
 * Controller JavaFX che gestisce la logica di interazione per la finestra di "consiglia un libro".
 * <p>
 * Responsabilità principali:
 * <ul>
 *   <li>Connessione ai servizi remoti {@code CercaLibriService} e {@code ConsigliService} via RMI;</li>
 *   <li>ricerca di libri per titolo o ID e visualizzazione dei risultati;</li>
 *   <li>selezione di fino a {@code MAX_CONSIGLI} libri da proporre come consigli;</li>
 *   <li>raccolta di un commento opzionale e invio dei consigli al backend;</li>
 *   <li>gestione delle interazioni UI e della loro sincronizzazione con il thread JavaFX.</li>
 * </ul>
 *
 * Questo controller è progettato per essere utilizzato esclusivamente sul thread JavaFX; le chiamate
 * che modificano la UI devono essere eseguite tramite {@link Platform#runLater(Runnable)} quando
 * originate da thread esterni. L'inizializzazione stabilisce una lookup RMI verso localhost:1099 e,
 * in caso di insuccesso, mostra un alert e chiude la finestra.
 *
 * <p><b>Note architetturali:</b> il controller delega la persistenza e la ricerca al backend remoto e
 * applica validazioni di business (es. nessun consiglio duplicato, non consigliare lo stesso libro,
 * massimo tre consigli) prima di inviare le richieste di persistenza.</p>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Kahri Mohamed Ameur 754773
 * @author Zoghbani Lilia 759652
 * @version 1.0
 * @since 1.0
 */
public class ConsigliaLibroController {

    /**
     * Il pannello radice della vista, utilizzato per il contesto della UI.
     */
    @FXML
    private StackPane rootPane;
    /**
     * Il canvas utilizzato per visualizzare l'animazione di particelle in background.
     */
    @FXML
    private Canvas backgroundCanvas;
    /**
     * Il pulsante per annullare l'operazione e tornare alla vista precedente.
     * Nota: questo campo è iniettato ma potrebbe non essere utilizzato direttamente se l'azione è gestita da `handleCancel`.
     */
    @FXML
    private Button backButton;
    /**
     * L'etichetta che mostra il titolo del libro per cui si stanno fornendo i consigli.
     */
    @FXML
    private Label libroDaConsigliareLabel;
    /**
     * Etichetta per visualizzare messaggi di stato o di errore.
     * Nota: attualmente non utilizzata, i messaggi vengono mostrati tramite dialoghi di alert.
     */
    @FXML
    private Label messageLabel;
    /**
     * Il campo di testo in cui l'utente inserisce un titolo o un ID per cercare libri da consigliare.
     */
    @FXML
    private TextField searchField;
    /**
     * La ListView che visualizza i risultati della ricerca dei libri.
     */
    @FXML
    private ListView<Libro> searchResultsView;
    /**
     * La ListView che mostra i libri che l'utente ha selezionato per il consiglio.
     */
    @FXML
    private ListView<Libro> selectedBooksView;
    /**
     * L'area di testo in cui l'utente può inserire un commento opzionale per i suoi consigli.
     */
    @FXML
    private TextArea commentArea;
    /**
     * Il pulsante che l'utente preme per inviare i consigli selezionati.
     */
    @FXML
    private Button suggestButton;

    /**
     * Il servizio RMI per la ricerca dei libri.
     */
    private CercaLibriService cercaLibriService;
    /**
     * Il servizio RMI per l'invio dei consigli.
     */
    private ConsigliService consigliService;
    /**
     * Il gestore della sessione utente per ottenere l'ID dell'utente corrente.
     */
    private GestoreSessione gestoreSessione;
    /**
     * L'oggetto che gestisce l'animazione delle particelle in background.
     */
    private ParticleAnimation particleAnimation;

    /**
     * Il libro letto dall'utente, per il quale si stanno creando i consigli.
     */
    private Libro libroLetto;
    /**
     * L'ID della libreria a cui il consiglio è associato.
     */
    private Integer libreriaId;

    /**
     * Il numero massimo di libri che possono essere consigliati in una singola operazione.
     */
    private final int MAX_CONSIGLI = 3;
    /**
     * La lista osservabile dei libri selezionati per il consiglio, collegata alla {@code selectedBooksView}.
     */
    private final ObservableList<Libro> selectedBooks = FXCollections.observableArrayList();

    /**
     * Inizializza il controller.
     *
     * <p>Operazioni eseguite:
     * <ol>
     *   <li>Recupero dell'istanza di {@code GestoreSessione} per accedere all'utente corrente;</li>
     *   <li>associazione della lista osservabile {@code selectedBooks} alla {@code selectedBooksView};</li>
     *   <li>tentativo di lookup RMI per {@code CercaLibriService} e {@code ConsigliService} su
     *       <code>localhost:1099</code>. In caso di fallimento viene mostrato un alert e la finestra viene chiusa;</li>
     *   <li>configurazione dei listener e delle celle di visualizzazione tramite {@link #setupListeners()}.</li>
     * </ol>
     *
     * Thread-safety: this method è destinato ad essere invocato dal thread JavaFX (ad es. dall'FXML loader).
     *
     * @implNote il metodo cattura eccezioni generiche durante il lookup RMI; eventuali errori
     *           vengono notificati all'utente mediante alert e determinano la chiusura della finestra.
     * @see #setupListeners()
     */
    @FXML
    public void initialize() {
        gestoreSessione = GestoreSessione.getInstance();
        selectedBooksView.setItems(selectedBooks);

        // Initialize ParticleAnimation
        if (rootPane != null && backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();
        }

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            cercaLibriService = (CercaLibriService) registry.lookup(CercaLibriService.NAME);
            consigliService = (ConsigliService) registry.lookup(ConsigliService.NAME);
        } catch (Exception e) {
            showError("Errore di Connessione", "Impossibile connettersi ai servizi RMI. Assicurarsi che il server sia in esecuzione.");
            Platform.runLater(() -> ViewsController.mostraLibrerie());
        }

        setupListeners();
    }

    /**
     * Configura i listener UI e i factory delle celle per le {@code ListView}.
     *
     * <p>Comportamento dettagliato:
     * <ul>
     *   <li>Il campo di ricerca ascolta le modifiche testuali e invoca {@link #searchBooks(String)}
     *       quando l'input non è vuoto;</li>
     *   <li>la {@code searchResultsView} mostra titolo e autori e aggiunge un libro selezionato
     *       alla selezione corrente (tramite double-click o selection change gestito con
     *       {@link Platform#runLater(Runnable)} per garantire l'esecuzione sul thread JavaFX);
     *   <li>la {@code selectedBooksView} mostra solo il titolo ed espone la rimozione con doppio click.</li>
     * </ul>
     * <p>
     * Side-effects: modifica diretta delle {@code ListView} e di {@code selectedBooks}.
     */
    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null || newText.trim().isEmpty()) {
                searchResultsView.getItems().clear();
                return;
            }
            searchBooks(newText.trim());
        });

        searchResultsView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Libro item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.titolo() + " (" + item.autori() + ")");
                }
            }
        });

        searchResultsView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                Platform.runLater(() -> addBookToSelection(newSelection));
            }
        });

        selectedBooksView.setCellFactory(lv -> new ListCell<>() {
             @Override
            protected void updateItem(Libro item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.titolo());
                }
            }
        });

        selectedBooksView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Libro selected = selectedBooksView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectedBooks.remove(selected);
                }
            }
        });
    }

    /**
     * Imposta i dati contestuali necessari al controller prima della visualizzazione.
     *
     * @param libroLetto istanza del libro che ha generato il flusso di suggerimenti (non nullo).
     * @param libreriaId l'identificativo della libreria in cui verranno inviati i consigli.
     *
     * Effetto collaterale: aggiorna il testo visibile {@code libroDaConsigliareLabel} con il titolo
     * del libro fornito.
     */
    public void setData(Libro libroLetto, int libreriaId) {
        this.libroLetto = libroLetto;
        this.libreriaId = libreriaId;
        if (libroDaConsigliareLabel != null) {
            libroDaConsigliareLabel.setText("Suggerisci libri per: " + libroLetto.titolo());
        }
    }

    /**
     * Esegue la ricerca dei libri utilizzando il servizio remoto {@code CercaLibriService}.
     *
     * <p>Logica:
     * <ol>
     *   <li>Se la query può essere convertita in {@link Long} viene invocata la ricerca per ID
     *       {@code cercaLibro_Per_Id};</li>
     *   <li>altrimenti viene effettuata la ricerca per titolo {@code cercaLibro_Per_Titolo}.</li>
     * </ol>
     *
     * Il risultato viene mappato in una {@link javafx.collections.ObservableList} e assegnato a
     * {@code searchResultsView}.
     *
     * @param query la stringa di ricerca. Non deve essere null; se vuota, la view dei risultati viene svuotata.
     *
     * Gestione degli errori: eventuali eccezioni (ad es. problemi di rete o RMI) vengono catturate
     * e l'utente viene informato tramite {@link #showError(String, String)}.
     */
    private void searchBooks(String query) {
        try {
            List<Libro> results;
            try {
                Long id = Long.parseLong(query);
                results = cercaLibriService.cercaLibro_Per_Id(id);
            } catch (NumberFormatException e) {
                results = cercaLibriService.cercaLibro_Per_Titolo(query);
            }
            searchResultsView.setItems(FXCollections.observableArrayList(results));
        } catch (Exception e) {
            showError("Errore di Ricerca", "Si è verificato un errore durante la ricerca dei libri.");
        }
    }

    /**
     * Aggiunge un libro alla lista dei libri selezionati rispettando le regole di business.
     *
     * <p>Validazioni applicate in ordine:
     * <ol>
     *   <li>non superare {@code MAX_CONSIGLI};</li>
     *   <li>non consentire la selezione del medesimo libro passato come {@code libroLetto};</li>
     *   <li>evitare duplicati nella selezione corrente.</li>
     * </ol>
     * <p>
     * Effetti collaterali: modifica la collezione osservabile {@code selectedBooks} e pulisce la
     * selezione della {@code searchResultsView}.
     *
     * @param book l'istanza di {@link Libro} da aggiungere; se null il metodo non ha effetto.
     */
    private void addBookToSelection(Libro book) {
        if (selectedBooks.size() >= MAX_CONSIGLI) {
            showInfo("Limite Raggiunto", "Puoi consigliare al massimo " + MAX_CONSIGLI + " libri.");
            return;
        }
        if (book.id().equals(libroLetto.id())) {
            showInfo("Azione non permessa", "Non puoi consigliare lo stesso libro.");
            return;
        }
        if (!selectedBooks.contains(book)) {
            selectedBooks.add(book);
        }
        searchResultsView.getSelectionModel().clearSelection();
    }

    /**
     * Gestisce l'azione di invio dei consigli.
     *
     * Comportamento:
     * <ul>
     *   <li>recupera l'ID utente corrente tramite {@code GestoreSessione};</li>
     *   <li>verifica che sia stato selezionato almeno un libro;</li>
     *   <li>disabilita temporaneamente il pulsante {@code suggestButton} per evitare invii duplicati;</li>
     *   <li>per ogni libro selezionato chiama {@code consigliService.aggiungiConsiglio(...)} passando
     *       userId, libreriaId, id del libro letto, id del libro consigliato e commento;</li>
     *   <li>in caso di successo mostra un messaggio informativo e chiude la finestra; in caso di errore
     *       mostra un alert e riabilita il pulsante.</li>
     * </ul>
     * <p>
     * Side-effects: invoca il servizio remoto {@code ConsigliService} che persiste i consigli.
     *
     * @see ConsigliService#aggiungiConsiglio(String, int, long, long, String)
     */
    @FXML
    private void handleSuggest() {
        String userId = gestoreSessione.getCurrentUserId();
        String commento = commentArea.getText();

        if (selectedBooks.isEmpty()) {
            showError("Nessun libro selezionato", "Devi selezionare almeno un libro da consigliare.");
            return;
        }

        suggestButton.setDisable(true);

        try {
            for (Libro libroDaConsigliare : selectedBooks) {
                consigliService.aggiungiConsiglio(userId, libreriaId, libroLetto.id(), libroDaConsigliare.id(), commento);
            }
            // Se il ciclo for termina senza eccezioni, l'operazione è riuscita
            showInfo("Successo", "Consigli inviati con successo!");
            ViewsController.mostraLibrerie();

        } catch (Exception e) {
            // Se si verifica un'eccezione, l'operazione è considerata fallita.
            // Cerchiamo di dare un messaggio più specifico e comprensibile all'utente.
            Throwable cause = e;
            // Scendiamo nella catena delle cause per trovare l'eccezione originale
            while (cause.getCause() != null && cause.getCause() != cause) {
                cause = cause.getCause();
            }

            String exceptionMessage = cause.getMessage();
            String userMessage;

            // Traduciamo i messaggi di errore noti in frasi più semplici per l'utente.
            if (exceptionMessage != null) {
                if (exceptionMessage.contains("Limite massimo")) {
                    userMessage = "Hai raggiunto il numero massimo di consigli (3) per questo libro.";
                } else if (exceptionMessage.contains("Non puoi consigliare lo stesso libro")) {
                    userMessage = "Azione non permessa: non puoi consigliare lo stesso libro che hai letto.";
                } else if (exceptionMessage.toLowerCase().contains("duplicate key") || exceptionMessage.toLowerCase().contains("violates unique constraint")) {
                    userMessage = "Uno dei libri selezionati è già stato consigliato in precedenza. Non puoi consigliarlo di nuovo.";
                } else {
                    // Messaggio generico per altri errori del server che non vogliamo mostrare direttamente.
                    userMessage = "Si è verificato un errore durante l'invio dei consigli. Riprova.";
                }
            } else {
                // Messaggio per errori di comunicazione o imprevisti senza un messaggio specifico.
                userMessage = "Si è verificato un errore di comunicazione con il server. Controlla la tua connessione e riprova.";
            }

            // Mostra l'errore e riabilita il pulsante
            showError("Errore nell'invio", userMessage);
            suggestButton.setDisable(false);
            e.printStackTrace(); // Logga lo stack trace completo per il debug
        }
    }

    /**
     * Gestisce l'azione di cancellazione/chiusura senza inviare i consigli.
     * Ferma l'animazione di sfondo e naviga alla vista delle librerie.
     */ 
    @FXML
    private void handleCancel() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        ViewsController.mostraLibrerie();
    }

    

    /**
     * Mostra un dialogo di errore all'utente.
     *
     * @param title   il titolo dell'alert (breve e descrittivo).
     * @param message il testo esplicativo che descrive l'errore e, se utile, suggerimenti per la risoluzione.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Mostra un dialogo informativo all'utente.
     *
     * @param title   il titolo dell'alert (breve e descrittivo).
     * @param message il testo informativo che comunica l'esito di un'operazione.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
