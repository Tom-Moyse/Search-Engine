package searchengine.structs;
import java.util.Comparator;

public class DocRankComparator implements Comparator<Integer>{
    private final Double[] array;

    public DocRankComparator(Double[] arr){
        array = arr;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i; // Autoboxing
        }
        return indexes;
    }

    @Override
    public int compare(Integer index1, Integer index2)
    {
         // Autounbox from Integer to int to use as array indexes
        return array[index2].compareTo(array[index1]);
    }
}
