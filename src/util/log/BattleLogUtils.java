package util.log;

import util.BattleConst;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BattleLogUtils {

    private static int tick = 0;

    public static void setTick(int tick) {
        tick = tick;
    }

    public static String generateFileName() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ss-mm-HH-dd-MM-yyyy");
        String formattedDateTime = now.format(formatter);
        return formattedDateTime + ".txt";
    }
    public static void writeLog(String message) {
        File logFile = new File(BattleConst.LOG_URL+generateFileName());

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
        writeLog( "HELLO ANH EM");
        writeLog( "HELLO ANH EM 2");
        writeLog( "HELLO ANH EM 3");
    }
}
