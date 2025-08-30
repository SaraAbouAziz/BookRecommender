package bookrecommender.server.consigli;

import bookrecommender.condivisi.consigli.Consiglio;
import bookrecommender.condivisi.consigli.ConsiglioDettagliato;
import bookrecommender.condivisi.consigli.LibroConsigliato;
import bookrecommender.condivisi.libri.Libro;
import bookrecommender.server.utili.DBConnectionSingleton;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implementazione JDBC (Java Database Connectivity) dell'interfaccia {@link ConsigliDAO}.
 * <p>
 * Questa classe è responsabile della persistenza e del recupero degli oggetti
 * {@link Consiglio} e delle relative viste (come {@link ConsiglioDettagliato})
 * da un database relazionale. Implementa tutte le operazioni di accesso ai dati
 * definite nel contratto {@link ConsigliDAO}.
 * <p>
 * Ogni metodo gestisce autonomamente la connessione al database tramite il singleton
 * {@link DBConnectionSingleton}, aprendo e chiudendo una connessione per ogni
 * operazione. In caso di {@link SQLException}, l'eccezione viene catturata,
 * loggata e rilanciata come una {@link RuntimeException} per semplificare la
 * gestione degli errori nei layer superiori.
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @see ConsigliDAO
 * @see Consiglio
 * @see DBConnectionSingleton
 * @version 1.0
 */
public class JdbcConsigliDAO implements ConsigliDAO {

    /** Logger per la registrazione degli eventi della classe. */
    private static final Logger logger = LogManager.getLogger(JdbcConsigliDAO.class);

    /** Query per inserire un nuovo consiglio nella tabella ConsigliLibri. */
    private static final String ADD_CONSIGLIO = "INSERT INTO ConsigliLibri (user_id, libreria_id, libro_letto_id, libro_consigliato_id, commento) VALUES (?, ?, ?, ?, ?)";
    /** Query per contare i consigli dati da un utente per un libro specifico. */
    private static final String COUNT_CONSIGLI = "SELECT COUNT(*) FROM ConsigliLibri WHERE user_id = ? AND libro_letto_id = ?";
    /** Query per trovare i libri consigliati per un dato libro in una specifica libreria. */
    private static final String FIND_CONSIGLIATI = """
        SELECT l.id, l.titolo, l.autori, l.anno, l.descrizione, l.categorie, l.editore, l.prezzo
        FROM ConsigliLibri cl
        JOIN Libri l ON cl.libro_consigliato_id = l.id
        WHERE cl.libreria_id = ? AND cl.libro_letto_id = ?
        """;

    /** Query per trovare i libri consigliati con il conteggio delle raccomandazioni, per una specifica libreria. */
    private static final String FIND_CONSIGLIATI_CON_CONTEGGIO = """
        SELECT l.id, l.titolo, l.autori, l.anno, l.descrizione, l.categorie, l.editore, l.prezzo, COUNT(cl.libro_consigliato_id) as conteggio
        FROM ConsigliLibri cl
        JOIN Libri l ON cl.libro_consigliato_id = l.id
        WHERE cl.libreria_id = ? AND cl.libro_letto_id = ?
        GROUP BY l.id, l.titolo, l.autori, l.anno, l.descrizione, l.categorie, l.editore, l.prezzo
        ORDER BY conteggio DESC
        """;

    /** Query per trovare i libri consigliati con il conteggio delle raccomandazioni, aggregando da tutte le librerie. */
    private static final String FIND_CONSIGLIATI_CON_CONTEGGIO_ALL = """
        SELECT l.id, l.titolo, l.autori, l.anno, l.descrizione, l.categorie, l.editore, l.prezzo, COUNT(cl.libro_consigliato_id) as conteggio
        FROM ConsigliLibri cl
        JOIN Libri l ON cl.libro_consigliato_id = l.id
        WHERE cl.libro_letto_id = ?
        GROUP BY l.id, l.titolo, l.autori, l.anno, l.descrizione, l.categorie, l.editore, l.prezzo
        ORDER BY conteggio DESC
        """;

    /** Query per trovare tutti i consigli (in forma base) dati da un utente. */
    private static final String FIND_BY_USER = """
        SELECT user_id, libreria_id, libro_letto_id, libro_consigliato_id, commento, data_consiglio
        FROM ConsigliLibri
        WHERE user_id = ?
        ORDER BY data_consiglio DESC
        """;

    /** Query per aggiornare il commento di un consiglio esistente. */
    private static final String UPDATE_COMMENTO = """
        UPDATE ConsigliLibri
        SET commento = ?
        WHERE user_id = ? AND libreria_id = ? AND libro_letto_id = ? AND libro_consigliato_id = ?
        """;

