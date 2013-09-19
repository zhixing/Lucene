package lucene.demo.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class QueryOptimizer {
	
	private static <K,V extends Comparable<? super V>> 
    List<Entry<K, V>> entriesSortedByValues(Map<K,V> map) {

		List<Entry<K,V>> sortedEntries = new ArrayList<Entry<K,V>>(map.entrySet());

		Collections.sort(sortedEntries, 
			new Comparator<Entry<K,V>>() {
				@Override
				public int compare(Entry<K,V> e1, Entry<K,V> e2) {
					return e2.getValue().compareTo(e1.getValue());
				}
			}
		);

		return sortedEntries;
	}
	
	// Effect: regulate freq from 1 to 2:
	private static float regulateFreq(int freq){
		return 1 + (float) freq / 10;
	}
	
	public static String performRelevenceFeedback(String queryString, Map<String, Integer> frequencies){
		
		List<Entry<String, Integer>> sorted_frequencies = (List<Entry<String, Integer>>) entriesSortedByValues(frequencies);
		
		for (int i = 0; i < sorted_frequencies.size() && i < 3; i++){
			Entry<String, Integer> entry = sorted_frequencies.get(i);
			String term = entry.getKey();
			float term_freq_weight = regulateFreq(entry.getValue());
			queryString += " " + term + "^" + term_freq_weight;
		}
		
		return queryString;
		
	}
}
