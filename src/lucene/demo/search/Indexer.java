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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {
	boolean isIndexedAlready;

	/** Creates a new instance of Indexer */
	public Indexer() {
	}

	private IndexWriter indexWriter = null;

	public IndexWriter getIndexWriter(boolean create) throws IOException {
		if (indexWriter == null && create) {

			final File docDir = new File("index-directory");

			if (docDir.exists()) {
				System.out.println("Index at '" + docDir.getAbsolutePath()
						+ "' is already built (delete first!)");
				isIndexedAlready = true;
				return null;
			} else {
				isIndexedAlready = false;
			}

			FSDirectory idx = FSDirectory.open(new File("index-directory"));
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
					Version.LUCENE_36, new StandardAnalyzer(
							Version.LUCENE_36));

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
		doc.add(new Field("id", rawDocument.getId(), Field.Store.YES,
				Field.Index.NO));
		doc.add(new Field("title", rawDocument.getTitle(), Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("text", rawDocument.getText(), Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("author", rawDocument.getAuthor(), Field.Store.YES,
				Field.Index.ANALYZED));
		doc.add(new Field("others", rawDocument.getOthers(), Field.Store.YES,
				Field.Index.ANALYZED));

		String fullSearchableText = rawDocument.getTitle() + " "
				+ rawDocument.getText() + " " + rawDocument.getAuthor();
		doc.add(new Field("content", fullSearchableText, Field.Store.NO,
				Field.Index.ANALYZED));
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
