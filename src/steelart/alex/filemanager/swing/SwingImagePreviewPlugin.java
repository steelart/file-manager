package steelart.alex.filemanager.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import steelart.alex.filemanager.api.ContentProvider;
import steelart.alex.filemanager.api.swing.SwingPreviewPlugin;

/**
 * Simple preview plug-in for images
 *
 * @author Alexey Merkulov
 * @date 11 February 2018
 */
class SwingImagePreviewPlugin implements SwingPreviewPlugin {
    @Override
    public Component getPreview(ContentProvider provider) throws IOException {
        File file = provider.getFile();
        String mimeType = provider.getMimeType();
        if (mimeType != null && !mimeType.startsWith("image/")) {
            return null;
        }
        final BufferedImage image = ImageIO.read(file);
        if (image == null) {
            return null;
        }
        JPanel panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    int maxW = this.getWidth();
                    int maxH = this.getHeight();
                    double imW = image.getWidth();
                    double imH = image.getHeight();
                    double wr = maxW/imW;
                    double hr = maxH/imH;
                    double min = Double.min(wr, hr);
                    int w = (int)Math.round(min*imW);
                    int h = (int)Math.round(min*imH);
                    g.drawImage(image, (maxW-w)/2, (maxH-h)/2, w, h, null);
                }
            }
        };
        return panel;
    }
}
