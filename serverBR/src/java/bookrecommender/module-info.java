module serverBR {
    requires inComune;
    requires java.rmi;
    requires java.sql;
    requires org.apache.logging.log4j;

    exports bookrecommender.server.utenti;
    exports bookrecommender.server.utili;
    exports bookrecommender.server.librerie;
    exports bookrecommender.server.libri;
}
