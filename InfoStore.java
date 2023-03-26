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
    private Integer pageEntryCount = 0;

    InfoStore() throws IOException{
        // Initialize jdbm classes
        rm = RecordManagerFactory.createRecordManager("RM");
        long URLMapID = rm.getNamedObject("URLMap");
        long PageInfoID = rm.getNamedObject("PageInfo");
        long KeywordIDMapID = rm.getNamedObject("KeywordIDMap");
        long IDKeywordMapID = rm.getNamedObject("IDKeywordMap");

        // Create/Load existing records
        if (URLMapID != 0){
            URLMap = HTree.load(rm, URLMapID);
        } else {
            URLMap = HTree.createInstance(rm);
            rm.setNamedObject("URLMap", URLMap.getRecid());
        }

        if (PageInfoID != 0){
            PageInfo = HTree.load(rm, PageInfoID);
        } else {
            PageInfo = HTree.createInstance(rm);
            rm.setNamedObject("PageInfo", PageInfo.getRecid());
        }

        if (KeywordIDMapID != 0){
            KeywordIDMap = HTree.load(rm, KeywordIDMapID);
        } else {
            KeywordIDMap = HTree.createInstance(rm);
            rm.setNamedObject("KeywordIDMap", KeywordIDMap.getRecid());
        }

        if (IDKeywordMapID != 0){
            IDKeywordMap = HTree.load(rm, IDKeywordMapID);
        } else {
            IDKeywordMap = HTree.createInstance(rm);
            rm.setNamedObject("IDKeywordMap", IDKeywordMap.getRecid());
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