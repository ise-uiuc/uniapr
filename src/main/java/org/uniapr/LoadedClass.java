/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import org.uniapr.commons.misc.MemberNameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.Serializable;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class LoadedClass implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String classJavaName;

    private final byte[] bytes;

    /**
     * Constructor based on Java/internal name and byte[]
     *
     * @param className Java/internal name of the loaded class
     * @param bytes Contents of the class file
     */
    public LoadedClass(String className, byte[] bytes) {
        Validate.notNull(className);
        this.classJavaName = className.replace('/', '.');
        this.bytes = bytes;
    }

    public static LoadedClass fromFile(final File classFile) throws Exception {
        final String className = MemberNameUtils.getClassName(classFile);
        final byte[] bytes = FileUtils.readFileToByteArray(classFile);
        return new LoadedClass(className, bytes);
    }

    public String getJavaName() {
        return this.classJavaName;
    }

    public String getInternalName() {
        return this.classJavaName.replace('.', '/');
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LoadedClass that = (LoadedClass) o;
        return this.classJavaName.equals(that.classJavaName);
    }

    @Override
    public int hashCode() {
        return this.classJavaName.hashCode();
    }
}
