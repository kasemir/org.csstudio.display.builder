/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.csstudio.javafx;


import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;

import javafx.scene.image.Image;


/**
 * At ESS a lot of OPIs are based on some schematics, using a great number
 * of symbol widgets often reusing the same images.
 * <p>
 * This class will allow caching of images using their resolved filename as key.
 * <p/>
 * <p>
 * Current implementation uses a {@link ReferenceMap} of soft pointers for both
 * the key and the value (an {@link Image}). This will allow for the garbage
 * collector to reclaim the memory of unreferenced images on critically low
 * memory
 * situations.
 * </p>
 * <p>
 * A better implementation will use a real cache library (e.g.
 * <a href="https://cache2k.org">cache2k</a>) with proper policies.
 * </p>
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 17 Oct 2018
 */
public class ImageCache {

    private static final Map<String, Image> CACHE = Collections.synchronizedMap(new ReferenceMap<>(ReferenceStrength.SOFT, ReferenceStrength.SOFT));

    /**
     * @return The number of entries before the cache is cleared.
     */
    public static int clear ( ) {

        int entries = size();

        CACHE.clear();

        return entries;

    }

    /**
     * @param key The unique identifier of the cached image, usually its
     *            resolved filename.
     * @return The cached {@link Image} instance or {@code null}.
     */
    public static Image get ( String key ) {
        return CACHE.get(key);
    }

    /**
     * @param key The unique identifier of the cached image, usually its
     *            resolved filename.
     * @param value The {@link Image} to be cached.
     * @return The previously {@link Image} associated with the given
     *         {@code key}, or {@code null}.
     */
    public static Image put ( String key, Image value ) {
        return CACHE.put(key, value);
    }

    /**
     * @param key The unique identifier of the cached image, usually its
     *            resolved filename.
     * @return The previously {@link Image} associated with the given
     *         {@code key}, or {@code null}.
     */
    public static Image remove ( String key ) {
        return CACHE.remove(key);
    }

    /**
     * @return The current cache size, i.e. the number of elements stored in the
     *         cache.
     */
    public static int size ( ) {
        return CACHE.size();
    }

    private ImageCache ( ) {
    }

}
