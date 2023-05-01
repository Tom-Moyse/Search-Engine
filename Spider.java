import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.htmlparser.beans.StringBean;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.Node;
import org.htmlparser.Parser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;


public class Spider {
    private ArrayList<URL> toCrawl;
    private int indexCount = 0;
    private InfoStore info;
    private StopStem stopStem;

    Spider() throws IOException{
        info = new InfoStore();
        toCrawl = new ArrayList<URL>();
        stopStem = new StopStem();

        // Restore index count and toCrawl list from db if applicable
        indexCount = info.getIndexedCount();

        if (indexCount == 0){
            toCrawl = new ArrayList<URL>();
        }
        else{
            toCrawl = info.getUnindexedList();
        }
    }

    private void newCrawl(String startURL, int maxIndexed) throws IOException, ParserException{
        // Set up parameters for new first time call
        if (indexCount != 0) {
            System.out.println("To start a new crawl please first remove 'RM.db' and 'RM.lg'");
            return;
        }
        
        toCrawl.add(normalizeURL(new URL(startURL)));
        crawlPages(maxIndexed);
    }

    private void crawlPages(int maxIndexed) throws IOException, ParserException{
        // Crawl as many pages as possible (without repeats)
        // until max is reached
        while (indexCount < maxIndexed && !toCrawl.isEmpty()){
            System.out.println("Processing Page: " + (indexCount + 1));
            URL url = toCrawl.remove(0);

            // Grab page id if exists
            Integer id = info.getURLID(url);
            PageStore currentPage = null;
            if (id != null){ currentPage = info.getPageInfo(id); }
            
            // Check if URL has already been Indexed and remains unmodified therefore skip
            if (id != null && currentPage.indexed != 0 && !getModifiedDate(url).isAfter(currentPage.lastModified)){
                continue;
            }

            // Otherwise index new page
            if (indexPage(url)){ indexCount++; } 
        }

        // Commit all changes to database
        System.out.println("Saving information to DB");
        info.finalize();
        System.out.println("Crawling Complete");
    }

    // Returns true if successfully indexed
    private boolean indexPage(URL url) throws ParserException, IOException{
        // Get associated URL page or create new associated URL page
        Integer id = info.getURLID(url);
        PageStore indexPage;
        boolean newentry;

        if (id != null) { 
            indexPage = info.getPageInfo(id);
            newentry = false;
        }
        else{
            indexPage = new PageStore(url);
            newentry = true;
        }


        // Open url connection via parser
        System.out.println("Fetching webpage: " + url.toString());
        Parser parser;
        try{
            parser = new Parser(url.toString());
        } catch (ParserException e) {
            System.out.println("Unable to index:  - Skipping page");
            indexPage.indexed = 2;
            return false;
        }
        
        // Extract header information
        HttpURLConnection httpCon = (HttpURLConnection) parser.getConnection();
        System.out.println("Fetching page info");
        indexPage.lastModified = getModifiedDate(httpCon);
        indexPage.size = getSize(httpCon, parser);
        parser.reset();

        // Save meta information
        indexPage.title = getTitleWithParser(parser);
        parser.reset();

        // Store page in db
        Integer indexPageID;
        if (newentry){ indexPageID = info.addPageEntry(indexPage); }
        else{ indexPageID = id; }
        
        // Fetch text and links from html
        System.out.println("Fetching page text");
        ArrayList<String> text = getTextWithParser(parser);
        parser.reset();
        System.out.println("Fetching page links");
        ArrayList<URL> links = getLinksWithParser(parser);

        // Drop http connection
        httpCon.disconnect();

        // Run db indexing methods for links and text content
        System.out.println("Indexing Page");
        indexChildPages(indexPageID, links);
        indexTitle(indexPageID, indexPage.title);
        indexBody(indexPageID, text);

        indexPage.indexed = 1;

        info.updatePageEntry(indexPageID, indexPage);
        // Add new pages to crawl list
        toCrawl.addAll(links);
        return true;
    }

