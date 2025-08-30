package bookrecommender.server.utili;

import java.sql.SQLException;

/**
 * Classe di utilità per la gestione delle eccezioni SQL.
 * Fornisce metodi per la stampa dettagliata di {@link SQLException}.
 * La classe è finale e ha un costruttore privato per impedire l'istanziazione.
 */
public final class DBUtil {

    /**
     * Costruttore privato per impedire l'istanziazione della classe di utilità.
     */
    private DBUtil() {}

    /**
     * Stampa in modo dettagliato le informazioni di una {@link SQLException} concatenata.
     * Itera attraverso la catena di eccezioni e per ogni {@code SQLException} stampa
     * SQLState, Error Code, messaggio e le cause sottostanti.
     *
     * @param ex l'eccezione {@code SQLException} da stampare.
     */
    public static void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException sqlEx) {
                System.err.println("SQLState: " + sqlEx.getSQLState());
                System.err.println("Error Code: " + sqlEx.getErrorCode());
                System.err.println("Message: " + sqlEx.getMessage());
                Throwable t = ex.getCause();
                while (t != null) {
                    System.err.println("Cause: " + t);
                    t = t.getCause();
                }
            }
        }
    }
}


