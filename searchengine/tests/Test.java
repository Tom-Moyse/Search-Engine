package searchengine.tests;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;

import searchengine.structs.PageStore;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

public class Test {
    public static void main(String[] args)
	{
		try
		{
            // Initialize DB
            RecordManager rm = RecordManagerFactory.createRecordManager("RM");
            long PageInfo_ID = rm.getNamedObject("PageInfo");
            long IDKeywordMap_ID = rm.getNamedObject("IDKeywordMap");

            HTree PageInfo;
            HTree IDKeywordMap;

            if (PageInfo_ID != 0){
                PageInfo = HTree.load(rm, PageInfo_ID);
            }else{
                System.out.println("Please run spider before test program");
                return;
            }
            if (IDKeywordMap_ID != 0){
                IDKeywordMap = HTree.load(rm, IDKeywordMap_ID);
            }else{
                return;
            }

            // Initialize File print writer
            PrintWriter pw = new PrintWriter("spider_result.txt");

            FastIterator pages = PageInfo.values();
            PageStore page;
            PageStore tempPage;

            while( (page = (PageStore)pages.next()) != null )
            {
                if (page.indexed != 1){
                    continue;
                }
                pw.println("----------------");
                pw.println(page.title);
                pw.println(page.url);
                if (page.lastModified != null){    
                    pw.println(page.lastModified.toString() + ", " + page.size);
                }
                else{
                    System.out.println("Null last modified detected");
                }
                

                int i = 0;

                if (page.keyfreqbody != null){

                    // for (Map.Entry<Integer, Integer> entry : page.keyfreqbody.entrySet()) {
                    //     if (i++ == 9) { break; }
                    //     pw.print(IDKeywordMap.get(entry.getKey()) + " " + entry.getValue() + "; ");
                    // }
                    Map<Integer, Integer> sortedMap = 
                        page.keyfreqbody.entrySet().stream()
                        .sorted(Entry.<Integer, Integer>comparingByValue().reversed())
                        .collect(Collectors.toMap(Entry<Integer, Integer>::getKey, Entry<Integer, Integer>::getValue,
                                                 (e1, e2) -> e1, LinkedHashMap::new));
                    int count = 0;
                    for (Map.Entry<Integer, Integer> entry: sortedMap.entrySet()){
                        if (count > 50){ break; }
                        pw.print(IDKeywordMap.get(entry.getKey()));
                        pw.print(entry.getValue());
                        if (count != 50){
                            pw.print(", ");
                        }
                        count++;
                    }
                }
                else{
                    System.out.println("Null keyfreq detected");
                }
                
                
                pw.print('\n');
                
                i = 0;
                if (page.childIDs != null){
                    for (Integer id : page.childIDs) {
                        if (i++ == 9) { break; }
                        tempPage = (PageStore) PageInfo.get(id);
                        pw.println(tempPage.url);
                    }
                }
                else{
                    System.out.println("Null childids detected");
                }
                
                pw.println("----------------");
            }
            pw.close();

            System.out.println("Output complete, please see 'spider_result.txt' for details");
		}
		catch(IOException ex)
		{
			System.err.println(ex.toString());
		}
	}
}
