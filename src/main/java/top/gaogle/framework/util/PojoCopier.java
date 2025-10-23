/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.gaogle.framework.util;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <p>
 *     业务实体对象的浅复制工具，要求对象具有 getter/setter 和 无参构造方法。
 * </p>
 *
 * @author gaogle
 * @since 1.0.0
 */
public class PojoCopier {

    // ----- copy object ----- beginning
    /**
     * 将源对象复制为指定类型的新对象，源对象中值为 {@code null} 的字段也将被复制
     *
     * @param source 源对象
     * @param targetClass 目标对象类型
     * @param ignoredSourceFields 指定忽略源对象中的字段名
     * @param <T> 目标对象泛型
     * @return 指定类型的新对象
     * @throws Exception 任意异常
     */
    public static <T> T from(Object source, Class<T> targetClass, String... ignoredSourceFields) throws Exception {
        T target = targetClass.getDeclaredConstructor().newInstance();
        copyProperties(source, target, ignoredSourceFields);
        return target;
    }

    /**
     * 将源对象复制为指定类型的新对象
     *
     * @param source 源对象
     * @param targetClass 目标对象类型
     * @param ignoreSourceNullValue 是否忽略源对象中值为 {@code null} 的字段
     * @param ignoredSourceFields 指定忽略源对象中的字段名
     * @param <T> 目标对象泛型
     * @return 指定类型的新对象
     * @throws Exception 任意异常
     */
    public static <T> T from(Object source, Class<T> targetClass, boolean ignoreSourceNullValue, String... ignoredSourceFields) throws Exception {
        T target = targetClass.getDeclaredConstructor().newInstance();
        copyProperties(source, target, ignoreSourceNullValue, ignoredSourceFields);
        return target;
    }

    /**
     * 将源对象复制为指定类型的新对象，源对象中值为 {@code null} 的字段也将被复制
     *
     * @param source 源对象
     * @param targetClass 目标对象类型
     * @param option 复制选项
     * @param <T> 目标对象泛型
     * @return 指定类型的新对象
     * @throws Exception 任意异常
     */
    public static <T> T from(Object source, Class<T> targetClass, CopyOption option) throws Exception {
        T target = targetClass.getDeclaredConstructor().newInstance();
        copyProperties(source, target, option);
        return target;
    }
    // ----- copy object ----- ending

    // ----- copy collection ----- beginning
    /**
     * 将源对象集合复制为指定类型的新对象集合
     *
     * @param sourceCollection 源对象的集合
     * @param targetClass 目标对象的类型
     * @param ignoredSourceFields 指定忽略源对象中的字段名
     * @param <E> 目标对象的泛型
     * @param <C> 目标集合类型，{@link Collection} 的实现类
     * @return 目标对象的集合
     * @throws Exception 任意异常
     */
    public static <E, C extends Collection<E>> C from(Collection<?> sourceCollection, Class<E> targetClass, String... ignoredSourceFields) throws Exception {
        return from(sourceCollection,
                targetClass,
                CopyOption.builder().ignoredSourceFields(ignoredSourceFields).build()
        );
    }

    /**
     * 将源对象集合复制为指定类型的新对象集合
     *
     * @param sourceCollection 源对象的集合
     * @param targetClass 目标对象的类型
     * @param ignoreSourceNullValue 是否忽略源对象中值为 {@code null} 的字段
     * @param ignoredSourceFields 指定忽略源对象中的字段名
     * @param <E> 目标对象的泛型
     * @param <C> 目标集合类型，{@link Collection} 的实现类
     * @return 目标对象的集合
     * @throws Exception 任意异常
     */
    public static <E, C extends Collection<E>> C from(Collection<?> sourceCollection, Class<E> targetClass, boolean ignoreSourceNullValue, String... ignoredSourceFields) throws Exception {
        return from(sourceCollection,
                targetClass,
                CopyOption.builder().ignoreSourceNullValue(ignoreSourceNullValue).ignoredSourceFields(ignoredSourceFields).build()
        );
    }

    /**
     * 将源对象集合复制为指定类型的新对象集合
     *
     * @param sourceCollection 源对象的集合
     * @param targetClass 目标对象的类型
     * @param option 复制选项
     * @param <E> 目标对象的泛型
     * @param <C> 目标集合类型，{@link Collection} 的实现类
     * @return 目标对象的集合
     * @throws Exception 任意异常
     */
    @SuppressWarnings("unchecked")
    public static <E, C extends Collection<E>> C from(Collection<?> sourceCollection, Class<E> targetClass, CopyOption option) throws Exception {
        if (sourceCollection == null) {
            return null;
        }
        if (sourceCollection.size() == 0) {
            return (C) sourceCollection.getClass().newInstance();
        }
        C targetCollection = (C) sourceCollection.getClass().newInstance();
        for (Object source : sourceCollection) {
            E target = targetClass.getDeclaredConstructor().newInstance();
            copyProperties(source, target, option);
            targetCollection.add(target);
        }
        return targetCollection;
    }

