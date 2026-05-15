package com.importtax.server.rmi;

import com.importtax.server.model.ImportItem;

import java.rmi.RemoteException;
import java.util.List;

public interface ImportItemService extends RemoteCrudService<ImportItem> {

    ImportItem saveItem(ImportItem item, Long userId) throws RemoteException;

    ImportItem updateItem(ImportItem item, Long userId) throws RemoteException;

    void deleteItem(Long itemId) throws RemoteException;

    List<ImportItem> findAllItems() throws RemoteException;

    ImportItem findItemById(Long itemId) throws RemoteException;

    List<ImportItem> searchItemsByName(String itemName) throws RemoteException;

    List<ImportItem> findItemsByStatus(String status) throws RemoteException;

    List<ImportItem> findItemsByUser(Long userId) throws RemoteException;

    List<ImportItem> findByStatus(String status) throws RemoteException;

    List<ImportItem> searchByItemName(String itemName) throws RemoteException;
}
