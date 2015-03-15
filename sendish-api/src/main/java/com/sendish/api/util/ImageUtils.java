package com.sendish.api.util;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public final class ImageUtils {

    public static Dimension getDimension(InputStream p_resourceStream) throws IOException {
        ImageInputStream in = ImageIO.createImageInputStream(p_resourceStream);
        try {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            }
        } finally {
            if (in != null) in.close();
        }

        throw new IllegalArgumentException("Cannot read image dimensions");
    }
    
    public static String getImageTypeFromContentType(String contentType) {
    	if (contentType.equals("image/jpeg")) {
    		return "jpeg";
    	} else if (contentType.equals("image/png")) {
    		return "png";
    	} else if (contentType.equals("image-gif")) {
    		return "gif";
    	}
    	
    	return null;
    }

}
