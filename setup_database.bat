@echo off
echo ========================================
echo    BookRecommender Database Setup
echo ========================================
echo.

echo [1/3] Compilazione tool database...
cd /d "%~dp0\creazioneDB"

REM Verifica disponibilita' di Maven
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Maven non trovato nel PATH. Provo a procedere se il JAR e' gia' presente...
) else (
    echo Eseguo: mvn -f pom-temp.xml clean package
    mvn -f pom-temp.xml clean package
)

if not exist "target\DBCreatorBR-1.0.jar" (
    echo ERRORE: JAR non trovato: target\DBCreatorBR-1.0.jar
    echo Suggerimenti:
    echo  - Assicurati di avere Maven installato e nel PATH (mvn -v)
    echo  - Esegui manualmente: mvn -f creazioneDB\pom-temp.xml clean package
    echo  - Controlla eventuali errori stampati sopra
    pause
    exit /b 1
)

echo.
echo [2/3] Inserisci le credenziali PostgreSQL
set /p DB_USER=Username PostgreSQL: 
set /p DB_PASS=Password PostgreSQL: 

echo.
echo [3/3] Creazione database e tabelle...
java -jar target/DBCreatorBR-1.0.jar %DB_USER% %DB_PASS%

echo.
echo Operazione completata.
pause
