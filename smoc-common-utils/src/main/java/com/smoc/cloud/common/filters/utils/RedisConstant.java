package com.smoc.cloud.common.filters.utils;

/**
 * redis常量
 */
public class RedisConstant {

    /**
     * 系统级别过滤配置数据加载
     */
    //系统配置-系统参数前缀
    public static final String FILTERS_CONFIG_SYSTEM = "filters:config:system:";

    //系统黑名单
    public static final String FILTERS_CONFIG_SYSTEM_BLACK = FILTERS_CONFIG_SYSTEM + "list:black";
    //系统白名单
    public static final String FILTERS_CONFIG_SYSTEM_WHITE = FILTERS_CONFIG_SYSTEM + "list:white";
    //系统关键词前缀
    public static final String FILTERS_CONFIG_SYSTEM_WORDS = FILTERS_CONFIG_SYSTEM + "words:";
    //系统敏感词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_SENSITIVE = FILTERS_CONFIG_SYSTEM_WORDS + "sensitive";
    //系统审核词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_CHECK = FILTERS_CONFIG_SYSTEM_WORDS + "check";
    //系统白词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_WHITE = FILTERS_CONFIG_SYSTEM_WORDS + "white:";
    //系统超级白词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_WHITE_SUPER = FILTERS_CONFIG_SYSTEM_WORDS_WHITE + "super";
    //系统洗黑白词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_WHITE_SENSITIVE = FILTERS_CONFIG_SYSTEM_WORDS_WHITE + "sensitive";
    //系统免审白词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_WHITE_NO_CHECK = FILTERS_CONFIG_SYSTEM_WORDS_WHITE + "no_check";
    //系统正则白词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_WHITE_REGULAR = FILTERS_CONFIG_SYSTEM_WORDS_WHITE + "regular";

    //行业分类敏感词
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_INFO_TYPE_SENSITIVE = FILTERS_CONFIG_SYSTEM_WORDS + "info_type:sensitive:";
    //行业敏感词类型
    public static final String FILTERS_CONFIG_SYSTEM_WORDS_INFO_TYPE = FILTERS_CONFIG_SYSTEM_WORDS + "info_type:type";

    /**
     * 账户级过滤
     */
    //账号参数-参数前缀
    public static final String FILTERS_CONFIG_ACCOUNT = "filters:config:account:";

    //账号参数-号码过滤参数
    public static final String FILTERS_CONFIG_ACCOUNT_COMMON = FILTERS_CONFIG_ACCOUNT + "common:";
    //账号参数-号码过滤参数
    public static final String FILTERS_CONFIG_ACCOUNT_NUMBER = FILTERS_CONFIG_ACCOUNT + "number:";
    //账号参数-内容过滤参数
    public static final String FILTERS_CONFIG_ACCOUNT_MESSAGE = FILTERS_CONFIG_ACCOUNT + "message:";
    //业务账号关键词前缀
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS = FILTERS_CONFIG_ACCOUNT + "words:";
    //业务账号敏感词
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_SENSITIVE = FILTERS_CONFIG_ACCOUNT_WORDS + "sensitive:";
    //业务账号审核词
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_CHECK = FILTERS_CONFIG_ACCOUNT_WORDS + "check:";
    //业务账号白词前缀
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_WHITE = FILTERS_CONFIG_ACCOUNT_WORDS + "white:";
    //业务账号超级白词
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_WHITE_SUPER = FILTERS_CONFIG_ACCOUNT_WORDS_WHITE + "super:";
    //业务账号洗黑白词
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_WHITE_SENSITIVE = FILTERS_CONFIG_ACCOUNT_WORDS_WHITE + "sensitive:";
    //业务账号免审白词
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_WHITE_NO_CHECK = FILTERS_CONFIG_ACCOUNT_WORDS_WHITE + "no_check:";
    //业务账号正则白词
    public static final String FILTERS_CONFIG_ACCOUNT_WORDS_WHITE_REGULAR = FILTERS_CONFIG_ACCOUNT_WORDS_WHITE + "regular:";

    /**
     * 临时数据，表示有有效期，基本上都是限流、限量
     */
    //限流、限制
    public static final String FILTERS_TEMPORARY_LIMIT = "filters:temporary:limit:";
    //账户参数-运营商日限流Key  DATE 格式为 yyyyMMdd
    public static final String FILTERS_TEMPORARY_LIMIT_FLOW_CARRIER_DATE = FILTERS_TEMPORARY_LIMIT + "flow:carrier:";
    //单号码账号级发送频率限制临时限流数据
    public static final String FILTERS_TEMPORARY_LIMIT_ACCOUNT_NUMBER = FILTERS_TEMPORARY_LIMIT + "flow:number:";
    //单号码系统级级发送频率限制临时限流数据
    public static final String FILTERS_TEMPORARY_LIMIT_SYSTEM_NUMBER = FILTERS_TEMPORARY_LIMIT + "flow:system:";


}