Viene fornito un tool per creare in automatico il database e le relative tabelle (cancellandoli se già presenti).

Per usufruirne, è sufficiente dare il comando "java -jar bin\DBCreatorBR-1.0-jar-with-dependencies.jar <your_user> <your_password>"
da riga di comando, dove <your_user> e <your_password> sono, rispettivamente, l'userid e la password di PostgreSQL dell'utente. 

N.B.: Esegui il comando dalla cartella principale del progetto (ad esempio
C:\Users\<tuo_utente>\...\BookRecommender). Lo script si aspetta che il file
`Libri.dati.csv` sia presente nella working directory: se lanci il comando da
un'altra cartella, copia prima `Libri.dati.csv` nella cartella corrente.

Esempio (PowerShell) eseguito dalla root del progetto:
	java -jar bin\DBCreatorBR-1.0-jar-with-dependencies.jar <db_user> <db_password>

Motivazione: il tool legge `Libri.dati.csv` con percorso relativo dalla working
directory corrente; se il file non è nella stessa cartella il caricamento dei dati
nel database fallirà o non importerà i record desiderati.