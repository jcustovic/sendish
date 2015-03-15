package com.sendish.api.service.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.filters.Transparency;
import net.coobird.thumbnailator.geometry.Positions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.util.RetryUtils;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.ResizedPhotoRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.ResizedPhoto;

@Service
@Transactional
public class ResizedPhotoServiceImpl {

    public static final Map<String, int[]> KEY_SIZE_MAP;

    static {
        KEY_SIZE_MAP = new HashMap<>();
        KEY_SIZE_MAP.put("list_small", new int[] { 160, 80 }); // Aspect 2:1 (4x smaller)
        KEY_SIZE_MAP.put("list_small_blur", new int[] { 160, 80 }); // Aspect 2:1 (4x smaller) blur effect        
        KEY_SIZE_MAP.put("list_medium", new int[] { 320, 160 }); // Aspect 2:1 (2x smaller)
        KEY_SIZE_MAP.put("list_medium_blur", new int[] { 320, 160 }); // Aspect 2:1 (2x smaller) blur effect
        KEY_SIZE_MAP.put("list_square_small", new int[] { 160, 160 });
        KEY_SIZE_MAP.put("list_square_small_blur", new int[] { 160, 160 });
    }

    //private final ImageFilter blurFilter = new GaussianBlurFilter(20);

    @Autowired
    private ResizedPhotoRepository resizedPhotoRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FileStore fileStore;
    
    @Value("${app.image.overlay.sendish_logo}")
    private Resource logoOverlayPath;

    public ResizedPhoto getResizedPhoto(Long photoId, String sizeKey) {
    	return RetryUtils.retry(() -> {
    		ResizedPhoto resizedPhoto = resizedPhotoRepository.findByPhotoIdAndKey(photoId, sizeKey);
            if (resizedPhoto == null) {
            	resizedPhoto = resize(photoId, sizeKey);	
            }
            
            return resizedPhoto;
    	}, 3, 50);
    }

    private ResizedPhoto resize(Long photoId, String sizeKey) throws DataIntegrityViolationException {
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
                //thumbnails.addFilter(blurFilter);
            	thumbnails.addFilter(new Transparency(0.3));
            	thumbnails.watermark(Positions.CENTER, ImageIO.read(logoOverlayPath.getInputStream()), 1f);
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
