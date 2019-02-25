import { Component, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs/Rx';
import { Subscription } from 'rxjs/Rx';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiEventManager, JhiParseLinks, JhiPaginationUtil, JhiAlertService } from 'ng-jhipster';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import * as moment from 'moment';

import { CashInflowReceivables } from '../../shared/model/cash-inflow-receivables.model';
import { CashbudgetService } from './cashbudget.service';
import { CashInflowData } from '../../shared/model/cash-inflow-data.model';
import { CashInflowWrapper } from './cash-inflow-wrapper.model';
import { ICashInflowMasterWrapper } from './cashinflow-master-wrapper.model';

@Component({
    selector: 'jhi-cash-inflow-data-entry-compoent',
    templateUrl: './cash-inflow-data-entry.component.html'
})
export class CashInflowDataEntryComponent implements OnInit {

    cashInflowData: CashInflowData;
    cifWrapper: CashInflowWrapper;
    cifMasterWrapper: ICashInflowMasterWrapper;
    minDate: any;
    maxDate: any;
    isSaving: boolean;

    constructor(
        public activeModal: NgbActiveModal,
        private route: ActivatedRoute,
        private alertService: JhiAlertService,
        private eventManager: JhiEventManager,
        public cashbudgetService: CashbudgetService,
    ) {
    }

    applySalesAmount(arg) {
        arg.salesCashAmount = arg.salesAmount;
        let salesRecAmt = 0;
        if (this.cifWrapper.cashInflowRbls) {
            for (const cir of this.cifWrapper.cashInflowRbls) {
                if (cir.receivablePercent) {
                    salesRecAmt = salesRecAmt + (arg.salesAmount * (Number(cir.receivablePercent) / 100));
                    console.log('salesRecAmt::', salesRecAmt);
                }
            }
            if (salesRecAmt > 0) {
                arg.salesCashAmount = arg.salesAmount - salesRecAmt;
            }
        }
    }

    changeCreditPeriod(ciw) {
        const salesDate = moment(ciw.salesDate);
        const dueDate = moment(ciw.dueDateCollection);
        const duration = dueDate.diff(salesDate, 'days');
        console.log('duraton:', duration);
        this.cifWrapper.cashInflowRbls[0].creditPeriod = duration+'';
    }

    ngOnInit() {
        this.cashInflowData = this.cashbudgetService.cifWrapper.cashInflowData;
        this.cifWrapper = this.cashbudgetService.cifWrapper;
        this.cifMasterWrapper = this.cashbudgetService.cifMasterWrapper;
        console.log('this.cifMasterWrapper',this.cifMasterWrapper);
        this.minDate = this.cashbudgetService.minDate;
        this.maxDate = this.cashbudgetService.maxDate;
    }

    addRow() {
        if (this.cifWrapper.cashInflowRbls) {
            const cop = new CashInflowReceivables();
            cop.creditPeriod = 2 * Number(this.cifWrapper.cashInflowRbls[0].creditPeriod) + '';
            cop.receivablePercent = this.cifWrapper.cashInflowRbls[0].receivablePercent;
            this.cifWrapper.cashInflowRbls.push(cop);
        }
    }

    deleteRow(index) {
        this.cifWrapper.cashInflowRbls.splice(index, 1);
    }

    save_old() {
        this.applySalesAmount(this.cashInflowData);
        this.eventManager.broadcast({ name: 'cashInflowModification', content: 'OK'});
        this.activeModal.dismiss('cancel');
    }

    save() {
        this.applySalesAmount(this.cashInflowData);
        this.cifMasterWrapper.cashbudgetWrappers[0].cashInflowWrappers = [];
        this.cifMasterWrapper.cashbudgetWrappers[0].cashInflowWrappers.push(this.cifWrapper);
        console.log('2.this.cifMasterWrapper',this.cifMasterWrapper);
        this.isSaving = true;
        this.subscribeToSaveResponse(
            this.cashbudgetService.saveCashInflow(this.cifMasterWrapper));
    }

    private subscribeToSaveResponse(result: Observable<HttpResponse<ICashInflowMasterWrapper>>) {
        result.subscribe(
                (res: HttpResponse<ICashInflowMasterWrapper>) => {
                    this.onSaveSuccess(res);
                },
                (res: HttpErrorResponse) => this.onError(res.message));
    }

    private onSaveSuccess(result) {
        this.isSaving = false;
        this.eventManager.broadcast({ name: 'cashInflowModification', content: 'OK'});
    }

    clear() {
        this.eventManager.broadcast({ name: 'cashInflowCancel', content: 'OK'});
        this.activeModal.dismiss('cancel');
    }

    private onError(error) {
        this.alertService.error(error.message, null, null);
    }
}
