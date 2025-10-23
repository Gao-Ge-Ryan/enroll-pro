package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.Querying;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.framework.pojo.PageModel;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.model.CommentModel;
import top.gaogle.pojo.param.CommentEditParam;
import top.gaogle.pojo.param.CommentQueryParam;
import top.gaogle.service.CommentService;

import java.util.List;

/**
 * 问题反馈
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 获取所有评论
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/all")
    public ResponseEntity<I18nResult<List<CommentModel>>> getAllComments() {
        return commentService.getAllComments().toResponseEntity();
    }

    /**
     * 新增评论
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> addComment(@RequestBody CommentEditParam editParam) {
        return commentService.addComment(editParam).toResponseEntity();
    }

    /**
     * 修改评论
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> putComment(@RequestBody CommentEditParam editParam) {
        return commentService.putComment(editParam).toResponseEntity();
    }

    /**
     * 分页条件查询
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority('feedback_view')")
    @GetMapping("/page")
    public ResponseEntity<I18nResult<PageModel<CommentModel>>> queryByPageAndCondition(@Querying CommentQueryParam queryParam) {
        return commentService.queryByPageAndCondition(queryParam).toResponseEntity();
    }

    /**
     * 根据id查询详情
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<CommentModel>> queryOneById(@PathVariable("id") String id) {
        return commentService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据id删除
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id) {
        return commentService.deleteById(id).toResponseEntity();
    }

}
