/*
 * %W% %E%
 *
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.misc;

/* A package-private class implementing a signal handler in native code. */

final class NativeSignalHandler implements SignalHandler {

    private final long handler;

    NativeSignalHandler(long handler) {
        this.handler = handler;
    }

    private static native void handle0(int number, long handler);

    long getHandler() {
        return handler;
    }

    public void handle(Signal sig) {
        handle0(sig.getNumber(), handler);
    }
}
