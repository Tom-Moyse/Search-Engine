import java.util.Scanner;
import java.io.IOException;

public class TestSearch {
    public static void main(String[] args) {
        try{
            Search searcher = new Search();
            Scanner inputReader = new Scanner(System.in);
            System.out.println("Enter search query: ");

            String query = "temp";
            while (query != ""){
                query = inputReader.nextLine();
                searcher.EvaluateQuery(query);
            }
        }
        catch(IOException ex)
        {
            System.err.println(ex.toString());
        }
    }
}
