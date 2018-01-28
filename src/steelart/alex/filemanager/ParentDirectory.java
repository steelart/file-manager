package steelart.alex.filemanager;

/**
 * File manager parent directory exit point
 *
 * @author Alexey Merkulov
 * @date 29 January 2018
 */
public class ParentDirectory implements FMDirectory {
    private final FMDirectory dir;

    public ParentDirectory(FMDirectory dir) {
        this.dir = dir;
    }

    @Override
    public String name() {
        return "..";
    }

    @Override
    public FMPanel enterDir() {
        return dir.enterDir();
    }

}
