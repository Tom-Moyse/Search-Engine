import java.io.*;
import java.util.HashSet;
import java.util.stream.Collectors;

public class StopStem
{
	private Porter porter;
	private HashSet<String> stopWords;

	public StopStem()
	{
		porter = new Porter();
		stopWords = new HashSet<String>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader("stopwords.txt"));
			stopWords.addAll(reader.lines().collect(Collectors.toList()));
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println(e.toString());
			return;
		} catch (IOException e) {
			System.out.println(e.toString());
			return;
		}
		
	}
	public boolean isStopWord(String str)
	{
		return stopWords.contains(str);	
	}
	public String stem(String str)
	{
		return porter.stripAffixes(str);
	}
}
