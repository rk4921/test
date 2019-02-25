package com.doodil.proforma.custom.web.rest.process;

import com.codahale.metrics.annotation.Timed;
import com.doodil.proforma.custom.repository.CmpnyBudgetDataRepository;
import com.doodil.proforma.custom.repository.CmpnyBudgetRepository;
import com.doodil.proforma.custom.service.CashBudgetReportService;
import com.doodil.proforma.custom.service.CashBudgetService;
import com.doodil.proforma.custom.service.CustomBudgetService;
import com.doodil.proforma.custom.service.CustomUserService;
import com.doodil.proforma.custom.service.dto.*;
import com.doodil.proforma.custom.utils.CustomUtil;
import com.doodil.proforma.domain.CompanyBudget;
import com.doodil.proforma.domain.CompanyBudgetData;
import com.doodil.proforma.repository.CompanyBudgetRepository;
import com.doodil.proforma.service.dto.*;
import com.doodil.proforma.service.mapper.CompanyBudgetDataMapper;
import com.doodil.proforma.service.mapper.CompanyBudgetMapper;
import com.doodil.proforma.web.rest.errors.BadRequestAlertException;
import com.doodil.proforma.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing cashbudget.
 */
@RestController
@RequestMapping("/api")
public class CashbudgetResource {

    private final Logger log = LoggerFactory.getLogger(CashbudgetResource.class);

    private static final String ENTITY_NAME = "Cashbudget";

    @Autowired
    CustomUserService customUserService;
    @Autowired
    CashBudgetService cashBudgetService;
    @Autowired
    CashBudgetReportService cashBudgetReportService;

    @PostMapping("/cash-inflow-ar-rpt")
    @Timed
    public ResponseEntity<CashInflowMasterWrapperDTO> getCashInflowARReport(@RequestBody CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        log.debug("REST request to get all getCashInflowARReport"+cashInflowMasterWrapperDTO);
        LocalDate currentDate = LocalDate.now();
        LocalDate fromDate = currentDate.minusMonths(3);
        LocalDate toDate = currentDate.plusMonths(3);
        if(cashInflowMasterWrapperDTO != null && cashInflowMasterWrapperDTO.getFromDate() != null ) {
            fromDate = cashInflowMasterWrapperDTO.getFromDate();
        }
        if(cashInflowMasterWrapperDTO != null && cashInflowMasterWrapperDTO.getToDate() != null ) {
            toDate = cashInflowMasterWrapperDTO.getToDate();
        }
        List<CashbudgetWrapperDTO> cashbudgetWrapperDTOS = cashBudgetReportService.getARReportPartyWise(fromDate,toDate);
        cashInflowMasterWrapperDTO.setCashbudgetWrappers(cashbudgetWrapperDTOS);
        return ResponseEntity.ok().body(cashInflowMasterWrapperDTO);
    }

    @PostMapping("/cash-outflow-ap-rpt")
    @Timed
    public ResponseEntity<CashInflowMasterWrapperDTO> getCashOutflowARReport(@RequestBody CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) {
        log.debug("REST request to get all getCashOutflowARReport"+cashInflowMasterWrapperDTO);
        LocalDate currentDate = LocalDate.now();
        LocalDate fromDate = currentDate.minusMonths(3);
        LocalDate toDate = currentDate.plusMonths(3);
        if(cashInflowMasterWrapperDTO != null && cashInflowMasterWrapperDTO.getFromDate() != null ) {
            fromDate = cashInflowMasterWrapperDTO.getFromDate();
        }
        if(cashInflowMasterWrapperDTO != null && cashInflowMasterWrapperDTO.getToDate() != null ) {
            toDate = cashInflowMasterWrapperDTO.getToDate();
        }
        List<CashbudgetWrapperDTO> cashbudgetWrapperDTOS = cashBudgetReportService.getAPReportPartyWise(fromDate,toDate);
        cashInflowMasterWrapperDTO.setCashbudgetWrappers(cashbudgetWrapperDTOS);
        return ResponseEntity.ok().body(cashInflowMasterWrapperDTO);
    }
    /**
     * GET  /cash-inflow : get cashinflow data for current month or the given month.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of cashbudgetwrapper in body
     */
    @GetMapping("/cash-inflow")
    @Timed
    public CashInflowMasterWrapperDTO getCashInflowData() {
        log.debug("REST request to get all getCashInflowData");
        //CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = getCashInflowMasterWrapperDTO();
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = cashBudgetService.getCashInflowData(LocalDate.now(), "current");
        return cashInflowMasterWrapperDTO;
    }

