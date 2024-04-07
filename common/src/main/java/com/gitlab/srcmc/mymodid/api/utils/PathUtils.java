package com.gitlab.srcmc.mymodid.api.utils;

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
