package bookrecommender.condivisi.consigli;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Rappresenta un singolo consiglio di un libro dato da un utente.
 * <p>
 * Questo record è un Data Transfer Object (DTO) immutabile, utilizzato per
 * trasferire le informazioni complete di un consiglio tra i vari layer
 * dell'applicazione, in particolare tra server e client tramite RMI.
 * <p>
 * La sua chiave primaria logica è composta da {@code userId}, {@code libreriaId},
 * {@code libroLettoId} e {@code libroConsigliatoId}.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @param userId L'identificativo dell'utente che ha dato il consiglio.
 * @param libreriaId L'identificativo della libreria a cui appartiene il libro letto.
 * @param libroLettoId L'identificativo del libro letto per cui si dà il consiglio.
 * @param libroConsigliatoId L'identificativo del libro che viene consigliato.
 * @param commento Un commento testuale opzionale associato al consiglio.
 * @param dataConsiglio La data e l'ora in cui il consiglio è stato registrato nel sistema.
 *                      Può essere {@code null} se l'oggetto rappresenta un consiglio non ancora persistito.
 * @version 1.0
 */
public record Consiglio(
    String userId,
    int libreriaId,
    long libroLettoId,
    long libroConsigliatoId,
    String commento,
    LocalDateTime dataConsiglio
) implements Serializable {

    /** Campo per il controllo della versione durante la serializzazione. */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Costruttore di convenienza per creare un'istanza di {@code Consiglio}
     * che rappresenta un consiglio non ancora persistito. La data del consiglio
     * viene inizializzata a {@code null}, poiché si assume che verrà generata
     * dal database al momento dell'inserimento.
     *
     * @param userId L'ID dell'utente che dà il consiglio.
     * @param libreriaId L'ID della libreria di riferimento.
     * @param libroLettoId L'ID del libro letto.
     * @param libroConsigliatoId L'ID del libro consigliato.
     * @param commento Il commento opzionale.
     */
    public Consiglio(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) {
        this(userId, libreriaId, libroLettoId, libroConsigliatoId, commento, null);
    }
}