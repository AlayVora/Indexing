import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import edu.stanford.nlp.util.StringUtils;


public class BuildIndexClass {

	static Map<String, Terms> dictStems = new HashMap<String, Terms>();
	static Map<String, Terms> dictLemma = new HashMap<String, Terms>();
	static HashSet<String> dictionaryStopWords = new HashSet<String>();
	static Map<String, Information> stemsDocInfo = new HashMap<String, Information>();
	static Map<String, Information> lemmaDocInfo = new HashMap<String, Information>();

	public static void main(String[] args) {

		if(args.length == 3){
			try {
				buildIndex(args);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			System.out.println("Please enter arguments as below:");
			System.out.println("[Cranfield-path] [stopwords file-path] [output-directory path]");
		}
	}

	public static void buildIndex(String[] arr) throws Exception{
			String outputDirectoryPath = arr[2];			
			BuildIndexClass buildIndexClass = new BuildIndexClass();
			buildIndexClass.removeFiles(outputDirectoryPath);
			dictionaryStopWords = buildIndexClass.stopWordList(arr[1]);
			
			/*Index Version 1*/ 
			long startT1 = System.currentTimeMillis();
			Map<String, Terms> dictVer1 = buildIndexClass.bulidIndex_Ver1(arr[0]);
			Map<String,Terms> indexVer1 = buildIndexClass.removeStopWords(dictVer1,dictionaryStopWords);
			long endT1 = System.currentTimeMillis();

			long finalT1 = endT1 - startT1;
			System.out.println("Time required to build Version 1 of Index in ms: "+finalT1);
			Map<String, Terms> indexVer1Sort = new TreeMap<String, Terms>(indexVer1);
			buildIndexClass.printIndexMethod(indexVer1Sort, outputDirectoryPath+File.separator+"Index_Version1.uncompressed");

			/* 	Index Version 2*/
			long startT2 = System.currentTimeMillis();
			Map<String, Terms> dictVer2 = buildIndexClass.bulidIndex_Ver2(arr[0]);
			Map<String,Terms> indexVer2 = buildIndexClass.removeStopWords(dictVer2,dictionaryStopWords);
			long endT2 = System.currentTimeMillis();
			long finalT2 = endT2 - startT2;
			
			System.out.println("Time required to build Version 2 of Index in ms: "+finalT2);
			Map<String, Terms> indexVer2Sort = new TreeMap<String, Terms>(indexVer2);			
			buildIndexClass.printIndexMethod(indexVer2Sort, outputDirectoryPath+File.separator+"Index_Version2.uncompressed");

			/* Compression */
			Compression compression = new Compression();
			Map<String, Compression.CDTerm> compressVer1 = Compression.compressedIndexVer1(dictVer1);
			Map<String, Compression.CDTerm> compressVer1Sort = new TreeMap<String, Compression.CDTerm>(compressVer1);

			compression.printCompressedDict(compressVer1Sort, outputDirectoryPath+File.separator+"Index_Version1.compressed");

			Map<String, Compression.CDTerm> compressVer2 = Compression.compressedIndexVer2(dictVer2);
			Map<String, Compression.CDTerm> compressVer2Sort = new TreeMap<String, Compression.CDTerm>(compressVer2);	

			compression.printCompressedDict(compressVer2Sort, outputDirectoryPath+File.separator+"Index_Version2.compressed");


			System.out.println("The size of Index Version 1 uncompressed (in bytes): "+fileSize(outputDirectoryPath+File.separator+"Index_Version1.uncompressed"));
			System.out.println("The size of Index Version 1 compressed (in bytes): "+fileSize(outputDirectoryPath+File.separator+"Index_Version1.compressed"));
			System.out.println("The size of Index Version 2 uncompressed (in bytes): "+fileSize(outputDirectoryPath+File.separator+"Index_Version2.uncompressed"));
			System.out.println("The size of Index Version 2 compressed (in bytes): "+fileSize(outputDirectoryPath+File.separator+"Index_Version2.compressed"));
			System.out.println("The number of inverted Lists in index V1: "+dictVer1.size());
			System.out.println("The number of inverted Lists in index V2: "+dictVer2.size());

			Stemmer stemmer = new Stemmer();
			String[] terms = { "Reynolds", "NASA", "Prandtl", "flow", "pressure", "boundary", "shock" };
			
			System.out.println();
			System.out.println("The df, tf, and inverted list length in bytes for the terms:");
			System.out.println(String.format(
					"\n\t %-10s  %-10s %-10s %-10s ", "Lemma", "Doc Frequency","Term Frequency", "Length of inverted list in bytes"));


			for (String term : terms) {
				String lemmaTerm = Lemmatizer.getInstance().getLemma(term.toLowerCase());
				lemmaTerm = lemmaTerm.replace(" ", "");
				Terms entry = indexVer1Sort.get(lemmaTerm);
				System.out
				.println(String.format("\t %-10s  \t %-10d \t %-10d \t %-10d",lemmaTerm,entry.documentFrequency,entry.tFrequency,
						Compression.getCompressedPostingListSize(compressVer1.get(lemmaTerm).postingList)));
			}




			System.out.println(String.format("\n\t %-10s  %-10s %-10s %-10s ", "Stem", "Doc Frequency", "Term Frequency", "Length of inverted list in bytes"));

			for (String t : terms) {
				String stemTerm = stemmer.stem(t.toLowerCase());
				Terms entry = dictVer2.get(stemTerm);
				System.out.println(String.format("\t %-10s \t %-10d \t %-10d \t %-10d", stemTerm,entry.documentFrequency,entry.tFrequency,Compression.getCompressedPostingListSize(compressVer2.get(stemTerm).postingList)));
			}
			

			String lemmaTerms = Lemmatizer.getInstance().getLemma("NASA".toLowerCase());
			lemmaTerms = lemmaTerms.replace(" ", "");
			Terms e = indexVer1Sort.get(lemmaTerms);
			System.out.println(String.format("\n\t %-10s \t %-15s \t %-10s \t %-10s \t %-10s", "Document ID", "Doc Frequency", "Term Frequency", "Document Length","max_TF"));

			List<PostingEntry> pEntry = indexVer1Sort.get(lemmaTerms).postingList;
			Collections.sort(pEntry, new ListComparator());
			for(int i=0;i<3;i++){
				String infoArr[] =pEntry.get(i).toString().split(":");
				Information docEntry = lemmaDocInfo.get(infoArr[0].trim());
				System.out.println(String.format("\t %-10s \t %-15d \t %-10s \t %-10d  \t \t %-10d", infoArr[0], e.documentFrequency, infoArr[1],docEntry.documentLength,docEntry.maxFreq));
			}

			
			int maximum =Integer.MIN_VALUE;
			int min =Integer.MAX_VALUE;
			List<String> maximumTList = new ArrayList<String>();
			List<String> minimumTList = new ArrayList<String>();
			for(String dt1:indexVer1Sort.keySet()){
				maximum = MaximumValue(maximum, indexVer1Sort.get(dt1).documentFrequency.intValue());
				min = MinimumValue(min, indexVer1Sort.get(dt1).documentFrequency.intValue());			
			}
			
			for(String dt2:indexVer1Sort.keySet()){
				if(indexVer1Sort.get(dt2).documentFrequency.intValue() == maximum){
					maximumTList.add(dt2);
				}
				if(indexVer1Sort.get(dt2).documentFrequency.intValue() == min){
					minimumTList.add(dt2);
				}
			}
			
			System.out.println("Dictionary term(s) from index Version 1 with largest df - " +StringUtils.join(maximumTList,","));
			System.out.println("Dictionary term(s) from index Version 1 with lowest df - " +StringUtils.join(minimumTList,","));

			
			int maximum1=Integer.MIN_VALUE;
			int minimum1=Integer.MAX_VALUE;
			List<String> maxTermList1 = new ArrayList<String>();
			List<String> minTermList1 = new ArrayList<String>();
			
			for(String dt1:indexVer2Sort.keySet()){
				maximum1 = MaximumValue(maximum1, indexVer2Sort.get(dt1).documentFrequency.intValue());
				minimum1 = MinimumValue(minimum1, indexVer2Sort.get(dt1).documentFrequency.intValue());			
			}
			
			
			for(String dt2:indexVer2Sort.keySet()){

				if(indexVer2Sort.get(dt2).documentFrequency.intValue() == maximum1){
					maxTermList1.add(dt2);
				}

				if(indexVer2Sort.get(dt2).documentFrequency.intValue() == minimum1){
					minTermList1.add(dt2);
				}
			}

			System.out.println("Dictionary stem(s) from index Version 2 with largest df - " +StringUtils.join(maxTermList1,","));
			System.out.println("Dictionary stem(s) from index Version 2 with lowest df - " +StringUtils.join(minTermList1,","));


			List<String> maxTFDoc= new ArrayList<String>();
			List<String> maxDocLen=new ArrayList<String>();
			int maxDocLength = Integer.MIN_VALUE,maxtf = Integer.MIN_VALUE;

			for(String docId : lemmaDocInfo.keySet()){

				maxDocLength = MaximumValue((int)lemmaDocInfo.get(docId).documentLength, maxDocLength);
				maxtf =MaximumValue((int)lemmaDocInfo.get(docId).maxFreq,maxtf);
			}

			for(String docId : lemmaDocInfo.keySet()){

				if(maxDocLength == lemmaDocInfo.get(docId).documentLength){
					maxDocLen.add(docId);
				}
				if(maxtf == lemmaDocInfo.get(docId).maxFreq){
					maxTFDoc.add(docId);
				}
			}

			System.out.println("Document(s) with the largest max_tf in the collection - " +StringUtils.join(maxTFDoc,","));
			System.out.println("Document(s) with the largest documentLength in the collection - "+StringUtils.join(maxDocLen,","));
			
	}

	private Map<String, Terms> removeStopWords(Map<String, Terms> uncompressIndexVer1, HashSet<String> stopwordsDict2) {
		Map<String, Terms> temporaryIndex = uncompressIndexVer1;
		Iterator<Entry<String, Terms>> it = temporaryIndex.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if(stopwordsDict2.contains(pair.getKey())){
            	temporaryIndex.remove(pair.getKey());
            }
        }
        return temporaryIndex;

		}

