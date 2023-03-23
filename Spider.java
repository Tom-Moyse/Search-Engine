import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.htmlparser.beans.StringBean;
import org.htmlparser.util.ParserException;
import org.htmlparser.beans.LinkBean;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.net.MalformedURLException;

public class Spider {
    private class URLParent{
        public String URL;
        public Integer parentID;
        URLParent(String url, Integer id){
            URL = url;
            id = parentID;
        }
    }
    private ArrayList<URLParent> toCrawl;
    private int indexCount = 0;
    private InfoStore info;

    Spider() throws IOException{
        info = new InfoStore();
        toCrawl = new ArrayList<URLParent>();
    }

    private void crawlPages(String startURL, int maxIndexed) throws IOException{
        toCrawl.add(new URLParent(startURL, maxIndexed));

        while (indexCount < maxIndexed && !toCrawl.isEmpty()){
            URLParent temp = toCrawl.remove(0);
            String url = temp.URL;
            Integer parentID = temp.parentID;

            Integer id = info.getURLID(url);
            PageStore currentPage = info.getURLInfo(id);
            // Check if URL has already been Indexed and remains unmodified
            if (id != null && currentPage.lastModified.isAfter(getModifiedDate(url))){
                // Add new parent pointer if required
                if (!currentPage.parentIDs.contains(parentID)){
                    currentPage.parentIDs.add(parentID);
                }

                continue;
            }

            indexPage(url);
            indexCount++;
        }
    }

    private void indexPage(String url){

    }

    private LocalDateTime getModifiedDate(String url) throws MalformedURLException, IOException{
        URL u = new URL(url);
        HttpURLConnection httpCon = (HttpURLConnection) u.openConnection();
    
        long date = httpCon.getLastModified();
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), TimeZone.getDefault().toZoneId());
    }

    private ArrayList<String> getWordsFromURL(String url) throws ParserException{
        StringBean sb = new StringBean();
        sb.setLinks(false);
        sb.setCollapse(true);
        sb.setURL(url);
        String longString = sb.getStrings();

        StringTokenizer sTokenizer = new StringTokenizer(longString,"\n ");
		ArrayList<String> tokens = new ArrayList<String>();
		while (sTokenizer.hasMoreElements()) {
			tokens.add(sTokenizer.nextToken());
		}

		return tokens;
    }
    private ArrayList<String> getLinksFromURL(String url) throws ParserException
	{
		LinkBean lb = new LinkBean();
		lb.setURL(url);
		URL[] urls = lb.getLinks();

		ArrayList<String> links = new ArrayList<String>();
		for (URL u : urls) {
			links.add(u.toString());
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
            }
		}
		catch(IOException ex)
		{
			System.err.println(ex.toString());
		}

	}
}
