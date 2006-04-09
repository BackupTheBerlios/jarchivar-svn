CREATE TABLE DATEI(
ID SERIAL PRIMARY KEY,
HASH VARCHAR(600) NOT NULL,
LAENGE DECIMAL NOT NULL,
NAME VARCHAR(255) NOT NULL,
SUCHMUSTER VARCHAR(500) NOT NULL);

CREATE TABLE ARCHIV(
ID SERIAL PRIMARY KEY,
NAME VARCHAR(100) NOT NULL);

CREATE TABLE PFAD(
ID SERIAL PRIMARY KEY ,
NAME VARCHAR(600) NOT NULL,
SUCHMUSTER VARCHAR(1000) NOT NULL);

CREATE TABLE DATEIPFAD(
ID SERIAL PRIMARY KEY,
DATEI_ID INTEGER NOT NULL,
PFAD_ID INTEGER NOT NULL,
CONSTRAINT SYS_PK_DATEIPFAD UNIQUE (DATEI_ID,PFAD_ID),
CONSTRAINT SYS_FK_15 FOREIGN KEY(DATEI_ID) REFERENCES DATEI(ID) ON DELETE CASCADE,
CONSTRAINT SYS_FK_16 FOREIGN KEY(PFAD_ID) REFERENCES PFAD(ID) ON DELETE CASCADE);

CREATE TABLE DATEIPFADARCHIV(
DATEIPFAD_ID INTEGER NOT NULL,
ARCHIV_ID INTEGER NOT NULL,
CONSTRAINT SYS_PK_DATEIARCHIV PRIMARY KEY(DATEIPFAD_ID,ARCHIV_ID),
CONSTRAINT SYS_FK_7 FOREIGN KEY(DATEIPFAD_ID) REFERENCES DATEIPFAD(ID) ON DELETE CASCADE,
CONSTRAINT SYS_FK_8 FOREIGN KEY(ARCHIV_ID) REFERENCES ARCHIV(ID) ON DELETE CASCADE);

CREATE TABLE IMPORT(
Archiv_ID INTEGER,
Archiv_Name VARCHAR(100),
DATEI_PFAD_ID INTEGER,
PFAD_ID INTEGER,
PFAD VARCHAR(600),
PFAD_SUCHMUSTER VARCHAR(1000),
DATEI_ID INTEGER,
HASH VARCHAR(600),
LAENGE DECIMAL,
DATEI_NAME VARCHAR(255),
DATEI_SUCHMUSTER VARCHAR(500));

CREATE VIEW DATEIINFO AS
SELECT ARCHIV.ID as Archiv_ID, ARCHIV.name as Archiv, 
DATEIPFAD.ID AS DATEIPFAD_ID,
PFAD.id as Pfad_ID, PFAD.name AS Pfad, PFAD.suchmuster AS Pfad_Suchmuster,
DATEI.ID AS DATEI_ID, DATEI.HASH AS HASH, DATEI.LAENGE AS LAENGE, DATEI.NAME AS DATEI_NAME, DATEI.SUCHMUSTER AS DATEI_SUCHMUSTER
FROM DATEI 
INNER JOIN DATEIPFAD ON DATEIPFAD.datei_ID = DATEI.id
INNER JOIN PFAD ON PFAD.ID = DATEIPFAD.pfad_id
INNER JOIN DATEIPFADARCHIV ON DATEIPFAD.id= DATEIPFADARCHIV.dateiPFAD_id
INNER JOIN ARCHIV on ARCHIV.id=DATEIPFADARCHIV.archiv_id;

