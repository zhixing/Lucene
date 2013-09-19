/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

import lucene.demo.search.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class Main {

	/** Creates a new instance of Main */
	public Main() {
	}
	
	private static void performNewSearch(String query) throws Exception{
		System.out.println("performSearch");
		SearchEngine instance = new SearchEngine();
		ScoreDoc[] hits = instance.performSearch(query, 10);

		System.out.println("Results found: " + hits.length);
		for (int i = 0; i < hits.length; i++) {
			ScoreDoc hit = hits[i];
			Document doc = instance.searcher.doc(hits[i].doc); // This
																// retrieves
																// the

			System.out.println(i + " " + doc.get("name") + " " + doc.get("city")
					+ " (" + hit.score + ")");

		}
		System.out.println("performSearch done");
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		try {
			// build a lucene index
			System.out.println("rebuildIndexes");
			Indexer indexer = new Indexer();
			indexer.rebuildIndexes();
			System.out.println("rebuildIndexes done");
			
			// Let user enter query
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);
			System.out.println("Please enter your query:");
			String userQuery = in.readLine();
			
			// and retrieve the result
			try{
				performNewSearch(userQuery);
				
				System.out.println("Enter indexes of relevent results, seperated by space. Press enter to proceed.");
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
					performNewSearch(userQuery);
					
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
