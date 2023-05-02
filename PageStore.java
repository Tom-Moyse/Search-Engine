import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.ArrayList;
import java.net.URL;

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
    PageStore(URL url){
        this.url = url;
    }
}
