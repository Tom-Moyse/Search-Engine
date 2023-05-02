package searchengine;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

import searchengine.structs.*;
import searchengine.helper.StopStem;

public class Search {
    private InfoStore info;

    public Search() throws IOException{
        info = new InfoStore();
    }

    public SearchResult EvaluateQuery(String q) throws IOException{
        ArrayList<String> terms = new ArrayList<String>();
        ArrayList<String> bigrams = new ArrayList<String>();
        ArrayList<String> trigrams = new ArrayList<String>();
        StopStem stemmer = new StopStem();

        // Split query into individual words/phrases
        String buffer = "";
        boolean phrase = false;
        int phrase_length = 1;
        for (char c : q.toCharArray()) {
            if (!phrase && c == '"') { phrase = true; }
            else if (phrase && c == '"') { 
                phrase = false;
                if (phrase_length == 2) { bigrams.add(buffer); }
                else if (phrase_length == 3) { trigrams.add(buffer); }
                phrase_length = 1;
                buffer = "";
            }
            else if (!phrase && c == ' '){
                if (buffer != "" && !stemmer.isStopWord(buffer)) {
                    terms.add(stemmer.stem(buffer));
                }
                buffer = "";
            }
            else { buffer += c; }

            if (phrase && c == ' '){ phrase_length++; }
        }
        // Add final term word if required
        if (buffer != "" && !stemmer.isStopWord(buffer)){ terms.add(stemmer.stem(buffer)); }
        
        ArrayList<String[]> stemmedBigrams = new ArrayList<String[]>();
        ArrayList<String[]> stemmedTrigrams = new ArrayList<String[]>();
        boolean invalid = false;

        // Stem bigram phrases
        for (String bigram : bigrams) {
            String[] stemmed = bigram.split(" ", 2); 
            for (int i = 0; i < stemmed.length; i++){
                if (invalid = stemmer.isStopWord(stemmed[i])){ break; }
                stemmed[i] = stemmer.stem(stemmed[i]);
                invalid = info.getKeywordID(stemmed[i]) == null;
            }
            if (!invalid){
                stemmedBigrams.add(stemmed);
            }
        }
        
        // Stem trigram phrases
        invalid = false;
        for (String trigram : trigrams) {
            String[] stemmed = trigram.split(" ", 3); 
            for (int i = 0; i < stemmed.length; i++){
                if (invalid = stemmer.isStopWord(stemmed[i])){ break; }
                stemmed[i] = stemmer.stem(stemmed[i]);
                invalid = info.getKeywordID(stemmed[i]) == null;
            }
            if (!invalid){
                stemmedTrigrams.add(stemmed);
            }
        }

        double[] bodyScores = computePageScores(terms, stemmedBigrams, stemmedTrigrams, false);
        double[] titleScores = computePageScores(terms, stemmedBigrams, stemmedTrigrams, true);

        Double[] combinedScores = new Double[info.getIndexedCount()];

        // Get sorted score indices (=document ids)
        for (int i = 0; i < combinedScores.length; i++) {
            combinedScores[i] = bodyScores[i] + titleScores[i];
        }
        DocRankComparator drc = new DocRankComparator(combinedScores);
        Integer[] rankedDocs = drc.createIndexArray();
        Arrays.sort(rankedDocs, drc);

        ArrayList<Integer> finalDocIDs = new ArrayList<Integer>();
        // Cap results to 50 and remove zero scores
        for (int i = 0; i < 50; i++){
            if (combinedScores[rankedDocs[i]] == 0) { break; }
            finalDocIDs.add(rankedDocs[i]);
        }

        return new SearchResult(finalDocIDs, combinedScores);
    }

