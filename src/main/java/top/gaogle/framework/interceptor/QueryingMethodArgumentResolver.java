
package top.gaogle.framework.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.*;
import org.springframework.validation.annotation.ValidationAnnotationUtils;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.multipart.support.StandardServletPartUtils;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.pojo.QueryingArgument;
import top.gaogle.framework.util.NamingConverter;
import top.gaogle.framework.util.PojoCopier;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author gaogle
 * @since 2.0.0
 */
public class QueryingMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(@NonNull MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Querying.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter, ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Assert.state(mavContainer != null, "QueryingMethodArgumentResolver requires ModelAndViewContainer");
        Assert.state(binderFactory != null, "QueryingMethodArgumentResolver requires WebDataBinderFactory");

        String name = ModelFactory.getNameForParameter(parameter);

        Object attribute;
        BindingResult bindingResult = null;

        try {
            attribute = createAttribute(name, parameter, binderFactory, webRequest);
        } catch (BindException ex) {
            if (isBindExceptionRequired(parameter)) {
                throw ex;
            }
            if (parameter.getParameterType() == Optional.class) {
                attribute = Optional.empty();
            } else {
                attribute = ex.getTarget();
            }
            bindingResult = ex.getBindingResult();
        }

        if (bindingResult == null) {
            WebDataBinder binder = binderFactory.createBinder(webRequest, attribute, name);
            if (binder.getTarget() != null) {
                if (!mavContainer.isBindingDisabled(name)) {
                    bindRequestParameters(binder, webRequest);
                }
                validateIfApplicable(binder, parameter);
                if (binder.getBindingResult().hasErrors() && isBindExceptionRequired(binder, parameter)) {
                    throw new BindException(binder.getBindingResult());
                }
            }
            if (!parameter.getParameterType().isInstance(attribute)) {
                attribute = binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType(), parameter);
            }
            bindingResult = binder.getBindingResult();
        }

        Map<String, Object> bindingResultModel = bindingResult.getModel();
        mavContainer.removeAttributes(bindingResultModel);
        mavContainer.addAllAttributes(bindingResultModel);

        // 使用请求中的参数构建 SuperQuerying 对象
        QueryingArgument querying = new QueryingArgument();
        WebDataBinder binder = binderFactory.createBinder(webRequest, querying, name);
        bindRequestParameters(binder, webRequest);
        // 验证 sort 和 order 参数
        validateSortOrderField(querying, parameter);

        // 创建代理对象，混入 SuperQuerying 参数
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(parameter.getParameterType());
        enhancer.setCallback(new QueryingArgumentInterceptor(querying));
        Object proxy = enhancer.create();
        PojoCopier.copyProperties(attribute, proxy);

        return proxy;
    }

    /**
     * 创建参数模型，以便后续通过 bean 属性进行参数绑定。
     * 默认使用模型类的公共无参构造函数，
     * 但也可以识别 {@link java.beans.ConstructorProperties} 注解，使用请求参数与构造函数的参数按名称关联来查找构造函数，
     * 如果找不到这样的构造函数，则使用默认构造函数（即使是非公共的）。
     *
     * @see #constructAttribute(Constructor, String, MethodParameter, WebDataBinderFactory, NativeWebRequest)
     */
    protected Object createAttribute(String attributeName,
                                     MethodParameter parameter,
                                     WebDataBinderFactory binderFactory,
                                     NativeWebRequest webRequest) throws Exception {

        MethodParameter nestedParameter = parameter.nestedIfOptional();
        Class<?> clazz = nestedParameter.getNestedParameterType();

        Constructor<?> ctor = BeanUtils.getResolvableConstructor(clazz);
        Object attribute = constructAttribute(ctor, attributeName, parameter, binderFactory, webRequest);
        if (parameter != nestedParameter) {
            attribute = Optional.of(attribute);
        }
        return attribute;
    }

    /**
     * 使用给定构造函数构造一个新的模型实例
     */
    @SuppressWarnings("serial")
    protected Object constructAttribute(Constructor<?> ctor,
                                        String attributeName,
                                        MethodParameter parameter,
                                        WebDataBinderFactory binderFactory,
                                        NativeWebRequest webRequest) throws Exception {

        if (ctor.getParameterCount() == 0) {
            // 默认构造函数
            return BeanUtils.instantiateClass(ctor);
        }

        // 有参构造函数 -> 从请求参数中解析构造参数
        String[] paramNames = BeanUtils.getParameterNames(ctor);
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        WebDataBinder binder = binderFactory.createBinder(webRequest, null, attributeName);
        String fieldDefaultPrefix = binder.getFieldDefaultPrefix();
        String fieldMarkerPrefix = binder.getFieldMarkerPrefix();
        boolean bindingFailure = false;
        Set<String> failedParams = new HashSet<>(4);

        for (int i = 0; i < paramNames.length; i++) {
            String paramName = paramNames[i];
            Class<?> paramType = paramTypes[i];
            Object value = webRequest.getParameterValues(paramName);

            if (ObjectUtils.isArray(value) && Array.getLength(value) == 1) {
                value = Array.get(value, 0);
            }

            if (value == null) {
                if (fieldDefaultPrefix != null) {
                    value = webRequest.getParameter(fieldDefaultPrefix + paramName);
                }
                if (value == null) {
                    if (fieldMarkerPrefix != null && webRequest.getParameter(fieldMarkerPrefix + paramName) != null) {
                        value = binder.getEmptyValue(paramType);
                    } else {
                        value = resolveConstructorArgument(paramName, paramType, webRequest);
                    }
                }
            }

            try {
                MethodParameter methodParam = new FieldAwareConstructorParameter(ctor, i, paramName);
                if (value == null && methodParam.isOptional()) {
                    args[i] = (methodParam.getParameterType() == Optional.class ? Optional.empty() : null);
                } else {
                    args[i] = binder.convertIfNecessary(value, paramType, methodParam);
                }
            } catch (TypeMismatchException ex) {
                ex.initPropertyName(paramName);
                args[i] = null;
                failedParams.add(paramName);
                binder.getBindingResult().recordFieldValue(paramName, paramType, value);
                binder.getBindingErrorProcessor().processPropertyAccessException(ex, binder.getBindingResult());
                bindingFailure = true;
            }
        }

        if (bindingFailure) {
            BindingResult result = binder.getBindingResult();
            for (int i = 0; i < paramNames.length; i++) {
                String paramName = paramNames[i];
                if (!failedParams.contains(paramName)) {
                    Object value = args[i];
                    result.recordFieldValue(paramName, paramTypes[i], value);
                    validateValueIfApplicable(binder, parameter, ctor.getDeclaringClass(), paramName, value);
                }
            }
            if (!parameter.isOptional()) {
                try {
                    Object target = BeanUtils.instantiateClass(ctor, args);
                    throw new BindException(result) {
                        @Override
                        public Object getTarget() {
                            return target;
                        }
                    };
                } catch (BeanInstantiationException ex) {
                    // swallow and proceed without target instance
                }
            }
            throw new BindException(result);
        }

        return BeanUtils.instantiateClass(ctor, args);
    }

    /**
     * 将请求参数绑定到目标对象
     */
    protected void bindRequestParameters(WebDataBinder binder, NativeWebRequest request) {
        ServletRequest servletRequest = request.getNativeRequest(ServletRequest.class);
        Assert.state(servletRequest != null, "No ServletRequest");
        ServletRequestDataBinder servletBinder = (ServletRequestDataBinder) binder;
        servletBinder.bind(servletRequest);
    }

    @Nullable
    public Object resolveConstructorArgument(String paramName, Class<?> paramType, NativeWebRequest request) {

        MultipartRequest multipartRequest = request.getNativeRequest(MultipartRequest.class);
        if (multipartRequest != null) {
            List<MultipartFile> files = multipartRequest.getFiles(paramName);
            if (!files.isEmpty()) {
                return (files.size() == 1 ? files.get(0) : files);
            }
        } else if (StringUtils.startsWithIgnoreCase(
                request.getHeader(HttpHeaders.CONTENT_TYPE), MediaType.MULTIPART_FORM_DATA_VALUE)) {
            HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
            if (servletRequest != null && HttpMethod.POST.matches(servletRequest.getMethod())) {
                List<Part> parts = StandardServletPartUtils.getParts(servletRequest, paramName);
                if (!parts.isEmpty()) {
                    return (parts.size() == 1 ? parts.get(0) : parts);
                }
            }
        }
        return null;
    }

    /**
     * 验证模型属性，
     * 默认检查 {@link javax.validation.Valid}、{@link org.springframework.validation.annotation.Validated} 以及名称以 "Valid" 开头的自定义注解。
     */
    protected void validateIfApplicable(WebDataBinder binder, MethodParameter parameter) {
        for (Annotation ann : parameter.getParameterAnnotations()) {
            Object[] validationHints = ValidationAnnotationUtils.determineValidationHints(ann);
            if (validationHints != null) {
                binder.validate(validationHints);
                break;
            }
        }
    }

    /**
     * 验证指定候选值，
     * 默认检查 {@link javax.validation.Valid}、{@link org.springframework.validation.annotation.Validated} 以及名称以 "Valid" 开头的自定义注解。
     */
    protected void validateValueIfApplicable(WebDataBinder binder, MethodParameter parameter,
                                             Class<?> targetType, String fieldName, @Nullable Object value) {

        for (Annotation ann : parameter.getParameterAnnotations()) {
            Object[] validationHints = ValidationAnnotationUtils.determineValidationHints(ann);
            if (validationHints != null) {
                for (Validator validator : binder.getValidators()) {
                    if (validator instanceof SmartValidator) {
                        try {
                            ((SmartValidator) validator).validateValue(targetType, fieldName, value,
                                    binder.getBindingResult(), validationHints);
                        } catch (IllegalArgumentException ex) {
                            // No corresponding field on the target class...
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * 是否在验证错误时引发致命的绑定异常。
     * 默认实现委托给 isBindExceptionRequired(MethodParameter)。
     */
    protected boolean isBindExceptionRequired(WebDataBinder binder, MethodParameter parameter) {
        return isBindExceptionRequired(parameter);
    }

    /**
     * 是否在验证错误时引发致命的绑定异常。
     */
    protected boolean isBindExceptionRequired(MethodParameter parameter) {
        int i = parameter.getParameterIndex();
        Class<?>[] paramTypes = parameter.getExecutable().getParameterTypes();
        boolean hasBindingResult = (paramTypes.length > (i + 1) && Errors.class.isAssignableFrom(paramTypes[i + 1]));
        return !hasBindingResult;
    }

    protected void validateSortOrderField(QueryingArgument querying, MethodParameter parameter) {
        if (StringUtils.isNotBlank(querying.getSort())) {
            Querying anno = parameter.getParameterAnnotation(Querying.class);
            if (anno != null) {
                Class<?> normClass = anno.norm();
                if (normClass == Querying.class) {
                    normClass = parameter.getParameterType();
                }
                Field sortField = findFieldByName(normClass, querying.getSort());
                if (sortField == null) {
                    querying.setSort(null);
                } else {
                    if (anno.sortFieldNaming() == Querying.Naming.SNAKE) {
                        querying.setSort(NamingConverter.camel2Snake(querying.getSort()));
                    }
                }
            }
        }
        if (!StringUtils.equalsAnyIgnoreCase(querying.getOrder(), "asc", "desc")) {
            querying.setOrder(null);
        }
        Integer pageNum = querying.getPageNum();
        Integer pageSize = querying.getPageSize();
        if (pageNum == null || pageNum < 1) {
            querying.setPageNum(1);
        }
        if (pageSize == null || pageSize < 1) {
            querying.setPageSize(Integer.MAX_VALUE);
        }
    }

    private static Field findFieldByName(Class<?> clazz, String name) {
        while (clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (StringUtils.equals(field.getName(), name)) {
                    return field;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * 用于检查字段注解
     */
    private static class FieldAwareConstructorParameter extends MethodParameter {

        private final String parameterName;

        @Nullable
        private volatile Annotation[] combinedAnnotations;

        public FieldAwareConstructorParameter(Constructor<?> constructor, int parameterIndex, String parameterName) {
            super(constructor, parameterIndex);
            this.parameterName = parameterName;
        }

        @Override
        public Annotation[] getParameterAnnotations() {
            Annotation[] anns = this.combinedAnnotations;
            if (anns == null) {
                anns = super.getParameterAnnotations();
                try {
                    Field field = getDeclaringClass().getDeclaredField(this.parameterName);
                    Annotation[] fieldAnns = field.getAnnotations();
                    if (fieldAnns.length > 0) {
                        List<Annotation> merged = new ArrayList<>(anns.length + fieldAnns.length);
                        merged.addAll(Arrays.asList(anns));
                        for (Annotation fieldAnn : fieldAnns) {
                            boolean existingType = false;
                            for (Annotation ann : anns) {
                                if (ann.annotationType() == fieldAnn.annotationType()) {
                                    existingType = true;
                                    break;
                                }
                            }
                            if (!existingType) {
                                merged.add(fieldAnn);
                            }
                        }
                        anns = merged.toArray(new Annotation[0]);
                    }
                } catch (NoSuchFieldException | SecurityException ex) {
                    // ignore
                }
                this.combinedAnnotations = anns;
            }
            return anns;
        }

        @Override
        public String getParameterName() {
            return this.parameterName;
        }
    }
}
