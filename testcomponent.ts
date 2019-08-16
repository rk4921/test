.cal-month-view .cal-header {
    text-align: center;
    font-weight: bolder; }

.cal-month-view .cal-header .cal-cell {
    padding: 5px 0;
    overflow: hidden;
    -o-text-overflow: ellipsis;
    text-overflow: ellipsis;
    display: block;
    white-space: nowrap; }

.cal-month-view .cal-days {
    border: 1px solid;
    border-bottom: 0; }

.cal-month-view .cal-cell-top {
    min-height: 78px;
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1; }

.cal-month-view .cal-cell-row {
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex; }

.cal-month-view .cal-cell {
    float: left;
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    -webkit-box-orient: vertical;
    -webkit-box-direction: normal;
    -ms-flex-direction: column;
    flex-direction: column;
    -webkit-box-align: stretch;
    -ms-flex-align: stretch;
    align-items: stretch; }

.cal-month-view .cal-day-cell {
    min-height: 100px; }
@media all and (-ms-high-contrast: none) {
    .cal-month-view .cal-day-cell {
        display: block; } }

.cal-month-view .cal-day-cell:not(:last-child) {
    border-right: 1px solid; }

.cal-month-view .cal-days .cal-cell-row {
    border-bottom: 1px solid; }

.cal-month-view .cal-day-badge {
    margin-top: 18px;
    margin-left: 10px;
    display: inline-block;
    min-width: 10px;
    padding: 3px 7px;
    font-size: 12px;
    font-weight: 700;
    line-height: 1;
    text-align: center;
    white-space: nowrap;
    vertical-align: middle;
    border-radius: 10px; }

.cal-month-view .cal-day-number {
    font-size: 1.2em;
    font-weight: 400;
    opacity: 0.5;
    margin-top: 15px;
    margin-right: 15px;
    float: right;
    margin-bottom: 10px; }

.cal-month-view .cal-events {
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    -webkit-box-align: end;
    -ms-flex-align: end;
    align-items: flex-end;
    margin: 3px;
    line-height: 10px;
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    -ms-flex-wrap: wrap;
    flex-wrap: wrap; }

.cal-month-view .cal-event {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    display: inline-block;
    margin: 2px; }

.cal-month-view .cal-day-cell.cal-in-month.cal-has-events {
    cursor: pointer; }

.cal-month-view .cal-day-cell.cal-out-month .cal-day-number {
    opacity: 0.1;
    cursor: default; }

.cal-month-view .cal-day-cell.cal-today .cal-day-number {
    font-size: 1.9em; }

.cal-month-view .cal-open-day-events {
    padding: 15px; }

.cal-month-view .cal-open-day-events .cal-event {
    position: relative;
    top: 2px; }

.cal-month-view .cal-out-month .cal-day-badge,
.cal-month-view .cal-out-month .cal-event {
    opacity: 0.3; }

.cal-month-view .cal-draggable {
    cursor: move; }

.cal-month-view .cal-drag-active * {
    pointer-events: none; }

.cal-month-view .cal-event-title {
    cursor: pointer; }
.cal-month-view .cal-event-title:hover {
    text-decoration: underline; }

