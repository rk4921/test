import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs/Rx';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import * as moment from 'moment';

import { ICashInflowMasterWrapper } from './cashinflow-master-wrapper.model';
import { CashOutflowWrapper } from './cash-outflow-wrapper.model';
import { CashInflowWrapper } from './cash-inflow-wrapper.model';
import { CashInflowData } from '../../shared/model/cash-inflow-data.model';
import { ICashInflowData } from '../../shared/model/cash-inflow-data.model';
import { ICashInflowWrapper } from './cash-inflow-wrapper.model';
import { ICashOutflowWrapper } from './cash-outflow-wrapper.model';
import { ICashInflowReceivables } from '../../shared/model/cash-inflow-receivables.model';
import { ICashOutflowPayables } from '../../shared/model/cash-outflow-payables.model';
import { ICashbudgetWrapper } from './cashbudget-wrapper.model';

@Injectable({ providedIn: 'root' })
export class CashbudgetService {

    cofWrapper: CashOutflowWrapper;
    cifWrapper: CashInflowWrapper;
    cifMasterWrapper: ICashInflowMasterWrapper;
    minDate: any;
    maxDate: any;

    constructor(private http: HttpClient) {
    }

    getCashInflowARReport(cashInflowMasterWrapper: ICashInflowMasterWrapper): Observable<HttpResponse<ICashInflowMasterWrapper>> {
        const cashInflowARRptUrl = 'api/cash-inflow-ar-rpt';
        return this.http.post(`${cashInflowARRptUrl}`, cashInflowMasterWrapper).map((res: HttpResponse<ICashInflowMasterWrapper>) => res);
    }

    getCashOutflowAPReport(cashInflowMasterWrapper: ICashInflowMasterWrapper): Observable<HttpResponse<ICashInflowMasterWrapper>> {
        const cashInflowAPRptUrl = 'api/cash-outflow-ap-rpt';
        return this.http.post(`${cashInflowAPRptUrl}`, cashInflowMasterWrapper).map((res: HttpResponse<ICashInflowMasterWrapper>) => res);
    }

    getCashInTemplate() {
        const dndCinTemplateUrl = 'api/dnd-cashin-template';
        return this.http.get(`${dndCinTemplateUrl}`)
            .map((res: HttpResponse<any>) => res);
    }

    uploadCashInTemplate(formData): Observable<HttpResponse<ICashInflowWrapper[]>> {
        const uploadCashInUrl = 'api/upload-cashin-template';
        return this.http.post(`${uploadCashInUrl}`, formData).map((res: HttpResponse<ICashInflowWrapper[]>) => {
            return res;
        });
    }

    getCashOutTemplate() {
        const dndCoutTemplateUrl = 'api/dnd-cashout-template';
        return this.http.get(`${dndCoutTemplateUrl}`)
            .map((res: HttpResponse<any>) => res);
    }

    uploadCashOutTemplate(formData): Observable<HttpResponse<ICashOutflowWrapper[]>> {
        const uploadCashOutUrl = 'api/upload-cashout-template';
        return this.http.post(`${uploadCashOutUrl}`, formData).map((res: HttpResponse<ICashOutflowWrapper[]>) => {
            return res;
        });
    }

    getCashInflowPage(pageInd: string, dt: string) {
        const cashInflowUrl = 'api/cash-inflow-page';
        return this.http.get<ICashInflowMasterWrapper>(`${cashInflowUrl}/${pageInd}/${dt}`, { observe: 'response' });
    }

    getCashInflowData() {
        const cashInflowUrl = 'api/cash-inflow';
        return this.http.get<ICashInflowMasterWrapper>(`${cashInflowUrl}`, { observe: 'response' });
    }

    saveCashInflow(cashInflowMasterWrapper: ICashInflowMasterWrapper): Observable<HttpResponse<ICashInflowMasterWrapper>> {
        const cashInflowSaveUrl = 'api/cashinflow-save';
        console.log('service wrapper:',cashInflowMasterWrapper);
        const copy = this.convertDateFromClient1(cashInflowMasterWrapper);
        return this.http.post(`${cashInflowSaveUrl}`, cashInflowMasterWrapper).map((res: HttpResponse<ICashInflowMasterWrapper>) => res);
    }

    getCashOutflowPage(pageInd: string, dt: string) {
        const cashInflowUrl = 'api/cash-outflow-page';
        return this.http.get<ICashInflowMasterWrapper>(`${cashInflowUrl}/${pageInd}/${dt}`, { observe: 'response' });
    }

    getCashOutflowData() {
        const cashOutflowUrl = 'api/cash-outflow';
        return this.http.get<ICashInflowMasterWrapper>(`${cashOutflowUrl}`, { observe: 'response' });
    }

    saveCashOutflow(cashInflowMasterWrapper: ICashInflowMasterWrapper): Observable<HttpResponse<ICashInflowMasterWrapper>> {
        const cashOutflowSaveUrl = 'api/cash-outflow-save';
        const copy = this.convertDateFromClient1(cashInflowMasterWrapper);
        return this.http.post(`${cashOutflowSaveUrl}`, cashInflowMasterWrapper).map((res: HttpResponse<ICashInflowMasterWrapper>) => res);
    }

    deleteCashInflowData(id: number): Observable<any> {
        const deleteCifUrl = 'api/del-cif-data';
        return this.http.delete(`${deleteCifUrl}/${id}`);
    }

    deleteCashOutflowData(id: number): Observable<any> {
        const deleteCofUrl = 'api/del-cof-data';
        return this.http.delete(`${deleteCofUrl}/${id}`);
    }

