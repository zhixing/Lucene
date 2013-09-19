/*
 * Indexer.java
 *
 * Created on 6 March 2006, 13:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lucene.demo.search;

import java.io.File;
import java.io.IOException;
import java.util.List;

import lucene.demo.business.RawDocument;
import lucene.demo.business.RawDocumentDatabase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	boolean isIndexedAlready;
	Analyzer analyzer;

	/** Creates a new instance of Indexer */
	public Indexer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}

	private IndexWriter indexWriter = null;

	public IndexWriter getIndexWriter(boolean create) throws IOException {
		if (indexWriter == null && create) {

			final File docDir = new File(IndexFileDirectoryGenerator.generatePath(analyzer));

			if (docDir.exists()) {
				System.out.println("Index at '" + docDir.getAbsolutePath()
						+ "' is already built (delete first!)");
				isIndexedAlready = true;
				return null;
			} else {
				isIndexedAlready = false;
			}

			FSDirectory idx = FSDirectory.open(new File(IndexFileDirectoryGenerator.generatePath(analyzer)));
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
					Version.LUCENE_44, analyzer);

			indexWriter = new IndexWriter(idx, indexWriterConfig);

		}
		return indexWriter;
	}

	public void closeIndexWriter() throws IOException {
		if (indexWriter != null) {
			indexWriter.close();
		}
	}

	public void indexRawDocument(RawDocument rawDocument) throws IOException {

		indexWriter = getIndexWriter(false);

		if (isIndexedAlready) {
			return;
		} else {
		}

		System.out.println("Indexing raw document: " + rawDocument.getId());

		Document doc = new Document();
		doc.add(new StringField("id", rawDocument.getId(), Field.Store.YES));
		
		FieldType ft = new FieldType();
		ft.setIndexed(true);
		ft.setStored(true);
		ft.setStoreTermVectors(true);
		ft.setTokenized(true);
		Field titleField = new Field("title", rawDocument.getTitle(), ft);
		titleField.setBoost(2.0f);
		doc.add(titleField);
		
		doc.add(new Field("text", rawDocument.getText(), ft));
		doc.add(new Field("author", rawDocument.getAuthor(), ft));
		doc.add(new Field("tag", rawDocument.getTag(), ft));
		doc.add(new Field("others", rawDocument.getOthers(), ft));
		
		String allContent = rawDocument.getTitle() + " " + rawDocument.getText() + " "
							+ rawDocument.getAuthor() + " " + rawDocument.getTag();
		doc.add(new Field("content", allContent, ft));

		indexWriter.addDocument(doc);
	}

	public void rebuildIndexes() throws IOException {
		getIndexWriter(true);

		List<RawDocument> rawDocuments = RawDocumentDatabase.getRawDocuments();
		for (RawDocument rawDocument : rawDocuments) {
			indexRawDocument(rawDocument);
		}

		closeIndexWriter();
	}

}
