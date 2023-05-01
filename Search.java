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
                System.out.println("Stemmed keyword: " + term + " not found");
                continue;
            }

            dp = info.getKeywordPostingBody(keywordID);
            for (Integer docid : dp.getDocumentIDs()) {
                scores[docid] += computeSimilarity(dp, docid);
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
                count += (f * f);
            }

            docLength = Math.sqrt(count);

            scores[i] *= 1 / (queryLength * docLength);
        }

        System.out.println(Arrays.toString(scores));
    }

    // Computing partial score similarity reduces to returning tf/tfmax*idf
    private double computeSimilarity(DocPostings dp, Integer docID) throws IOException{
        int tf = dp.getTF(docID);
        double df = dp.getDocFreq();
        double idf = Math.log(info.getIndexedCount() / df);
        //System.out.println(tf + " " + tfmax + " " + df + " " + idf);
        // Compute tfmax
        int tfmax = 0;
        for (int f: info.getPageInfo(docID).keyfreq.values()){
            if (f > tfmax){ tfmax = f; }
        }
        return tf * idf / tfmax;
    }

    // Phrase assumed to only contain valid stemmed keywords
    private DocPostings computeBigramDP(String[] phrase) throws IOException{
        // Find phrase locations
        DocPostings w1 = info.getKeywordPostingBody(info.getKeywordID(phrase[0]));
        DocPostings w2 = info.getKeywordPostingBody(info.getKeywordID(phrase[1]));

        Integer[] w1Docs = w1.getDocumentIDs();

        ArrayList<Integer> w1Positions;
        ArrayList<Integer> w2Positions;

        DocPostings newDP = new DocPostings();

        // Populate new doc posting with phrase
        for (Integer docID : w1Docs) {
            if ((w2Positions = w2.getPositionList(docID)) == null){ continue; }
            w1Positions = w1.getPositionList(docID);

            // Check for consecutive terms
            int w1ind = 0;
            int w2ind = 0;
            int t1;
            int t2;
            while (w1ind < w1Positions.size() && w2ind < w2Positions.size()){
                t1 = w1Positions.get(w1ind).intValue();
                t2 = w2Positions.get(w2ind).intValue();
                if (t1 + 1 == t2){
                    newDP.addPosting(docID, 0);
                    t1++;
                    t2++;
                }
                else if (t1 < t2) { t1++; }
                else { t2++; }
            }
        }

        return newDP;
    }

    // Phrase assumed to only contain valid stemmed keywords
    private DocPostings computeTrigramDP(String[] phrase) throws IOException{
        // Find phrase locations
        DocPostings w1 = info.getKeywordPostingBody(info.getKeywordID(phrase[0]));
        DocPostings w2 = info.getKeywordPostingBody(info.getKeywordID(phrase[1]));
        DocPostings w3 = info.getKeywordPostingBody(info.getKeywordID(phrase[2]));

        Integer[] w1Docs = w1.getDocumentIDs();

        ArrayList<Integer> w1Positions;
        ArrayList<Integer> w2Positions;
        ArrayList<Integer> w3Positions;

        DocPostings newDP = new DocPostings();

        // Populate new doc posting with phrase
        for (Integer docID : w1Docs) {
            if ((w2Positions = w2.getPositionList(docID)) == null){ continue; }
            if ((w3Positions = w3.getPositionList(docID)) == null){ continue; }
            w1Positions = w1.getPositionList(docID);

            // Check for consecutive terms
            int w1ind = 0;
            int w2ind = 0;
            int w3ind = 0;
            int t1;
            int t2;
            int t3;
            while (w1ind < w1Positions.size() && w2ind < w2Positions.size() && w3ind < w3Positions.size()){
                t1 = w1Positions.get(w1ind).intValue();
                t2 = w2Positions.get(w2ind).intValue();
                t3 = w3Positions.get(w3ind).intValue();
                if (t1 + 1 == t2 && t2 + 1 == t3){
                    newDP.addPosting(docID, 0);
                    t1++;
                    t2++;
                    t3++;
                }
                else if (t1 <= t2 && t1 <= t3) { t1++; }
                else if (t2 <= t1 && t2 <= t3) { t2++; }
                else { t3++; }
            }
        }

        return newDP;
    }
}
