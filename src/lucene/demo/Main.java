/*
 * Main.java
 *
 * Created on 6 March 2006, 11:51
 *
 */

package lucene.demo;

import lucene.demo.search.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.ScoreDoc;

public class Main {

	/** Creates a new instance of Main */
	public Main() {
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

			// perform search on "Dame museum"
			// and retrieve the result
			System.out.println("performSearch");
			SearchEngine instance = new SearchEngine();
			ScoreDoc[] hits = instance.performSearch("Dame museum", 10);

			System.out.println("Results found: " + hits.length);
			for (int i = 0; i < hits.length; i++) {
				ScoreDoc hit = hits[i];
				// Document doc = hit.doc();
				Document doc = instance.searcher.doc(hits[i].doc); // This
																	// retrieves
																	// the

				System.out.println(doc.get("name") + " " + doc.get("city")
						+ " (" + hit.score + ")");

			}
			System.out.println("performSearch done");
		} catch (Exception e) {
			System.out.println("Exception caught.\n");
			System.out.println(e.toString());
		}
	}

}
