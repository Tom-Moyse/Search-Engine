import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class PageStore implements Serializable{
    public String URL;
    public String title;
    public LocalDateTime lastModified;
    public int size;
    public ArrayList<IntegerPair> keyfreq;
    public ArrayList<Integer> childIDs;
    public ArrayList<Integer> parentIDs;

    PageStore(String URL, String title, LocalDateTime lm, int size, ArrayList<IntegerPair> keyfreq, ArrayList<Integer> childIDs, ArrayList<Integer> parentIDs){
        this.URL = URL;
        this.title = title;
        this.lastModified = lm;
        this.size = size;
        this.keyfreq = keyfreq;
        this.childIDs = childIDs;
        this.parentIDs = parentIDs;
    }
}
