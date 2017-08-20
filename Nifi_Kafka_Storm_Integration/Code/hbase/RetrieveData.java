import java.io.IOException;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class RetrieveData{

   public static void main(String[] args) throws IOException, Exception{

      // Instantiating Configuration class
      Configuration config = HBaseConfiguration.create();

      // Instantiating HTable class
      HTable table = new HTable(config, "transitAlert_Weather");

      // Instantiating Get class
      Get g = new Get(Bytes.toBytes("row1"));

      // Reading the data
      Result result = table.get(g);

      // Reading values from Result class object
      byte [] value = result.getValue(Bytes.toBytes("stopId"),Bytes.toBytes("Id"));
      byte [] value1 = result.getValue(Bytes.toBytes("transitAlert"),Bytes.toBytes("alert"));
	  byte [] value2 = result.getValue(Bytes.toBytes("weather"),Bytes.toBytes("info"));

      // Printing the values
      String stopId = Bytes.toString(value);
      String transitAlert = Bytes.toString(value1);
	  String weatherInfo = Bytes.toString(value2);

      System.out.println("stopId: " + stopId + " transitAlert: " + transitAlert+ " weatherInfo: " + weatherInfo);
   }
}
