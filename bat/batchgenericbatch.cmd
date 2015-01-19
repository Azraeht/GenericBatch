@echo off
cls

REM ######################################################################
REM # # 07/10/2014 - Batch nom du batch                          ##
REM ## Fichier commande Maitre pour le batch nom du batch		##
REM ######################################################################

rem ##########################################################
rem #  1- Définition des chemins                             #
rem ##########################################################
call ..\..\SalsaConfig.bat "XXX"

if NOT "%EXIT_CODE%"=="%RETOUR_OK%" exit %RETOUR_TECH%

rem ##########################################################
rem #  2- Définition des variables                           #
rem ##########################################################
rem - Nom du .bat Slave à exécuter
set SLAVE_CMD=
rem # Exemple : set SLAVE_CMD=java -Xms40m -Xmx64m -cp lib\commons-dbutils-1.5.jar;lib\GenericBatch-1.5.jar;lib\log4j-1.2.17.jar;lib\EpuratorSTDREdition-1.1.jar;lib\ojdbc6.jar; org.paris.batch.EpuratorSTDREdition 

rem ##########################################################
rem #  3- Appeler le programme 
rem ##########################################################
echo -- Exécution du traitement ...
cd %BATCH_HOME_DIR%
cmd /C %SLAVE_CMD%
echo -- ...fin du traitement.

set EXIT_CODE=%ERRORLEVEL%
goto sortie

rem ##########################################################
rem #  4- Sortir avec le code conforme MdP pour Autosys      #
rem ##########################################################
:sortie
echo: 				
echo --  Code de sortie = %EXIT_CODE% 

pause
rem exit %EXIT_CODE%