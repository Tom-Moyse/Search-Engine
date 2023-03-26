import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.net.URL;

public class PageStore implements Serializable{
    public URL url;
    public String title = null;
    public LocalDateTime lastModified = null;
    public int size = 0;
    public HashMap<Integer, Integer> keyfreq = null;
    public HashSet<Integer> childIDs = null;
    public HashSet<Integer> parentIDs = null;
    public Boolean indexed = false;

    PageStore(URL url, String title, LocalDateTime lm, int size, HashMap<Integer, Integer> keyfreq, HashSet<Integer> childIDs, HashSet<Integer> parentIDs){
        this.url = url;
        this.title = title;
        this.lastModified = lm;
        this.size = size;
        this.keyfreq = keyfreq;
        this.childIDs = childIDs;
        this.parentIDs = parentIDs;
    }
    PageStore(URL url){
        this.url = url;
        this.indexed = false;
    }
}
