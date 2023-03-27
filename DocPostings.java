import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DocPostings implements Serializable{
    private HashMap<Integer, ArrayList<Integer>> postings;

    DocPostings(){
        postings = new HashMap<Integer, ArrayList<Integer>>();
    }

    // Insert a new posting
    public void addPosting(Integer docID, Integer position){
        ArrayList<Integer> positions = postings.get(docID);

        // Check if new document entry is require
        if (positions == null){
            ArrayList<Integer> pos = new ArrayList<Integer>();
            pos.add(position);
            postings.put(docID, pos);
            return;
        }

        positions.add(position);
    }

    public int getDocFreq(){
        return postings.size();
    }

    // Calculates max term frequency
    public int getTFmax(){
        int max = 0;
        for (ArrayList<Integer> pos : postings.values()) {
            if (pos.size() > max){
                max = pos.size();
            }
        }

        return max;
    }
}
