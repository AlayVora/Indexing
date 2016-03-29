import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class Lemmatizer {
	
	static Lemmatizer mLemma;
	Properties props;
	private Map<String, Integer> lDict;
	StanfordCoreNLP pipeline;
	

	Lemmatizer() {
		props = new Properties();
		props.put("annotators", "tokenize,ssplit, pos,  lemma");
		pipeline = new StanfordCoreNLP(props, false);
	}

	public static Lemmatizer getInstance() {
		if (mLemma == null) {
			mLemma = new Lemmatizer();
		}
		return mLemma;

	}

	public String getLemma(String text) {
		String lemma = "";
		Annotation document = pipeline.process(text);
		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				lemma += token.get(LemmaAnnotation.class) + " ";
			}
		}
		return lemma;
	}

	public Map<String, Integer> buildLemmaDictionary(File file) {
		try {
			lDict = new HashMap<String, Integer>();
			FileInputStream fileInputStream = null;
			DataInputStream dataInputStream = null;
			BufferedReader bufferedReader = null;
			
			if (file.isFile()) {				
				fileInputStream = new FileInputStream(file);
				dataInputStream = new DataInputStream(fileInputStream);
				bufferedReader = new BufferedReader(new InputStreamReader(dataInputStream));
				String strline = null;			
			
				
				while((strline=bufferedReader.readLine())!=null){

					StringTokenizer tokenizer = new StringTokenizer(strline," ");

					while(tokenizer.hasMoreTokens()){

						String currString = tokenizer.nextToken();
						currString =currString.replaceAll("<[^>]+>", "").toLowerCase();
						currString = currString.replaceAll("[0-9]","");
						currString = currString.replaceAll("[^\\w\\s-'.!:;]+", "");
							currString = currString.replaceAll("['.`]+", "");
							tokenizeAndaddToLemmaDictionary(currString,lDict);		

					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lDict;
	}

	public static void tokenizeAndaddToLemmaDictionary(String tokenstring,
			Map<String, Integer> dicname) {

		if (tokenstring.trim().length() > 0) {

			// handle the cases
			if (tokenstring.endsWith("'s")) {
				tokenstring = tokenstring.replace("'s", "").trim();
				 if (tokenstring.contains("-")) {
					String[] newTokens = tokenstring.split("-");
					for (String newToken : newTokens) {
						addToLemmaDic(newToken, dicname);
					}
				} else if (tokenstring.contains("_")) {
					String[] newTokens = tokenstring.split("_");
					for (String newToken : newTokens) {
						addToLemmaDic(newToken, dicname);
					}
				}
				else{
					addToLemmaDic(tokenstring, dicname);
				}
			} else if (tokenstring.contains("-")) {
				String[] newTokens = tokenstring.split("-");
				for (String newToken : newTokens) {
					addToLemmaDic(newToken, dicname);
				}
			} else if (tokenstring.contains("_")) {
				String[] newTokens = tokenstring.split("_");
				for (String newToken : newTokens) {
					addToLemmaDic(newToken, dicname);
				}
			} else {
				addToLemmaDic(tokenstring, dicname);
			}
		}

	}

	public static void addToLemmaDic(String strToken, Map<String, Integer> dicname) {
		String lemma = Lemmatizer.getInstance()
				.getLemma(strToken);
		
		if (lemma.trim().length() > 0) {
			lemma=lemma.replace(" ", "").toLowerCase();
			if (dicname.containsKey(lemma)) {
				dicname.put(lemma, dicname.get(lemma) + 1);
			} else
				dicname.put(lemma, 1);
		}

	}
	/*
	 * Test Class for Lemmatizer
	 */
	public static void main(String[] args) {
		System.out.println(Lemmatizer.getInstance().getLemma("Lemmatizer Testing"));
	}

}
