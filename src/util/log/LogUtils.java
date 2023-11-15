package util.log;

import util.BattleConst;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {

    private static int tick = 0;
    private static String fileName = "";

    public static void setTick(int tick) {
        tick = tick;
    }

    public static void reset() {
        tick = 0;
        fileName = "";
    }

    public static String generateFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH-mm-dd-MM-yyyy");
        String formattedDateTime = "sync.log." + now.format(formatter);
        return formattedDateTime + ".txt";
    }

    public static void writeLog(String message) {
        File logFile;
        if (fileName.isEmpty()) {
            fileName = generateFileName();
        }
        logFile = new File(BattleConst.LOG_URL + fileName);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("Tick: " + tick + "\n");
            buf.append(message + "\n");

            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        writeLog("HELLO ANH EM");
        writeLog("HELLO ANH EM 2");
        writeLog("HELLO ANH EM 3");
    }
}
