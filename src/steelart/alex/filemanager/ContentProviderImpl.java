package steelart.alex.filemanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import steelart.alex.filemanager.api.ContentProvider;

/**
 * Just simple implementation for {@link ContentProvider}
 *
 * @author Alexey Merkulov
 * @date 9 February 2018
 */
public class ContentProviderImpl implements ContentProvider {
    private final File file;
    private String mimeType = null;
    private boolean mimeTypeInited = false;

    public ContentProviderImpl(File file) {
        this.file = file;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getMimeType() throws IOException {
        if (!mimeTypeInited) {
            assert mimeType == null;
            mimeType = Files.probeContentType(file.toPath());
            mimeTypeInited = true;
            if (mimeType == null) {
                return null;
            }
        }
        return mimeType;
    }
}
