package searchengine.tests;
import java.util.Scanner;

import searchengine.Search;
import searchengine.structs.SearchResult;

import java.util.Arrays;
import java.io.IOException;

public class TestSearch {
    public static void main(String[] args) {
        try{
            Search searcher = new Search(".");
            Scanner inputReader = new Scanner(System.in);
            System.out.print("Enter search query: ");

            String query = "temp";
            while (query.length() != 0){
                query = inputReader.nextLine();
                System.out.println("You queried: '" + query + "'");
                SearchResult sr = searcher.EvaluateQuery(query);
                System.out.println(Arrays.toString(sr.documents));
                System.out.print("Enter search query: ");
            }
            System.out.println();
            inputReader.close();
        }
        catch(IOException ex)
        {
            System.err.println(ex.toString());
        }
    }
}
