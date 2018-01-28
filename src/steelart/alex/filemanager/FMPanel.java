package steelart.alex.filemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* It is a collection of file manager elements which could be sorted in several ways
*
* @author Alexey Merkulov
* @date 25 January 2018
*/
public class FMPanel {
    // TODO: it could be made as parameter (whether we need to use directory sort or not)
    // NOTE: used reverse e1 and e2:
    private static final Comparator<FMElement> dirComp = (e1, e2) -> Boolean.compare(e2.isDir(), e1.isDir());

    private final Set<FMElement> elements;


    private FMPanel(Set<FMElement> elements) {
        this.elements = elements;
    }

    public int length() {
        return elements.size();
    }

    public List<FMElement> getSortedList(ElementColumnProperty property, boolean reversed) {
        List<FMElement> sorted = new ArrayList<>(elements);
        Comparator<FMElement> comparator = property.comparator(reversed);
        sorted.sort(dirComp.thenComparing(comparator));
        return sorted;
    }

    /////////////////////////////////////////
    /////////////FACTORIES///////////////////
    /////////////////////////////////////////

    public static FMPanel constructForDirectory(File dir) {
        Set<FMElement> elements = new HashSet<FMElement>();
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                elements.add(new RegularFile(file));
            } else if (file.isDirectory()) {
                elements.add(new RegularDirectory(file));
            }
        }
        return new FMPanel(elements);
    }
}
