package bookrecommender;

/**
 * Classe principale e punto di ingresso per l'applicazione client BookRecommender.
 * <p>
 * Questa classe ha la sola responsabilità di avviare l'applicazione JavaFX,
 * delegando l'inizializzazione e la gestione dell'interfaccia utente alla classe {@link GUI}.
 * Funge da wrapper per il lancio dell'applicazione, garantendo che eventuali
 * errori critici in fase di avvio vengano catturati e registrati.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see GUI
 * @version 1.0
 */
public class BookRecommender {
    /**
     * Il metodo main è il punto di ingresso standard per le applicazioni Java.
     * <p>
     * Invoca il metodo {@code launch} della classe {@link GUI}, che estende
     * {@code javafx.application.Application}, per avviare il toolkit JavaFX e
     * caricare l'interfaccia utente principale dell'applicazione.
     * <p>
     * Un blocco try-catch avvolge la chiamata di avvio per intercettare e
     * stampare su standard error eventuali eccezioni non gestite che potrebbero
     * verificarsi durante la fase di bootstrap dell'applicazione, impedendone
     * l'avvio.
     *
     * @param args Gli argomenti passati dalla riga di comando. Questi vengono
     *             inoltrati al metodo launch di JavaFX.
     */
    public static void main(String[] args) {
        try {
            // Avvia l'applicazione JavaFX
            GUI.launch(GUI.class, args);
        } catch (Exception e) {
            System.err.println("Errore nell'avvio dell'applicazione: " + e.getMessage());
            e.printStackTrace();
        }
    }
}