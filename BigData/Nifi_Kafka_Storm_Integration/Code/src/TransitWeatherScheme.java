import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;
import java.io.StringReader;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

//import org.apache.log4j.Logger;

import org.apache.storm.spout.Scheme;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

public class TransitWeatherScheme implements Scheme {

  public static final String STOP_ID = "stopId";
  public static final String TRANSIT_ALERT = "transitAlert";
  public static final String WEATHER_INFO = "weatherInfo";
  private static final Charset UTF8_CHARSET = StandardCharsets.UTF_8;
  

  //private static final Logger LOG = Logger.getLogger(TransitWeatherScheme.class);

  /**
   * stopId,transitAlert,weatherInfo
   * 
   * @param bytes
   * @return
   */
    public static String deserializeString(ByteBuffer string) {
        if (string.hasArray()) {
            int base = string.arrayOffset();
            return new String(string.array(), base + string.position(), string.remaining(), UTF8_CHARSET);
        } else {
            return new String(Utils.toByteArray(string), UTF8_CHARSET);
        }
    }
    @Override
  public List<Object> deserialize(ByteBuffer bytes) {
    try {
      String transitWeatherEvent = deserializeString(bytes);
	  System.out.println("Kafka Maya JSON is"+transitWeatherEvent);
	  JsonReader reader = Json.createReader(new StringReader(transitWeatherEvent));      
        
        // Get the JsonObject structure from JsonReader.    
       
        JsonObject jsonObj = reader.readObject();
        reader.close();
        String stopId="default";
        String alert="default";
        String weatherInfo="default";
       
                  
            try
            {
            stopId=jsonObj.getString("Stop_id");
            }
            catch (Exception e)
            {
            	
            }
            try
            {
            alert=jsonObj.getString("header_txt");
            }
            catch (Exception e)
            {
            	
            }
            try
            {
            weatherInfo=jsonObj.getString("weather_summary");
            }
            catch (Exception e)
            {
            	
            }
     
	 
      return new Values(stopId,alert,weatherInfo);

    } catch (Exception e) {
     // LOG.error(e);
      throw new RuntimeException(e);
    }

  }

  public Fields getOutputFields() {
    return new Fields(STOP_ID, TRANSIT_ALERT, WEATHER_INFO);

  }

  private String cleanup(String str) {
    if (str != null) {
      return str.trim().replace("\n", "").replace("\t", "");
    } else {
      return str;
    }

  }
}