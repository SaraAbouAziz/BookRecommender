package bookrecommender.condivisi.libri;

import java.io.Serial;
import java.io.Serializable;

/**
 * Rappresenta un libro nel sistema BookRecommender.
 * <p>
 * Questo record è un Data Transfer Object (DTO) immutabile, progettato per
 * incapsulare e trasferire le informazioni di un libro tra i vari layer
 * dell'applicazione, in particolare tra il server e il client tramite RMI.
 * <p>
 * Essendo un record, fornisce automaticamente implementazioni per i metodi
 * {@code equals()}, {@code hashCode()}, {@code toString()} e i metodi di accesso
 * per ogni componente.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @param id          L'identificativo univoco del libro.
 * @param titolo      Il titolo del libro.
 * @param autori      Il nome dell'autore o degli autori del libro.
 * @param anno        L'anno di pubblicazione del libro.
 * @param descrizione Una breve descrizione o trama del libro.
 * @param categorie   Le categorie o i generi a cui appartiene il libro.
 * @param editore     La casa editrice del libro.
 * @param prezzo      Il prezzo di copertina del libro.
 * @see java.io.Serializable
 * @version 1.0
 */
public record Libro(Long id, String titolo, String autori, String anno, String descrizione, String categorie, String editore, String prezzo) implements Serializable {
    /** Campo per il controllo della versione durante la serializzazione. */
    private static final long serialVersionUID = 1L;
}
