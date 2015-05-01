package com.sendish.api.service.impl;

import com.sendish.api.exception.UnsupportedResizeKey;
import com.sendish.api.service.ResizeImageService;
import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.util.RetryUtils;
import com.sendish.repository.ImageRepository;
import com.sendish.repository.ResizedImageRepository;
import com.sendish.repository.model.jpa.Image;
import com.sendish.repository.model.jpa.ResizedImage;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Primary
@Transactional
public class ResizeImageServiceImpl implements ResizeImageService {

    public static final Map<String, int[]> KEY_SIZE_MAP;

    static {
        KEY_SIZE_MAP = new HashMap<>();
        KEY_SIZE_MAP.put("square_small", new int[] { 60, 60 });
    }

    @Autowired
    private ResizedImageRepository resizedImageRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private FileStore fileStore;

    @Override
    public ResizedImage getResizedImage(Long imageId, String sizeKey) {
        ResizedImage resizedImage = resizedImageRepository.findByImageIdAndKey(imageId, sizeKey);
        if (resizedImage == null) {
            resizedImage = resize(imageId, sizeKey);
        }

        return resizedImage;
    }
    
    private ResizedImage resize(Long imageId, String sizeKey) {
    	int[] size = KEY_SIZE_MAP.get(sizeKey);
        if (size == null) {
            throw new UnsupportedResizeKey("Size key " + sizeKey + " not supported");
        }
        
        Image image = imageRepository.findOne(imageId);
        InputStream originalIS = null;
        try {
            originalIS = fileStore.getAsInputStream(image.getStorageId());
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Builder<? extends InputStream> thumbnails = Thumbnails.of(originalIS).useOriginalFormat().size(size[0], size[1]);

            thumbnails.toOutputStream(out);

            return createResizedImage(image, sizeKey, size[0], size[1], out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResizedImage createResizedImage(Image image, String sizeKey, int width, int height, byte[] content) throws IOException {
        ResizedImage resizedImage = new ResizedImage();
        resizedImage.setImage(image);
        resizedImage.setKey(sizeKey);
        resizedImage.setWidth(width);
        resizedImage.setHeight(height);
        resizedImage.setSize((long) content.length);

        String storeId = fileStore.save(new ByteArrayInputStream(content), "image_resized");

        resizedImage.setStorageId(storeId);

        return resizedImageRepository.save(resizedImage);
    }

}
