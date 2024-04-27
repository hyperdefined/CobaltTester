package lol.hyper.cobalttester.tools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class FileUtil {

    private static final Logger logger = LogManager.getLogger(FileUtil.class);

    public static void writeFile(Object json, File file) {
        logger.info("Writing to file " + file.getAbsolutePath());
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
        } catch (IOException exception) {
            logger.error("Unable to write file " + file, exception);
        }
    }

    public static String readFile(File file) {
        byte[] encoded;
        try {
            encoded = Files.readAllBytes(file.toPath());
        } catch (IOException exception) {
            logger.error("Unable to read file " + file, exception);
            return null;
        }
        return new String(encoded, StandardCharsets.UTF_8);
    }

    public static ArrayList<String> readInstances(File instancesFile) {
        ArrayList<String> contents = new ArrayList<>();
        try (LineIterator it = FileUtils.lineIterator(instancesFile, "UTF-8");) {
            while (it.hasNext()) {
                String line = it.nextLine();
                // skip lines that have comment/whitespace
                if (line.startsWith("#") || line.trim().isEmpty()) {
                    continue;
                }
                contents.add(line);

            }
        } catch (IOException exception) {
            logger.error("Unable to read contents of instances file!", exception);
            return null;
        }
        return contents;
    }
}
