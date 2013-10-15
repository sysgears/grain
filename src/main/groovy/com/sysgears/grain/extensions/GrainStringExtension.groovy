package com.sysgears.grain.extensions

import org.apache.commons.lang.StringEscapeUtils

import java.text.Normalizer
import java.util.regex.Pattern


/**
 * String extension methods
 */
class GrainStringExtension {

    /**
     * Diacritic pattern.
     */
    private static final Pattern DIACRITIC_PATTERN = Pattern.compile('\\p{InCombiningDiacriticalMarks}+', Pattern.UNICODE_CASE)

    /**
     * Punctuation pattern.
     */
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile('\\p{Punct}+', Pattern.UNICODE_CASE)

    /**
     * Whitespaces pattern.
     */
    private static final Pattern WHITESPACES_PATTERN = Pattern.compile('\\s+', Pattern.UNICODE_CASE)

    /**
     * Encodes a string as a url
     * For instance, if you're trying to make a slug from a string: <code>"A Good Day"</code>,
     * you will receive <code>"a-good-day"</code> string.
     * @param self String instance.
     * @return String encoded as a slug (permalink).
     */
    static String encodeAsSlug(String self) {
        if (self.isEmpty()) {
            return ""
        }
        removeWhitespaces(normalize(self.toLowerCase()))
    }

    /**
     * Escapes a non-HTML String into an HTML compatible String.
     * @param self String instance.
     * @return String encoded as HTML.
     */
    static String encodeAsHTML(String self) {
        if (self.isEmpty()) {
            return ""
        }
        StringEscapeUtils.escapeHtml(self)
    }

    /**
     * Decodes HTML String into an HTML compatible String.
     * @param self String instance.
     * @return String encoded as HTML.
     */
    static String decodeHTML(String self) {
        if (self.isEmpty()) {
            return ""
        }
        StringEscapeUtils.unescapeHtml(self)
    }

    /**
     * Returns lines count in current string instance
     * @param self String instance.
     * @return integer instance with lines count
     */
    static int countLines(String self) {
        LineNumberReader ln = new LineNumberReader(new StringReader(self));
        int count = 0;
        while (ln.readLine() != null) {
            count++;
        }
        count
    }

    /**
     * Removes whitespaces from a String.
     * @param str String instance, which should be whitespace cleaned.
     * @return a String instance without whitespaces.
     */
    private static String removeWhitespaces(String str) {
        str.replaceAll(WHITESPACES_PATTERN, "-")
    }

    /**
     * Normalizes string, using <code>DIACRITIC_PATTERN</code> and <code>PUNCTUATION_PATTERN</code>
     * @param str which should be normalized.
     * @return a normalized String instance.
     */
    private static String normalize(String str) {
        Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll(DIACRITIC_PATTERN, "")
                .replaceAll(PUNCTUATION_PATTERN, " ")
                .trim()
    }
}
