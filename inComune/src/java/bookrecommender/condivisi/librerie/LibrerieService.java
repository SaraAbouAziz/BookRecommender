package bookrecommender.condivisi.librerie;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfaccia del servizio RMI per la gestione delle librerie personali degli utenti.
 * <p>
 * Definisce le operazioni remote che il client può invocare sul server per
 * gestire le librerie personali degli utenti, inclusa la creazione, eliminazione,
 * aggiunta e rimozione di libri dalle librerie.
 * <p>
 * Questa interfaccia è il contratto tra il client e il server per tutto ciò
 * che riguarda la gestione delle librerie personali.
 *
 * @author Abou Aziz Sara Hesham Abdel Hamid 757004
 * @author Ben Mahjoub Ali 759560
 * @author Hidri Mohamed Taha 756235
 * @author Zoghbani Lilia 759652
 * @version 1.0
 */
public interface LibrerieService extends Remote {
    
    /**
     * Crea una nuova libreria per un utente
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @return true se la creazione è riuscita, false altrimenti
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    boolean creaLibreria(String userId, String nomeLibreria) throws RemoteException;
    
    /**
     * Aggiunge un libro a una libreria esistente
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @param libroId ID del libro da aggiungere
     * @return true se l'aggiunta è riuscita, false altrimenti
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    boolean aggiungiLibroALibreria(String userId, String nomeLibreria, long libroId) throws RemoteException;
    
    /**
     * Rimuove un libro da una libreria
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @param libroId ID del libro da rimuovere
     * @return true se la rimozione è riuscita, false altrimenti
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    boolean rimuoviLibroDaLibreria(String userId, String nomeLibreria, long libroId) throws RemoteException;
    
    /**
     * Ottiene tutte le librerie di un utente
     * @param userId ID dell'utente
     * @return lista dei nomi delle librerie
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    List<String> getLibrerieUtente(String userId) throws RemoteException;
    
    /**
     * Ottiene tutti i libri di una libreria specifica
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @return lista degli ID dei libri nella libreria
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    List<Long> getLibriInLibreria(String userId, String nomeLibreria) throws RemoteException;
    
    /**
     * Elimina una libreria completa
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria da eliminare
     * @return true se l'eliminazione è riuscita, false altrimenti
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    boolean eliminaLibreria(String userId, String nomeLibreria) throws RemoteException;
    
    /**
     * Verifica se un libro è presente in una libreria
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @param libroId ID del libro da verificare
     * @return true se il libro è presente, false altrimenti
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    boolean isLibroInLibreria(String userId, String nomeLibreria, long libroId) throws RemoteException;
    
    /**
     * Verifica se un nome libreria è già esistente per un utente
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria da verificare
     * @return true se il nome è già esistente, false altrimenti
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    boolean isNomeLibreriaEsistente(String userId, String nomeLibreria) throws RemoteException;

    /**
     * Ottiene l'ID di una libreria dato il nome.
     * @param userId ID dell'utente
     * @param nomeLibreria nome della libreria
     * @return l'ID della libreria, o -1 se non trovata
     * @throws RemoteException in caso di errore di comunicazione RMI
     */
    int getLibreriaId(String userId, String nomeLibreria) throws RemoteException;
}


