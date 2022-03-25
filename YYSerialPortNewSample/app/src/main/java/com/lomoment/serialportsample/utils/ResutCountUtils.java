package com.lomoment.serialportsample.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author libin
 * @date 2019/3/14
 * @Description
 */

public class ResutCountUtils {
    private Map<String, Integer> resultMap = new HashMap<>();

    /**
     * statistics
     *
     * @param code
     */
    public void countResult(String code) {
        if (resultMap.containsKey(code)) {
            resultMap.put(code, resultMap.get(code) + 1);
        } else {
            resultMap.put(code, 1);
        }
    }


    public String toString() {
        return resultMap.toString();
    }

}
