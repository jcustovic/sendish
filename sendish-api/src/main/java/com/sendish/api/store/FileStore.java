package com.sendish.api.store;

import com.sendish.api.store.exception.ResourceNotFoundException;

import java.io.IOException;
import java.io.InputStream;

public interface FileStore {

    String save(InputStream p_inputStream) throws IOException;

    void delete(String resourceId) throws ResourceNotFoundException;

    InputStream getAsInputStream(String resourceId) throws ResourceNotFoundException;

}