	/*Building Index for Version 1, Version 2*/
	public  Map<String, Terms> bulidIndex_Ver1(String folderPath){
		Lemmatizer lemmatizer = new Lemmatizer();
		File folder = new File(folderPath);
		for(File file: folder.listFiles()){
			if(file.isFile()){
				Map<String, Integer> termFreqMap = lemmatizer.buildLemmaDictionary(file);
				buildIndexVer1(file.getName(),termFreqMap);
			}
		}
		return dictLemma;
	}
	
	
	public  Map<String, Terms> bulidIndex_Ver2(String folderPath){
		Stemmer stemmer = new Stemmer();
		File folder = new File(folderPath);
		for(File file: folder.listFiles()){

			if(file.isFile()){
				Map<String, Integer> termFreqMap = stemmer.buildTermDictionary(file);

				bulidingIndex(file.getName(),termFreqMap);
			}
		}

		return dictStems;
	}
	
	private static int MaximumValue(int no1, int no2) {
		if(no1 > no2){
			return no1;
		}	
		else{
			return no2;
		} 
	}
	
	private static int MinimumValue(int no1, int no2) {	
		if(no1 < no2){
			return no1;
		}
		else{
			return no2;
		} 
	}
	
	public  void buildIndexVer1(String docId, Map<String,Integer> lemmaTermFreqDict){
		long maxTf = 0;
		long docLength = 0;
		String maxFt = null;
		
		for (String term : lemmaTermFreqDict.keySet()) {
			int termFreq = lemmaTermFreqDict.get(term);
			docLength += termFreq;
			if (!(dictionaryStopWords.contains(term))) {
				if (termFreq > maxTf) {
					maxTf = termFreq;
					maxFt = term;
				}
				updatePostingValuesVer1(docId, term, lemmaTermFreqDict.get(term));
			}
		}
		Information entry = new Information(docLength, maxTf,maxFt);
		lemmaDocInfo.put(docId.trim(), entry);
	}



