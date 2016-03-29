import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Compression {
	public static Map<String, CDTerm> compressedIndexVer1(Map<String, Terms> uncompressedIndex) {
		Map<String, CDTerm> compressedIndex = new HashMap<>();
		for (String t : uncompressedIndex.keySet()) {
			Terms e = uncompressedIndex.get(t);
			List<PostingValueEntry> postingList = new ArrayList<>(e.postingList.size());
			for (PostingEntry postingEntry : e.postingList) {
				byte[] documentId = gammaCode(postingEntry.documentId);
				byte[] freq = deltaCode(postingEntry.frequency) ;
				postingList.add(new PostingValueEntry(documentId, freq));
			}
			
			int docFrequency = e.documentFrequency, termFrequency = e.tFrequency;
			CDTerm compressedEntry = new CDTerm(t, docFrequency, termFrequency, postingList);
			compressedIndex.put(t, compressedEntry);
		}
		
		return compressedIndex;
	}

  
	public static byte[] gammmaCode(String number) {
		String gammacode = getGammaCode(number);
		return convertToByteArray(gammacode);
	}
	

	public static byte[] gammaCode(int number) {
		String gammacode = getGammaCode(number);
		return convertToByteArray(gammacode);
	}

	
	public static Map<String, CDTerm> compressedIndexVer2(Map<String, Terms> uncIndex) {
		Map<String, CDTerm> cIndex = new HashMap<>();
		for (String t : uncIndex.keySet()) {
			Terms e = uncIndex.get(t);
			List<PostingValueEntry> postingList = new ArrayList<>(e.postingList.size());
			for (PostingEntry postingEntry : e.postingList) {
				byte[] docId = gammaCode(postingEntry.documentId);
				byte[] frequency = deltaCode(postingEntry.frequency);
				postingList.add(new PostingValueEntry(docId, frequency));
			}
			int docFrequency = e.documentFrequency;
			int termFrequency = e.tFrequency;
			CDTerm compressedEntry = new CDTerm(t,
					docFrequency, termFrequency, postingList);
			cIndex.put(t, compressedEntry);
		}
		return cIndex;

	}

	public static byte[] gammaCode(String number) {
		String binaryNum = stringToBinary(number);
		String gammaCode = getGammaCode(binaryNum.length());
		String offset = gammaCode.substring(1);
		return convertToByteArray(gammaCode.concat(offset));
	}

	public static String stringToBinary(String str ) {
		byte[] bytes = str.getBytes();
		StringBuilder binary = new StringBuilder();
		for (byte b : bytes)
		{
			binary.append(Integer.toBinaryString((int) b));

		}
		return binary.toString();        
	}

	public static byte[] deltaCode(String num) {
		String binaryNum = stringToBinary(num);
		String gammaCode = getGammaCode(binaryNum.length());
		String offset = gammaCode.substring(1);
		return convertToByteArray(gammaCode.concat(offset));

	}

	public static byte[] deltaCode(int i) {
		String binaryNum = Integer.toBinaryString(i);
		String gammaCode = getGammaCode(binaryNum.length());
		String offset = gammaCode.substring(1);
		return convertToByteArray(gammaCode.concat(offset));

	}

	private static String getGammaCode(String number) {
		String binaryNumber = stringToBinary(number);
		String offset = binaryNumber.substring(1);
		String unaryCode = getUnaryCode(offset.length());
		String gammacode = unaryCode + "0" + offset;
		return gammacode;
	}

	private static String getGammaCode(int number) {
		String binaryNumber = Integer.toBinaryString(number);
		String offset = binaryNumber.substring(1);
		String unaryCode = getUnaryCode(offset.length());
		String gammacode = unaryCode + "0" + offset;
		return gammacode;
	}

	public static String getUnaryCode(int length) {
		String unaryCode = "";
		for (int i = 0; i < length; i++) {
			unaryCode += "1";
		}
		return unaryCode;
	}



	private static byte[] convertToByteArray(String gammacode) {
		BitSet bitSet = new BitSet(gammacode.length());
		for (int i = 0; i < gammacode.length(); i++) {
			Boolean value = gammacode.charAt(i) == '1' ? true : false;
			bitSet.set(i, value);
		}
		return bitSet.toByteArray();
	}

	public  void printCompressedDict(Map<String,CDTerm> compressedIndex, String FileName) {
		try {
			RandomAccessFile newTextFile = new RandomAccessFile(FileName, "rw");
			ArrayList<String> pointer = new ArrayList<String>();
			Trie dictionary = new Trie();
			
			String originalPrifix = "";
			String oldPrefixValue = "";
			String suffixValue = "";
			ArrayList<String> kArr = new ArrayList<String>(compressedIndex.keySet());
			String key = kArr.get(0);
			if (key.length() > 2)
				dictionary.insert(key);
			
			String a = Long.toString(newTextFile.getFilePointer()+1);
			pointer.add(a);
			newTextFile.write((key.length()+key).getBytes());
			
			for (int l = 1; l < kArr.size(); l++) {
				if (key.length() > 2 && kArr.get(l).length() > 2 && dictionary != null) {
					if (dictionary.getSamePrefix(kArr.get(l)).length() > 0) {
						originalPrifix = dictionary.getSamePrefix(kArr.get(l));
						if (!oldPrefixValue.equals(originalPrifix) && oldPrefixValue.contains(originalPrifix)) {
							key = kArr.get(l);
							suffixValue = kArr.get(l).replace(originalPrifix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + originalPrifix + "*" + suffixValue).getBytes());
							dictionary = null;
						} else {
							suffixValue = kArr.get(l).replace(originalPrifix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((suffixValue.length()+"#" + suffixValue).getBytes());
						}
					} else {
						dictionary = null;
						key = kArr.get(l);
						dictionary = new Trie();
						dictionary.insert(key);
						if (l < (kArr.size() - 1) && dictionary.getSamePrefix(kArr.get(l + 1)).length() > 0) {
							originalPrifix = dictionary.getSamePrefix(kArr.get(l + 1));
							suffixValue = kArr.get(l).replace(originalPrifix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + originalPrifix + "*" + suffixValue).getBytes());
						} 
						else{
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + key).getBytes());}
					}
				} else if (key.length() > 3 && kArr.get(l).length() > 3 && dictionary == null) {
					dictionary = new Trie();
					dictionary.insert(key);
					if (dictionary.getSamePrefix(kArr.get(l)).length() > 0) {
						originalPrifix = dictionary.getSamePrefix(kArr.get(l));
						if (!oldPrefixValue.equals(originalPrifix) && oldPrefixValue.contains(originalPrifix)) {
							key = kArr.get(l);
							suffixValue = kArr.get(l).replace(originalPrifix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + originalPrifix+"*").getBytes());
							newTextFile.write((suffixValue.length()+"#" + suffixValue).getBytes());
							dictionary = null;
						} else {
							suffixValue = kArr.get(l).replace(originalPrifix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((suffixValue.length()+"#" + suffixValue).getBytes());
						}
					} else {
						dictionary = null;
						key = kArr.get(l);
						dictionary = new Trie();
						dictionary.insert(key);
						if (l < (kArr.size() - 1) && dictionary.getSamePrefix(kArr.get(l + 1)).length() > 0) {
							originalPrifix = dictionary.getSamePrefix(kArr.get(l + 1));
							suffixValue = kArr.get(l).replace(originalPrifix, "").trim();
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + originalPrifix+"*" ).getBytes());
							newTextFile.write(( suffixValue).getBytes());
						} else{
							String p = Long.toString(newTextFile.getFilePointer()+1);
							pointer.add(p);
							newTextFile.write((key.length() + key).getBytes());}
					}
				} else {
					key = kArr.get(l);
					String p = Long.toString(newTextFile.getFilePointer()+1);
					pointer.add(p);
					newTextFile.write((key.length() + key).getBytes());
					dictionary = null;
				}
				oldPrefixValue = originalPrifix;
			}

			newTextFile.write(System.getProperty("line.separator").getBytes());
			Iterator<String> postite = compressedIndex.keySet().iterator();
			int count=1;
			for (int j = 0; j < pointer.size(); j++) {
				if(count>8)
					count=1;
				if(count==1)
					newTextFile.write((pointer.get(j) + "-").getBytes());
				if (postite.hasNext()) {
					String pKey = (String) postite.next();
					newTextFile.write(("" + compressedIndex.get(pKey).docFrequency).getBytes());
					Iterator<PostingValueEntry> ite = compressedIndex.get(pKey).postingList.iterator();
					while (ite.hasNext()) {
						PostingValueEntry c = (PostingValueEntry) ite.next();
						newTextFile.write(c.documentId);
						newTextFile.writeBytes("-");
						newTextFile.write((c.freq));
					}


				}
				newTextFile.write(System.getProperty("line.separator").getBytes());
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	static class PostingValueEntry implements Serializable {
		private static final long serialVersionUID = 1L;
		byte[] documentId;
		byte[] freq;
		public PostingValueEntry(byte[] docID, byte[] frequency) {
			this.documentId = docID;
			this.freq = frequency;
		}

		public PostingValueEntry() {
		}

	}

	static class CDTerm implements Serializable {
		private static final long serialVersionUID = 1L;
		String term;
		int docFrequency;
		int termFrequency;
		List<PostingValueEntry> postingList;
		public CDTerm(String term, int docFrequency, int termFrequency, List<PostingValueEntry> postingList) {
			this.term = term;
			this.docFrequency = docFrequency;
			this.termFrequency = termFrequency;
			this.postingList = postingList;
		}
	}


	public static int byteArrayToInt(byte[] b) {
		if (b.length == 4)
			return b[0] << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff);
		else if (b.length == 2)
			return 0x00 << 24 | 0x00 << 16 | (b[0] & 0xff) << 8 | (b[1] & 0xff);

		return 0;
	}



	public static long getCompressedPostingListSize(List<PostingValueEntry> postingList) {
		long l = 0;
		for (PostingValueEntry pEntry : postingList) {
			l += pEntry.documentId.length + byteArrayToInt(pEntry.freq) ; 
		}
		return l;
	}
}
