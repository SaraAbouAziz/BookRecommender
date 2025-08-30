package bookrecommender.utili;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bookrecommender.BookRecommender;
import bookrecommender.condivisi.libri.Libro;
import bookrecommender.consigli.ConsigliaLibroController;
import bookrecommender.libri.DettagliLibroController;
import bookrecommender.valutazioni.AllValutazioniController;
import bookrecommender.valutazioni.ValutazioneCompletaController;

import java.io.IOException;
import java.util.Objects;

/**
 * Gestore di navigazione centralizzato per le viste dell'applicazione.
 * <p>
 * Questa classe, implementata come un utility class con metodi statici,
 * orchestra la transizione tra le diverse schermate (viste FXML)
 * dell'applicazione. Il suo scopo principale è gestire una singola {@link Scene}
 * sul {@link Stage} primario, sostituendo il nodo radice ({@code Parent})
 * ogni volta che si cambia vista.
 * <p>
 * <b>Funzionalità chiave:</b>
 * <ul>
 *   <li><b>Efficienza:</b> Riutilizza la stessa istanza di {@code Scene}, migliorando le performance.</li>
 *   <li><b>Stato della Finestra:</b> Preserva lo stato della finestra (massimizzata o a schermo intero)
 *       durante la navigazione.</li>
 *   <li><b>Passaggio Dati:</b> Fornisce metodi per caricare viste passando dati ai loro controller
 *       prima della visualizzazione.</li>
 *   <li><b>Gestione Errori:</b> Mostra una vista di errore se un file FXML non può essere caricato.</li>
 * </ul>
 *
 * <p><b>Utilizzo:</b> Il metodo {@link #initialize(Stage)} deve essere invocato una sola volta
 * all'avvio dell'applicazione (tipicamente nel metodo {@code start} della classe {@code Application}).
 */
public class ViewsController {
    /** Enum per tracciare la provenienza di una navigazione, utile per la logica "indietro". */
    public enum Provenienza { LIBRERIE, CERCA_LIBRI }

    private static Scene primaryScene;
    private static Stage primaryStage;
    private static final Logger logger = LogManager.getLogger(ViewsController.class);

    // memorizzazione dello stato iniziale della stage
    private static boolean initiallyMaximized = false;
    private static boolean initiallyFullScreen = false;

    /**
     * Inizializza il ViewsController con lo Stage principale dell'applicazione.
     * <p>
     * Questo metodo deve essere chiamato una sola volta, all'avvio, per fornire
      * al controller il riferimento allo stage primario. Memorizza anche lo stato
      * iniziale della finestra (massimizzata o a schermo intero) per poterlo
      * ripristinare durante i cambi di vista.
     * @param stage Lo {@link Stage} principale dell'applicazione.
     */
    public static void initialize(Stage stage) {
        primaryStage = stage;
        initiallyMaximized = stage.isMaximized();
        initiallyFullScreen = stage.isFullScreen();
        logger.info("ViewsController inizializzato con stage principale (maximized="
                + initiallyMaximized + ", fullscreen=" + initiallyFullScreen + ")");
    }