    /** Query per eliminare un consiglio specifico. */
    private static final String DELETE_CONSIGLIO = """
        DELETE FROM ConsigliLibri
        WHERE user_id = ? AND libreria_id = ? AND libro_letto_id = ? AND libro_consigliato_id = ?
        """;

    /** Query per trovare tutti i consigli di un utente, arricchiti con dettagli testuali. */
    private static final String FIND_DETTAGLIATI_BY_USER = """
        SELECT cl.user_id, cl.libreria_id, l.nome_libreria as nome_libreria,
               cl.libro_letto_id, ll.titolo as titolo_letto, ll.autori as autore_letto,
               cl.libro_consigliato_id, lc.titolo as titolo_consigliato, lc.autori as autore_consigliato,
               cl.commento, cl.data_consiglio
        FROM ConsigliLibri cl
        JOIN Librerie l ON cl.libreria_id = l.libreria_id
        JOIN Libri ll ON cl.libro_letto_id = ll.id
        JOIN Libri lc ON cl.libro_consigliato_id = lc.id
        WHERE cl.user_id = ?
        ORDER BY l.nome_libreria, ll.titolo, lc.titolo
        """;


    /**
     * {@inheritDoc}
     * <p>
     * Esegue un'operazione di INSERT sulla tabella {@code ConsigliLibri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public void add(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) {
        logger.info("Adding consiglio: userId={}, libreriaId={}, libroLettoId={}, libroConsigliatoId={}", userId, libreriaId, libroLettoId, libroConsigliatoId);
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(ADD_CONSIGLIO)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, libreriaId);
            pstmt.setLong(3, libroLettoId);
            pstmt.setLong(4, libroConsigliatoId);
            pstmt.setString(5, commento);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error adding consiglio", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una query di conteggio sulla tabella {@code ConsigliLibri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public int countConsigliDati(String userId, long libroLettoId) {
        logger.debug("Counting consigli for: userId={}, libroLettoId={}", userId, libroLettoId);
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(COUNT_CONSIGLI)) {
            pstmt.setString(1, userId);
            pstmt.setLong(2, libroLettoId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Error counting consigli", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una query con JOIN tra {@code ConsigliLibri} e {@code Libri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public List<Libro> findConsigliati(int libreriaId, long libroLettoId) {
        logger.debug("Finding consigliati for: libreriaId={}, libroLettoId={}", libreriaId, libroLettoId);
        List<Libro> libri = new ArrayList<>();
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_CONSIGLIATI)) {
            pstmt.setInt(1, libreriaId);
            pstmt.setLong(2, libroLettoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                libri.add(new Libro(
                    rs.getLong("id"),
                    rs.getString("titolo"),
                    rs.getString("autori"),
                    rs.getString("anno"),
                    rs.getString("descrizione"),
                    rs.getString("categorie"),
                    rs.getString("editore"),
                    rs.getString("prezzo")
                ));
            }
        } catch (SQLException e) {
            logger.error("Error finding consigliati", e);
            throw new RuntimeException(e);
        }
        return libri;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una query aggregata con JOIN tra {@code ConsigliLibri} e {@code Libri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public List<LibroConsigliato> findConsigliatiConConteggio(int libreriaId, long libroLettoId) {
        logger.debug("Finding consigliati con conteggio for: libreriaId={}, libroLettoId={}", libreriaId, libroLettoId);
        List<LibroConsigliato> libri = new ArrayList<>();
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_CONSIGLIATI_CON_CONTEGGIO)) {
            pstmt.setInt(1, libreriaId);
            pstmt.setLong(2, libroLettoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Libro libro = new Libro(
                    rs.getLong("id"),
                    rs.getString("titolo"),
                    rs.getString("autori"),
                    rs.getString("anno"),
                    rs.getString("descrizione"),
                    rs.getString("categorie"),
                    rs.getString("editore"),
                    rs.getString("prezzo")
                );
                libri.add(new LibroConsigliato(libro, rs.getInt("conteggio")));
            }
        } catch (SQLException e) {
            logger.error("Error finding consigliati con conteggio", e);
            throw new RuntimeException(e);
        }
        return libri;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una query aggregata con JOIN tra {@code ConsigliLibri} e {@code Libri}, senza filtrare per libreria.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public List<LibroConsigliato> findConsigliatiConConteggio(long libroLettoId) {
        logger.debug("Finding consigliati con conteggio for libroLettoId={}", libroLettoId);
        List<LibroConsigliato> libri = new ArrayList<>();
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_CONSIGLIATI_CON_CONTEGGIO_ALL)) {
            pstmt.setLong(1, libroLettoId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Libro libro = new Libro(
                    rs.getLong("id"),
                    rs.getString("titolo"),
                    rs.getString("autori"),
                    rs.getString("anno"),
                    rs.getString("descrizione"),
                    rs.getString("categorie"),
                    rs.getString("editore"),
                    rs.getString("prezzo")
                );
                libri.add(new LibroConsigliato(libro, rs.getInt("conteggio")));
            }
        } catch (SQLException e) {
            logger.error("Error finding consigliati con conteggio", e);
            throw new RuntimeException(e);
        }
        return libri;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una query di selezione sulla tabella {@code ConsigliLibri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public List<Consiglio> findByUser(String userId) {
        logger.debug("Finding consigli by user: {}", userId);
        List<Consiglio> result = new ArrayList<>();
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_BY_USER)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String uid = rs.getString("user_id");
                int libreriaId = rs.getInt("libreria_id");
                long libroLettoId = rs.getLong("libro_letto_id");
                long libroConsigliatoId = rs.getLong("libro_consigliato_id");
                String commento = rs.getString("commento");
                java.sql.Timestamp ts = rs.getTimestamp("data_consiglio");
                java.time.LocalDateTime data = (ts != null) ? ts.toLocalDateTime() : null;
                result.add(new Consiglio(uid, libreriaId, libroLettoId, libroConsigliatoId, commento, data));
            }
        } catch (SQLException e) {
            logger.error("Error finding consigli by user", e);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue un'operazione di UPDATE sulla tabella {@code ConsigliLibri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public void updateCommento(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId, String commento) {
        logger.info("Updating commento for consiglio: userId={}, libreriaId={}, libroLettoId={}, libroConsigliatoId={}", userId, libreriaId, libroLettoId, libroConsigliatoId);
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_COMMENTO)) {
            pstmt.setString(1, commento);
            pstmt.setString(2, userId);
            pstmt.setInt(3, libreriaId);
            pstmt.setLong(4, libroLettoId);
            pstmt.setLong(5, libroConsigliatoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error updating commento", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue un'operazione di DELETE sulla tabella {@code ConsigliLibri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public void delete(String userId, int libreriaId, long libroLettoId, long libroConsigliatoId) {
        logger.info("Deleting consiglio: userId={}, libreriaId={}, libroLettoId={}, libroConsigliatoId={}", userId, libreriaId, libroLettoId, libroConsigliatoId);
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_CONSIGLIO)) {
            pstmt.setString(1, userId);
            pstmt.setInt(2, libreriaId);
            pstmt.setLong(3, libroLettoId);
            pstmt.setLong(4, libroConsigliatoId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting consiglio", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una query complessa con tre JOIN tra le tabelle {@code ConsigliLibri}, {@code Librerie} e {@code Libri}.
     *
     * @throws RuntimeException se si verifica una {@link SQLException} durante l'accesso al database.
     */
    @Override
    public List<ConsiglioDettagliato> findDettagliatiByUser(String userId) {
        logger.debug("Finding consigli dettagliati by user: {}", userId);
        List<ConsiglioDettagliato> result = new ArrayList<>();
        try (Connection conn = DBConnectionSingleton.openNewConnection();
             PreparedStatement pstmt = conn.prepareStatement(FIND_DETTAGLIATI_BY_USER)) {
            pstmt.setString(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String uid = rs.getString("user_id");
                int libreriaId = rs.getInt("libreria_id");
                String nomeLibreria = rs.getString("nome_libreria");
                long libroLettoId = rs.getLong("libro_letto_id");
                String titoloLetto = rs.getString("titolo_letto");
                String autoreLetto = rs.getString("autore_letto");
                long libroConsigliatoId = rs.getLong("libro_consigliato_id");
                String titoloConsigliato = rs.getString("titolo_consigliato");
                String autoreConsigliato = rs.getString("autore_consigliato");
                String commento = rs.getString("commento");
                java.sql.Timestamp ts = rs.getTimestamp("data_consiglio");
                java.time.LocalDateTime data = (ts != null) ? ts.toLocalDateTime() : null;
                
                result.add(new ConsiglioDettagliato(
                    uid, libreriaId, nomeLibreria,
                    libroLettoId, titoloLetto, autoreLetto,
                    libroConsigliatoId, titoloConsigliato, autoreConsigliato,
                    commento, data
                ));
            }
        } catch (SQLException e) {
            logger.error("Error finding consigli dettagliati by user", e);
            throw new RuntimeException(e);
        }
        return result;
    }
}