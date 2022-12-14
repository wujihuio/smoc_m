package com.smoc.cloud.common.iot.validator;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Setter
@Getter
public class IotAccountPackageItemsValidator {

    private String id;

    private String accountId;

    private String packageId;

    private String createdBy;

    private String createdTime;

    private List<IotPackageInfoValidator> packageList = new ArrayList<>();
}
