package com.smoc.cloud.customer.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.excel.write.metadata.fill.FillWrapper;
import com.google.gson.Gson;
import com.smoc.cloud.common.auth.entity.SecurityUser;
import com.smoc.cloud.common.auth.qo.Dict;
import com.smoc.cloud.common.auth.qo.DictType;
import com.smoc.cloud.common.page.PageList;
import com.smoc.cloud.common.page.PageParams;
import com.smoc.cloud.common.response.ResponseCode;
import com.smoc.cloud.common.response.ResponseData;
import com.smoc.cloud.common.smoc.customer.qo.CarrierCount;
import com.smoc.cloud.common.smoc.customer.qo.Export;
import com.smoc.cloud.common.smoc.customer.qo.ExportModel;
import com.smoc.cloud.common.smoc.customer.qo.ExportRegisterModel;
import com.smoc.cloud.common.smoc.customer.validator.AccountSignRegisterExportRecordValidator;
import com.smoc.cloud.common.smoc.customer.validator.AccountSignRegisterForFileValidator;
import com.smoc.cloud.common.utils.DateTimeUtils;
import com.smoc.cloud.customer.service.AccountSignRegisterForFileService;
import com.smoc.cloud.properties.SmocProperties;
import com.smoc.cloud.utils.CompressUtil;
import com.smoc.cloud.utils.Utils;
import com.smoc.cloud.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("sign/register/file")
@Scope(value = WebApplicationContext.SCOPE_REQUEST)
public class AccountSignRegisterForFileController {

    @Autowired
    private SmocProperties smocProperties;

    @Autowired
    private AccountSignRegisterForFileService accountSignRegisterForFileService;

    /**
     * ??????
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_list");

        //???????????????
        PageParams<AccountSignRegisterForFileValidator> params = new PageParams<>();
        params.setPageSize(10);
        params.setCurrentPage(1);
        AccountSignRegisterForFileValidator accountSignRegisterForFileValidator = new AccountSignRegisterForFileValidator();
        params.setParams(accountSignRegisterForFileValidator);

        //??????
        ResponseData<PageList<AccountSignRegisterForFileValidator>> data = accountSignRegisterForFileService.page(params);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountSignRegisterForFileValidator", accountSignRegisterForFileValidator);
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
    public ModelAndView page(@ModelAttribute AccountSignRegisterForFileValidator accountSignRegisterForFileValidator, PageParams pageParams) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_list");

        //????????????
        pageParams.setParams(accountSignRegisterForFileValidator);

        ResponseData<PageList<AccountSignRegisterForFileValidator>> data = accountSignRegisterForFileService.page(pageParams);
        if (!ResponseCode.SUCCESS.getCode().equals(data.getCode())) {
            view.addObject("error", data.getCode() + ":" + data.getMessage());
            return view;
        }

        view.addObject("accountSignRegisterForFileValidator", accountSignRegisterForFileValidator);
        view.addObject("list", data.getData().getList());
        view.addObject("pageParams", data.getData().getPageParams());
        return view;

    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/toExport", method = RequestMethod.GET)
    public ModelAndView toExport() {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_export");
        ResponseData<List<CarrierCount>> responseData = this.accountSignRegisterForFileService.countByCarrier();

        if (!ResponseCode.SUCCESS.getCode().equals(responseData.getCode())) {
            view.addObject("error", responseData.getCode() + ":" + responseData.getMessage());
            return view;
        }

        Map<String,Integer> carrier = new HashMap<>();
        carrier.put("CMCC",0);
        carrier.put("UNIC",0);
        carrier.put("TELC",0);
        if(null != responseData.getData() && responseData.getData().size()>0){
           for(CarrierCount carrierCount: responseData.getData()){
               carrier.put(carrierCount.getCarrier(),carrierCount.getCount());
           }
        }
        view.addObject("carrier", carrier);
        //log.info("[carrier]:{}",new Gson().toJson(carrier));
        Export export = new Export();
        export.setNumber(1000);
        view.addObject("export", export);
        return view;
    }


    /**
     * ??????????????????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/register_button", method = RequestMethod.GET)
    public ModelAndView register_button() {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_register_button");


        return view;
    }

    /**
     * ?????????????????????????????????
     *
     * @return
     */
    @RequestMapping(value = "/register_query/{registerOrderNo}", method = RequestMethod.GET)
    public void register_query(@PathVariable String registerOrderNo, HttpServletRequest request, HttpServletResponse response) {

        ResponseData<AccountSignRegisterExportRecordValidator> record = this.accountSignRegisterForFileService.findByRegisterOrderNo(registerOrderNo);
        if (!ResponseCode.SUCCESS.getCode().equals(record.getCode())) {
            return;
        }

        String carrier = record.getData().getCarrier();

        //???????????????
        PageParams params = new PageParams<>();
        params.setPageSize(1000);
        params.setCurrentPage(1);
        ResponseData<PageList<ExportModel>> exportPage = this.accountSignRegisterForFileService.query(params,registerOrderNo);
        log.info("[exportPage]???{}",new Gson().toJson(exportPage));
        if (!ResponseCode.SUCCESS.getCode().equals(exportPage.getCode())) {
            return;
        }

        if (!(null != exportPage.getData() && null != exportPage.getData().getList() && exportPage.getData().getList().size() > 0)) {
            return;
        }

        //????????????
        Map<String, String> provinces = this.provinces(request);

        //????????????????????????????????????
        String rootPath = smocProperties.getSignRegisterRootPath();
        //?????????????????????
        String certifyFileRootPath = smocProperties.getCertifyFileRootPath();

        //????????????
        String exportOrderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);

