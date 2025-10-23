package top.gaogle.framework.pojo;

public interface SuperQuerying {

    default Integer getOffset() {
        return null;
    }

    default void setOffset(Integer offset) {

    }

    default Integer getLimit() {
        return null;
    }

    default void setLimit(Integer limit) {

    }

    default Integer getPageNum() {
        return null;
    }

    default void setPageNum(Integer pageNum) {

    }

    default Integer getPageSize() {
        return null;
    }

    default void setPageSize(Integer pageSize) {

    }

    default String getSearch() {
        return null;
    }

    default void setSearch(String search) {

    }

    default String getSort() {
        return null;
    }

    default void setSort(String sort) {

    }

    default String getOrder() {
        return null;
    }

    default void setOrder(String order) {

    }

}
