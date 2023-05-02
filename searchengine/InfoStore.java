package searchengine;
import java.io.IOException;
import java.util.ArrayList;
import java.net.URL;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;
import searchengine.structs.PageStore;
import searchengine.structs.DocPostings;

public class InfoStore{
    private RecordManager rm;
    private HTree URLMap;
    private HTree PageInfo;
    private HTree KeywordIDMap;
    private HTree IDKeywordMap;
    private HTree IDPostingsMapBody;
    private HTree IDPostingsMapTitle;
    private Integer pageEntryCount = 0;
    private Integer keywordCount = 0;

    InfoStore() throws IOException{
        // Initialize jdbm classes
        rm = RecordManagerFactory.createRecordManager("searchengine/files/RM");
        long URLMap_ID = rm.getNamedObject("URLMap");
        long PageInfo_ID = rm.getNamedObject("PageInfo");
        long KeywordIDMap_ID = rm.getNamedObject("KeywordIDMap");
        long IDKeywordMap_ID = rm.getNamedObject("IDKeywordMap");
        long IDPostingsMapBody_ID = rm.getNamedObject("IDPostingsMapBody");
        long IDPostingsMapTitle_ID = rm.getNamedObject("IDPostingsMapTitle");

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

        if (IDPostingsMapBody_ID != 0){
            IDPostingsMapBody = HTree.load(rm, IDPostingsMapBody_ID);
        } else {
            IDPostingsMapBody = HTree.createInstance(rm);
            rm.setNamedObject("IDPostingsMapBody", IDPostingsMapBody.getRecid());
        }

        if (IDPostingsMapTitle_ID != 0){
            IDPostingsMapTitle = HTree.load(rm, IDPostingsMapTitle_ID);
        } else {
            IDPostingsMapTitle = HTree.createInstance(rm);
            rm.setNamedObject("IDPostingsMapTitle", IDPostingsMapTitle.getRecid());
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

    public void addDocPostingBody(Integer id, DocPostings dp) throws IOException{
        IDPostingsMapBody.put(id, dp);
    }

    public void addDocPostingTitle(Integer id, DocPostings dp) throws IOException{
        IDPostingsMapTitle.put(id, dp);
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

    public DocPostings getKeywordPostingBody(Integer id) throws IOException{
        return (DocPostings) IDPostingsMapBody.get(id);
    }

    public DocPostings getKeywordPostingTitle(Integer id) throws IOException{
        return (DocPostings) IDPostingsMapTitle.get(id);
    }

    public void updatePageEntry(Integer key, PageStore ps) throws IOException{
        PageInfo.remove(key);
        PageInfo.put(key, ps);
    }

    public void updateDocPostingBody(Integer id, DocPostings dp) throws IOException{
        IDPostingsMapBody.remove(id);
        IDPostingsMapBody.put(id, dp);
    }

    public void updateDocPostingTitle(Integer id, DocPostings dp) throws IOException{
        IDPostingsMapTitle.remove(id);
        IDPostingsMapTitle.put(id, dp);
    }

    public int getIndexedCount() throws IOException{
        FastIterator pages = PageInfo.values();
        PageStore page;
        int indexCount = 0;

        while( (page = (PageStore)pages.next()) != null ) {
            if (page.indexed == 1) { indexCount++; }
        }

        return indexCount;
    }

    public ArrayList<URL> getUnindexedList() throws IOException{
        FastIterator pages = PageInfo.values();
        PageStore page;
        ArrayList<URL> toIndex = new ArrayList<URL>(pageEntryCount);

        while( (page = (PageStore)pages.next()) != null ) {
            if (page.indexed == 0) { toIndex.add(page.url); }
        }

        return toIndex;
    }
}