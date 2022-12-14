package com.smoc.cloud.complaint.service;


import com.alibaba.fastjson.JSON;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.response.ResponseDataUtil;
import com.smoc.cloud.common.smoc.message.MessageChannelComplaintValidator;
import com.smoc.cloud.common.smoc.message.MessageComplaintInfoValidator;
import com.smoc.cloud.common.smoc.message.MessageDetailInfoValidator;
import com.smoc.cloud.common.smoc.message.TableStoreMessageDetailInfoValidator;
import com.smoc.cloud.common.smoc.message.model.ComplaintExcelModel;
import com.smoc.cloud.common.smoc.utils.ChannelUtils;
import com.smoc.cloud.common.smoc.utils.SysFilterUtil;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.complaint.entity.MessageComplaintInfo;
import com.smoc.cloud.complaint.repository.ComplaintRepository;
import com.smoc.cloud.configure.channel.entity.ConfigChannelInterface;
import com.smoc.cloud.configure.channel.repository.ChannelInterfaceRepository;
import com.smoc.cloud.configure.channel.service.ChannelInterfaceService;
import com.smoc.cloud.customer.entity.AccountBasicInfo;
import com.smoc.cloud.customer.entity.AccountSignRegisterForFile;
import com.smoc.cloud.customer.entity.EnterpriseBasicInfo;
import com.smoc.cloud.customer.repository.AccountSignRegisterForFileRepository;
import com.smoc.cloud.customer.repository.BusinessAccountRepository;
import com.smoc.cloud.customer.repository.EnterpriseRepository;
import com.smoc.cloud.filter.entity.FilterGroupList;
import com.smoc.cloud.filter.repository.BlackRepository;
import com.smoc.cloud.filter.repository.GroupRepository;
import com.smoc.cloud.filter.service.BlackService;
import com.smoc.cloud.message.entity.MessageDetailInfo;
import com.smoc.cloud.message.repository.MessageDetailInfoRepository;
import com.smoc.cloud.tablestore.repository.TableStoreMessageDetailInfoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ????????????
 **/
