/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class Main {
	
	private static final int NUM_OF_HITS = 5000;

	/** Creates a new instance of Main */
	public Main() {
	}
	
	private static List<Float> runTestData(Map<String, String> queries, 
			Similarity similarity, Analyzer analyzer, TestEngine testEngine) throws Exception{
		SearchEngine searchEngine = new SearchEngine(analyzer, similarity);
		Map<String, List<String>> retrievedDocs = new HashMap<String, List<String>>();
		
		for (Map.Entry<String, String> entry : queries.entrySet()) {
			String queryIdentifier = entry.getKey();
			String queryString = entry.getValue();
			ScoreDoc[] hits = searchEngine.performSearch(queryString, NUM_OF_HITS);
			List<String> hitDocs = new ArrayList<String>();
			for (int i=0; i< hits.length; i++) {
				ScoreDoc hit = hits[i];
				Document doc = searchEngine.searcher.doc(hit.doc);
				hitDocs.add(doc.get("id"));
			}
			retrievedDocs.put(queryIdentifier, hitDocs);
		}
		
		writeRetrievedDocsToFile(retrievedDocs);
		
		List<Float> accuracy = testEngine.calculateAccuracy(retrievedDocs);
		
		return accuracy;
	}
		
	private static void writeRetrievedDocsToFile(
			Map<String, List<String>> retrievedDocs) {
		Writer writer = null;

		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("test/output.txt"), "utf-8"));
		    
		    for (Map.Entry<String, List<String>> entry : retrievedDocs.entrySet()){
		    	String queryIdentifier = entry.getKey();
		    	List<String> ids = entry.getValue();
		    	for (String id : ids) {
		    		writer.write(queryIdentifier + " " + id );
		    		writer.write("\n");
		    	}
		    }
		} catch (IOException ex){
			
		} finally {
		   try {writer.close();} catch (Exception ex) {}
		}
		
	}

	private static ScoreDoc[] performNewSearch(String query, Analyzer analyzer, Similarity sim) throws Exception{
		System.out.println("performSearch");
		SearchEngine instance = new SearchEngine(analyzer, sim);
		ScoreDoc[] hits = instance.performSearch(query, NUM_OF_HITS);

		System.out.println("Results found: " + hits.length);
		for (int i = 0; i < hits.length; i++) {
			ScoreDoc hit = hits[i];
			Document doc = instance.searcher.doc(hits[i].doc);
			int index = i + 1;
			System.out.println(index + ". ID: " + doc.get("id") + " score: " + hit.score);
		}
		System.out.println("performSearch done");
		
		return hits;
	}
	
	private static void calculateSingleAccuracy(
			ScoreDoc[] hits, 
			Analyzer analyzer, 
			Similarity sim,
			String originalQuery,
			Map<String, String> queries,
			TestEngine testEngine
			) throws Exception{
		List<String> hitsIDs = getHitsIDs(hits, analyzer, sim);
		String queryID = getQueryID(originalQuery, queries);
		List<Float> result = testEngine.calculateSingleAccuracy(queryID, hitsIDs);
		System.out.println("The original query was in the test case, hence, we compute the following measurements:");
		System.out.println("Percision: " + result.get(0) + " Recall: " + result.get(1) + " F1: " + result.get(2));
	}
	
	private static String getQueryID(String originalQuery, Map<String, String> queries){
		String toReturn = "";
		for (String key : queries.keySet()) {
		    String value = queries.get(key);
		    if (value.equals(originalQuery)){
		    	toReturn = key;
		    }
		}
		return toReturn;
	}
	
	private static List<String> getHitsIDs(ScoreDoc[] hits, Analyzer analyzer, Similarity sim) throws Exception{
		SearchEngine instance = new SearchEngine(analyzer, sim);
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
			Map<String, String> queries =testEngine.getTestQueries();
			
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
			
			System.out.println("Please choose similarity measurement:");
			System.out.println("Press 1: DefaultSimilarity");
			System.out.println("Press 2: LMDirichletSimilarity");
			System.out.println("Press 3: BM25Similarity");
			
			int choiceOfSimilarity = Integer.parseInt(in.readLine());
			Similarity sim;
			switch(choiceOfSimilarity){
				default:
					System.out.println("Wrong choice of similarity! We'll choose TFIDFSimilarity by default.");
				case 1:
					sim = new DefaultSimilarity(); // Based on TFIDF
					break;
				case 2:
					sim = new LMDirichletSimilarity();
					break;
				case 3:
					sim = new BM25Similarity();
					break;
			}
			
			Indexer indexer = new Indexer(analyzer, sim);
			indexer.rebuildIndexes();
			System.out.println("rebuildIndexes done");
			
			System.out.println("Please choose what you want to run:");
			System.out.println("Press 1: run test data");
			System.out.println("Press 2: input your own query (if your query is in the test data, we'll give you feedback as well)");
			
			int choiceOfMode = Integer.parseInt(in.readLine());
			switch(choiceOfMode){
				default:
					System.out.println("Wrong choice of Mode! We'll choose test by default.");
				case 1:
					List<Float> accuracy = runTestData(queries, sim, analyzer, testEngine);
					System.out.println(
							"Percision: " + accuracy.get(0) + 
							" Recall: " + accuracy.get(1) + 
							" F1: " + accuracy.get(2)
							);
					return;
				case 2:
					
					break;
			}
			
			// Let user enter query
			System.out.println("Please enter your query:");
			String originalUserQuery = in.readLine();
			String userQuery = originalUserQuery;
			
			boolean isQueryContainedInTestCase;
			if (queries.values().contains(originalUserQuery)){
				isQueryContainedInTestCase = true;
			} else{
				isQueryContainedInTestCase = false;
			}
			
			// and retrieve the result
			try{
				ScoreDoc[] hits;
				hits = performNewSearch(userQuery, analyzer, sim);
				
				if (isQueryContainedInTestCase){
					calculateSingleAccuracy(hits, analyzer, sim, originalUserQuery, queries, testEngine);
				}
					
				System.out.println("Enter indexes of relevent results, seperated by space. Press enter to exit.");
				String currentLine = in.readLine();
				while(!currentLine.equals("")){
					
					// See what's relevent:
					String[] splited = currentLine.split(" ");
					int[] index = new int[splited.length];
					for (int i = 0; i < splited.length; i++) {
					    index[i] =  Integer.parseInt(splited[i]) - 1;
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
					hits = performNewSearch(userQuery, analyzer, sim);
					
					if (isQueryContainedInTestCase){
						calculateSingleAccuracy(hits, analyzer, sim, originalUserQuery, queries, testEngine);
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
