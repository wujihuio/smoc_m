package com.smoc.cloud.common.smoc.message;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class MessageWebTaskInfoValidator {

    private String id;

    private String subject;

    @NotNull(message = "模板不能为空！")
    private String templateId;

    @NotNull(message = "企业不能为空！")
    private String enterpriseId;

    @NotNull(message = "账号不能为空！")
    private String businessAccount;

    private String businessType;

    private String infoType;

    private String messageType;

    private String sendType;

    private String timingTime;

    private String expandNumber;

    private Integer splitNumber;

    private Integer submitNumber;

    private Integer successNumber;

    private Integer successSendNumber;

    private Integer failureNumber;

    private Integer noReportNumber;

    private String appleSendTime;

    private String sendTime;

    private String sendStatus;

    //表单输入手机号
    private String inputNumber;

    @NotNull(message = "内容不能为空！")
    @Size(min = 1, max = 2000, message = "短信内容长度不符合规则！")
    private String messageContent;

    private String createdBy;

    private String createdTime;

    private String updatedBy;

    private Date updatedTime;

    private String protocolType;

    private String enterpriseName;
    private String startDate;
    private String endDate;
    private String groupId;
    private String groupName;

    //原手机号上传路径
    private String numberFiles;
    //文件大小
    private Long numberAttachmentSize;
    //发送手机号路径
    private String sendNumberAttachment;
    //异常手机号路径
    private String exceptionNumberAttachment;
    //状态报告路径
    private String reportAttachment;
    //资源数据
    private String multimediaAttachmentData;

    private String upType;

    private List<String> mobiles = new ArrayList<>();
}
