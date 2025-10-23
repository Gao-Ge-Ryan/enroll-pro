package top.gaogle.dao.slave;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import top.gaogle.pojo.dto.*;
import top.gaogle.pojo.model.DynamicRegisterInfoModel;
import top.gaogle.pojo.param.DynamicRegisterInfoQueryParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface DynamicRegisterInfoMapper {

    int insertDynamic(String tableName, List<String> keys, List<Object> values);

    int deleteDynamic();

    int updateDynamic(@Param("tableName") String tableName, @Param("setClauses") List<Map<String, Object>> setClauses,
                      @Param("conditions") List<Map<String, Object>> conditions);

    List<Map<String, Object>> selectDynamic(@Param("tableName") String tableName, @Param("fields") List<String> fields,
                                            @Param("conditions") List<Map<String, Object>> conditions);

    List<Map<String, Object>> selectDynamicByQueryParam(@Param("tableName") String tableName, @Param("fields") List<String> fields,
                                                        @Param("conditions") List<Map<String, Object>> conditions, @Param("queryParam") DynamicRegisterInfoQueryParam queryParam);

    DynamicRegisterInfoModel queryModelByCreateBy(@Param("tableName") String tableName, @Param("createBy") String createBy);

    DynamicRegisterInfoModel queryModelById(@Param("tableName") String tableName, @Param("id") String id);

    int createTableDynamic(@Param("tableName") String tableName,
                           @Param("columns") List<Map<String, Object>> columns);

    List<DynamicRegisterInfoModel> selectBaseInfoByRegisterPublishId(@Param("tableName") String tableName, @Param("registerPublishId") String registerPublishId);

    List<DynamicRegisterInfoModel> queryBaseInfoByRegisterPublishIdAndStatus(@Param("tableName") String tableName, @Param("status") int status);

    List<DynamicRegisterInfoModel> queryBaseInfoByRegisterPublishIdAndStatusAndInterviewFlag(@Param("tableName") String tableName, @Param("status") int status, @Param("interviewFlag") Boolean interviewFlag);

    int updateScoreByUnicode(@Param("tableName") String tableName, @Param("updateScoreDTO") UpdateScoreDTO updateScoreDTO);

    int updateFinalScoreByUnicode(@Param("tableName") String tableName, @Param("updateFinalScoreDTO") UpdateFinalScoreDTO updateFinalScoreDTO);

    int updateInterviewScoreByUnicode(@Param("tableName") String tableName, @Param("interviewScore") BigDecimal interviewScore, @Param("updateBy") String updateBy, @Param("updateAt") Long updateAt,
                                      @Param("id") String id, @Param("idNumber") String idNumber);

    int updateOfferByUnicode(@Param("tableName") String tableName, @Param("offerFlag") Boolean offerFlag, @Param("offerExplain") String offerExplain, @Param("updateBy") String updateBy, @Param("updateAt") Long updateAt,
                             @Param("id") String id, @Param("idNumber") String idNumber);

    int updateInterviewInfoByUnicode(@Param("tableName") String tableName, @Param("updateInterviewInfoDTO") UpdateInterviewInfoDTO updateInterviewInfoDTO);

    int updateStatusByCreateBy(@Param("tableName") String tableName, @Param("createBy") String createBy, @Param("status") int status);

    int updateStatusById(@Param("tableName") String tableName, @Param("id") String id, @Param("status") int status);

    Integer queryValidCountByStatus(@Param("tableName") String tableName, @Param("status") int status);

    DynamicRegisterInfoModel queryScoreByCreateBy(@Param("tableName") String tableName, @Param("createBy") String createBy);

    List<DynamicRegisterInfoModel> queryBaseInfoByOfferFlagAndStatus(@Param("tableName") String tableName, @Param("offerFlag") Boolean offerFlag,
                                                                     @Param("status") int status, @Param("search") String search, @Param("sortOrder") String sortOrder);

    List<String> queryCreateBysByIdAndStatus(@Param("tableName") String tableName, @Param("status") int status);

    List<String> queryCreateBysByIdAndStatusForAllocateSpot(@Param("tableName") String tableName, @Param("status") int status,
                                                            @Param("sortOrder") String sortOrder);

    int updateSpotByCreateBy(@Param("tableName") String tableName, @Param("createBy") String createBy, @Param("spotDTO") AllocateSpotDTO spotDTO);

    List<DynamicRegisterInfoModel> queryBaseInfoByRoomQuantity(@Param("tableName") String tableName, @Param("roomQuantity") Integer roomQuantity);

    List<StatisticsRoomSeatCountDTO> statisticsRoomSeatCount(@Param("tableName") String tableName);

    List<String> queryRoomByRegisterPublishIdAndSpotId(@Param("tableName") String tableName, @Param("spotId") String spotId);

    List<SeatInfoDTO> querySeatInfoByRegisterPublishIdAndSpotId(@Param("tableName") String tableName, @Param("spotId") String spotId, @Param("roomNumber") String roomNumber);

    int updateTicketDownloadCount(@Param("tableName") String tableName, @Param("createBy") String createBy);

    int updateInterviewTicketDownloadCount(@Param("tableName") String tableName, @Param("createBy") String createBy);

    List<SignInInfoDTO> querySignInInfoDTO(@Param("tableName") String tableName, @Param("spotId") String spotId, @Param("room") String room);

    int dropTableDynamic(@Param("tableName") String tableName);

}
