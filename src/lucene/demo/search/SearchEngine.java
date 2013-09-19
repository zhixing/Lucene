/*
 * SearchEngine.java
 *
 * Created on 6 March 2006, 14:52
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lucene.demo.search;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchEngine {
	public IndexSearcher searcher = null;
	private boolean VERBOSE = true;

	/** Creates a new instance of SearchEngine */

	public SearchEngine() throws IOException {
		FSDirectory idx = FSDirectory.open(new File("index-directory"));
		searcher = new IndexSearcher(DirectoryReader.open(idx));
	}

	@SuppressWarnings("deprecation")
	public ScoreDoc[] performSearch(String queryString, int noOfTopDocs)
			throws Exception {
		
		String[] fields = {"title", "text", "tag"};

	  	MultiFieldQueryParser mfqp = 
					new MultiFieldQueryParser(
							Version.LUCENE_44, 
							fields, 
							new EnglishAnalyzer(Version.LUCENE_44)
					);
			Query mfqpQuery = mfqp.parse(queryString);
			
			TopDocs topDocs = searcher.search(mfqpQuery, noOfTopDocs);
	    
	    /*
	    public MultiFieldQueryParser(Version matchVersion,
	                     String[] fields,
	                     Analyzer analyzer,
	                     Map<String,Float> boosts)
	                     */

		// System.out.println(topDocs);
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		if (VERBOSE)
			System.out.println("Total hits in topDocs: " + topDocs.totalHits
					+ "\n");

		for (int i = 0; i < scoreDocs.length; i++) {
			Document doc = searcher.doc(scoreDocs[i].doc); // This retrieves the
		}

		return scoreDocs;
	} // end of query(...)

}
