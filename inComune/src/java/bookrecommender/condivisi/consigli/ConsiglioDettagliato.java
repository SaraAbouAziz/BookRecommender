package bookrecommender.condivisi.consigli;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresenta una versione "denormalizzata" di un {@link Consiglio}, arricchita
 * con dettagli testuali (nomi di libri, autori, ecc.) per una facile
 * visualizzazione nell'interfaccia utente.
 * <p>
 * Questo record è un Data Transfer Object (DTO) immutabile, progettato
 * specificamente per essere inviato dal server al client. Il server lo
 * costruisce tipicamente tramite query SQL con JOIN per recuperare i nomi
 * associati agli ID.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @param userId L'identificativo dell'utente che ha dato il consiglio.
 * @param libreriaId L'identificativo della libreria di riferimento.
 * @param nomeLibreria Il nome testuale della libreria.
 * @param libroLettoId L'ID del libro che l'utente ha letto.
 * @param titoloLibroLetto Il titolo del libro letto.
 * @param autoreLibroLetto L'autore o gli autori del libro letto.
 * @param libroConsigliatoId L'ID del libro che viene consigliato.
 * @param titoloLibroConsigliato Il titolo del libro consigliato.
 * @param autoreLibroConsigliato L'autore o gli autori del libro consigliato.
 * @param commento Il commento opzionale lasciato dall'utente.
 * @param dataConsiglio La data e l'ora in cui il consiglio è stato creato.
 * @version 1.0
 */
public record ConsiglioDettagliato(
    String userId,
    int libreriaId,
    String nomeLibreria,
    long libroLettoId,
    String titoloLibroLetto,
    String autoreLibroLetto,
    long libroConsigliatoId,
    String titoloLibroConsigliato,
    String autoreLibroConsigliato,
    String commento,
    LocalDateTime dataConsiglio
) implements Serializable {

    /** Campo per il controllo della versione durante la serializzazione. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Converte questa istanza dettagliata in un oggetto {@link Consiglio} di base.
     * <p>
     * Questo metodo è utile per le operazioni di backend (come aggiornamento o
     * eliminazione) che richiedono la chiave primaria e i dati essenziali del
     * consiglio, ma non i dettagli aggiuntivi.
     *
     * @return Una nuova istanza di {@code Consiglio} contenente i dati fondamentali.
     */
    public Consiglio toConsiglio() {
        return new Consiglio(userId, libreriaId, libroLettoId, libroConsigliatoId, commento, dataConsiglio);
    }

    /**
     * Restituisce una rappresentazione testuale user-friendly.
     */
    public String getDisplayText() {
        return String.format("📚 %s\n" +
                           "📖 Libro letto: %s (%s) [ID: %d]\n" +
                           "💡 Consigliato: %s (%s) [ID: %d]\n" +
                           "💬 %s",
                           nomeLibreria,
                           titoloLibroLetto, autoreLibroLetto, libroLettoId,
                           titoloLibroConsigliato, autoreLibroConsigliato, libroConsigliatoId,
                           commento != null && !commento.trim().isEmpty() 
                               ? commento : "Nessun commento");
    }
}