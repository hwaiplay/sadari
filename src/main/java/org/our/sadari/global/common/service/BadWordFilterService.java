package org.our.sadari.global.common.service;

import com.vane.badwordfiltering.BadWordFiltering;
import org.our.sadari.global.common.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
public class BadWordFilterService {

    private BadWordFiltering badWordFiltering;

    public boolean hasBadWord(String value) {
        if (StringUtil.isEmpty(value)) {
            return false;
        }

        return getBadWordFiltering().blankCheck(value);
    }

    private BadWordFiltering getBadWordFiltering() {
        if (StringUtil.isEmpty(badWordFiltering)) {
            badWordFiltering = new BadWordFiltering();
        }

        return badWordFiltering;
    }
}
