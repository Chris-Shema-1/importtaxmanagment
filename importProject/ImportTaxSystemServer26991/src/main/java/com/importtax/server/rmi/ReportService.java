package com.importtax.server.rmi;

import com.importtax.server.model.ImportStatusCounts;
import com.importtax.server.model.MonthlyTaxSummary;
import com.importtax.server.model.RecentActivityEntry;
import com.importtax.server.model.ReportSummary;
import com.importtax.server.model.ReportTableRow;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ReportService extends Remote {

    ReportSummary getDashboardSummary() throws RemoteException;

    ImportStatusCounts getImportStatusCounts() throws RemoteException;

    List<MonthlyTaxSummary> getMonthlyTaxSummary() throws RemoteException;

    List<RecentActivityEntry> getRecentActivities() throws RemoteException;

    List<ReportTableRow> getReportTableRows() throws RemoteException;
}
