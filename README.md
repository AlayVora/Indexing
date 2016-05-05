#Indexing

##Need to use Java Version 1.7 as Jar files are compatible with java version 1.7

##Commands to Run
+ javac Information.java
+ javac -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models.jar Lemmatizer.java
+ javac Stemmer.java
+ javac TrieDictionary.java
+ javac Compression.java
+ javac ListComparator.java
+ javac -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models.jar BuildIndexClass.java

BuildIndexClass.java is the main file to run.

##Use following command to run it:
java -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models.jar   BuildIndexClass <Carnfield Dataset Path> <StopWord file Path> <Output Directory Path>

For example : java -classpath .:/usr/local/corenlp341/stanford-corenlp-3.4.1.jar:/usr/local/corenlp341/stanford-corenlp-3.4.1-models .jar BuildIndexClass /people/cs/s/sanda/cs6322/Cranfield/ /people/cs/s/sanda/cs6322/resourcesIR/stopwords ../HW2_1

