package com.importtax.server.dao.impl;

import com.importtax.server.dao.ImportItemDao;
import com.importtax.server.model.ImportItem;
import jakarta.persistence.TypedQuery;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class ImportItemDaoImpl extends GenericDaoImpl<ImportItem> implements ImportItemDao {

    public ImportItemDaoImpl() {
        super(ImportItem.class);
    }

    public ImportItemDaoImpl(SessionFactory sessionFactory) {
        super(ImportItem.class, sessionFactory);
    }

    @Override
    public ImportItem saveItem(ImportItem item) {
        return save(item);
    }

    @Override
    public ImportItem updateItem(ImportItem item) {
        return update(item);
    }

    @Override
    public void deleteItem(ImportItem item) {
        delete(item);
    }

    @Override
    public List<ImportItem> findAllItems() {
        return executeReadOnly(session -> session.createQuery(
                "select i from ImportItem i left join fetch i.user order by i.importDate desc, i.itemId desc",
                ImportItem.class).getResultList(), "findAllItems");
    }

    @Override
    public Optional<ImportItem> findItemById(Long itemId) {
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    "select i from ImportItem i left join fetch i.user where i.itemId = :itemId",
                    ImportItem.class);
            query.setParameter("itemId", itemId);
            return query.getResultStream().findFirst();
        }, "findItemById");
    }

    @Override
    public List<ImportItem> searchItemsByName(String itemName) {
        return searchByItemName(itemName);
    }

    @Override
    public List<ImportItem> findItemsByStatus(String status) {
        return findByStatus(status);
    }

    @Override
    public List<ImportItem> findItemsByUser(Long userId) {
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    "select i from ImportItem i left join fetch i.user "
                            + "where i.user.userId = :userId "
                            + "order by i.importDate desc, i.itemId desc",
                    ImportItem.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        }, "findItemsByUser");
    }

    @Override
    public List<ImportItem> findByStatus(String status) {
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    "select i from ImportItem i left join fetch i.user "
                            + "where i.status = :status order by i.importDate desc, i.itemId desc",
                    ImportItem.class);
            query.setParameter("status", status);
            return query.getResultList();
        }, "findByStatus");
    }

    @Override
    public List<ImportItem> searchByItemName(String itemName) {
        String searchTerm = itemName == null ? "" : itemName.trim().toLowerCase();
        return executeReadOnly(session -> {
            TypedQuery<ImportItem> query = session.createQuery(
                    "select i from ImportItem i left join fetch i.user "
                            + "where lower(i.itemName) like :itemName "
                            + "order by i.itemName",
                    ImportItem.class);
            query.setParameter("itemName", "%" + searchTerm + "%");
            return query.getResultList();
        }, "searchByItemName");
    }

    @Override
    public long countAllItems() {
        return executeReadOnly(session -> session.createQuery(
                "select count(i) from ImportItem i", Long.class).getSingleResult(), "countAllItems");
    }

    @Override
    public long countByStatus(String status) {
        return executeReadOnly(session -> session.createQuery(
                        "select count(i) from ImportItem i where upper(i.status) = upper(:status)", Long.class)
                .setParameter("status", status)
                .getSingleResult(), "countByStatus");
    }

    @Override
    public List<ImportItem> findRecentItems(int limit) {
        int max = Math.max(1, Math.min(limit, 20));
        return executeReadOnly(session -> session.createQuery(
                        "select i from ImportItem i left join fetch i.user "
                                + "order by i.importDate desc, i.itemId desc",
                        ImportItem.class)
                .setMaxResults(max)
                .getResultList(), "findRecentItems");
    }
}
