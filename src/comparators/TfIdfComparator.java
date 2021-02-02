
package comparators;

import java.util.Comparator;

import documents.DocumentId;
import index.SearchEngine;

/**
 * Compare two documents in a search engine by tf-idf using a given term.
 * 
 * Using this comparator, the *larger* item should "come before" a smaller one
 * so that sort returns the list in descending (largest-to-smallest) order.
 * 
 * It breaks ties by using the lexicographic ordering of the document IDs (that
 * is, by using o1.id.compareTo(o2.id)).
 * 
 *
 */
public class TfIdfComparator implements Comparator<DocumentId>
{
	private final SearchEngine searchEngine;
	private final String term;

	public TfIdfComparator(SearchEngine searchEngine, String term)
	{
		this.searchEngine = searchEngine;
		this.term = term;
	}

	@Override
	public int compare(DocumentId o1, DocumentId o2)
	{
		double tf1 = searchEngine.tfIdf(o1, term);
		double tf2 = searchEngine.tfIdf(o2, term);
		if (tf1 > tf2)
		{
			return -1;
		}
		if (tf1 == tf2)
		{
			return o1.id.compareTo(o2.id);
		}
		return 1;
	}
}
