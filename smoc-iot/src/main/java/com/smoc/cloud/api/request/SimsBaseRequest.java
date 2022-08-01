package com.smoc.cloud.api.request;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SimsBaseRequest extends BaseRequest{

    private List<String> msisdns;

    private List<String> iccids;

    private List<String> imsis;
}