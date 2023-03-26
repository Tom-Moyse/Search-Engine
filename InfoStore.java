import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
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
    private Integer keywordCount = 0;

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
        FastIterator ks1 = URLMap.keys();
        while (ks1.next() != null) { pageEntryCount++; }

        // Initialize keyword count
        FastIterator ks2 = KeywordIDMap.keys();
        while (ks2.next() != null) { keywordCount++; }
    }

    public void finalize() throws IOException{
        rm.commit();
        rm.close();
    }

    public Integer addPageEntry(PageStore ps) throws IOException{
        // Assign new id equal to current number of pages
        URLMap.put(ps.url.toString(), pageEntryCount);
        PageInfo.put(pageEntryCount, ps);

        return pageEntryCount++;
    }

    public Integer addKeywordEntry(String kw) throws IOException{
        // Assign new id equal to current number of keywords
        KeywordIDMap.put(kw, keywordCount);
        IDKeywordMap.put(keywordCount, kw);

        return keywordCount++;
    }

    public Integer getURLID(URL url) throws IOException{
        return (Integer) URLMap.get(url.toString());
    }

    public PageStore getPageInfo(Integer id) throws IOException{
        return (PageStore) PageInfo.get(id);
    }

    public Integer getKeywordID(String keyword) throws IOException{
        return (Integer) KeywordIDMap.get(keyword);
    }

    public DocPostings getKeywordPosting(Integer id) throws IOException{
        return (DocPostings) IDPostingsMap.get(id);
    }

    public void writeDB(){
        try{
            PrintWriter pw = new PrintWriter("spider_result.txt");

            FastIterator pages = PageInfo.values();
            PageStore page;
            PageStore tempPage;

            while( (page = (PageStore)pages.next())!=null)
            {
                pw.println("----------------");
                pw.println(page.title);
                pw.println(page.url);
                pw.println(page.lastModified.toString() + ", " + page.size);

                int i = 0;
                for (Map.Entry<Integer, Integer> entry : page.keyfreq.entrySet()) {
                    if (i++ == 9) { break; }
                    pw.print(IDKeywordMap.get(entry.getKey()) + " " + entry.getValue() + "; ");
                }
                pw.print('\n');
                
                i = 0;
                for (Integer id : page.childIDs) {
                    if (i++ == 9) { break; }
                    tempPage = (PageStore) PageInfo.get(id);
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