        //????????????
        if ("CMCC".equals(carrier)) {
            //??????????????????????????????
            String currentFold;
            try {
                Set<String> files = new HashSet<>();
                Integer size = exportPage.getData().getList().size();
                List<ExportModel> models = new ArrayList<>();
                for (int i = 0; i < size; i++) {

                    ExportModel exportModel = exportPage.getData().getList().get(i);

                    //????????????????????????????????????????????????import???
                    if(exportModel.getCertifyId().equals(exportModel.getSocialCreditCode())){
                        //??????????????????/?????????????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getBusinessLicense());

                        //????????????????????????????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getLiableCertUrl());

                        //?????????????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getHandledCertUrl());

                        //???????????????
                        if(!StringUtils.isEmpty(exportModel.getAuthorizeCert())){
                            files.add(certifyFileRootPath + "import/" + exportModel.getAuthorizeCert());
                        }

                        //???????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getOfficePhotos());
                    }else{
                        //??????????????????/?????????????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getBusinessLicense());

                        //????????????????????????????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getLiableCertUrl());

                        //?????????????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getHandledCertUrl());

                        //???????????????
                        if(!StringUtils.isEmpty(exportModel.getAuthorizeCert())){
                            files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getAuthorizeCert());
                        }

                        //???????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getOfficePhotos());
                    }


                    exportModel.setAccessProvince(provinces.get(exportModel.getAccessProvince()));
                    exportModel.setMainApplication(exportModel.getServiceType());
                    exportModel.setOperate("??????");
                    exportModel.setServiceType("????????????,????????????,????????????,????????????,????????????");
                    exportModel.setPosition("??????????????????");

                    if(!StringUtils.isEmpty(exportModel.getAuthorizeStart())){
                        exportModel.setAuthorizeStart(DateTimeUtils.dateFormat(DateTimeUtils.getDateFormat(exportModel.getAuthorizeStart()),"yyyy/MM/dd"));
                    }
                    if(!StringUtils.isEmpty(exportModel.getAuthorizeEnd())){
                        exportModel.setAuthorizeEnd(DateTimeUtils.dateFormat(DateTimeUtils.getDateFormat(exportModel.getAuthorizeEnd()),"yyyy/MM/dd"));
                    }

                    models.add(exportModel);
                    if (((i + 1) % 100 == 0 || i == (size - 1))) {

                        //??????????????????????????????
                        String orderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);
                        currentFold = "??????????????????" + "/" + exportOrderNo + "/" + orderNo;
                        File fold = new File(rootPath + currentFold);
                        while (!fold.exists()) {
                            fold.mkdirs();
                        }

                        //???????????????????????????
                        String zipFile = rootPath + currentFold + "/" + "??????.zip";
                        CompressUtil.compress(files, zipFile, false);

                        //??????excel??????
                        String excelFile = rootPath + currentFold + "/" + orderNo + ".xlsx";
                        writerExcelFile(excelFile, models, "cmcc.xlsx");
                        //log.info("[files]:{}", new Gson().toJson(files));
                        files = new HashSet<>();
                        models = new ArrayList<>();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            String tempPath = rootPath + "??????????????????" + "/" + "temp";
            File temp = new File(tempPath);
            while (!temp.exists()) {
                temp.mkdirs();
            }
            String zipFilePath = temp + "/" + exportOrderNo + ".zip";

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(new File(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // ??????????????????????????????
            ZipUtils.toZip(rootPath + "??????????????????" + "/" + exportOrderNo, fileOutputStream, true);

            // ?????????????????????????????????
            try {
                this.downLoadFile(request, response, "??????-" + exportOrderNo + ".zip", zipFilePath);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("????????????~");
            }

        }

        //????????????
        if ("UNIC".equals(carrier)) {
            //??????????????????????????????
            String currentFold;
            try {
                Integer size = exportPage.getData().getList().size();
                List<ExportModel> models = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ExportModel exportModel = exportPage.getData().getList().get(i);
                    exportModel.setLiableCertType("???????????????" + exportModel.getLiableCertNum().length() + "???");
                    exportModel.setHandledCertType("???????????????" + exportModel.getHandledCertNum().length() + "???");
                    exportModel.setAccessProvince(provinces.get(exportModel.getAccessProvince()));
                    exportModel.setOperate("??????");
                    models.add(exportModel);
                }

                //??????????????????????????????
                String orderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);
                currentFold = "??????????????????" + "/" + exportOrderNo + "/" + orderNo;
                File fold = new File(rootPath + currentFold);
                while (!fold.exists()) {
                    fold.mkdirs();
                }

                //??????excel??????
                String excelFile = rootPath + currentFold + "/" + orderNo + ".xlsx";
                writerExcelFile(excelFile, models, "unic.xlsx");

            } catch (Exception e) {
                e.printStackTrace();
            }

            String tempPath = rootPath + "??????????????????" + "/" + "temp";
            File temp = new File(tempPath);
            while (!temp.exists()) {
                temp.mkdirs();
            }
            String zipFilePath = temp + "/" + exportOrderNo + ".zip";

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(new File(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // ??????????????????????????????
            ZipUtils.toZip(rootPath + "??????????????????" + "/" + exportOrderNo, fileOutputStream, true);

            // ?????????????????????????????????
            try {
                this.downLoadFile(request, response, "??????-" + exportOrderNo + ".zip", zipFilePath);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("????????????~");
            }

        }

        //????????????
        if ("TELC".equals(carrier)) {
            //??????????????????????????????
            String currentFold;
            try {
                Integer size = exportPage.getData().getList().size();
                List<ExportModel> models = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ExportModel exportModel = exportPage.getData().getList().get(i);
                    exportModel.setServiceType(exportModel.getServiceType().replace(",", "&"));
                    exportModel.setIsAuthorize("???");
                    exportModel.setLiableCertType("?????????");
                    exportModel.setHandledCertType("?????????");
                    exportModel.setIsSign("???");
                    exportModel.setIsGreen("???");
                    exportModel.setBlackList("?????????");
                    exportModel.setAccessProvince(provinces.get(exportModel.getAccessProvince()));
                    exportModel.setOperate("??????");
                    models.add(exportModel);
                }

                //??????????????????????????????
                String orderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);
                currentFold = "??????????????????" + "/" + exportOrderNo + "/" + orderNo;
                File fold = new File(rootPath + currentFold);
                while (!fold.exists()) {
                    fold.mkdirs();
                }

                //??????excel??????
                String excelFile = rootPath + currentFold + "/" + orderNo + ".xlsx";
                writerExcelFile(excelFile, models, "telc.xlsx");

            } catch (Exception e) {
                e.printStackTrace();
            }

            String tempPath = rootPath + "??????????????????" + "/" + "temp";
            File temp = new File(tempPath);
            while (!temp.exists()) {
                temp.mkdirs();
            }
            String zipFilePath = temp + "/" + exportOrderNo + ".zip";

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(new File(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // ??????????????????????????????
            ZipUtils.toZip(rootPath + "??????????????????" + "/" + exportOrderNo, fileOutputStream, true);

            // ?????????????????????????????????
            try {
                this.downLoadFile(request, response, "??????-" + exportOrderNo + ".zip", zipFilePath);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("????????????~");
            }

        }
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView register() {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_register");
        return view;
    }

    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/register_op/{registerOrderNo}", method = RequestMethod.GET)
    public ModelAndView register_op(@PathVariable String registerOrderNo) {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_register");

        ResponseData<AccountSignRegisterExportRecordValidator> record = this.accountSignRegisterForFileService.findByRegisterOrderNo(registerOrderNo);
        if (!ResponseCode.SUCCESS.getCode().equals(record.getCode())) {
            view.addObject("error", record.getCode() + ":" + record.getMessage());
            return view;
        }

        AccountSignRegisterExportRecordValidator recordValidator = record.getData();
        if("3".equals(recordValidator.getRegisterStatus())){
            view.addObject("error", "??????????????????????????????");
            return view;
        }

        ResponseData responseData = this.accountSignRegisterForFileService.updateRegisterStatusByOrderNo("3",registerOrderNo);
        //log.info("[responseData]:{}",new Gson().toJson(responseData));
        if (!ResponseCode.SUCCESS.getCode().equals(responseData.getCode())) {
            view.addObject("error", responseData.getCode() + ":" + responseData.getMessage());
            return view;
        }

        view.addObject("error", "????????????");
        return view;
    }

    /**
     * ??????????????????
     *
     * @return
     */
    @RequestMapping(value = "/export_button", method = RequestMethod.GET)
    public ModelAndView export_button() {
        ModelAndView view = new ModelAndView("sign/register/sign_register_file_export_button");

        ResponseData<List<CarrierCount>> responseData = this.accountSignRegisterForFileService.countByCarrier();

        if (!ResponseCode.SUCCESS.getCode().equals(responseData.getCode())) {
            view.addObject("error", responseData.getCode() + ":" + responseData.getMessage());
            return view;
        }

        Map<String,Integer> carrier = new HashMap<>();
        carrier.put("CMCC",0);
        carrier.put("UNIC",0);
        carrier.put("TELC",0);
        if(null != responseData.getData() && responseData.getData().size()>0){
            for(CarrierCount carrierCount: responseData.getData()){
                carrier.put(carrierCount.getCarrier(),carrierCount.getCount());
            }
        }
        view.addObject("carrier", carrier);
        return view;
    }


    /**
     * ????????????
     *
     * @return
     */
    @RequestMapping(value = "/export/{carrier}", method = RequestMethod.GET)
    public void export(@PathVariable String carrier, HttpServletRequest request, HttpServletResponse response) {

        if (!("CMCC".equals(carrier) || "UNIC".equals(carrier) || "TELC".equals(carrier))) {
            return;
        }
        SecurityUser user = (SecurityUser) request.getSession().getAttribute("user");
        List<String> ids = new ArrayList<>();

        ExportModel queryExportModel = new ExportModel();
        queryExportModel.setRegisterCarrier(carrier);
        //???????????????
        PageParams<ExportModel> params = new PageParams<>();
        params.setPageSize(1000);
        params.setCurrentPage(1);
        params.setParams(queryExportModel);
        ResponseData<PageList<ExportModel>> exportPage = this.accountSignRegisterForFileService.export(params);
        log.info("[exportPage]???{}",new Gson().toJson(exportPage));
        if (!ResponseCode.SUCCESS.getCode().equals(exportPage.getCode())) {
            return;
        }

        if (!(null != exportPage.getData() && null != exportPage.getData().getList() && exportPage.getData().getList().size() > 0)) {
            return;
        }

        //????????????
        Map<String, String> provinces = this.provinces(request);

        //????????????????????????????????????
        String rootPath = smocProperties.getSignRegisterRootPath();
        //?????????????????????
        String certifyFileRootPath = smocProperties.getCertifyFileRootPath();

        //????????????
        String exportOrderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);

        //????????????
        if ("CMCC".equals(carrier)) {
            //??????????????????????????????
            String currentFold;
            try {
                Set<String> files = new HashSet<>();
                Integer size = exportPage.getData().getList().size();
                List<ExportModel> models = new ArrayList<>();
                for (int i = 0; i < size; i++) {

                    ExportModel exportModel = exportPage.getData().getList().get(i);
                    ids.add(exportModel.getId());
                    //????????????????????????????????????
                    if(exportModel.getCertifyId().equals(exportModel.getSocialCreditCode())){
                        //??????????????????/?????????????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getBusinessLicense());

                        //????????????????????????????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getLiableCertUrl());

                        //?????????????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getHandledCertUrl());

                        //???????????????
                        if(!StringUtils.isEmpty(exportModel.getAuthorizeCert())){
                            files.add(certifyFileRootPath + "import/" + exportModel.getAuthorizeCert());
                        }

                        //???????????????
                        files.add(certifyFileRootPath + "import/" + exportModel.getOfficePhotos());
                    }else{
                        //??????????????????/?????????????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getBusinessLicense());

                        //????????????????????????????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getLiableCertUrl());

                        //?????????????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getHandledCertUrl());

                        //???????????????
                        if(!StringUtils.isEmpty(exportModel.getAuthorizeCert())){
                            files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getAuthorizeCert());
                        }

                        //???????????????
                        files.add(certifyFileRootPath + exportModel.getCertifyId() + "/" + exportModel.getOfficePhotos());
                    }

                    exportModel.setAccessProvince(provinces.get(exportModel.getAccessProvince()));
                    exportModel.setMainApplication(exportModel.getServiceType());
                    exportModel.setOperate("??????");
                    exportModel.setServiceType("????????????,????????????,????????????,????????????,????????????");
                    exportModel.setPosition("??????????????????");

                    if(!StringUtils.isEmpty(exportModel.getAuthorizeStart())){
                        exportModel.setAuthorizeStart(DateTimeUtils.dateFormat(DateTimeUtils.getDateFormat(exportModel.getAuthorizeStart()),"yyyy/MM/dd"));
                    }
                    if(!StringUtils.isEmpty(exportModel.getAuthorizeEnd())){
                        exportModel.setAuthorizeEnd(DateTimeUtils.dateFormat(DateTimeUtils.getDateFormat(exportModel.getAuthorizeEnd()),"yyyy/MM/dd"));
                    }

                    models.add(exportModel);
                    if (((i + 1) % 100 == 0 || i == (size - 1))) {

                        //??????????????????????????????
                        String orderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);
                        currentFold = "??????????????????" + "/" + exportOrderNo + "/" + orderNo;
                        File fold = new File(rootPath + currentFold);
                        while (!fold.exists()) {
                            fold.mkdirs();
                        }

                        //???????????????????????????
                        String zipFile = rootPath + currentFold + "/" + "??????.zip";
                        CompressUtil.compress(files, zipFile, false);

                        //??????excel??????
                        String excelFile = rootPath + currentFold + "/" + orderNo + ".xlsx";
                        writerExcelFile(excelFile, models, "cmcc.xlsx");
                        //log.info("[files]:{}", new Gson().toJson(files));
                        files = new HashSet<>();
                        models = new ArrayList<>();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }

            String tempPath = rootPath + "??????????????????" + "/" + "temp";
            File temp = new File(tempPath);
            while (!temp.exists()) {
                temp.mkdirs();
            }
            String zipFilePath = temp + "/" + exportOrderNo + ".zip";

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(new File(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // ??????????????????????????????
            ZipUtils.toZip(rootPath + "??????????????????" + "/" + exportOrderNo, fileOutputStream, true);

            // ?????????????????????????????????
            try {
                ExportRegisterModel exportRegisterModel = new ExportRegisterModel();
                exportRegisterModel.setRegisterOrderNo(exportOrderNo);
                exportRegisterModel.setCarrier(carrier);
                exportRegisterModel.setIds(ids);
                exportRegisterModel.setCreatedBy(user.getRealName());
                this.accountSignRegisterForFileService.register(exportRegisterModel);
                this.downLoadFile(request, response, "??????-" + exportOrderNo + ".zip", zipFilePath);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("????????????~");
            }

        }

        //????????????
        if ("UNIC".equals(carrier)) {
            //??????????????????????????????
            String currentFold;
            try {
                Integer size = exportPage.getData().getList().size();
                List<ExportModel> models = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ExportModel exportModel = exportPage.getData().getList().get(i);
                    ids.add(exportModel.getId());
                    exportModel.setLiableCertType("???????????????" + exportModel.getLiableCertNum().length() + "???");
                    exportModel.setHandledCertType("???????????????" + exportModel.getHandledCertNum().length() + "???");
                    exportModel.setAccessProvince(provinces.get(exportModel.getAccessProvince()));
                    exportModel.setOperate("??????");
                    models.add(exportModel);
                }

                //??????????????????????????????
                String orderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);
                currentFold = "??????????????????" + "/" + exportOrderNo + "/" + orderNo;
                File fold = new File(rootPath + currentFold);
                while (!fold.exists()) {
                    fold.mkdirs();
                }

                //??????excel??????
                String excelFile = rootPath + currentFold + "/" + orderNo + ".xlsx";
                writerExcelFile(excelFile, models, "unic.xlsx");

            } catch (Exception e) {
                e.printStackTrace();
            }

            String tempPath = rootPath + "??????????????????" + "/" + "temp";
            File temp = new File(tempPath);
            while (!temp.exists()) {
                temp.mkdirs();
            }
            String zipFilePath = temp + "/" + exportOrderNo + ".zip";

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(new File(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // ??????????????????????????????
            ZipUtils.toZip(rootPath + "??????????????????" + "/" + exportOrderNo, fileOutputStream, true);

            // ?????????????????????????????????
            try {
                ExportRegisterModel exportRegisterModel = new ExportRegisterModel();
                exportRegisterModel.setRegisterOrderNo(exportOrderNo);
                exportRegisterModel.setCarrier(carrier);
                exportRegisterModel.setIds(ids);
                exportRegisterModel.setCreatedBy(user.getRealName());
                this.accountSignRegisterForFileService.register(exportRegisterModel);
                this.downLoadFile(request, response, "??????-" + exportOrderNo + ".zip", zipFilePath);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("????????????~");
            }

        }

        //????????????
        if ("TELC".equals(carrier)) {
            //??????????????????????????????
            String currentFold;
            try {
                Integer size = exportPage.getData().getList().size();
                List<ExportModel> models = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    ExportModel exportModel = exportPage.getData().getList().get(i);
                    ids.add(exportModel.getId());
                    exportModel.setServiceType(exportModel.getServiceType().replace(",", "&"));
                    exportModel.setIsAuthorize("???");
                    exportModel.setLiableCertType("?????????");
                    exportModel.setHandledCertType("?????????");
                    exportModel.setIsSign("???");
                    exportModel.setIsGreen("???");
                    exportModel.setBlackList("?????????");
                    exportModel.setAccessProvince(provinces.get(exportModel.getAccessProvince()));
                    exportModel.setOperate("??????");
                    models.add(exportModel);
                }

                //??????????????????????????????
                String orderNo = DateTimeUtils.getDateFormat(new Date(), "yyyyMMddHHmmssSSS") + Utils.getRandom(4);
                currentFold = "??????????????????" + "/" + exportOrderNo + "/" + orderNo;
                File fold = new File(rootPath + currentFold);
                while (!fold.exists()) {
                    fold.mkdirs();
                }

                //??????excel??????
                String excelFile = rootPath + currentFold + "/" + orderNo + ".xlsx";
                writerExcelFile(excelFile, models, "telc.xlsx");

            } catch (Exception e) {
                e.printStackTrace();
            }

            String tempPath = rootPath + "??????????????????" + "/" + "temp";
            File temp = new File(tempPath);
            while (!temp.exists()) {
                temp.mkdirs();
            }
            String zipFilePath = temp + "/" + exportOrderNo + ".zip";

            FileOutputStream fileOutputStream = null;

            try {
                fileOutputStream = new FileOutputStream(new File(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // ??????????????????????????????
            ZipUtils.toZip(rootPath + "??????????????????" + "/" + exportOrderNo, fileOutputStream, true);

            // ?????????????????????????????????
            try {
                ExportRegisterModel exportRegisterModel = new ExportRegisterModel();
                exportRegisterModel.setRegisterOrderNo(exportOrderNo);
                exportRegisterModel.setCarrier(carrier);
                exportRegisterModel.setIds(ids);
                exportRegisterModel.setCreatedBy(user.getRealName());
                this.accountSignRegisterForFileService.register(exportRegisterModel);
                this.downLoadFile(request, response, "??????-" + exportOrderNo + ".zip", zipFilePath);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("????????????~");
            }

        }
    }

    /**
     * @param resultFile ?????????????????????????????????????????????????????????
     */
    public void writerExcelFile(String resultFile, List<ExportModel> registerData, String fileType) {

        try {

            ClassPathResource classPathResource = new ClassPathResource("static/files/register/" + fileType);
            InputStream fis = classPathResource.getInputStream();
            // ????????????????????????????????????
            ExcelWriter excelWriter = EasyExcel.write(resultFile).withTemplate(fis).build();
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // ?????????????????????????????????????????????????????????????????????
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            //????????????????????????????????? hisData
            excelWriter.fill(new FillWrapper("rd", registerData), fillConfig, writeSheet);
            excelWriter.finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????
     */
    private Map<String, String> provinces(HttpServletRequest request) {
        Map<String, DictType> dictMap = (Map<String, DictType>) request.getServletContext().getAttribute("dict");
        //?????????
        DictType provinces = dictMap.get("provices");

        Map<String, String> provincesMap = new HashMap<>();
        for (Dict dict : provinces.getDict()) {
            provincesMap.put(dict.getFieldCode(), dict.getFieldName());
        }

        return provincesMap;
    }

    /**
     * ????????????zip???????????????
     *
     * @param request
     * @param response
     * @param fileZip  zip?????????
     * @param filePath zip?????????
     * @throws UnsupportedEncodingException
     */
    public static void downLoadFile(HttpServletRequest request, HttpServletResponse response, String fileZip, String filePath) throws UnsupportedEncodingException {

        //?????????????????????
        final String userAgent = request.getHeader("USER-AGENT");
        //?????????????????????????????????????????????????????????????????????
        String finalFileName = null;
        if (StringUtils.contains(userAgent, "MSIE") || StringUtils.contains(userAgent, "Trident")) {
            // IE?????????
            finalFileName = URLEncoder.encode(fileZip, "UTF8");
            System.out.println("IE?????????");
        } else if (StringUtils.contains(userAgent, "Mozilla")) {
            // google,???????????????
            finalFileName = new String(fileZip.getBytes(), "ISO8859-1");
        } else {
            // ???????????????
            finalFileName = URLEncoder.encode(fileZip, "UTF8");
        }
        // ??????????????????????????????????????????????????????????????????????????????
        response.setContentType("application/x-download");
        // ?????????????????????
        response.setHeader("Content-Disposition", "attachment;filename=\"" + finalFileName + "\"");

        ServletOutputStream servletOutputStream = null;
        try {
            servletOutputStream = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        DataOutputStream temps = new DataOutputStream(servletOutputStream);
        // ??????????????????????????????
        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] b = new byte[2048];
        // ????????????????????????????????????
        File reportZip = new File(filePath);
        try {
            while ((in.read(b)) != -1) {
                temps.write(b);
            }
            temps.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (temps != null) {
                try {
                    temps.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reportZip != null) {
                // ????????????????????????????????????????????????!
                reportZip.delete();
            }
            try {
                servletOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
