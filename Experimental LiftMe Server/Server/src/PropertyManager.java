import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by minnaar on 2016/03/26.
 */
public final class PropertyManager {
    private static final String LOCAL_PROPERTIES_FILE = "local_properties.xml";
    private static PropertyManager instance = null;
    private Properties properties = null;

    private PropertyManager() {
    }

    public static PropertyManager getInstance() throws IOException {
        if (instance == null) {
            instance = new PropertyManager();
            instance.loadProperties();
        }
        return instance;
    }

    private void loadProperties() throws IOException {
        File file = new File(LOCAL_PROPERTIES_FILE);
        FileInputStream fileInput = new FileInputStream(file);
        properties = new Properties();
        properties.loadFromXML(fileInput);
        fileInput.close();
    }

    public void writeProperties() throws IOException {
        File file = new File(LOCAL_PROPERTIES_FILE);
        FileOutputStream fileOut = new FileOutputStream(file);
        properties.storeToXML(fileOut, "Local Settings");
        fileOut.close();
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

}
