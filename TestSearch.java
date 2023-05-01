import java.util.Scanner;
import java.io.IOException;

public class TestSearch {
    public static void main(String[] args) {
        try{
            Search searcher = new Search();
            Scanner inputReader = new Scanner(System.in);
            System.out.print("Enter search query: ");

            String query = "temp";
            while (query.length() != 0){
                query = inputReader.nextLine();
                System.out.println("You queried: '" + query + "'");
                searcher.EvaluateQuery(query);
                System.out.print("Enter search query: ");
            }
            System.out.println();
        }
        catch(IOException ex)
        {
            System.err.println(ex.toString());
        }
    }
}
