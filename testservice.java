package com.doodil.proforma.custom.service;

import com.doodil.proforma.custom.CustomConstant;
import com.doodil.proforma.custom.repository.*;
import com.doodil.proforma.custom.service.dto.*;

import com.doodil.proforma.custom.utils.CustomDeepCopy;
import com.doodil.proforma.custom.utils.CustomUtil;
import com.doodil.proforma.domain.*;
import com.doodil.proforma.service.dto.*;
import com.doodil.proforma.service.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    CashInflowTotalsMapper cashInflowTotalsMapper;
    @Autowired
    RecurringCBEntriesMapper recurringCBEntriesMapper;
    @Autowired
    CashBatchRepository cashBatchRepository;
    @Autowired
    CashBudgetBatchMapper cashBudgetBatchMapper;
    @Autowired
    CashPartyRepository cashPartyRepository;
    @Autowired
    CashBudgetPartyMapper cashBudgetPartyMapper;
    @Autowired
    CashInflowReceivablesQueryService cashInflowReceivablesQueryService;


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

    public CashInflowMasterWrapperDTO getCashInflowData(LocalDate localDate, String pageind, String calfreq, Long batchId, Long partyId) {
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
        log.debug("going to get cif for "+stDate+"::endDate::"+endDate);
        List<CashInflowData> cashInflowDataList = customCashInflowDataRepository.findBySalesDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate);
        List<CashInflowDataDTO> cashInflowDataDTOS = cashInflowDataMapper.toDto(cashInflowDataList);
        // add out of range cash inflow datas
        addOutOfRangeCashInflowData(cashInflowDataDTOS, stDate,endDate);
        // add range cashinflows to note that there are some AR expecting in the future months
        addRangeCashInflows(cashInflowDataDTOS, stDate,endDate);
        log.debug("cashinflowdatas before adding recurring.."+cashInflowDataDTOS);
        /*
        recurring will come part of cashinflow and cash receivables records.. while inserting
        recurring we are going to create that many cashinflow and cash receivables
        if(!("delete".equalsIgnoreCase(pageind))){
            addCashInflowRecurringMasterEntries(cashInflowDataDTOS, stDate);
        }
        */
        List<CashBudgetBatch> cashBudgetBatches = cashBatchRepository.findByCompanyInfo(customUserService.getLoggedInCompanyInfo());
        List<CashBudgetParty> cashBudgetParties = cashPartyRepository.findByCompanyInfo(customUserService.getLoggedInCompanyInfo());
        List<CashbudgetWrapperDTO> cashbudgetWrapperDTOs = new ArrayList<CashbudgetWrapperDTO>();
        CashbudgetWrapperDTO cashbudgetWrapperDTO = null;
        List<DayDTOWrapper> dayDTOWrappers1 = new ArrayList<DayDTOWrapper>();
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS1 = new ArrayList<CashInflowWrapperDTO>();
        // first add sales entries
        DayDTOWrapper dayDTOWrapper = null;
        List<CashInflowDataDTO> filteredCifDtos = getFilteredCifDtos(cashInflowDataDTOS,batchId,partyId);
        // add out of range receivables and their corresponding sales entry
        // get receivables for the given date period and get the cash inflow data for that receivable and add to the cashinflow data list
        int editId = 0;
        for(CashInflowDataDTO cashInflowDataDTO: filteredCifDtos) {
            fillBatchName(cashBudgetBatches,cashInflowDataDTO);
            fillPartyName(cashBudgetParties,cashInflowDataDTO);
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
                dayDTOWrapper.setCashInflowData(cashInflowDataDTO);
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
        for(CashInflowDataDTO cashInflowDataDTO: filteredCifDtos) {
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
                //if(!(cashInflowReceivablesDTOList != null && cashInflowReceivablesDTOList.size() > 0)
                //    && cashInflowDataDTO.getCreditPeriod() != null
                //    && cashInflowDataDTO.getCreditSalesPercent() != null){
                //    cashInflowReceivablesDTOList = new ArrayList<CashInflowReceivablesDTO>();
                //    addReceivableForRecurringEntries(cashInflowDataDTO, cashInflowReceivablesDTOList, stDate);
                //}
                log.debug("after recurring...."+cashInflowReceivablesDTOList);
                //addTargetReceibables(cashInflowWrapperDTO, cashInflowReceivablesDTOList);
                //log.debug(cashInflowDataDTO.getId()+"::R::cashReceivablesList:"+cashInflowReceivablesDTOList);
                cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesDTOList);
                cashInflowWrapperDTOS2.add(cashInflowWrapperDTO);
                List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
                addEditId(dayDTOS1,"down"+editId);
                addDataToDayDTO(cashInflowWrapperDTO, dayDTOS1, cashInflowReceivablesDTOList);
                dayDTOWrapper = new DayDTOWrapper();
                dayDTOWrapper.setDayDtos(dayDTOS1);
                dayDTOWrapper.setEditId("down"+editId);
                dayDTOWrapper.setCashInflowData(cashInflowDataDTO);
                dayDTOWrappers2.add(dayDTOWrapper);
                editId++;
            }
        }
        cashbudgetWrapperDTO.setCashInflowWrappers(cashInflowWrapperDTOS2);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers2);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        //go through the objects and move to the batch buckets
        groupToBatchBuckets(cashbudgetWrapperDTOs);
        //get the last day of prev months totals
        log.debug(customUserService.getLoggedInCompanyInfo().getId()+"::stDate::"+stDate+"::batchId::"+batchId+"::partyId::"+partyId);
        CashInflowTotals cashInflowTotals1 = customCashInflowTotalsRepository.findCarryForwardTotals(customUserService.getLoggedInCompanyInfo().getId(),stDate,batchId,partyId);
        CashInflowTotalsDTO prevCashInflowTotals = null;
        // then totals ...
        log.debug("endDate from "+stDate+"::"+endDate);
        List<CashInflowTotals> cashInflowTotals = customCashInflowTotalsRepository.findByTotalDateAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),stDate,endDate,batchId,partyId);
        Double carryForwardAR = 0.0;
        if(cashInflowTotals1 != null && cashInflowTotals1.getId() != null) {
            carryForwardAR = cashInflowTotals1.getTotalAR();
            log.debug("carryforward from "+stDate+"::"+carryForwardAR);
            prevCashInflowTotals = cashInflowTotalsMapper.toDto(cashInflowTotals1);
        }
        /*
        else {
            //get the last record of totals
            cashInflowTotals1 = customCashInflowTotalsRepository.findTotalsByMaxIdAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(),batchId,partyId);
            //previous date should be after the max date - then consider the carry forward, otherwise 0
            if(cashInflowTotals1 != null && prevDate.compareTo(cashInflowTotals1.getCashInflowTotalDate()) >= 0) {
                carryForwardAR = cashInflowTotals1.getTotalAR();
                prevCashInflowTotals = cashInflowTotalsMapper.toDto(cashInflowTotals1);
                log.debug("carry forward from latest.."+carryForwardAR);
            }
        }
        */

        if(cashInflowTotals != null && cashInflowTotals.size() > 0) {
            List<CashInflowTotalsDTO> cashInflowTotalsDTOList = cashInflowTotalsMapper.toDto(cashInflowTotals);
            List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
            LocalDate lastDate = null;
            CashInflowTotalsDTO prevDayDtoTotals = null;
            int count = 0;
            for (DayDTO dayDTO : dayDTOS1) {
                //log.debug("setting totals for "+dayDTO.getEventDate());
                setCashInflowTotalValue(cashInflowTotalsDTOList, dayDTO);
                //get the last total record
                if (dayDTO.getCashInflowTotals() == null) {
                    /*
                    lastDate = cashInflowTotalsDTOList.get(cashInflowTotalsDTOList.size() - 1).getCashInflowTotalDate();
                    if(dayDTO.getEventDate().compareTo(lastDate) >= 0) {
                        log.debug("no totals at the end of the months ...");
                        //these records comes after the existing totals
                        dayDTO.setCashInflowTotals(cashInflowTotalsDTOList.get(cashInflowTotalsDTOList.size() - 1));
                    }
                    */
                    // if its first date and there is no totals .. then check for prevCashInflowTotals
                    if(prevCashInflowTotals != null && count == 0){
                        //set only carry forward and total misses = two vlaues
                        dayDTO.setCashInflowTotals(prevCashInflowTotals);
                    }
                    else if(prevDayDtoTotals != null) {
                        log.debug("no totals start of the months ...");
                        //these records comes before the existing totals
                        dayDTO.setCashInflowTotals(prevDayDtoTotals);
                    }
                    else{
                        log.debug("no totals start of the months and prev totals too");
                        //else shows it as zero
                        dayDTO.setCashInflowTotals(getCashInflowTotalsWithZeros());
                    }
                }
                prevDayDtoTotals = dayDTO.getCashInflowTotals();
                count++;
            }
            cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
            cashbudgetWrapperDTO.setDayList(dayDTOS1);
            cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        }
        else if(prevCashInflowTotals != null) {
            // if there are no records for the current period repeat the last record of the prev period
            List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
            for (DayDTO dayDTO : dayDTOS1) {
                log.debug("setting totals for "+dayDTO.getEventDate());
                //get the last total record
                if (dayDTO.getCashInflowTotals() == null) {
                    dayDTO.setCashInflowTotals(prevCashInflowTotals);
                }
            }
            cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
            cashbudgetWrapperDTO.setDayList(dayDTOS1);
            cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        }
        else {
            log.debug("there is no carryforward as there are no prev entries and current month");
            //if there is nothing ... then add dummy records
            List<DayDTO> dayDTOS1 = customUserService.getDayList(localDate);
            for (DayDTO dayDTO : dayDTOS1) {
                dayDTO.setCashInflowTotals(getCashInflowTotalsWithZeros());
            }
            cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
            cashbudgetWrapperDTO.setDayList(dayDTOS1);
            cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        }
        cashInflowMasterWrapperDTO.setCarryForwardAR(carryForwardAR);
        cashInflowMasterWrapperDTO.setCashbudgetWrappers(cashbudgetWrapperDTOs);
        return cashInflowMasterWrapperDTO;
    }

    /**
     * first element of the list cashbudgetWrapperDTO is normal cashinflows with calendar entry receivables
     * second element of the list cashbudgetWrapperDTO is recurring cashinflows with calendar entry receivables
     * @param cashbudgetWrapperDTOs
     */
    private void groupToBatchBuckets(List<CashbudgetWrapperDTO> cashbudgetWrapperDTOs) {
        //to change - each cashbugetwrapperdto will have list of batchwrapper going forward
        List<CashBatchWrapper> normalCashBatchWrappers = new ArrayList<CashBatchWrapper>();
        List<CashBatchWrapper> recurCashBatchWrappers = new ArrayList<CashBatchWrapper>();
        //firts one is normalcashinflows
        for(CashInflowWrapperDTO cashInflowWrapperDTO: cashbudgetWrapperDTOs.get(0).getCashInflowWrappers()){
            checkAndAddtoCashBatchers(normalCashBatchWrappers, cashInflowWrapperDTO);
        }
        for(DayDTOWrapper dayDTOWrapper: cashbudgetWrapperDTOs.get(0).getDayDTOWrappers()){
            checkDayDtoWrapperAddtoCashBatchers(normalCashBatchWrappers, dayDTOWrapper);
        }
        //second one recurcashinflows
        for(CashInflowWrapperDTO cashInflowWrapperDTO:cashbudgetWrapperDTOs.get(1).getCashInflowWrappers()){
            checkAndAddtoCashBatchers(recurCashBatchWrappers, cashInflowWrapperDTO);
        }
        for(DayDTOWrapper dayDTOWrapper: cashbudgetWrapperDTOs.get(1).getDayDTOWrappers()){
            checkDayDtoWrapperAddtoCashBatchers(recurCashBatchWrappers, dayDTOWrapper);
        }
        cashbudgetWrapperDTOs.get(0).setCashBatchWrappers(normalCashBatchWrappers);
        cashbudgetWrapperDTOs.get(1).setCashBatchWrappers(recurCashBatchWrappers);
    }

    private void checkDayDtoWrapperAddtoCashBatchers(List<CashBatchWrapper> cashBatchWrappers, DayDTOWrapper dayDTOWrapper) {
        for(CashBatchWrapper cashBatchWrapper: cashBatchWrappers){
            log.debug("dayDtoWrapper:cashinflow====================="+dayDTOWrapper.getCashInflowData());
            if(cashBatchWrapper.getId().equals(dayDTOWrapper.getCashInflowData().getCbBatchId())){
                //add the cashinflowwrapper to this batchwrapper
                if(cashBatchWrapper.getDayDTOWrappers() != null){
                    cashBatchWrapper.getDayDTOWrappers().add(dayDTOWrapper);
                }
                else{
                    List<DayDTOWrapper> dayDTOWrappers = new ArrayList<DayDTOWrapper>();
                    dayDTOWrappers.add(dayDTOWrapper);
                    cashBatchWrapper.setDayDTOWrappers(dayDTOWrappers);
                }
                return;
            }
        }
        CashBatchWrapper cashBatchWrapper = new CashBatchWrapper();
        cashBatchWrapper.setId(dayDTOWrapper.getCashInflowData().getCbBatchId());
        cashBatchWrapper.setName(dayDTOWrapper.getCashInflowData().getBatchName());
        List<DayDTOWrapper> dayDTOWrappers = new ArrayList<DayDTOWrapper>();
        dayDTOWrappers.add(dayDTOWrapper);
        cashBatchWrapper.setDayDTOWrappers(dayDTOWrappers);
        cashBatchWrappers.add(cashBatchWrapper);
    }

    private void checkAndAddtoCashBatchers(List<CashBatchWrapper> cashBatchWrappers, CashInflowWrapperDTO cashInflowWrapperDTO) {
        for(CashBatchWrapper cashBatchWrapper: cashBatchWrappers){
            if(cashBatchWrapper.getId().equals(cashInflowWrapperDTO.getCashInflowData().getCbBatchId())){
                //add the cashinflowwrapper to this batchwrapper
                if(cashBatchWrapper.getCashInflowWrappers() != null){
                    cashBatchWrapper.getCashInflowWrappers().add(cashInflowWrapperDTO);
                }
                else{
                    List<CashInflowWrapperDTO> cashInflowWrapperDTOS = new ArrayList<CashInflowWrapperDTO>();
                    cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
                    cashBatchWrapper.setCashInflowWrappers(cashInflowWrapperDTOS);
                }
                return;
            }
        }
        CashBatchWrapper cashBatchWrapper = new CashBatchWrapper();
        cashBatchWrapper.setId(cashInflowWrapperDTO.getCashInflowData().getCbBatchId());
        cashBatchWrapper.setName(cashInflowWrapperDTO.getCashInflowData().getBatchName());
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS = new ArrayList<CashInflowWrapperDTO>();
        cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
        cashBatchWrapper.setCashInflowWrappers(cashInflowWrapperDTOS);
        cashBatchWrappers.add(cashBatchWrapper);
    }

    private List<CashInflowDataDTO> getFilteredCifDtos(List<CashInflowDataDTO> cashInflowDataDTOS, Long batchId, Long partyId) {
        List<CashInflowDataDTO> filteredList = new ArrayList<CashInflowDataDTO>();
        log.debug("batchId:"+batchId+"::partyId::"+partyId);
        if(batchId == 0 && partyId == 0){
            return cashInflowDataDTOS;
        }
        else{
            if(batchId != 0 && partyId != 0){
                for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOS){
                    if(cashInflowDataDTO.getCbBatchId().equals(batchId) && cashInflowDataDTO.getPartyId().equals(partyId)){
                        filteredList.add(cashInflowDataDTO);
                    }
                }
            }
            else if(batchId != 0){
                for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOS){
                    if(cashInflowDataDTO.getCbBatchId().equals(batchId)){
                        filteredList.add(cashInflowDataDTO);
                    }
                }
            }
            else if(partyId != 0){
                for(CashInflowDataDTO cashInflowDataDTO: cashInflowDataDTOS){
                    if(cashInflowDataDTO.getPartyId().equals(partyId)){
                        filteredList.add(cashInflowDataDTO);
                    }
                }
            }
            log.debug("returing filtered list "+filteredList);
            return filteredList;
        }
    }

    private void fillPartyName(List<CashBudgetParty> cashBudgetParties, CashInflowDataDTO cashInflowDataDTO) {
        for(CashBudgetParty cashBudgetParty: cashBudgetParties){
            if(cashInflowDataDTO.getPartyId() != null && cashBudgetParty.getId().equals(cashInflowDataDTO.getPartyId())){
                cashInflowDataDTO.setPartyName(cashBudgetParty.getName());
                return;
            }
        }
    }

    private void fillBatchName(List<CashBudgetBatch> cashBudgetBatches, CashInflowDataDTO cashInflowDataDTO){
        for(CashBudgetBatch cashBudgetBatch: cashBudgetBatches){
            if(cashInflowDataDTO.getCbBatchId() != null && cashInflowDataDTO.getCbBatchId().equals(cashBudgetBatch.getId())){
                cashInflowDataDTO.setBatchName(cashBudgetBatch.getName());
                return;
            }
        }
    }

    private CashInflowTotalsDTO getCashInflowTotalsWithZeros() {
        CashInflowTotalsDTO cashInflowTotalsDTO = new CashInflowTotalsDTO();
        cashInflowTotalsDTO.setCashCollectionSales(0.0);
        cashInflowTotalsDTO.setSumOfDueARs(0.0);
        cashInflowTotalsDTO.setNetMissedCollection(0.0);
        cashInflowTotalsDTO.setDueARs(0.0);
        cashInflowTotalsDTO.setTotalCashInflow(0.0);
        cashInflowTotalsDTO.setTotalAR(0.0);
        cashInflowTotalsDTO.setCashCollectionForAR(0.0);
        return cashInflowTotalsDTO;
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

    private void setCashInflowTotalValue(List<CashInflowTotalsDTO> cashInflowTotals, DayDTO dayDTO) {
        for(CashInflowTotalsDTO cashInflowTotals1: cashInflowTotals) {
            if(cashInflowTotals1.getCashInflowTotalDate().equals(dayDTO.getEventDate())){
                log.debug("setting totals for inside "+cashInflowTotals1.getCashInflowTotalDate());
                dayDTO.setTotalId(cashInflowTotals1.getId());
                //just set the cashtotals object for that date
                //in ui respective fields will be shown
                dayDTO.setCashInflowTotals(cashInflowTotals1);
                /*
                if (count == 2) {
                    // cash collection for sales
                    dayDTO.setAmtValue(cashInflowTotals1.getCashCollectionSales()+"");
                }
                else if(count == 3) {
                    // Cash collection for Accounts Receivable
                    dayDTO.setAmtValue(cashInflowTotals1.getCashCollectionForAR()+"");
                }
                else if(count == 4) {
                    log.debug("totalAR:::::"+cashInflowTotals1.getTotalAR()+"::"+dayDTO.getEventDate());
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
                    log.debug("getSumOfDueARs:::::"+cashInflowTotals1.getSumOfDueARs()+"::"+dayDTO.getEventDate());
                    // Sum of Receivables that are past due dates
                    dayDTO.setAmtValue(cashInflowTotals1.getSumOfDueARs()+"");
                }
                */
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
    }

    private void addDataToDayDTO(CashInflowWrapperDTO cashInflowWrapperDTO, List<DayDTO> dayDTOS, List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS) {
        if(cashInflowReceivablesDTOS == null)
            return;
        boolean found = false;
        for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOS){
            found = false;
            //log.debug("cashinflowreceivablesdto......"+cashInflowReceivablesDTO);
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
    public void deleteCashInflowData(Long id) {
        CashInflowData cashInflowData = customCashInflowDataRepository.getOne(id);
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS = new ArrayList<CashInflowWrapperDTO>();
        CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
        CashInflowDataDTO cashInflowDataDTO = cashInflowDataMapper.toDto(cashInflowData);
        List<CashInflowReceivables> cashInflowReceivables = customCashInflowReceivablesRepository.findByCid(cashInflowData);
        if(cashInflowReceivables != null) {
            cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesMapper.toDto(cashInflowReceivables));

        }
        cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
        cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
        List<LocalDate> salesDate = new ArrayList<LocalDate>();
        collectAllTransactionDates(cashInflowWrapperDTOS, salesDate);
        deleteCifData(cashInflowData);
        Long batchId = cashInflowDataDTO.getCbBatchId();
        Long partyId = cashInflowDataDTO.getPartyId();
        callTotalUpdatesWithAllScenarios(salesDate, batchId, partyId);
    }

    private void callTotalUpdatesWithAllScenarios(List<LocalDate> salesDate, Long batchId, Long partyId) {
        // first for company specific
        updateTotalsWithDates(salesDate, 0L, 0L);
        // batch specific
        updateTotalsWithDates(salesDate, batchId, 0L);
        //party specific
        updateTotalsWithDates(salesDate, 0L, partyId);
        // batch and party specific
        updateTotalsWithDates(salesDate, batchId, partyId);

    }

    public void deleteCifData(CashInflowData cashInflowData) {
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
        cashInflowReceivablesDTO1.setEditId("Y"+CustomUtil.getUniqueString());
        cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO1);
        CashInflowReceivablesDTO cashInflowReceivablesDTO2 = new CashInflowReceivablesDTO();
        //cashInflowReceivablesDTO2.setCreditPeriod(cashInflowReceivablesDTO.getCreditPeriod());
        //cashInflowReceivablesDTO2.setReceivablePercent(cashInflowReceivablesDTO.getReceivablePercent());
        //cashInflowReceivablesDTO2.setReceivableAmt(cashInflowReceivablesDTO.getReceivableAmt());
        //cashInflowReceivablesDTO2.setSalesDate(cashInflowReceivablesDTO.getSalesDate());
        //cashInflowReceivablesDTO2.setReceivableDate(cashInflowReceivablesDTO.getSalesDate().plusDays((new Long(cashInflowReceivablesDTO.getCreditPeriod())).longValue()));
        cashInflowReceivablesDTO2.setColorCode("W");
        cashInflowReceivablesDTO2.setEditId("WR"+CustomUtil.getUniqueString());
        cashInflowReceivablesDTO2.setParId(cashInflowReceivablesDTO1.getEditId());
        cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO2);
        return cashInflowReceivablesDTOS;
    }
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void uploadCashInflows(List<CashInflowWrapperDTO> cashInflowWrapperDTOS) {
        //save cashinflow data
        //save receivables data - inside loop for each cash inflow data
        /*
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
        */
        saveOrUpdateCashBudget(cashInflowWrapperDTOS);
        updateARTotals(cashInflowWrapperDTOS, null);
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
        //saveCashOutflowRange(cashOutflowWrapperDTO.getCashOutflowPbls(),cashOutflowData);
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
     * @param
     */
    public void recalculateARTotals(List<CashInflowTotalsDTO> cashInflowTotalsDTOList,
                                    List<CashInflowReceivablesDTO> cashInflowReceivablesDTOList,
                                    Long batchId,
                                    Long partyId,
                                    CashInflowTotals carryForwardCashInflowTotals) {
        Double cashCollectionsForSales = 0.0;
        Double cashCollectionsAR = 0.0;
        Double totalAR = 0.0;
        Double totalCashInflows = 0.0;
        Double dueReceivables = 0.0;
        Double netMissedCollection = 0.0;
        Double sumOfReceivables = 0.0;
        int count = 0;
        // loop through transactions dates here instead of totalsdto as there
        // wont be continous days records
        if(carryForwardCashInflowTotals != null){
            if(carryForwardCashInflowTotals.getTotalAR() != null) {
                totalAR = carryForwardCashInflowTotals.getTotalAR();
            }
            if(carryForwardCashInflowTotals.getSumOfDueARs() != null) {
                sumOfReceivables = carryForwardCashInflowTotals.getSumOfDueARs();
            }
        }
        for(CashInflowTotalsDTO cashInflowTotalsDTO: cashInflowTotalsDTOList) {
            log.debug("count...."+count+"::date::"+cashInflowTotalsDTO.getCashInflowTotalDate());
            cashCollectionsForSales = 0.0;
            cashCollectionsAR = 0.0;
            totalCashInflows = 0.0;
            dueReceivables = 0.0;
            netMissedCollection = 0.0;
            for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOList){
                //log.debug("totalAR...."+totalAR);
                if((("G".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode()) ||
                    "Y".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())) &&
                        cashInflowReceivablesDTO.getReceivableDate().equals(cashInflowTotalsDTO.getCashInflowTotalDate())) ||
                    (("W".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode()) ||
                        "R".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())) &&
                        cashInflowReceivablesDTO.getReceivedDate().equals(cashInflowTotalsDTO.getCashInflowTotalDate()))) {
                    //log.debug("cashinflowReceivablesDto..totals..."+cashInflowReceivablesDTO);
                    if ("G".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())) {
                        log.debug("Green...."+cashInflowReceivablesDTO+"::totalAR::"+totalAR+"::receamt::"+cashInflowReceivablesDTO.getReceivableAmt());
                        cashCollectionsForSales = cashCollectionsForSales + cashInflowReceivablesDTO.getReceivedAmt();
                        totalAR = totalAR + cashInflowReceivablesDTO.getReceivableAmt();
                        totalCashInflows = totalCashInflows + cashInflowReceivablesDTO.getReceivedAmt();
                    }
                    else if ("Y".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())) {
                        log.debug("Yellow...."+cashInflowReceivablesDTO);
                        //its already added from the green or carry forward
                        //totalAR = totalAR + cashInflowReceivablesDTO.getReceivableAmt();
                        dueReceivables = dueReceivables + cashInflowReceivablesDTO.getReceivableAmt();
                        // add it to net missed and if there is a W - subtract from net missed
                        netMissedCollection = netMissedCollection + cashInflowReceivablesDTO.getReceivableAmt();
                        sumOfReceivables = sumOfReceivables + cashInflowReceivablesDTO.getReceivableAmt();
                    }
                    else if ("W".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())) {
                        log.debug("White...."+cashInflowReceivablesDTO);
                        totalAR = totalAR - cashInflowReceivablesDTO.getReceivedAmt();
                        totalCashInflows = totalCashInflows + cashInflowReceivablesDTO.getReceivedAmt();
                        cashCollectionsAR = cashCollectionsAR + cashInflowReceivablesDTO.getReceivedAmt();
                        // add it to net missed and if there is a W - subtract from net missed
                        // W means it will be on the same day - it will get nullify
                        // if there is no W for that day with that amount then it will show in the netmissedday
                        netMissedCollection = netMissedCollection - cashInflowReceivablesDTO.getReceivedAmt();
                        sumOfReceivables = sumOfReceivables - cashInflowReceivablesDTO.getReceivedAmt();
                    }
                    else if ("R".equalsIgnoreCase(cashInflowReceivablesDTO.getColorCode())) {
                        log.debug("Red...."+cashInflowReceivablesDTO);
                        totalAR = totalAR - cashInflowReceivablesDTO.getReceivedAmt();
                        totalCashInflows = totalCashInflows + cashInflowReceivablesDTO.getReceivedAmt();
                        cashCollectionsAR = cashCollectionsAR + cashInflowReceivablesDTO.getReceivedAmt();
                        sumOfReceivables = sumOfReceivables - cashInflowReceivablesDTO.getReceivedAmt();
                    }
                }
            }
            count++;
            log.debug("cashCollectionsForSales...."+cashCollectionsForSales);
            log.debug("cashCollectionsAR...."+cashCollectionsAR);
            log.debug("totalAR...."+totalAR);
            log.debug("totalCashInflows...."+totalCashInflows);
            log.debug("dueReceivables...."+dueReceivables);
            log.debug("netMissedCollection...."+netMissedCollection);
            log.debug("sumOfReceivables...."+sumOfReceivables);
            cashInflowTotalsDTO.setCashCollectionSales(cashCollectionsForSales);
            cashInflowTotalsDTO.setCashCollectionForAR(cashCollectionsAR);
            cashInflowTotalsDTO.setTotalAR(totalAR);
            cashInflowTotalsDTO.setTotalCashInflow(totalCashInflows);
            cashInflowTotalsDTO.setDueARs(dueReceivables);
            cashInflowTotalsDTO.setNetMissedCollection(netMissedCollection);
            cashInflowTotalsDTO.setSumOfDueARs(sumOfReceivables);
        }
        customCashInflowTotalsRepository.saveAll(cashInflowTotalsMapper.toEntity(cashInflowTotalsDTOList));
    }

    /**
     *
     * second arg is the event that date has been changed in the UI, its getting from db before the update
     * this needs to be tracked to update the saved totals accordingly
     * @param cashInflowWrapperDTOS
     * @param prevEventDate
     */
    public void updateARTotals(List<CashInflowWrapperDTO> cashInflowWrapperDTOS,
                               LocalDate prevEventDate) {
        /**
         * find out the batch ids and party ids coming using the list of cashinflow wrappers
         * do this method for one company wise total - batch id and party id 0
         * then for each batch id - party id will be 0
         * then for party id - batch id will be 0
         * then combination of batch id and party id
         */
        List<CashBatchWrapper> batchParties = new ArrayList<CashBatchWrapper>();
        for(CashInflowWrapperDTO cashInflowWrapperDTO: cashInflowWrapperDTOS){
            checkAndAddToBatchParties(batchParties, cashInflowWrapperDTO.getCashInflowData());
        }
        log.debug("batchParties..."+batchParties);
        // first for each batch and party id 0 for company wide
        addBatchAndPartyWiseTotals(cashInflowWrapperDTOS, prevEventDate, 0L, 0L);
        // second batch wise
        for(CashBatchWrapper cashBatchWrapper: batchParties) {
            List<CashInflowWrapperDTO> cashInflowDataWrapperDTOS = getFilteredCifWrapperDtos(cashInflowWrapperDTOS,cashBatchWrapper.getId(),0L);
            addBatchAndPartyWiseTotals(cashInflowDataWrapperDTOS, prevEventDate, cashBatchWrapper.getId(), 0L);
        }
        // third party wise
        for(CashBatchWrapper cashBatchWrapper: batchParties) {
            List<CashInflowWrapperDTO> cashInflowDataWrapperDTOS = getFilteredCifWrapperDtos(cashInflowWrapperDTOS,0L,cashBatchWrapper.getPartyId());
            addBatchAndPartyWiseTotals(cashInflowDataWrapperDTOS, prevEventDate, 0L, cashBatchWrapper.getPartyId());
        }
        // fourth both batch and party wise
        for(CashBatchWrapper cashBatchWrapper: batchParties) {
            List<CashInflowWrapperDTO> cashInflowDataWrapperDTOS = getFilteredCifWrapperDtos(cashInflowWrapperDTOS,cashBatchWrapper.getId(),cashBatchWrapper.getPartyId());
            addBatchAndPartyWiseTotals(cashInflowDataWrapperDTOS, prevEventDate, cashBatchWrapper.getId(), cashBatchWrapper.getPartyId());
        }
    }

    private List<CashInflowWrapperDTO> getFilteredCifWrapperDtos(List<CashInflowWrapperDTO> cashInflowWrapperDTOS, Long batchId, Long partyId) {
        List<CashInflowWrapperDTO> filteredList = new ArrayList<CashInflowWrapperDTO>();
        log.debug("batchId:"+batchId+"::partyId::"+partyId);
        if(batchId == 0 && partyId == 0){
            return cashInflowWrapperDTOS;
        }
        else{
            if(batchId != 0 && partyId != 0){
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashInflowWrapperDTOS) {
                    if(cashInflowWrapperDTO.getCashInflowData().getCbBatchId().equals(batchId) &&
                        cashInflowWrapperDTO.getCashInflowData().getPartyId().equals(partyId)){
                        filteredList.add(cashInflowWrapperDTO);
                    }
                }
            }
            else if(batchId != 0){
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashInflowWrapperDTOS) {
                    if(cashInflowWrapperDTO.getCashInflowData().getCbBatchId().equals(batchId)){
                        filteredList.add(cashInflowWrapperDTO);
                    }
                }
            }
            else if(partyId != 0){
                for (CashInflowWrapperDTO cashInflowWrapperDTO : cashInflowWrapperDTOS) {
                    if(cashInflowWrapperDTO.getCashInflowData().getPartyId().equals(partyId)){
                        filteredList.add(cashInflowWrapperDTO);
                    }
                }
            }
            log.debug("returing filtered list "+filteredList);
            return filteredList;
        }
    }

    public void addBatchAndPartyWiseTotals(List<CashInflowWrapperDTO> cashInflowWrapperDTOS, LocalDate prevEventDate,
                                           Long batchId, Long partyId) {
        //get all sales date and receivable dates
        //and find out the earliest date among that and start updating the totals from that by getting cashinflow data including the existing totals
        //and overwrite the totals with the new total calculation and save the totals alone. (yet to write the method for that)
        List<LocalDate> salesDates = new ArrayList<LocalDate>();
        if (prevEventDate != null) {
            salesDates.add(prevEventDate);
        }
        //if its single record and then check for changed flag in any of the date fields, the add that date to the list
        //for (CashbudgetWrapperDTO cashbudgetWrapperDTO : cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
        //if (cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
        collectAllTransactionDates(cashInflowWrapperDTOS, salesDates);
        //}
        //}
        updateTotalsWithDates(salesDates, batchId, partyId);
    }

    public void collectAllTransactionDates(List<CashInflowWrapperDTO> cashInflowWrapperDTOS, List<LocalDate> salesDates) {
        for (CashInflowWrapperDTO cashInflowWrapperDTO : cashInflowWrapperDTOS) {
            if (cashInflowWrapperDTO.getCashInflowData().getSalesDate() != null) {
                salesDates.add(cashInflowWrapperDTO.getCashInflowData().getSalesDate());
                salesDates.add(cashInflowWrapperDTO.getCashInflowData().getSalesDate().plusDays(1));
            }
            //for each cashinflow add the receivable dates
            for (CashInflowReceivablesDTO cashInflowReceivablesDTO : cashInflowWrapperDTO.getCashInflowRbls()) {
                //
                if (cashInflowReceivablesDTO.getReceivableDate() != null && (cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("Y"))) {
                    salesDates.add(cashInflowReceivablesDTO.getReceivableDate());
                    salesDates.add(cashInflowReceivablesDTO.getReceivableDate().plusDays(1));
                }
                if (cashInflowReceivablesDTO.getReceivedDate() != null && (cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("W") || cashInflowReceivablesDTO.getColorCode().equalsIgnoreCase("R"))) {
                    salesDates.add(cashInflowReceivablesDTO.getReceivedDate());
                    salesDates.add(cashInflowReceivablesDTO.getReceivedDate().plusDays(1));
                }
            }
        }
    }

    private void checkAndAddToBatchParties(List<CashBatchWrapper> batchParties, CashInflowDataDTO cashInflowData) {
        for(CashBatchWrapper cashBatchWrapper: batchParties){
            if(cashBatchWrapper != null && cashBatchWrapper.getId().equals(cashInflowData.getCbBatchId()) &&
                cashBatchWrapper.getPartyId().equals(cashInflowData.getPartyId())){
                return;
            }
        }
        CashBatchWrapper cashBatchWrapper = new CashBatchWrapper();
        cashBatchWrapper.setId(cashInflowData.getCbBatchId());
        cashBatchWrapper.setPartyId(cashInflowData.getPartyId());
        batchParties.add(cashBatchWrapper);
    }

    /**
     * @param salesDates
     * @param batchId
     * @param partyId
     */
    public void updateTotalsWithDates(List<LocalDate> salesDates,
                                      Long batchId, Long partyId) {
        List<LocalDate> noDupesList = salesDates.stream().sorted().distinct().collect(Collectors.toList());
        for (LocalDate localDate: noDupesList){
            log.debug("date ...."+localDate);
        }
        LocalDate startDate = noDupesList.get(0);
        LocalDate endDate = noDupesList.get(noDupesList.size() - 1);
        log.debug("StartDate:"+startDate);
        log.debug("endDate:"+endDate);
        log.debug("batchId:"+batchId);
        log.debug("partyId:"+partyId);
        //get last date from the current totals for this company .. and if that is the last then take that as the end date
        // or treat the current endDate is the final one
        CashInflowTotals cashInflowTotals = customCashInflowTotalsRepository.findTotalsByMaxIdAndCompanyInfo(customUserService.getLoggedInCompanyInfo().getId(), batchId, partyId);
        log.debug("latest cashinflowtotals::"+cashInflowTotals);
        LocalDate currentEndDate = null;
        if(cashInflowTotals != null) {
            currentEndDate = cashInflowTotals.getCashInflowTotalDate();
            log.debug("currentEndDate:"+currentEndDate);
            if(currentEndDate.isAfter(endDate)) {
                endDate = currentEndDate;
            }
            log.debug("endDate1:"+endDate);
        }
        //again go back to the transaction dates
        List<CashInflowReceivablesDTO> cashInflowReceivablesDTOList = getCashInflowReceivablesForTheGivenPeriod(startDate,endDate,batchId,partyId);
        log.debug("cashInflowReceivablesDTOList::"+cashInflowReceivablesDTOList);
        /**
         * here get all receivable transaction dates and based on that fill the cashinflowtotals
         * for each date, add +1 day so that there wont be any transactions for that day
         * this way we can keep that days totals for the coming days till another valid entry
         * comes in the totals.
         */
        List<LocalDate> transactionDates = getTransactionDates(cashInflowReceivablesDTOList);
        //get one date prior to get the total ARs and sum of net missed ARs
        CashInflowTotals carryForwardCashInflowTotals = customCashInflowTotalsRepository.findCarryForwardTotals(customUserService.getLoggedInCompanyInfo().getId(),
            startDate, batchId, partyId);
        log.debug("carryForwardCashInflowTotals::"+carryForwardCashInflowTotals);
        List<CashInflowTotalsDTO> cashInflowTotalsDTOList = getCashInflowTotalsForTheGivenPeriod(startDate,endDate, batchId, partyId);
        log.debug("cashInflowTotalsDTOList:::"+cashInflowTotalsDTOList);
        List<CashInflowTotalsDTO> filteredCashInflowTotals = new ArrayList<CashInflowTotalsDTO>();
        if( cashInflowReceivablesDTOList != null && cashInflowReceivablesDTOList.size() > 0) {
            checkAndFillDateEntriesInCashInflowTotals(filteredCashInflowTotals, cashInflowTotalsDTOList,transactionDates, batchId, partyId);
            // if some totals are not there in filteredTotals because of delete from the ui - then delete it from totals records
            deleteTotalsByComparingFilteredTotals(cashInflowTotalsDTOList, filteredCashInflowTotals);
            recalculateARTotals(filteredCashInflowTotals, cashInflowReceivablesDTOList, batchId, partyId, carryForwardCashInflowTotals);
        }
        else{
            log.debug("setting all values to 0::");
            for (CashInflowTotalsDTO cashInflowTotalsDTO : cashInflowTotalsDTOList) {
                //since there are no receivables / transaction in that date range for that batch id and party id - delete everything
                filteredCashInflowTotals.add(cashInflowTotalsDTO);
            }
            // since everything is 0.0 .. delete those recrods.
            customCashInflowTotalsRepository.deleteAll(cashInflowTotalsMapper.toEntity(filteredCashInflowTotals));
        }
        log.debug("filteredCashInflowTotals:::"+filteredCashInflowTotals);
    }

    private void deleteTotalsByComparingFilteredTotals(List<CashInflowTotalsDTO> cashInflowTotalsDTOList, List<CashInflowTotalsDTO> filteredCashInflowTotals) {
        List<CashInflowTotalsDTO> toDelete = new ArrayList<CashInflowTotalsDTO>();
        boolean found = false;
        for (CashInflowTotalsDTO cashInflowTotalsDTO : cashInflowTotalsDTOList) {
            found = false;
            for (CashInflowTotalsDTO cashInflowTotalsDTO1 : filteredCashInflowTotals) {
                if (cashInflowTotalsDTO.getCashInflowTotalDate().equals(cashInflowTotalsDTO1.getCashInflowTotalDate())){
                    found = true;
                    break;
                }
            }
            if(!found){
                toDelete.add(cashInflowTotalsDTO);
            }
        }
        log.debug("going to delete::"+toDelete);
        customCashInflowTotalsRepository.deleteAll(cashInflowTotalsMapper.toEntity(toDelete));
    }

    private List<LocalDate> getTransactionDates(List<CashInflowReceivablesDTO> cashInflowReceivablesDTOList) {
        List<LocalDate> transactionDates = new ArrayList<LocalDate>();
        for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOList){
            // check for which date needs to be added - receivable or received
            if(cashInflowReceivablesDTO.getReceivedDate() != null) {
                transactionDates.add(cashInflowReceivablesDTO.getReceivedDate());
                transactionDates.add(cashInflowReceivablesDTO.getReceivedDate().plusDays(1));
            }
            if (cashInflowReceivablesDTO.getReceivableDate() != null){
                transactionDates.add(cashInflowReceivablesDTO.getReceivableDate());
                transactionDates.add(cashInflowReceivablesDTO.getReceivableDate().plusDays(1));
            }
        }
        log.debug("transactionDates::"+transactionDates);
        List<LocalDate> noDupesList = transactionDates.stream().sorted().distinct().collect(Collectors.toList());
        log.debug("noDupesList::"+noDupesList);
        return noDupesList;
    }

    private List<CashInflowTotalsDTO> checkAndFillDateEntriesInCashInflowTotals(List<CashInflowTotalsDTO> filteredCashInflowTotals,
                                                                                List<CashInflowTotalsDTO> cashInflowTotalsDTOList,
                                                                                List<LocalDate> trxnDates, Long batchId, Long partyId) {
        boolean found = false;
        String strNewStartDate = "";
        String strTotalDate = "";
        //since we are zeroing every totals in the trasaction date onwards
        log.debug("checkAndFillDateEntriesInCashInflowTotals::"+trxnDates);
        if(trxnDates != null && trxnDates.size() > 0) {
            for (LocalDate stDate : trxnDates) {
                found = false;
                strNewStartDate = CustomUtil.getDateInFormat(stDate, CustomConstant.date_yyyy_MM_dd);
                for (CashInflowTotalsDTO cashInflowTotalsDTO : cashInflowTotalsDTOList) {
                    strTotalDate = CustomUtil.getDateInFormat(cashInflowTotalsDTO.getCashInflowTotalDate(), CustomConstant.date_yyyy_MM_dd);
                    //log.debug("strTotalDate::"+strTotalDate+"::strNewStartDate::"+strNewStartDate);
                    if (strTotalDate.equalsIgnoreCase(strNewStartDate) &&
                            cashInflowTotalsDTO.getBatchId().equals(batchId) &&
                            cashInflowTotalsDTO.getPartyId().equals(partyId) ) {
                        log.debug(strTotalDate + " found in totals.."+cashInflowTotalsDTO);
                        //mark everything zero
                        cashInflowTotalsDTO.setCashCollectionSales(0.0);
                        cashInflowTotalsDTO.setSumOfDueARs(0.0);
                        cashInflowTotalsDTO.setNetMissedCollection(0.0);
                        cashInflowTotalsDTO.setDueARs(0.0);
                        cashInflowTotalsDTO.setTotalCashInflow(0.0);
                        cashInflowTotalsDTO.setTotalAR(0.0);
                        cashInflowTotalsDTO.setCashCollectionForAR(0.0);
                        found = true;
                        filteredCashInflowTotals.add(cashInflowTotalsDTO);
                        break;
                    }
                }
                if (!found) {
                    log.debug("this date is not there in totals.. so adding.." + stDate+"::for batch::"+batchId+"::partyid::"+partyId);
                    //add an object to cashInflowdatalist with date
                    CashInflowTotalsDTO cashInflowTotalsDTO = getCashInflowTotalsWithZeros();
                    cashInflowTotalsDTO.setBatchId(batchId);
                    cashInflowTotalsDTO.setPartyId(partyId);
                    cashInflowTotalsDTO.setCashInflowTotalDate(stDate);
                    cashInflowTotalsDTO.setCompanyInfoId(customUserService.getLoggedInCompanyInfo().getId());
                    filteredCashInflowTotals.add(cashInflowTotalsDTO);
                }
            }
        }
        return filteredCashInflowTotals;
    }

    private List<CashInflowTotalsDTO> getCashInflowTotalsForTheGivenPeriod(LocalDate stDate1, LocalDate endDate1, Long batchId, Long partyId) {
        List<CashInflowTotals> cashInflowTotals = customCashInflowTotalsRepository.findByTotalDateAndCompanyInfo(
            customUserService.getLoggedInCompanyInfo().getId(),
            stDate1,endDate1,
            batchId, partyId);
        return cashInflowTotalsMapper.toDto(cashInflowTotals);
    }

    private List<CashInflowReceivablesDTO> getCashInflowReceivablesForTheGivenPeriod(LocalDate stDate1, LocalDate endDate1, Long batchId, Long partyId) {
        List<CashInflowReceivables> cashInflowReceivables = cashInflowReceivablesQueryService.getReceivablesWithinRange(
            customUserService.getLoggedInCompanyInfo().getId(),
            stDate1, endDate1,
            batchId, partyId);
        log.debug("receivables within range "+cashInflowReceivables);
        //List<CashInflowReceivables> cashInflowReceivables = customCashInflowReceivablesRepository.getReceivablesWithinRange(
        //    customUserService.getLoggedInCompanyInfo().getId(),
        //    stDate1, endDate1, batchId, partyId);
        return cashInflowReceivablesMapper.toDto(cashInflowReceivables);
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
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void saveOrUpdateCashInflow(CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        CashInflowMaster cashInflowMaster = cashInflowMasterMapper.toEntity(cashInflowMasterWrapperDTO.getCashInflowMaster());
        cashInflowMaster.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        customCashInflowMasterRepository.save(cashInflowMaster);
        cashInflowMasterWrapperDTO.setCashInflowMaster(cashInflowMasterMapper.toDto(cashInflowMaster));
        CashInflowDataDTO cashInflowDataDTO = null;
        LocalDate prevEventDate = null;
            CashbudgetWrapperDTO cashbudgetWrapperDTO = cashInflowMasterWrapperDTO.getCashbudgetWrappers().get(0);
            if (cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {
                cashInflowDataDTO = cashbudgetWrapperDTO.getCashInflowWrappers().get(0).getCashInflowData();
                prevEventDate = getPrevEventDateAndDeleteOldEntries(cashInflowDataDTO, cashbudgetWrapperDTO);
                saveOrUpdateCashBudget(cashbudgetWrapperDTO.getCashInflowWrappers());
            }
        //}
        updateARTotals(cashbudgetWrapperDTO.getCashInflowWrappers(),prevEventDate);
    }

    public LocalDate getPrevEventDateAndDeleteOldEntries(CashInflowDataDTO cashInflowDataDTO, CashbudgetWrapperDTO cashbudgetWrapperDTO) {
        LocalDate prevEventDate = null;
        if(cashInflowDataDTO != null && cashInflowDataDTO.getId() != null) {
            log.debug("its and edit and have data"+cashInflowDataDTO);
            CashInflowData cashInflowData = cashInflowDataMapper.toEntity(cashInflowDataDTO);
            List<CashInflowReceivables> oldreceivables = customCashInflowReceivablesRepository.findByCid(cashInflowData);
            for(CashInflowReceivables cashInflowReceivables1: oldreceivables){
                if("G".equalsIgnoreCase(cashInflowReceivables1.getColorCode())){
                    prevEventDate = cashInflowReceivables1.getSalesDate();
                    break;
                }
            }
            //delete old receivables
            customCashInflowReceivablesRepository.deleteAll(oldreceivables);
            //delete all ids of the new receivables so that it will insert as new records
            List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = cashbudgetWrapperDTO.getCashInflowWrappers().get(0).getCashInflowRbls();
            for(CashInflowReceivablesDTO cashInflowReceivablesDTO: cashInflowReceivablesDTOS){
                cashInflowReceivablesDTO.setId(null);
            }
        }
        return prevEventDate;
    }

    public void saveOrUpdateCashBudget(List<CashInflowWrapperDTO> cashInflowWrapperDTOS){
        //save cashinflow or recur entry and get the id
        //save corresponding cash inflow receivables with this id and payments
        //save cashinflow range according to the sales date and receivable date
        //then call the total update method to update the totals
        //save cashinflow master

        //save recur data if there is no recur id in the records
        /*
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
        */
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
            saveCashInflowReceivables(cashInflowWrapperDTO, cashInflowData);
        }

        //total should be called outside this loop, if its set of records, then total calculation should start from the earliest date
        //and it should happen for month by month till the current month or the last receivable date
        //saveOrUpdateARTotals(cashInflowMasterWrapperDTO);
        //for (CashbudgetWrapperDTO cashbudgetWrapperDTO: cashInflowMasterWrapperDTO.getCashbudgetWrappers()) {
            //if(cashbudgetWrapperDTO != null && cashbudgetWrapperDTO.getCashInflowWrappers() != null) {

            //}
        //}
    }

    public void saveCashInflowReceivables(CashInflowWrapperDTO cashInflowWrapperDTO,
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
                        //check once again the due date and received date - if its changed to equal or less than the receivable date then make it to W
                        if(cashInflowReceivablesDTO1.getReceivedDate() != null && (cashInflowReceivablesDTO1.getReceivedDate().compareTo(cashInflowReceivablesDTO.getReceivableDate()) < 1)){
                            cashInflowReceivablesDTO1.setColorCode("W");
                        }
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
                    // if its G - put the cash received at sales here
                    cashInflowReceivablesDTO.setReceivedAmt(cashInflowData.getSalesCashAmount());
                }
                else {
                    cashInflowReceivablesDTO.setReceivableAmt(cashInflowData.getSalesAmount());
                    cashInflowReceivablesDTO.setReceivedAmt(0.0);
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
     * create cashinflow for every month till the end date from start date
     * also - create a yellow receivable record for each month
     * create a list of cashinflows and cashreceivable records after saving recurring entry
     * and reuse other methods to save cashinflows and cashreceiveables and totals
     */
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void saveOrUpdateRecurEntries(RecurringCBEntriesDTO recurringCBEntriesDTO) {
        log.debug("recurringEntriesDto.."+recurringCBEntriesDTO);
        //first save a recurring entry and get the recur id
        RecurringCBEntries recurringCBEntries = recurringCBEntriesMapper.toEntity(recurringCBEntriesDTO);
        customRecurringCBEntriesRepository.save(recurringCBEntries);
        recurringCBEntriesDTO = recurringCBEntriesMapper.toDto(recurringCBEntries);
        LocalDate startDate = recurringCBEntriesDTO.getEntryStartDate();
        LocalDate endDate = recurringCBEntriesDTO.getEntryEndDate();
        String increment = recurringCBEntriesDTO.getIncrement();
        String frequency = recurringCBEntriesDTO.getFrequency();
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS = new ArrayList<CashInflowWrapperDTO>();
        LocalDate eventDate = startDate;
        while(endDate.compareTo(eventDate) >= 0) {
            if ("Last".equalsIgnoreCase(increment)){
                eventDate = CustomUtil.getLastDayOfMonth(eventDate);
            }
            else {
                eventDate = eventDate.withDayOfMonth(Integer.parseInt(increment));
            }
            log.debug("eventDate::"+eventDate);
            CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
            CashInflowDataDTO cashInflowDataDTO = new CashInflowDataDTO();
            cashInflowDataDTO.setSalesDate(eventDate);
            cashInflowDataDTO.setReceivableName(recurringCBEntriesDTO.getEntryName());
            cashInflowDataDTO.setSalesAmount(recurringCBEntriesDTO.getEntryAmount());
            cashInflowDataDTO.setCompanyInfoId(customUserService.getLoggedInCompanyInfo().getId());
            cashInflowDataDTO.setEditId("C"+CustomUtil.getUniqueString());
            cashInflowDataDTO.setRecurId(recurringCBEntriesDTO.getId());
            cashInflowDataDTO.setPartyId(recurringCBEntriesDTO.getPartyId());
            cashInflowDataDTO.setCbBatchId(recurringCBEntriesDTO.getBatchId());
            cashInflowDataDTO.setCbType(recurringCBEntriesDTO.getCbType());
            cashInflowDataDTO.setInflowType("R");
            cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
            List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = new ArrayList<CashInflowReceivablesDTO>();
            CashInflowReceivablesDTO cashInflowReceivablesDTO = new CashInflowReceivablesDTO();
            cashInflowReceivablesDTO.setReceivableDate(eventDate);
            cashInflowReceivablesDTO.setSalesDate(eventDate);
            cashInflowReceivablesDTO.setReceivableAmt(cashInflowDataDTO.getSalesAmount());
            cashInflowReceivablesDTO.setColorCode("G");
            cashInflowReceivablesDTO.setEditId("G"+CustomUtil.getUniqueString());
            cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO);
            CashInflowReceivablesDTO cashInflowReceivablesDTO1 = new CashInflowReceivablesDTO();
            cashInflowReceivablesDTO1.setReceivableDate(eventDate);
            cashInflowReceivablesDTO1.setSalesDate(eventDate);
            cashInflowReceivablesDTO1.setReceivableAmt(cashInflowDataDTO.getSalesAmount());
            //for recurr it will be 100% for the first time, elese user can go and change in UI
            cashInflowReceivablesDTO1.setReceivablePercent(100.0);
            cashInflowReceivablesDTO1.setCreditPeriod("0");
            cashInflowReceivablesDTO1.setColorCode("Y");
            cashInflowReceivablesDTO1.setEditId("Y"+CustomUtil.getUniqueString());
            cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO1);
            cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesDTOS);
            cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
            CashInflowReceivablesDTO cashInflowReceivablesDTO2 = new CashInflowReceivablesDTO();
            //cashInflowReceivablesDTO2.setReceivableDate(eventDate);
            //cashInflowReceivablesDTO2.setSalesDate(eventDate);
            //cashInflowReceivablesDTO2.setReceivableAmt(cashInflowDataDTO.getSalesAmount());
            //for recurr it will be 100% for the first time, elese user can go and change in UI
            //cashInflowReceivablesDTO1.setReceivablePercent(100.0);
            //cashInflowReceivablesDTO1.setCreditPeriod("0");
            cashInflowReceivablesDTO2.setColorCode("W");
            cashInflowReceivablesDTO2.setEditId("WR"+CustomUtil.getUniqueString());
            //parent id will be edit id of yellow
            cashInflowReceivablesDTO2.setParId(cashInflowReceivablesDTO1.getEditId());
            cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO2);
            cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesDTOS);
            cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
            // create cashinflow and receivables just like other cashinflow from the ui
            // and form a list of cashinflow with receivables
            // write a new method to save this list and call the totals with start and
            // end date - this method can be used for upload functionality too
            eventDate = eventDate.plusMonths(1);
        }
        log.debug("cashinflowwrapperdtos.."+cashInflowWrapperDTOS);
        saveOrUpdateCashBudget(cashInflowWrapperDTOS);
        updateARTotals(cashInflowWrapperDTOS, null);
    }
    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void deleteRecurData(Long id) {
        List<CashInflowData> cashInflowDataList = customCashInflowDataRepository.findByRecurIdAndCompanyInfo(id, customUserService.getLoggedInCompanyInfo());
        log.debug("cif list for delete.."+cashInflowDataList);
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS = new ArrayList<CashInflowWrapperDTO>();
        Long batchId = 0L;
        Long partyId = 0L;
        if(cashInflowDataList != null && cashInflowDataList.size() > 0){
            for(CashInflowData cashInflowData: cashInflowDataList){
                CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
                CashInflowDataDTO cashInflowDataDTO = cashInflowDataMapper.toDto(cashInflowData);
                List<CashInflowReceivables> cashInflowReceivables = customCashInflowReceivablesRepository.findByCid(cashInflowData);
                if(cashInflowReceivables != null) {
                    cashInflowWrapperDTO.setCashInflowRbls(cashInflowReceivablesMapper.toDto(cashInflowReceivables));
                }
                cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
                cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
                batchId = cashInflowDataDTO.getCbBatchId();
                partyId = cashInflowDataDTO.getPartyId();
            }
        }
        List<LocalDate> salesDate = new ArrayList<LocalDate>();
        collectAllTransactionDates(cashInflowWrapperDTOS, salesDate);
        log.debug("salesdate from delete items::"+salesDate);
        if(cashInflowDataList != null && cashInflowDataList.size() > 0){
            for(CashInflowData cashInflowData: cashInflowDataList){
                deleteCifData(cashInflowData);
            }
        }
        callTotalUpdatesWithAllScenarios(salesDate,batchId,partyId);
        customRecurringCBEntriesRepository.deleteById(id);
    }

    /**
     *
     * Create a batch or category
     * Each cashinflow create will go into one batch
     * In the UI over all batch wise, they can expand and see each batch
     * how do we store totals??
     * one option is to introduce party id, batch id, total id in the totals
     * put company id in total id column to get all receivables for that company
     * there will be seperate total records for each party id, batch id, and total id (company id)
     * user would be able to fetch records based on party id, batch id, total id or party id and batch id or batch id and total id
     * dynamic fields in cash inflow or cashinflow payment records - how do we use it?
     * for return or cancel, we can create another color field and denotes that its cancel or return in the payment
     * screen.
     */

    public List<CashBudgetBatchDTO> getCashBatches(){
        List<CashBudgetBatch> cashBudgetBatches = cashBatchRepository.findByCompanyInfo(customUserService.getLoggedInCompanyInfo());
        return cashBudgetBatchMapper.toDto(cashBudgetBatches);
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void saveOrUpdateCashBatch(CashBudgetBatchDTO cashBudgetBatchDTO) {
        CashBudgetBatch cashBudgetBatch = cashBudgetBatchMapper.toEntity(cashBudgetBatchDTO);
        cashBudgetBatch.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        cashBatchRepository.save(cashBudgetBatch);
    }

    public List<CashBudgetPartyDTO> getCashParties() {
        List<CashBudgetParty> cashBudgetParties = cashPartyRepository.findByCompanyInfo(customUserService.getLoggedInCompanyInfo());
        return cashBudgetPartyMapper.toDto(cashBudgetParties);
    }

    @Transactional(readOnly = false,rollbackFor = Exception.class)
    public void saveOrUpdateCashParty(List<CashBudgetPartyDTO> cashBudgetPartyDTOS) {
        List<CashBudgetParty> cashBudgetParties = cashBudgetPartyMapper.toEntity(cashBudgetPartyDTOS);
        for(CashBudgetParty cashBudgetParty:cashBudgetParties){
            cashBudgetParty.setCompanyInfo(customUserService.getLoggedInCompanyInfo());
        }
        cashPartyRepository.saveAll(cashBudgetParties);
    }

}
