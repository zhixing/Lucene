package lucene.demo.business;

public class RawDocument {
	
	private String mId;
	private String mTitle;
	private String mText;
	private String mAuthor;
	private String mOthers;
	
	public RawDocument() {
		mId = "";
		mTitle = "";
		mText = "";
		mAuthor = "";
		mOthers = "";
	}
	
	public RawDocument(String id,
			String title,
			String text,
			String author,
			String others)
	{
		mId = id;
		mTitle = title;
		mText = text;
		mAuthor = author;
		mOthers = others;
	}
	
	public void setId(String id) {
		mId = id;
	}
	
	public String getId() {
		return mId;
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}
	
	public void setText(String text) {
		mText = text;
	}
	
	public String getText() {
		return mText;
	}
	
	public void setAuthor(String author) {
		mAuthor = author;
	}
	
	public String getAuthor() {
		return mAuthor;
	}
	
	public void setOthers(String others) {
		mOthers = others;
	}
	
	public String getOthers() {
		return mOthers;
	}

}
