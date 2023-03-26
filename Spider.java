import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.htmlparser.beans.FilterBean;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.NodeFilter;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;


public class Spider {
    private class URLParent{
        public URL url;
        public Integer parentID;
        URLParent(URL url, Integer id){
            this.url = url;
            id = parentID;
        }
    }
    private ArrayList<URLParent> toCrawl;
    private int indexCount = 0;
    private InfoStore info;
    private StopStem stopStem;

    Spider() throws IOException{
        info = new InfoStore();
        toCrawl = new ArrayList<URLParent>();
        stopStem = new StopStem();
    }

    private void crawlPages(String startURL, int maxIndexed) throws IOException, ParserException{
        URL entryURL = new URL(startURL);
        toCrawl.add(new URLParent(entryURL, null));

        while (indexCount < maxIndexed && !toCrawl.isEmpty()){
            URLParent temp = toCrawl.remove(0);
            URL url = temp.url;
            Integer parentID = temp.parentID;

            Integer id = info.getURLID(url);
            PageStore currentPage = info.getPageInfo(id);
            // Check if URL has already been Indexed and remains unmodified
            if (id != null && currentPage.indexed && currentPage.lastModified.isAfter(getModifiedDate(url))){
                // Add new parent pointer if required

                if (currentPage.parentIDs == null){
                    currentPage.parentIDs = new HashSet<Integer>();
                }
                if (!currentPage.parentIDs.contains(parentID)){
                    currentPage.parentIDs.add(parentID);
                }

                continue;
            }

            indexPage(url);
            indexCount++;
        }
    }

    private void indexPage(URL url) throws ParserException, IOException{
        // Get associated URL page or create new associated URL page
        Integer id = info.getURLID(url);
        PageStore indexPage = info.getPageInfo(id);
        if (indexPage == null){
            indexPage = new PageStore(url);
        }

        // First extract header information
        try{
            HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
            indexPage.size = getSize(httpCon);
            indexPage.lastModified = getModifiedDate(httpCon);
            httpCon.disconnect();
        } 
        catch (IOException e) {
            System.err.println(e.toString());
        }
        
        // Save meta information
        indexPage.title = getTitle(url);

        Integer indexPageID = info.addPageEntry(indexPage);

        ArrayList<URL> links = getLinksFromURL(url);
        ArrayList<String> text = getTextFromURL(url);

        indexChildPages(indexPageID, links);
        indexTitle(indexPageID, indexPage.title);
        indexBody(indexPageID, text);
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
                indexTitleKeyword(pageID, stopStem.stem(keyword), i);
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
        
        // Index keywords (stopwords are included in keyword position as still indicate break in phrase)
        for (int i = 0; i < tokens.size(); i++) {
            String keyword = tokens.get(i);
            if(!stopStem.isStopWord(keyword)){
                indexKeyword(pageID, stopStem.stem(keyword), i);
            }      
        }
    }

    private void indexTitleKeyword(Integer pageID, String keyword, Integer keypos) throws IOException{
        // Add keyword to mapping table
        Integer keywordID = info.getKeywordID(keyword);
        if (keywordID == null){
            keywordID = info.addKeywordEntry(keyword);
        }

        // Add posting
        info.getKeywordPostingTitle(keywordID).addPosting(pageID, keypos);

    }

    private void indexKeyword(Integer pageID, String keyword, Integer keypos) throws IOException{
        // Add keyword to mapping table
        Integer keywordID = info.getKeywordID(keyword);
        if (keywordID == null){
            keywordID = info.addKeywordEntry(keyword);
        }

        // Add posting
        info.getKeywordPostingBody(keywordID).addPosting(pageID, keypos);

        // Add to keyfreq (page store forward index)
        PageStore page = info.getPageInfo(pageID);
        Integer freq = page.keyfreq.get(keywordID);
        if (freq == null){
            page.keyfreq.put(keywordID, 1);
            return;
        }
        page.keyfreq.replace(keywordID, freq + 1);
    }

    private void indexChildPages(Integer parentID, ArrayList<URL> childLinks) throws IOException{
         // Assign list of child id's creating new page entries where required
         HashSet<Integer> childIDs = new HashSet<Integer>();
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
                 childPage.parentIDs = new HashSet<Integer>();
             }
             if (!childPage.parentIDs.contains(parentID)){
                 childPage.parentIDs.add(parentID);
             }
         }

         // Assign child id list to parent page
         PageStore parentPage = info.getPageInfo(parentID);
         parentPage.childIDs = childIDs;
    }

    private String getTitle(URL url){
        FilterBean fb = new FilterBean();
        fb.setFilters (new NodeFilter[] { new TagNameFilter ("TITLE") });
        fb.setURL(url.toString());
        return fb.getText();
    }

    private int getSize(HttpURLConnection httpCon){
        return httpCon.getContentLength();
    }
    
    private LocalDateTime getModifiedDate(HttpURLConnection httpCon){
        long date = httpCon.getLastModified();

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getDefault().toZoneId());
    }

    private LocalDateTime getModifiedDate(URL url) throws IOException{
        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
        long date = httpCon.getLastModified();
        httpCon.disconnect();

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getDefault().toZoneId());
    }

    private ArrayList<String> getTextFromURL(URL url) throws ParserException{
        // Get strings from webpage
        StringBean sb = new StringBean();
        sb.setLinks(false);
        sb.setCollapse(true);
        sb.setURL(url.toString());
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
    private ArrayList<URL> getLinksFromURL(URL url) throws ParserException
	{
		LinkBean lb = new LinkBean();
		lb.setURL(url.toString());
		URL[] urls = lb.getLinks();

		ArrayList<URL> links = new ArrayList<URL>();
		for (URL u : urls) {
			links.add(u);
		}

		return links;
	}

    public static void main(String[] args)
	{
		try
		{
			Spider spider = new Spider();

            if (args.length == 2){
                //spider.crawlPages(args[0], Integer.parseInt(args[1]));
            }
            else{
                System.out.println("Please provide arguments of form: 'Start URL' 'Max Page Count'");
                System.out.println(spider.getTextFromURL(new URL("http://www.cse.ust.hk")).toString());
            }
		}
		catch(IOException ex)
		{
			System.err.println(ex.toString());
		}
        catch (ParserException e){

        }
	}
}
