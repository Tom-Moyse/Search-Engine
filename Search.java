import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Search {
    private InfoStore info;

    Search() throws IOException{
        info = new InfoStore();
    }

    public void EvaluateQuery(String q) throws IOException{
        ArrayList<String> terms = new ArrayList<String>();
        StopStem stemmer = new StopStem();

        // Split title into individual words
        StringTokenizer sTokenizer = new StringTokenizer(q," ");
        String temp;
        while (sTokenizer.hasMoreElements()) {
            temp = sTokenizer.nextToken();
            if (stemmer.isStopWord(temp)){
                System.out.println("Removing stop word: " + temp + " from query");
                continue;
            }
            terms.add(stemmer.stem(temp));
        }

        Integer keywordID;
        DocPostings dp;
        // Should check this works to get N
        double[] scores = new double[info.getIndexedCount()];

        // Sum partial similarities
        for (String term : terms) {
            if ((keywordID = info.getKeywordID(term)) == null){
                System.out.println("Stemmed keyword :" + term + " not found");
                continue;
            }

            dp = info.getKeywordPostingBody(keywordID);
            System.out.println(Arrays.toString(dp.getDocumentIDs()));
            for (Integer docid : dp.getDocumentIDs()) {
                scores[docid] += computeSimilarity(docid, keywordID);
            }
        }

        // Normalize for cosine similarity
        PageStore page;
        double queryLength = Math.sqrt(terms.size());
        double docLength;
        int count;
        for (int i = 0; i < scores.length; i++){
            if (scores[i] == 0){ continue; }
            count = 0;

            page = info.getPageInfo((Integer) i);
            for (Integer f : page.keyfreq.values()) {
                count += f;
            }

            docLength = Math.sqrt(count);

            scores[i] *= 1 / (queryLength * docLength);
        }

        System.out.println(scores.toString());
    }

    // Computing partial score similarity reduces to returning keyword frequency in document
    private int computeSimilarity(Integer docID, Integer keywordID) throws IOException{
        PageStore page = info.getPageInfo(docID);

        return page.keyfreq.get(keywordID);
    }
}
