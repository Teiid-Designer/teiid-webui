/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.teiid.webui.client.messages;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.errai.ui.client.local.spi.TranslationService;

import com.google.gwt.core.shared.GWT;

/**
 * A simple injectable class for doing i18n.  This implementation uses the
 * translation service generated by Errai.  Simply add your own entries into
 * the i18n JSON bundle to make them available here.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ClientMessages {

    private static final TranslationService translationService = GWT.create(TranslationService.class);

    /**
     * Constructor.
     */
    public ClientMessages() {
    }

    /**
     * Look up a message in the i18n resource message bundle by key, then format the
     * message with the given arguments and return the result.
     * @param key
     * @param args
     */
    public String format(String key, Object ... args) {
        String pattern = translationService.getTranslation(key);
        if (pattern == null)
            return "!!!" + key + "!!!"; //$NON-NLS-1$ //$NON-NLS-2$
        if (args.length == 0)
            return pattern;

        // TODO add support for actually using { in a message
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            String part1 = pattern.substring(0,pattern.indexOf('{'));
            String part2 = pattern.substring(pattern.indexOf('}') + 1);
            builder.append(part1).append(arg).append(part2);
        }
        return builder.toString();
    }
}
