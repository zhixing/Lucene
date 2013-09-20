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
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchEngine {
	public IndexSearcher searcher = null;
	private boolean VERBOSE = true;
	private Analyzer analyzer;
	private Similarity sim;
	private final int MAX_LENGTH_TITLE = 72;
	private final float HIT_SCORE_THRESHOLD = 0.5f;

	/** Creates a new instance of SearchEngine */

	public SearchEngine(Analyzer analyzer, Similarity sim) throws IOException {
		FSDirectory idx = FSDirectory.open(new File(IndexFileDirectoryGenerator.generatePath(analyzer)));
		searcher = new IndexSearcher(DirectoryReader.open(idx));
		this.analyzer = analyzer;
		this.sim = sim;
	}

	@SuppressWarnings("deprecation")
	public ScoreDoc[] performSearch(String queryString, int noOfTopDocs)
			throws Exception {
		
		String[] fields;
		
		if (queryString.length() <= MAX_LENGTH_TITLE){
			fields = new String[] {"title", "text", "tag"};
		} else{
			fields = new String[] {"text"};
		}

	  	MultiFieldQueryParser mfqp = 
					new MultiFieldQueryParser(
							Version.LUCENE_44, 
							fields, 
							analyzer
					);
			Query mfqpQuery = mfqp.parse(queryString);
			searcher.setSimilarity(sim);
			
			TopDocs topDocs = searcher.search(mfqpQuery, noOfTopDocs);

		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		
		List<ScoreDoc> toReturn = new ArrayList<ScoreDoc>();
		for (int i = 0; i < scoreDocs.length; i++) {
			ScoreDoc hit = scoreDocs[i];
			if (hit.score > HIT_SCORE_THRESHOLD){
				toReturn.add(hit);
			}
		}
		ScoreDoc[] finalResult = new ScoreDoc[toReturn.size()];
		for (int i = 0; i < toReturn.size(); i++){
			finalResult[i] = toReturn.get(i);
		}

		return finalResult;
	} 
}
