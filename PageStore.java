import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PageStore implements Serializable{
    public String URL;
    public String title = null;
    public LocalDateTime lastModified = null;
    public int size = 0;
    public ArrayList<IntegerPair> keyfreq = null;
    public ArrayList<Integer> childIDs = null;
    public ArrayList<Integer> parentIDs = null;

    PageStore(String URL, String title, LocalDateTime lm, int size, ArrayList<IntegerPair> keyfreq, ArrayList<Integer> childIDs, ArrayList<Integer> parentIDs){
        this.URL = URL;
        this.title = title;
        this.lastModified = lm;
        this.size = size;
        this.keyfreq = keyfreq;
        this.childIDs = childIDs;
        this.parentIDs = parentIDs;
    }
    PageStore(String URL){
        this.URL = URL;
    }
}
