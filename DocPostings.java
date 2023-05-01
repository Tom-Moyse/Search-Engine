import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class DocPostings implements Serializable{
    private HashMap<Integer, ArrayList<Integer>> postings;
    private int tfmax = 0;

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
            if (tfmax == 0) { tfmax = 1; } 
            return;
        }

        positions.add(position);
        if (positions.size() > tfmax) { tfmax = positions.size(); }
    }

    public int getDocFreq(){
        return postings.size();
    }

    public int getTF(Integer docID){
        return postings.get(docID).size();
    }

    // Calculates max term frequency
    public int getTFmax(){
        return tfmax;
    }

    public Integer[] getDocumentIDs(){
        return postings.keySet().toArray(new Integer[0]);
    }

    public ArrayList<Integer> getPositionList(Integer docID){
        return postings.get(docID);
    }
}