    private void indexTitle(Integer pageID, String title) throws IOException{
        ArrayList<String> tokens = new ArrayList<String>();

        // Split title into individual words
        StringTokenizer sTokenizer = new StringTokenizer(title," ");
        while (sTokenizer.hasMoreElements()) {
            tokens.add(sTokenizer.nextToken());
        }

        // Index keywords (stopwords are included in keyword position as still indicate break in phrase)
        for (int i = 0; i < tokens.size(); i++) {
            String keyword = tokens.get(i);
            if(!stopStem.isStopWord(keyword)){
                String stemmedKeyword = stopStem.stem(keyword);
                if (stemmedKeyword != "" && !stopStem.isStopWord(stemmedKeyword)){
                    indexTitleKeyword(pageID, stemmedKeyword, i);
                }
            }      
        }
    }

    private void indexBody(Integer pageID, ArrayList<String> text) throws IOException{
        ArrayList<String> tokens = new ArrayList<String>();

        // Split body into individual words
        for (String line : text) {
            StringTokenizer sTokenizer = new StringTokenizer(line," ");
            while (sTokenizer.hasMoreElements()) {
                tokens.add(sTokenizer.nextToken());
            }
        }
        
        // Index keywords and remove stopwords
        int stopped = 0;
        for (int i = 0; i < tokens.size(); i++) {
            String keyword = tokens.get(i);
            if(!stopStem.isStopWord(keyword)){
                String stemmedKeyword = stopStem.stem(keyword);
                if (stemmedKeyword != "" && !stopStem.isStopWord(stemmedKeyword)){
                    indexKeyword(pageID, stemmedKeyword, i - stopped);
                }
                else { stopped++; }    
            }      
        }
    }

    private void indexTitleKeyword(Integer pageID, String keyword, Integer keypos) throws IOException{
        // Add keyword to mapping table
        Integer keywordID = info.getKeywordID(keyword);
        if (keywordID == null){
            keywordID = info.addKeywordEntry(keyword);
        }

        // Add posting or edit existing posting
        DocPostings dp = info.getKeywordPostingTitle(keywordID);
        if (dp == null){
            dp = new DocPostings();
            dp.addPosting(pageID, keypos);
            info.addDocPostingTitle(keywordID, dp);
        }
        else{
            dp.addPosting(pageID, keypos);
            info.updateDocPostingTitle(keywordID, dp);
        }
        
    }

    private void indexKeyword(Integer pageID, String keyword, Integer keypos) throws IOException{
        // Add keyword to mapping table
        Integer keywordID = info.getKeywordID(keyword);
        if (keywordID == null){
            keywordID = info.addKeywordEntry(keyword);
        }

        // Add posting
        DocPostings dp = info.getKeywordPostingBody(keywordID);

        if (dp == null){
            dp = new DocPostings();
            dp.addPosting(pageID, keypos);
            info.addDocPostingBody(keywordID, dp);
        }
        else{
            dp.addPosting(pageID, keypos);
            info.updateDocPostingBody(keywordID, dp);
        }

        // Add to keyfreq (page store forward index)
        PageStore page = info.getPageInfo(pageID);
        if (page.keyfreq == null){
            page.keyfreq = new HashMap<Integer, Integer>();
        }
        Integer freq = page.keyfreq.get(keywordID);
        if (freq == null){
            page.keyfreq.put(keywordID, 1);
            return;
        }
        page.keyfreq.replace(keywordID, freq + 1);
    }

