/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import lucene.demo.search.Indexer;
import lucene.demo.search.SearchEngine;
import lucene.demo.search.queryOptimizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.Version;

public class Main {

	/** Creates a new instance of Main */
	public Main() {
	}
	
	private static void performNewSearch(String query, Analyzer analyzer) throws Exception{
		System.out.println("performSearch");
		SearchEngine instance = new SearchEngine(analyzer);
		ScoreDoc[] hits = instance.performSearch(query, 30);

		System.out.println("Results found: " + hits.length);
		for (int i = 0; i < hits.length; i++) {
			ScoreDoc hit = hits[i];
			Document doc = instance.searcher.doc(hits[i].doc); // This
																// retrieves
																// the
			//System.out.println(i + ". ID: " + doc.get("id") + " Title: " + doc.get("title") + " Author: " + doc.get("author")
			//		+ " Text: " + doc.get("text") + " Others: " + doc.get("others") + " Hit_Score: " + hit.score);
			
			System.out.println(i + ". ID: " + doc.get("id"));
		}
		System.out.println("performSearch done");
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		try {
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			
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
			
			System.out.println("Please enter your query:");
			String userQuery = in.readLine();
			
			// and retrieve the result
			try{
				performNewSearch(userQuery, analyzer);
				
				System.out.println("Enter indexes of relevent results, seperated by space. Press enter to exit.");
				String currentLine = in.readLine();
				while(!currentLine.equals("")){
					
					// See what's relevent:
					String[] splited = currentLine.split(" ");
					int[] index = new int[splited.length];
					for (int i = 0; i < splited.length; i++) {
					    index[i] = Character.getNumericValue(splited[i].charAt(0));
					}
					
					System.out.print("You have chosen indexes: "); 
					for(int i : index){
						System.out.print(" " + i);
					}
					System.out.println("");
					
					// Optimize the query. And search again:
					ArrayList<String> releventList = null;
					ArrayList<String> irreleventList = null;
					userQuery = queryOptimizer.performRelevenceFeedback(userQuery, releventList, irreleventList);
					System.out.println("Searching again based on your feedback, and the query is:");
					System.out.println(userQuery);
					performNewSearch(userQuery, analyzer);
					
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
