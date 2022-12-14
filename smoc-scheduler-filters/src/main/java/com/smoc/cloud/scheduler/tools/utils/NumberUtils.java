package com.smoc.cloud.scheduler.tools.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public class NumberUtils {

    private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    private static PhoneNumberOfflineGeocoder geocoder = PhoneNumberOfflineGeocoder.getInstance();

    /**
     * 根据手机号获取国家的中文名称
     * @param mobile
     * @return
     */
    public static String getInternationalAreaName(String mobile){
        if(!StringUtils.equals("+", mobile.substring(0,1))) {
            mobile = new StringBuffer("+").append(mobile).toString();
        }
        Phonenumber.PhoneNumber swissNumberProto = null;
        try {
            swissNumberProto = phoneNumberUtil.parse(mobile, "CH");
            return geocoder.getDescriptionForNumber(swissNumberProto, Locale.CHINESE);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据手机号获取国家的手机区号
     * @param mobile
     * @return
     */
    public static Integer getCountryCode(String mobile) {
        if(!StringUtils.equals("+", mobile.substring(0,1))) {
            mobile = new StringBuffer("+").append(mobile).toString();
        }
        Phonenumber.PhoneNumber swissNumberProto = null;
        try {
            phoneNumberUtil.getSupportedRegions();
            swissNumberProto = phoneNumberUtil.parse(mobile, "CH");
            return swissNumberProto.getCountryCode();
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
