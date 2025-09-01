package bookrecommender.utili;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Gestisce un'animazione di particelle su un oggetto {@link Canvas} di JavaFX,
 * progettata per funzionare come sfondo dinamico senza interferire con il layout della scena.
 * <p>
 * Questa classe implementa una soluzione robusta che affronta diverse sfide comuni
 * nelle animazioni di background in JavaFX:
 * <ul>
 *     <li><b>Indipendenza dal Layout:</b> Il {@code Canvas} viene reso "non gestito"
 *         ({@code canvas.setManaged(false)}), impedendogli di influenzare il calcolo
 *         automatico delle dimensioni della scena (es. {@code stage.sizeToScene()}).</li>
 *     <li><b>Ridimensionamento Dinamico:</b> Ascolta le modifiche delle dimensioni del suo
 *         contenitore ({@code Parent}) e si ridimensiona di conseguenza per riempire
 *         sempre lo spazio disponibile.</li>
 *     <li><b>Inizializzazione Ritardata:</b> L'animazione viene avviata solo quando il
 *         {@code Canvas} ha effettivamente dimensioni valide (> 0), gestendo i casi in cui
 *         il layout non è ancora stato calcolato al momento della chiamata a {@link #start()}.</li>
 *     <li><b>Pulizia delle Risorse:</b> Ferma automaticamente l'{@link AnimationTimer} quando
 *         la finestra viene chiusa, prevenendo memory leak.</li>
 *     <li><b>Effetto Fade-out:</b> L'animazione ha una durata definita e rallenta
 *         gradualmente fino a fermarsi.</li>
 * </ul>
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @version 1.0
 */
public class ParticleAnimation {

    /** Il Canvas su cui viene disegnata l'animazione. */
    private final Canvas canvas;
    /** Il contesto grafico del canvas, usato per le operazioni di disegno. */
    private final GraphicsContext gc;
    /** La lista contenente tutte le particelle dell'animazione. */
    private final List<Particle> particles;
    /** Il timer che guida il ciclo di animazione, chiamando il metodo {@code handle} ad ogni frame. */
    private final AnimationTimer timer;
    /** Generatore di numeri casuali per inizializzare le proprietà delle particelle. */
    private final Random random = new Random();

    /** Flag volatile che indica se l'animazione è attualmente in esecuzione. */
    private volatile boolean running = false;
    /** Timestamp (in nanosecondi) di quando l'animazione è iniziata. */
    private long startTime;
    /** Durata totale dell'animazione in nanosecondi (5 secondi). */
    private final long animationDuration = 5_000_000_000L; // 5 secondi in nanosecondi

    /** Colore base utilizzato per le particelle. */
    private static final Color PARTICLE_COLOR = new Color(0, 0.847, 0.941, 1.0);

    /**
     * Flag che indica se è stata richiesta l'avvio dell'animazione ({@link #start()})
     * prima che il canvas avesse dimensioni valide. Se {@code true}, l'animazione
     * partirà non appena il canvas sarà inizializzato.
     */
    private boolean pendingStart = false;
    /** Flag per assicurarsi che i listener di ridimensionamento vengano collegati una sola volta. */
    private boolean listenersAttached = false;
    /** Flag che indica che il canvas/animazione sono stati definitivamente dismessi (scene/window chiusa). */
    private volatile boolean disposed = false;

    /** Dimensione massima prudenziale (in px) oltre la quale evitiamo di ridimensionare il canvas (limitazioni texture GPU). */
    private static final double MAX_SAFE_SIZE = 8192; // valore conservativo per la maggior parte delle pipeline

    /**
     * Costruisce un'istanza di ParticleAnimation associata a un {@link Canvas}.
     *
     * @param canvas Il canvas su cui verrà eseguita l'animazione. Non deve essere nullo.
     */
    public ParticleAnimation(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.particles = new ArrayList<>();

        // Evitiamo che il canvas influisca sul layout: lo rendiamo "unmanaged"
        // e lo ridimensioniamo manualmente quando il parent cambia dimensione.
        canvas.setManaged(false);

        // Quando il parent viene assegnato, colleghiamo il listener sui layoutBounds
        canvas.parentProperty().addListener((obs, oldParent, newParent) -> {
            attachParentResizeListener(newParent);
            attachWindowListenersIfNeeded();
            // Proviamo ad inizializzare in background (Platform.runLater così i bounds sono aggiornati)
            Platform.runLater(this::ensureCanvasInitialized);
        });

        // Se la scene viene impostata più tardi, colleghiamo i listener della window
        canvas.sceneProperty().addListener((obs, oldScene, newScene) -> {
            attachWindowListenersIfNeeded();
            Platform.runLater(this::ensureCanvasInitialized);
        });

        // Se il canvas ha già un parent al costruttore, attacchiamo subito il listener
        if (canvas.getParent() != null) {
            attachParentResizeListener(canvas.getParent());
        }

        // Prova iniziale di inizializzazione (in runLater per attendere il layout pass)
        Platform.runLater(this::ensureCanvasInitialized);

        // Timer per l'animazione (identica logica visiva)
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (disposed) { // sicurezza extra
                    stop();
                    return;
                }
                if (!running) return;

                // Verifica che il canvas sia ancora renderizzabile; in caso contrario interrompiamo
                if (!canRender()) {
                    // Non fermiamo subito: proviamo a reinizializzare se ritorna valido
                    return;
                }

                if (startTime == 0) startTime = now;

                long elapsed = now - startTime;

                if (elapsed >= animationDuration) {
                    updateAndDraw(0.0);
                    stop();
                } else {
                    double speedMultiplier = 1.0 - ((double) elapsed / animationDuration);
                    updateAndDraw(speedMultiplier);
                }
            }
        };
    }

    /**
     * Collega un listener alla proprietà {@code layoutBounds} del nodo genitore del canvas.
     * Questo permette di ridimensionare il canvas manualmente ogni volta che il suo contenitore
     * cambia dimensione, mantenendo il canvas come sfondo che riempie l'area senza
     * partecipare alla gestione del layout.
     * @param parent Il nodo genitore del canvas.
     */
    private void attachParentResizeListener(Parent parent) {
        if (parent == null) return;
        if (listenersAttached) return;

        // listener su layoutBounds del parent: ad ogni cambiamento imposto size e posizione del canvas
        parent.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            applyBoundsToCanvas(newBounds);
            // Dopo che abbiamo dimensioni valide, inizializziamo lo stato dell'animazione se necessario
            Platform.runLater(this::ensureCanvasInitialized);
        });

        // Applichiamo subito i bounds correnti (se presenti)
        Bounds b = parent.getLayoutBounds();
        if (b != null && b.getWidth() > 0 && b.getHeight() > 0) {
            applyBoundsToCanvas(b);
        }

        listenersAttached = true;
    }

    /**
     * Applica manualmente larghezza, altezza e posizione al canvas basandosi sui {@link Bounds}
     * del suo contenitore. Prima di impostare i valori, si assicura che le proprietà
     * {@code widthProperty} e {@code heightProperty} non siano associate (bound) ad altre proprietà.
     * @param bounds I bounds del nodo genitore da applicare al canvas.
     */
    private void applyBoundsToCanvas(Bounds bounds) {
        if (bounds == null) return;
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        // Protezione: valori validi
        if (w <= 0 || h <= 0) return;

        // Se le proprietà sono associate (bound), non possiamo impostarle manualmente.
        // Le "dissociamo" (unbind) per poter prendere il controllo.
        if (canvas.widthProperty().isBound()) {
            canvas.widthProperty().unbind();
        }
        if (canvas.heightProperty().isBound()) {
            canvas.heightProperty().unbind();
        }

        // Impostiamo dimensione e posizione
        canvas.setWidth(w);
        canvas.setHeight(h);
        canvas.setLayoutX(bounds.getMinX());
        canvas.setLayoutY(bounds.getMinY());
    }

    /**
     * Collega gli handler di eventi alla finestra ({@link Window}) che contiene il canvas.
     * In particolare, gestisce l'evento {@code WINDOW_SHOWN} per assicurarsi che l'animazione
     * sia inizializzata correttamente quando la finestra diventa visibile, e l'evento
     * {@code onCloseRequest} per fermare l'animazione e liberare risorse.
     */
    private void attachWindowListenersIfNeeded() {
        if (canvas.getScene() == null) return;
        Window window = canvas.getScene().getWindow();
        if (window == null) return;

        // Evitiamo di riaffrontare ripetutamente (non critico ma pulito)
        // WINDOW_SHOWN: assicuriamoci che il canvas venga inizializzato al momento della visualizzazione
        window.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> Platform.runLater(this::ensureCanvasInitialized));

        // Alla chiusura stoppiamo l'animazione per evitare memory leak
    window.setOnCloseRequest(e -> safeDispose());
    window.addEventHandler(WindowEvent.WINDOW_HIDDEN, e -> safeDispose());
    }

    /**
     * Metodo di controllo che assicura l'inizializzazione dello stato dell'animazione.
     * Se il canvas ha dimensioni valide (larghezza e altezza > 0), esegue una delle seguenti azioni:
     * <ul><li>Se c'era una richiesta di avvio in sospeso ({@code pendingStart}), avvia l'animazione.</li>
     * <li>Altrimenti, se le particelle non sono state create, le crea e disegna un frame statico.</li></ul>
     */
    private void ensureCanvasInitialized() {
        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            if (pendingStart) {
                pendingStart = false;
                performStart();
            } else {
                // inizializza lo stato statico se necessario
                if (particles.isEmpty()) {
                    createAndDrawStatic();
                } else {
                    updateAndDraw(0.0);
                }
            }
        }
    }

    /**
     * Crea e inizializza le particelle. Il numero di particelle è calcolato in base
     * all'area del canvas per mantenere una densità costante.
     */
    private void createParticles() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        particles.clear();
        int numberOfParticles = (int) Math.max(1, (w * h) / 5000);
        for (int i = 0; i < numberOfParticles; i++) {
            particles.add(new Particle());
        }
    }

    /**
     * Helper method che crea le particelle e le disegna in una posizione statica.
     * Utile per visualizzare uno sfondo iniziale prima che l'animazione parta.
     */
    private void createAndDrawStatic() {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) return;
        createParticles();
        updateAndDraw(0.0);
    }

    /**
     * Pulisce il canvas e aggiorna la posizione e l'aspetto di ogni particella, poi la disegna.
     * @param speedMultiplier Un fattore (da 0.0 a 1.0) che modula la velocità delle particelle.
     */
    private void updateAndDraw(double speedMultiplier) {
        if (!canRender()) return;
        try {
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            for (Particle p : particles) {
                p.update(speedMultiplier);
                p.draw();
            }
        } catch (NullPointerException npe) {
            // NPE interno di Prism (RTTexture null). Interrompiamo e marchiamo come da reinizializzare.
            System.err.println("[ParticleAnimation] Render interrotto per NPE interno Canvas: " + npe.getMessage());
            // Tentiamo un reset leggero (senza marcare disposed) se la scene esiste ancora.
            if (canvas.getScene() == null) {
                safeDispose();
            } else {
                // Stop e riprova dopo un piccolo delay se ancora valido.
                stop();
                Platform.runLater(() -> {
                    if (!disposed && canRender()) {
                        pendingStart = true;
                        ensureCanvasInitialized();
                    }
                });
            }
        }
    }

    /**
     * Avvia l'animazione delle particelle.
     * Se il canvas non ha ancora dimensioni valide, l'avvio viene posticipato
     * e avverrà automaticamente non appena il layout sarà pronto.
     */
    public void start() {
        if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
            performStart();
        } else {
            pendingStart = true;
        }
    }

    /**
     * Esegue l'avvio effettivo dell'animazione. Se era già in esecuzione,
     * la resetta. Altrimenti, crea le particelle e avvia il timer.
     */
    private void performStart() {
        if (running) {
            // Ri-genera per refresh visivo
            createParticles();
            this.startTime = 0L;
            return;
        }
        createParticles();
        this.startTime = 0L;
        running = true;
        timer.start();
    }

    /**
     * Ferma l'animazione e l'{@link AnimationTimer} associato.
     * È sicuro chiamare questo metodo più volte.
     */
    public void stop() {
        running = false;
        try {
            timer.stop();
        } catch (Exception ignored) {}
    }

    /** Ferma l'animazione e marca la classe come dismessa definitivamente. */
    private void safeDispose() {
        if (disposed) return;
        stop();
        disposed = true;
        particles.clear();
    }

    /**
     * Verifica se il canvas è in uno stato in cui è sensato disegnare:
     * - non dismesso
     * - ha scene e window
     * - width/height in (0, MAX_SAFE_SIZE]
     */
    private boolean canRender() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (disposed) return false;
        if (canvas.getScene() == null) return false;
        if (w <= 0 || h <= 0) return false;
        if (w > MAX_SAFE_SIZE || h > MAX_SAFE_SIZE) return false; // evitiamo richieste di texture enormi
        return true;
    }

    /**
     * Classe interna che rappresenta una singola particella dell'animazione.
     * Ogni particella ha una posizione, un raggio, un'opacità e una velocità.
     * Le sue proprietà vengono inizializzate casualmente.
     */
    private class Particle {
        // Proprietà della particella
        private double x;
        private double y;
        private double radius;
        private double opacity;
        private double xSpeed;
        private double ySpeed;

        /**
         * Costruttore di una particella. Chiama {@link #reset()} per inizializzare i valori.
         */
        Particle() { reset(); }

        /**
         * Inizializza o resetta le proprietà della particella con valori casuali basati sulle dimensioni del canvas.
         */
        void reset() {
            double w = Math.max(1, canvas.getWidth());
            double h = Math.max(1, canvas.getHeight());
            x = random.nextDouble() * w;
            y = random.nextDouble() * h;
            radius = 1 + random.nextDouble() * 1.5;
            opacity = 0.1 + random.nextDouble() * 0.6;
            xSpeed = (random.nextDouble() - 0.5) * (0.5 + random.nextDouble() * 0.5);
            ySpeed = (random.nextDouble() - 0.5) * (0.5 + random.nextDouble() * 0.5);
        }

        /**
         * Aggiorna la posizione della particella in base alla sua velocità e al moltiplicatore di velocità.
         * Se la particella esce dai bordi del canvas, viene riposizionata sul lato opposto (effetto "wrap-around").
         * @param speedMultiplier Fattore che modula la velocità di movimento.
         */
        void update(double speedMultiplier) {
            x += xSpeed * speedMultiplier;
            y += ySpeed * speedMultiplier;

            if (x < 0) x = canvas.getWidth();
            if (x > canvas.getWidth()) x = 0;
            if (y < 0) y = canvas.getHeight();
            if (y > canvas.getHeight()) y = 0;
        }

        /**
         * Disegna la particella sul canvas come un ovale pieno, usando il suo colore, opacità e raggio.
         */
        void draw() {
            gc.setFill(new Color(PARTICLE_COLOR.getRed(), PARTICLE_COLOR.getGreen(), PARTICLE_COLOR.getBlue(), opacity));
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}
