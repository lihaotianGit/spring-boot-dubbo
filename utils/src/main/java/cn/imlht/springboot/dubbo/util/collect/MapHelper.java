package cn.imlht.springboot.dubbo.util.collect;

import java.util.Map;
import java.util.Objects;

public class MapHelper {

    private MapHelper() {
    }

    public static boolean isBlank(Map map) {
        return Objects.isNull(map) || map.isEmpty();
    }

    public static boolean isNotBlank(Map map) {
        return !isBlank(map);
    }
}
