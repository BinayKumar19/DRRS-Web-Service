import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {

    BufferedWriter output = null;
    String logFIlePath = "./logs/";

    LogWriter(String logFileName, Boolean appendFlag)
    {
        FileWriter file = null;
        try {
            file = new FileWriter(logFIlePath + logFileName,appendFlag);
        } catch (IOException e) {
            e.printStackTrace();
        }
        output = new BufferedWriter(file);
    }


    public void writeToLog(String line)
    {
        try {
            output.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeLogWriter()
    {
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
