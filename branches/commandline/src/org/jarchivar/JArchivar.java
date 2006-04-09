/*
 * Main.java
 *
 * Created on 1. Mai 2004, 12:24
 */

package org.jarchivar;

import java.io.FileOutputStream;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import org.jarchivar.io.FileManager;
import org.jarchivar.io.HashCalculator;
import org.jarchivar.io.SHA1Calculator;
import org.jarchivar.sql.DBManager;
import org.jarchivar.diagnostic.Debug;

/**
 * 
 * @author Lars Wuckel
 */
/**
 * @author lars
 * 
 */
public class JArchivar {

    protected String path = "";

    protected HashCalculator hashCalc;

    private DBManager dbManager;

    /** Creates a new instance of Main */
    /* args[0] ini-Datei */
    public JArchivar(String[] args) {

        try {

            boolean error = false;

            if (args.length > 1) {
                String command = args[1].toLowerCase();

                File file = new File(args[0]);

                if (file.exists()) {
                    this.hashCalc = new SHA1Calculator();

                    Properties prop = new Properties();
                    prop.load(new FileInputStream(file));

                    String jdbcPath = prop.getProperty("jdbcdriverpath");
                    String jdbcDriver = prop.getProperty("jdbcdriver");
                    String jdbcUrl = prop.getProperty("jdbcurl");
                    String user = prop.getProperty("user");
                    String password = prop.getProperty("password");

                    dbManager = new DBManager(jdbcPath, jdbcDriver, jdbcUrl,
                            user, password);

                    if (command.equalsIgnoreCase("import") && args.length == 4
                            && new File(args[2]).exists()) {

                        importFiles(args[2], args[3]);

                    } else if (command.equalsIgnoreCase("add")){
                        this.add();
                    } else if (command.equalsIgnoreCase("clear_import")){
                        this.clearImport();
                    } else if (command.equalsIgnoreCase("list_imported_archive")){
                        this.listImportedArchive();
                    } else if (command.equalsIgnoreCase("remove")
                            && args.length == 3) {

                        remove(args[2]);

                    } else if (command.equalsIgnoreCase("list")
                            && args.length >= 2) {

                        // Wenn archivname angegeben
                        if (args.length > 2)
                            list(args[2].replace("'", "''"));
                        else
                            list(null);

                    } else if (command.equalsIgnoreCase("listarchive")) {

                        listArchive();

                    } else if (command.equalsIgnoreCase("diff")
                            && args.length == 3) {

                        diff(args[2]);

                    } else if (command.equalsIgnoreCase("same")
                            && args.length == 3) {

                        same(args[2]);

                    } else if (command.equalsIgnoreCase("find")
                            && args.length == 3) {

                        find(args[2]);

                    } else if (command.equalsIgnoreCase("finddir")
                            && args.length == 3) {

                        findPath(args[2]);

                    } else if (command.equalsIgnoreCase("create_scripts")) {

                        String path;

                        // Wenn pfad angegeben
                        if (args.length == 3) {

                            path = args[2];
                        } else {

                            path = System.getProperty("user.dir");
                        }

                        createScripts(path, args[0]);
                    } else {
                        error = true;
                    }
                } else {
                    error = true;

                    System.out.println("Es wurde keine ini-Datei gefunden.");

                }
            } else {

                error = true;
            }

            if (error) {
                System.out
                        .println("JArchivar Version 0.2a (c) 2004 by Lars Wuckel");
                System.out.println();
                System.out
                        .println("command: JArchivar <inifile> clear_import | import | add | remove | list | listarchive | list_imported_archive | find | finddir | same | diff | help | create_scripts");

                System.out.println();
                System.out.println("import <path> <archive-name>");
                System.out
                        .println("   -fügt die Dateien vom angegebenen Pfad rekursiv dem Archiv hinzu.");
                System.out.println("remove <archive-name>");
                System.out.println("diff <path>");
                System.out
                        .println("   -listet alle Dateien des angegebenen Pfades (rekursiv) auf, die noch nicht erfasst wurden.");
                System.out.println("same <path>");
                System.out
                        .println("   -listet alle Dateien des angegebenen Pfades (rekursiv) auf, die noch bereits erfasst wurden.");
                System.out.println("list <archive-name>");
                System.out
                        .println("   -listet alle Dateien des angegebenen Archivs auf.");
                System.out.println("find <datei-name>");
                System.out
                        .println("   -listet Archive auf, welche die Datei enthalten.");
                System.out.println("finddir <pfad-name>");
                System.out
                        .println("   -listet alle Dateien aller Archive auf, welche in einem bestimmten Pfad enthalten sind.");
                System.out.println("create_scripts [<pfad>]");
                System.out
                        .println("   -erstellt linux script-dateien zum leichteren Aufruf der Funktionen.");
                System.out.println("help");
                System.out.println("   -Zeigt diesen Hilfetext an");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        new JArchivar(args);
    }

    private void createScripts(String path, String iniFile) throws Exception {

        String startPath = System.getProperty("user.dir");

        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        if (!startPath.endsWith(File.separator)) {
            startPath += File.separator;
        }

        write("jArchivar.import", path, startPath, iniFile, "import $1 $2");
        write("jArchivar.add", path, startPath, iniFile, "add");
        write("jArchivar.remove", path, startPath, iniFile, "remove $1");
        write("jArchivar.list", path, startPath, iniFile, "list $1");
        write("jArchivar.listarchive", path, startPath, iniFile, "listarchive");
        write("jArchivar.find", path, startPath, iniFile, "find $1");
        write("jArchivar.finddir", path, startPath, iniFile, "finddir $1");
        write("jArchivar.same", path, startPath, iniFile, "same $1");
        write("jArchivar.diff", path, startPath, iniFile, "diff $1");
        write("jArchivar.clear_import", path, startPath, iniFile, "clear_import");
        write("jArchivar.list_imported_archive", path, startPath, iniFile, "list_imported_archive");

    }

    private void write(String scriptFile, String path, String startPath,
            String iniFile, String command) throws Exception {
        Debug.println(path + scriptFile);
        PrintWriter pw = new PrintWriter(
                new FileOutputStream(path + scriptFile));

        pw.println("#/bin/sh");
        pw.println(String.format("java -jar " + startPath
                + "org.jarchivar.jar " + iniFile + " " + command));

        pw.close();
    }

    private void clearImport() {
        try{
            dbManager.freeImport();
        }
        catch(Exception e ){
            if (Debug.DEBUG)
                System.out.print(e);
            else
                e.printStackTrace();            
        }
    }
    
    private void add() {
        Debug.println("add ");
        // printHeap ();

        try {
            dbManager.saveToDatabase();

        } catch (Exception e) {

            if (Debug.DEBUG)
                System.out.print(e);
            else
                e.printStackTrace();
        }
    }

    private void importFiles(String path, String archName) {
        Debug.println("import " + path + " Archive: " + archName);
        // printHeap ();

        try {
            importData(archName, path);

        } catch (Exception e) {

            if (Debug.DEBUG)
                System.out.print(e);
            else
                e.printStackTrace();
        }
    }

    private void importData(String archName, String path) throws Exception {
        FileManager fileManager = new FileManager();
        Debug.println("readFiles:");
        File file = new File(path);
        path = file.getCanonicalPath();

        ArrayList<File> fileList = fileManager.readFiles(path);

        Debug.println("Dateien in die Datenbank integrieren");

        ArrayList<File> errorList = dbManager.importFileData(path, archName, fileList, hashCalc);

        dbManager.updateImport();
        
        if (errorList.size() > 0){
            System.out.println("Folgende Dateien konnten nicht gelesen werden:");
        }
        
        for(File errorFile : errorList){
            System.out.println(errorFile.getPath());
        }
    }

    private void remove(String archName) {

        try {
            int id = dbManager.getArchiveID(archName);
            dbManager.removeArchive(id);
            dbManager.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void find(String name) {
        try {
            dbManager.findFileName(name);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void findPath(String pathName) {
        try {
            dbManager.findPathName(pathName);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void listImportedArchive() {
        try {

            dbManager.listImportedArchive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void listArchive() {
        try {

            dbManager.listArchive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void list(String archiveName) {
        try {

            dbManager.listArchiveFiles(archiveName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Gibt alle Dateien aus, die noch nicht in der
    // Datenbank vorhanden sind
    private void diff(String path) {

        try {
            importData(null, path);
            dbManager.findNewFiles();

        } catch (Exception e) {
        }
    }

    // Gibt alle Dateien aus, die bereits in der
    // Datenbank vorhanden sind
    private void same(String path) {

        try {
            importData(null, path);
            dbManager.findExistingFiles();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
