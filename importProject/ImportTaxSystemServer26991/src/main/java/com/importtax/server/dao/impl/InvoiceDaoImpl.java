package com.importtax.server.dao.impl;

import com.importtax.server.dao.InvoiceDao;
import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.MonthlyTaxSummary;
import com.importtax.server.util.InvoiceRmiMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InvoiceDaoImpl extends GenericDaoImpl<Invoice> implements InvoiceDao {

    public InvoiceDaoImpl() {
        super(Invoice.class);
    }

    public InvoiceDaoImpl(SessionFactory sessionFactory) {
        super(Invoice.class, sessionFactory);
    }

    private static final String INVOICE_LIST_FETCH =
            "select distinct i from Invoice i left join fetch i.importItem imp ";

    @Override
    public List<Invoice> findAll() {
        return executeReadOnly(session -> {
            List<Invoice> loaded = session.createQuery(
                            INVOICE_LIST_FETCH + "order by i.issueDate desc, i.invoiceId desc",
                            Invoice.class)
                    .getResultList();
            List<Invoice> safe = new ArrayList<>(loaded.size());
            for (Invoice invoice : loaded) {
                safe.add(InvoiceRmiMapper.toSafeInvoice(invoice));
            }
            return safe;
        }, "findAll");
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return executeReadOnly(session -> {
            List<Invoice> list = session.createQuery(
                            INVOICE_LIST_FETCH + "where i.invoiceId = :id",
                            Invoice.class)
                    .setParameter("id", id)
                    .setMaxResults(1)
                    .getResultList();
            if (list.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(InvoiceRmiMapper.toSafeInvoice(list.get(0)));
        }, "findById");
    }

    @Override
    public Optional<Long> findImportItemIdByInvoiceId(Long invoiceId) {
        return executeReadOnly(session -> {
            Long itemId = session.createQuery(
                            "select i.importItem.itemId from Invoice i where i.invoiceId = :invoiceId",
                            Long.class)
                    .setParameter("invoiceId", invoiceId)
                    .uniqueResult();
            return Optional.ofNullable(itemId);
        }, "findImportItemIdByInvoiceId");
    }

    @Override
    public Optional<String> findInvoiceNumberByImportItemId(Long itemId) {
        return executeReadOnly(session -> {
            String number = session.createQuery(
                            "select i.invoiceNumber from Invoice i where i.importItem.itemId = :itemId",
                            String.class)
                    .setParameter("itemId", itemId)
                    .uniqueResult();
            return Optional.ofNullable(number);
        }, "findInvoiceNumberByImportItemId");
    }

    @Override
    public Optional<Invoice> createInvoiceForImportItemIfAbsent(ImportItem importItem) {
        if (importItem == null || importItem.getItemId() == null) {
            throw new IllegalArgumentException("Persisted import item with ID is required for invoice generation");
        }
        Long itemId = importItem.getItemId();
        return executeInTransaction(session -> {
            Long existing = session.createQuery(
                            "select count(i) from Invoice i where i.importItem.itemId = :itemId",
                            Long.class)
                    .setParameter("itemId", itemId)
                    .getSingleResult();
            if (existing != null && existing > 0) {
                return Optional.empty();
            }

            ImportItem managedItem = session.find(ImportItem.class, itemId);
            if (managedItem == null) {
                throw new IllegalArgumentException("Import item not found");
            }
            if (managedItem.getTotalTax() == null) {
                throw new IllegalArgumentException("Import item total tax is required for invoice generation");
            }

            String invoiceNumber = nextInvoiceNumber(session);
            LocalDate issueDate = managedItem.getImportDate() != null
                    ? managedItem.getImportDate()
                    : LocalDate.now();
            Invoice invoice = new Invoice(
                    invoiceNumber,
                    managedItem.getTotalTax(),
                    issueDate,
                    managedItem);
            session.persist(invoice);
            return Optional.of(invoice);
        }, "createInvoiceForImportItemIfAbsent");
    }

    private static String nextInvoiceNumber(Session session) {
        Long count = session.createQuery("select count(i) from Invoice i", Long.class).getSingleResult();
        long sequence = (count != null ? count : 0L) + 1L;
        int year = LocalDate.now().getYear();
        return String.format("INV-%d-%03d", year, sequence);
    }

    @Override
    public long countInvoices() {
        return executeReadOnly(session -> session.createQuery(
                "select count(i) from Invoice i", Long.class).getSingleResult(), "countInvoices");
    }

    @Override
    public BigDecimal sumTotalTaxAmount() {
        return executeReadOnly(session -> {
            BigDecimal sum = session.createQuery(
                    "select coalesce(sum(i.totalTaxAmount), 0) from Invoice i", BigDecimal.class)
                    .getSingleResult();
            return sum != null ? sum : BigDecimal.ZERO;
        }, "sumTotalTaxAmount");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MonthlyTaxSummary> aggregateTaxByMonth(int maxMonths) {
        int months = Math.max(1, Math.min(maxMonths, 12));
        return executeReadOnly(session -> {
            List<Object[]> rows = session.createQuery(
                            "select i.issueDate, i.totalTaxAmount from Invoice i where i.issueDate is not null",
                            Object[].class)
                    .getResultList();
            Map<YearMonth, BigDecimal> totals = new LinkedHashMap<>();
            for (Object[] row : rows) {
                if (row[0] == null || row[1] == null) {
                    continue;
                }
                java.time.LocalDate date = (java.time.LocalDate) row[0];
                BigDecimal tax = (BigDecimal) row[1];
                YearMonth ym = YearMonth.from(date);
                totals.merge(ym, tax, BigDecimal::add);
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
            List<YearMonth> keys = totals.keySet().stream().sorted().toList();
            int from = Math.max(0, keys.size() - months);
            List<MonthlyTaxSummary> result = new ArrayList<>();
            for (int i = from; i < keys.size(); i++) {
                YearMonth ym = keys.get(i);
                result.add(new MonthlyTaxSummary(ym.format(fmt), totals.get(ym)));
            }
            return result;
        }, "aggregateTaxByMonth");
    }

    @Override
    public List<Invoice> findRecentInvoices(int limit) {
        int max = Math.max(1, Math.min(limit, 20));
        return executeReadOnly(session -> session.createQuery(
                        "select distinct i from Invoice i "
                                + "left join fetch i.importItem item "
                                + "left join fetch item.user "
                                + "order by i.issueDate desc, i.invoiceId desc",
                        Invoice.class)
                .setMaxResults(max)
                .getResultList(), "findRecentInvoices");
    }
}
