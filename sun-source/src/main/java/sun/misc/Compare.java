/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * Compare: an interface to enable users to define the result of
 * a comparison of two objects.
 *
 * @version %I%, %G%
 * @author Sunita Mani
 */

package sun.misc;

public interface Compare {

    /**
     * doCompare
     *
     * @param obj1 first object to compare.
     * @param obj2 second object to compare.
     * @return -1 if obj1 < obj2, 0 if obj1 == obj2, 1 if obj1 > obj2.
     */
    public int doCompare(Object obj1, Object obj2);

}
