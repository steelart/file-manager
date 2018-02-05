package steelart.alex.filemanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
* A collection of static methods for file manager project
*
* @author Alexey Merkulov
* @date 2 February 2018
*/
public class FMUtils {
    // TODO: it could be made as parameter (whether we need to use directory sort or not)
    // NOTE: used reverse e1 and e2:
    private static final Comparator<FMElement> dirComp = (e1, e2) -> Boolean.compare(e2.isDirectory(), e1.isDirectory());

    // TODO: used a hack with instanceof!
    // NOTE: used reverse e1 and e2:
    private static final Comparator<FMElement> parentDirComp = (e1, e2) -> Boolean.compare(e2 instanceof ParentDirectory, e1 instanceof ParentDirectory);

    public static List<FMElement> getSortedList(Collection<FMElement> elements, ElementColumnProperty property, boolean reversed) {
        List<FMElement> sorted = new ArrayList<>(elements);
        Comparator<FMElement> comparator = property.comparator(reversed);
        sorted.sort(parentDirComp.thenComparing(dirComp).thenComparing(comparator));
        return sorted;
    }

    public static FMElement filterElement(FMElement e, Supplier<FMElementCollection> exitPoint) {
        if (e.name().endsWith(".zip") || e.name().endsWith(".jar")) {
            return new FMZipFile(e, exitPoint);
        } else {
            return e;
        }
    }
}
