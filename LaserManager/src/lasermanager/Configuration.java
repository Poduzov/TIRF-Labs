/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lasermanager;

/**
 * @author Denis Poduzov poduzov@gmail.com
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author user
 */
public class Configuration {

    public static final int AO_COUNT = 4;
    public static final int DO_COUNT = 2;

    public static final double MAX_RESPONSE_POWER = 2000d;
    public static final double MAX_OUT_VOLTAGE = 5.0d;
    public static final int MAX_CURVE_SIZE = 30;

    public String ComPortName;
    public String SequenceFileName;

    private String folderPath;
    private String confgigFilePath;

    public Configuration() {
        try {
            File jarFile = new File(Configuration.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            folderPath = jarFile.getParentFile().getPath();
            confgigFilePath = folderPath + File.separator + "configuration.txt";
            File configFile = new File(confgigFilePath);

            if (!configFile.exists()) {
                SaveDefault();
            } else {
                Load();
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public final void SaveDefault() {
        this.ComPortName = "";
        this.SequenceFileName = "";
        Save();
    }

    public final void Save() {
        try (PrintWriter out = new PrintWriter(confgigFilePath)) {
            out.println("ComPortName = " + ComPortName);
            out.println("SequenceFileName = " + SequenceFileName);
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public final void Load() {
        try (Stream<String> stream = Files.lines(Paths.get(confgigFilePath))) {
            stream.forEach(line -> {
                ParseConfigLine(line);
            });
        } catch (Exception ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ParseConfigLine(String line) {
        try {
            String[] parts = line.split("=");

            switch (parts[0].trim().toLowerCase()) {
                case "comportname":
                    ComPortName = parts[1].trim();
                    break;
                case "sequencefilename":
                    SequenceFileName = parts[1].trim();
                    break;
            }
        } catch (Exception ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
