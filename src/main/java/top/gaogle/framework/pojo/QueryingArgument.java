package top.gaogle.framework.pojo;/*
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


import org.springframework.cglib.proxy.Enhancer;
import top.gaogle.framework.interceptor.QueryingArgumentInterceptor;
import top.gaogle.framework.util.PojoCopier;

/**
 * <p>
 *     TODO-gaogle QueryingArg
 * </p>
 *
 * @author gaogle
 * @since 2.0.0
 */
public class QueryingArgument implements SuperQuerying {

    /**
     * 分页参数：数据索引偏移量
     */
    private Integer offset;
    /**
     * 分页参数：数据条数
     */
    private Integer limit;
    /**
     * 分页参数：页码
     */
    private Integer pageNum;
    /**
     * 分页参数：每页数据条数
     */
    private Integer pageSize;
    /**
     * 查询参数：模糊查询参数
     */
    private String search;
    /**
     * 排序参数：排序字段
     */
    private String sort;
    /**
     * 排序参数：排序顺序（asc/desc）
     */
    private String order;

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String getSearch() {
        return search;
    }

    @Override
    public void setSearch(String search) {
        this.search = search;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public void setSort(String sort) {
        this.sort = sort;
    }
    @Override

    public String getOrder() {
        return order;
    }

    @Override
    public void setOrder(String order) {
        this.order = order;
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxy(T target) {
        QueryingArgument querying = new QueryingArgument();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass());
        enhancer.setCallback(new QueryingArgumentInterceptor(querying));
        Object proxy = enhancer.create();
        try {
            PojoCopier.copyProperties(target, proxy);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return (T) proxy;
    }

    @SuppressWarnings("unchecked")
    public static <T> T proxy(Class<T> targetClass) {
        QueryingArgument querying = new QueryingArgument();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback(new QueryingArgumentInterceptor(querying));
        return (T) enhancer.create();
    }

}
