package configuration;

import configuration.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class ConfigNode {

    private final Config config;

    private final Map<String, Object> map;

    public ConfigNode(Config config) {
        this.config = Objects.requireNonNull(config);
        this.map = config.getOptions().getMapFactory().get();
    }

    public Config getConfig() {
        return config;
    }

    public Object get(String path) {
        if (StringUtils.isNullOrEmpty(path)) {
            return this;
        }

        PathParser.Key[] keys = PathParser.parse(path, config.getOptions());

        ConfigNode node = this;
        for (int i = 0; i < keys.length - 1; i++) {
            node = node.getNodeInternal(keys[i]);
            if (node == null) {
                return null;
            }
        }

        return node.getInternal(keys[keys.length - 1]);
    }

    public Object get(String path, Object defaultValue) {
        Object value = get(path);
        return value != null ? value : defaultValue;
    }

    public Object get(String path, Supplier<Object> defaultValue) {
        Object value = get(path);
        return value != null ? value : defaultValue.get();
    }

    public Object set(String path, Object value) {
        PathParser.Key[] keys = PathParser.parse(path, config.getOptions());

        if (keys.length == 1) {
            return setInternal(keys[0], value);
        }

        ConfigNode node = this;
        for (int i = 0; i < keys.length - 1; i++) {
            ConfigNode child = node.getNodeInternal(keys[i]);
            if (child == null) {
                if (value == null) {
                    return null;
                }
                node = node.createNodeInternal(keys[i]);
            } else {
                node = child;
            }
        }

        return node.setInternal(keys[keys.length - 1], value);
    }

    protected ConfigNode getNodeInternal(PathParser.Key key) {
        Object value = getInternal(key);
        return value instanceof ConfigNode ? (ConfigNode) value : null;
    }

    protected ConfigNode createNodeInternal(PathParser.Key key) {
        ConfigNode node = new ConfigNode(config);
        setInternal(key, node);
        return node;
    }

    protected Object getInternal(PathParser.Key key) {
        if (key.isList()) {
            Object value = map.get(key.getName());
            int[] indexs = key.getIndexs();
            for (int i = 0; i < indexs.length; i++) {
                if (!(value instanceof List))
                    return null;
                value = ((List<Object>) value).get(i);
            }
            return value;
        } else {
            return map.get(key.getName());
        }
    }

    protected Object setInternal(PathParser.Key key, Object value) {
        if (key.isList()) {
            Supplier<List<Object>> listFactory = config.getOptions().getListFactory();

            List<Object> list = getListInternal(key.getName());
            int[] indexs = key.getIndexs();
            for (int i = 0; i < indexs.length - 1; i++) {
                Object childList = list.get(indexs[i]);
                if (childList instanceof List) {
                    list = (List<Object>) childList;
                } else {
                    List<Object> childList2 = config.getOptions().getListFactory().get();
                    list.set(indexs[i], childList2);
                    list = childList2;
                }
            }
            return list.get(indexs[indexs.length - 1]);
        } else {
            return map.put(key.getName(), value);
        }
    }

    protected List<Object> getListInternal(String key) {
        Object listObj = map.get(key);
        if (listObj instanceof List) {
            return (List<Object>) listObj;
        } else {
            List<Object> list = config.getOptions().getListFactory().get();
            map.put(key, list);
            return list;
        }
    }
}
