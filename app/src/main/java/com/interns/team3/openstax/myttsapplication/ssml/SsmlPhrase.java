package com.interns.team3.openstax.myttsapplication.ssml;

import java.util.function.Consumer;

/**
 * Defines the methods used to build a SSML speech phrase
 */
public interface SsmlPhrase {
    SsmlPhrase text(String text);

    SsmlPhrase text(String format, Object... params);

    SsmlPhrase sentence(String text);

    SsmlPhrase sentence(Consumer<SsmlPhrase> consumer);

    SsmlPhrase sentence(String format, Object... params);

    SsmlPhrase paragraph(String text);

    SsmlPhrase paragraph(Consumer<SsmlPhrase> consumer);

    SsmlPhrase paragraph(String format, Object... params);

    SsmlPhrase comma();

    SsmlPhrase strongBreak();

    SsmlPhrase newParagraph();

    /**
     * Requests that the number be spoken like a quantity, e.g.:
     * <ul>
     *   <li>1 is spoken as "one"</li>
     *   <li>100 is spoken as "one-hundred"</li>
     * </ul>
     *
     * @param number to be spoken
     * @return the builder object for further method chaining
     */
    SsmlPhrase cardinalNumber(long number);

    /**
     * @see #cardinalNumber(long)
     */
    SsmlPhrase cardinalNumber(String number);

    /**
     * Requests that the number be spoken like position in a series, e.g.:
     * <ul>
     *   <li>1 is spoken as "first"</li>
     *   <li>100 is spoken as "one-hundredth"</li>
     * </ul>
     *
     * @param number to be spoken
     * @return the builder object for further method chaining
     */
    SsmlPhrase ordinalNumber(long number);

    /**
     * @see #ordinalNumber(long)
     */
    SsmlPhrase ordinalNumber(String number);

    SsmlPhrase spellOut(String text);
}