package bookrecommender.libri;

import bookrecommender.condivisi.valutazioni.ValutazioneService;
import bookrecommender.condivisi.consigli.ConsigliService;
import bookrecommender.condivisi.consigli.LibroConsigliato;
import bookrecommender.condivisi.libri.Libro;
import bookrecommender.utenti.GestoreSessione;
import bookrecommender.utili.ParticleAnimation;
import bookrecommender.utili.ViewsController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;

import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller per la schermata di dettaglio di un libro (dettagliLibro-view.fxml).
 * <p>
 * Questa classe è responsabile della visualizzazione di tutte le informazioni
 * relative a un singolo libro. Le sue funzionalità principali includono:
 * <ul>
 *     <li>Visualizzare i metadati del libro (titolo, autore, anno, editore, etc.).</li>
 *     <li>Recuperare e mostrare la media delle valutazioni degli utenti, sia generale
 *         che per criteri specifici (stile, contenuto, etc.), rappresentandole
 *         visivamente con delle stelle.</li>
 *     <li>Recuperare e visualizzare una lista di libri consigliati da altri utenti
 *         che hanno letto il libro corrente, con un conteggio di quante volte
 *         ciascun libro è stato suggerito.</li>
 *     <li>Gestire la navigazione, permettendo all'utente di tornare alla schermata
 *         precedente (es. ricerca libri o librerie) o di visualizzare i dettagli
 *         di un libro consigliato.</li>
 *     <li>Controllare un'animazione di sfondo tramite {@link ParticleAnimation}.</li>
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
public class DettagliLibroController implements Initializable {

    @FXML private Label titoloLabel;
    @FXML private Label autoriLabel;
    @FXML private Label idLabel;
    @FXML private Label annoLabel;
    @FXML private Label editoreLabel;
    @FXML private Label categorieLabel;
    @FXML private Label descrizioneLabel;
    @FXML private Label prezzoLabel;
    @FXML private Canvas backgroundCanvas;

    @FXML private TableView<LibroConsigliatoRecord> suggerimentiTable;
    @FXML private TableColumn<LibroConsigliatoRecord, String> titoloSuggeritoColumn;
    @FXML private TableColumn<LibroConsigliatoRecord, String> idSuggeritoColumn;
    @FXML private TableColumn<LibroConsigliatoRecord, Number> conteggioSuggeritoColumn;

    /** Gestore dell'animazione di sfondo. */
    private ParticleAnimation particleAnimation;
    /** Traccia la vista di provenienza per gestire correttamente il pulsante "Indietro". */
    private ViewsController.Provenienza provenienza;
    /** Servizio RMI per recuperare i consigli. */
    private ConsigliService consigliService;
    /** Il libro attualmente visualizzato in questa schermata. */
    private Libro libroCorrente;
    /** Servizio RMI per recuperare le valutazioni. */
    private ValutazioneService valutazioneService;


     // Campi per la media delle valutazioni
    @FXML private Label mediaStella1, mediaStella2, mediaStella3, mediaStella4, mediaStella5;
    @FXML private Label lblMediaUtenti, lblNumeroValutazioni;

    // Campi per le valutazioni dettagliate per criterio
    @FXML private Label stileStella1, stileStella2, stileStella3, stileStella4, stileStella5;
    @FXML private Label contenutoStella1, contenutoStella2, contenutoStella3, contenutoStella4, contenutoStella5;
    @FXML private Label gradevolezzaStella1, gradevolezzaStella2, gradevolezzaStella3, gradevolezzaStella4, gradevolezzaStella5;
    @FXML private Label originalitaStella1, originalitaStella2, originalitaStella3, originalitaStella4, originalitaStella5;
    @FXML private Label edizioneStella1, edizioneStella2, edizioneStella3, edizioneStella4, edizioneStella5;
    @FXML private Label lblMediaStile, lblMediaContenuto, lblMediaGradevolezza, lblMediaOriginalita, lblMediaEdizione;

