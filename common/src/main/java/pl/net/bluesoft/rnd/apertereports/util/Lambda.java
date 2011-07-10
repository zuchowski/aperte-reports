package pl.net.bluesoft.rnd.apertereports.util;

/**
 * Lambda pattern utility interface.
 *
 * @author tlipski@bluesoft.net.pl Date: 2009-03-06 Time: 12:24:15
 */
public interface Lambda<inputType, resultType> {
    resultType l(inputType x);
}
