package com.mycompany.crtmain;
 
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
 
/**
 * Writes an activity log (activity_log.txt) for the File Encryption Tool.
 * Usage: call ActivityLogger.install() once at the start of main().
 * Only file names and outcomes are logged - never keys or file contents.
 */
public class ActivityLogger {
 
    public enum Action { APP_START, KEY_GENERATION, ENCRYPT, DECRYPT, VIEW_LOG, CLEAR_LOG, APP_EXIT }
    public enum Status { SUCCESS, FAILURE, INFO }
 
    private static final Path LOG = Paths.get("activity_log.txt");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static boolean installed = false;
 
    /** Adds one line to the log, e.g. "2026-10-27 14:03:11 | ENCRYPT | SUCCESS | a.txt.enc". Never throws. */
    public static synchronized void log(Action action, Status status, String details) {
        String clean = details.replaceAll("[\r\n]+", " ").replace("|", "/").trim();
        String line = LocalDateTime.now().format(TIME) + " | " + action + " | " + status + " | " + clean
                + System.lineSeparator();
        try {
            Files.write(LOG, line.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not write to activity log: " + e.getMessage());
        }
    }
 
    /** Returns the whole log as text (also handy for the GUI text area). */
    public static synchronized String getLogAsText() {
        try {
            List<String> lines = Files.exists(LOG) ? Files.readAllLines(LOG) : List.of();
            if (lines.isEmpty()) return "No activity recorded yet.";
            return "----- Activity Log (" + lines.size() + " entries) -----\n" + String.join("\n", lines);
        } catch (IOException e) {
            return "Could not read activity log: " + e.getMessage();
        }
    }
 
    /** View log: prints the whole log to the console (menu option 4). */
    public static void printLog() {
        System.out.println(getLogAsText());
        log(Action.VIEW_LOG, Status.INFO, "Activity log viewed");
    }
 
    /** Clear log: deletes all entries, then records that the log was cleared. Returns true on success. */
    public static synchronized boolean clearLog() {
        try {
            Files.deleteIfExists(LOG);
        } catch (IOException e) {
            System.err.println("Could not clear activity log: " + e.getMessage());
            return false;
        }
        log(Action.CLEAR_LOG, Status.INFO, "Activity log cleared");
        return true;
    }
 
    /** Call once at the start of main(): logs automatically from then on. */
    public static synchronized void install() {
        if (installed) return;
        installed = true;
        System.setOut(new Watcher(System.out));
        log(Action.APP_START, Status.INFO, "Application started");
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> log(Action.APP_EXIT, Status.INFO, "Application closed")));
    }
 
    // Prints everything normally, but also turns the existing messages
    // from FileCryptoService / main into log entries.
    // If those messages are reworded, update the matching text below.
    private static class Watcher extends PrintStream {
        private String last = "", error = "";
 
        Watcher(PrintStream out) { super(out, true); }
 
        @Override
        public synchronized void println(String x) {
            super.println(x);
            String t = (x == null) ? "" : x.trim();
 
            if (t.startsWith("File will be saved:"))
                log(Action.ENCRYPT, Status.SUCCESS, "Encrypted to " + name(t));
            else if (t.startsWith("File saved:"))
                log(Action.DECRYPT, Status.SUCCESS, "Decrypted to " + name(t));
            else if (t.equals("Encryption was Unsuccessful") || t.equals("Decryption was Unsuccessful")) {
                Action a = t.startsWith("Enc") ? Action.ENCRYPT : Action.DECRYPT;
                log(a, Status.FAILURE, error.isEmpty() ? t : error);
                error = "";
            } else if (t.equals("Writing Empty file to disk"))
                log(last.startsWith("Encrypted") ? Action.DECRYPT : Action.ENCRYPT, Status.INFO,
                        "Empty or invalid input - empty file written");
            else if (t.startsWith("New RSA key pair generated"))
                log(Action.KEY_GENERATION, Status.SUCCESS, "New RSA key pair generated");
            else if (t.contains("Error has occured:"))
                error = t;
            last = t;
        }
 
        // "File saved: /home/me/a.txt" -> "a.txt"
        private static String name(String line) {
            Path p = Paths.get(line.substring(line.indexOf(':') + 1).trim()).getFileName();
            return p == null ? "-" : p.toString();
        }
    }
 
    /** Without running the app:  java com.mycompany.crtmain.ActivityLogger [clear] */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            System.out.println(clearLog() ? "Activity log cleared." : "Failed to clear log.");
        } else {
            printLog();
        }
    }
}
 