    private void indexChildPages(Integer parentID, ArrayList<URL> childLinks) throws IOException{
        // Assign list of child id's creating new page entries where required
        ArrayList<Integer> childIDs = new ArrayList<Integer>();
        Integer tempID;
        PageStore childPage;

        // Iterate over all child pages
        for (URL link : childLinks) {
            tempID = info.getURLID(link);
 
            if (tempID == null){
                childPage = new PageStore(link);
                tempID = info.addPageEntry(childPage);
            }
 
            childIDs.add(tempID);
             
            // Add parent id to child page
            childPage = info.getPageInfo(tempID);
            if (childPage.parentIDs == null){
                childPage.parentIDs = new ArrayList<Integer>();
            }
            childPage.parentIDs.add(parentID);
            info.updatePageEntry(tempID, childPage);
        }

         
        // Assign child id list to parent page
        PageStore parentPage = info.getPageInfo(parentID);
        parentPage.childIDs = childIDs;
    }

    private String getTitleWithParser(Parser p) throws ParserException{
        // Use parser with filter to extract title from raw html
        NodeList nl = p.parse(null);
        NodeList titlelist = nl.extractAllNodesThatMatch(new TagNameFilter ("TITLE"), true);
        Node title = titlelist.elementAt(0);

        return title.toPlainTextString();
    }

    private int getSize(HttpURLConnection httpCon, Parser p) throws ParserException{
        // Evaluate html size, initially via http header but failing that count
        // characters from parsers extracted html string (slow)
        int size = httpCon.getContentLength();

        if (size != -1) { return size; }

        return p.parse(null).toHtml().length();
    }
    
    private LocalDateTime getModifiedDate(HttpURLConnection httpCon){
        // Reads modified date header via existing http connection
        long date = httpCon.getLastModified();
        if (date == 0){ date = httpCon.getDate(); }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getDefault().toZoneId());
    }


    private LocalDateTime getModifiedDate(URL url) throws IOException{
        // Reads modified date header via establishing new http connection
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        long date = httpCon.getLastModified();
        httpCon.disconnect();

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getDefault().toZoneId());
    }

    private ArrayList<String> getTextWithParser(Parser p) throws ParserException{
        // Get strings from webpage utilising string bean and existing parser connection
        StringBean sb = new StringBean();
        sb.setLinks(false);
        sb.setCollapse(true);
        
        p.visitAllNodesWith(sb);
        String longString = sb.getStrings();

        ArrayList<String> tokens = new ArrayList<String>();
        StringTokenizer sTokenizer = new StringTokenizer(longString,"\n");

		while (sTokenizer.hasMoreElements()) {
			tokens.add(sTokenizer.nextToken());
		}

        // Remove title from keywords
        tokens.remove(0);

		return tokens;
    }


    private ArrayList<URL> getLinksWithParser(Parser p) throws ParserException{   
        // Use link bean, with existing parser connection to extract links from page
		LinkBean lb = new LinkBean();
		lb.setURL(p.getURL());
		URL[] urls = lb.getLinks();

		ArrayList<URL> links = new ArrayList<URL>();
        for (URL url : urls) {
            links.add(normalizeURL(url));
        }

		return links;
	}
    
    private URL normalizeURL(URL url){
        // Basic URL normalization/fragment removal
        try{
            URI u = url.toURI();
            if( u.getFragment() != null ) { u = new URI( u.getScheme(), u.getSchemeSpecificPart(), null ); }
            url = u.toURL();
        } catch (Exception e){ System.out.println("Failed to normalize url, using default"); }
        return url;
    }

    public static void main(String[] args)
	{
		try
		{
			Spider spider = new Spider();
            
            if (args.length == 1){
                spider.crawlPages(Integer.parseInt(args[0]));
            }
            else if (args.length == 2){
                spider.newCrawl(args[0], Integer.parseInt(args[1]));
            }
            else{
                System.out.println("Please provide arguments of form: 'Start URL' 'Max Page Count' to start a new crawl");
                System.out.println("Please provide arguments of form: 'Max Page Count' to continue an existing crawl");
            }
		}
		catch(IOException ex)
		{
			System.err.println(ex.toString());
		}
        catch (ParserException e){
            System.err.println(e.toString());
        }
	}
}
