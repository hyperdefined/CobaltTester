package lol.hyper.cobalttester;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class JSONUtil {

    private static final Logger logger = LogManager.getLogger(JSONUtil.class);

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

    public static void writeFile(Object json, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
        } catch (IOException exception) {
            logger.error("Unable to write file " + file, exception);
        }
    }

    public static void replaceSelected(String table) throws IOException {
        ArrayList<String> fileContent = new ArrayList<String>(Files.readAllLines(Path.of("template.md"), StandardCharsets.UTF_8));
        for (int i = 0; i < fileContent.size(); ++i) {
            String line = fileContent.get(i);
            if (line.contains("<TABLE>")) {
                fileContent.set(i, table);
                continue;
            }
            if (!line.contains("<TIME>")) continue;
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            fileContent.set(i, line.replace("<TIME>", f.format(new Date())));
        }
        Files.write(Path.of("index.md"), fileContent, StandardCharsets.UTF_8);
    }
}
