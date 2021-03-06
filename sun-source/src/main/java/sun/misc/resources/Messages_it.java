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

public class Messages_it extends java.util.ListResourceBundle {

    private static final Object[][] contents = {
            {"optpkg.versionerror", "ERRORE: Formato versione non valido nel file JAR {0}. Verificare nella documentazione il formato della versione supportato."},
            {"optpkg.attributeerror", "ERRORE: L''attributo manifesto JAR {0} richiesto non \u00e8 impostato nel file JAR {1}."},
            {"optpkg.attributeserror", "ERRORE: Alcuni attributi manifesti JAR {0} richiesti non sono impostati nel file JAR {1}."}
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
