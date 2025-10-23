package top.gaogle.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import top.gaogle.framework.annotation.RateLimiter;
import top.gaogle.framework.i18n.I18nResult;
import top.gaogle.pojo.dto.SeatInfoDTO;
import top.gaogle.pojo.enums.LimitType;
import top.gaogle.pojo.model.PublishSpotModel;
import top.gaogle.pojo.param.PublishSpotEditParam;
import top.gaogle.service.PublishSpotService;

import java.util.List;

/**
 * 发布报名下考点管理
 *
 * @author gaogle
 * @since 1.0.0
 */
@RestController
@RequestMapping("/publish_spot")
public class PublishSpotController {

    private final PublishSpotService publishSpotService;

    @Autowired
    public PublishSpotController(PublishSpotService publishSpotService) {
        this.publishSpotService = publishSpotService;
    }

    /**
     * 添加考点(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @PostMapping
    public ResponseEntity<I18nResult<Boolean>> insert(@RequestBody PublishSpotEditParam editParam) {
        return publishSpotService.insert(editParam).toResponseEntity();
    }

    /**
     * 修改考点(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @PutMapping
    public ResponseEntity<I18nResult<Boolean>> putSpotInfo(@RequestBody PublishSpotEditParam editParam) {
        return publishSpotService.putSpotInfo(editParam).toResponseEntity();
    }

    /**
     * 根据id查询详情(用户端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @GetMapping("/{id}")
    public ResponseEntity<I18nResult<PublishSpotModel>> queryOneById(@PathVariable("id") String id) {
        return publishSpotService.queryOneById(id).toResponseEntity();
    }

    /**
     * 根据register_publish_id查询发布对应考点(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @GetMapping("/register_publish")
    public ResponseEntity<I18nResult<List<PublishSpotModel>>> queryByRegisterPublishId(@RequestParam("registerPublishId") String registerPublishId) {
        return publishSpotService.queryByRegisterPublishId(registerPublishId).toResponseEntity();
    }

    /**
     * 根据register_publish_id查询发布对应考点用到的所有考场号(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @GetMapping("/register_publish/room")
    public ResponseEntity<I18nResult<List<String>>> queryRoomByRegisterPublishId(@RequestParam("registerPublishId") String registerPublishId,
                                                                                           @RequestParam("spotId") String spotId) {
        return publishSpotService.queryRoomByRegisterPublishId(registerPublishId,spotId).toResponseEntity();
    }

    /**
     * 根据register_publish_id查询发布对应考点用到的所有考场下座号信息(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @GetMapping("/register_publish/seat")
    public ResponseEntity<I18nResult<List<SeatInfoDTO>>> querySeatByRegisterPublishId(@RequestParam("registerPublishId") String registerPublishId,
                                                                                     @RequestParam("spotId") String spotId,
                                                                                     @RequestParam("roomNumber")String roomNumber) {
        return publishSpotService.querySeatByRegisterPublishId(registerPublishId,spotId,roomNumber).toResponseEntity();
    }

    /**
     * 根据id删除(企业端)
     */
    @RateLimiter(time = 60, count = 60, limitType = LimitType.IP)
    @PreAuthorize("hasAuthority(T(top.gaogle.pojo.enums.AuthorityEnum).ENTERPRISE_SPOT_ENTERPRISE.value())")
    @DeleteMapping("/{id}")
    public ResponseEntity<I18nResult<Boolean>> deleteById(@PathVariable("id") String id, @RequestParam("registerPublishId") String registerPublishId) {
        return publishSpotService.deleteById(id, registerPublishId).toResponseEntity();
    }


}
