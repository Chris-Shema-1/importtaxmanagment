package com.importtax.server.util;

import com.importtax.server.model.ImportItem;
import com.importtax.server.model.Invoice;
import com.importtax.server.model.Notification;
import com.importtax.server.model.Payment;
import com.importtax.server.model.Tax;
import com.importtax.server.model.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DatabaseSeeder {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSeeder.class);

    private DatabaseSeeder() {
    }

    public static void seedIfNeeded() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            if (hasExistingData(session)) {
                LOGGER.info("Seed data skipped because the database already contains records.");
                return;
            }

            Transaction transaction = session.beginTransaction();
            try {
                List<User> users = createUsers();
                users.forEach(session::persist);

                createTaxes().forEach(session::persist);
                createImportItems(users).forEach(session::persist);
                createInvoices().forEach(session::persist);
                createNotifications().forEach(session::persist);

                transaction.commit();
                LOGGER.info("Seeded the database with demo users, taxes, import items, invoices, payments, and notifications.");
            } catch (RuntimeException exception) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    private static boolean hasExistingData(Session session) {
        Long userCount = session.createSelectionQuery("select count(u) from User u", Long.class).getSingleResult();
        Long taxCount = session.createSelectionQuery("select count(t) from Tax t", Long.class).getSingleResult();
        Long importItemCount = session.createSelectionQuery("select count(i) from ImportItem i", Long.class).getSingleResult();
        Long invoiceCount = session.createSelectionQuery("select count(i) from Invoice i", Long.class).getSingleResult();
        Long paymentCount = session.createSelectionQuery("select count(p) from Payment p", Long.class).getSingleResult();
        Long notificationCount = session.createSelectionQuery("select count(n) from Notification n", Long.class).getSingleResult();

        return userCount > 0
                || taxCount > 0
                || importItemCount > 0
                || invoiceCount > 0
                || paymentCount > 0
                || notificationCount > 0;
    }

    private static List<User> createUsers() {
        return List.of(
                new User("Admin User", "admin@importtax.com", "admin", "admin123", "ADMIN", LocalDate.of(2024, 1, 10)),
                new User("Maria Santos", "maria@importtax.com", "maria", "maria123", "MANAGER", LocalDate.of(2024, 2, 14)),
                new User("James Okonkwo", "james@importtax.com", "james", "james123", "OFFICER", LocalDate.of(2024, 3, 1)),
                new User("Priya Nair", "priya@importtax.com", "priya", "priya123", "OFFICER", LocalDate.of(2024, 3, 15)),
                new User("Carlos Mendez", "carlos@importtax.com", "carlos", "carlos123", "VIEWER", LocalDate.of(2024, 4, 20)),
                new User("Aisha Kamara", "aisha@importtax.com", "aisha", "aisha123", "MANAGER", LocalDate.of(2024, 5, 5)),
                new User("Tom Eriksson", "tom@importtax.com", "tom", "tom123", "OFFICER", LocalDate.of(2024, 6, 11)),
                new User("Fatima Al-Hassan", "fatima@importtax.com", "fatima", "fatima123", "VIEWER", LocalDate.of(2024, 7, 22))
        );
    }

    private static List<Tax> createTaxes() {
        return List.of(
                new Tax("Standard Import Duty", new BigDecimal("15.00"), "Default duty applied to most imported goods"),
                new Tax("Electronics Surcharge", new BigDecimal("20.00"), "Additional levy on consumer electronics"),
                new Tax("Agricultural Exemption", new BigDecimal("5.00"), "Reduced rate for agricultural equipment"),
                new Tax("Luxury Goods Tax", new BigDecimal("35.00"), "High-value items such as jewellery and watches"),
                new Tax("Pharmaceutical Relief", new BigDecimal("2.50"), "Medicines and medical devices"),
                new Tax("Textile Import Duty", new BigDecimal("12.00"), "Clothing, fabrics and related materials"),
                new Tax("Automotive Parts Duty", new BigDecimal("18.00"), "Vehicle components and spare parts"),
                new Tax("Chemical Products Tax", new BigDecimal("22.00"), "Industrial and consumer chemicals"),
                new Tax("Food and Beverage Duty", new BigDecimal("8.00"), "Processed food and beverages"),
                new Tax("Raw Materials Relief", new BigDecimal("3.00"), "Unprocessed raw materials for manufacturing")
        );
    }

    private static List<ImportItem> createImportItems(List<User> users) {
        return List.of(
                importItem(users, 0, "Samsung 65-inch QLED TV", "Electronics", "4K Smart TV with HDR", 50,
                        "1200.00", "South Korea", "TechWorld Imports", "20.00", "12000.00", "2026-01-05", "CLEARED"),
                importItem(users, 1, "iPhone 15 Pro Max", "Electronics", "Apple flagship smartphone", 200,
                        "1099.00", "China", "AppleZone Ltd", "20.00", "43960.00", "2026-01-12", "CLEARED"),
                importItem(users, 2, "Toyota Camry Parts Kit", "Automotive", "OEM engine and body parts", 30,
                        "3500.00", "Japan", "AutoParts Global", "18.00", "18900.00", "2026-01-20", "CLEARED"),
                importItem(users, 3, "Organic Cotton Fabric", "Textiles", "100% organic, 200 GSM", 500,
                        "12.50", "India", "FabricHouse Co.", "12.00", "750.00", "2026-02-03", "CLEARED"),
                importItem(users, 4, "Paracetamol 500mg Bulk", "Pharmaceuticals", "Generic paracetamol tablets", 1000,
                        "0.80", "Germany", "MedSupply GmbH", "2.50", "20.00", "2026-02-10", "CLEARED"),
                importItem(users, 5, "Arabica Coffee Beans", "Food and Beverage", "Premium single-origin beans", 300,
                        "8.00", "Ethiopia", "CoffeeTrade Africa", "8.00", "192.00", "2026-02-18", "CLEARED"),
                importItem(users, 6, "Industrial Solvent X200", "Chemicals", "High-purity industrial solvent", 100,
                        "45.00", "Netherlands", "ChemImport BV", "22.00", "990.00", "2026-03-01", "CLEARED"),
                importItem(users, 7, "Rolex Submariner Watch", "Luxury Goods", "Swiss luxury dive watch", 10,
                        "12000.00", "Switzerland", "LuxuryTime Imports", "35.00", "42000.00", "2026-03-08", "CLEARED"),
                importItem(users, 0, "Wheat Harvester Attachment", "Agriculture", "Combine harvester header unit", 5,
                        "8500.00", "USA", "AgriMach International", "5.00", "2125.00", "2026-03-15", "CLEARED"),
                importItem(users, 1, "Dell XPS 15 Laptops", "Electronics", "High-performance business laptops", 100,
                        "1800.00", "China", "TechWorld Imports", "20.00", "36000.00", "2026-03-22", "CLEARED"),
                importItem(users, 2, "Silk Saree Collection", "Textiles", "Hand-woven pure silk sarees", 200,
                        "95.00", "India", "FabricHouse Co.", "12.00", "2280.00", "2026-04-01", "CLEARED"),
                importItem(users, 3, "BMW 3-Series Brake Pads", "Automotive", "OEM brake pad set", 150,
                        "85.00", "Germany", "AutoParts Global", "18.00", "2295.00", "2026-04-10", "CLEARED")
        );
    }

    private static ImportItem importItem(List<User> users, int userIndex, String itemName, String category,
                                         String description, int quantity, String unitPrice, String origin,
                                         String importerName, String taxRate, String totalTax, String importDate,
                                         String status) {
        return new ImportItem(
                itemName,
                category,
                description,
                quantity,
                new BigDecimal(unitPrice),
                origin,
                importerName,
                new BigDecimal(taxRate),
                new BigDecimal(totalTax),
                LocalDate.parse(importDate),
                status,
                users.get(userIndex % users.size())
        );
    }

    private static List<Invoice> createInvoices() {
        return List.of(
                invoice("INV-2026-001", "12000.00", "2026-01-06", "12000.00", "2026-01-08", "BANK_TRANSFER", "COMPLETED"),
                invoice("INV-2026-002", "43960.00", "2026-01-13", "43960.00", "2026-01-15", "BANK_TRANSFER", "COMPLETED"),
                invoice("INV-2026-003", "18900.00", "2026-01-21", "18900.00", "2026-01-23", "CREDIT_CARD", "COMPLETED"),
                invoice("INV-2026-004", "750.00", "2026-02-04", "750.00", "2026-02-06", "CASH", "COMPLETED"),
                invoice("INV-2026-005", "20.00", "2026-02-11", "20.00", "2026-02-13", "CASH", "COMPLETED"),
                invoice("INV-2026-006", "192.00", "2026-02-19", "192.00", "2026-02-21", "BANK_TRANSFER", "COMPLETED"),
                invoice("INV-2026-007", "990.00", "2026-03-02", "990.00", "2026-03-04", "CHEQUE", "COMPLETED"),
                invoice("INV-2026-008", "42000.00", "2026-03-09", "42000.00", "2026-03-11", "BANK_TRANSFER", "COMPLETED"),
                invoice("INV-2026-009", "2125.00", "2026-03-16", "2125.00", "2026-03-18", "BANK_TRANSFER", "COMPLETED"),
                invoice("INV-2026-010", "36000.00", "2026-03-23", "36000.00", "2026-03-25", "CREDIT_CARD", "PENDING")
        );
    }

    private static Invoice invoice(String invoiceNumber, String invoiceAmount, String issueDate, String paymentAmount,
                                   String paymentDate, String paymentMethod, String paymentStatus) {
        Invoice invoice = new Invoice(invoiceNumber, new BigDecimal(invoiceAmount), LocalDate.parse(issueDate));
        Payment payment = new Payment(new BigDecimal(paymentAmount), LocalDate.parse(paymentDate),
                paymentMethod, paymentStatus, invoice);
        invoice.setPayment(payment);
        return invoice;
    }

    private static List<Notification> createNotifications() {
        return List.of(
                new Notification("Import batch INV-2026-001 cleared by customs.", "INFO", "admin",
                        LocalDate.of(2026, 1, 8), "READ"),
                new Notification("Payment of 43,960 received for INV-2026-002.", "INFO", "maria",
                        LocalDate.of(2026, 1, 15), "READ"),
                new Notification("INV-2026-003 payment confirmed via credit card.", "INFO", "james",
                        LocalDate.of(2026, 1, 23), "READ"),
                new Notification("Tax rate for Electronics updated to 20 percent.", "SYSTEM", "admin",
                        LocalDate.of(2026, 2, 1), "READ"),
                new Notification("Paracetamol bulk shipment cleared with low tax applied.", "INFO", "priya",
                        LocalDate.of(2026, 2, 13), "READ"),
                new Notification("New user Carlos Mendez registered as VIEWER.", "SYSTEM", "admin",
                        LocalDate.of(2026, 2, 20), "READ"),
                new Notification("Luxury goods shipment requires additional documentation.", "WARNING", "admin",
                        LocalDate.of(2026, 3, 8), "READ"),
                new Notification("INV-2026-008 payment of 42,000 processed successfully.", "INFO", "aisha",
                        LocalDate.of(2026, 3, 11), "READ"),
                new Notification("System backup completed successfully.", "SYSTEM", "admin",
                        LocalDate.of(2026, 3, 20), "READ"),
                new Notification("Diamond jewellery shipment placed on HOLD awaiting valuation.", "ALERT", "admin",
                        LocalDate.of(2026, 5, 9), "UNREAD"),
                new Notification("Irrigation pump import pending customs clearance.", "WARNING", "james",
                        LocalDate.of(2026, 5, 14), "UNREAD"),
                new Notification("Monthly tax report generated for May 2026.", "SYSTEM", "admin",
                        LocalDate.of(2026, 5, 15), "READ")
        );
    }
}