    /**
     * Metodo chiamato da FXMLLoader all'inizializzazione del controller.
     * <p>
     * Esegue le seguenti operazioni:
     * <ol>
     *     <li>Inizializza i servizi RMI {@link #initServices()}.</li>
     *     <li>Configura le colonne della tabella dei suggerimenti {@link #setupTableColumns()}.</li>
     *     <li>Imposta il gestore di eventi per il doppio click sulla tabella {@link #setupTableClickHandler()}.</li>
     *     <li>Inizializza e avvia l'animazione di sfondo, gestendone la terminazione alla chiusura della finestra.</li>
     * </ol>
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initServices();
        setupTableColumns();
        setupTableClickHandler();

        if (backgroundCanvas != null) {
            particleAnimation = new ParticleAnimation(backgroundCanvas);
            particleAnimation.start();
            backgroundCanvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                        if (newWindow != null) {
                            newWindow.setOnCloseRequest(event -> {
                                if (particleAnimation != null) {
                                    particleAnimation.stop();
                                }
                            });
                        }
                    });
                }
            });
        }
    }

    /**
     * Tenta di connettersi ai servizi RMI necessari (ConsigliService e ValutazioneService).
     * In caso di fallimento, mostra un alert di errore e imposta i servizi a {@code null},
     * degradando le funzionalità della vista.
     */
    private void initServices() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost");
            consigliService = (ConsigliService) registry.lookup(ConsigliService.NAME);
             valutazioneService = (ValutazioneService) registry.lookup("ValutazioneService");
        } catch (NotBoundException | RemoteException e) {
            showAlert(Alert.AlertType.ERROR, "Errore di Connessione", "Impossibile connettersi al servizio di consigli.", e.getMessage());
            consigliService = null;
        }
    }

    /**
     * Configura le {@code CellValueFactory} per ogni colonna della tabella dei suggerimenti,
     * collegandole alle proprietà corrispondenti della classe interna {@link LibroConsigliatoRecord}.
     */
    private void setupTableColumns() {
        titoloSuggeritoColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitolo()));
        idSuggeritoColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getId()));
        conteggioSuggeritoColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getNumeroConsigli()));
    }

    /**
     * Imposta un gestore di eventi per il doppio click sulla tabella dei suggerimenti.
     * Se un utente fa doppio click su una riga, naviga alla vista dei dettagli del libro suggerito.
     */
    private void setupTableClickHandler() {
        suggerimentiTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LibroConsigliatoRecord selectedRecord = suggerimentiTable.getSelectionModel().getSelectedItem();
                if (selectedRecord != null) {
                    try {
                        if (particleAnimation != null) {
                            particleAnimation.stop();
                        }
                        ViewsController.mostraDettagliLibro(selectedRecord.getLibro(), provenienza);
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Errore di Navigazione", "Impossibile caricare la pagina del libro.", e.getMessage());
                    }
                }
            }
        });
    }

    /**
     * Imposta il libro da visualizzare e popola l'interfaccia con i suoi dati.
     * <p>
     * Questo metodo è il punto di ingresso per i dati in questo controller. Una volta ricevuto
     * l'oggetto {@link Libro}, aggiorna tutte le etichette e avvia il caricamento asincrono
     * dei suggerimenti e delle valutazioni.
     * @param libro L'oggetto {@link Libro} da visualizzare. Non deve essere nullo.
     */
    public void setLibro(Libro libro) {
        this.libroCorrente = libro;
        if (libro != null) {
            titoloLabel.setText(libro.titolo() != null ? libro.titolo() : "N/D");
            autoriLabel.setText(libro.autori() != null ? libro.autori() : "N/D");
            idLabel.setText(libro.id() != null ? "#" + libro.id().toString() : "N/D");
            annoLabel.setText(libro.anno() != null ? libro.anno() : "N/D");
            editoreLabel.setText(libro.editore() != null ? libro.editore() : "N/D");
            categorieLabel.setText(libro.categorie() != null ? libro.categorie() : "N/D");
            descrizioneLabel.setText(libro.descrizione() != null ? libro.descrizione() : "N/D");
            prezzoLabel.setText(libro.prezzo() != null ? "€ " + libro.prezzo() : "N/D");

            caricaSuggerimenti();
            caricaMediaValutazioni();
        }
    }

    /**
     * Carica i suggerimenti per il libro corrente dal servizio RMI e popola la tabella.
     * Se il servizio non è disponibile o si verifica un errore, mostra un alert.
     * Le operazioni di rete vengono eseguite sul thread dell'interfaccia utente.
     */
    private void caricaSuggerimenti() {
        if (consigliService != null && libroCorrente != null) {
            try {
                List<LibroConsigliato> suggerimenti = consigliService.getConsigliatiConConteggio(libroCorrente.id());

                List<LibroConsigliatoRecord> records = suggerimenti.stream()
                    .map(LibroConsigliatoRecord::new)
                    .collect(Collectors.toList());

                suggerimentiTable.setItems(FXCollections.observableArrayList(records));

            } catch (RemoteException e) {
                showAlert(Alert.AlertType.ERROR, "Errore di Caricamento", "Impossibile caricare i suggerimenti.", e.getMessage());
            }
        }
    }


    
    /**
     * Carica e visualizza la media delle valutazioni di tutti gli utenti per il libro corrente.
     * Recupera la media generale, il numero di valutazioni e le medie per ogni singolo criterio,
     * quindi aggiorna la UI di conseguenza.
     */
    private void caricaMediaValutazioni() {
        if (libroCorrente == null || valutazioneService == null) {
            return;
        }
        
        try {
            // Calcola la media generale
            double media = valutazioneService.calcolaMediaValutazioni(libroCorrente.id().intValue());
            int numeroValutazioni = valutazioneService.getNumeroValutazioni(libroCorrente.id().intValue());
            
            // Aggiorna i valori numerici generali
            lblMediaUtenti.setText(String.format("%.1f", media));
            lblNumeroValutazioni.setText(String.format("(%d valutazioni)", numeroValutazioni));
            
            // Aggiorna le stelle generali
            aggiornaStelleMedia(media);
            
            // Calcola e mostra le medie separate per ogni criterio
            caricaMediePerCriterio();
            
        } catch (Exception e) {
            e.printStackTrace();
            // In caso di errore, mostra valori di default
            lblMediaUtenti.setText("0.0");
            lblNumeroValutazioni.setText("(0 valutazioni)");
            aggiornaStelleMedia(0.0);
            aggiornaMediePerCriterio(0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }
    
    /**
     * Recupera dal servizio RMI le medie di valutazione per ogni criterio specifico
     * (stile, contenuto, etc.) e le passa al metodo che aggiorna la UI.
     * Gestisce gli errori mostrando valori di default.
     */
    private void caricaMediePerCriterio() {
        try {
            double mediaStile = valutazioneService.calcolaMediaStile(libroCorrente.id().intValue());
            double mediaContenuto = valutazioneService.calcolaMediaContenuto(libroCorrente.id().intValue());
            double mediaGradevolezza = valutazioneService.calcolaMediaGradevolezza(libroCorrente.id().intValue());
            double mediaOriginalita = valutazioneService.calcolaMediaOriginalita(libroCorrente.id().intValue());
            double mediaEdizione = valutazioneService.calcolaMediaEdizione(libroCorrente.id().intValue());
            
            aggiornaMediePerCriterio(mediaStile, mediaContenuto, mediaGradevolezza, mediaOriginalita, mediaEdizione);
            
        } catch (Exception e) {
            e.printStackTrace();
            // In caso di errore, mostra valori di default
            aggiornaMediePerCriterio(0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }
    
    /**
     * Aggiorna la UI con i valori delle medie per ogni criterio.
     * Imposta il testo delle etichette numeriche e chiama {@link #aggiornaStelleCriterio}
     * per aggiornare la rappresentazione visuale a stelle.
     * @param mediaStile Media per il criterio "stile".
     * @param mediaContenuto Media per il criterio "contenuto".
     * @param mediaGradevolezza Media per il criterio "gradevolezza".
     * @param mediaOriginalita Media per il criterio "originalità".
     * @param mediaEdizione Media per il criterio "edizione".
     */
    private void aggiornaMediePerCriterio(double mediaStile, double mediaContenuto, double mediaGradevolezza, 
                                         double mediaOriginalita, double mediaEdizione) {
        // Aggiorna i valori numerici
        lblMediaStile.setText(String.format("%.1f", mediaStile));
        lblMediaContenuto.setText(String.format("%.1f", mediaContenuto));
        lblMediaGradevolezza.setText(String.format("%.1f", mediaGradevolezza));
        lblMediaOriginalita.setText(String.format("%.1f", mediaOriginalita));
        lblMediaEdizione.setText(String.format("%.1f", mediaEdizione));
        
        // Aggiorna le stelle per ogni criterio
        aggiornaStelleCriterio(stileStella1, stileStella2, stileStella3, stileStella4, stileStella5, mediaStile);
        aggiornaStelleCriterio(contenutoStella1, contenutoStella2, contenutoStella3, contenutoStella4, contenutoStella5, mediaContenuto);
        aggiornaStelleCriterio(gradevolezzaStella1, gradevolezzaStella2, gradevolezzaStella3, gradevolezzaStella4, gradevolezzaStella5, mediaGradevolezza);
        aggiornaStelleCriterio(originalitaStella1, originalitaStella2, originalitaStella3, originalitaStella4, originalitaStella5, mediaOriginalita);
        aggiornaStelleCriterio(edizioneStella1, edizioneStella2, edizioneStella3, edizioneStella4, edizioneStella5, mediaEdizione);
    }
    
    /**
     * Aggiorna la visualizzazione delle stelle per un dato criterio in base a un valore di media.
     * Arrotonda il valore al 0.5 più vicino per decidere se una stella debba essere piena o vuota.
     * @param stella1 La prima etichetta-stella.
     * @param media Il valore medio (da 0 a 5) da rappresentare.
     */
    private void aggiornaStelleCriterio(Label stella1, Label stella2, Label stella3, Label stella4, Label stella5, double media) {
        Label[] stelle = {stella1, stella2, stella3, stella4, stella5};
        
        for (int i = 0; i < stelle.length; i++) {
            // Arrotonda al 0.5 più vicino per decidere se la stella è piena o vuota
            // es. 3.7 >= 3.5 (stella 4) -> piena; 3.2 < 3.5 (stella 4) -> vuota
            if (media >= (i + 0.5)) {
                stelle[i].setText("★"); // Stella piena
            } else {
                stelle[i].setText("☆"); // Stella vuota
            }
        }
    }
    
    /**
     * Aggiorna la visualizzazione delle stelle per la media generale, riutilizzando la logica
     * per i criteri specifici.
     */
    private void aggiornaStelleMedia(double media) {
        aggiornaStelleCriterio(mediaStella1, mediaStella2, mediaStella3, mediaStella4, mediaStella5, media);
    }

    /**
     * Imposta la vista di provenienza per il controller.
     * Questo è essenziale per il corretto funzionamento del pulsante "Indietro".
     * @param provenienza L'enum che indica da quale schermata si è arrivati.
     */
    public void setProvenienza(ViewsController.Provenienza provenienza) {
        this.provenienza = provenienza;
    }


    /**
     * Gestisce l'evento di click sul pulsante "Indietro".
     * Ferma l'animazione e naviga alla schermata precedente usando {@link ViewsController}.
     */
    @FXML
    private void onBack() {
        if (particleAnimation != null) {
            particleAnimation.stop();
        }
        try {
            if (provenienza == ViewsController.Provenienza.LIBRERIE) {
                ViewsController.mostraLibrerie();
            } else {
                ViewsController.mostraCercaLibri();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gestisce l'evento di click sul pulsante "Visualizza Tutte le Recensioni".
     * Naviga alla schermata che mostra l'elenco completo delle recensioni per il libro corrente.
     */
    @FXML
    private void onVisualizzaTutteLeRecensioni() {
        if (libroCorrente != null) {
            if (particleAnimation != null) {
                particleAnimation.stop();
            }
            ViewsController.mostraTutteLeValutazioni(libroCorrente);
        }
    }

    /**
     * Metodo di utilità per mostrare un {@link Alert} all'utente.
     * @param type il tipo di alert (es. INFORMATION, ERROR).
     * @param title il titolo della finestra di dialogo.
     * @param header l'intestazione del messaggio.
     * @param content il contenuto del messaggio.
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    

    /**
     * Classe interna che modella un record di libro consigliato per l'uso nella {@link TableView}.
     * <p>
     * Incapsula un oggetto {@link LibroConsigliato} e fornisce metodi getter semplici per il data binding.
     */
    public static class LibroConsigliatoRecord {
        private final Libro libro;
        private final int numeroConsigli;

        public LibroConsigliatoRecord(LibroConsigliato libroConsigliato) {
            this.libro = libroConsigliato.libro();
            this.numeroConsigli = libroConsigliato.numeroConsigli();
        }

        /** @return L'oggetto {@link Libro} completo del libro consigliato. */
        public Libro getLibro() {
            return libro;
        }

        /** @return Il titolo del libro consigliato. */
        public String getTitolo() {
            return libro.titolo();
        }

        /** @return L'ID del libro consigliato come stringa. */
        public String getId() {
            return libro.id().toString();
        }

        /** @return Il numero di volte che questo libro è stato consigliato. */
        public int getNumeroConsigli() {
            return numeroConsigli;
        }
    }
}
