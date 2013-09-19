package lucene.demo.search;

import org.apache.lucene.analysis.Analyzer;

public class IndexFileDirectoryGenerator {
	
	public static String generatePath(Analyzer analyzer){
		return "index-directory." + analyzer.getClass().toString();
	}
}
