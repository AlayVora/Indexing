import java.util.Comparator;


class ListComparator implements Comparator<PostingEntry>{
	public int compare(PostingEntry e1, PostingEntry e2){
		if(Integer.parseInt(e1.documentId.substring(9)) > Integer.parseInt(e2.documentId.substring(9))){
			return 1;
		}
		else{
			return -1;
		
		}
	}
}