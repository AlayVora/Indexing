import java.util.List;

public class Terms {
	String t;
	Integer documentFrequency;
	Integer tFrequency;
	List<PostingEntry> postingList;
	
	public Terms(String term, Integer documentFrequency, Integer termFrequency, List<PostingEntry> list){
		this.t=term;
		this.documentFrequency=documentFrequency;
		this.tFrequency=termFrequency;
		this.postingList=list;
	}
}


class PostingEntry {

	String documentId;
	Integer frequency;
	public PostingEntry(String docId, Integer freq){
		this.documentId=docId;
		this.frequency=freq;
	}

	@Override
	public String toString() {
		return documentId + " : " + frequency;
	}
}

