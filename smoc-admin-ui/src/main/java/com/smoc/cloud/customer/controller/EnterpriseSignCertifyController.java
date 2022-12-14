package com.smoc.cloud.customer.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.smoc.cloud.admin.security.remote.service.SystemUserLogService;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.qo.ExcelRegisterImportData;
import com.smoc.cloud.common.smoc.customer.qo.ExcelRegisterListen;
import com.smoc.cloud.common.smoc.customer.validator.EnterpriseSignCertifyValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.common.utils.UUID;
import com.smoc.cloud.common.validator.MpmIdValidator;
import com.smoc.cloud.common.validator.MpmValidatorUtil;
import com.smoc.cloud.customer.service.AccountSignRegisterService;
import com.smoc.cloud.customer.service.EnterpriseSignCertifyService;
import com.smoc.cloud.properties.SmocProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("sign/certify")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class EnterpriseSignCertifyController {

    @Autowired
    private SmocProperties smocProperties;

    @Autowired
    private SystemUserLogService systemUserLogService;

    @Autowired
    private EnterpriseSignCertifyService enterpriseSignCertifyService;

    @Autowired
    private AccountSignRegisterService accountSignRegisterService;

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_list");

        //???????????????
        PageParams<EnterpriseSignCertifyValidator> params = new PageParams<>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        EnterpriseSignCertifyValidator enterpriseSignCertifyValidator = new EnterpriseSignCertifyValidator();
        params.setParams(enterpriseSignCertifyValidator);

        //??????
        ResponseData<PageList<EnterpriseSignCertifyValidator>> data = enterpriseSignCertifyService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("enterpriseSignCertifyValidator", enterpriseSignCertifyValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());

        return view;

    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public ModelAndView page(@ModelAttribute EnterpriseSignCertifyValidator enterpriseSignCertifyValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_list");

        //????????????
        pageParams.setParams(enterpriseSignCertifyValidator);

        ResponseData<PageList<EnterpriseSignCertifyValidator>> data = enterpriseSignCertifyService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("enterpriseSignCertifyValidator", enterpriseSignCertifyValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public ModelAndView add() {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_edit");

        //???????????????
        EnterpriseSignCertifyValidator enterpriseSignCertifyValidator = new EnterpriseSignCertifyValidator();
        enterpriseSignCertifyValidator.setId(UUID.uuid32());
        enterpriseSignCertifyValidator.setCertifyStatus("1");
        enterpriseSignCertifyValidator.setAuthorizeStartDate(DateTimeUtils.getDateFormat(new Date()));
        enterpriseSignCertifyValidator.setAuthorizeExpireDate(DateTimeUtils.getDateFormat(DateTimeUtils.dateAddYears(new Date(), 5)));
        enterpriseSignCertifyValidator.setPersonLiableCertificateType("???????????????");
        enterpriseSignCertifyValidator.setPersonHandledCertificateType("???????????????");
        enterpriseSignCertifyValidator.setPosition("??????????????????");
        enterpriseSignCertifyValidator.setRegisterEnterpriseId(UUID.uuid32());

//        enterpriseSignCertifyValidator.setRegisterEnterpriseName("???????????????????????????????????????");
//        enterpriseSignCertifyValidator.setSocialCreditCode("91110108769914581E");
//
//        enterpriseSignCertifyValidator.setPersonLiableName("??????");
//        enterpriseSignCertifyValidator.setPersonLiableCertificateNumber("32098119850129371X");
//
//        enterpriseSignCertifyValidator.setPersonHandledName("??????");
//        enterpriseSignCertifyValidator.setPersonHandledCertificateNumber("32098119850129371X");

        view.addObject("enterpriseSignCertifyValidator", enterpriseSignCertifyValidator);
        view.addObject("op", "add");

        return view;
    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public ModelAndView edit(@PathVariable String id) {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_edit");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????:????????????
        ResponseData<EnterpriseSignCertifyValidator> data = enterpriseSignCertifyService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
        }

        view.addObject("enterpriseSignCertifyValidator", data.getData());
        //op???????????????add???????????????edit????????????
        view.addObject("op", "edit");

        return view;
    }

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/save/{op}", method = RequestMethod.POST)
    public ModelAndView save(@ModelAttribute EnterpriseSignCertifyValidator enterpriseSignCertifyValidator, @PathVariable String op, @RequestPart("license") MultipartFile license, @RequestPart("liableCertificate") MultipartFile liableCertificate, @RequestPart("handledCertificate") MultipartFile handledCertificate, @RequestPart("authorizeCertificateFile") MultipartFile authorizeCertificateFile, @RequestPart("positionFile") MultipartFile positionFile, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //?????????????????????
        if (!StringUtils.isEmpty(op) && "add".equals(op)) {
            enterpriseSignCertifyValidator.setCreatedTime(DateTimeUtils.getDateTimeFormat(new Date()));
            enterpriseSignCertifyValidator.setCreatedBy(user.getRealName());
        } else if (!StringUtils.isEmpty(op) && "edit".equals(op)) {
            enterpriseSignCertifyValidator.setUpdatedTime(new Date());
            enterpriseSignCertifyValidator.setUpdatedBy(user.getRealName());
        } else {
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + ResponseCode.PARAM_LINK_ERROR.getMessage());
            return view;
        }

        //???????????????
        String certifyFileRootPath = smocProperties.getCertifyFileRootPath();
        String certifyFilePath = enterpriseSignCertifyValidator.getId();
        File fold = new File(certifyFileRootPath + certifyFilePath);
        while (!fold.exists()) {
            fold.mkdirs();
        }

        /**
         * ????????????
         */
        try {
            //????????????
            if (!ObjectUtils.isEmpty(license) && license.getSize() > 0) {
                String licenseFileType = license.getOriginalFilename().substring(license.getOriginalFilename().lastIndexOf("."));
                String businessLicenseFileName = enterpriseSignCertifyValidator.getRegisterEnterpriseName() + "????????????" + licenseFileType;
                if ((license.getSize() / 1024) > 100) {
                    view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":?????????????????????100K");
                    return view;
                }
                File file = new File(certifyFileRootPath + certifyFilePath + File.separator + businessLicenseFileName);
                if (file.exists()) {
                    file.delete();
                }
                license.transferTo(file);
                enterpriseSignCertifyValidator.setBusinessLicense(businessLicenseFileName);
            } else if ("add".equals(op)) {
                view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":??????????????????");
                return view;
            }

            //??????????????????????????????
            if (!ObjectUtils.isEmpty(liableCertificate) && liableCertificate.getSize() > 0) {
                String certificateType = liableCertificate.getOriginalFilename().substring(liableCertificate.getOriginalFilename().lastIndexOf("."));
                String certificateFileName = enterpriseSignCertifyValidator.getRegisterEnterpriseName() + "?????????" + certificateType;
                if ((liableCertificate.getSize() / 1024) > 100) {
                    view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":?????????????????????100K");
                    return view;
                }
                File file = new File(certifyFileRootPath + certifyFilePath + File.separator + certificateFileName);
                if (file.exists()) {
                    file.delete();
                }
                liableCertificate.transferTo(file);
                enterpriseSignCertifyValidator.setPersonLiableCertificateUrl(certificateFileName);
            } else if ("add".equals(op)) {
                view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":??????????????????");
                return view;
            }

            //???????????????
            if (!ObjectUtils.isEmpty(handledCertificate) && handledCertificate.getSize() > 0) {
                String handledCertificateType = handledCertificate.getOriginalFilename().substring(handledCertificate.getOriginalFilename().lastIndexOf("."));
                String handledCertificateFileName = enterpriseSignCertifyValidator.getRegisterEnterpriseName() + "?????????" + handledCertificateType;
                if ((handledCertificate.getSize() / 1024) > 100) {
                    view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":?????????????????????100K");
                    return view;
                }
                File file = new File(certifyFileRootPath + certifyFilePath + File.separator + handledCertificateFileName);
                if (file.exists()) {
                    file.delete();
                }
                handledCertificate.transferTo(file);
                enterpriseSignCertifyValidator.setPersonHandledCertificateUrl(handledCertificateFileName);
            } else if ("add".equals(op)) {
                view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":??????????????????");
                return view;
            }

            //?????????
            if (!ObjectUtils.isEmpty(authorizeCertificateFile) && authorizeCertificateFile.getSize() > 0) {
                String authorizeCertificateType = authorizeCertificateFile.getOriginalFilename().substring(authorizeCertificateFile.getOriginalFilename().lastIndexOf("."));
                String authorizeCertificateFileName = enterpriseSignCertifyValidator.getRegisterEnterpriseName() + "?????????" + authorizeCertificateType;
                if ((authorizeCertificateFile.getSize() / 1024) > 100) {
                    view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":?????????????????????100K");
                    return view;
                }
                File file = new File(certifyFileRootPath + certifyFilePath + File.separator + authorizeCertificateFileName);
                if (file.exists()) {
                    file.delete();
                }
                authorizeCertificateFile.transferTo(file);
                enterpriseSignCertifyValidator.setAuthorizeCertificate(authorizeCertificateFileName);
            }

            //?????????
            if (!ObjectUtils.isEmpty(positionFile) && positionFile.getSize() > 0) {
                String positionType = positionFile.getOriginalFilename().substring(positionFile.getOriginalFilename().lastIndexOf("."));
                String positionFileName = enterpriseSignCertifyValidator.getRegisterEnterpriseName() + "????????????????????????" + positionType;
                if ((positionFile.getSize() / 1024) > 100) {
                    view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":?????????????????????100K");
                    return view;
                }
                File file = new File(certifyFileRootPath + certifyFilePath + File.separator + positionFileName);
                if (file.exists()) {
                    file.delete();
                }
                positionFile.transferTo(file);
                enterpriseSignCertifyValidator.setOfficePhotos(positionFileName);
            } else if ("add".equals(op)) {
                view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":??????????????????");
                return view;
            }

        } catch (Exception e) {
            e.printStackTrace();
            view.addObject("error", ResponseCode.PARAM_LINK_ERROR.getCode() + ":" + e.getMessage());
            return view;
        }

        //????????????
        ResponseData data = enterpriseSignCertifyService.save(enterpriseSignCertifyValidator, op);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("SIGN_CERTIFY", enterpriseSignCertifyValidator.getId(), "add".equals(op) ? enterpriseSignCertifyValidator.getCreatedBy() : enterpriseSignCertifyValidator.getUpdatedBy(), op, "add".equals(op) ? "??????????????????" : "??????????????????", JSON.toJSONString(enterpriseSignCertifyValidator));
        }

        //????????????
        log.info("[????????????][{}][{}]??????:{}", op, user.getUserName(), JSON.toJSONString(enterpriseSignCertifyValidator));

        view.setView(new RedirectView("/sign/certify/list", true, false));
        return view;

    }

    /**
     * ??????
     *
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "/deleteById/{id}", method = RequestMethod.GET)
    public ModelAndView deleteById(@PathVariable String id, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_edit");

        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        //????????????????????????
        MpmIdValidator validator = new MpmIdValidator();
        validator.setId(id);
        if (!MpmValidatorUtil.validate(validator)) {
            view.addObject("error", ResponseCode.PARAM_ERROR.getCode() + ":" + MpmValidatorUtil.validateMessage(validator));
            return view;
        }

        //??????????????????
        ResponseData<EnterpriseSignCertifyValidator> responseData = enterpriseSignCertifyService.findById(id);
        if (!ResponseCode.SUCCESS.getCode().equals(responseData.getCode())) {
            view.addObject("error", responseData.getCode() + ":" + responseData.getMessage());
        }

        //???????????????????????????
        ResponseData data = enterpriseSignCertifyService.deleteById(id);

        //??????????????????
        if (ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            systemUserLogService.logsAsync("SIGN_CERTIFY", responseData.getData().getId(), user.getRealName(), "edit", "????????????", JSON.toJSONString(responseData.getData()));
        }

        //????????????
        log.info("[????????????][delete][{}]??????:{}", user.getUserName(), JSON.toJSONString(responseData.getData()));

        view.setView(new RedirectView("/sign/certify/list", true, false));
        return view;

    }

    /**
     * ???????????????????????????????????????????????????????????????
     *
     * @param oldName
     * @return
     */
    public String generateFileName(String oldName) {
        String suffix = oldName.substring(oldName.lastIndexOf("."));
        return UUID.uuid32() + suffix;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/toImport", method = RequestMethod.GET)
    public ModelAndView toImport() {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_import");
        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public ModelAndView importFile(@RequestPart("importFile") MultipartFile importFile, HttpServletRequest request) {
        ModelAndView view = new ModelAndView("sign/certify/sign_certify_import");
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");

        ExcelRegisterListen excelListen = new ExcelRegisterListen();
        try {
            InputStream inputStream = importFile.getInputStream();
            String fileType = importFile.getOriginalFilename().substring(importFile.getOriginalFilename().lastIndexOf("."));
            if (!((".xlsx".equals(fileType) || ".xls".equals(fileType)))) {
                view.addObject("error", "?????????????????????");
                return view;
            }

            EasyExcel.read(inputStream, ExcelRegisterImportData.class, excelListen).sheet().doRead();

        } catch (Exception e) {
            e.printStackTrace();
            view.addObject("error", "?????????????????????");
            return view;
        }

        List<ExcelRegisterImportData> importList = excelListen.result;

        accountSignRegisterService.registerImport(importList);

        view.setView(new RedirectView("/sign/certify/list", true, false));
        return view;
    }

}