.cal-month-view {
    background-color: #fff; }
.cal-month-view .cal-cell-row:hover {
    background-color: #fafafa; }
.cal-month-view .cal-cell-row .cal-cell:hover,
.cal-month-view .cal-cell.cal-has-events.cal-open {
    background-color: #ededed; }
.cal-month-view .cal-days {
    border-color: #e1e1e1; }
.cal-month-view .cal-day-cell:not(:last-child) {
    border-right-color: #e1e1e1; }
.cal-month-view .cal-days .cal-cell-row {
    border-bottom-color: #e1e1e1; }
.cal-month-view .cal-day-badge {
    background-color: #b94a48;
    color: #fff; }
.cal-month-view .cal-event {
    background-color: #1e90ff;
    border-color: #d1e8ff;
    color: #fff; }
.cal-month-view .cal-day-cell.cal-weekend .cal-day-number {
    color: #8b0000; }
.cal-month-view .cal-day-cell.cal-today {
    background-color: #e8fde7; }
.cal-month-view .cal-day-cell.cal-drag-over {
    background-color: #e0e0e0 !important; }
.cal-month-view .cal-open-day-events {
    color: #fff;
    background-color: #555;
    -webkit-box-shadow: inset 0 0 15px 0 rgba(0, 0, 0, 0.5);
    box-shadow: inset 0 0 15px 0 rgba(0, 0, 0, 0.5); }

.cal-week-view {
    /* stylelint-disable-next-line selector-type-no-unknown */ }
.cal-week-view .cal-day-headers {
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    padding-left: 70px;
    border: 1px solid; }
.cal-week-view .cal-day-headers .cal-header {
    -webkit-box-flex: 1;
    -ms-flex: 1;
    flex: 1;
    text-align: center;
    padding: 5px; }
.cal-week-view .cal-day-headers .cal-header:not(:last-child) {
    border-right: 1px solid; }
.cal-week-view .cal-day-headers .cal-header:first-child {
    border-left: 1px solid; }
.cal-week-view .cal-day-headers span {
    font-weight: 400;
    opacity: 0.5; }
.cal-week-view .cal-day-column {
    -webkit-box-flex: 1;
    -ms-flex-positive: 1;
    flex-grow: 1;
    border-left: solid 1px; }
.cal-week-view .cal-event {
    font-size: 12px;
    border: 1px solid; }
.cal-week-view .cal-time-label-column {
    width: 70px;
    height: 100%; }
.cal-week-view .cal-all-day-events {
    border: solid 1px;
    border-top: 0;
    border-bottom-width: 3px;
    padding-top: 3px;
    position: relative; }
.cal-week-view .cal-all-day-events .cal-day-columns {
    height: 100%;
    width: 100%;
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    position: absolute;
    top: 0;
    z-index: 0; }
.cal-week-view .cal-all-day-events .cal-events-row {
    position: relative;
    height: 31px;
    margin-left: 70px; }
.cal-week-view .cal-all-day-events .cal-event-container {
    display: inline-block;
    position: absolute; }
.cal-week-view .cal-all-day-events .cal-event-container.resize-active {
    z-index: 1;
    pointer-events: none; }
.cal-week-view .cal-all-day-events .cal-event {
    padding: 0 5px;
    margin-left: 2px;
    margin-right: 2px;
    height: 28px;
    line-height: 28px; }
.cal-week-view .cal-all-day-events .cal-starts-within-week .cal-event {
    border-top-left-radius: 5px;
    border-bottom-left-radius: 5px; }
.cal-week-view .cal-all-day-events .cal-ends-within-week .cal-event {
    border-top-right-radius: 5px;
    border-bottom-right-radius: 5px; }
.cal-week-view .cal-all-day-events .cal-time-label-column {
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    -webkit-box-align: center;
    -ms-flex-align: center;
    align-items: center;
    -webkit-box-pack: center;
    -ms-flex-pack: center;
    justify-content: center;
    font-size: 14px; }
.cal-week-view .cal-all-day-events .cal-resize-handle {
    width: 6px;
    height: 100%;
    cursor: col-resize;
    position: absolute;
    top: 0; }
.cal-week-view .cal-all-day-events .cal-resize-handle.cal-resize-handle-after-end {
    right: 0; }
.cal-week-view .cal-event,
.cal-week-view .cal-header {
    overflow: hidden;
    -o-text-overflow: ellipsis;
    text-overflow: ellipsis;
    white-space: nowrap; }
.cal-week-view .cal-drag-active {
    pointer-events: none;
    z-index: 1; }
.cal-week-view .cal-drag-active * {
    pointer-events: none; }
.cal-week-view .cal-time-events {
    position: relative;
    border: solid 1px;
    border-top: 0;
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex; }
.cal-week-view .cal-time-events .cal-day-columns {
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    -webkit-box-flex: 1;
    -ms-flex-positive: 1;
    flex-grow: 1; }
.cal-week-view .cal-time-events .cal-day-column {
    position: relative; }
.cal-week-view .cal-time-events .cal-event-container {
    position: absolute;
    z-index: 1; }
.cal-week-view .cal-time-events .cal-event {
    width: calc(100% - 2px);
    height: calc(100% - 2px);
    margin: 1px;
    padding: 0 5px;
    line-height: 25px; }
.cal-week-view .cal-time-events .cal-resize-handle {
    width: 100%;
    height: 4px;
    cursor: row-resize;
    position: absolute; }
.cal-week-view .cal-time-events .cal-resize-handle.cal-resize-handle-after-end {
    bottom: 0; }
.cal-week-view .cal-hour-segment {
    position: relative; }
.cal-week-view .cal-hour-segment::after {
    content: '\00a0'; }
.cal-week-view .cal-event-container:not(.cal-draggable) {
    cursor: pointer; }
.cal-week-view .cal-draggable {
    cursor: move; }
.cal-week-view mwl-calendar-week-view-hour-segment,
.cal-week-view .cal-hour-segment {
    display: block; }
.cal-week-view .cal-hour:not(:last-child) .cal-hour-segment,
.cal-week-view .cal-hour:last-child :not(:last-child) .cal-hour-segment {
    border-bottom: thin dashed; }
.cal-week-view .cal-time {
    font-weight: bold;
    padding-top: 5px;
    width: 70px;
    text-align: center; }
.cal-week-view .cal-hour-segment.cal-after-hour-start .cal-time {
    display: none; }
.cal-week-view .cal-starts-within-day .cal-event {
    border-top-left-radius: 5px;
    border-top-right-radius: 5px; }
.cal-week-view .cal-ends-within-day .cal-event {
    border-bottom-left-radius: 5px;
    border-bottom-right-radius: 5px; }

.cal-week-view {
    background-color: #fff; }
.cal-week-view .cal-day-headers {
    border-color: #e1e1e1; }
.cal-week-view .cal-day-headers .cal-header:not(:last-child) {
    border-right-color: #e1e1e1; }
.cal-week-view .cal-day-headers .cal-header:first-child {
    border-left-color: #e1e1e1; }
.cal-week-view .cal-day-headers .cal-header:hover,
.cal-week-view .cal-day-headers .cal-drag-over {
    background-color: #ededed; }
.cal-week-view .cal-day-column {
    border-left-color: #e1e1e1; }
.cal-week-view .cal-event {
    background-color: #d1e8ff;
    border-color: #1e90ff;
    color: #1e90ff; }
.cal-week-view .cal-all-day-events {
    border-color: #e1e1e1; }
.cal-week-view .cal-header.cal-today {
    background-color: #e8fde7; }
.cal-week-view .cal-header.cal-weekend span {
    color: #8b0000; }
.cal-week-view .cal-time-events {
    border-color: #e1e1e1; }
.cal-week-view .cal-time-events .cal-day-columns:not(.cal-resize-active) .cal-hour-segment:hover {
    background-color: #ededed; }
.cal-week-view .cal-hour-odd {
    background-color: #fafafa; }
.cal-week-view .cal-drag-over .cal-hour-segment {
    background-color: #ededed; }
.cal-week-view .cal-hour:not(:last-child) .cal-hour-segment,
.cal-week-view .cal-hour:last-child :not(:last-child) .cal-hour-segment {
    border-bottom-color: #e1e1e1; }

.cal-day-view {
    /* stylelint-disable-next-line selector-type-no-unknown */ }
.cal-day-view .cal-hour-rows {
    width: 100%;
    border: solid 1px;
    overflow-x: auto;
    position: relative; }
.cal-day-view mwl-calendar-day-view-hour-segment,
.cal-day-view .cal-hour-segment {
    display: block; }
.cal-day-view .cal-hour-segment::after {
    content: '\00a0'; }
.cal-day-view .cal-hour:not(:last-child) .cal-hour-segment,
.cal-day-view .cal-hour:last-child :not(:last-child) .cal-hour-segment {
    border-bottom: thin dashed; }
.cal-day-view .cal-time {
    font-weight: bold;
    width: 70px;
    height: 100%;
    display: -webkit-box;
    display: -ms-flexbox;
    -js-display: flex;
    display: flex;
    -webkit-box-pack: center;
    -ms-flex-pack: center;
    justify-content: center;
    -webkit-box-align: center;
    -ms-flex-align: center;
    align-items: center; }
.cal-day-view .cal-hour-segment.cal-after-hour-start .cal-time {
    display: none; }
.cal-day-view .cal-drag-active .cal-hour-segment {
    pointer-events: none; }
.cal-day-view .cal-event-container {
    position: absolute;
    cursor: pointer; }
.cal-day-view .cal-event-container.resize-active {
    z-index: 1;
    pointer-events: none; }
.cal-day-view .cal-event {
    padding: 5px;
    font-size: 12px;
    border: 1px solid;
    -webkit-box-sizing: border-box;
    box-sizing: border-box;
    overflow: hidden;
    -o-text-overflow: ellipsis;
    text-overflow: ellipsis;
    white-space: nowrap;
    height: 100%; }
.cal-day-view .cal-all-day-events > * {
    cursor: pointer; }
.cal-day-view .cal-draggable {
    cursor: move; }
.cal-day-view .cal-starts-within-day .cal-event {
    border-top-left-radius: 5px;
    border-top-right-radius: 5px; }
.cal-day-view .cal-ends-within-day .cal-event {
    border-bottom-left-radius: 5px;
    border-bottom-right-radius: 5px; }
.cal-day-view .cal-drag-active {
    z-index: 1; }
.cal-day-view .cal-drag-active * {
    pointer-events: none; }
.cal-day-view .cal-resize-handle {
    width: 100%;
    height: 4px;
    cursor: row-resize;
    position: absolute; }
.cal-day-view .cal-resize-handle.cal-resize-handle-after-end {
    bottom: 0; }

.cal-day-view {
    background-color: #fff; }
.cal-day-view .cal-hour-rows {
    border-color: #e1e1e1; }
.cal-day-view .cal-hour:nth-child(odd) {
    background-color: #fafafa; }
.cal-day-view .cal-hour:not(:last-child) .cal-hour-segment,
.cal-day-view .cal-hour:last-child :not(:last-child) .cal-hour-segment {
    border-bottom-color: #e1e1e1; }
.cal-day-view .cal-hour-segment:hover,
.cal-day-view .cal-drag-over .cal-hour-segment {
    background-color: #ededed; }
.cal-day-view .cal-event {
    background-color: #d1e8ff;
    border-color: #1e90ff;
    color: #1e90ff; }

.cal-tooltip {
    position: absolute;
    z-index: 1070;
    display: block;
    font-style: normal;
    font-weight: normal;
    letter-spacing: normal;
    line-break: auto;
    line-height: 1.5;
    text-align: start;
    text-decoration: none;
    text-shadow: none;
    text-transform: none;
    white-space: normal;
    word-break: normal;
    word-spacing: normal;
    font-size: 11px;
    word-wrap: break-word;
    opacity: 0.9; }

.cal-tooltip.cal-tooltip-top {
    padding: 5px 0;
    margin-top: -3px; }

.cal-tooltip.cal-tooltip-top .cal-tooltip-arrow {
    bottom: 0;
    left: 50%;
    margin-left: -5px;
    border-width: 5px 5px 0; }

.cal-tooltip.cal-tooltip-right {
    padding: 0 5px;
    margin-left: 3px; }

.cal-tooltip.cal-tooltip-right .cal-tooltip-arrow {
    top: 50%;
    left: 0;
    margin-top: -5px;
    border-width: 5px 5px 5px 0; }

.cal-tooltip.cal-tooltip-bottom {
    padding: 5px 0;
    margin-top: 3px; }

.cal-tooltip.cal-tooltip-bottom .cal-tooltip-arrow {
    top: 0;
    left: 50%;
    margin-left: -5px;
    border-width: 0 5px 5px; }

.cal-tooltip.cal-tooltip-left {
    padding: 0 5px;
    margin-left: -3px; }

.cal-tooltip.cal-tooltip-left .cal-tooltip-arrow {
    top: 50%;
    right: 0;
    margin-top: -5px;
    border-width: 5px 0 5px 5px; }

.cal-tooltip-inner {
    max-width: 200px;
    padding: 3px 8px;
    text-align: center;
    border-radius: 0.25rem; }

.cal-tooltip-arrow {
    position: absolute;
    width: 0;
    height: 0;
    border-color: transparent;
    border-style: solid; }

.cal-tooltip.cal-tooltip-top .cal-tooltip-arrow {
    border-top-color: #000; }

.cal-tooltip.cal-tooltip-right .cal-tooltip-arrow {
    border-right-color: #000; }

.cal-tooltip.cal-tooltip-bottom .cal-tooltip-arrow {
    border-bottom-color: #000; }

.cal-tooltip.cal-tooltip-left .cal-tooltip-arrow {
    border-left-color: #000; }

.cal-tooltip-inner {
    color: #fff;
    background-color: #000; }

.rounded-center-blue {
    width: 30px;
    height: 30px;
    background-color: blue;
    text-align:center;
    color:white;
}

.rounded-center-grey {
    width: 30px;
    height: 30px;
    //background-color: lightgrey;
    text-align:center;
    //color:white;
}



=====================

<div class="container">
  <div class="row">
    <div class="col-md-4">
      <div class="btn-group">
        <div class="btn btn-primary btn-sm">
          <button class="btn btn-primary btn-sm" (click)="prevView()">Previous</button>
        </div>
        <div class="btn btn-outline-secondary btn-sm">
          <button class="btn btn-primary btn-sm" (click)="dayView()">Today</button>
        </div>
        <div class="btn btn-primary btn-sm">
          <button class="btn btn-primary btn-sm" (click)="nextView()">Next</button>
        </div>
      </div>
    </div>
    <div class="col-md-4">
      <h3 _ngcontent-c1="">{{currentMonth}}&nbsp;{{currentYear}}</h3>
    </div>
    <div class="col-md-4">
      <div class="btn-group">
        <button class="btn btn-primary btn-sm active" (click)="getMonthView()">
          Month
        </button>
        <button class="btn btn-primary btn-sm" (click)="getWeekView()">
          Week
        </button>
        <button class="btn btn-primary btn-sm" (click)="getDayView()">
          Day
        </button>
      </div>
    </div>
  </div>
    <br _ngcontent-c1="">
    <div _ngcontent-c1="">
    <div class="cal-month-view">
      <!---->
      <!---->
      <div class="cal-cell-row cal-header ng-star-inserted">
        <ng-container *ngFor="let dw of weekWrappers[0].dayWrappers">
        <!---->
        <div class="cal-cell">
          {{dw.dayTxt}}
        </div>
        </ng-container>
      </div>
      <div class="cal-days">
        <!---->
        <div class="ng-star-inserted">
          <ng-container *ngFor="let wk of weekWrappers">
          <div class="cal-cell-row cal-cell-top ng-star-inserted">
            <ng-container *ngFor="let dw of wk.dayWrappers">
            <!---->
            <div class="cal-cell" (click)="openDetail(dw, wk)">
              <!---->
                <ng-container *ngIf="dw.isToday === true">
                    <div class="rounded-center-blue rounded-circle"> {{dw.dayNumber}}</div>
                </ng-container>
                <ng-container *ngIf="dw.isToday !== true">
                    <div class="rounded-center-grey rounded-circle"> {{dw.dayNumber}}</div>
                </ng-container>
              <!---->
            </div>
            </ng-container>
          </div>
          <div class="cal-cell-row cal-cell-top ng-star-inserted" *ngIf="wk.openFlag === true">
            <span (click)="closeDetail(wk)">X</span>
            <!---->
            <!---->
          </div>
          </ng-container>
        </div>
      </div>
    </div>
    <!---->
    <!---->
  </div>
  <!---->
</div>


=============================

import { Component, OnInit } from '@angular/core';
import {IWeekWrapper, WeekWrapper} from './weekwrapper.model';
import {DayWrapper, IDayWrapper} from './daywrapper.model';
import {MonthWrapper} from './monthwrapper.model';

@Component({
  selector: 'app-cal',
  templateUrl: './cal.component.html',
  styleUrls: ['./cal.component.css']
})
export class CalComponent implements OnInit {
    currentMonth: any;
    currentYear: any;
    currentFreq: any;
    today: any;
    refDate: any;
    prevDate: any;
    nextDate: any;
    monthWrapper: MonthWrapper;
    weekWrappers: IWeekWrapper[] = [];
    dayWrappers: IDayWrapper[] = [];
    days: any = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];

  constructor() { }

  ngOnInit() {
      this.today = new Date();
      this.refDate = new Date(this.today.getTime());
      this.getMonthView();
  }

  dayView() {
      this.refDate = new Date(this.today.getTime());
      this.getDayList(this.currentFreq);
  }

    getWeekView() {
      this.refDate = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), 1);//first day of current month
      this.getDayList('Week')
    }

    getMonthView() {
        this.refDate = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), 1);//first day of current month
        this.getDayList('Month')
    }

    getDayView() {
        this.refDate = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), 1);//first day of current month
        this.getDayList('Day')
    }

  prevView() {
      this.refDate = new Date(this.prevDate.getTime());
      this.getDayList(this.currentFreq);
  }

  nextView() {
    this.refDate =  new Date(this.nextDate.getTime());
    this.getDayList(this.currentFreq);
  }

  setNextAndPrev() {
    this.currentMonth = this.refDate.toLocaleString('en-us', { month: 'long' });
    this.currentYear = this.refDate.toLocaleString('en-us', { year: 'numeric' });
    this.prevDate = new Date(this.refDate.getTime());
    this.nextDate = new Date(this.refDate.getTime());
    if (this.currentFreq === 'Day') {
        this.prevDate.setDate(this.prevDate.getDate() - 1);
        this.nextDate.setDate(this.nextDate.getDate() + 1);
    } else if (this.currentFreq === 'Week') {
        this.prevDate.setDate(this.prevDate.getDate() - 7);
        this.nextDate.setDate(this.nextDate.getDate() + 7);
    } else if (this.currentFreq === 'Month') {
        this.prevDate.setMonth(this.prevDate.getMonth() - 1);
        this.nextDate.setMonth(this.nextDate.getMonth() + 1);
    }
    // console.log('prev', this.prevDate);
    // console.log('next', this.nextDate);
  }

  getDayList(freq) {
    this.currentFreq = freq;
    this.setNextAndPrev();
    // console.log('getting freq.', freq);
    this.weekWrappers = [];
    // console.log('today ', this.refDate);
    const month = this.refDate.getMonth();
    const noOfDays = this.daysInThisMonth(this.refDate);
    // console.log('noOfDays ', noOfDays);
    let firstDay = new Date(this.refDate.getTime());
    let lastDay = new Date(this.refDate.getTime());
    const dayTxtCount = this.refDate.getDay();
    // console.log('dayTxtCount ', dayTxtCount);
    if (freq === 'Week') {
        firstDay = this.getViewStartDate(firstDay, dayTxtCount);
        lastDay = this.getLastDayOfWeek(lastDay, dayTxtCount);
    } else if (freq === 'Month') {
        firstDay = new Date(this.refDate.getFullYear(), this.refDate.getMonth(), 1);
        lastDay = new Date(this.refDate.getFullYear(), this.refDate.getMonth() + 1, 0);
    }
    // console.log('first day ', firstDay);
    // console.log('last day ', lastDay);
    let weekWrapper = null;
    let lastRowAdd = false;
    if (freq === 'Day') {
        weekWrapper = new WeekWrapper();
        weekWrapper.dayWrappers = [];
        weekWrapper.dayWrappers.push(this.getDayWrapper(this.refDate,true));
    } else {
        const viewStartDt = new Date(firstDay.getTime());
        // console.log('viewStartDt ', viewStartDt);
        let i = 0;
        let firstTime = true;
        while (i < 7) {
            if (i === 0) {
                weekWrapper = new WeekWrapper();
                weekWrapper.dayWrappers = [];
                if (firstTime) {
                    i = this.addPrevMonthEntries(viewStartDt, weekWrapper);
                    firstTime = false;
                }
              // and increment the i accordingly
            }
            weekWrapper.dayWrappers.push(this.getDayWrapper(viewStartDt,true));
            if (i === 6) {
              this.weekWrappers.push(weekWrapper);
            }
            i = i + 1;
            if (viewStartDt < lastDay) {
              if (i === 7 && freq === 'Month') {
                i = 0;
              }
              viewStartDt.setDate(viewStartDt.getDate() + 1);
            } else {
              break;
            }
        }
        while (i < 7) {
            lastRowAdd = true;
            viewStartDt.setDate(viewStartDt.getDate() + 1);
            weekWrapper.dayWrappers.push(this.getDayWrapper(viewStartDt, false));
            i = i + 1;
            console.log('adding lastrow count', i);
        }
    }
    if ((freq === 'Month' && lastRowAdd) || freq === 'Day') {
        this.weekWrappers.push(weekWrapper);
    }
    // console.log('this.weekWrappers ',this.weekWrappers);
  }

    addPrevMonthEntries(dt, weekWrapper) {
        const count = dt.getDay();
        for (let j = 0; j < count; j++) {
            const newDt = new Date(dt.getTime());
            newDt.setDate(newDt.getDate() + j - count);
            weekWrapper.dayWrappers.push(this.getDayWrapper(newDt,false));
            //console.log('prevmonth', newDt);
        }
        return count;
    }

  getDayWrapper(dt, statusFlag) {
    const dayWrapper = new DayWrapper();
    dayWrapper.statusFlag = statusFlag;
    dayWrapper.dayNumber = dt.getDate() + '';
    const dayName = this.days[dt.getDay()];
    dayWrapper.dayTxt = dayName;
    if (dt.getDate() === this.today.getDate()) {
        const dt1 = new Date(dt.getFullYear(), dt.getMonth(), dt.getDate());
        const dt2 = new Date(this.today.getFullYear(), this.today.getMonth(), this.today.getDate());
        if (dt1.getTime() === dt2.getTime() && (dt1.getMonth() === this.refDate.getMonth())) {
            dayWrapper.isToday = true;
        }
    }
    // console.log("dt.getTime()",dt.getTime());
    // console.log("this.today.getTime()",this.today.getTime());
    // console.log('dayWrapper:', dayWrapper);
    return dayWrapper;
  }

  getViewStartDate(firstDate: Date, dayTxtCount: number) {
      const startDate = new Date(firstDate.getTime());
      startDate.setDate(startDate.getDate() - dayTxtCount);
      // console.log('dt ',dt);
      return startDate;
  }
  
  gotoHeader(elId) {
      const element = this.renderer.selectRootElement('#hdr'+elId+'0',true);
      console.log('11element::',element);
      setTimeout(() => element.scrollIntoView({ behavior: 'smooth', block: "center", inline: "center" },true), 0);
      //setTimeout(() => (element.scrollLeft = 50), 0);
      //setTimeout(() => element.style.backgroundColor = "red",0);
  }

  getLastDayOfWeek(today: Date, dayTxtCount: number) {
    const dt = new Date(today.getTime());
    const count = dt.getDay();
    dt.setDate(dt.getDate() + (7 - count));
    // console.log('dt ',dt);
    return dt;
  }

  daysInThisMonth(dt: Date) {
    return new Date(dt.getFullYear(), (dt.getMonth() + 1), 0).getDate();
  }

  openDetail(dw, wk) {
      console.log('open flag::', wk.openFlag);
      if(dw.statusFlag === false){
          return;
      }
      if (wk.openFlag === true) {
          wk.openFlag = false;
      } else {
          wk.openFlag = true;
      }
  }

  closeDetail(wk) {
      wk.openFlag = false;
  }
}

=======================


export interface ICalEvent {
    eventName?: string;
}

export class CalEvent {
constructor(
        public eventName?: string,
    ) {
    }
}


=========================

import { Moment } from 'moment';

export interface IDayWrapper {
    eventDate?: Moment;
    isToday?: boolean;
    dayTxt?: string;
    dayNumber?: string;
    statusFlag?: boolean;
}

export class DayWrapper {
constructor(
        public eventDate?: Moment,
        public isToday?: boolean,
        public dayTxt?: string,
        public dayNumber?: string,
        public statusFlag?: boolean,
    ) {
    }
}


========================

import {IWeekWrapper} from './weekwrapper.model';


export interface IMonthWrapper {
    weekWrappers?: IWeekWrapper[];
}

export class MonthWrapper {
    constructor(
        public weekWrappers?: IWeekWrapper[],
    ) {
    }
}


===================

import {IDayWrapper} from './daywrapper.model';


export interface IWeekWrapper {
    dayWrappers?: IDayWrapper[];
    openFlag?: boolean;
}

export class WeekWrapper {
constructor(
        public dayWrappers?: IDayWrapper[],
        public openFlag?: boolean,
    ) {
    }
}

SXSSFWorkbook workbook = new SXSSFWorkbook(100);
    private void writeIntoExcel1(CbReportWrapper cbReportWrapper, Workbook workbook, FileOutputStream streamOut) throws IOException {
        Sheet sheet = workbook.createSheet();
        sheet.setColumnWidth(0,2000);
        sheet.setColumnWidth(1,2000);
        sheet.setColumnWidth(2,2000);
        sheet.setColumnWidth(3,2000);
        sheet.setColumnWidth(4,2000);
        sheet.setColumnWidth(5,2000);
        sheet.setColumnWidth(6,2000);
        sheet.setColumnWidth(7,2000);
        sheet.setColumnWidth(8,2000);
        int startRowIndex = 0;
        int startColumnIndex = 0;
        Row row = sheet.createRow(startRowIndex);
        startRowIndex++;
        Cell cell = row.createCell(startColumnIndex);
        startColumnIndex++;
        cell.setCellValue("Batch Name : "+cbReportWrapper.getCashBudgetBatch().getName());
        cell = row.createCell(startColumnIndex);
        startColumnIndex++;
        cell.setCellValue("From : "+cbReportWrapper.getDayDtos().get(0).getEventDate());
        cell = row.createCell(startColumnIndex);
        startColumnIndex++;
        cell.setCellValue("To : "+cbReportWrapper.getDayDtos().get(cbReportWrapper.getDayDtos().size() - 1).getEventDate());
        createEmptyLine(sheet, startRowIndex, getCellStyle(workbook,"noborder",false,"left"), 8);
        startRowIndex++;
        startColumnIndex = 0;
        row = sheet.createRow(startRowIndex);
        startRowIndex++;
        CellStyle colorStyle = getCellStyle(workbook,"noborder",false,"left");
        colorStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        colorStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        createCellAndSetValue(row,startColumnIndex," Date", colorStyle, SXSSFCell.CELL_TYPE_STRING);
        startColumnIndex++;
        int startCol = 0;
        int endCol = 0;
        for(DayDTO dayDTO: cbReportWrapper.getDayDtos()) {
            startCol = startColumnIndex;
            createCellAndSetValue(row,startColumnIndex,dayDTO.getEventDate()+"", getNoStyleCell(workbook), SXSSFCell.CELL_TYPE_STRING);
            startColumnIndex++;
            row.createCell(startColumnIndex);
            startColumnIndex++;
            row.createCell(startColumnIndex);
            endCol = startColumnIndex;
            startColumnIndex++;
            setMerge(sheet,startRowIndex - 1, startRowIndex - 1,startCol,endCol);
        }
        workbook.write(streamOut);
    }

    private void createEmptyLine(Sheet worksheet, int startRowIndex, CellStyle cellStyle, Integer columnCount) {
        Row row;
        int startColumnIndex;
        row = worksheet.createRow(startRowIndex);
        startColumnIndex = 0;
        for (int j = 1; j < columnCount; j++) {
            createCellAndSetValue(row,startColumnIndex,"",cellStyle,SXSSFCell.CELL_TYPE_STRING);
            startColumnIndex = startColumnIndex + 1;
        }
    }

    private void createCellAndSetValue(Row row, int startColumnIndex, String data, CellStyle cellStyle, Integer cellType) {
        Cell cell = row.createCell(startColumnIndex);
        cell.setCellValue(data);
        cell.setCellType(cellType);
        cellStyle.setWrapText(true);
        cell.setCellStyle(cellStyle);
    }

    public CellStyle getNoStyleCell(Workbook workbook) {
        return workbook.createCellStyle();
    }

    public CellStyle getCellStyle(Workbook workbook,
                                      String borderSide,
                                      boolean boldflag,
                                      String align) {
        // 4.To create header style
        CellStyle headerCellStyle = workbook.createCellStyle();
       // headerCellStyle.setFillBackgroundColor(IndexedColors.RED.getIndex());
        // 5.To create font style
        Font boldFont = workbook.createFont();
        boldFont.setFontName("Calibri");
        boldFont.setFontHeightInPoints((short) 11);
        //boldFont.setColor(HSSFColor.WHITE.index);
        if(boldflag) {
            boldFont.setBoldweight(Short.parseShort("1"));
        }
        if(StringUtils.isNotEmpty(borderSide)) {
            if("all".equalsIgnoreCase(borderSide)) {
                headerCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
                headerCellStyle.setBorderTop(CellStyle.BORDER_THIN);
                headerCellStyle.setBorderRight(CellStyle.BORDER_THIN);
                headerCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            }
            else if("left".equalsIgnoreCase(borderSide)) {
                headerCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            }
            else if("right".equalsIgnoreCase(borderSide)) {
                headerCellStyle.setBorderRight(CellStyle.BORDER_THIN);
            }
            else if("top".equalsIgnoreCase(borderSide)) {
                headerCellStyle.setBorderTop(CellStyle.BORDER_THIN);
            }
            else if("bottom".equalsIgnoreCase(borderSide)) {
                headerCellStyle.setBorderLeft(CellStyle.BORDER_NONE);
                headerCellStyle.setBorderRight(CellStyle.BORDER_NONE);
                headerCellStyle.setBorderTop(CellStyle.BORDER_NONE);
                headerCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            }
            else if("noborder".equalsIgnoreCase(borderSide)) {
                headerCellStyle.setBorderLeft(CellStyle.BORDER_NONE);
                headerCellStyle.setBorderRight(CellStyle.BORDER_NONE);
                headerCellStyle.setBorderTop(CellStyle.BORDER_NONE);
                headerCellStyle.setBorderBottom(CellStyle.BORDER_NONE);
            }
        }
        headerCellStyle.setFont(boldFont);
        if(StringUtils.isNotEmpty(align)) {
            if ("center".equalsIgnoreCase(align)) {
                headerCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
            }
            else if("left".equalsIgnoreCase(align)) {
                headerCellStyle.setAlignment(CellStyle.ALIGN_LEFT);
            }
            else if("right".equalsIgnoreCase(align)) {
                headerCellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            }
        }
        return headerCellStyle;
    }

    public void setMerge(Sheet sheet, int startRow, int endRow, int startCol, int endCol) {
        CellRangeAddress cellMerge = new CellRangeAddress(startRow, endRow, startCol, endCol);
        sheet.addMergedRegion(cellMerge);
    }





