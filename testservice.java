package com.doodil.proforma.custom.service;

import com.doodil.proforma.custom.repository.*;
import com.doodil.proforma.custom.service.dto.*;

import com.doodil.proforma.custom.utils.CustomUtil;
import com.doodil.proforma.domain.*;
import com.doodil.proforma.repository.*;
import com.doodil.proforma.service.dto.*;
import com.doodil.proforma.service.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kannery on 31-Oct-2018.
 */
@Service
@Transactional
public class CashBudgetService {

    private final Logger log = LoggerFactory.getLogger(CashBudgetService.class);
    @Autowired
    CustomCashInflowMasterRepository customCashInflowMasterRepository;
    @Autowired
    CashInflowMasterMapper cashInflowMasterMapper;
    @Autowired
    CustomUserService customUserService;
    @Autowired
    CashInflowDataMapper cashInflowDataMapper;
    @Autowired
    CustomCashInflowDataRepository customCashInflowDataRepository;
    @Autowired
    CustomCashInflowReceivablesRepository customCashInflowReceivablesRepository;
    @Autowired
    CashInflowReceivablesMapper cashInflowReceivablesMapper;
    @Autowired
    CustomRecurringCBEntriesRepository customRecurringCBEntriesRepository;
    @Autowired
    CustomCashInflowTotalsRepository customCashInflowTotalsRepository;
    @Autowired
    CustomCashOutflowDataRepository customCashOutflowDataRepository;
    @Autowired
    CashOutflowDataMapper cashOutflowDataMapper;
    @Autowired
    CustomCashOutflowPayablesRepository customCashOutflowPayablesRepository;
    @Autowired
    CashOutflowPayablesMapper cashOutflowPayablesMapper;
    @Autowired
    CustomCashOutflowTotalsRepository customCashOutflowTotalsRepository;
    @Autowired
    CustomCashOutflowMasterRepository customCashOutflowMasterRepository;
    @Autowired
    CashOutflowMasterMapper cashOutflowMasterMapper;
    @Autowired
    CustomCashInflowRangeRepository customCashInflowRangeRepository;
    @Autowired
    CustomCashReceivableDetailsRepository customCashReceivableDetailsRepository;
    @Autowired
    CustomCashOutflowRangeRepository customCashOutflowRangeRepository;
    @Autowired
    CustomCashPayableDetailsRepository customCashPayableDetailsRepository;

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void saveOrUpdateCashInflow_deprecated(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        //save cashinflow master
        CashInflowMaster cashInflowMaster = cashInflowMasterMapper.toEntity(cashInflowMasterWrapperDTO.getCashInflowMaster());
        cashInflowMaster.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        customCashInflowMasterRepository.save(cashInflowMaster);
        cashInflowMasterWrapperDTO.setCashInflowMaster(cashInflowMasterMapper.toDto(cashInflowMaster));
        //save recur data if there is no recur id in the records
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashbudgetWrapperDTO.getCashInflowWrappers()) {
                    CashInflowDataDTO cashInflowDataDTO = cashInflowWrapperDTO.getCashInflowData();
                    if (cashInflowDataDTO.getInflowType() != null && "R".equalsIgnoreCase(cashInflowDataDTO.getInflowType())) {
                        RecurringCBEntries recurringCBEntries = null;
                        log.debug("cashInflowDataDTO::" + cashInflowDataDTO);
                        if (cashInflowDataDTO.getRecurId() == null) {
                            recurringCBEntries = getRecurringCEEntries(cashInflowDataDTO, null, "AR");
                            recurringCBEntries.setId(cashInflowDataDTO.getRecurId());
                            customRecurringCBEntriesRepository.save(recurringCBEntries);
                            cashInflowDataDTO.setRecurId(recurringCBEntries.getId());
                        }
                    }
                }
            }
        }
        //save cashinflow data
        //save receivables data - inside loop for each cash inflow data
        String editId = null;
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashbudgetWrapperDTO.getCashInflowWrappers()) {
                    log.debug("salesDate::"+cashInflowWrapperDTO.getCashInflowData().getSalesDate());
                    CashInflowData cashInflowData = cashInflowDataMapper.toEntity(cashInflowWrapperDTO.getCashInflowData());
                    cashInflowData.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
                    customCashInflowDataRepository.save(cashInflowData);
                    //before over writing with the saved object, retain the ids so that we can copy over again
                    editId = cashInflowWrapperDTO.getCashInflowData().getEditId();
                    cashInflowWrapperDTO.setCashInflowData(cashInflowDataMapper.toDto(cashInflowData));
                    cashInflowWrapperDTO.getCashInflowData().setEditId(editId);
                    deleteCashInflowSubData(cashInflowData);
                    saveCashInflowReceivables(cashbudgetWrapperDTO, cashInflowWrapperDTO, cashInflowData);
                }
            }
        }
        saveOrUpdateARTotals(cashInflowMasterWrapperDTO);

    }

    public void saveOrUpdateARTotals(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        //save totals
        int count = 0;
        List<CashInflowTotals> cashInflowTotals = new ArrayList<CashInflowTotals>();
        for(DayDTO dayDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayList()) {
            count = 0;
            CashInflowTotals cashInflowTotals1 = new CashInflowTotals();
            cashInflowTotals1.setCashInflowTotalDate(dayDTO.getEventDate());
            cashInflowTotals1.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
            for (CashbudgetWrapperDTO cashbudgetWrapperDTO : cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
                if(count > 1) {
                    for (DayDTO dayDTO1 : cashbudgetWrapperDTO.getDayList()) {
                        if (dayDTO.getEventDate().equals(dayDTO1.getEventDate())) {
                            if (count == 2) {
                                log.debug("dayDto1::"+dayDTO1);
                                // cash collection for sales
                                cashInflowTotals1.setCashCollectionSales(new Double(dayDTO1.getAmtValue()));
                                if(dayDTO1.getTotalId() != null) {
                                    cashInflowTotals1.setId(dayDTO1.getTotalId());
                                }
                            }
                            else if(count == 3) {
                                // Cash collection for Accounts Receivable
                                cashInflowTotals1.setCashCollectionForAR(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 4) {
                                // Total Accounts Receivable
                                cashInflowTotals1.setTotalAR(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 5) {
                                // Total Cash In flows
                                cashInflowTotals1.setTotalCashInflow(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 6) {
                                // Receivables that are due
                                cashInflowTotals1.setDueARs(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 7) {
                                // Net Missed collection (or net old Collection) on the day
                                cashInflowTotals1.setNetMissedCollection(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 8) {
                                // Sum of Receivables that are past due dates
                                cashInflowTotals1.setSumOfDueARs(new Double(dayDTO1.getAmtValue()));
                            }
                            break;
                        }
                    }
                }
                count++;
            }
            cashInflowTotals.add(cashInflowTotals1);
        }
        log.debug("before cashInflowTotals:"+cashInflowTotals);
        customCashInflowTotalsRepository.saveAll(cashInflowTotals);
        log.debug("after cashInflowTotals:"+cashInflowTotals);
        for(CashInflowTotals cashInflowTotals1: cashInflowTotals){
            count = 0;
            for (CashbudgetWrapperDTO cashbudgetWrapperDTO : cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
                //log.debug(count+"::cashbudgetWrapperDTO::"+cashbudgetWrapperDTO);
                if (count > 1) {
                    for (DayDTO dayDTO1 : cashbudgetWrapperDTO.getDayList()) {
                        if (cashInflowTotals1.getCashInflowTotalDate().equals(dayDTO1.getEventDate())) {
                            //log.debug(cashInflowTotals1.getId()+"::setting total id for "+cashInflowTotals1.getCashInflowTotalDate());
                            dayDTO1.setTotalId(cashInflowTotals1.getId());
                            break;
                        }
                    }
                }
                count++;
            }
        }
    }

    public void saveCashInflowReceivables_deprecated(CashbudgetWrapperDTO cashbudgetWrapperDTO, CashInflowWrapperDTO cashInflowWrapperDTO,
                                          CashInflowData cashInflowData) {
        //log.debug("cashbudgetWrapperDTO::"+cashbudgetWrapperDTO);
        //log.debug("cashInflowWrapperDTO::"+cashInflowWrapperDTO);
        //set cash inflow data id receivables and save it.
        List<CashInflowReceivablesDTO> newList = new ArrayList<CashInflowReceivablesDTO>();
        for(DayDTOWrapper dayDTOWrapper: cashbudgetWrapperDTO.getDayDTOWrappers()) {
            if(cashInflowWrapperDTO.getCashInflowData().getEditId().equals(dayDTOWrapper.getEditId())) {
                for (DayDTO dayDTO: dayDTOWrapper.getDayDtos()) {
                    if(dayDTO.getCashInflowReceivables() != null){
                        for (CashInflowReceivablesDTO cashInflowReceivablesDTO: dayDTO.getCashInflowReceivables()) {
                            cashInflowReceivablesDTO.setCidId(cashInflowWrapperDTO.getCashInflowData().getId());
                            cashInflowReceivablesDTO.setSalesDate(cashInflowWrapperDTO.getCashInflowData().getSalesDate());
                            if(("W".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode()) ||
                                ("R".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode()))) &&
                            //if("R".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode()) &&
                                cashInflowReceivablesDTO.getReceivablePercent() == null) {
                                log.debug("************not adding entry.."+cashInflowReceivablesDTO);
                            }
                            else{
                                log.debug("receivableDate::"+cashInflowReceivablesDTO.getReceivableDate());
                                log.debug("salesDate::"+cashInflowReceivablesDTO.getSalesDate());
                                newList.add(cashInflowReceivablesDTO);
                                log.debug("************adding entry.."+cashInflowReceivablesDTO);
                                // CashInflowReceivables cashInflowReceivables = cashInflowReceivablesMapper.toEntity(cashInflowReceivablesDTO);
                                // always add a new one
                                // cashInflowReceivables.setId(null);
                                // customCashInflowReceivablesRepository.save(cashInflowReceivables);
                                // save paidAmts if its yellow and range here
                                // saveCashInflowReceivedAmts(cashInflowReceivablesDTO, cashInflowReceivables, cashInflowData);
                                // cashInflowReceivablesDTO.setId(cashInflowReceivables.getId());
                            }
                        }
                    }
                }
            }
        }
        log.debug("receivables entry....."+newList);
        if(newList.size() > 0) {
            int count = 0;
            for(CashInflowReceivablesDTO cashInflowReceivablesDTO: newList){
                cashInflowReceivablesDTO.setEditId(count+"");
                count++;
            }
            List<CashInflowReceivables> cashInflowReceivables = cashInflowReceivablesMapper.toEntity(newList);
            for (CashInflowReceivables cashInflowReceivables1 : cashInflowReceivables) {
                //for inserting new one
                cashInflowReceivables1.setId(null);
            }
            customCashInflowReceivablesRepository.saveAll(cashInflowReceivables);
            List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = cashInflowReceivablesMapper.toDto(cashInflowReceivables);
            for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOS){
                cashInflowReceivablesDTO.setPaidAmts(getPaidAmts(cashInflowReceivablesDTO.getEditId(), newList));
            }
            // save paidAmts if its yellow and range here
            saveCashInflowReceivedAmts(cashInflowReceivablesDTOS, cashInflowData);
            if (!("R".equalsIgnoreCase(cashInflowData.getInflowType()))) {
                saveCashInflowRange(newList, cashInflowData);
            }
        }

        //save out of range receivables for that sale
        for (CashInflowWrapperDTO cashInflowWrapperDTO1 : cashbudgetWrapperDTO.getCashInflowWrappers()) {
            log.debug("cashInflowWrapperDTO1::"+cashInflowWrapperDTO1);
            //log.debug("cashInflowData::"+cashInflowData);
            //log.debug("cashInflowWrapperDTO::"+cashInflowWrapperDTO);
            log.debug("cashInflowWrapperDTO1.getCashInflowData().getEditId()::"+cashInflowWrapperDTO1.getCashInflowData().getEditId());
            log.debug("cashInflowWrapperDTO.getCashInflowData().getEditId()::"+cashInflowWrapperDTO.getCashInflowData().getEditId());
            if (cashInflowWrapperDTO1.getOutOfRangeRbls() != null &&
                cashInflowWrapperDTO1.getCashInflowData().getEditId().equals(cashInflowWrapperDTO.getCashInflowData().getEditId())) {
                for (CashInflowReceivablesDTO cashInflowReceivablesDTO : cashInflowWrapperDTO1.getOutOfRangeRbls()) {
                    cashInflowReceivablesDTO.setCidId(cashInflowData.getId());
                    cashInflowReceivablesDTO.setSalesDate(cashInflowWrapperDTO1.getCashInflowData().getSalesDate());
                }
                List<CashInflowReceivables> cashInflowReceivables = cashInflowReceivablesMapper.toEntity(cashInflowWrapperDTO1.getOutOfRangeRbls());
                customCashInflowReceivablesRepository.saveAll(cashInflowReceivables);
                cashInflowWrapperDTO1.setOutOfRangeRbls(cashInflowReceivablesMapper.toDto(cashInflowReceivables));
                if(!("R".equalsIgnoreCase(cashInflowData.getInflowType()))){
                    saveCashInflowRange(cashInflowWrapperDTO1.getOutOfRangeRbls(), cashInflowData);
                }

            }
        }
    }

    private List<KeyValueDTO> getPaidAmts(String editId, List<CashInflowReceivablesDTO> newList) {
        for(CashInflowReceivablesDTO cashInflowReceivablesDTO: newList){
            if(editId.equalsIgnoreCase(cashInflowReceivablesDTO.getEditId())){
                return cashInflowReceivablesDTO.getPaidAmts();
            }
        }
        return null;
    }

    public void deleteCashInflowSubData(CashInflowData cashInflowData) {
        log.debug("going to delete ********************cashInflowData::"+cashInflowData);
        customCashInflowRangeRepository.deleteByCid(cashInflowData);
        // delete and insert
        //customCashReceivableDetailsRepository.deleteByCid(cashInflowData);
        //first delete the receivables -
        //customCashInflowReceivablesRepository.deleteByCid(cashInflowData);
        log.debug("delete done ********************cashInflowData id::"+cashInflowData.getId());
    }

    private void saveCashInflowRange(List<CashInflowReceivablesDTO> cashInflowReceivables, CashInflowData cashInflowData) {
        //create cashinflowrange list and save it
        List<CashInflowRange> cashInflowRanges = new ArrayList<CashInflowRange>();
        CashInflowReceivablesDTO cashInflowReceivables1 = null;
        for(CashInflowReceivablesDTO cashInflowReceivables2: cashInflowReceivables){
            if("Y".equalsIgnoreCase(cashInflowReceivables2.getColorCode())){
                //find the last cash inflow receivables  in yellow color code
                cashInflowReceivables1 = cashInflowReceivables2;
            }
        }
        log.debug("saveCashInflowRange::cashInflowReceivables1.."+cashInflowReceivables1);
        log.debug("cashInflowData.getSalesDate()-----"+CustomUtil.getDateInFormat(cashInflowData.getSalesDate(),"YYYYMM"));
        if(cashInflowReceivables1 != null && (!(CustomUtil.getDateInFormat(cashInflowData.getSalesDate(),"YYYYMM").equalsIgnoreCase
                (CustomUtil.getDateInFormat(cashInflowReceivables1.getReceivableDate(),"YYYYMM"))))){
            List<LocalDate> rangeEntries = getRangeEntries(cashInflowData.getSalesDate(), cashInflowReceivables1.getReceivableDate());
            for(LocalDate rangeEntry: rangeEntries) {
                CashInflowRange cashInflowRange = new CashInflowRange();
                cashInflowRange.setCid(cashInflowData);
                cashInflowRange.setSalesDate(cashInflowData.getSalesDate());
                cashInflowRange.setRangeDate(rangeEntry);
                cashInflowRanges.add(cashInflowRange);
            }
            customCashInflowRangeRepository.saveAll(cashInflowRanges);
        }
    }

    private void saveCashInflowReceivedAmts(List<CashInflowReceivablesDTO> cashInflowReceivablesDTOs, CashInflowData cashInflowData) {
        log.debug("saveCashInflowReceivedAmts::cashInflowReceivablesDTOs:"+cashInflowReceivablesDTOs);
        List<CashReceivableDetails> cashReceivableDetailsList = new ArrayList<CashReceivableDetails>();
        for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOs) {
            // go through cash receivables for yellow color code and add the paid amts details
            if ("Y".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode()) && cashInflowReceivablesDTO.getPaidAmts() != null
                && cashInflowReceivablesDTO.getPaidAmts().size() > 0) {
                for (KeyValueDTO keyValueDTO : cashInflowReceivablesDTO.getPaidAmts()) {
                    CashReceivableDetails cashReceivableDetails = new CashReceivableDetails();
                    cashReceivableDetails.setTrackId(keyValueDTO.getTrackId());
                    cashReceivableDetails.setCid(cashInflowData);
                    cashReceivableDetails.setCir(cashInflowReceivablesMapper.toEntity(cashInflowReceivablesDTO));
                    cashReceivableDetails.setReceivedAmt(new Double(keyValueDTO.getValue()));
                    cashReceivableDetails.setReceivedDate(keyValueDTO.getKey());
                    cashReceivableDetailsList.add(cashReceivableDetails);
                }
            }
        }
        log.debug("saveCashInflowReceivedAmts::cashReceivableDetailsList:"+cashReceivableDetailsList);
        if(cashReceivableDetailsList != null && cashReceivableDetailsList.size() > 0) {
            customCashReceivableDetailsRepository.saveAll(cashReceivableDetailsList);
        }
    }

    public List<LocalDate> getRangeEntries(LocalDate salesDate, LocalDate receivableDate) {
        List<LocalDate> localDates = new ArrayList<LocalDate>();
        LocalDate nextdate1 = salesDate.plusMonths(1);
        log.debug("nextdate1-----"+CustomUtil.getDateInFormat(nextdate1,"YYYYMM"));
        log.debug("receivableDate-----"+CustomUtil.getDateInFormat(receivableDate,"YYYYMM"));
        while(!(CustomUtil.getDateInFormat(nextdate1,"YYYYMM").equalsIgnoreCase
            (CustomUtil.getDateInFormat(receivableDate,"YYYYMM")))) {
            localDates.add(LocalDate.of(nextdate1.getYear(), nextdate1.getMonth(), 1));
            nextdate1 = nextdate1.plusMonths(1);
            log.debug("nextdate1::"+nextdate1);
        }
        log.debug("range entries::"+localDates);
        return localDates;
    }

    private RecurringCBEntries getRecurringCEEntries(CashInflowDataDTO cashInflowDataDTO, CashOutflowDataDTO cashOutflowDataDTO, String recurType) {
        RecurringCBEntries recurringCBEntries = new RecurringCBEntries();
        recurringCBEntries.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        recurringCBEntries.setCreateTime(ZonedDateTime.now());
        if("AR".equalsIgnoreCase(recurType)) {
            recurringCBEntries.setEntryAmount(cashInflowDataDTO.getSalesAmount());
            recurringCBEntries.setEntryName(cashInflowDataDTO.getReceivableName());
        }
        else {
            recurringCBEntries.setEntryAmount(cashOutflowDataDTO.getExpenseAmount());
            recurringCBEntries.setEntryName(cashOutflowDataDTO.getPayableName());
        }
        recurringCBEntries.setEntryStatus("A");
        recurringCBEntries.setEntryType(recurType);
        recurringCBEntries.setUpdateTime(ZonedDateTime.now());
        return recurringCBEntries;
    }

    public CashInflowMasterWrapperDTO getCashInflowData(LocalDate localDate, String pageInd) {
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = new CashInflowMasterWrapperDTO();
        CashInflowMaster cashInflowMaster = customCashInflowMasterRepository.findByCompanyInfo(customUserService.getLoggedInCompanyInfo());
        if(cashInflowMaster != null) {
            cashInflowMasterWrapperDTO.setCashInflowMaster(cashInflowMasterMapper.toDto(cashInflowMaster));
        }
        else{
            cashInflowMasterWrapperDTO.setCashInflowMaster(new CashInflowMasterDTO());
        }
        List<DayDTO> dayDTOS = customUserService.getDayList(localDate);
        LocalDate stDate = dayDTOS.get(0).getEventDate();
        LocalDate endDate = dayDTOS.get(dayDTOS.size() - 1).getEventDate();
        List<CashInflowData> cashInflowDataList = customCashInflowDataRepository.findBySalesDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        List<CashInflowDataDTO> cashInflowDataDTOS = cashInflowDataMapper.toDto(cashInflowDataList);
        // add out of range cash inflow datas
        addOutOfRangeCashInflowData(cashInflowDataDTOS, stDate,endDate);
        // add range cashinflows to note that there are some AR expecting in the future months
        addRangeCashInflows(cashInflowDataDTOS, stDate,endDate);
        log.debug("cashinflowdatas before adding recurring.."+cashInflowDataDTOS);
        if(!("delete".equalsIgnoreCase(pageInd))){
            addCashInflowRecurringMasterEntries(cashInflowDataDTOS, stDate);
        }
        List<CashbudgetWrapperDTO> cashbudgetWrapperDTOs = new ArrayList<CashbudgetWrapperDTO>();
        CashbudgetWrapperDTO cashbudgetWrapperDTO = null;
        List<DayDTOWrapper> dayDTOWrappers1 = new ArrayList<DayDTOWrapper>();
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS1 = new ArrayList<CashInflowWrapperDTO>();
        // first add sales entries
        DayDTOWrapper dayDTOWrapper = null;
        // add out of range receivables and their corresponding sales entry
        // get receivables for the given date period and get the cash inflow data for that receivable and add to the cashinflow data list
        int editId = 0;
        for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOS) {
            if(!("R".equalsIgnoreCase(cashInflowDataDTO.getInflowType()))){
                CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
                cashInflowDataDTO.setEditId("top"+editId);
                cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
                List<CashInflowReceivables> cashInflowReceivablesList = customCashInflowReceivablesRepository.findByCid(cashInflowDataMapper.toEntity(cashInflowDataDTO));
                List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = cashInflowReceivablesMapper.toDto(cashInflowReceivablesList);
                cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesDTOS);
                cashInflowWrapperDTOS1.add(cashInflowWrapperDTO);
                //add received amts for the yellow color code so that it can be tracked easily in UI
                //addReceivedAmts(cashInflowReceivablesDTOS);
                //add target receivables to cash inflow wrapper
                //addTargetReceibables(cashInflowWrapperDTO, cashInflowReceivablesDTOS);
                log.debug(cashInflowDataDTO.getId()+"::cashReceivablesList:"+cashInflowReceivablesDTOS);
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                addEditId(dayDTOS1,"top"+editId);
                addDataToDayDTO(cashInflowWrapperDTO, dayDTOS1, cashInflowReceivablesDTOS);
                dayDTOWrapper = new DayDTOWrapper();
                dayDTOWrapper.setDayDtos(dayDTOS1);
                dayDTOWrapper.setEditId("top"+editId);
                dayDTOWrappers1.add(dayDTOWrapper);
                editId++;
            }
        }

        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setCashInflowWrappers(cashInflowWrapperDTOS1);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers1);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        List<DayDTOWrapper> dayDTOWrappers2 = new ArrayList<DayDTOWrapper>();
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS2 = new ArrayList<CashInflowWrapperDTO>();
        // then add recurring entries
        editId = 0;
        for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOS) {
            if("R".equalsIgnoreCase(cashInflowDataDTO.getInflowType())){
                CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
                cashInflowDataDTO.setEditId("down"+editId);
                cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
                List<CashInflowReceivables> cashInflowReceivablesList = null;
                List<CashInflowReceivablesDTO> cashInflowReceivablesDTOList = null;
                if(cashInflowDataDTO.getId() != null) {
                    cashInflowReceivablesList = customCashInflowReceivablesRepository.findByCid(cashInflowDataMapper.toEntity(cashInflowDataDTO));
                    cashInflowReceivablesDTOList = cashInflowReceivablesMapper.toDto(cashInflowReceivablesList);
                    //add received amts for the yellow color code so that it can be tracked easily in UI
                    //addReceivedAmts(cashInflowReceivablesDTOList);
                }
                // if there are no cashInflowReceivables and if the cashinflow data has credit period and sales percent - create receivable with yellow color and add to the list
                // this will be for adding from the master recurring entries
                if(!(cashInflowReceivablesDTOList != null && cashInflowReceivablesDTOList.size() > 0)
                    && cashInflowDataDTO.getCreditPeriod() != null
                    && cashInflowDataDTO.getCreditSalesPercent() != null){
                    cashInflowReceivablesDTOList = new ArrayList<CashInflowReceivablesDTO>();
                    addReceivableForRecurringEntries(cashInflowDataDTO, cashInflowReceivablesDTOList, stDate);
                }
                log.debug("after recurring...."+cashInflowReceivablesDTOList);
                //addTargetReceibables(cashInflowWrapperDTO, cashInflowReceivablesDTOList);
                log.debug(cashInflowDataDTO.getId()+"::R::cashReceivablesList:"+cashInflowReceivablesDTOList);
                cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesDTOList);
                cashInflowWrapperDTOS2.add(cashInflowWrapperDTO);
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                addEditId(dayDTOS1,"down"+editId);
                addDataToDayDTO(cashInflowWrapperDTO, dayDTOS1, cashInflowReceivablesDTOList);
                dayDTOWrapper = new DayDTOWrapper();
                dayDTOWrapper.setDayDtos(dayDTOS1);
                dayDTOWrapper.setEditId("down"+editId);
                dayDTOWrappers2.add(dayDTOWrapper);
                editId++;
            }
        }
        cashbudgetWrapperDTO.setCashInflowWrappers(cashInflowWrapperDTOS2);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers2);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        //get the last day of prev months totals
        LocalDate prevDate = stDate.minusDays(1);
        log.debug(customUserService.getLoggedInCompanyInfo().getId()+"prevDate::"+prevDate);
        CashInflowTotals cashInflowTotals1 = customCashInflowTotalsRepository.findTotalsByDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),prevDate);
        // then totals ...
        List<CashInflowTotals> cashInflowTotals = customCashInflowTotalsRepository.findByTotalDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        Double carryForwardAR = 0.0;
        if(cashInflowTotals1 != null && cashInflowTotals1.getId() != null) {
            carryForwardAR = cashInflowTotals1.getTotalAR();
            log.debug("carryforward from "+stDate+"::"+carryForwardAR);
            List<CashInflowTotals> totalsList = new ArrayList<CashInflowTotals>();
            totalsList.add(cashInflowTotals1);
            for(int i=2;i<=8;i++) {
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                for (DayDTO dayDTO : dayDTOS1) {
                    setCashInflowTotalValue(totalsList, dayDTO, i);
                }
                cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
                cashbudgetWrapperDTO.setDayList(dayDTOS1);
                cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
            }
        }
        else {
            cashInflowTotals = null;
        }
        if(cashInflowTotals != null && cashInflowTotals.size() > 0) {
            //carryForwardAR = cashInflowTotals.get(cashInflowTotals.size() - 1).getTotalAR();
            log.debug("there is no carryforward as there are no prev entries only current month is there");
            for(int i=2;i<=8;i++) {
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                for (DayDTO dayDTO : dayDTOS1) {
                    setCashInflowTotalValue(cashInflowTotals, dayDTO, i);
                }
                cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
                cashbudgetWrapperDTO.setDayList(dayDTOS1);
                cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
            }
        }
        else {
            cashInflowTotals1 = null;
        }
        if(cashInflowTotals == null && cashInflowTotals1 == null) {
            log.debug("there is no carryforward as there are no prev entries and current month");
            //if there is nothing ... then add dummy records
            setCashflowTotalsPlaceHolders(cashbudgetWrapperDTOs, dayDTOS);
        }
        cashInflowMasterWrapperDTO.setCarryForwardAR(carryForwardAR);
        cashInflowMasterWrapperDTO.setCashbudgetWrappers(cashbudgetWrapperDTOs);
        return cashInflowMasterWrapperDTO;
    }

    public void addReceivedAmts_old(List<CashInflowReceivablesDTO> cashInflowReceivablesDTOList) {
        if(cashInflowReceivablesDTOList != null && cashInflowReceivablesDTOList.size() > 0){
            for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOList) {
                List<CashReceivableDetails> cashReceivableDetails = customCashReceivableDetailsRepository.findByCir(cashInflowReceivablesMapper.toEntity(cashInflowReceivablesDTO));
                if(cashReceivableDetails != null){
                    List<KeyValueDTO> keyValueDTOS = new ArrayList<KeyValueDTO>();
                    for(CashReceivableDetails cashReceivableDetails1: cashReceivableDetails){
                        KeyValueDTO keyValueDTO = new KeyValueDTO();
                        keyValueDTO.setValue(cashReceivableDetails1.getReceivedAmt()+"");
                        keyValueDTO.setKey(cashReceivableDetails1.getReceivedDate());
                        keyValueDTO.setTrackId(cashReceivableDetails1.getTrackId());
                        keyValueDTOS.add(keyValueDTO);
                    }
                    cashInflowReceivablesDTO.setPaidAmts(keyValueDTOS);
                }
            }
        }
    }

    private void addRangeCashInflows(List<CashInflowDataDTO> cashInflowDataDTOS, LocalDate stDate, LocalDate endDate) {
        List<CashInflowRange> cashInflowRanges = customCashInflowRangeRepository.getAllByRangeDatesAndCompany(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        List<CashInflowDataDTO> cashInflowDataDTOS1 = new ArrayList<CashInflowDataDTO>();
        if(cashInflowRanges != null) {
            for (CashInflowRange cashInflowRange: cashInflowRanges) {
                cashInflowDataDTOS1.add(cashInflowDataMapper.toDto(cashInflowRange.getCid()));
            }
            cashInflowDataDTOS.addAll(cashInflowDataDTOS1);
        }
    }

    private void addReceivableForRecurringEntries(CashInflowDataDTO cashInflowDataDTO, List<CashInflowReceivablesDTO> cashInflowReceivablesList, LocalDate stDate) {
        log.debug("adding recur details..."+cashInflowDataDTO);
        CashInflowReceivablesDTO cashInflowReceivablesDTO = new CashInflowReceivablesDTO();
        cashInflowReceivablesDTO.setColorCode("Y");
        cashInflowReceivablesDTO.setReceivableDate(stDate);
        cashInflowReceivablesDTO.setSalesDate(stDate);
        cashInflowReceivablesDTO.setReceivablePercent(100.0);
        cashInflowReceivablesDTO.setReceivableAmt(cashInflowDataDTO.getSalesAmount());
        cashInflowReceivablesList.add(cashInflowReceivablesDTO);
        log.debug("adding recur cashInflowReceivablesList..."+cashInflowReceivablesList);
    }

    private void addCashInflowRecurringMasterEntries(List<CashInflowDataDTO> cashInflowDataDTOList, LocalDate stDate) {
        List<RecurringCBEntries> recurringCBEntries = customRecurringCBEntriesRepository.findByEntryStatusAndEntryTypeAndCompanyInfo("A","AR",customUserService.getLoggedInCompanyInfo());
        for(RecurringCBEntries recurringCBEntries1: recurringCBEntries){
            if(!(checkCashInflowRecurringEntryExists(cashInflowDataDTOList, recurringCBEntries1.getId()))){
                log.debug("adding.recur inflow.."+recurringCBEntries1.getId()+"::"+recurringCBEntries1.getEntryName());
                CashInflowDataDTO cashInflowDataDTO = new CashInflowDataDTO();
                cashInflowDataDTO.setReceivableName(recurringCBEntries1.getEntryName());
                cashInflowDataDTO.setSalesDate(stDate);
                cashInflowDataDTO.setCreditPeriod("1");
                cashInflowDataDTO.setCreditSalesPercent(100.0);
                cashInflowDataDTO.setSalesAmount(recurringCBEntries1.getEntryAmount());
                cashInflowDataDTO.setInflowType("R");
                cashInflowDataDTO.setRecurId(recurringCBEntries1.getId());
                cashInflowDataDTOList.add(cashInflowDataDTO);
            }
        }
    }

    private boolean checkCashInflowRecurringEntryExists(List<CashInflowDataDTO> cashInflowDataDTOList, Long recurId){
        for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOList) {
            if(cashInflowDataDTO.getRecurId() != null && cashInflowDataDTO.getRecurId().equals(recurId)){
                return true;
            }
        }
        return false;
    }

    private void addPayableForRecurringEntries(CashOutflowDataDTO cashOutflowDataDTO, List<CashOutflowPayablesDTO> cashOutflowPayables, LocalDate stDate) {
        CashOutflowPayablesDTO cashOutflowPayablesDTO = new CashOutflowPayablesDTO();
        cashOutflowPayablesDTO.setColorCode("Y");
        cashOutflowPayablesDTO.setPayableDate(stDate);
        cashOutflowPayablesDTO.setPayableAmt(cashOutflowDataDTO.getExpenseAmount());
        cashOutflowPayablesDTO.setPayablePercent(100.0);
        cashOutflowPayables.add(cashOutflowPayablesDTO);
    }

    private void addCashOutflowRecurringMasterEntries(List<CashOutflowDataDTO> cashOutflowDataList, LocalDate stDate) {
        List<RecurringCBEntries> recurringCBEntries = customRecurringCBEntriesRepository.findByEntryStatusAndEntryTypeAndCompanyInfo("A","AP",customUserService.getLoggedInCompanyInfo());
        for(RecurringCBEntries recurringCBEntries1: recurringCBEntries){
            if(!(checkCashOutflowRecurringEntryExists(cashOutflowDataList, recurringCBEntries1.getId()))){
                log.debug("adding.recur outflow.."+recurringCBEntries1.getId()+"::"+recurringCBEntries1.getEntryName());
                CashOutflowDataDTO cashOutflowDataDTO = new CashOutflowDataDTO();
                cashOutflowDataDTO.setPayableName(recurringCBEntries1.getEntryName());
                cashOutflowDataDTO.setExpenseDate(stDate);
                cashOutflowDataDTO.setCreditPeriod("1");
                cashOutflowDataDTO.setCreditSalesPercent(100.0);
                cashOutflowDataDTO.setExpenseAmount(recurringCBEntries1.getEntryAmount());
                cashOutflowDataDTO.setOutflowType("R");
                cashOutflowDataDTO.setRecurId(recurringCBEntries1.getId());
                cashOutflowDataList.add(cashOutflowDataDTO);
            }
        }
    }

    private boolean checkCashOutflowRecurringEntryExists(List<CashOutflowDataDTO> cashOutflowDataList, Long recurId){
        for (CashOutflowDataDTO cashOutflowDataDTO : cashOutflowDataList) {
            if(recurId.equals(cashOutflowDataDTO.getRecurId())){
                return true;
            }
        }
        return false;
    }

    public void addTargetReceibables_old(CashInflowWrapperDTO cashInflowWrapperDTO1, List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS) {
        if(cashInflowReceivablesDTOS == null)
            return;
        for (CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOS){
            if(cashInflowReceivablesDTO != null && "Y".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())){
                if(cashInflowWrapperDTO1.getCashInflowRbls() != null) {
                    cashInflowWrapperDTO1.getCashInflowRbls().add(cashInflowReceivablesDTO);
                    log.debug("added addTargetReceibables....");
                } else {
                    List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS1 = new ArrayList<CashInflowReceivablesDTO>();
                    cashInflowReceivablesDTOS1.add(cashInflowReceivablesDTO);
                    cashInflowWrapperDTO1.setCashInflowRbls(cashInflowReceivablesDTOS1);
                    log.debug("created addTargetReceibables....");
                }
            }
        }
    }

    private void addTargetPayables(CashOutflowWrapperDTO cashOutflowWrapperDTO1, List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS) {
        if(cashOutflowPayablesDTOS == null)
            return;
        for (CashOutflowPayablesDTO cashOutflowPayablesDTO: cashOutflowPayablesDTOS){
            if(cashOutflowPayablesDTO != null && "Y".equalsIgnoreCase(cashOutflowPayablesDTO.getColorCode())){
                if(cashOutflowWrapperDTO1.getCashOutflowPbls() != null) {
                    cashOutflowWrapperDTO1.getCashOutflowPbls().add(cashOutflowPayablesDTO);
                    log.debug("added addTargetPayables....");
                } else {
                    List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS1 = new ArrayList<CashOutflowPayablesDTO>();
                    cashOutflowPayablesDTOS1.add(cashOutflowPayablesDTO);
                    cashOutflowWrapperDTO1.setCashOutflowPbls(cashOutflowPayablesDTOS1);
                    log.debug("created addTargetPayables....");
                }
            }
        }
    }

    /**
     * go through the receivables for this start and end date and get the corresponding cash inflow data
     * @param cashInflowDataDTOList
     * @param stDate
     * @param endDate
     */
    private void addOutOfRangeCashInflowData(List<CashInflowDataDTO> cashInflowDataDTOList, LocalDate stDate, LocalDate endDate) {
        List<CashInflowData> cashInflowDataList1 = customCashInflowDataRepository.findOutOfRangeReceivables(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        List<CashInflowDataDTO> cashInflowDataDTOS = cashInflowDataMapper.toDto(cashInflowDataList1);
        for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOS){
            if(!(checkCashInflowDataExists(cashInflowDataDTOList, cashInflowDataDTO.getId()))){
                log.debug("adding.outof inflow.."+cashInflowDataDTO.getId()+"::"+cashInflowDataDTO.getReceivableName());
                cashInflowDataDTOList.add(cashInflowDataDTO);
            }
        }
    }

    private boolean checkCashInflowDataExists(List<CashInflowDataDTO> cashInflowDataDTOList, Long id){
        for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOList) {
            if(id.equals(cashInflowDataDTO.getId())){
                return true;
            }
        }
        return false;
    }

    private void setCashInflowTotalValue(List<CashInflowTotals> cashInflowTotals, DayDTO dayDTO, int count) {
        for(CashInflowTotals cashInflowTotals1: cashInflowTotals) {
            if(cashInflowTotals1.getCashInflowTotalDate().equals(dayDTO.getEventDate())){
                dayDTO.setTotalId(cashInflowTotals1.getId());
                if (count == 2) {
                    // cash collection for sales
                    dayDTO.setAmtValue(cashInflowTotals1.getCashCollectionSales()+"");
                }
                else if(count == 3) {
                    // Cash collection for Accounts Receivable
                    dayDTO.setAmtValue(cashInflowTotals1.getCashCollectionForAR()+"");
                }
                else if(count == 4) {
                    // Total Accounts Receivable
                    dayDTO.setAmtValue(cashInflowTotals1.getTotalAR()+"");
                }
                else if(count == 5) {
                    // Total Cash In flows
                    dayDTO.setAmtValue(cashInflowTotals1.getTotalCashInflow()+"");
                }
                else if(count == 6) {
                    // Receivables that are due
                    dayDTO.setAmtValue(cashInflowTotals1.getDueARs()+"");
                }
                else if(count == 7) {
                    // Net Missed collection (or net old Collection) on the day
                    dayDTO.setAmtValue(cashInflowTotals1.getNetMissedCollection()+"");
                }
                else if(count == 8) {
                    // Sum of Receivables that are past due dates
                    dayDTO.setAmtValue(cashInflowTotals1.getSumOfDueARs()+"");
                }
                break;
            }
        }
    }

    private void setCashOutflowTotalValue(List<CashOutflowTotals> cashOutflowTotals, DayDTO dayDTO, int count) {
        for(CashOutflowTotals cashOutflowTotals1: cashOutflowTotals) {
            if(cashOutflowTotals1.getCashOutflowTotalDate().equals(dayDTO.getEventDate())){
                dayDTO.setTotalId(cashOutflowTotals1.getId());
                if (count == 2) {
                    // cash payment for purchases
                    dayDTO.setAmtValue(cashOutflowTotals1.getCashPaymentPurchases()+"");
                }
                else if(count == 3) {
                    // Cash payments for Accounts Payable
                    dayDTO.setAmtValue(cashOutflowTotals1.getCashRepaymentAP()+"");
                }
                else if(count == 4) {
                    // Total Accounts Payable
                    dayDTO.setAmtValue(cashOutflowTotals1.getTotalAP()+"");
                }
                else if(count == 5) {
                    // Total Cash Out flows
                    dayDTO.setAmtValue(cashOutflowTotals1.getTotalCashOutflow()+"");
                }
                else if(count == 6) {
                    // Payables that are due
                    dayDTO.setAmtValue(cashOutflowTotals1.getDueAPs()+"");
                }
                else if(count == 7) {
                    // Net Missed payment (or net old Collection) on the day
                    dayDTO.setAmtValue(cashOutflowTotals1.getNetMissedPayment()+"");
                }
                else if(count == 8) {
                    // Sum of payables that are past due dates
                    dayDTO.setAmtValue(cashOutflowTotals1.getSumOfDueAPs()+"");
                }
                break;
            }
        }
    }

    private void setCashflowTotalsPlaceHolders(List<CashbudgetWrapperDTO> cashbudgetWrapperDTOs, List<DayDTO> dayDTOS) {
        CashbudgetWrapperDTO cashbudgetWrapperDTO = null;
        // adding totals - cash collection for sales
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        // Cash collection for Accounts Receivable
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        // Total Accounts Receivable
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        // Total Cash In flows
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        // Receivables that are due
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        // Net Missed collection (or net old Collection) on the day
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        // Sum of Receivables that are past due dates
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
    }

    private void addDataToDayDTO(CashInflowWrapperDTO cashInflowWrapperDTO, List<DayDTO> dayDTOS, List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS) {
        if(cashInflowReceivablesDTOS == null)
            return;
        boolean found = false;
        for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOS){
            found = false;
            log.debug("cashinflowreceivablesdto......"+cashInflowReceivablesDTO);
            for(DayDTO dayDTO: dayDTOS){
                dayDTO.setCiwId(cashInflowWrapperDTO.getCashInflowData().getId());
                //for W and R we have to check received date
                if((cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("R") || cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("W"))
                    && dayDTO.getEventDate().equals(cashInflowReceivablesDTO.getReceivedDate())) {
                    found = true;
                    if(cashInflowReceivablesDTO.getReceivableAmt() != null){
                        cashInflowReceivablesDTO.setTargetAmt(cashInflowReceivablesDTO.getReceivableAmt());
                    }
                    if(dayDTO.getCashInflowReceivables() != null) {
                        dayDTO.getCashInflowReceivables().add(cashInflowReceivablesDTO);
                        log.debug("1added receivables...."+cashInflowReceivablesDTO);
                    } else {
                        List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS1 = new ArrayList<CashInflowReceivablesDTO>();
                        cashInflowReceivablesDTOS1.add(cashInflowReceivablesDTO);
                        dayDTO.setCashInflowReceivables(cashInflowReceivablesDTOS1);
                        log.debug("2created receivables...."+cashInflowReceivablesDTO);
                    }
                }
                else if((cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("Y") || cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("G"))
                && dayDTO.getEventDate().equals(cashInflowReceivablesDTO.getReceivableDate())){
                    found = true;
                    if(cashInflowReceivablesDTO.getReceivableAmt() != null){
                        cashInflowReceivablesDTO.setTargetAmt(cashInflowReceivablesDTO.getReceivableAmt());
                    }
                    if(dayDTO.getCashInflowReceivables() != null) {
                        dayDTO.getCashInflowReceivables().add(cashInflowReceivablesDTO);
                        log.debug("3added receivables...."+cashInflowReceivablesDTO);
                    } else {
                        List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS1 = new ArrayList<CashInflowReceivablesDTO>();
                        cashInflowReceivablesDTOS1.add(cashInflowReceivablesDTO);
                        dayDTO.setCashInflowReceivables(cashInflowReceivablesDTOS1);
                        log.debug("4created receivables...."+cashInflowReceivablesDTO);
                    }
                }
                /*
                else if(CustomUtil.isDateInBetween(dayDTO.getEventDate(), cashInflowWrapperDTO.getCashInflowData().getSalesDate(),
                    cashInflowReceivablesDTOS.get(cashInflowReceivablesDTOS.size() - 1).getReceivableDate())) {
                    log.debug("event date is inbetween ");
                    dayDTO.setLink(true);
                }
                */
            }
            if(!found){
                if(cashInflowWrapperDTO.getOutOfRangeRbls() != null) {
                    cashInflowWrapperDTO.getOutOfRangeRbls().add(cashInflowReceivablesDTO);
                    log.debug("added out of range receivables....");
                } else {
                    List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS1 = new ArrayList<CashInflowReceivablesDTO>();
                    cashInflowReceivablesDTOS1.add(cashInflowReceivablesDTO);
                    cashInflowWrapperDTO.setOutOfRangeRbls(cashInflowReceivablesDTOS1);
                    log.debug("created out of range receivables....");
                }
            }
        }
        // check for Y color code in receivables and add a dummy white code if its not there. This is specifically for display purpose
        // so that they can click and pay on the target date.
        for(DayDTO dayDTO: dayDTOS){
            if(dayDTO.getCashInflowReceivables() != null) {
                log.debug("getCashInflowReceivables... checking........."+dayDTO.getCashInflowReceivables());
                if(dayDTO.getCashInflowReceivables().size() == 1 && "Y".equalsIgnoreCase(dayDTO.getCashInflowReceivables().get(0).getColorCode())){
                    CashInflowReceivablesDTO cashInflowReceivablesDTO = new CashInflowReceivablesDTO();
                    cashInflowReceivablesDTO.setColorCode("W");
                    cashInflowReceivablesDTO.setSalesDate(dayDTO.getCashInflowReceivables().get(0).getSalesDate());
                    cashInflowReceivablesDTO.setReceivableDate(dayDTO.getCashInflowReceivables().get(0).getReceivableDate());
                    dayDTO.getCashInflowReceivables().add(cashInflowReceivablesDTO);
                    log.debug("getCashInflowReceivables... adding........."+cashInflowReceivablesDTO);
                    log.debug("getCashInflowReceivables... after adding........."+dayDTO.getCashInflowReceivables());
                }
            }
        }
    }

    private void addPayablesToDayDTO(CashOutflowWrapperDTO cashOutflowWrapperDTO, List<DayDTO> dayDTOS, List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS) {
        if(cashOutflowPayablesDTOS == null)
            return;
        boolean found = false;
        for(CashOutflowPayablesDTO cashOutflowPayablesDTO: cashOutflowPayablesDTOS){
            found = false;
            for(DayDTO dayDTO: dayDTOS){
                if(dayDTO.getEventDate().equals(cashOutflowPayablesDTO.getPayableDate())){
                    found = true;
                    if(cashOutflowPayablesDTO.getPayableAmt() != null){
                        cashOutflowPayablesDTO.setTargetAmt(cashOutflowPayablesDTO.getPayableAmt());
                    }
                    if(dayDTO.getCashOutflowPayables() != null) {
                        dayDTO.getCashOutflowPayables().add(cashOutflowPayablesDTO);
                        log.debug("added payables....");
                    } else {
                        List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS1 = new ArrayList<CashOutflowPayablesDTO>();
                        cashOutflowPayablesDTOS1.add(cashOutflowPayablesDTO);
                        dayDTO.setCashOutflowPayables(cashOutflowPayablesDTOS1);
                        log.debug("created payables....");
                    }
                }
            }
            if(!found){
                if(cashOutflowWrapperDTO.getOutOfRangePbls() != null) {
                    cashOutflowWrapperDTO.getOutOfRangePbls().add(cashOutflowPayablesDTO);
                    log.debug("added outof range payables....");
                } else {
                    List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS1 = new ArrayList<CashOutflowPayablesDTO>();
                    cashOutflowPayablesDTOS1.add(cashOutflowPayablesDTO);
                    cashOutflowWrapperDTO.setOutOfRangePbls(cashOutflowPayablesDTOS1);
                    log.debug("created out of range payables....");
                }
            }
        }
        // check for Y color code in receivables and add a dummy white code if its not there. This is specifically for display purpose
        // so that they can click and pay on the target date.
        for(DayDTO dayDTO: dayDTOS){
            if(dayDTO.getCashOutflowPayables() != null) {
                log.debug("cashoutpayables... checking........."+dayDTO.getCashOutflowPayables());
                if(dayDTO.getCashOutflowPayables().size() == 1 && "Y".equalsIgnoreCase(dayDTO.getCashOutflowPayables().get(0).getColorCode())){
                    CashOutflowPayablesDTO cashOutflowPayablesDTO = new CashOutflowPayablesDTO();
                    cashOutflowPayablesDTO.setColorCode("W");
                    cashOutflowPayablesDTO.setExpenseDate(dayDTO.getCashOutflowPayables().get(0).getExpenseDate());
                    cashOutflowPayablesDTO.setPayableDate(dayDTO.getCashOutflowPayables().get(0).getPayableDate());
                    dayDTO.getCashOutflowPayables().add(cashOutflowPayablesDTO);
                    log.debug("cashoutpayables... adding........."+cashOutflowPayablesDTO);
                    log.debug("cashoutpayables... after adding........."+dayDTO.getCashOutflowPayables());
                }
            }
        }
    }

    private void addEditId(List<DayDTO> dayDTOS, String editId) {
        for(DayDTO dayDTO:dayDTOS){
            dayDTO.setEditId(editId);
        }
    }

    public CashInflowMasterWrapperDTO getCashOutflowData(LocalDate localDate, String pageInd) {
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = new CashInflowMasterWrapperDTO();
        CashOutflowMaster cashOutflowMaster = customCashOutflowMasterRepository.findByCompanyInfo(customUserService.getLoggedInCompanyInfo());
        if(cashOutflowMaster != null) {
            cashInflowMasterWrapperDTO.setCashOutflowMaster(cashOutflowMasterMapper.toDto(cashOutflowMaster));
        }
        else{
            cashInflowMasterWrapperDTO.setCashOutflowMaster(new CashOutflowMasterDTO());
        }
        List<DayDTO> dayDTOS = customUserService.getDayList(localDate);
        LocalDate stDate = dayDTOS.get(0).getEventDate();
        LocalDate endDate = dayDTOS.get(dayDTOS.size() - 1).getEventDate();
        List<CashOutflowData> cashOutflowDataList = customCashOutflowDataRepository.findByExpenseDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        // add out of range cash inflow datas
        List<CashOutflowDataDTO> cashOutflowDataDTOS = cashOutflowDataMapper.toDto(cashOutflowDataList);
        addRangeCashOutflows(cashOutflowDataDTOS, stDate,endDate);
        addOutOfRangeCashOutflowData(cashOutflowDataDTOS, stDate,endDate);
        //if the call is after delete dont add any recurring master entries
        if(!("delete".equalsIgnoreCase(pageInd))) {
            addCashOutflowRecurringMasterEntries(cashOutflowDataDTOS, stDate);
        }
        List<CashbudgetWrapperDTO> cashbudgetWrapperDTOs = new ArrayList<CashbudgetWrapperDTO>();
        CashbudgetWrapperDTO cashbudgetWrapperDTO = null;
        List<DayDTOWrapper> dayDTOWrappers1 = new ArrayList<DayDTOWrapper>();
        List<CashOutflowWrapperDTO> cashOutflowWrapperDTOS1 = new ArrayList<CashOutflowWrapperDTO>();
        // first add sales entries
        DayDTOWrapper dayDTOWrapper = null;
        // add out of range receivables and their corresponding sales entry
        // Todo add out of range receivables and their corresponding sales entry
        // get receivables for the given date period and get the cash inflow data for that receivable and add to the cashinflow data list
        int editId = 0;
        for(CashOutflowDataDTO cashOutflowDataDTO: cashOutflowDataDTOS) {
            if(!("R".equalsIgnoreCase(cashOutflowDataDTO.getOutflowType()))){
                CashOutflowWrapperDTO cashOutflowWrapperDTO = new CashOutflowWrapperDTO();
                cashOutflowDataDTO.setEditId("top"+editId);
                cashOutflowWrapperDTO.setCashOutflowData(cashOutflowDataDTO);
                cashOutflowWrapperDTOS1.add(cashOutflowWrapperDTO);
                List<CashOutflowPayables> cashOutflowPayables = customCashOutflowPayablesRepository.findByCod(cashOutflowDataMapper.toEntity(cashOutflowDataDTO));
                List<CashOutflowPayablesDTO> cashOutflowPayablesDTOList = cashOutflowPayablesMapper.toDto(cashOutflowPayables);
                //add paid amts for the yellow color code so that it can be tracked easily in UI
                addPaidAmts(cashOutflowPayablesDTOList);
                addTargetPayables(cashOutflowWrapperDTO, cashOutflowPayablesDTOList);
                log.debug(cashOutflowDataDTO.getId()+"::cashpayableslist:"+cashOutflowPayablesDTOList);
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                addEditId(dayDTOS1,"top"+editId);
                addPayablesToDayDTO(cashOutflowWrapperDTO, dayDTOS1, cashOutflowPayablesDTOList);
                dayDTOWrapper = new DayDTOWrapper();
                dayDTOWrapper.setDayDtos(dayDTOS1);
                dayDTOWrapper.setEditId("top"+editId);
                dayDTOWrappers1.add(dayDTOWrapper);
                editId++;
            }
        }

        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setCashOutflowWrappers(cashOutflowWrapperDTOS1);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers1);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        List<DayDTOWrapper> dayDTOWrappers2 = new ArrayList<DayDTOWrapper>();
        List<CashOutflowWrapperDTO> cashOutflowWrapperDTOS2 = new ArrayList<CashOutflowWrapperDTO>();
        // then add recurring entries
        editId = 0;
        for(CashOutflowDataDTO cashOutflowDataDTO: cashOutflowDataDTOS) {
            if("R".equalsIgnoreCase(cashOutflowDataDTO.getOutflowType())){
                CashOutflowWrapperDTO cashOutflowWrapperDTO = new CashOutflowWrapperDTO();
                cashOutflowDataDTO.setEditId("down"+editId);
                cashOutflowWrapperDTO.setCashOutflowData(cashOutflowDataDTO);
                cashOutflowWrapperDTOS2.add(cashOutflowWrapperDTO);
                List<CashOutflowPayables> cashOutflowPayables = null;
                List<CashOutflowPayablesDTO> cashOutflowPayablesDTOList = null;
                if(cashOutflowDataDTO.getId() != null) {
                    cashOutflowPayables = customCashOutflowPayablesRepository.findByCod(cashOutflowDataMapper.toEntity(cashOutflowDataDTO));
                    cashOutflowPayablesDTOList = cashOutflowPayablesMapper.toDto(cashOutflowPayables);
                    addPaidAmts(cashOutflowPayablesDTOList);
                }
                log.debug(cashOutflowDataDTO.getId()+"::R::cashOutflowPayables:"+cashOutflowPayables);
                if(!(cashOutflowPayablesDTOList != null && cashOutflowPayablesDTOList.size() > 0)
                    && cashOutflowDataDTO.getCreditPeriod() != null
                    && cashOutflowDataDTO.getCreditSalesPercent() != null){
                    cashOutflowPayablesDTOList = new ArrayList<CashOutflowPayablesDTO>();
                    addPayableForRecurringEntries(cashOutflowDataDTO, cashOutflowPayablesDTOList, stDate);
                }
                addTargetPayables(cashOutflowWrapperDTO, cashOutflowPayablesDTOList);
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                addEditId(dayDTOS1,"down"+editId);
                addPayablesToDayDTO(cashOutflowWrapperDTO, dayDTOS1, cashOutflowPayablesDTOList);
                dayDTOWrapper = new DayDTOWrapper();
                dayDTOWrapper.setDayDtos(dayDTOS1);
                dayDTOWrapper.setEditId("down"+editId);
                dayDTOWrappers2.add(dayDTOWrapper);
                editId++;
            }
        }
        cashbudgetWrapperDTO.setCashOutflowWrappers(cashOutflowWrapperDTOS2);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers2);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        //get the last day of prev months totals
        LocalDate prevDate = stDate.minusDays(1);
        CashOutflowTotals cashOutflowTotals1 = customCashOutflowTotalsRepository.findTotalsByDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(), prevDate);
        // then totals ...
        List<CashOutflowTotals> cashOutflowTotals = customCashOutflowTotalsRepository.findByTotalDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(), stDate, endDate);
        log.debug("*********cashoutflowtotals:::::::::::"+cashOutflowTotals);
        Double carryForwardAP = 0.0;
        if(cashOutflowTotals1 != null && cashOutflowTotals1.getId() != null) {
            //send it as first day of the calendar showing month
            cashOutflowTotals1.setCashOutflowTotalDate(stDate);
            List<CashOutflowTotals> totalsList = new ArrayList<CashOutflowTotals>();
            totalsList.add(cashOutflowTotals1);
            carryForwardAP = cashOutflowTotals1.getTotalAP();
            for(int i=2;i<=8;i++) {
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                for (DayDTO dayDTO : dayDTOS1) {
                    setCashOutflowTotalValue(totalsList, dayDTO, i);
                }
                cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
                cashbudgetWrapperDTO.setDayList(dayDTOS1);
                cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
            }
        } else if(cashOutflowTotals != null && cashOutflowTotals.size() > 0) {
            //carryForwardAP = cashOutflowTotals.get(cashOutflowTotals.size() - 1).getTotalAP();
            for(int i=2;i<=8;i++) {
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                for (DayDTO dayDTO : dayDTOS1) {
                    setCashOutflowTotalValue(cashOutflowTotals, dayDTO, i);
                }
                cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
                cashbudgetWrapperDTO.setDayList(dayDTOS1);
                cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
            }
        }
        else {
            //if there is nothing ... then add dummy records
            setCashflowTotalsPlaceHolders(cashbudgetWrapperDTOs, dayDTOS);
        }
        cashInflowMasterWrapperDTO.setCarryForwardAP(carryForwardAP);
        cashInflowMasterWrapperDTO.setCashbudgetWrappers(cashbudgetWrapperDTOs);
        return cashInflowMasterWrapperDTO;
    }

    private void addPaidAmts(List<CashOutflowPayablesDTO> cashOutflowPayablesDTOList) {
        if(cashOutflowPayablesDTOList != null && cashOutflowPayablesDTOList.size() > 0){
            for(CashOutflowPayablesDTO cashOutflowPayablesDTO: cashOutflowPayablesDTOList) {
                List<CashPayableDetails> cashPayableDetails = customCashPayableDetailsRepository.findByCop(cashOutflowPayablesMapper.toEntity(cashOutflowPayablesDTO));
                if(cashPayableDetails != null){
                    List<KeyValueDTO> keyValueDTOS = new ArrayList<KeyValueDTO>();
                    for(CashPayableDetails cashPayableDetails1: cashPayableDetails){
                        KeyValueDTO keyValueDTO = new KeyValueDTO();
                        keyValueDTO.setValue(cashPayableDetails1.getPaidAmt()+"");
                        keyValueDTO.setKey(cashPayableDetails1.getPaidDate());
                        keyValueDTO.setTrackId(cashPayableDetails1.getTrackId());
                        keyValueDTOS.add(keyValueDTO);
                    }
                    cashOutflowPayablesDTO.setPaidAmts(keyValueDTOS);
                }
            }
        }
    }

    private void addRangeCashOutflows(List<CashOutflowDataDTO> cashOutflowDataDTOS, LocalDate stDate, LocalDate endDate) {
        List<CashOutflowRange> cashInflowRanges = customCashOutflowRangeRepository.getAllByRangeDatesAndCompany(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        List<CashOutflowDataDTO> cashOutflowDataDTOS1 = new ArrayList<CashOutflowDataDTO>();
        if(cashInflowRanges != null) {
            for (CashOutflowRange cashOutflowRange: cashInflowRanges) {
                cashOutflowDataDTOS1.add(cashOutflowDataMapper.toDto(cashOutflowRange.getCod()));
            }
            cashOutflowDataDTOS.addAll(cashOutflowDataDTOS1);
        }
    }

    private void addOutOfRangeCashOutflowData(List<CashOutflowDataDTO> cashOutflowDataList, LocalDate stDate, LocalDate endDate) {
        List<CashOutflowData> cashOutflowDataList1 = customCashOutflowDataRepository.findOutOfRangePayables(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        log.debug("cashOutflowDataList1.."+cashOutflowDataList1);
        if(cashOutflowDataList1 != null) {
            List<CashOutflowDataDTO> cashOutflowDataDTOS = cashOutflowDataMapper.toDto(cashOutflowDataList1);
            for (CashOutflowDataDTO cashOutflowDataDTO : cashOutflowDataDTOS) {
                if (!(checkCashOutflowDataExists(cashOutflowDataList, cashOutflowDataDTO.getId()))) {
                    log.debug("adding..." + cashOutflowDataDTO.getId() + "::" + cashOutflowDataDTO.getPayableName());
                    cashOutflowDataList.add(cashOutflowDataDTO);
                }
            }
        }
    }

    private boolean checkCashOutflowDataExists(List<CashOutflowDataDTO> cashOutflowDataList, Long id){
        for (CashOutflowDataDTO cashOutflowDataDTO : cashOutflowDataList) {
            if(id.equals(cashOutflowDataDTO.getId())){
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void saveOrUpdateCashOutflow(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        //save cashinflow master
        CashOutflowMaster cashOutflowMaster = cashOutflowMasterMapper.toEntity(cashInflowMasterWrapperDTO.getCashOutflowMaster());
        cashOutflowMaster.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        customCashOutflowMasterRepository.save(cashOutflowMaster);
        cashInflowMasterWrapperDTO.setCashOutflowMaster(cashOutflowMasterMapper.toDto(cashOutflowMaster));
        //save recur data if there is no recur id in the records
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashOutflowWrappers() != null) {
                for (CashOutflowWrapperDTO cashOutflowWrapperDTO : cashbudgetWrapperDTO.getCashOutflowWrappers()) {
                    CashOutflowDataDTO cashOutflowDataDTO = cashOutflowWrapperDTO.getCashOutflowData();
                    if (cashOutflowDataDTO.getOutflowType() != null && "R".equalsIgnoreCase(cashOutflowDataDTO.getOutflowType())) {
                        log.debug("cashOutflowDataDTO::" + cashOutflowDataDTO);
                        RecurringCBEntries recurringCBEntries = null;
                        if (cashOutflowDataDTO.getRecurId() == null) {
                            recurringCBEntries = getRecurringCEEntries(null, cashOutflowDataDTO, "AP");
                            recurringCBEntries.setId(cashOutflowDataDTO.getRecurId());
                            customRecurringCBEntriesRepository.save(recurringCBEntries);
                            cashOutflowDataDTO.setRecurId(recurringCBEntries.getId());
                        }
                    }
                }
            }
        }
        //save cashinflow data
        //save receivables data - inside loop for each cash inflow data
        String editId = null;
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashOutflowWrappers() != null) {
                for (CashOutflowWrapperDTO cashOutflowWrapperDTO : cashbudgetWrapperDTO.getCashOutflowWrappers()) {
                    log.debug("expense date::"+cashOutflowWrapperDTO.getCashOutflowData().getExpenseDate());
                    CashOutflowData cashOutflowData = cashOutflowDataMapper.toEntity(cashOutflowWrapperDTO.getCashOutflowData());
                    cashOutflowData.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
                    customCashOutflowDataRepository.save(cashOutflowData);
                    //before over writing with the saved object, retain the ids so that we can copy over again
                    editId = cashOutflowWrapperDTO.getCashOutflowData().getEditId();
                    cashOutflowWrapperDTO.setCashOutflowData(cashOutflowDataMapper.toDto(cashOutflowData));
                    cashOutflowWrapperDTO.getCashOutflowData().setEditId(editId);
                    deleteCashOutflowSub(cashOutflowData);
                    saveCashOutflowPayables(cashbudgetWrapperDTO, cashOutflowWrapperDTO, cashOutflowData);
                }
            }
        }

        //save totals
        int count = 0;
        List<CashOutflowTotals> cashOutflowTotals = new ArrayList<CashOutflowTotals>();
        for(DayDTO dayDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayList()) {
            count = 0;
            CashOutflowTotals cashOutflowTotals1 = new CashOutflowTotals();
            cashOutflowTotals1.setCashOutflowTotalDate(dayDTO.getEventDate());
            cashOutflowTotals1.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
            for (CashbudgetWrapperDTO cashbudgetWrapperDTO : cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
                if(count > 1) {
                    for (DayDTO dayDTO1 : cashbudgetWrapperDTO.getDayList()) {
                        if (dayDTO.getEventDate().equals(dayDTO1.getEventDate())) {
                            if (count == 2) {
                                log.debug("dayDto1::"+dayDTO1);
                                // cash payment for purchases
                                cashOutflowTotals1.setCashPaymentPurchases(new Double(dayDTO1.getAmtValue()));
                                if(dayDTO1.getTotalId() != null) {
                                    cashOutflowTotals1.setId(dayDTO1.getTotalId());
                                }
                            }
                            else if(count == 3) {
                                // Cash payment for Accounts Payable
                                cashOutflowTotals1.setCashRepaymentAP(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 4) {
                                // Total Accounts Payable
                                cashOutflowTotals1.setTotalAP(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 5) {
                                // Total Cash Out flows
                                cashOutflowTotals1.setTotalCashOutflow(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 6) {
                                // Payables that are due
                                cashOutflowTotals1.setDueAPs(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 7) {
                                // Net Missed payments (or net old payment) on the day
                                cashOutflowTotals1.setNetMissedPayment(new Double(dayDTO1.getAmtValue()));
                            }
                            else if(count == 8) {
                                // Sum of Payments that are past due dates
                                cashOutflowTotals1.setSumOfDueAPs(new Double(dayDTO1.getAmtValue()));
                            }
                            break;
                        }
                    }
                }
                count++;
            }
            cashOutflowTotals.add(cashOutflowTotals1);
        }
        log.debug("before cashOutflowTotals:"+cashOutflowTotals);
        customCashOutflowTotalsRepository.saveAll(cashOutflowTotals);
        log.debug("after cashOutflowTotals:"+cashOutflowTotals);
        for(CashOutflowTotals cashOutflowTotals1: cashOutflowTotals){
            count = 0;
            for (CashbudgetWrapperDTO cashbudgetWrapperDTO : cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
                //log.debug(count+"::cashbudgetWrapperDTO::"+cashbudgetWrapperDTO);
                if (count > 1) {
                    for (DayDTO dayDTO1 : cashbudgetWrapperDTO.getDayList()) {
                        if (cashOutflowTotals1.getCashOutflowTotalDate().equals(dayDTO1.getEventDate())) {
                            //log.debug(cashOutflowTotals1.getId()+"::setting total id for "+cashOutflowTotals1.getCashOutflowTotalDate());
                            dayDTO1.setTotalId(cashOutflowTotals1.getId());
                            break;
                        }
                    }
                }
                count++;
            }
        }
    }

    private void saveCashOutflowRange(List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS, CashOutflowData cashOutflowData) {
        //create cashinflowrange list and save it
        List<CashOutflowRange> cashOutflowRanges = new ArrayList<CashOutflowRange>();
        CashOutflowPayablesDTO cashOutflowPayablesDTO = null;
        for(CashOutflowPayablesDTO cashOutflowPayablesDTO1: cashOutflowPayablesDTOS){
            if("Y".equalsIgnoreCase(cashOutflowPayablesDTO1.getColorCode())){
                //find the last cash inflow receivables  in yellow color code
                cashOutflowPayablesDTO = cashOutflowPayablesDTO1;
            }
        }
        if(cashOutflowPayablesDTO != null && (!(CustomUtil.getDateInFormat(cashOutflowData.getExpenseDate(),"YYYYMM").equalsIgnoreCase
            (CustomUtil.getDateInFormat(cashOutflowPayablesDTO.getPayableDate(),"YYYYMM"))))){
            List<LocalDate> rangeEntries = getRangeEntries(cashOutflowData.getExpenseDate(), cashOutflowPayablesDTO.getPayableDate());
            for(LocalDate rangeEntry: rangeEntries) {
                CashOutflowRange cashOutflowRange = new CashOutflowRange();
                cashOutflowRange.setCod(cashOutflowData);
                cashOutflowRange.setExpenseDate(cashOutflowData.getExpenseDate());
                cashOutflowRange.setRangeDate(rangeEntry);
                cashOutflowRanges.add(cashOutflowRange);
            }
            customCashOutflowRangeRepository.saveAll(cashOutflowRanges);
        }
    }

    private void saveCashOutflowPaymentAmts(CashOutflowPayablesDTO cashOutflowPayablesDTO, CashOutflowPayables cashOutflowPayables,CashOutflowData cashOutflowData) {
        if("Y".equalsIgnoreCase(cashOutflowPayablesDTO.getColorCode()) && cashOutflowPayablesDTO.getPaidAmts() != null
            && cashOutflowPayablesDTO.getPaidAmts().size() > 0){
            List<CashPayableDetails> cashPayableDetailsList = new ArrayList<CashPayableDetails>();
            for(KeyValueDTO keyValueDTO: cashOutflowPayablesDTO.getPaidAmts()){
                CashPayableDetails cashPayableDetails = new CashPayableDetails();
                cashPayableDetails.setTrackId(keyValueDTO.getTrackId());
                cashPayableDetails.setCod(cashOutflowData);
                cashPayableDetails.setCop(cashOutflowPayables);
                cashPayableDetails.setPaidAmt(new Double(keyValueDTO.getValue()));
                cashPayableDetails.setPaidDate(keyValueDTO.getKey());
                cashPayableDetailsList.add(cashPayableDetails);
            }
            customCashPayableDetailsRepository.saveAll(cashPayableDetailsList);
        }
    }

    public void saveCashOutflowPayables(CashbudgetWrapperDTO cashbudgetWrapperDTO, CashOutflowWrapperDTO cashOutflowWrapperDTO, CashOutflowData cashOutflowData) {
        log.debug("cashbudgetWrapperDTO::"+cashbudgetWrapperDTO);
        log.debug("cashOutflowWrapperDTO::"+cashOutflowWrapperDTO);
        //set cash inflow data id receivables and save it.
        for(DayDTOWrapper dayDTOWrapper: cashbudgetWrapperDTO.getDayDTOWrappers()) {
            if(cashOutflowWrapperDTO.getCashOutflowData().getEditId().equals(dayDTOWrapper.getEditId())) {
                for (DayDTO dayDTO: dayDTOWrapper.getDayDtos()) {
                    if(dayDTO.getCashOutflowPayables() != null){
                        List<CashOutflowPayablesDTO> newList = new ArrayList<CashOutflowPayablesDTO>();
                        for (CashOutflowPayablesDTO cashOutflowPayablesDTO: dayDTO.getCashOutflowPayables()) {
                            cashOutflowPayablesDTO.setCodId(cashOutflowWrapperDTO.getCashOutflowData().getId());
                            cashOutflowPayablesDTO.setExpenseDate(cashOutflowWrapperDTO.getCashOutflowData().getExpenseDate());
                            if(("W".equalsIgnoreCase(cashOutflowPayablesDTO.getColorCode()) ||
                                ("R".equalsIgnoreCase(cashOutflowPayablesDTO.getColorCode()))) &&
                            //if("R".equalsIgnoreCase(cashOutflowPayablesDTO.getColorCode()) &&
                                cashOutflowPayablesDTO.getPayablePercent() == null) {
                                log.debug("************not adding entry.."+cashOutflowPayablesDTO);
                            }
                            else{
                                log.debug("payable date::"+cashOutflowPayablesDTO.getPayableDate());
                                log.debug("expenseDate::"+cashOutflowPayablesDTO.getExpenseDate());
                                newList.add(cashOutflowPayablesDTO);
                                log.debug("************adding entry.."+cashOutflowPayablesDTO);
                                CashOutflowPayables cashOutflowPayables = cashOutflowPayablesMapper.toEntity(cashOutflowPayablesDTO);
                                cashOutflowPayables.setId(null);
                                customCashOutflowPayablesRepository.save(cashOutflowPayables);
                                cashOutflowPayablesDTO.setId(cashOutflowPayables.getId());
                                saveCashOutflowPaymentAmts(cashOutflowPayablesDTO, cashOutflowPayables, cashOutflowData);
                            }
                        }
                        log.debug("receivables entry....."+newList);
                        // List<CashOutflowPayables> cashOutflowPayables = cashOutflowPayablesMapper.toEntity(newList);
                        // customCashOutflowPayablesRepository.saveAll(cashOutflowPayables);
                        saveCashOutflowRange(newList, cashOutflowData);
                        dayDTO.setCashOutflowPayables(newList);
                    }
                }
            }
        }
        log.debug("out of range cashOutflowPayables entry....."+cashOutflowWrapperDTO.getOutOfRangePbls());
        //save out of range payables for that sale
        /*
        if(cashOutflowWrapperDTO.getOutOfRangePbls() != null){
            for (CashOutflowPayablesDTO cashOutflowPayablesDTO: cashOutflowWrapperDTO.getOutOfRangePbls()) {
                cashOutflowPayablesDTO.setCodId(cashOutflowWrapperDTO.getCashOutflowData().getId());
                cashOutflowPayablesDTO.setExpenseDate(cashOutflowWrapperDTO.getCashOutflowData().getExpenseDate());
            }
            List<CashOutflowPayables> cashOutflowPayables = cashOutflowPayablesMapper.toEntity(cashOutflowWrapperDTO.getOutOfRangePbls());
            customCashOutflowPayablesRepository.saveAll(cashOutflowPayables);
            log.debug("out of range cashOutflowPayables....."+cashOutflowPayables);
            cashOutflowWrapperDTO.setOutOfRangePbls(cashOutflowPayablesMapper.toDto(cashOutflowPayables));
        }
        */
        for (CashOutflowWrapperDTO cashOutflowWrapperDTO1 : cashbudgetWrapperDTO.getCashOutflowWrappers()) {
            if (cashOutflowWrapperDTO1.getOutOfRangePbls() != null &&
                cashOutflowWrapperDTO1.getCashOutflowData().getEditId().equals(cashOutflowWrapperDTO.getCashOutflowData().getEditId())) {
                for (CashOutflowPayablesDTO cashOutflowPayablesDTO : cashOutflowWrapperDTO1.getOutOfRangePbls()) {
                    cashOutflowPayablesDTO.setCodId(cashOutflowWrapperDTO1.getCashOutflowData().getId());
                    cashOutflowPayablesDTO.setExpenseDate(cashOutflowWrapperDTO1.getCashOutflowData().getExpenseDate());
                }
                List<CashOutflowPayables> cashOutflowPayables = cashOutflowPayablesMapper.toEntity(cashOutflowWrapperDTO1.getOutOfRangePbls());
                customCashOutflowPayablesRepository.saveAll(cashOutflowPayables);
                log.debug("out of range cashOutflowPayables....." + cashOutflowPayables);
                cashOutflowWrapperDTO1.setOutOfRangePbls(cashOutflowPayablesMapper.toDto(cashOutflowPayables));
            }
        }
    }

    public void deleteCashOutflowSub(CashOutflowData cashOutflowData) {
        customCashOutflowRangeRepository.deleteByCod(cashOutflowData);
        //first delete the receivables -
        customCashOutflowPayablesRepository.deleteByCod(cashOutflowData);
        // delete and insert
        customCashPayableDetailsRepository.deleteByCod(cashOutflowData);
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void deleteCashInflowData(Long id) {
        CashInflowData cashInflowData = new CashInflowData();
        cashInflowData.setId(id);
        customCashReceivableDetailsRepository.deleteByCid(cashInflowData);
        customCashInflowReceivablesRepository.deleteByCid(cashInflowData);
        customCashInflowRangeRepository.deleteByCid(cashInflowData);
        customCashInflowDataRepository.delete(cashInflowData);
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void deleteCashOutflowData(Long id) {
        CashOutflowData cashOutflowData = new CashOutflowData();
        cashOutflowData.setId(id);
        customCashPayableDetailsRepository.deleteByCod(cashOutflowData);
        customCashOutflowPayablesRepository.deleteByCod(cashOutflowData);
        customCashOutflowRangeRepository.deleteByCod(cashOutflowData);
        customCashOutflowDataRepository.delete(cashOutflowData);
    }

    public List<CashInflowReceivablesDTO> getCashInflowReceivablesDTOs(CashInflowReceivablesDTO cashInflowReceivablesDTO) {
        List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = new ArrayList<CashInflowReceivablesDTO>();
        cashInflowReceivablesDTO.setColorCode("G");
        cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO);
        CashInflowReceivablesDTO cashInflowReceivablesDTO1 = new CashInflowReceivablesDTO();
        cashInflowReceivablesDTO1.setCreditPeriod(cashInflowReceivablesDTO.getCreditPeriod());
        cashInflowReceivablesDTO1.setReceivablePercent(cashInflowReceivablesDTO.getReceivablePercent());
        cashInflowReceivablesDTO1.setReceivableAmt(cashInflowReceivablesDTO.getReceivableAmt());
        cashInflowReceivablesDTO1.setSalesDate(cashInflowReceivablesDTO.getSalesDate());
        cashInflowReceivablesDTO1.setReceivableDate(cashInflowReceivablesDTO.getSalesDate().plusDays((new Long(cashInflowReceivablesDTO.getCreditPeriod())).longValue()));
        cashInflowReceivablesDTO1.setColorCode("Y");
        cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO1);
        return cashInflowReceivablesDTOS;
    }
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void uploadCashInflows(List<CashInflowWrapperDTO> cashInflowWrapperDTOS) {
        //save cashinflow data
        //save receivables data - inside loop for each cash inflow data
        String editId = null;
        for (CashInflowWrapperDTO cashInflowWrapperDTO : cashInflowWrapperDTOS) {
            log.debug("salesDate::"+cashInflowWrapperDTO.getCashInflowData().getSalesDate());
            CashInflowData cashInflowData = cashInflowDataMapper.toEntity(cashInflowWrapperDTO.getCashInflowData());
            cashInflowData.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
            customCashInflowDataRepository.save(cashInflowData);
            //before over writing with the saved object, retain the ids so that we can copy over again
            editId = cashInflowWrapperDTO.getCashInflowData().getEditId();
            cashInflowWrapperDTO.setCashInflowData(cashInflowDataMapper.toDto(cashInflowData));
            cashInflowWrapperDTO.getCashInflowData().setEditId(editId);
            uploadCashInflowReceivables(cashInflowWrapperDTO, cashInflowData);
        }
    }

    private void uploadCashInflowReceivables(CashInflowWrapperDTO cashInflowWrapperDTO, CashInflowData cashInflowData) {
        log.debug("cashInflowWrapperDTO.getCashInflowRbls()::"+cashInflowWrapperDTO.getCashInflowRbls());
        List<CashInflowReceivables> cashInflowReceivablesList = cashInflowReceivablesMapper.toEntity(cashInflowWrapperDTO.getCashInflowRbls());
        for(CashInflowReceivables cashInflowReceivables1: cashInflowReceivablesList){
            cashInflowReceivables1.setCid(cashInflowData);
        }
        customCashInflowReceivablesRepository.saveAll(cashInflowReceivablesList);
        saveCashInflowRange(cashInflowWrapperDTO.getCashInflowRbls(),cashInflowData);
    }

    public List<CashOutflowPayablesDTO> getCashOutflowPayablesDTOs(CashOutflowPayablesDTO cashOutflowPayablesDTO) {
        List<CashOutflowPayablesDTO> cashOutflowPayablesDTOS = new ArrayList<CashOutflowPayablesDTO>();
        cashOutflowPayablesDTO.setColorCode("B");
        cashOutflowPayablesDTOS.add(cashOutflowPayablesDTO);
        CashOutflowPayablesDTO cashOutflowPayablesDTO1 = new CashOutflowPayablesDTO();
        cashOutflowPayablesDTO1.setCreditPeriod(cashOutflowPayablesDTO.getCreditPeriod());
        cashOutflowPayablesDTO1.setPayablePercent(cashOutflowPayablesDTO.getPayablePercent());
        cashOutflowPayablesDTO1.setPayableAmt(cashOutflowPayablesDTO.getPayableAmt());
        cashOutflowPayablesDTO1.setExpenseDate(cashOutflowPayablesDTO.getExpenseDate());
        cashOutflowPayablesDTO1.setPayableDate(cashOutflowPayablesDTO.getExpenseDate().plusDays((new Long(cashOutflowPayablesDTO.getCreditPeriod())).longValue()));
        cashOutflowPayablesDTO1.setColorCode("Y");
        cashOutflowPayablesDTOS.add(cashOutflowPayablesDTO1);
        return cashOutflowPayablesDTOS;
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void uploadCashOutflows(List<CashOutflowWrapperDTO> cashOutflowWrapperDTOS) {
        //save cashOutflow data
        //save receivables data - inside loop for each cash Outflow data
        String editId = null;
        for (CashOutflowWrapperDTO cashOutflowWrapperDTO : cashOutflowWrapperDTOS) {
            log.debug("expense date::"+cashOutflowWrapperDTO.getCashOutflowData().getExpenseDate());
            CashOutflowData cashOutflowData = cashOutflowDataMapper.toEntity(cashOutflowWrapperDTO.getCashOutflowData());
            cashOutflowData.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
            customCashOutflowDataRepository.save(cashOutflowData);
            //before over writing with the saved object, retain the ids so that we can copy over again
            editId = cashOutflowWrapperDTO.getCashOutflowData().getEditId();
            cashOutflowWrapperDTO.setCashOutflowData(cashOutflowDataMapper.toDto(cashOutflowData));
            cashOutflowWrapperDTO.getCashOutflowData().setEditId(editId);
            uploadCashOutflowPayables(cashOutflowWrapperDTO, cashOutflowData);
        }
    }

    private void uploadCashOutflowPayables(CashOutflowWrapperDTO cashOutflowWrapperDTO, CashOutflowData cashOutflowData) {
        log.debug("cashOutflowWrapperDTO.getCashOutflowPbls()::"+cashOutflowWrapperDTO.getCashOutflowPbls());
        List<CashOutflowPayables> cashOutflowPayablesList = cashOutflowPayablesMapper.toEntity(cashOutflowWrapperDTO.getCashOutflowPbls());
        for(CashOutflowPayables cashOutflowReceivables1: cashOutflowPayablesList){
            cashOutflowReceivables1.setCod(cashOutflowData);
        }
        customCashOutflowPayablesRepository.saveAll(cashOutflowPayablesList);
        saveCashOutflowRange(cashOutflowWrapperDTO.getCashOutflowPbls(),cashOutflowData);
    }
    /**
     * Get totals for that specific period
     * Get all receivables for that period
     *
     */
    /**
     * if month wise performance is not that great - we can change to the below one.
     * get the earliest changed date or the earliest sales date from the list of cashinflows
     * then get all receivables from that date to the current date or to the last date from the receivables which ever is later
     * the get all totals from start date to the end date and update all totals by recalculating each and save to the DB together
     * @param cashInflowMasterWrapperDTO
     */
    public void recalculateARTotals(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        //calculate total carry forward ARs
        Double totalCarryForwardAR = 0.0;
        for (CashInflowWrapperDTO cashInflowWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getCashInflowWrappers()){
            if (cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR() != null ) {
                totalCarryForwardAR = totalCarryForwardAR + cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR();
            }
        }
        for (CashInflowWrapperDTO cashInflowWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getCashInflowWrappers()){
            if (cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR() != null ) {
                totalCarryForwardAR = totalCarryForwardAR + cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR();
            }
        }
        // every calculation should be in one month loop
        //calculate net missed sum
        Double netMissedSum = 0.0;
        Double delayedAmt = 0.0;
        String temp = "0";
        DayDTO prevDt = null;
        String prevDtSum = "0";
        // one loop for getting from regular cashinflow and another loop for getting from recurring
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList()) {
            delayedAmt = 0.0;
            dt.amtValue = "0";
            netMissedSum = 0.0;
            temp = this.getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList(), dt);
            if (temp != null) {
                netMissedSum = new Double(temp);
            }
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && cir1.getColorCode().equalsIgnoreCase("R")) {
                                delayedAmt = delayedAmt + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            if (delayedAmt > 0.0) {
                netMissedSum = netMissedSum - delayedAmt;
            }
            dt.amtValue = netMissedSum + "";
        }
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList()) {
            prevDtSum = "0" ;
            netMissedSum = 0.0;
            delayedAmt = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && cir1.getColorCode().equalsIgnoreCase("R")) {
                                delayedAmt = delayedAmt + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            if (prevDt != null) {
                prevDtSum = this.getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList(), prevDt);
            }
            netMissedSum = new Double(prevDtSum) + new Double(this.getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList(), dt)) - delayedAmt;
            dt.amtValue = netMissedSum + "";
            prevDt = dt;
        }

        //net missed day collection
        Double netMissedDay = 0.0;
        Double targetAmt = 0.0;
        Double paidAmt = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList()) {
            dt.amtValue = "0";
            netMissedDay = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        targetAmt = 0.0;
                        paidAmt = 0.0;
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                targetAmt = cir1.getTargetAmt();
                            } else if (cir1.getPaidAmt() != null) {
                                paidAmt = cir1.getPaidAmt();
                            }
                        }
                        if (targetAmt > 0.0) {
                            netMissedDay = netMissedDay + (targetAmt - paidAmt);
                        }
                        break;
                    }
                }
            }
            dt.amtValue = netMissedDay + "";
        }
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList()) {
            netMissedDay = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        targetAmt = 0.0;
                        paidAmt = 0.0;
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                targetAmt = cir1.getTargetAmt();
                            } else if (cir1.getPaidAmt() != null) {
                                paidAmt = cir1.getPaidAmt();
                            }
                        }
                        if (targetAmt > 0) {
                            netMissedDay = netMissedDay + (targetAmt - paidAmt);
                        }
                        break;
                    }
                }
            }
            netMissedDay = netMissedDay + new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList(), dt));
            dt.amtValue = netMissedDay + "";
        }

        // Receivables that are due from sales
        Double dayDueARs = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(6).getDayList()) {
            dt.amtValue = "0";
            dayDueARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                dayDueARs = dayDueARs + cir1.getTargetAmt();
                            }
                        }
                        break;
                    }
                }
            }
            dt.amtValue = dayDueARs + "";
        }
        // from recurring
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(6).getDayList()) {
            dayDueARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                dayDueARs = dayDueARs + cir1.getTargetAmt();
                            }
                        }
                        break;
                    }
                }
            }
            dayDueARs = dayDueARs + new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(6).getDayList(), dt));
            dt.amtValue = dayDueARs + "";
        }

        // Total CashInflows
        Double totalCif = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(5).getDayList()) {
            dt.amtValue = "0";
            totalCif = new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(2).getDayList(), dt)) +
                new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList(), dt));
            dt.amtValue = totalCif + "";
        }

        // Cash collection for Accounts Receivable
        Double cashARs = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList()) {
            dt.amtValue = "0";
            cashARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && (cir1.getColorCode().equalsIgnoreCase("W") || cir1.getColorCode().equalsIgnoreCase("R"))) {
                                cashARs = cashARs + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            dt.amtValue = cashARs + "";
        }
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList()) {
            cashARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && (cir1.getColorCode().equalsIgnoreCase("W") || cir1.getColorCode().equalsIgnoreCase("R"))) {
                                cashARs = cashARs + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            cashARs = cashARs + new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList(), dt));
            dt.amtValue = cashARs + "";
        }

        // Total Accounts Receivable
        // start with the prev total acct receivables
        Double totalARs = totalCarryForwardAR;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(4).getDayList()) {
            dt.amtValue = "0";
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getReceivableAmt() != null && cir1.getColorCode().equalsIgnoreCase("G")) {
                                totalARs = totalARs + cir1.getReceivableAmt();
                            }
                        }
                        break;
                    }
                }
            }
            totalARs = totalARs - new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList(), dt));
            dt.amtValue = totalARs + "";
        }
        // <!--cash collection for sales-->
        // get cash collection for sales .. salescashamt for each date for all products and stored in the array of cash inflow totals
        Double totalCashCollection = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(2).getDayList()) {
            dt.amtValue = "0";
            totalCashCollection = 0.0;
            for (CashInflowWrapperDTO ciw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getCashInflowWrappers()){
                if (ciw.getCashInflowData().getSalesDate() != null && checkDates(ciw.getCashInflowData().getSalesDate(), dt.getEventDate())) {
                    totalCashCollection = totalCashCollection + ciw.getCashInflowData().getSalesCashAmount();
                    continue;
                }
            }
            dt.amtValue = totalCashCollection + "";
        }
    }

    public void recalculateARTotals_old(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        //calculate total carry forward ARs
        Double totalCarryForwardAR = 0.0;
        for (CashInflowWrapperDTO cashInflowWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getCashInflowWrappers()){
            if (cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR() != null ) {
                totalCarryForwardAR = totalCarryForwardAR + cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR();
            }
        }
        for (CashInflowWrapperDTO cashInflowWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getCashInflowWrappers()){
            if (cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR() != null ) {
                totalCarryForwardAR = totalCarryForwardAR + cashInflowWrapperDTO.getCashInflowData().getCarryForwardAR();
            }
        }

        //calculate net missed sum
        Double netMissedSum = 0.0;
        Double delayedAmt = 0.0;
        String temp = "0";
        DayDTO prevDt = null;
        String prevDtSum = "0";
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList()) {
            delayedAmt = 0.0;
            dt.amtValue = "0";
            netMissedSum = 0.0;
            temp = this.getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList(), dt);
            if (temp != null) {
                netMissedSum = new Double(temp);
            }
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && cir1.getColorCode().equalsIgnoreCase("R")) {
                                delayedAmt = delayedAmt + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            if (delayedAmt > 0.0) {
                netMissedSum = netMissedSum - delayedAmt;
            }
            dt.amtValue = netMissedSum + "";
        }
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList()) {
            prevDtSum = "0" ;
            netMissedSum = 0.0;
            delayedAmt = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && cir1.getColorCode().equalsIgnoreCase("R")) {
                                delayedAmt = delayedAmt + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            if (prevDt != null) {
                prevDtSum = this.getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList(), prevDt);
            }
            netMissedSum = new Double(prevDtSum) + new Double(this.getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(8).getDayList(), dt)) - delayedAmt;
            dt.amtValue = netMissedSum + "";
            prevDt = dt;
        }

        //net missed day collection
        Double netMissedDay = 0.0;
        Double targetAmt = 0.0;
        Double paidAmt = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList()) {
            dt.amtValue = "0";
            netMissedDay = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        targetAmt = 0.0;
                        paidAmt = 0.0;
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                targetAmt = cir1.getTargetAmt();
                            } else if (cir1.getPaidAmt() != null) {
                                paidAmt = cir1.getPaidAmt();
                            }
                        }
                        if (targetAmt > 0.0) {
                            netMissedDay = netMissedDay + (targetAmt - paidAmt);
                        }
                        break;
                    }
                }
            }
            dt.amtValue = netMissedDay + "";
        }
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList()) {
            netMissedDay = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        targetAmt = 0.0;
                        paidAmt = 0.0;
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                targetAmt = cir1.getTargetAmt();
                            } else if (cir1.getPaidAmt() != null) {
                                paidAmt = cir1.getPaidAmt();
                            }
                        }
                        if (targetAmt > 0) {
                            netMissedDay = netMissedDay + (targetAmt - paidAmt);
                        }
                        break;
                    }
                }
            }
            netMissedDay = netMissedDay + new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(7).getDayList(), dt));
            dt.amtValue = netMissedDay + "";
        }

        // Receivables that are due from sales
        Double dayDueARs = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(6).getDayList()) {
            dt.amtValue = "0";
            dayDueARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                dayDueARs = dayDueARs + cir1.getTargetAmt();
                            }
                        }
                        break;
                    }
                }
            }
            dt.amtValue = dayDueARs + "";
        }
        // from recurring
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(6).getDayList()) {
            dayDueARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getTargetAmt() != null && cir1.getColorCode().equalsIgnoreCase("Y")) {
                                dayDueARs = dayDueARs + cir1.getTargetAmt();
                            }
                        }
                        break;
                    }
                }
            }
            dayDueARs = dayDueARs + new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(6).getDayList(), dt));
            dt.amtValue = dayDueARs + "";
        }

        // Total CashInflows
        Double totalCif = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(5).getDayList()) {
            dt.amtValue = "0";
            totalCif = new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(2).getDayList(), dt)) +
                new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList(), dt));
            dt.amtValue = totalCif + "";
        }

        // Cash collection for Accounts Receivable
        Double cashARs = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList()) {
            dt.amtValue = "0";
            cashARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && (cir1.getColorCode().equalsIgnoreCase("W") || cir1.getColorCode().equalsIgnoreCase("R"))) {
                                cashARs = cashARs + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            dt.amtValue = cashARs + "";
        }
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList()) {
            cashARs = 0.0;
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(1).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getPaidAmt() != null && (cir1.getColorCode().equalsIgnoreCase("W") || cir1.getColorCode().equalsIgnoreCase("R"))) {
                                cashARs = cashARs + cir1.getPaidAmt();
                            }
                        }
                        break;
                    }
                }
            }
            cashARs = cashARs + new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList(), dt));
            dt.amtValue = cashARs + "";
        }

        // Total Accounts Receivable
        // start with the prev total acct receivables
        Double totalARs = totalCarryForwardAR;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(4).getDayList()) {
            dt.amtValue = "0";
            for (DayDTOWrapper ddw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getDayDTOWrappers()) {
                for (DayDTO dayDto: ddw.getDayDtos()) {
                    if (dayDto.getCashInflowReceivables() != null && checkDates(dayDto.getEventDate(), dt.getEventDate())) {
                        for (CashInflowReceivablesDTO cir1: dayDto.getCashInflowReceivables()) {
                            if (cir1.getReceivableAmt() != null && cir1.getColorCode().equalsIgnoreCase("G")) {
                                totalARs = totalARs + cir1.getReceivableAmt();
                            }
                        }
                        break;
                    }
                }
            }
            totalARs = totalARs - new Double(getAmount(cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(3).getDayList(), dt));
            dt.amtValue = totalARs + "";
        }

        // get cash collection for sales .. salescashamt for each date for all products and stored in the array of cash inflow totals
        Double totalCashCollection = 0.0;
        for (DayDTO dt: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(2).getDayList()) {
            dt.amtValue = "0";
            totalCashCollection = 0.0;
            for (CashInflowWrapperDTO ciw: cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0).getCashInflowWrappers()){
                if (ciw.getCashInflowData().getSalesDate() != null && checkDates(ciw.getCashInflowData().getSalesDate(), dt.getEventDate())) {
                    totalCashCollection = totalCashCollection + ciw.getCashInflowData().getSalesCashAmount();
                    continue;
                }
            }
            dt.amtValue = totalCashCollection + "";
        }
    }

    public void updateARTotals(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO){
        //get all sales date and receivable dates
        //and find out the earliest date among that and start updating the totals from that by getting cashinflow data including the existing totals
        //and overwrite the totals with the new total calculation and save the totals alone. (yet to write the method for that)
        List<LocalDate> salesDates = new ArrayList<LocalDate>();
        //if its single record and then check for changed flag in any of the date fields, the add that date to the list
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if (cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashbudgetWrapperDTO.getCashInflowWrappers()) {
                    if (cashInflowWrapperDTO.getCashInflowData().getSalesDate() != null) {
                        salesDates.add(cashInflowWrapperDTO.getCashInflowData().getSalesDate());
                    }
                    //for each cashinflow add the receivable dates
                    for (CashInflowReceivablesDTO cashInflowReceivablesDTO : cashInflowWrapperDTO.getCashInflowRbls()) {
                        if (cashInflowReceivablesDTO.getReceivedDate() != null && (cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("W") || cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("R"))) {
                            salesDates.add(cashInflowReceivablesDTO.getReceivedDate());
                        }
                    }
                }
            }
        }
        if(salesDates != null && salesDates.size() > 1) {
            Collections.sort(salesDates);
        }
        for (LocalDate localDate: salesDates){
            log.debug("date ...."+localDate);
        }
        LocalDate startDate = salesDates.get(0);
        LocalDate endDate = salesDates.get(salesDates.size() - 1);
        cashInflowMasterWrapperDTO = null;
        log.debug("StartDate:"+startDate);
        log.debug("endDate:"+endDate);
        while (startDate.compareTo(endDate) <= 0) {
            log.debug("getting for cashinflowdata for "+startDate);
            // cashInflowMasterWrapperDTO = getCashInflowData(startDate, "current");
            List<DayDTO> dayDTOS = customUserService.getDayList(startDate);
            LocalDate stDate1 = dayDTOS.get(0).getEventDate();
            LocalDate endDate1 = dayDTOS.get(dayDTOS.size() - 1).getEventDate();
            List<CashInflowReceivablesDTO> cashInflowReceivablesDTOList = getCashInflowReceivablesForTheGivenPeriod(stDate1,endDate1);
            List<CashInflowTotalsDTO> cashInflowTotalsDTOList = getCashInflowTotalsForTheGivenPeriod(stDate1,endDate1);
            if( cashInflowMasterWrapperDTO != null) {
                recalculateARTotals(cashInflowMasterWrapperDTO);
            }
            log.debug("going to save totals ...");
            saveOrUpdateARTotals(cashInflowMasterWrapperDTO);
            log.debug("save totals done ...");
            startDate = startDate.plusMonths(1);
            log.debug("StartDate:"+startDate);
        }
    }

    private List<CashInflowTotalsDTO> getCashInflowTotalsForTheGivenPeriod(LocalDate stDate1, LocalDate endDate1) {
        return new ArrayList<>();
    }

    private List<CashInflowReceivablesDTO> getCashInflowReceivablesForTheGivenPeriod(LocalDate stDate1, LocalDate endDate1) {
        return new ArrayList<>();
    }

    private String getAmount(List<DayDTO> dayList, DayDTO targetDt) {
        for (DayDTO dt: dayList) {
            if (CustomUtil.getDateInFormat(targetDt.getEventDate(),"YYYY-MM-DD").equalsIgnoreCase(CustomUtil.getDateInFormat(dt.getEventDate(),"YYYY-MM-DD"))) {
                return dt.amtValue;
            }
        }
        return "0.0";
    }

    private boolean checkDates(LocalDate dt1, LocalDate dt2) {
        if (CustomUtil.getDateInFormat(dt1,"YYYY-MM-DD").equalsIgnoreCase(CustomUtil.getDateInFormat(dt2,"YYYY-MM-DD"))) {
            return true;
        }
        else {
            return false;
        }

    }

    public void saveOrUpdateCashInflow(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO){
        //save cashinflow or recur entry and get the id
        //save corresponding cash inflow receivables with this id and payments
        //save cashinflow range according to the sales date and receivable date
        //then call the total update method to update the totals
        //save cashinflow master
        CashInflowMaster cashInflowMaster = cashInflowMasterMapper.toEntity(cashInflowMasterWrapperDTO.getCashInflowMaster());
        cashInflowMaster.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        customCashInflowMasterRepository.save(cashInflowMaster);
        cashInflowMasterWrapperDTO.setCashInflowMaster(cashInflowMasterMapper.toDto(cashInflowMaster));
        //save recur data if there is no recur id in the records
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashbudgetWrapperDTO.getCashInflowWrappers()) {
                    CashInflowDataDTO cashInflowDataDTO = cashInflowWrapperDTO.getCashInflowData();
                    if (cashInflowDataDTO.getInflowType() != null && "R".equalsIgnoreCase(cashInflowDataDTO.getInflowType())) {
                        RecurringCBEntries recurringCBEntries = null;
                        log.debug("cashInflowDataDTO::" + cashInflowDataDTO);
                        if (cashInflowDataDTO.getRecurId() == null) {
                            recurringCBEntries = getRecurringCEEntries(cashInflowDataDTO, null, "AR");
                            recurringCBEntries.setId(cashInflowDataDTO.getRecurId());
                            customRecurringCBEntriesRepository.save(recurringCBEntries);
                            cashInflowDataDTO.setRecurId(recurringCBEntries.getId());
                        }
                    }
                }
            }
        }
        //save cashinflow data
        //save receivables data - inside loop for each cash inflow data
        String editId = null;
        for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashbudgetWrapperDTO.getCashInflowWrappers()) {
                    log.debug("salesDate::"+cashInflowWrapperDTO.getCashInflowData().getSalesDate());
                    CashInflowData cashInflowData = cashInflowDataMapper.toEntity(cashInflowWrapperDTO.getCashInflowData());
                    cashInflowData.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
                    customCashInflowDataRepository.save(cashInflowData);
                    //before over writing with the saved object, retain the ids so that we can copy over again
                    editId = cashInflowWrapperDTO.getCashInflowData().getEditId();
                    cashInflowWrapperDTO.setCashInflowData(cashInflowDataMapper.toDto(cashInflowData));
                    cashInflowWrapperDTO.getCashInflowData().setEditId(editId);
                    saveCashInflowReceivables(cashbudgetWrapperDTO, cashInflowWrapperDTO, cashInflowData);
                }
            }
        }
        //total should be called outside this loop, if its set of records, then total calculation should start from the earliest date
        //and it should happen for month by month till the current month or the last receivable date
        //saveOrUpdateARTotals(cashInflowMasterWrapperDTO);
        //for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            //if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                updateARTotals(cashInflowMasterWrapperDTO);
            //}
        //}
    }

    public void saveCashInflowReceivables(CashbudgetWrapperDTO cashbudgetWrapperDTO, CashInflowWrapperDTO cashInflowWrapperDTO,
                                                     CashInflowData cashInflowData) {
        log.debug("cashInflowWrapperDTO::"+cashInflowWrapperDTO);
        log.debug("saved cashInflowData::"+cashInflowData);
        List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = cashInflowWrapperDTO.getCashInflowRbls();
        log.debug("receivables entry....."+cashInflowReceivablesDTOS);
        //get existing records and see if anything got deleted from front end - then delete from back end too
        checkReceivablesAndDelete(cashInflowReceivablesDTOS, cashInflowData);
        if(cashInflowReceivablesDTOS.size() > 0) {
            //calculate the received amount for that yellow record and save or update receivables
            calculateReceivedAmount(cashInflowReceivablesDTOS, cashInflowData);
            log.debug("after calculate receivables entry....."+cashInflowReceivablesDTOS);
            List<CashInflowReceivables> cashInflowReceivablesList = customCashInflowReceivablesRepository.saveAll(cashInflowReceivablesMapper.toEntity(cashInflowReceivablesDTOS));
            cashInflowReceivablesDTOS = cashInflowReceivablesMapper.toDto(cashInflowReceivablesList);
            // save paidAmts if its yellow and range here
            //saveCashInflowReceivedAmts(cashInflowReceivablesDTOS, cashInflowData);
            if (!("R".equalsIgnoreCase(cashInflowData.getInflowType()))) {
                deleteCashInflowSubData(cashInflowData);
                saveCashInflowRange(cashInflowReceivablesDTOS, cashInflowData);
            }
        }
    }

    private void checkReceivablesAndDelete(List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS, CashInflowData cashInflowData) {
        List<CashInflowReceivables> cashInflowReceivables = customCashInflowReceivablesRepository.findByCid(cashInflowData);
        if(cashInflowReceivablesDTOS.size() == 0) {
            //everything got deleted from front end
            customCashInflowReceivablesRepository.deleteByCid(cashInflowData);
        }
        else {
            List<CashInflowReceivables> cids = new ArrayList<CashInflowReceivables>();
            boolean found = false;
            for(CashInflowReceivables cashInflowReceivables1: cashInflowReceivables){
                found = false;
                for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOS){
                    if(cashInflowReceivables1.getId().equals(cashInflowReceivablesDTO.getId())){
                        found = true;
                        break;
                    }
                }
                if(!found){
                    cids.add(cashInflowReceivables1);
                }
            }
            if(cids.size() > 0){
                customCashInflowReceivablesRepository.deleteAll(cids);
            }
        }
    }

    private void calculateReceivedAmount(List<CashInflowReceivablesDTO> cashInflowRbls, CashInflowData cashInflowData) {
        Double totalReceived = 0.0;
        Double singleReceived = 0.0;
        for (CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowRbls) {
            if("Y".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())){
                singleReceived = 0.0;
                totalReceived = 0.0;
                log.debug("1getReceivableAmt........"+cashInflowReceivablesDTO.getReceivableAmt());
                //if(cashInflowData.getSalesCashAmount() != null) {
                //    cashInflowReceivablesDTO.setReceivableAmt((cashInflowData.getSalesAmount() - cashInflowData.getSalesCashAmount()) * cashInflowReceivablesDTO.getReceivablePercent() / 100);
                //}
                //else {
                //    cashInflowReceivablesDTO.setReceivableAmt(cashInflowData.getSalesAmount() * cashInflowReceivablesDTO.getReceivablePercent() / 100);
                //}
                log.debug("2getReceivableAmt........"+cashInflowReceivablesDTO.getReceivableAmt());
                cashInflowReceivablesDTO.setSalesDate(cashInflowData.getSalesDate());
                // already set from the UI
                // cashInflowReceivablesDTO.setReceivableDate(cashInflowData.getSalesDate().plusDays(new Long(cashInflowReceivablesDTO.getCreditPeriod())));
                // for each yellow - there may be one or two or three white or red recevied dates
                for (CashInflowReceivablesDTO cashInflowReceivablesDTO1: cashInflowRbls) {
                    if(cashInflowReceivablesDTO1.getReceivedPercent() != null &&
                        ("R".equalsIgnoreCase(cashInflowReceivablesDTO1.getColorCode()) || "W".equalsIgnoreCase(cashInflowReceivablesDTO1.getColorCode()))
                        && cashInflowReceivablesDTO1.getParId().equals(cashInflowReceivablesDTO.getEditId())){
                        singleReceived = cashInflowData.getSalesAmount() * cashInflowReceivablesDTO1.getReceivedPercent() / 100;
                        log.debug("singleReceived........"+singleReceived);
                        totalReceived = totalReceived + singleReceived;
                        log.debug("totalReceived........"+totalReceived);
                    }
                }
                cashInflowReceivablesDTO.setReceivedAmt(totalReceived);
                cashInflowReceivablesDTO.setPaidAmt(totalReceived);
                log.debug("cashInflowReceivablesDto.."+cashInflowReceivablesDTO);
            } else if ("G".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())){
                if(cashInflowData.getSalesCashAmount() != null) {
                    cashInflowReceivablesDTO.setReceivableAmt(cashInflowData.getSalesAmount() - cashInflowData.getSalesCashAmount());
                }
                else {
                    cashInflowReceivablesDTO.setReceivableAmt(cashInflowData.getSalesAmount());
                }
                cashInflowReceivablesDTO.setSalesAmt(cashInflowData.getSalesAmount());
                //for green both sales date and receivable date are same
                cashInflowReceivablesDTO.setSalesDate(cashInflowData.getSalesDate());
                cashInflowReceivablesDTO.setReceivableDate(cashInflowData.getSalesDate());
            }
            cashInflowReceivablesDTO.setCidId(cashInflowData.getId());
        }
    }
    /**
     * check the paid amount logic to get the correct totals.
     * also check the feasibility to get the totals and the receivables straight
     * and do the total logic accordingly... may be a seperate method.
     */
    /**
     * keep the save as its - if any edit happens - keep track that and save only those records -- if you drag and drop - open
     * the edit screen so that they can enter the date
     * show totals only if the user presess the button - that way if the user changes at the front end
     * we dont have to change the total that time itself. while getting the data we can save the
     * changed files and get the total accordingly.
     */

}
