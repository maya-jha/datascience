import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.spout.Scheme;
import org.apache.storm.kafka.StringScheme;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;
import org.apache.storm.LocalCluster; 

import org.apache.storm.kafka.BrokerHosts;
import org.apache.storm.kafka.KafkaSpout;
import org.apache.storm.kafka.SpoutConfig;
import org.apache.storm.kafka.ZkHosts;


public class TransitWeatherEventProcessingTopology {
  private static final String KAFKA_SPOUT_ID = "kafkaSpout"; 
  private static final String HBASE_BOLT_ID = "hbaseBolt";

  private static SpoutConfig constructKafkaSpoutConf() {
    String kafkaHost="demo.hortonworks.com:2181";	 
    BrokerHosts hosts = new ZkHosts(kafkaHost);
    String topic = "cs498ProjectFinalV3";
    String zkRoot = "/" + topic;
    String consumerGroupId = "StormSpout";

    SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, consumerGroupId);
    spoutConfig.scheme =new SchemeAsMultiScheme(new TransitWeatherScheme());

    return spoutConfig;
  }
/*
  public void configureKafkaSpout(TopologyBuilder builder) {
    KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
    int spoutCount = 1;
    builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout);
  }

 

  public void configureHBaseBolt(TopologyBuilder builder) {
    TransitWeatherHBaseBolt hbaseBolt = new TransitWeatherHBaseBolt();
    builder.setBolt(HBASE_BOLT_ID, hbaseBolt, 2).shuffleGrouping(KAFKA_SPOUT_ID);
  }
*/
  public static void main(String[] str) throws Exception {    
     TopologyBuilder builder = new TopologyBuilder();
    //configureKafkaSpout(builder);
    // configureLogTruckEventBolt(builder);
  

    //configureHBaseBolt(builder);
	
	KafkaSpout kafkaSpout = new KafkaSpout(constructKafkaSpoutConf());
    int spoutCount = 1;
    builder.setSpout(KAFKA_SPOUT_ID, kafkaSpout);
	
	TransitWeatherHBaseBolt hbaseBolt = new TransitWeatherHBaseBolt();
    builder.setBolt(HBASE_BOLT_ID, hbaseBolt, 2).shuffleGrouping(KAFKA_SPOUT_ID);
	

    Config conf = new Config();
    conf.setDebug(true);

    conf.setMaxTaskParallelism(3);

    LocalCluster cluster = new LocalCluster();
    cluster.submitTopology("transit_weather", conf, builder.createTopology());

    //wait for 2 minutes and then kill the job
    Thread.sleep( 2 * 60 * 1000);

    cluster.shutdown();
  }

}