    /**
     * Carica un file FXML e lo imposta come vista corrente.
     * <p>
     * Metodo helper che prende il percorso di un file FXML, lo carica usando
     * {@link FXMLLoader} e passa il nodo radice risultante a {@link #applyRoot(Parent)}.
     * In caso di {@link IOException}, mostra una schermata di errore.
     * @param fxmlPath Il percorso della risorsa FXML da caricare.
     */
    private static void setRoot(String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(BookRecommender.class.getResource(fxmlPath)));
            applyRoot(root);
            logger.debug("Cambiata view a: " + fxmlPath);
        } catch (IOException e) {
            logger.error("Errore nel caricamento della view: " + fxmlPath, e);
            showErrorView();
        }
    }

    /**
     * Applica un nuovo nodo radice alla scena primaria.
     * <p>
     * Questo è il metodo centrale che gestisce la visualizzazione.
     * <ul>
     *   <li><b>Alla prima chiamata:</b> Crea una nuova {@link Scene} con il nodo radice,
     *       la imposta sullo stage primario e mostra lo stage. Successivamente, usando
     *       {@link Platform#runLater(Runnable)}, adatta le dimensioni della finestra e
     *       imposta le dimensioni minime per evitare ridimensionamenti anomali.</li>
     *   <li><b>Alle chiamate successive:</b> Sostituisce semplicemente il nodo radice della
     *       scena esistente. Questo approccio è più performante e mantiene stabili le dimensioni
     *       e la posizione della finestra.</li>
     * </ul>
     * Ripristina inoltre lo stato massimizzato o a schermo intero se era attivo all'avvio.
     * @param root Il nodo {@link Parent} da impostare come radice della scena.
     */
    private static void applyRoot(Parent root) {
        if (primaryStage == null) {
            logger.error("PrimaryStage non inizializzato. Chiama ViewsController.initialize(stage) all'avvio.");
            return;
        }

        if (primaryScene == null) {
            // Prima impostazione: creo la Scene e mostro lo stage
            primaryStage.setScene(new Scene(root));
            primaryScene = primaryStage.getScene();
            primaryStage.show();

            // Dopo il primo layout memorizziamo una min size sicura e ripristiniamo lo stato iniziale
            Platform.runLater(() -> {
                // Se non eravamo inizialmente maximized/fullscreen, possiamo adattare la size al contenuto
                if (!initiallyMaximized && !initiallyFullScreen) {
                    try {
                        primaryStage.sizeToScene();
                    } catch (Exception ignored) { }
                    // Impostiamo una min size basata sulla dimensione attuale per evitare riduzioni future
                    double currentW = Math.max(400, primaryStage.getWidth());
                    double currentH = Math.max(300, primaryStage.getHeight());
                    primaryStage.setMinWidth(currentW);
                    primaryStage.setMinHeight(currentH);
                    primaryStage.centerOnScreen();
                } else {
                    // Se inizialmente eravamo max/fullscreen, assicurati che lo stato sia attivo
                    try {
                        primaryStage.setFullScreen(initiallyFullScreen);
                        primaryStage.setMaximized(initiallyMaximized);
                    } catch (Exception ignored) { }
                    // Non cambiamo minWidth/minHeight troppo piccoli
                    double minW = Math.max(400, primaryStage.getMinWidth());
                    double minH = Math.max(300, primaryStage.getMinHeight());
                    primaryStage.setMinWidth(minW);
                    primaryStage.setMinHeight(minH);
                }
            });
        } else {
            // Sostituisco solo il root: NON tocco larghezza/altezza della stage.
            primaryScene.setRoot(root);

            // Ripristina stato maximized/fullscreen se necessario (evita che venga perso)
            Platform.runLater(() -> {
                try {
                    if (initiallyFullScreen) primaryStage.setFullScreen(true);
                    if (initiallyMaximized) primaryStage.setMaximized(true);
                } catch (Exception ignored) { }
                // Non eseguire sizeToScene né riduzioni: mantieni dimensione corrente.
            });
        }
    }

    /** Mostra la schermata di benvenuto. */
    public static void mostraBenvenuti() { setRoot("/bookrecommender/benvenuti-view.fxml"); }
    /** Mostra la schermata di login. */
    public static void mostraLogin() { setRoot("/bookrecommender/login-view.fxml"); }
    /** Mostra la schermata di registrazione. */
    public static void mostraRegistrazione() { setRoot("/bookrecommender/registrazione-view.fxml"); }
    /** Mostra la dashboard dell'area privata. */
    public static void mostraAreaPrivata() { setRoot("/bookrecommender/areaprivata-view.fxml"); }
    /** Mostra la schermata di ricerca libri. */
    public static void mostraCercaLibri() { setRoot("/bookrecommender/cercaLibri-view.fxml"); }
    /** Mostra la schermata di gestione delle librerie. */
    public static void mostraLibrerie() { setRoot("/bookrecommender/librerie-view.fxml"); }
    /** Mostra la schermata di gestione dei consigli. */
    public static void mostraConsigli() { setRoot("/bookrecommender/consigli-view.fxml"); }
    /** Mostra la schermata di gestione delle valutazioni. */
    public static void mostraValutazioni() { setRoot("/bookrecommender/valutazioni-view.fxml"); }

    /**
     * Mostra la schermata con i dettagli di un libro specifico.
     * @param libro Il libro di cui mostrare i dettagli.
     * @param provenienza La vista da cui è stata avviata la navigazione.
     */
    public static void mostraDettagliLibro(Libro libro, Provenienza provenienza) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(BookRecommender.class.getResource("/bookrecommender/dettagliLibro-view.fxml")));
            Parent root = loader.load();
            DettagliLibroController controller = loader.getController();
            controller.setLibro(libro);
            controller.setProvenienza(provenienza);
            applyRoot(root);
            logger.debug("Cambiata view a: /bookrecommender/dettagliLibro-view.fxml");
        } catch (IOException e) {
            logger.error("Errore nel caricamento della view: /bookrecommender/dettagliLibro-view.fxml", e);
            showErrorView();
        }
    }

    /**
     * Mostra la schermata per consigliare un libro a partire da un altro.
     * @param libro Il libro che si è letto e per cui si vogliono dare consigli.
     * @param libreriaId L'ID della libreria a cui il consiglio è associato.
     */
    public static void mostraConsigliaLibro(Libro libro, int libreriaId) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(BookRecommender.class.getResource("/bookrecommender/consiglia-libro-view.fxml")));
            Parent root = loader.load();
            ConsigliaLibroController controller = loader.getController();
            controller.setData(libro, libreriaId);
            applyRoot(root);
            logger.debug("Cambiata view a: /bookrecommender/consiglia-libro-view.fxml");
        } catch (IOException e) {
            logger.error("Errore nel caricamento della view: /bookrecommender/consiglia-libro-view.fxml", e);
            showErrorView();
        }
    }
    
    /**
     * Mostra la schermata per inserire o modificare una valutazione completa per un libro.
     * @param libro Il libro da valutare.
     * @param nomeLibreria Il nome della libreria in cui si trova il libro.
     */
    public static void mostraValutazioneLibro(Libro libro, String nomeLibreria) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(BookRecommender.class.getResource("/bookrecommender/valutazione-completa.fxml")));
            Parent root = loader.load();
            ValutazioneCompletaController controller = loader.getController();
            controller.setLibro(libro);
            controller.setNomeLibreria(nomeLibreria);
            applyRoot(root);
            logger.debug("Cambiata view a: /bookrecommender/valutazione-completa.fxml");
        } catch (IOException e) {
            logger.error("Errore nel caricamento della view: /bookrecommender/valutazione-completa.fxml", e);
            showErrorView();
        }
    }
    
    /**
     * Mostra la schermata con l'elenco di tutte le valutazioni degli utenti per un libro.
     * @param libro Il libro di cui visualizzare le valutazioni.
     */
    public static void mostraTutteLeValutazioni(Libro libro) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(BookRecommender.class.getResource("/bookrecommender/all-valutazioni-view.fxml")));
            Parent root = loader.load();
            AllValutazioniController controller = loader.getController();
            controller.setLibro(libro);
            applyRoot(root);
            logger.debug("Cambiata view a: /bookrecommender/all-valutazioni-view.fxml");
        } catch (IOException e) {
            logger.error("Errore nel caricamento della view: /bookrecommender/all-valutazioni-view.fxml", e);
            showErrorView();
        }
    }
    
    /**
     * Mostra una schermata di errore generica.
     * <p>
     * Questo metodo viene chiamato quando il caricamento di un file FXML fallisce,
     * per evitare che l'applicazione si blocchi e per notificare l'utente.
     */
    private static void showErrorView() {
        try {
            javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Errore nel caricamento dell'interfaccia");
            errorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: red;");

            if (primaryScene == null) {
                primaryStage.setScene(new Scene(errorLabel));
                primaryScene = primaryStage.getScene();
                primaryStage.show();
            } else {
                primaryScene.setRoot(errorLabel);
            }
            logger.error("Mostrata schermata di errore");
        } catch (Exception e) {
            logger.error("Errore critico nell'applicazione", e);
            System.exit(1);
        }
    }
}
