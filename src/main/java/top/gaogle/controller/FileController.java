package top.gaogle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.gaogle.framework.annotation.Log;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.enums.BusinessTypeEnum;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.enums.OperatorTypeEnum;
import top.gaogle.pojo.enums.RegisterInfoStatusEnum;
import top.gaogle.service.FileService;

import javax.servlet.http.HttpServletResponse;

/**
 * 文件管理
 *
 * @author goge
 * @since 1.0.0
 */
@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 文件上传
     */
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/upload")
    public ResponseEntity<I18nResult<String>> upload(MultipartFile file) {
        return fileService.upload(file).toResponseEntity();
    }

    /**
     * 图片上传
     */
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/upload/picture")
    public ResponseEntity<I18nResult<String>> uploadPicture(MultipartFile file) {
        return fileService.uploadPicture(file).toResponseEntity();
    }

    /**
     * 文件访问路径
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/object_url")
    public ResponseEntity<I18nResult<String>> getObjectUrl(@RequestParam String objectName) {
        return fileService.getObjectUrl(objectName).toResponseEntity();
    }

    /**
     * 获取笔试准考证
     */
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/obtain_admission_ticket")
    public void obtainAdmissionTicket(HttpServletResponse response, @RequestParam String registerPublishId) {
        fileService.obtainAdmissionTicket(response, registerPublishId);
    }

    /**
     * 获取面试准考证
     */
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/obtain_admission_interview_ticket")
    public void obtainAdmissionInterviewTicket(HttpServletResponse response, @RequestParam String registerPublishId) {
        fileService.obtainAdmissionInterviewTicket(response, registerPublishId);
    }

    /**
     * 导出报名者全部信息Excel（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_VIEW_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_register_info")
    public void exportRegisterInfo(HttpServletResponse response, @RequestParam("registerPublishId") String registerPublishId,
                                   @RequestParam(value = "status", required = false) RegisterInfoStatusEnum status) {
        fileService.exportRegisterInfo(response, registerPublishId, status);
    }

//    /**
//     * 导出报名者基础信息Excel（企业端）
//     */
//    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_VIEW_ENTERPRISE.value())")
//    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
//    @GetMapping("/export_register_base_info")
//    public void exportRegisterBaseInfo(HttpServletResponse response, @RequestParam String registerPublishId) {
//        fileService.exportRegisterBaseInfo(response, registerPublishId);
//    }

    /**
     * 导出拟录用录入模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_offer_template")
    public void exportOfferTemplate(HttpServletResponse response, @RequestParam String registerPublishId) {
        fileService.exportOfferTemplate(response, registerPublishId);
    }

    /**
     * 导入拟录用(企业端)
     */
    @Log(title = "导入拟录用(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/upload_offer_template")
    public ResponseEntity<I18nResult<Boolean>> uploadOfferTemplate(@RequestParam("file") MultipartFile file, @RequestParam String registerPublishId) {
        return fileService.uploadOfferTemplate(file, registerPublishId).toResponseEntity();
    }

    /**
     * 导出面试成绩录入模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_interview_score_template")
    public void exportInterviewScoreTemplate(HttpServletResponse response, @RequestParam String registerPublishId) {
        fileService.exportInterviewScoreTemplate(response, registerPublishId);
    }

    /**
     * 导入面试成绩(企业端)
     */
    @Log(title = "导入面试成绩(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/upload_interview_score_template")
    public ResponseEntity<I18nResult<Boolean>> uploadInterviewScoreTemplate(@RequestParam("file") MultipartFile file, @RequestParam String registerPublishId) {
        return fileService.uploadInterviewScoreTemplate(file, registerPublishId).toResponseEntity();
    }

    /**
     * 导出面试信息录入模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_interview_template")
    public void exportInterviewTemplate(HttpServletResponse response, @RequestParam String registerPublishId) {
        fileService.exportInterviewTemplate(response, registerPublishId);
    }

    /**
     * 导入面试信息(企业端)
     */
    @Log(title = "导入面试信息(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/upload_interview_template")
    public ResponseEntity<I18nResult<Boolean>> uploadInterviewTemplate(@RequestParam("file") MultipartFile file, @RequestParam String registerPublishId) {
        return fileService.uploadInterviewTemplate(file, registerPublishId).toResponseEntity();
    }

    /**
     * 导出笔试成绩模板（企业端）
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_score_template")
    public void exportScoreTemplate(HttpServletResponse response, @RequestParam String registerPublishId) {
        fileService.exportScoreTemplate(response, registerPublishId);
    }

    /**
     * 导入笔试成绩(企业端)
     */
    @Log(title = "导入成绩(企业端)", businessType = BusinessTypeEnum.UPDATE, operatorType = OperatorTypeEnum.ENTERPRISE)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_SCORE_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @PostMapping("/upload_score_template")
    public ResponseEntity<I18nResult<Boolean>> uploadScoreTemplate(@RequestParam("file") MultipartFile file, @RequestParam String registerPublishId) {
        return fileService.uploadScoreTemplate(file, registerPublishId).toResponseEntity();
    }

    /**
     * 导出报名签到表
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_VIEW_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_register_sign")
    public void exportRegisterSign(HttpServletResponse response, @RequestParam String registerPublishId,
                                   @RequestParam String spotId) {
        fileService.exportRegisterSign(response, registerPublishId, spotId);
    }

    /**
     * 导出报名所有附件
     */
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).REGISTER_INFO_VIEW_ENTERPRISE.value())")
    @RateLimiter(time = 120, count = 5, limitType = LimitType.IP)
    @GetMapping("/export_register_attachment")
    public void exportRegisterAttachment(HttpServletResponse response, @RequestParam String registerPublishId,
                                         @RequestParam(value = "status", required = false) RegisterInfoStatusEnum status) {
        fileService.exportRegisterAttachment(response, registerPublishId, status);
    }
}
