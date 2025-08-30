package bookrecommender.condivisi.consigli;

import bookrecommender.condivisi.libri.Libro;
import java.io.Serial;
import java.io.Serializable;

/**
 * Rappresenta un libro consigliato, abbinando un oggetto {@link Libro}
 * al numero di volte in cui è stato suggerito.
 * <p>
 * Questo record è un Data Transfer Object (DTO) immutabile, utilizzato per
 * trasferire dal server al client una lista di libri consigliati, tipicamente
 * ordinata per popolarità (numero di consigli).
 *
 * @param libro Il libro che è stato consigliato.
 * @param numeroConsigli Il conteggio di quante volte questo libro è stato
 *                       consigliato in un determinato contesto (es. per un
 *                       particolare libro letto).
 * @see ConsigliService#getConsigliatiConConteggio(long)
 * @see bookrecommender.libri.DettagliLibroController
 */
public record LibroConsigliato(Libro libro, int numeroConsigli) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