    private convertDateFromClient(cashInflowMasterWrapper: ICashInflowMasterWrapper): ICashInflowMasterWrapper {
        const copy1: ICashInflowMasterWrapper = Object.assign({}, cashInflowMasterWrapper);
        console.log('1copy1..', copy1);
        if (copy1.cashbudgetWrappers) {
            for (const cbw of copy1.cashbudgetWrappers) {
                if (cbw.cashInflowWrappers) {
                    for (const ciw of cbw.cashInflowWrappers) {
                        console.log('sales date:', ciw.cashInflowData.salesDate);
                    }
                }
                if (cbw.cashOutflowWrappers) {
                    for (const ciw of cbw.cashOutflowWrappers) {
                        console.log('expense date:', ciw.cashOutflowData.expenseDate);
                    }
                }
            }
            for (const cbw of copy1.cashbudgetWrappers) {
                if (cbw.dayDTOWrappers) {
                    for (const dtw of cbw.dayDTOWrappers) {
                        if (dtw.dayDtos) {
                            for (const ddt of dtw.dayDtos) {
                                if (ddt.cashInflowReceivables) {
                                    for (const cir of ddt.cashInflowReceivables) {
                                        console.log('receivable date:', cir.receivableDate);
                                        console.log('sales date:', cir.salesDate);
                                    }
                                }
                                if (ddt.cashOutflowPayables) {
                                    for (const cir of ddt.cashOutflowPayables) {
                                        console.log('payable date:', cir.payableDate);
                                        console.log('expense date:', cir.expenseDate);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return copy1;
    }

    private convertDateFromClient1(cashInflowMasterWrapper: ICashInflowMasterWrapper): ICashInflowMasterWrapper {
        const copy1: ICashInflowMasterWrapper = Object.assign({}, cashInflowMasterWrapper);
        console.log('1copy1..', copy1);
        if (copy1.cashbudgetWrappers) {
            for (const cbw of copy1.cashbudgetWrappers) {
                if (cbw.cashInflowWrappers) {
                    for (const ciw of cbw.cashInflowWrappers) {
                        let ciwc = ciw.cashInflowData;
                        const copy: ICashInflowData = Object.assign({}, ciwc, {
                            salesDate:
                                ciwc.salesDate != null && moment(ciwc.salesDate).isValid()
                                    ? moment(ciwc.salesDate).format(DATE_FORMAT)
                                    : null,
                            dueDateCollection:
                                ciwc.dueDateCollection != null && moment(ciwc.dueDateCollection).isValid()
                                    ? moment(ciwc.dueDateCollection).format(DATE_FORMAT)
                                    : null
                        });
                        ciw.cashInflowData = copy;
                    }
                }
                if (cbw.cashOutflowWrappers) {
                    for (const ciw of cbw.cashOutflowWrappers) {
                        console.log('expense date:', ciw.cashOutflowData.expenseDate);
                        let ciwc = ciw.cashOutflowData;
                        const copy: ICashInflowData = Object.assign({}, ciwc, {
                            expenseDate:
                                ciwc.expenseDate != null && moment(ciwc.expenseDate).isValid()
                                    ? moment(ciwc.expenseDate).format(DATE_FORMAT)
                                    : null,
                            dueDatePayment:
                                ciwc.dueDatePayment != null && moment(ciwc.dueDatePayment).isValid()
                                    ? moment(ciwc.dueDatePayment).format(DATE_FORMAT)
                                    : null
                        });
                        ciw.cashOutflowData = copy;
                    }
                }
            }
            for (const cbw of copy1.cashbudgetWrappers) {
                if (cbw.dayDTOWrappers) {
                    for (const dtw of cbw.dayDTOWrappers) {
                        if (dtw.dayDtos) {
                            for (const ddt of dtw.dayDtos) {
                                if (ddt.cashInflowReceivables) {
                                    const cifrs: ICashInflowReceivables[] = [];
                                    for (const cir of ddt.cashInflowReceivables) {
                                        const copy: ICashInflowReceivables = Object.assign({}, cir, {
                                            salesDate:
                                                cir.salesDate != null && moment(cir.salesDate).isValid()
                                                    ? moment(cir.salesDate).format(DATE_FORMAT)
                                                    : null,
                                            receivableDate:
                                                cir.receivableDate != null && moment(cir.receivableDate).isValid()
                                                    ? moment(cir.receivableDate).format(DATE_FORMAT)
                                                    : null
                                        });
                                        cifrs.push(copy);
                                    }
                                    console.log('cifrs:',cifrs);
                                    ddt.cashInflowReceivables = cifrs;
                                }
                                if (ddt.cashOutflowPayables) {
                                    const cops: ICashOutflowPayables[] = [];
                                    for (const cir of ddt.cashOutflowPayables) {
                                        const copy: ICashOutflowPayables = Object.assign({}, cir, {
                                            expenseDate:
                                                cir.expenseDate != null && moment(cir.expenseDate).isValid()
                                                    ? moment(cir.expenseDate).format(DATE_FORMAT)
                                                    : null,
                                            payableDate:
                                                cir.payableDate != null && moment(cir.payableDate).isValid()
                                                    ? moment(cir.payableDate).format(DATE_FORMAT)
                                                    : null
                                        });
                                        cops.push(copy);
                                    }
                                    console.log('cops:',cops);
                                    ddt.cashOutflowPayables = cops;
                                }
                            }
                        }
                    }
                }
            }
        }
        return copy1;
    }

}
