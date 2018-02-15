package steelart.alex.filemanager;

import java.io.IOException;


/**
 * This exception is used by progress tracker to interrupt
 * processed operation (by user or other reason)
 *
 * @author Alexey Merkulov
 * @date 16 February 2018
 */
public class OperationInterrupt extends IOException {
    private static final long serialVersionUID = 1L;

}
