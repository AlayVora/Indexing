public class Information {
	long documentLength, maxFreq;
	String maxFeqTerm;	
	
	public Information(long docLength, long maxFrequency, String maxFeqTerm){
		this.documentLength=docLength;
		this.maxFreq=maxFrequency;
		this.maxFeqTerm = maxFeqTerm;
	}
	
}
