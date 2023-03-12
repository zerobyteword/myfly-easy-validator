package io.github.zerobyteword.easyvalidator;

import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class DoubleCheck {
    private static final PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();

    private Map<String, String> mappingMap = new HashMap<>();
    private Set<String> excludeSet = new HashSet<>();
    /**
     * 严格模式
     */
    private boolean useStrict = false;

    public DoubleCheck(Map<String, String> mappingMap, boolean useStrict, Set<String> excludeSet) {
        this.mappingMap = mappingMap;
        this.useStrict = useStrict;
        this.excludeSet = excludeSet;
    }


    @SneakyThrows
    private void loadBack2(Object sourceValue, Object target, String sourcePath, String targetPath, List<DiffItem> checkResult) {
        if (excludeSet.contains(sourcePath)) {
            return;
        }
        if (sourceValue == null) {
            Object targetValue = getValueByPath(target, targetPath, sourceValue);
            if (targetValue != null) {
                checkResult.add(new DiffItem(sourcePath, targetPath, sourceValue, targetValue));
            }
            return;
        }
        Class type = sourceValue.getClass();
        if (sourceValue instanceof Map) {
            Map map = (Map) sourceValue;
            for (Object key : map.keySet()) {
                Object sourceValue2 = map.get(key);
                String nextPath = sourcePath + "[" + key + "]";
                String computeTargetPath = getTargetPath(nextPath, targetPath + "[" + key + "]");
                loadBack2(sourceValue2, target, nextPath, computeTargetPath, checkResult);
            }
            return;
        } else if (sourceValue instanceof Collection) {
            Collection collection = (Collection) sourceValue;
            int index = 0;
            for (Object sourceValue2 : collection) {
                String nextPath = sourcePath + "[" + index + "]";
                String computeTargetPath = getTargetPath(nextPath, targetPath + "[" + index + "]");
                loadBack2(sourceValue2, target, nextPath, computeTargetPath, checkResult);
                index++;
            }
            return;
        } else if (type.getName().startsWith("java.") || type.isPrimitive()) {
            Object targetValue = getValueByPath(target, targetPath, sourceValue);
            if (!checkEquals(sourceValue, targetValue)) {
                checkResult.add(new DiffItem(sourcePath, targetPath, sourceValue, targetValue));
            }
            return;
        }
        PropertyDescriptor[] propertyDescriptors = propertyUtilsBean.getPropertyDescriptors(sourceValue.getClass());
        for (PropertyDescriptor descriptor : propertyDescriptors) {
            Object sourceValue2 = descriptor.getReadMethod().invoke(sourceValue);
            String nextPath = sourcePath + "." + descriptor.getName();
            String computeTargetPath = getTargetPath(nextPath, targetPath + "." + descriptor.getName());
            loadBack2(sourceValue2, target, nextPath, computeTargetPath, checkResult);
        }
    }

    private String getTargetPath(String sourcePath, String targetPath) {
        if (mappingMap.containsKey(sourcePath)) {
            return mappingMap.get(sourcePath);
        }
        return targetPath;
    }


    public List<DiffItem> check(Object source, Object target) {
        List<DiffItem> diffItemList = new ArrayList<>();
        String root = "$";
        String targetPath = getTargetPath(root, root);
        loadBack2(source, target, root, targetPath, diffItemList);
        return diffItemList;
    }

    private boolean checkEquals(Object sourceValue, Object targetValue) {
        if (!Objects.equals(sourceValue, targetValue)) {
            if (useStrict) {
                return false;
            } else {
                // 非严格模式
                if (sourceValue == null || targetValue == null) {
                    return false;
                } else {
                    // 尝试转成字符串
                    boolean flag = String.valueOf(sourceValue).equals(String.valueOf(targetValue));
                    if (!flag) {
                        Object nweSourceValue = sourceValue;
                        Object nweTargetValue = targetValue;
                        if (Boolean.class == sourceValue.getClass() || boolean.class == sourceValue.getClass()) {
                            nweSourceValue = (Boolean) sourceValue ? 1 : 0;
                        }
                        if (Boolean.class == targetValue.getClass() || boolean.class == targetValue.getClass()) {
                            nweTargetValue = (Boolean) targetValue ? 1 : 0;
                        }
                        flag = String.valueOf(nweSourceValue).equals(String.valueOf(nweTargetValue));
                    }
                    if (!flag) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static <S, T> DoubleCheckBuilder newBuilder() {
        return new DoubleCheckBuilder();
    }

    @SneakyThrows
    private Object getValueByPath(Object source, String path, Object sourceValue) {
        String[] names = StringUtils.splitPreserveAllTokens(path, ".[]");
        List<String> nameList = Arrays.stream(names)
                .filter(e -> !"$".equals(e))
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Object target = source;
        int index = 0;
        while (target != null && index < nameList.size()) {
            if (target instanceof List) {
                List list = (List) target;
                target = list.get(Integer.parseInt(nameList.get(index)));
            } else if (target instanceof Set) {
                Set set = (Set) target;
                if (set.contains(sourceValue)) {
                    target = sourceValue;
                } else {
                    target = null;
                }
            } else {
                target = propertyUtilsBean.getProperty(target, nameList.get(index));
            }
            index++;
        }
        return target;
    }




    static class DoubleCheckBuilder {

        private Map<String, String> fieldMapping = new HashMap<>();
        private Map<String, PropertyDescriptor> sourceMap;
        private Map<String, PropertyDescriptor> targetMap;
        private Set<String> excludeSet = new HashSet<>();
        private boolean useStrict = false;

        public <S, T> DoubleCheckBuilder() {
        }

        public DoubleCheckBuilder mapping(String s, String s1) {
            fieldMapping.put(s, s1);
            return this;
        }

        public DoubleCheckBuilder mapping(String sourcePath, String targetPath, SubMapping subMapping) {
            fieldMapping.put(sourcePath, targetPath);
            SubMappingHandler handler = new SubMappingHandler(fieldMapping, sourcePath, targetPath);
            subMapping.mapping(handler);
            return this;
        }


        public DoubleCheckBuilder useStrict(boolean flag) {
            useStrict = flag;
            return this;
        }

        public DoubleCheck build() {
            return new DoubleCheck(fieldMapping, useStrict, excludeSet);
        }

        private Map<String, String> buildMapping() {
            Map<String, String> mappingMap = new HashMap<>();
            Map<String, String> sourceNewMapping = buildNewMapping(sourceMap, fieldMapping);
            for (Map.Entry<String, String> entry : sourceNewMapping.entrySet()) {
                if (targetMap.containsKey(entry.getValue())) {
                    mappingMap.put(entry.getKey(), entry.getValue());
                }
            }

            for (Map.Entry<String, PropertyDescriptor> entry : sourceMap.entrySet()) {
                String key = entry.getKey();
                if (mappingMap.containsKey(key)) {
                    continue;
                }
                if (targetMap.containsKey(entry.getKey())) {
                    mappingMap.put(entry.getKey(), entry.getKey());
                }
            }
            return mappingMap;
        }

        private Map<String, String> buildNewMapping(Map<String, PropertyDescriptor> sourceMap, Map<String, String> fieldMapping) {
            Map<String, String> newMap = new HashMap<String, String>();
            for (Map.Entry<String, PropertyDescriptor> entry : sourceMap.entrySet()) {
                for (Map.Entry<String, String> entry2 : fieldMapping.entrySet()) {
                    if (entry.getKey().startsWith(entry2.getKey())) {
                        String newKey = entry2.getValue() + entry.getKey().substring(entry2.getKey().length());
                        newMap.put(entry.getKey(), newKey);
                    }
                }
            }
            return newMap;
        }

        private <S, T> void load(Class<S> sClass, Class<T> tClass) {
            Map<String, PropertyDescriptor> sourceMap = new HashMap<String, PropertyDescriptor>();
            String root = "$";
            loadBack(sClass, sourceMap, root);
            Map<String, PropertyDescriptor> targetMap = new HashMap<String, PropertyDescriptor>();
            loadBack(tClass, targetMap, root);
            this.sourceMap = sourceMap;
            this.targetMap = targetMap;
        }


        private static void loadBack(Class<?> clz, Map<String, PropertyDescriptor> propertyDescriptorMap, String path) {
            PropertyDescriptor[] propertyDescriptors = propertyUtilsBean.getPropertyDescriptors(clz);
            for (PropertyDescriptor descriptor : propertyDescriptors) {
                Class type = descriptor.getPropertyType();
                if (type.getName().startsWith("java.") || type.isPrimitive()) {
                    propertyDescriptorMap.put(path + "." + descriptor.getName(), descriptor);
                } else {
                    loadBack(descriptor.getPropertyType(), propertyDescriptorMap, path + "." + descriptor.getName());
                }
            }
        }

        public DoubleCheckBuilder exclude(String path) {
            excludeSet.add(path);
            return this;
        }
    }

    static class MappingHelper {
        private String sourcePath;
        private String targetPath;
        private Map<String, String> mappingMap = new HashMap<>();

        public MappingHelper(String sourcePath, String targetPath) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            this.mappingMap.put(sourcePath, targetPath);
        }

        public static MappingHelper of(String s, String s1) {
            return new MappingHelper(s, s1);
        }

        public MappingHelper mapping(String sourcePath, String targetPath) {
            mappingMap.put(this.sourcePath + "." + sourcePath, this.targetPath + "." + targetPath);
            return this;
        }
    }

    @FunctionalInterface
    interface SubMapping {
        void mapping(SubMappingHandler handler);
    }

    static class SubMappingHandler {
        private final Map<String, String> fieldMapping;
        private final String sourcePath;
        private final String targetPath;

        public SubMappingHandler(Map<String, String> fieldMapping, String sourcePath, String targetPath) {
            this.fieldMapping = fieldMapping;
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
        }

        public SubMappingHandler mapping(String subSourcePath, String subTargetPath) {
            fieldMapping.put(this.sourcePath + "." + subSourcePath, this.targetPath + "." + subTargetPath);
            return this;
        }

        public SubMappingHandler mapping(String subSourcePath, String subTargetPath, SubMapping subMapping) {
            fieldMapping.put(sourcePath, targetPath);
            SubMappingHandler handler = new SubMappingHandler(fieldMapping, sourcePath, targetPath);
            subMapping.mapping(handler);
            return this;
        }
    }
}
