/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.misc.resources;

/**
 * <p> This class represents the <code>ResourceBundle</code>
 * for sun.misc.
 *
 * @author Michael Colburn
 * @version %I%, %G%
 */

public class Messages extends java.util.ListResourceBundle {

    private static final Object[][] contents = {
            {"optpkg.versionerror", "ERROR: Invalid version format used in {0} JAR file. Check the documentation for the supported version format."},
            {"optpkg.attributeerror", "ERROR: The required {0} JAR manifest attribution is not set in {1} JAR file."},
            {"optpkg.attributeserror", "ERROR: Some required JAR manifest attributes are not set in {0} JAR file."}
    };

    /**
     * Returns the contents of this <code>ResourceBundle</code>.
     * <p>
     *
     * @return the contents of this <code>ResourceBundle</code>.
     */
    public Object[][] getContents() {
        return contents;
    }

}
