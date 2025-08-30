package bookrecommender;

import bookrecommender.utili.ViewsController;
import javafx.application.Application;
import javafx.stage.Stage;



/**
 * La classe GUI è il punto di ingresso principale per l'applicazione
 * BookRecommender.
 * Estende `javafx.application.Application` e gestisce l'inizializzazione
 * dell'interfaccia utente
 * e la gestione delle scene.
 */
public class GUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Inizializza il ViewsController con lo stage principale
            ViewsController.initialize(primaryStage);

            // Impostazioni generali della finestra
            primaryStage.setTitle("BookRecommender - Scopri i tuoi prossimi libri");
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.setResizable(true);

            // Carica e mostra la view di benvenuto tramite ViewsController (gestisce la
            // scene)
            ViewsController.mostraBenvenuti();

        } catch (Exception e) {
            System.err.println("Errore nel caricamento dell'interfaccia: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Il metodo `main` è il punto di ingresso standard per le applicazioni Java.
     * Chiama `launch(args)` per avviare l'applicazione JavaFX.
     *
     * @param args Gli argomenti della riga di comando passati all'applicazione.
     */
    public static void main(String[] args) {
        launch(args);
    }
}