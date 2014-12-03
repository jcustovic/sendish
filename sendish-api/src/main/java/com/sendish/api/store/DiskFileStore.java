package com.sendish.api.store;

import com.sendish.api.store.exception.ResourceNotFoundException;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.UUID;

@Service
public class DiskFileStore implements FileStore {

    private static final Logger LOG = LoggerFactory.getLogger(DiskFileStore.class);

    @Value("${app.store.data.path}")
    private transient FileSystemResource dataResource;

    private transient File dataFolder;

    @PostConstruct
    public final void afterPropertiesSet() throws IOException {
        dataFolder = dataResource.getFile();
        if (!dataFolder.exists()) {
            LOG.info("Creating file store folder {}...", dataFolder.getAbsolutePath());
            if (!dataFolder.mkdirs()) {
                throw new BeanInitializationException("Folder " + dataFolder.getAbsolutePath() + " can't be created.");
            }
        }
        LOG.info("Using file store folder: {}", dataFolder.getAbsolutePath());
    }

    @Override
    public String save(InputStream p_inputStream) throws IOException {
        String newId = UUID.randomUUID().toString();
        String relativePath = buildRelativePath(newId);

        final File file = new File(dataFolder, relativePath);
        FileUtils.copyInputStreamToFile(p_inputStream, file);

        return relativePath;
    }

    @Override
    public void delete(String resourceId) throws ResourceNotFoundException {
        final File file = new File(dataFolder, resourceId);
        if (file.exists()) {
            file.delete();
        } else {
            LOG.error("Multimedia with path {} not found", resourceId);
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public InputStream getAsInputStream(String resourceId) throws ResourceNotFoundException {
        final File file = new File(dataFolder, resourceId);
        if (file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new ResourceNotFoundException();
            }
        } else {
            LOG.error("Multimedia with path {} not found", resourceId);
            throw new ResourceNotFoundException();
        }
    }

    private String buildRelativePath(String resourceName) {
        final DateTime now = DateTime.now();

        return now.getYear() + "/" + now.getMonthOfYear() + "/" + now.getDayOfMonth() + "/" + resourceName;
    }

}