    // Functions computes cosine similarity page body scores from query terms, bigrams and trigrams
    private double[] computePageScores(ArrayList<String> terms, ArrayList<String[]> bigrams, ArrayList<String[]> trigrams, boolean isTitle) throws IOException{
        Integer keywordID;
        DocPostings dp;
        double[] scores = new double[info.getIndexedCount()];

        // Sum partial similarities for terms
        for (String term : terms) {
            if ((keywordID = info.getKeywordID(term)) == null){
                System.out.println("Stemmed keyword: " + term + " not found");
                continue;
            }

            dp = null;
            if (isTitle){
                dp = info.getKeywordPostingTitle(keywordID);
            }
            else{
                dp = info.getKeywordPostingBody(keywordID);
            }

            if (dp == null) { continue; }
            
            for (Integer docid : dp.getDocumentIDs()) {
                scores[docid] += computeSimilarity(dp, docid, isTitle);
            }
        }

        // Sum partial similarities for bigrams
        for (String[] bigram: bigrams){

            dp = computeBigramDP(bigram, isTitle);
            if (dp == null) { continue; }

            for (Integer docid : dp.getDocumentIDs()) {
                scores[docid] += computeSimilarity(dp, docid, isTitle);
            }
        }

        // Sum partial similarities for trigrams
        for (String[] trigram: trigrams){
            dp = computeTrigramDP(trigram, isTitle);
            if (dp == null) { continue; }

            for (Integer docid : dp.getDocumentIDs()) {
                scores[docid] += computeSimilarity(dp, docid, isTitle);
            }
        }

        // Normalize for cosine similarity
        PageStore page;
        double queryLength = Math.sqrt(terms.size() + bigrams.size() + trigrams.size());
        double docLength;
        int count;
        for (int i = 0; i < scores.length; i++){
            if (scores[i] == 0){ continue; }
            count = 0;

            page = info.getPageInfo((Integer) i);
            if (isTitle){
                for (Integer f : page.keyfreqtitle.values()) {
                    count += (f * f);
                }
            }
            else{
                for (Integer f : page.keyfreqbody.values()) {
                    count += (f * f);
                }
            }

            docLength = Math.sqrt(count);

            scores[i] *= 1 / (queryLength * docLength);
        }

        return scores;
    }

    // Computing partial score similarity reduces to returning tf/tfmax*idf
    private double computeSimilarity(DocPostings dp, Integer docID, boolean isTitle) throws IOException{
        int tf = dp.getTF(docID);
        double df = dp.getDocFreq();
        double idf = Math.log(info.getIndexedCount() / df) / Math.log(2);
        
        // Compute tfmax
        int tfmax = 0;
        if (isTitle){
            for (int f: info.getPageInfo(docID).keyfreqtitle.values()){
                if (f > tfmax){ tfmax = f; }
            }
        }
        else{
            for (int f: info.getPageInfo(docID).keyfreqbody.values()){
                if (f > tfmax){ tfmax = f; }
            }
        }   
        //System.out.println(tf + " " + tfmax + " " + df + " " + idf);

        return tf * idf / tfmax;
    }

    // Phrase assumed to only contain valid stemmed keywords
    private DocPostings computeBigramDP(String[] phrase, boolean isTitle) throws IOException{
        // Find phrase locations
        DocPostings w1 = null;
        DocPostings w2 = null;
        if (isTitle){
            w1 = info.getKeywordPostingTitle(info.getKeywordID(phrase[0]));
            w2 = info.getKeywordPostingTitle(info.getKeywordID(phrase[1]));
        }
        else{
            w1 = info.getKeywordPostingBody(info.getKeywordID(phrase[0]));
            w2 = info.getKeywordPostingBody(info.getKeywordID(phrase[1]));
        }
        if (w1 == null || w2 == null){ return null; }

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
                    w1ind++;
                    w2ind++;
                }
                else if (t1 < t2) { w1ind++; }
                else { w2ind++; }
            }
        }

        return newDP;
    }

    // Phrase assumed to only contain valid stemmed keywords
    private DocPostings computeTrigramDP(String[] phrase, boolean isTitle) throws IOException{
        // Find phrase locations
        DocPostings w1 = null;
        DocPostings w2 = null;
        DocPostings w3 = null;
        if (isTitle){
            w1 = info.getKeywordPostingTitle(info.getKeywordID(phrase[0]));
            w2 = info.getKeywordPostingTitle(info.getKeywordID(phrase[1]));
            w3 = info.getKeywordPostingTitle(info.getKeywordID(phrase[2]));
        }
        else{
            w1 = info.getKeywordPostingBody(info.getKeywordID(phrase[0]));
            w2 = info.getKeywordPostingBody(info.getKeywordID(phrase[1]));
            w3 = info.getKeywordPostingBody(info.getKeywordID(phrase[2]));
        }
        if (w1 == null || w2 == null || w3 == null){ return null; }

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
                    w1ind++;
                    w2ind++;
                    w3ind++;
                }
                else if (t1 <= t2 && t1 <= t3) { w1ind++; }
                else if (t2 <= t1 && t2 <= t3) { w2ind++; }
                else { w3ind++; }
            }
        }

        return newDP;
    }
}
