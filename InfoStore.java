import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.net.URL;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class InfoStore{
    private RecordManager rm;
    private HTree URLMap;
    private HTree PageInfo;
    private HTree KeywordIDMap;
    private HTree IDKeywordMap;
    private HTree IDPostingsMap;
    private Integer pageEntryCount = 0;

    InfoStore() throws IOException{
        // Initialize jdbm classes
        rm = RecordManagerFactory.createRecordManager("RM");
        long URLMap_ID = rm.getNamedObject("URLMap");
        long PageInfo_ID = rm.getNamedObject("PageInfo");
        long KeywordIDMap_ID = rm.getNamedObject("KeywordIDMap");
        long IDKeywordMap_ID = rm.getNamedObject("IDKeywordMap");
        long IDPostingsMap_ID = rm.getNamedObject("IDPostingsMap");

        // Create/Load existing records
        if (URLMap_ID != 0){
            URLMap = HTree.load(rm, URLMap_ID);
        } else {
            URLMap = HTree.createInstance(rm);
            rm.setNamedObject("URLMap", URLMap.getRecid());
        }

        if (PageInfo_ID != 0){
            PageInfo = HTree.load(rm, PageInfo_ID);
        } else {
            PageInfo = HTree.createInstance(rm);
            rm.setNamedObject("PageInfo", PageInfo.getRecid());
        }

        if (KeywordIDMap_ID != 0){
            KeywordIDMap = HTree.load(rm, KeywordIDMap_ID);
        } else {
            KeywordIDMap = HTree.createInstance(rm);
            rm.setNamedObject("KeywordIDMap", KeywordIDMap.getRecid());
        }

        if (IDKeywordMap_ID != 0){
            IDKeywordMap = HTree.load(rm, IDKeywordMap_ID);
        } else {
            IDKeywordMap = HTree.createInstance(rm);
            rm.setNamedObject("IDKeywordMap", IDKeywordMap.getRecid());
        }

        if (IDPostingsMap_ID != 0){
            IDPostingsMap = HTree.load(rm, IDPostingsMap_ID);
        } else {
            IDPostingsMap = HTree.createInstance(rm);
            rm.setNamedObject("IDPostingsMap", IDPostingsMap.getRecid());
        }
        
        // Initialize page count
        FastIterator ks = URLMap.keys();
        while (ks.next() != null) { pageEntryCount++; }
    }

    public void finalize() throws IOException{
        rm.commit();
        rm.close();
    }

    public Integer addPageEntry(PageStore ps) throws IOException{
        // Assign new id equal to current number of pages
        URLMap.put(ps.url.toString(), pageEntryCount);
        PageInfo.put(pageEntryCount, ps);

        pageEntryCount++;
        return pageEntryCount;
    }

    public Integer getURLID(URL url) throws IOException{
        return (Integer) URLMap.get(url.toString());
    }

    public PageStore getURLInfo(Integer id) throws IOException{
        return (PageStore) PageInfo.get(id);
    }

    public DocPostings getKeywordPosting(Integer id) throws IOException{
        return (DocPostings) IDPostingsMap.get(id);
    }

    public void writeDB(){
        try{
            PrintWriter pw = new PrintWriter("spider_result.txt");

            FastIterator pages = PageInfo.values();
            PageStore page;
            IntegerPair temp;
            PageStore tempPage;

            while( (page = (PageStore)pages.next())!=null)
            {
                pw.println("----------------");
                pw.println(page.title);
                pw.println(page.url);
                pw.println(page.lastModified.toString() + ", " + page.size);
                for (int i = 0; i < page.keyfreq.size(); i++){
                    if (i == 9){
                        break;
                    }
                    temp = page.keyfreq.get(i);
                    pw.print(IDKeywordMap.get(temp.a) + " " + temp.b + "; ");
                }
                pw.print('\n');
                for (int i = 0; i < page.childIDs.size(); i++){
                    if (i == 9){
                        break;
                    }
                    tempPage = (PageStore) PageInfo.get(page.childIDs.get(i));
                    pw.println(tempPage.url);
                }
                
                pw.println("----------------");
            }
            pw.close();
        }
        catch (FileNotFoundException e){
            System.err.println("Caught FileNotFoundException: " + e.getMessage());
        }
        catch (IOException e){
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }
}