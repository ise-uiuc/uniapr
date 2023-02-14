/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class PatchLoader {
    private static final String[] CLASS_FILES_EXT = new String[] {"class"};

    private final File rootDir;

    public PatchLoader(File rootDir) {
        this.rootDir = rootDir;
    }

    private static FileFilter isFolder() {
        return new FileFilter() {
            @Override
            public boolean accept(final File file) {
                return file.isDirectory();
            }
        };
    }

    public List<Patch> loadPatches() throws NoPatchFoundException {
        final File[] filesList = this.rootDir.listFiles(isFolder());
        if (filesList == null || filesList.length == 0) {
            throw new NoPatchFoundException("No patches were found under" + this.rootDir.getAbsolutePath());
        }
        final ArrayList<Patch> patches = new ArrayList<>();
        for (final File patchBaseDir : filesList) {
        	String patchName=patchBaseDir.getName();
            final List<LoadedClass> classes = new ArrayList<>();
            for (final File classFile : listClassFiles(patchBaseDir)) {
                try {
                    classes.add(LoadedClass.fromFile(classFile));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to load the class");
                }
            }
            if (!classes.isEmpty()) {
                // TODO: add only covering tests
                patches.add(new Patch(classes, Collections.<String>emptyList(), patchName));
            }
        }
        patches.trimToSize();
        return patches;
    }

    private static Collection<File> listClassFiles(final File baseDir) {
        return FileUtils.listFiles(baseDir, CLASS_FILES_EXT, true);
    }
}
