import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collections;
import java.util.Comparator;
import java.io.Serializable;
/**
 * a bolt that finds the top n words.
 */
public class TopNFinderBolt extends BaseBasicBolt {
  private HashMap<String, Integer> currentTopWords = new HashMap<String, Integer>();
  private HashMap<String, Integer> allWordsCount = new HashMap<String, Integer>();
  ValueComparator bvc = new ValueComparator(allWordsCount);
  private TreeMap<String,Integer> currentTopWordsTMap =
  		new TreeMap<String,Integer>(bvc);
  private int N;

  private long intervalToReport = 20;
  private long lastReportTime = System.currentTimeMillis();

  public TopNFinderBolt(int N) {
    this.N = N;
  }
 

  @Override
  public void execute(Tuple tuple, BasicOutputCollector collector) {
 /*
    ----------------------TODO-----------------------
    Task: keep track of the top N words


    ------------------------------------------------- */
	  String word;
	  Integer count;
	  try
	  {
		  word=tuple.getString(0);		  
		  count=tuple.getInteger(1);		  
	  }
	  catch(Exception e)
	  {
		  throw new RuntimeException("Error in populating top n words map");
	  }
	  if (allWordsCount.containsKey(word))
	  {
		  if (count>allWordsCount.get(word))
			  {
			  allWordsCount.put(word,count);
			  }
	  }
	  else
	  {
	  allWordsCount.put(word,count);
	  }
	 // if (currentTopWordsTMap.size() > N) {
	        	//	currentTopWordsTMap.remove(currentTopWordsTMap.firstKey());
	        	//}
	        	

    //reports the top N words periodically
    if (System.currentTimeMillis() - lastReportTime >= intervalToReport) {
    	currentTopWordsTMap.clear();
    	currentTopWordsTMap.putAll(allWordsCount);
    	currentTopWords.clear();
    	//System.out.println("Size of currentTopWords After Clearning: "+currentTopWords.size());
    	for(Map.Entry<String,Integer>  m:currentTopWordsTMap.entrySet()){ 
    		if (currentTopWords.size() >= N) break;
    		String wordTSet =m.getKey();
    		Integer countTSet =m.getValue();
    		currentTopWords.put(wordTSet, countTSet);
    		}
      //System.out.println("currentTopWords TMap: "+currentTopWordsTMap);
      collector.emit(new Values(printMap()));
      lastReportTime = System.currentTimeMillis();
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {

     declarer.declare(new Fields("top-N"));

  }

  public String printMap() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("top-words = [ ");
    for (String word : currentTopWords.keySet()) {
      stringBuilder.append("(" + word + " , " + currentTopWords.get(word) + ") , ");
    }
    int lastCommaIndex = stringBuilder.lastIndexOf(",");
    stringBuilder.deleteCharAt(lastCommaIndex + 1);
    stringBuilder.deleteCharAt(lastCommaIndex);
    stringBuilder.append("]");
    return stringBuilder.toString();

  }
}
class ValueComparator implements Comparator<String>,Serializable {
    Map<String, Integer> base;

    public ValueComparator(Map<String, Integer> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with
    // equals.
    public int compare(String a, String b) {
        if (base.get(a) >= base.get(b)) {
            return -1;
        } else {
            return 1;
        } // returning 0 would merge keys
    }
}

