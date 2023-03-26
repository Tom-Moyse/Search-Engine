import java.io.Serializable;
import java.util.ArrayList;

public class DocPostings implements Serializable{
    private class Posting implements Serializable{
        public Integer docID;
        public ArrayList<Integer> positions;

        Posting(Integer docID, ArrayList<Integer> positions){
            this.docID = docID;
            this.positions = positions;
        }
    }
    
    private ArrayList<Posting> postings;

    DocPostings(){
        postings = new ArrayList<Posting>();
    }

    public void addPosting(Integer docID, ArrayList<Integer> positions){
        postings.add(new Posting(docID, positions));
    }

    public Posting getDocPosting(Integer docID){
        for (Posting p : postings) {
            if (p.docID.equals(docID)){ return p; }
        }
        return null;
    }

    public int getDocFreq(){
        return postings.size();
    }
    
    public int getTFmax(){
        int max = 0;
        for (Posting p : postings) {
            if (p.positions.size() > max){
                max = p.positions.size();
            }
        }

        return max;
    }
}
