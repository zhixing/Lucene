/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lucene.demo.search.IndexFileDirectoryGenerator;
import lucene.demo.search.Indexer;
import lucene.demo.search.QueryOptimizer;
import lucene.demo.search.SearchEngine;
import lucene.demo.test.TestEngine;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class Main {

	/** Creates a new instance of Main */
	public Main() {
	}
		
	private static ScoreDoc[] performNewSearch(String query, Analyzer analyzer) throws Exception{
		System.out.println("performSearch");
		SearchEngine instance = new SearchEngine(analyzer);
		ScoreDoc[] hits = instance.performSearch(query, 30);

		System.out.println("Results found: " + hits.length);
		for (int i = 0; i < hits.length; i++) {
			ScoreDoc hit = hits[i];
			Document doc = instance.searcher.doc(hits[i].doc);
			int index = i + 1;
			System.out.println(index + ". ID: " + doc.get("id"));
		}
		System.out.println("performSearch done");
		
		return hits;
	}
	
	private static List<String> getHitsIDs(ScoreDoc[] hits, Analyzer analyzer) throws Exception{
		SearchEngine instance = new SearchEngine(analyzer);
		List<String> toReturn = new ArrayList<String>();
		for(int i = 0; i < hits.length; i++){
			Document doc = instance.searcher.doc(hits[i].doc);
			toReturn.add(doc.get("id"));
		}
		return toReturn;
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		try {
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			TestEngine testEngine = new TestEngine("test/query.xml", "test/assertion.txt");
			List<String> queries = new ArrayList<String>(testEngine.getTestQueries().values());
			
			System.out.println("Building Indexes. Please choose analyzer for indexing and query:");
			System.out.println("Press 1: WhitespaceAnalyzer");
			System.out.println("Press 2: StandardAnalyzer");
			System.out.println("Press 3: EnglishAnalyzer");
			
			int choiceOfAnalyzer = Integer.parseInt(in.readLine());
			Analyzer analyzer;
			switch(choiceOfAnalyzer){
				case 1:
					analyzer = new WhitespaceAnalyzer(Version.LUCENE_44);
					break;
				case 2:
					analyzer = new StandardAnalyzer(Version.LUCENE_44);
					break;
				default:
					System.out.println("Wrong choice of analyzer! We'll choose EnglishAnalyzer by default.");
				case 3:
					analyzer = new EnglishAnalyzer(Version.LUCENE_44);
					break;
			}
			
			Indexer indexer = new Indexer(analyzer);
			indexer.rebuildIndexes();
			System.out.println("rebuildIndexes done");
			
			// Let user enter query
			System.out.println(":");
			System.out.println("Please enter your query:");
			String originalUserQuery = in.readLine();
			String userQuery = originalUserQuery;
			
			boolean isQueryContainedInTestCase;
			if (queries.contains(originalUserQuery)){
				isQueryContainedInTestCase = true;
			} else{
				isQueryContainedInTestCase = false;
			}
			
			// and retrieve the result
			try{
				ScoreDoc[] hits;
				hits = performNewSearch(userQuery, analyzer);
				
				if (isQueryContainedInTestCase){
					List<String> hitsIDs = getHitsIDs(hits, analyzer);
					//List<float> result = testEngine.calculateSingleAccuracy(originalQuery, hitsIDs);
					//System.out.println("Percision: " + result.get(0) + "Recall: " + result.get(1));
				}
					
				System.out.println("Enter indexes of relevent results, seperated by space. Press enter to exit.");
				String currentLine = in.readLine();
				while(!currentLine.equals("")){
					
					// See what's relevent:
					String[] splited = currentLine.split(" ");
					int[] index = new int[splited.length];
					for (int i = 0; i < splited.length; i++) {
					    index[i] = Character.getNumericValue(splited[i].charAt(0)) - 1;
					}
					
					System.out.print("You have chosen indexes: "); 
					
					FSDirectory idx = FSDirectory.open(new File(IndexFileDirectoryGenerator.generatePath(analyzer)));
				  	DirectoryReader reader = DirectoryReader.open(idx);
					Map<String, Integer> frequencies = new HashMap<String, Integer>();

					for(int i : index){
						int indexForPrint = i+1;
						System.out.print(" " + indexForPrint);
						
						Fields fields = reader.getTermVectors(hits[i].doc);
						Terms vector = fields.terms("content");
						TermsEnum termsEnum = null;
						termsEnum = vector.iterator(termsEnum);
						BytesRef text = null;
						while ((text = termsEnum.next()) != null) {
						    String term = text.utf8ToString();
						    int freq = (int) termsEnum.totalTermFreq();
						    int existingFreq;
						    if (frequencies.get(term) == null){
						    	existingFreq = 0;
						    } else{
						    	existingFreq = frequencies.get(term);
						    }
						    frequencies.put(term, freq + existingFreq);
						}
					}
					System.out.println("");
					
					// Optimize the query. And search again:
					userQuery = QueryOptimizer.performRelevenceFeedback(userQuery, frequencies);
					System.out.println("Searching again based on your feedback, and the query is:");
					System.out.println(userQuery);
					hits = performNewSearch(userQuery, analyzer);
					
					if (isQueryContainedInTestCase){
						List<String> hitsIDs = getHitsIDs(hits, analyzer);
						//List<float> result = testEngine.calculateSingleAccuracy(originalQuery, hitsIDs);
						//System.out.println("Percision: " + result.get(0) + "Recall: " + result.get(1));
					}
					
					System.out.println("Enter indexed of relevent results, seperated by space. Press enter to proceed.");
					currentLine = in.readLine();				
				}
			} catch(IOException e){
				System.out.println(e.getMessage());
			}

		} catch (Exception e) {
			System.out.println("Exception caught.\n");
			System.out.println(e.toString());
		}
	}

}
