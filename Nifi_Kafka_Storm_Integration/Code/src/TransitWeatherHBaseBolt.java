import java.sql.Timestamp;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
//import org.apache.log4j.Logger;


import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.base.BaseBasicBolt;

import java.util.Random;

public class TransitWeatherHBaseBolt extends BaseBasicBolt
{
    private static final long serialVersionUID = 2946379346389650318L;
    //private static final Logger LOG = Logger.getLogger(TransitWeatherHBaseBolt.class);

    //TABLES
    private static final String EVENTS_TABLE_NAME =  "transitAlert_Weather";
 

    //CF
    private static final byte[] CF_STOPID = Bytes.toBytes("stopId");
    private static final byte[] CF_TRANSITALERT = Bytes.toBytes("transitAlert");
	private static final byte[] CF_WEATHERINFO = Bytes.toBytes("weather");

    //COL
    private static final byte[] COL_STOPID = Bytes.toBytes("Id");
    private static final byte[] COL_TRANSITALERT = Bytes.toBytes("alert");
    private static final byte[] COL_WEATHERINFO = Bytes.toBytes("info");
    


    private OutputCollector collector;
    private HConnection connection;   
    private HTableInterface eventsTable;

    public TransitWeatherHBaseBolt() 
    {

    }
/*
    @Override
    public void prepare(Map stormConf, TopologyContext context,
                    OutputCollector collector) 
    {
        this.collector = collector;
        try 
        {
            Configuration config = HBaseConfiguration.create();
			this.connection = HConnectionManager.createConnection(config);            	
            this.eventsTable = connection.getTable(EVENTS_TABLE_NAME);
        } 
        catch (Exception e) 
        {
            String errMsg = "Error retrieving connection and access to HBase Tables";
            LOG.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }		
    }
*/
    @Override
    public void execute(Tuple tuple, BasicOutputCollector collector)
    {

        //LOG.info("About to insert tuple["+input +"] into HBase...");
		String stopId=tuple.getStringByField(TransitWeatherScheme.STOP_ID);
		String transitAlert=tuple.getStringByField(TransitWeatherScheme.TRANSIT_ALERT);
		String weatherInfo=tuple.getStringByField(TransitWeatherScheme.WEATHER_INFO);
        try 
        {
            Configuration config = HBaseConfiguration.create();
			config.set("zookeeper.znode.parent", "/hbase-unsecure");
			  //config.set("hbase.master", "demo.hortonworks.com:60000");
   config.set("hbase.zookeeper.quorum","demo.hortonworks.com");
   config.set("hbase.zookeeper.property.clientPort", "2181");
			
			this.connection = HConnectionManager.createConnection(config);            	
            this.eventsTable = connection.getTable(EVENTS_TABLE_NAME);
			if (this.eventsTable==null)
			{
				System.out.println("HBase Table is None 1");
			}
        } 
        catch (Exception e) 
        {
            String errMsg = "Error retrieving connection and access to HBase Tables";
            //LOG.error(errMsg, e);
            throw new RuntimeException(errMsg, e);
        }	
		  
        try 
        {

            Put put = constructRow(stopId, transitAlert, weatherInfo);
			System.out.println("Inserting row into table");
			if (this.eventsTable==null)
			{
				System.out.println("HBase Table is None 2");
			}
            this.eventsTable.put(put);
			System.out.println("Successfully inserted row into table");

        } 
        catch (Exception e) 
        {
            //LOG.error("Error inserting event into HBase table["+EVENTS_TABLE_NAME+"]", e);
			System.out.println("Error inserting event into HBase table["+EVENTS_TABLE_NAME+"]" +e.toString());
			e.printStackTrace();
        }       
        collector.emit(new Values(stopId, transitAlert, weatherInfo));
        
    }

    private Put constructRow(String stopId, String transitAlert, String weatherInfo) 
    {

        String rowKey = consructKey(stopId);
		System.out.println("Record with key[" + rowKey + "] going to be inserted...");
        Put put = new Put(Bytes.toBytes(rowKey));
		System.out.println("StopID: "+stopId+ "; TransitAlert: "+transitAlert+"; WeatherInfo: "+weatherInfo);
		try{

        put.add(CF_STOPID, COL_STOPID, Bytes.toBytes(stopId));
        put.add(CF_TRANSITALERT, COL_TRANSITALERT, Bytes.toBytes(transitAlert));
		put.add(CF_WEATHERINFO, COL_WEATHERINFO, Bytes.toBytes(weatherInfo)); 		
		}
		 catch (Exception e) 
        {
            //LOG.error("Error inserting event into HBase table["+EVENTS_TABLE_NAME+"]", e);
			System.out.println("Error inserting row into HBase table["+EVENTS_TABLE_NAME+"]" +e.toString());
			//e.printStackTrace();
        }  
		
        return put;
    }


    private String consructKey(String stopId)
    {
		String rowKey = stopId;
        try{
			double randNo = Math.random();
        rowKey = stopId+"|"+randNo;
		}
		catch (Exception e)
		{
			System.out.println("Error in constructing key: "+e.toString());
		}
        return rowKey;
		
    }	


    @Override
    public void cleanup() 
    {
        try 
        {
               // eventsCountTable.close();
                eventsTable.close();
                connection.close();
        } 
        catch (Exception  e) 
        {
                //LOG.error("Error closing connections", e);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
            declarer.declare(new Fields("Transit-Weather-Alert"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration()
    {
            return null;
    }    
}
