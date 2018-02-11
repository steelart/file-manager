package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import steelart.alex.filemanager.api.ContentProvider;
import steelart.alex.filemanager.api.swing.SwingPreviewPlugin;

/**
 * Simple preview plug-in for text files
 *
 * @author Alexey Merkulov
 * @date 11 February 2018
 */
class SwingTextPreviewPlugin implements SwingPreviewPlugin {
    @Override
    public Component getPreview(ContentProvider provider) throws IOException {
        //TODO: add encoding detection
        /*
        String mimeType = provider.getMimeType();
        if (mimeType == null || !mimeType.startsWith("text/")) {
            return null;
        }
        */
        File file = provider.getFile();
        JTextArea area = new JTextArea();
        area.setEditable(false);
        FileReader fr = new FileReader(file);
        BufferedReader reader = new BufferedReader(fr);

        area.read(reader, "HelloWorld");

        JScrollPane scrollPane = new JScrollPane(area) {
            private static final long serialVersionUID = 1L;

            @Override
            public void requestFocus() {
                area.requestFocus();
            }
        };
        return scrollPane;
    }
}