	private  void updatePostingValuesVer1(String docId, String term,
			Integer termFrequency) {
		Terms entry = null;
		if (dictLemma != null)
			entry = dictLemma.get(term);

		if (entry == null) {
			entry = new Terms(term, 0, 0,
					new LinkedList<PostingEntry>());
			entry.postingList = new LinkedList<PostingEntry>();

		}
		entry.postingList.add(new PostingEntry(docId, termFrequency));
		entry.documentFrequency += 1;
		entry.tFrequency += termFrequency;
		dictLemma.put(term, entry);

	}

	
	/* Building the index */
	public  void bulidingIndex(String docId, Map<String,Integer> termFreqDict){

		long maxTf = 0;
		long docLength = 0;
		String maxFt = null;
		for (String t : termFreqDict.keySet()) {
			int termFreq = termFreqDict.get(t);
			docLength += termFreq;
			
			if (!(dictionaryStopWords.contains(t))) {
				if (termFreq > maxTf) {
					maxTf = termFreq;
					maxFt = t;
				}
				changePostingListValues(docId, t, termFreqDict.get(t));
			}
		}
		Information entry = new Information(docLength, maxTf,maxFt);
		stemsDocInfo.put(docId, entry);
	}
	

	/* Changing the values and Updating the posting List Values */
	private  void changePostingListValues(String docId, String term, Integer termFrequency) {
		Terms e = null;
		if (dictStems != null){
			e = dictStems.get(term);
		}	
		
		if (e == null) {
			e = new Terms(term, 0, 0, new LinkedList<PostingEntry>());
			e.postingList = new LinkedList<PostingEntry>();
		}
		
		e.postingList.add(new PostingEntry(docId, termFrequency));
		e.documentFrequency = e.documentFrequency +  1;
		e.tFrequency = e.tFrequency + termFrequency;
		dictStems.put(term, e);
	}
	
	
	/* Remove the files from the directory*/
	public void removeFiles(String directoryPath){
		try{
			File file = new File(directoryPath);      
			String[] totalFiles;    
			if(file.isDirectory()){
				totalFiles = file.list();
				for (int i=0; i<totalFiles.length; i++) {
					File myFile = new File(file, totalFiles[i]); 
					myFile.delete();
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

	/* Get the file size in bytes*/
	public static double fileSize(String filePath){
		double fileSize=0;
		try{
			File file = new File(filePath);
			if(file.exists()){
				double fileSizeInBytes = file.length();
				fileSize=fileSizeInBytes;
			}
			else{
				throw new Exception();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return fileSize;
	}

	
	/* List of the Stop Words*/ 
	public  HashSet<String> stopWordList(String filePath){

		HashSet<String> stopWordsDict = new HashSet<String>();

		FileInputStream fileStream = null;
		DataInputStream dataStream = null;
		BufferedReader bufferedReader = null;

		try{
			File stopwordsFile = new File(filePath);
			fileStream = new FileInputStream(stopwordsFile);
			dataStream = new DataInputStream(fileStream);
			bufferedReader = new BufferedReader(new InputStreamReader(dataStream));
			String l = null;
			while((l=bufferedReader.readLine())!=null){
				stopWordsDict.add(l.toLowerCase().trim());
			}
		}
		catch(Exception e){
			try {
				fileStream.close();dataStream.close();bufferedReader.close();
			}
			catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return stopWordsDict;
	}


	/* Print the Index into the file*/
	public void printIndexMethod(Map<String, Terms> indexMap, String Filename) {
		BufferedWriter w = null;
		try {

			File textFile = new File(Filename);

			w = new BufferedWriter(new FileWriter(textFile, true));
			w.write("Key --> Term Frequency --> Doc Freq --> Postinglist");
			w.newLine();
			Set<String> key = indexMap.keySet();
			Iterator<String> i = key.iterator();
			while (i.hasNext()) {
				String keys = (String) i.next();
				Integer termFrequency = (Integer) indexMap.get(keys).tFrequency;
				Integer documentFrequency = (Integer) indexMap.get(keys).documentFrequency;
				w.write(keys + "-->" + termFrequency + "-->" + documentFrequency + "-->");
				Iterator ite = indexMap.get(keys).postingList.iterator();
				while (ite.hasNext()) {
					w.write(ite.next().toString() + ",");
				}
				w.newLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (w != null){
					w.close();
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}






