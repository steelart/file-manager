package steelart.alex.filemanager;

import java.util.Comparator;

/**
 * File manager element property represented in some panel column.
 * It is some unification for different information representation.
 *
 * TODO: maybe it is better to create interface instead of enumeration
 *
 * @author Alexey Merkulov
 * @date 26 January 2018
 */
public enum ElementColumnProperty {
    NAME {
        @Override
        public String propName() {
            return "name";
        }
        @Override
        public String data(FMElement e) {
            return e.name();
        }
        @Override
        public Comparator<FMElement> comparator(boolean reversed) {
            Comparator<FMElement> comparator = (e1, e2) -> e1.name().compareTo(e2.name());
            if (reversed) comparator = comparator.reversed();
            return comparator;
        }

    },
    SIZE {
        @Override
        public String propName() {
            return "size";
        }

        @Override
        public String data(FMElement e) {
            long size = e.size();
            if(size < 0) return "";
            return "" + size + "b"; // TODO: inefficient
        }

        @Override
        public Comparator<FMElement> comparator(boolean reversed) {
            Comparator<FMElement> comparator = (e1, e2) -> Long.compare(e1.size(), e2.size());
            if (reversed) comparator = comparator.reversed();
            return comparator.thenComparing(NAME.comparator(false));
        }

    };


    public abstract String propName();
    public abstract String data(FMElement e);

    /**
     * This method returns a comparator to sort elements by this column.
     * It could construct a comparator to sort elements in reverse order.
     * Reverse order is not just reverse comparator because in case of equal
     * primary sort parameter we need to apply alphabetic sort.
     *
     * @param reversed should we sort in reverse order
     * @return comparator to sort elements by this column
     */
    public abstract Comparator<FMElement> comparator(boolean reversed);
}
