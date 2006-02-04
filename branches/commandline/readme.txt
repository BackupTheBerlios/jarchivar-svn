jArchivar 0.1a

Dieses Programm hat die Aufgabe Dateien zu archivieren. Dabei wird ein Hashcode (SHA1) aus den Daten der Datei sowie ihres Namens und der Dateigr��e gebildet. Der Hashcode, der Dateiname und die Dateigr��e werden dann zu einem Archivnamen in einer Datenbank gespeichert.

Hierdurch entsteht der Vorteil, dass man seine Dateien auf Festplatte und auf CD vergleichen, aber auch besser finden kann.

F�r ein optimales Ergebnis empfehle ich die Programme in einer einheitlichen Ordnerstruktur zu speichern. So kann man leicht Programme oder Daten zu einer bestimmten Kategorie finden.

------------------------------------------------------------
Beispiel 1:
Sie m�chten wissen, welche Dateien Sie bereits von Ihrer Festplatte auf CD gebrannt haben und welche nicht.

Handelt es sich dabei um nur eine CD, so k�nnten Sie nun die Dateien der Festplatte einlesen.
Befehl: jArchivar add /pfad festplatte

und mit den Daten auf der CD vergleichen
Befehl: jArchivar diff /cdrom

Dadurch k�nnten Sie feststellen welche Dateien noch nicht gebrant wurden.

Besitzt man allerdings mehrere CDs so ist es besser die einzelnen CDs einzulesen. Auch kann man so die einzelnen Dateien besser wieder finden.

Befehl: jArchivar add /cdrom CDx
Befehl: jArchivar diff /pfad

Die Dateien die dann ein diff ausgibt wurden noch nicht gebrannt.

------------------------------------------------------------
Beispiel 2:
Sie m�chten Duplikate finden, die Sie l�schen k�nnen. Hierf�r sollten Sie alle Dateien in ein eigenes Archiv einlesen. Die Datenbank muss dabei leer sein.

Befehl: jArchivar add /pfad duplikate

Danach kann nach gleichen Dateien gesucht werden.
Befehl: jArchivar same /pfad 

Von der ausgegebenen Dateien solten Sie nie alle l�schen, da ja eine der gefundenen ja �brig bleiben sollte.

Nach dem finden kann man das Archiv wieder l�schen
Befehl: jArchivar remove duplikate

------------------------------------------------------------
Beispiel 3:
Sie suchen eine Datei, die sich irgendwo auf Ihren CDs befindet.

Nachdem Sie alle CDs eingelesen haben
Befehl: jArchivar add /pfad CDx (x=CD-Nummer)

k�nnen Sie nach der Datei suchen.
Befehl: jArchivar find dateiname

Wenn die Datei gefunden wurde wird diese mit Archivnamen ausgegeben.

Dies ist z.B. bei MP3-Sammlungen recht g�nstig.


*************************************************************

Installation:

Folgendes wird ben�tigt:
-JRE (ab 1.4)
-eine Datenbank wie z.B. HSQLDB (hsqldb.sourceforge.net)

Die Installation kann in 3 Schritten verwendet werden.
1.) Die Datei jArchivar.zip in ein Verzeichnis kopieren.
2.) Tabellen in der Datenbank anlegen (hsqldb.sql)
3.) jArchivar.ini anpassen (falls nicht HSQLDB verwendet wird)


F�r postgresql ist folgendes zu tun:

1.) anpassen der pg_hda.conf
2.) createdb jArchivar
3.) createuser jArchivar -P (Passwort eingeben: jArchivar)
4.) psql -U jArchivar jArchivar
5.) \i pg_create.sql
6.) jArchivar.ini anpassen

Beim �berf�hren der Daten von hsqldb nach postgresql sollte darauf geachtet werden, dass die sequenze-Werte angepasst werden bevor mit jArchivar gearbeitet wird. (Z.B. datei_id_seq)

************************************************************

Starten:

Alle Kommandos k�nnen mit dem Befehl help abgefragt werden.

Der normale Aufruf zum starten kann so aussehen
java -jar jArchivar.jar <kommando> [<parameter>]

Die hsqldb.jar muss sich dabei im selben Verzeichnis befinden, damit jArchivar darauf zugeifen kann. Ansonsten muss der Classpath mit angegeben werden.

java -cp ".;hsqldb.jar" -jar jArchivar.jar <kommando> [<parameter>]
