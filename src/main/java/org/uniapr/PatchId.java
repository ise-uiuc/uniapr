/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import java.io.Serializable;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchId implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int idCounter = 0;

    private final int id;

    private PatchId(int id) {
        this.id = id;
    }

    public static PatchId alloc() {
        return new PatchId(idCounter++);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PatchId that = (PatchId) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return ((Integer) this.id).hashCode();
    }
    
    public int getId(){
    	return this.id;
    }
}
