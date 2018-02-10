package steelart.alex.filemanager.api.swing;

import java.awt.Component;
import java.io.IOException;

import steelart.alex.filemanager.api.ContentProvider;

/**
 * Preview interface for swing GUI implementation
 *
 * TODO: Add methods to get plug-in name or description
 * TODO: Add methods for plug-in configuration
 *
 * @author Alexey Merkulov
 * @date 8 February 2018
 */
public interface SwingPreviewPlugin {
    /**
     * Test content from provider for this plugin correspondence and
     * calculate {@link Component} with content preview (or null)
     *
     * @param provider content provider
     * @return {@link Component} with preview or
     *         {@code null} if passed content is not appropriate
     */
    public Component getPreview(ContentProvider provider) throws IOException;
}
