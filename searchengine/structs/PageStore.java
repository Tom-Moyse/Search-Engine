package searchengine.structs;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PageStore implements Serializable{
    public URL url;
    public String title = null;
    public LocalDateTime lastModified = null;
    public int size = 0;
    public HashMap<Integer, Integer> keyfreqbody = null;
    public HashMap<Integer, Integer> keyfreqtitle = null;
    public ArrayList<Integer> childIDs = null;
    public ArrayList<Integer> parentIDs = null;
    // 0 represents unindexed, 1 is successfully indexed, 2 is failed to index
    public byte indexed = 0;

    // Only url required when specifying new object
    // Other public parameters can be set incrementally
    // No - I don't want to make them private and add 20 interface methods
    public PageStore(URL url){
        this.url = url;
        Map<Integer, Integer> sortedMap = 
                        keyfreqbody.entrySet().stream()
                        .sorted(Entry.comparingByValue())
                        .collect(Collectors.toMap(Entry<Integer, Integer>::getKey, Entry<Integer, Integer>::getValue,
                                                 (e1, e2) -> e1, HashMap::new));
    }
}
