import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.net.URL;

public class PageStore implements Serializable{
    public URL url;
    public String title = null;
    public LocalDateTime indexTime = null;
    public LocalDateTime lastModified = null;
    public int size = 0;
    public ArrayList<IntegerPair> keyfreq = null;
    public ArrayList<Integer> childIDs = null;
    public ArrayList<Integer> parentIDs = null;
    public Boolean indexed = false;

    PageStore(URL url, String title, LocalDateTime indexTime, LocalDateTime lm, int size, ArrayList<IntegerPair> keyfreq, ArrayList<Integer> childIDs, ArrayList<Integer> parentIDs){
        this.url = url;
        this.title = title;
        this.indexTime = indexTime;
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
