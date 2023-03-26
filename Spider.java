import java.util.ArrayList;
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
            PageStore currentPage = info.getURLInfo(id);
            // Check if URL has already been Indexed and remains unmodified
            if (id != null && currentPage.indexed && currentPage.lastModified.isAfter(getModifiedDate(url))){
                // Add new parent pointer if required

                if (currentPage.parentIDs == null){
                    currentPage.parentIDs = new ArrayList<Integer>();
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
        PageStore indexPage = info.getURLInfo(id);
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
        ArrayList<String> words = getWordsFromURL(url);

       indexChildPages(indexPageID, links);

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
             childPage = info.getURLInfo(tempID);
             if (childPage.parentIDs == null){
                 childPage.parentIDs = new ArrayList<Integer>();
             }
             if (!childPage.parentIDs.contains(parentID)){
                 childPage.parentIDs.add(parentID);
             }
         }

         // Assign child id list to parent page
         PageStore parentPage = info.getURLInfo(parentID);
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

    private ArrayList<String> getWordsFromURL(URL url) throws ParserException{
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
                System.out.println(spider.getWordsFromURL(new URL("http://www.cse.ust.hk")).toString());
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
