package com.smoc.cloud.iot.packages.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "iot_package_info")
public class IotPackageInfo {
    @Id
    @Column(name = "ID", nullable = false, length = 32)
    private String id;

    @Column(name = "PACKAGE_NAME", nullable = false, length = 32)
    private String packageName;

    @Column(name = "PACKAGE_TYPE", nullable = false, length = 32)
    private String packageType;

    @Column(name = "PACKAGE_CHARGING", precision = 24, scale = 6)
    private BigDecimal packageCharging;

    @Column(name = "PACKAGE_CHARGING_DISCOUNT", precision = 24, scale = 6)
    private BigDecimal packageChargingDiscount;

    @Column(name = "PACKAGE_POOL_SIZE", nullable = false, precision = 24, scale = 6)
    private BigDecimal packagePoolSize;
    @Column(name = "CHARGING_CYCLE", nullable = false, length = 32)
    private String chargingCycle;

    @Column(name = "CYCLE_QUOTA", nullable = false, precision = 24, scale = 6)
    private BigDecimal cycleQuota;

    @Column(name = "ABOVE_QUOTA_CHARGING", nullable = false, precision = 24, scale = 4)
    private BigDecimal aboveQuotaCharging;

    @Column(name = "PACKAGE_TEMP_AMOUNT", nullable = false, precision = 24, scale = 4)
    private BigDecimal packageTempAmount;

    @Column(name = "PACKAGE_TEMP_AMOUNT_FEE", nullable = false, precision = 24, scale = 4)
    private BigDecimal packageTempAmountFee;

    @Column(name = "IS_OPEN_FEE", length = 32)
    private String isOpenFee;

    @Column(name = "IS_FUNCTION_FEE", length = 32)
    private String  isFunctionFee;

    @Column(name = "CYCLE_FUNCTION_FEE", nullable = false, precision = 24, scale = 4)
    private BigDecimal cycleFunctionFee;

    @Column(name = "WARNING_LEVEL")
    private Integer warningLevel;

    @Column(name = "PACKAGE_CARDS_NUM")
    private Integer packageCardsNum;

    @Column(name = "REMARK", length = 900)
    private String remark;

    @Column(name = "USE_STATUS", nullable = false, length = 32)
    private String useStatus;

    @Column(name = "THIS_MONTH_USED_AMOUNT", nullable = false, precision = 24, scale = 6)
    private BigDecimal thisMonthUsedAmount;

    @Column(name = "LAST_MONTH_CARRY_AMOUNT", nullable = false, precision = 24, scale = 6)
    private BigDecimal lastMonthCarryAmount;

    @Column(name = "PACKAGE_STATUS", nullable = false, length = 32)
    private String packageStatus;

    @Column(name = "SYNC_DATE", length = 32)
    private String syncDate;

    @Column(name = "CREATED_BY", nullable = false, length = 32)
    private String createdBy;

    @Column(name = "CREATED_TIME", nullable = false)
    private Date createdTime;

    @Column(name = "UPDATED_BY", length = 32)
    private String updatedBy;

    @Column(name = "UPDATED_TIME")
    private Date updatedTime;

    public BigDecimal getPackageChargingDiscount() {
        return packageChargingDiscount;
    }

    public void setPackageChargingDiscount(BigDecimal packageChargingDiscount) {
        this.packageChargingDiscount = packageChargingDiscount;
    }

    public String getIsOpenFee() {
        return isOpenFee;
    }

    public void setIsOpenFee(String isOpenFee) {
        this.isOpenFee = isOpenFee;
    }

    public String getIsFunctionFee() {
        return isFunctionFee;
    }

    public void setIsFunctionFee(String isFunctionFee) {
        this.isFunctionFee = isFunctionFee;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUseStatus() {
        return useStatus;
    }

    public void setUseStatus(String useStatus) {
        this.useStatus = useStatus;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }


    public BigDecimal getPackageTempAmount() {
        return packageTempAmount;
    }

    public void setPackageTempAmount(BigDecimal packageTempAmount) {
        this.packageTempAmount = packageTempAmount;
    }

    public BigDecimal getPackageTempAmountFee() {
        return packageTempAmountFee;
    }

    public void setPackageTempAmountFee(BigDecimal packageTempAmountFee) {
        this.packageTempAmountFee = packageTempAmountFee;
    }

    public BigDecimal getCycleFunctionFee() {
        return cycleFunctionFee;
    }

    public void setCycleFunctionFee(BigDecimal cycleFunctionFee) {
        this.cycleFunctionFee = cycleFunctionFee;
    }

    public Integer getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(Integer warningLevel) {
        this.warningLevel = warningLevel;
    }

    public BigDecimal getCycleQuota() {
        return cycleQuota;
    }

    public void setCycleQuota(BigDecimal cycleQuota) {
        this.cycleQuota = cycleQuota;
    }

    public String getChargingCycle() {
        return chargingCycle;
    }

    public void setChargingCycle(String chargingCycle) {
        this.chargingCycle = chargingCycle;
    }

    public BigDecimal getPackageCharging() {
        return packageCharging;
    }

    public void setPackageCharging(BigDecimal packageCharging) {
        this.packageCharging = packageCharging;
    }

    public BigDecimal getAboveQuotaCharging() {
        return aboveQuotaCharging;
    }

    public void setAboveQuotaCharging(BigDecimal aboveQuotaCharging) {
        this.aboveQuotaCharging = aboveQuotaCharging;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageType() {
        return packageType;
    }

    public void setPackageType(String packageType) {
        this.packageType = packageType;
    }

    public BigDecimal getPackagePoolSize() {
        return packagePoolSize;
    }

    public void setPackagePoolSize(BigDecimal packagePoolSize) {
        this.packagePoolSize = packagePoolSize;
    }

    public Integer getPackageCardsNum() {
        return packageCardsNum;
    }

    public void setPackageCardsNum(Integer packageCardsNum) {
        this.packageCardsNum = packageCardsNum;
    }

    public String getPackageStatus() {
        return packageStatus;
    }

    public void setPackageStatus(String packageStatus) {
        this.packageStatus = packageStatus;
    }

    public BigDecimal getThisMonthUsedAmount() {
        return thisMonthUsedAmount;
    }

    public void setThisMonthUsedAmount(BigDecimal thisMonthUsedAmount) {
        this.thisMonthUsedAmount = thisMonthUsedAmount;
    }

    public BigDecimal getLastMonthCarryAmount() {
        return lastMonthCarryAmount;
    }

    public void setLastMonthCarryAmount(BigDecimal lastMonthCarryAmount) {
        this.lastMonthCarryAmount = lastMonthCarryAmount;
    }

    public String getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(String syncDate) {
        this.syncDate = syncDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}