    // ----- copy collection ----- ending

    // ----- copy properties ----- beginning
    private static final Map<Class<?>, Map<String, PropertyDescriptor>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * 复制同名字段值，源对象中值为 {@code null} 的字段也将被复制
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoredSourceFields 指定忽略源对象中的字段名
     * @throws Exception 任意异常
     */
    public static void copyProperties(Object source, Object target, String... ignoredSourceFields) throws Exception {
        copyProperties(source, target, false, ignoredSourceFields);
    }

    /**
     * 复制同名字段值
     *
     * @param source 源对象
     * @param target 目标对象
     * @param ignoreSourceNullValue 是否忽略源对象中值为 {@code null} 的字段
     * @param ignoredSourceFields 指定忽略源对象中的字段名
     * @throws Exception 任意异常
     */
    public static void copyProperties(Object source, Object target, boolean ignoreSourceNullValue, String... ignoredSourceFields) throws Exception {
        copyProperties(source, target, CopyOption.builder().ignoreSourceNullValue(ignoreSourceNullValue).ignoredSourceFields(ignoredSourceFields).build());
    }

    /**
     * 复制同名字段值
     *
     * @param source 源对象
     * @param target 目标对象
     * @param option 复制选项
     * @throws Exception 任意异常
     */
    public static void copyProperties(Object source, Object target, CopyOption option) throws Exception {
        Map<String, PropertyDescriptor> sourcePds = getPropertyDescriptors(source.getClass());
        Map<String, PropertyDescriptor> targetPds = getPropertyDescriptors(target.getClass());

        boolean ignoreSourceNullValue = false;
        Set<String> ignoredSourceFields = null;
        Collection<FieldValueResolver> fieldValueResolvers = null;
        if (option != null) {
            ignoreSourceNullValue = option.isIgnoreSourceNullValue();
            ignoredSourceFields = option.getIgnoredSourceFields();
            fieldValueResolvers = option.getFieldValueResolvers();
        }

        for (Map.Entry<String, PropertyDescriptor> sourceEntry : sourcePds.entrySet()) {
            PropertyDescriptor targetPd = targetPds.get(sourceEntry.getKey());
            if (targetPd == null) {
                continue;
            }


            PropertyDescriptor sourcePd = sourceEntry.getValue();
            Object sourceValue = resolveSourceFieldValue(sourcePd, source, fieldValueResolvers);

            if (CollectionUtils.isNotEmpty(ignoredSourceFields) && ignoredSourceFields.contains(sourcePd.getName())) {
                continue;
            }

            if (ignoreSourceNullValue && sourceValue == null) {
                continue;
            }

            writePropertyValue(targetPd, target, sourceValue);
        }
    }

    private static Object resolveSourceFieldValue(PropertyDescriptor sourcePd, Object source, Collection<FieldValueResolver> fieldValueResolvers) throws Exception {
        Object sourceValue = readPropertyValue(sourcePd, source);

        if (CollectionUtils.isNotEmpty(fieldValueResolvers)) {
            for (FieldValueResolver resolver : fieldValueResolvers) {
                if (resolver instanceof FieldTypeValueResolver) {
                    FieldTypeValueResolver typeValueResolver = (FieldTypeValueResolver) resolver;
                    if (sourcePd.getPropertyType() == typeValueResolver.getSourceClass()) {
                        sourceValue = typeValueResolver.resolve(source, sourcePd.getName(), sourceValue);
                    }
                }
                if (resolver instanceof FieldNameValueResolver) {
                    FieldNameValueResolver nameValueResolver = (FieldNameValueResolver) resolver;
                    if (StringUtils.equals(sourcePd.getName(), nameValueResolver.getSourceFieldName())) {
                        sourceValue = nameValueResolver.resolve(source, sourcePd.getName(), sourceValue);
                    }
                }
            }
        }

        return sourceValue;
    }

    private static Object readPropertyValue(PropertyDescriptor pd, Object object) throws InvocationTargetException, IllegalAccessException {
        Method readMethod = pd.getReadMethod();
        if (readMethod != null) {
            return readMethod.invoke(object);
        }
        return null;
    }

    private static void writePropertyValue(PropertyDescriptor pd, Object object, Object value) throws InvocationTargetException, IllegalAccessException {
        Method writeMethod = pd.getWriteMethod();
        if (writeMethod != null) {
            writeMethod.invoke(object, value);
        }
    }

    private static Map<String, PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) throws IntrospectionException {
        Map<String, PropertyDescriptor> pdMap = CACHE.get(clazz);
        if (MapUtils.isEmpty(pdMap)) {
            pdMap = new HashMap<>();
            BeanInfo beanInfo = IntrospectAide.getBeanInfo(clazz, Object.class);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                pdMap.put(pd.getName(), pd);
            }
            CACHE.put(clazz, pdMap);
        }
        return pdMap;
    }
    // ----- copy properties ----- ending
}
