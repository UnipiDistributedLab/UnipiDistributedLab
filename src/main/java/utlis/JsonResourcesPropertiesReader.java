package utlis;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonResourcesPropertiesReader {

    public interface TransformJsonArray<T> {
      public T transform(Object object);
    }

    private JSONObject jsonObject;
    private final java.util.logging.Logger logger = Logger.getLogger(JsonResourcesPropertiesReader.class.getName());

    public JsonResourcesPropertiesReader(String fileName) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
        try {
            JSONParser jsonParser = new JSONParser();
            jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
    }

    public String readVariable(String name) {
        return  jsonObject.get(name).toString();
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }

    public <T> ArrayList<T> readArray(String name, TransformJsonArray transformJsonArray) {
        JSONArray arrayData = (JSONArray) jsonObject.get(name);
        ArrayList<T> transformedData = new ArrayList<>();
        for (Object serverDataJson : arrayData) {
            transformedData.add((T) transformJsonArray.transform(serverDataJson));
        }
        return transformedData;
    }

    public JSONArray readArray(String name) {
      return  (JSONArray) jsonObject.get(name);
    }
}
