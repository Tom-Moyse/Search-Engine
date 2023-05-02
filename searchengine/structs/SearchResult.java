package searchengine.structs;
import java.util.ArrayList;

public class SearchResult {
    public Integer[] documents;
    public Double[] scores;

    public SearchResult(ArrayList<Integer> docs, Double[] docScores){
        documents = new Integer[docs.size()];
        scores = new Double[docs.size()];

        for (int i = 0; i < documents.length; i++){
            documents[i] = docs.get(i);
            scores[i] = docScores[documents[i]];
        }
    }
}
