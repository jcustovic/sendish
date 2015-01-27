package com.sendish.api.service.impl;

import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.thumbnailator.filter.GaussianBlurFilter;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.ResizedPhotoRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.ResizedPhoto;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.filters.ImageFilter;
import net.coobird.thumbnailator.geometry.Positions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class ResizedPhotoServiceImpl {

    public static final Map<String, int[]> KEY_SIZE_MAP;

    static {
        KEY_SIZE_MAP = new HashMap<>();
        KEY_SIZE_MAP.put("list_medium", new int[] { 320, 160 }); // Aspect 2:1 (2x smaller)
        KEY_SIZE_MAP.put("list_small", new int[] { 160, 80 }); // Aspect 2:1 (4x smaller)
        KEY_SIZE_MAP.put("list_medium_blur", new int[] { 320, 160 }); // Aspect 2:1 (2x smaller) blur effect
        KEY_SIZE_MAP.put("list_small_blur", new int[] { 160, 80 }); // Aspect 2:1 (4x smaller) blur effect
    }

    private final ImageFilter blurFilter = new GaussianBlurFilter(20);

    @Autowired
    private ResizedPhotoRepository resizedPhotoRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FileStore fileStore;

    public ResizedPhoto getResizedPhoto(Long photoId, String sizeKey) {
        ResizedPhoto resizedPhoto = resizedPhotoRepository.findByUuidAndKey(photoId, sizeKey);
        if (resizedPhoto == null) {
            resizedPhoto = resize(photoId, sizeKey);
        }

        return resizedPhoto;
    }

    private ResizedPhoto resize(Long photoId, String sizeKey) {
        Photo photo = photoRepository.findOne(photoId);
        InputStream originalIS = null;
        try {
            originalIS = fileStore.getAsInputStream(photo.getStorageId());
        } catch (ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }

        int[] size = KEY_SIZE_MAP.get(sizeKey);
        if (size == null) {
            throw new RuntimeException("Size key " + sizeKey + " not supported");
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Builder<? extends InputStream> thumbnails = Thumbnails.of(originalIS).useOriginalFormat().size(size[0], size[1]);
            if (size[0] != size[1]) {
                thumbnails.crop(Positions.CENTER);
            }
            if (sizeKey.endsWith("blur")) {
                thumbnails.addFilter(blurFilter);
            }

            thumbnails.toOutputStream(out);

            return createResizedPhoto(photo, sizeKey, size[0], size[1], out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResizedPhoto createResizedPhoto(Photo photo, String sizeKey, int width, int height, byte[] content) throws IOException {
        ResizedPhoto resizedPhoto = new ResizedPhoto();
        resizedPhoto.setPhoto(photo);
        resizedPhoto.setKey(sizeKey);
        resizedPhoto.setWidth(width);
        resizedPhoto.setHeight(height);
        resizedPhoto.setSize((long) content.length);

        String storeId = fileStore.save(new ByteArrayInputStream(content));

        resizedPhoto.setStorageId(storeId);

        return resizedPhotoRepository.save(resizedPhoto);
    }

}