@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ComplaintService {

    @Resource
    private ComplaintRepository complaintRepository;

    @Resource
    private BusinessAccountRepository businessAccountRepository;

    @Resource
    private BlackRepository blackRepository;

    @Resource
    private GroupRepository groupRepository;

    @Resource
    private ChannelInterfaceRepository channelInterfaceRepository;

    @Resource
    private BlackService blackService;

    @Resource
    private AccountSignRegisterForFileRepository accountSignRegisterForFileRepository;

    @Resource
    private EnterpriseRepository enterpriseRepository;

    @Resource
    private MessageDetailInfoRepository messageDetailInfoRepository;

    /**
     * ????????????
     *
     * @param pageParams
     * @return
     */
    public PageList<MessageComplaintInfoValidator> page(PageParams<MessageComplaintInfoValidator> pageParams) {
        return complaintRepository.page(pageParams);
    }

    /**
     * ??????id????????????
     *
     * @param id
     * @return
     */
    public ResponseData findById(String id) {
        Optional<MessageComplaintInfo> data = complaintRepository.findById(id);
        //????????? Optional ??????????????????????????????
        return ResponseDataUtil.buildSuccess(data.orElse(null));
    }

    /**
     * ???????????????
     *
     * @param messageComplaintInfoValidator
     * @param op  ???????????? ???add???edit
     * @return
     */
    @Transactional
    public ResponseData<MessageComplaintInfo> save(MessageComplaintInfoValidator messageComplaintInfoValidator, String op) {

        Optional<MessageComplaintInfo> data = complaintRepository.findById(messageComplaintInfoValidator.getId());
        if(data.isPresent()){
            MessageComplaintInfo entity = data.get();
            entity.setBusinessAccount(messageComplaintInfoValidator.getBusinessAccount());
            entity.setIs12321(messageComplaintInfoValidator.getIs12321());
            entity.setNumberCode(messageComplaintInfoValidator.getNumberCode());
            entity.setSendDate(messageComplaintInfoValidator.getSendDate());
            entity.setSendRate(messageComplaintInfoValidator.getSendRate());
            entity.setBusinessType(messageComplaintInfoValidator.getBusinessType());

            //????????????????????????
            List<ConfigChannelInterface> list = channelInterfaceRepository.findBySrcId(messageComplaintInfoValidator.getNumberCode());
            if(!StringUtils.isEmpty(list) && list.size()>0){
                entity.setChannelId(list.get(0).getChannelId());
            }

            //op ?????? edit ??? add
            if (!("edit".equals(op) || "add".equals(op))) {
                return ResponseDataUtil.buildError();
            }

            //????????????
            log.info("[????????????][{}]??????:{}", op, JSON.toJSONString(entity));
            complaintRepository.saveAndFlush(entity);
        }

        return ResponseDataUtil.buildSuccess();
    }

    /**
     * ??????
     * @param id
     * @return
     */
    @Transactional
    public ResponseData<MessageComplaintInfo> deleteById(String id) {

        MessageComplaintInfo data = complaintRepository.findById(id).get();
        //????????????
        log.info("[????????????][delete]??????:{}",JSON.toJSONString(data));
        complaintRepository.deleteById(id);


        return ResponseDataUtil.buildSuccess();
    }

    /**
     * ??????????????????
     * @param messageComplaintInfoValidator
     * @return
     */
    @Async
    public ResponseData batchSave(MessageComplaintInfoValidator messageComplaintInfoValidator) {


        List<ComplaintExcelModel> list = messageComplaintInfoValidator.getComplaintList();

        /**
         * ?????????????????????????????????
         * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        if("day".equals(messageComplaintInfoValidator.getComplaintSource())){
            for(ComplaintExcelModel info:list){

                //?????????????????????????????????????????????????????????
                List<AccountSignRegisterForFile> fileList = accountSignRegisterForFileRepository.findByNumberSegmentAndRegisterStatus(info.getReportNumber(),"3");
                if(!StringUtils.isEmpty(fileList) && fileList.size()>0){
                    AccountSignRegisterForFile accountSignRegisterForFile = fileList.get(0);
                    info.setBusinessAccount(accountSignRegisterForFile.getAccount());
                    info.setChannelId(accountSignRegisterForFile.getChannelId());

                }

                /*TableStoreMessageDetailInfoValidator message =  tableStoreMessageDetailInfoRepository.findByCarrierAndPhoneNumberAndMessageContent(messageComplaintInfoValidator.getCarrier(),info.getReportNumber(),info.getReportContent(),sDate,endDate);
                if(!StringUtils.isEmpty(message)){
                    info.setBusinessAccount(message.getBusinessAccount());
                    info.setSendDate(message.getUserSubmitTime());
                    info.setChannelId(message.getChannelId());
                    info.setNumberCode(message.getChannelSrcid());

                    //??????30??????????????????
                    MessageDetailInfoValidator validator = new MessageDetailInfoValidator();
                    validator.setPhoneNumber(info.getReportNumber());
                    Date startDate = DateTimeUtils.dateAddDays(new Date(),-30);
                    validator.setStartDate(DateTimeUtils.getDateFormat(startDate));
                    validator.setEndDate(DateTimeUtils.getDateFormat(new Date()));
                    int number = tableStoreMessageDetailInfoRepository.statisticMessageNumberByMobile(validator);
                    info.setSendRate(""+number);
                }*/
                //??????
                MessageComplaintInfo messageComplaintInfo = complaintRepository.findByCarrierSourceAndReportNumberAndReportContentAndReportDate(messageComplaintInfoValidator.getCarrier(),info.getReportNumber(),info.getReportContent(),info.getReportDate());
                if(!StringUtils.isEmpty(messageComplaintInfo)){
                    //????????????
                    log.info("[????????????][delete]??????:{}",JSON.toJSONString(messageComplaintInfo));
                    complaintRepository.deleteById(messageComplaintInfo.getId());
                }
            }
        }

        complaintRepository.batchSave(messageComplaintInfoValidator);

        /**
         * ???????????????
         */
        saveBlack(messageComplaintInfoValidator);

        return ResponseDataUtil.buildSuccess();

    }

    /**
     * ???????????????
     * @param messageComplaintInfoValidator
     */
    private void saveBlack(MessageComplaintInfoValidator messageComplaintInfoValidator) {

        //????????????
        if("day".equals(messageComplaintInfoValidator.getComplaintSource())){
            //?????????????????????????????????
            Optional<FilterGroupList> optional = groupRepository.findById(SysFilterUtil.GROUP_COMPLAINT_ID);
            if(!optional.isPresent()){
                FilterGroupList filterGroupList = new FilterGroupList();
                filterGroupList.setId(SysFilterUtil.GROUP_COMPLAINT_ID);
                filterGroupList.setEnterpriseId("smoc_black");
                filterGroupList.setGroupId(SysFilterUtil.GROUP_COMPLAINT_ID);
                filterGroupList.setGroupName(SysFilterUtil.GROUP_COMPLAINT_NAME);
                filterGroupList.setParentId("root");
                filterGroupList.setIsLeaf("1");
                filterGroupList.setStatus("1");
                filterGroupList.setSort(0);
                filterGroupList.setCreatedTime(new Date());
                filterGroupList.setCreatedBy(messageComplaintInfoValidator.getCreatedBy());
                groupRepository.saveAndFlush(filterGroupList);

                //????????????
                log.info("[????????????][??????????????????][{}]??????:{}","add",JSON.toJSONString(filterGroupList));
            }

            //???????????????
            blackRepository.complaintBathSave(messageComplaintInfoValidator,SysFilterUtil.GROUP_COMPLAINT_ID);

            //?????????redis???
            blackService.loadBlackList();
        }

        //12321??????
        if("12321".equals(messageComplaintInfoValidator.getComplaintSource())){
            //???????????????12321????????????
            Optional<FilterGroupList> optional = groupRepository.findById(SysFilterUtil.GROUP_12321_ID);
            if(!optional.isPresent()){
                FilterGroupList filterGroupList = new FilterGroupList();
                filterGroupList.setId(SysFilterUtil.GROUP_12321_ID);
                filterGroupList.setEnterpriseId("smoc_black");
                filterGroupList.setGroupId(SysFilterUtil.GROUP_12321_ID);
                filterGroupList.setGroupName(SysFilterUtil.GROUP_12321_NAME);
                filterGroupList.setParentId("root");
                filterGroupList.setIsLeaf("1");
                filterGroupList.setStatus("1");
                filterGroupList.setSort(0);
                filterGroupList.setCreatedTime(new Date());
                filterGroupList.setCreatedBy(messageComplaintInfoValidator.getCreatedBy());
                groupRepository.saveAndFlush(filterGroupList);

                //????????????
                log.info("[????????????][12321????????????][{}]??????:{}","add",JSON.toJSONString(filterGroupList));
            }

            //???????????????
            blackRepository.complaintBathSave(messageComplaintInfoValidator,SysFilterUtil.GROUP_12321_ID);

            //?????????redis???
            blackService.loadBlackList();

        }

    }

    /**
     * ????????????????????????
     * @param messageChannelComplaintValidator
     * @return
     */
    public ResponseData<List<MessageChannelComplaintValidator>> channelComplaintRanking(MessageChannelComplaintValidator messageChannelComplaintValidator) {
        List<MessageChannelComplaintValidator> list = complaintRepository.channelComplaintRanking(messageChannelComplaintValidator);
        return ResponseDataUtil.buildSuccess(list);
    }

    /**
     * ???????????????????????????10?????????????????????
     * @param detail
     * @return
     */
    public ResponseData<List<MessageDetailInfoValidator>> sendMessageList(MessageDetailInfoValidator detail) {
        List<MessageDetailInfoValidator> list = complaintRepository.sendMessageList(detail);
        return ResponseDataUtil.buildSuccess(list);
    }
}
