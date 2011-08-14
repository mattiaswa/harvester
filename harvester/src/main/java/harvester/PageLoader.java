/**
 * 
 */
package harvester;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * @author mattias
 * 
 */
public class PageLoader {
	private final static int NUMBER_OF_PAGES_TO_FETCH = 10;

	private final static Logger logger = LoggerFactory
			.getLogger(PageLoader.class);

	private SearchProvider searchProvider;

	private final PageLoaderReporter reporter;

	public PageLoader() {
		this(PageLoaderReporter.VOID_REPORTER);
	}

	public PageLoader(PageLoaderReporter reporter) {
		this.reporter = reporter;
	}

	public Iterable<HtmlPageData> getPagesForKeyword(String keyword)
			throws HarvesterException {
		List<String> urls = collectUrlsFromSearchResult(keyword);
		return loadPagesFromUrls(urls);
	}

	private List<String> collectUrlsFromSearchResult(String keyword) {
		List<String> collectedUrls = new ArrayList<String>();
		for (int pageStart = 1; collectedUrls.size() < NUMBER_OF_PAGES_TO_FETCH
				&& (pageStart > 0);) {

			logger.info("Searching for keyword {}, starting at {}", keyword,
					pageStart);
			JsonObject searchResult = searchProvider
					.searchForKeywordStartingAtSpecifiedIndex(keyword,
							pageStart);

			collectedUrls.addAll(Arrays
					.asList(GoogleSearchResultExtractorUtility
							.findUrls(searchResult)));

			pageStart = GoogleSearchResultExtractorUtility
					.extractNextStartIndex(searchResult);
		}
		return collectedUrls.subList(0,
				Math.min(collectedUrls.size(), NUMBER_OF_PAGES_TO_FETCH));
	}

	private Iterable<HtmlPageData> loadPagesFromUrls(List<String> urls) {
		List<HtmlPageData> pages = new ArrayList<HtmlPageData>();
		for (String url : urls) {
			HtmlPageData page = loadPageFromUrl(url);
			if (page != null) {
				pages.add(page);
			}
		}

		return pages;
	}

	private HtmlPageData loadPageFromUrl(String urlString) {
		InputStream in = null;
		try {
			in = createPageInputStream(urlString);
			if (in == null) {
				return null;
			}

			logger.info("Loading page {}", urlString);
			reporter.progress(String.format("Loading data from %s", urlString));

			return createHtmlFromPageContent(IOUtils.toString(in));
		} catch (IOException e) {
			logger.error("Could not load page for url " + urlString, e);
			reporter.progress(String.format(
					"Could not load data from %s, skipping (reason=%s)",
					urlString, e.getMessage()));
			return null;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private HtmlPageData createHtmlFromPageContent(String pageAsString) {
		HtmlElementFinder finder = new HtmlElementFinder(pageAsString);

		return new HtmlPageData(finder.getTitle(), finder.getMetaValue(),
				finder.getBodyValues());
	}

	protected InputStream createPageInputStream(String urlString)
			throws IOException {
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		InputStream in = conn.getInputStream();

		return in;
	}

	public void setSearchProvider(SearchProvider sp) {
		this.searchProvider = sp;
	}
}
