package index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import comparators.TfIdfComparator;
import documents.DocumentId;

/**
 * A simplified document indexer and search engine.
 * 
 * Documents are added to the engine one-by-one, and uniquely identified by a
 * DocumentId.
 *
 * Documents are internally represented as "terms", which are lowercased
 * versions of each word in the document.
 * 
 * Queries for terms are also made on the lowercased version of the term. Terms
 * are therefore case-insensitive.
 * 
 * Lookups for documents can be done by term, and the most relevant document(s)
 * to a specific term (as computed by tf-idf) can also be retrieved.
 *
 * See: - <https://en.wikipedia.org/wiki/Inverted_index> -
 * <https://en.wikipedia.org/wiki/Search_engine_(computing)> -
 * <https://en.wikipedia.org/wiki/Tf%E2%80%93idf>
 * 
 *
 */
public class SearchEngine
{
	Map<DocumentId, Map<String, Integer>> map = null;

	public SearchEngine()
	{
		map = new HashMap<DocumentId, Map<String, Integer>>();
	}

	/**
	 * Inserts a document into the search engine for later analysis and retrieval.
	 * 
	 * The document is uniquely identified by a documentId; attempts to re-insert
	 * the same document are ignored.
	 * 
	 * The document is supplied as a Reader; this method stores the document
	 * contents for later analysis and retrieval.
	 * 
	 * @param documentId
	 * @param reader
	 * @throws IOException iff the reader throws an exception
	 */
	public void addDocument(DocumentId documentId, Reader reader) throws IOException
	{
		Map<String, Integer> map2 = new HashMap<String, Integer>();
		map.put(documentId, map2);
		BufferedReader b = new BufferedReader(reader);
		String l = null;
		while ((l = b.readLine()) != null)
		{
			String[] words = l.toLowerCase().split("\\W+");
			for (String s : words)
			{
				Integer count = null;
				boolean hasKey = map2.containsKey(s);
				if (hasKey)
				{
					count = map2.get(s);
					count++;
				} else
				{
					count = 1;
				}
				map2.put(s, count);
			}
		}
	}

	/**
	 * Returns the set of DocumentIds contained within the search engine that
	 * contain a given term.
	 * 
	 * @param term
	 * @return the set of DocumentIds that contain a given term
	 */
	public Set<DocumentId> indexLookup(String term)
	{
		term = term.toLowerCase();
		Set<DocumentId> setDocs = new HashSet<DocumentId>();
		for (DocumentId d : map.keySet())
		{
			Map<String, Integer> map2 = map.get(d);
			if (map2.containsKey(term))
			{
				setDocs.add(d);
			}
		}
		return setDocs;
	}

	/**
	 * Returns the term frequency of a term in a particular document.
	 * 
	 * The term frequency is number of times the term appears in a document.
	 * 
	 * See
	 * 
	 * @param documentId
	 * @param term
	 * @return the term frequency of a term in a particular document
	 * @throws IllegalArgumentException if the documentId has not been added to the
	 *                                  engine
	 */
	public int termFrequency(DocumentId documentId, String term) throws IllegalArgumentException
	{
		term = term.toLowerCase();
		if (!map.keySet().contains(documentId))
		{
			throw new IllegalArgumentException();
		}
		Map<String, Integer> map2 = map.get(documentId);
		Integer freq = 0;
		if (map2.containsKey(term))
		{
			freq = map2.get(term);
		}
		return freq;
	}

	/**
	 * Returns the inverse document frequency of a term across all documents in the
	 * index.
	 * 
	 * For our purposes, IDF is defined as log ((1 + N) / (1 + M)) where N is the
	 * number of documents in total, and M is the number of documents where the term
	 * appears.
	 * 
	 * @param term
	 * @return the inverse document frequency of term
	 */
	public double inverseDocumentFrequency(String term)
	{
		Set<DocumentId> docs = indexLookup(term);
		int m = docs.size();
		int n = map.size();
		double val = (double) (1 + n) / (double) (1 + m);
		double foo = Math.log(val);
		return foo;
	}

	/**
	 * Returns the tfidf score of a particular term for a particular document.
	 * 
	 * tfidf is the product of term frequency and inverse document frequency for the
	 * given term and document.
	 * 
	 * @param documentId
	 * @param term
	 * @return the tfidf of the the term/document
	 * @throws IllegalArgumentException if the documentId has not been added to the
	 *                                  engine
	 */
	public double tfIdf(DocumentId documentId, String term) throws IllegalArgumentException
	{
		if (!map.keySet().contains(documentId))
		{
			throw new IllegalArgumentException();
		}
		int a = termFrequency(documentId, term);
		double b = inverseDocumentFrequency(term);
		return a * b;
	}

	/**
	 * Returns a sorted list of documents, most relevant to least relevant, for the
	 * given term.
	 * 
	 * A document with a larger tfidf score is more relevant than a document with a
	 * lower tfidf score.
	 * 
	 * Each document in the returned list must contain the term.
	 * 
	 * @param term
	 * @return a list of documents sorted in descending order by tfidf
	 */
	public List<DocumentId> relevanceLookup(String term)
	{
		List<DocumentId> termList = new ArrayList<DocumentId>();
		Set<DocumentId> setDocs = indexLookup(term);
		// List<DocumentId> sortedList = new ArrayList<DocumentId>();
		termList.addAll(setDocs);
		Collections.sort(termList, new TfIdfComparator(this, term));
		return termList;
	}
}
