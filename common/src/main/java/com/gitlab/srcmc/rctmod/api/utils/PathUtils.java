/*
 * This file is part of Radical Cobblemon Trainers.
 * Copyright (c) 2024, HDainester, All rights reserved.
 *
 * Radical Cobblemon Trainers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Radical Cobblemon Trainers is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Radical Cobblemon Trainers. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gitlab.srcmc.rctmod.api.utils;

public final class PathUtils {
    private PathUtils() {}

    /**
     * Retrieves the name component of a path, strips its extension and changes it to
     * lowercase. The path may only be separated by forward slash ('/') characters.
     * 
     * @param path Input path to retrieve the name component from.
     * @return Stripped lowercase name component.
     */
    public static String filename(String path) {
        var lastSep = path.lastIndexOf('/');
        var lastDot = path.lastIndexOf('.');

        return path.toLowerCase().substring(
            lastSep >= 0 ? lastSep + 1 : 0,
            (lastDot >= 0 && (lastSep < 0 || lastDot > lastSep)) ? lastDot : path.length());
    }

    /**
     * Strips the given path of its last component and changes it to lowercase. The
     * path may only be separated by forward slash ('/') characters. If the path
     * contains no separators an empty string is returned.
     * 
     * @param path Path to strip of its last component.
     * @return Stripped lowercase path.
     */
    public static String dirname(String path) {
        var lastSep = path.lastIndexOf('/');
        return lastSep >= 0 ? path.substring(0, lastSep) : "";
    }
}
