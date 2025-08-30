package bookrecommender.condivisi.librerie;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rappresenta una libreria personale di un utente.
 * <p>
 * Questo record è un Data Transfer Object (DTO) immutabile, utilizzato per
 * trasferire le informazioni di una libreria tra i vari layer dell'applicazione,
 * in particolare tra server e client.
 * <p>
 * Essendo un record, fornisce automaticamente implementazioni per i metodi
 * {@code equals()}, {@code hashCode()}, {@code toString()} e i metodi di accesso
 * per ogni componente.
 *
 * @param libreriaId    L'identificativo univoco della libreria, generato dal database.
 * @param userId        L'ID dell'utente proprietario della libreria.
 * @param nomeLibreria  Il nome assegnato dall'utente alla libreria.
 * @param dataCreazione La data e l'ora in cui la libreria è stata creata.
 * @param libriIds      La lista degli ID dei libri contenuti in questa libreria.
 * @see java.io.Serializable
 */
public record Libreria(Integer libreriaId, String userId, String nomeLibreria, LocalDateTime dataCreazione, List<Long> libriIds) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}