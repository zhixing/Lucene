package lucene.demo.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TestEngine {
	
	private Map<String, String> mQueries;
	private Map<String, List<String>> mAssertion;

	public TestEngine(String pathForQueryXML, String pathForAssertionFile) {
		parseQueryFile(pathForQueryXML);
		parseAssertionFile(pathForAssertionFile);
	}

	private void parseAssertionFile(String pathForAssertionFile) {
		mAssertion = new HashMap<String, List<String>>();
		
		try {
			File txt = new File(pathForAssertionFile);
			BufferedReader br = new BufferedReader(new FileReader(txt));
			String line;
			while ((line = br.readLine()) != null) {
			   Pattern pattern = Pattern.compile("(\\w+) (\\d+) (\\d)");
			   Matcher matcher = pattern.matcher(line);
			   if (matcher.find()){		   
				   String number = matcher.group(1);
				   String doc = matcher.group(2);
				   List<String> docs = mAssertion.get(number);
				   if (docs == null) {
					   docs = new ArrayList<String>();
				   }
				   docs.add(doc);
				   mAssertion.put(number, docs);
			   }
			}
			br.close();
		} catch (Exception e) {
			
		}
	}

	private void parseQueryFile(String pathForQueryXML) {
		mQueries = new HashMap<String, String>();
		
		try {
			File xml = new File(pathForQueryXML);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xml);
			NodeList docList = doc.getElementsByTagName("DOC");
			
			for (int i=0; i<docList.getLength(); i++) {
				Node node = docList.item(i);
				Node identifierNode = node.getChildNodes().item(1);
				Node queryNode = node.getChildNodes().item(2);
				
				String identifier = identifierNode.getTextContent().trim();
				String query = queryNode.getTextContent().trim();
				mQueries.put(identifier, query);
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * 
	 * @return Map<String, String> "q1" => "query string"
	 */
	
	public Map<String, String> getTestQueries(){
		return mQueries;
	}
	
	/**
	 * 
	 * @param retrived docs for each query identifier
	 * @return precision, recall
	 */
	public List<Float> calculateAccuracy(Map<String, List<String>> retrieved) {
		List<Float> accuracy = new ArrayList<Float>();
		Float precision = 0f;
		Float recall = 0f;
		
		for (Map.Entry<String, List<String>> entry : retrieved.entrySet()) {
			String queryIdentifier = entry.getKey();
			List<String> retrievedDocs = entry.getValue();
			List<String> relevantDocs = mAssertion.get(queryIdentifier);
			
			List<String> retrievedDocsIntersect = new ArrayList<String>();
			for (String doc : retrievedDocs) {
				retrievedDocsIntersect.add(new String(doc));
			}
			// Take the intersection of retrieved docs and relevant docs
			retrievedDocsIntersect.retainAll(relevantDocs);
			
			precision += retrievedDocsIntersect.size() / new Float(retrievedDocs.size());
			recall += retrievedDocsIntersect.size() / new Float(relevantDocs.size());
		}
		
		precision /= retrieved.size();
		recall /= retrieved.size();
		
		accuracy.add(precision);
		accuracy.add(recall);
		return accuracy;
	}
	
	public List<Float> calculateSingleAccuracy(String queryIdentifier, List<String> retrievedDocs) {
		List<Float> accuracy = new ArrayList<Float>();
		Float precision = 0f;
		Float recall = 0f;
		
		List<String> relevantDocs = mAssertion.get(queryIdentifier);
		List<String> retrievedDocsIntersect = new ArrayList<String>();
		for (String doc : retrievedDocs) {
			retrievedDocsIntersect.add(new String(doc));
		}
		retrievedDocsIntersect.retainAll(relevantDocs);
		
		precision = retrievedDocsIntersect.size() / new Float(retrievedDocs.size());
		recall = retrievedDocsIntersect.size() / new Float(relevantDocs.size());
		
		accuracy.add(precision);
		accuracy.add(recall);
		return accuracy;
	}
}