    @GetMapping("/cash-inflow-page/{pageInd}/{dt}")
    @Timed
    public CashInflowMasterWrapperDTO getCashInflowDataPage(@PathVariable String pageInd, @PathVariable String dt) {
        log.debug("REST request to get all getCashInflowData");
        //CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = getCashInflowMasterWrapperDTO();
        // dt - yyyy-mm-dd
        log.debug(pageInd+"::requested dt::"+dt);
        LocalDate localDate = LocalDate.of(new Integer(dt.substring(0,4)),
            new Integer(dt.substring(5,7)),
            new Integer(dt.substring(8,10)));
        log.debug("requested date::"+localDate);
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = cashBudgetService.getCashInflowData(localDate, pageInd);
        return cashInflowMasterWrapperDTO;
    }

    @PostMapping("/cashinflow-save")
    @Timed
    public ResponseEntity<CashInflowMasterWrapperDTO> saveOrUpdateCashInflow(@RequestBody CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) throws URISyntaxException {
        log.debug("before::cashInflowMasterWrapper::"+cashInflowMasterWrapperDTO);
        //cashBudgetService.saveOrUpdateCashInflow(cashInflowMasterWrapperDTO);
        log.debug("after::cashInflowMasterWrapper::"+cashInflowMasterWrapperDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Saved successfully.", ""))
            .body(cashInflowMasterWrapperDTO);
    }

    /**
     * GET  /cash-outflow : get cashoutflow data for current month or the given month.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of cashbudgetwrapper in body
     */
    @GetMapping("/cash-outflow")
    @Timed
    public CashInflowMasterWrapperDTO getCashOutflowData() {
        log.debug("REST request to get all getCashOutflowData");
        //CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = getCashInflowMasterWrapperDTO();
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = cashBudgetService.getCashOutflowData(LocalDate.now(),"current");
        return cashInflowMasterWrapperDTO;
    }

    @GetMapping("/cash-outflow-page/{pageInd}/{dt}")
    @Timed
    public CashInflowMasterWrapperDTO getCashOutflowDataPage(@PathVariable String pageInd, @PathVariable String dt) {
        log.debug("REST request to get all getCashOutflowData");
        //CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = getCashInflowMasterWrapperDTO();
        // dt - yyyy-mm-dd
        log.debug(pageInd+"::requested dt::"+dt);
        LocalDate localDate = LocalDate.of(new Integer(dt.substring(0,4)),
            new Integer(dt.substring(5,7)),
            new Integer(dt.substring(8,10)));
        log.debug("requested date::"+localDate);
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = cashBudgetService.getCashOutflowData(localDate,pageInd);
        return cashInflowMasterWrapperDTO;
    }

    @PostMapping("/cash-outflow-save")
    @Timed
    public ResponseEntity<CashInflowMasterWrapperDTO> saveOrUpdateCashOutflow(@RequestBody CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO) throws URISyntaxException {
        log.debug("before::cashInflowMasterWrapper::"+cashInflowMasterWrapperDTO);
        cashBudgetService.saveOrUpdateCashOutflow(cashInflowMasterWrapperDTO);
        log.debug("after::cashInflowMasterWrapper::"+cashInflowMasterWrapperDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createAlert("Saved successfully.", ""))
            .body(cashInflowMasterWrapperDTO);
    }

    /**
     * DELETE  /del-cif-data/:id : delete the "id" cash inflow data.
     *
     * @param id the id of the cash inflow data to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/del-cif-data/{id}")
    @Timed
    public ResponseEntity<Void> deleteCashInflowData(@PathVariable Long id) {
        log.debug("REST request to delete cash inflow data : {}", id);
        cashBudgetService.deleteCashInflowData(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * DELETE  /del-cof-data/:id : delete the "id" cash outflow data.
     *
     * @param id the id of the cash inflow data to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/del-cof-data/{id}")
    @Timed
    public ResponseEntity<Void> deleteCashOutflowData(@PathVariable Long id) {
        log.debug("REST request to delete cash outflow data : {}", id);
        cashBudgetService.deleteCashOutflowData(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * for testing - dummy records
     * @return
     */
    public CashInflowMasterWrapperDTO getCashInflowMasterWrapperDTO() {
        CashInflowMasterWrapperDTO cashInflowMasterWrapperDTO = new CashInflowMasterWrapperDTO();
        List<CashbudgetWrapperDTO> cashbudgetWrapperDTOs = new ArrayList<CashbudgetWrapperDTO>();
        CashbudgetWrapperDTO cashbudgetWrapperDTO = null;
        List<DayDTOWrapper> dayDTOWrappers1 = new ArrayList<DayDTOWrapper>();
        List<DayDTO> dayDTOS = customUserService.getDayList(LocalDate.now());
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS1 = new ArrayList<CashInflowWrapperDTO>();
        LocalDate localDate = LocalDate.now();
        //dummy data
        for(int i = 0;i<3;i++){
            CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
            CashInflowDataDTO cashInflowDataDTO = new CashInflowDataDTO();
            cashInflowDataDTO.setSalesDate(localDate.minusDays((28 - i)));
            cashInflowDataDTO.setPartyName("ABC"+i);
            cashInflowDataDTO.setProductName("Shirt"+i);
            cashInflowDataDTO.setReceivableName("sales of "+cashInflowDataDTO.getProductName());
            cashInflowDataDTO.setSalesAmount(1000.0 + i);
            cashInflowDataDTO.setEditId("top"+i);
            cashInflowDataDTO.setCreditPeriod("2");
            cashInflowDataDTO.setCreditSalesPercent(10.00);
            cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
            cashInflowWrapperDTOS1.add(cashInflowWrapperDTO);
        }
        DayDTOWrapper dayDTOWrapper = null;
        for(int i = 0; i < cashInflowWrapperDTOS1.size(); i++){
            List<DayDTO> dayDTOS1 = customUserService.getDayList(LocalDate.now());
            addEditId(dayDTOS1, "top"+i);
            addDataToDayDTO(dayDTOS1,cashInflowWrapperDTOS1.get(i));
            dayDTOWrapper = new DayDTOWrapper();
            dayDTOWrapper.setDayDtos(dayDTOS1);
            dayDTOWrapper.setEditId("top"+i);
            dayDTOWrappers1.add(dayDTOWrapper);
        }
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        cashbudgetWrapperDTO.setCashInflowWrappers(cashInflowWrapperDTOS1);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers1);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);
        cashbudgetWrapperDTO = new CashbudgetWrapperDTO();
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS2 = new ArrayList<CashInflowWrapperDTO>();
        for(int i = 0;i<2;i++){
            CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
            CashInflowDataDTO cashInflowDataDTO = new CashInflowDataDTO();
            cashInflowDataDTO.setSalesDate(localDate.minusDays(28 - i));
            cashInflowDataDTO.setPartyName("ABC"+i);
            cashInflowDataDTO.setProductName("Shirt"+i);
            cashInflowDataDTO.setReceivableName("sales of "+cashInflowDataDTO.getProductName());
            cashInflowDataDTO.setSalesAmount(1000.0 + i);
            cashInflowDataDTO.setEditId("down" + i);
            cashInflowDataDTO.setCreditSalesPercent(100.00);
            cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
            cashInflowWrapperDTOS2.add(cashInflowWrapperDTO);
        }
        List<DayDTOWrapper> dayDTOWrappers2 = new ArrayList<DayDTOWrapper>();
        for(int i = 0; i < cashInflowWrapperDTOS2.size(); i++){
            dayDTOWrapper = new DayDTOWrapper();
            dayDTOWrapper.setDayDtos(dayDTOS);
            dayDTOWrapper.setEditId("down" + i);
            dayDTOWrappers2.add(dayDTOWrapper);
        }
        cashbudgetWrapperDTO.setCashInflowWrappers(cashInflowWrapperDTOS2);
        cashbudgetWrapperDTO.setDayDTOWrappers(dayDTOWrappers2);
        cashbudgetWrapperDTO.setDayList(dayDTOS);
        cashbudgetWrapperDTOs.add(cashbudgetWrapperDTO);

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
        cashInflowMasterWrapperDTO.setCarryForwardAR(0.00);
        cashInflowMasterWrapperDTO.setCashbudgetWrappers(cashbudgetWrapperDTOs);
        CashInflowMasterDTO cashInflowMasterDTO = new CashInflowMasterDTO();
        cashInflowMasterWrapperDTO.setCashInflowMaster(cashInflowMasterDTO);
        return cashInflowMasterWrapperDTO;
    }

    private void addDataToDayDTO(List<DayDTO> dayDTOS, CashInflowWrapperDTO cashInflowWrapperDTO) {
        log.debug("adding receivables data cashInflowWrapperDTO:salesDate::"+cashInflowWrapperDTO.getCashInflowData().getSalesDate()+"::editId::"+
            cashInflowWrapperDTO.getCashInflowData().getEditId());
        if(cashInflowWrapperDTO.getCashInflowData().getSalesDate() != null){
            for(DayDTO dayDTO: dayDTOS){
                //log.debug("event Date:::"+dayDTO.getEventDate()+"::editId::"+dayDTO.getEditId());
                if(dayDTO.getEventDate().equals(cashInflowWrapperDTO.getCashInflowData().getSalesDate())
                    && dayDTO.getEditId().equals(cashInflowWrapperDTO.getCashInflowData().getEditId())){
                    //log.debug("adding receivables....");
                    List<CashInflowReceivablesDTO> cashInflowReceivablesDTOS = new ArrayList<CashInflowReceivablesDTO>();
                    CashInflowReceivablesDTO cashInflowReceivablesDTO = new CashInflowReceivablesDTO();
                    cashInflowReceivablesDTO.setReceivableAmt(cashInflowWrapperDTO.getCashInflowData().getSalesAmount());
                    cashInflowReceivablesDTO.setReceivableDate(dayDTO.getEventDate());
                    cashInflowReceivablesDTO.setColorCode("G");

                    //CashInflowReceivablesDTO cashInflowReceivablesDTO1 = new CashInflowReceivablesDTO();
                    //cashInflowReceivablesDTO1.setReceivableDate(dayDTO.getEventDate().plusDays(8));
                    //cashInflowReceivablesDTO1.setReceivablePercent(80.00);
                    //cashInflowReceivablesDTO1.setColorCode("Y");
                    //cashInflowReceivablesDTOS.add(cashInflowReceivablesDTO1);
                    dayDTO.setCashInflowReceivables(cashInflowReceivablesDTOS);
                    log.debug("added receivables....");
                }
            }
        }
    }

    private void addEditId(List<DayDTO> dayDTOS, String editId) {
        for(DayDTO dayDTO:dayDTOS){
            dayDTO.setEditId(editId);
        }
    }

    /**
     * GET  /dnd-cashin-template : get cash in template
     *
     * @return the template in KeyValueDTO
     */
    @GetMapping("/dnd-cashin-template")
    @Timed
    public KeyValueDTO getCashInTemplate() {
        String fileContent = "Sales Date (YYYY-MM-DD),Party Name,Product/Service Name,Receivable Name,Credit Period (Days),Credit Sales Percent,Sales Amount,Sales Cash Amount,Due Date Collection(YYYY-MM-DD)";
        KeyValueDTO keyValueDTO = new KeyValueDTO();
        keyValueDTO.setKey("file_content");
        keyValueDTO.setValue(fileContent);
        return keyValueDTO;
    }

    @PostMapping("/upload-cashin-template")
    @Timed
    public List<CashInflowWrapperDTO> uploadCashInTemplate(@RequestParam("tFile") MultipartFile file) throws URISyntaxException {
        log.debug("REST request to upload cash in template : {}", file);
        List<CashInflowWrapperDTO> cashInflowWrapperDTOS = new ArrayList<CashInflowWrapperDTO>();
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;
            int count = 0;
            Double cashAmt = 0.0;
            String dueCollection = null;
            while ((line = br.readLine()) != null) {
                cashAmt = 0.0;
                // skip first line as it is header
                log.debug("line.."+line);
                if( count > 0) {
                    if(line.endsWith(",")) {
                        line = line + "N";
                    }
                    CashInflowWrapperDTO cashInflowWrapperDTO = new CashInflowWrapperDTO();
                    String lines[] = line.split(",");
                    CashInflowDataDTO cashInflowDataDTO = new CashInflowDataDTO();
                    cashInflowDataDTO.setSalesDate(CustomUtil.getLocalDate(lines[0], "yyyy-MM-d"));
                    log.debug("sales date : "+cashInflowDataDTO.getSalesDate());
                    cashInflowDataDTO.setPartyName(lines[1]);
                    cashInflowDataDTO.setProductName(lines[2]);
                    cashInflowDataDTO.setReceivableName(lines[3]);
                    dueCollection = lines[8];
                    if(dueCollection != null && dueCollection.trim().length() > 0) {
                        cashInflowDataDTO.setDueDateCollection(CustomUtil.getLocalDate(lines[8], "yyyy-MM-d"));
                        // set the credit period accordingly
                        cashInflowDataDTO.setCreditPeriod(""+CustomUtil.getDurationInDays(cashInflowDataDTO.getSalesDate(), cashInflowDataDTO.getDueDateCollection()));
                    }
                    CashInflowReceivablesDTO cashInflowReceivablesDTO = new CashInflowReceivablesDTO();
                    if(cashInflowDataDTO.getCreditPeriod() != null && cashInflowDataDTO.getCreditPeriod().trim().length() > 0){
                        cashInflowReceivablesDTO.setCreditPeriod(cashInflowDataDTO.getCreditPeriod());
                    } else {
                        cashInflowReceivablesDTO.setCreditPeriod(lines[4]);
                    }
                    cashInflowReceivablesDTO.setReceivablePercent(new Double(lines[5]));
                    cashInflowDataDTO.setSalesAmount(new Double(lines[6]));
                    cashAmt = cashInflowDataDTO.getSalesAmount() * cashInflowReceivablesDTO.getReceivablePercent() / 100;
                    cashInflowDataDTO.setSalesCashAmount(cashAmt);
                    cashInflowReceivablesDTO.setSalesDate(cashInflowDataDTO.getSalesDate());
                    //for green sales and receivable date is same
                    cashInflowReceivablesDTO.setReceivableDate(cashInflowReceivablesDTO.getSalesDate());
                    cashInflowReceivablesDTO.setReceivableAmt((cashInflowDataDTO.getSalesAmount() - cashAmt));
                    cashInflowWrapperDTO.setCashInflowRbls(cashBudgetService.getCashInflowReceivablesDTOs(cashInflowReceivablesDTO));
                    cashInflowWrapperDTO.setCashInflowData(cashInflowDataDTO);
                    cashInflowWrapperDTOS.add(cashInflowWrapperDTO);
                }
                count++;
            }
        } catch (IOException e) {
            log.error("error in uploading cash in template",e);
            cashInflowWrapperDTOS = null;
        }
        if(cashInflowWrapperDTOS != null) {
            cashBudgetService.uploadCashInflows(cashInflowWrapperDTOS);
        }
        return cashInflowWrapperDTOS;
    }

    /**
     * GET  /dnd-cashin-template : get cash in template
     *
     * @return the template in KeyValueDTO
     */
    @GetMapping("/dnd-cashout-template")
    @Timed
    public KeyValueDTO getCashOutTemplate() {
        String fileContent = "Expense Date (YYYY-MM-DD),Party Name,Expense Name,Payable Name,Credit Period (Days),Credit Purchase Percent,Inventory Lead Times (Days),Amount,Cash Amount,Due Date Payment(YYYY-MM-DD)";
        KeyValueDTO keyValueDTO = new KeyValueDTO();
        keyValueDTO.setKey("file_content");
        keyValueDTO.setValue(fileContent);
        return keyValueDTO;
    }

    @PostMapping("/upload-cashout-template")
    @Timed
    public List<CashOutflowWrapperDTO> uploadCashOutTemplate(@RequestParam("tFile") MultipartFile file) throws URISyntaxException {
        log.debug("REST request to upload cash out template : {}", file);
        List<CashOutflowWrapperDTO> cashOutflowWrapperDTOS = new ArrayList<CashOutflowWrapperDTO>();
        try ( BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))){
            String line;
            int count = 0;
            Double cashAmt = 0.0;
            String duePayment = null;
            while ((line = br.readLine()) != null) {
                cashAmt = 0.0;
                // skip first line as it is header
                log.debug("line.."+line);
                if( count > 0) {
                    if(line.endsWith(",")) {
                        line = line + "N";
                    }
                    CashOutflowWrapperDTO cashOutflowWrapperDTO = new CashOutflowWrapperDTO();
                    String lines[] = line.split(",");
                    CashOutflowDataDTO cashOutflowDataDTO = new CashOutflowDataDTO();
                    cashOutflowDataDTO.setExpenseDate(CustomUtil.getLocalDate(lines[0], "yyyy-MM-d"));
                    log.debug("expense date : "+cashOutflowDataDTO.getExpenseDate());
                    cashOutflowDataDTO.setPartyName(lines[1]);
                    cashOutflowDataDTO.setExpenseName(lines[2]);
                    cashOutflowDataDTO.setPayableName(lines[3]);
                    duePayment = lines[9];
                    if(duePayment != null && duePayment.trim().length() > 0) {
                        cashOutflowDataDTO.setDueDatePayment(CustomUtil.getLocalDate(lines[9], "yyyy-MM-d"));
                        cashOutflowDataDTO.setCreditPeriod(""+CustomUtil.getDurationInDays(cashOutflowDataDTO.getExpenseDate(),cashOutflowDataDTO.getDueDatePayment()));
                    }
                    CashOutflowPayablesDTO cashOutflowPayablesDTO = new CashOutflowPayablesDTO();
                    if(cashOutflowDataDTO.getCreditPeriod() != null && cashOutflowDataDTO.getCreditPeriod().trim().length() > 0){
                        cashOutflowPayablesDTO.setCreditPeriod(cashOutflowDataDTO.getCreditPeriod());
                    } else {
                        cashOutflowPayablesDTO.setCreditPeriod(lines[4]);
                    }
                    cashOutflowPayablesDTO.setPayablePercent(new Double(lines[5]));
                    cashOutflowDataDTO.setExpenseAmount(new Double(lines[7]));
                    cashAmt = cashOutflowDataDTO.getExpenseAmount() * cashOutflowPayablesDTO.getPayablePercent() / 100;
                    cashOutflowDataDTO.setExpenseCashAmount(cashAmt);
                    cashOutflowPayablesDTO.setExpenseDate(cashOutflowDataDTO.getExpenseDate());
                    //for brown expense and payable date is same
                    cashOutflowPayablesDTO.setPayableDate(cashOutflowDataDTO.getExpenseDate());
                    cashOutflowPayablesDTO.setPayableAmt((cashOutflowDataDTO.getExpenseAmount() - cashAmt));
                    log.debug("cashOutflowDataDTO "+cashOutflowDataDTO);
                    log.debug("cashOutflowPayablesDTO "+cashOutflowPayablesDTO);
                    cashOutflowWrapperDTO.setCashOutflowPbls(cashBudgetService.getCashOutflowPayablesDTOs(cashOutflowPayablesDTO));
                    cashOutflowDataDTO.setInventoryLeadTime(lines[6]);
                    cashOutflowWrapperDTO.setCashOutflowData(cashOutflowDataDTO);
                    cashOutflowWrapperDTOS.add(cashOutflowWrapperDTO);
                }
                count++;
            }
        } catch (IOException e) {
            log.error("error in uploading cash in template",e);
            cashOutflowWrapperDTOS = null;
        }
        if(cashOutflowWrapperDTOS != null){
            cashBudgetService.uploadCashOutflows(cashOutflowWrapperDTOS);
        }
        return cashOutflowWrapperDTOS;
    }


}
