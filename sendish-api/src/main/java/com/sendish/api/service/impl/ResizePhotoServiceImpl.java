package com.sendish.api.service.impl;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.sendish.api.service.ResizePhotoService;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.Thumbnails.Builder;
import net.coobird.thumbnailator.geometry.Positions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sendish.api.store.FileStore;
import com.sendish.api.store.exception.ResourceNotFoundException;
import com.sendish.api.thumbnailator.filter.GaussianBlurFilter;
import com.sendish.api.thumbnailator.filter.TransparencyColorFilter;
import com.sendish.repository.PhotoRepository;
import com.sendish.repository.ResizedPhotoRepository;
import com.sendish.repository.model.jpa.Photo;
import com.sendish.repository.model.jpa.ResizedPhoto;

@Service
@Primary
@Transactional
public class ResizePhotoServiceImpl implements ResizePhotoService {

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

    private final GaussianBlurFilter blurFilter = new GaussianBlurFilter(10);
    private final TransparencyColorFilter transparencyColorFilter = new TransparencyColorFilter(0.8f, new Color(42, 48, 63));

    @Autowired
    private ResizedPhotoRepository resizedPhotoRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FileStore fileStore;
    
    @Value("${app.image.overlay.sendish_logo}")
    private Resource logoOverlayPath;

    @Override
    public ResizedPhoto getResizedPhoto(Long photoId, String sizeKey) {
        ResizedPhoto resizedPhoto = resizedPhotoRepository.findByPhotoIdAndKey(photoId, sizeKey);
        if (resizedPhoto == null) {
            resizedPhoto = resize(photoId, sizeKey);
        }

        return resizedPhoto;
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
            	thumbnails.addFilter(blurFilter);
            	thumbnails.addFilter(transparencyColorFilter);
				
//            	BufferedImage bufferedImage = thumbnails.asBufferedImage();
//				Thumbnails.of(bufferedImage)
//					.outputFormat(ImageUtils.getImageTypeFromContentType(photo.getContentType()))
//					.size(size[0], size[1])
//					.outputQuality(1)
//					.watermark(Positions.CENTER, ImageIO.read(logoOverlayPath.getInputStream()), 1f)
//					.toOutputStream(out);
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

        String storeId = fileStore.save(new ByteArrayInputStream(content), "photo_resized");

        resizedPhoto.setStorageId(storeId);

        return resizedPhotoRepository.save(resizedPhoto);
    }

}
