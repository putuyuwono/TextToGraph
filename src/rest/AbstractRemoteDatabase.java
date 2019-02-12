/**
 * Copyright (c) 2002-2013 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package rest;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.KernelEventHandler;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.IdGeneratorFactory;
import org.neo4j.kernel.KernelData;
import org.neo4j.kernel.TransactionBuilder;
import org.neo4j.kernel.guard.Guard;
import org.neo4j.kernel.impl.core.KernelPanicEventGenerator;
import org.neo4j.kernel.impl.core.NodeManager;
import org.neo4j.kernel.impl.core.RelationshipTypeHolder;
import org.neo4j.kernel.impl.nioneo.store.StoreId;
import org.neo4j.kernel.impl.persistence.PersistenceSource;
import org.neo4j.kernel.impl.transaction.LockManager;
import org.neo4j.kernel.impl.transaction.XaDataSourceManager;
import org.neo4j.kernel.impl.transaction.xaframework.TxIdGenerator;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.kernel.info.DiagnosticsManager;
import rest.transaction.NullTransaction;

import javax.transaction.TransactionManager;
import java.util.Collection;

abstract class AbstractRemoteDatabase implements GraphDatabaseAPI {
    public Transaction beginTx() {
        return new NullTransaction();
    }

    public <T> TransactionEventHandler<T> registerTransactionEventHandler( TransactionEventHandler<T> tTransactionEventHandler ) {
        throw new UnsupportedOperationException();
    }

    public <T> TransactionEventHandler<T> unregisterTransactionEventHandler( TransactionEventHandler<T> tTransactionEventHandler ) {
        throw new UnsupportedOperationException();
    }

    public KernelEventHandler registerKernelEventHandler( KernelEventHandler kernelEventHandler ) {
        throw new UnsupportedOperationException();
    }

    public KernelEventHandler unregisterKernelEventHandler( KernelEventHandler kernelEventHandler ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Guard getGuard() {
        throw new UnsupportedOperationException();
    }

    @Override
    public KernelPanicEventGenerator getKernelPanicGenerator() {
        throw new UnsupportedOperationException();
    }

    public <T> Collection<T> getManagementBeans(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersistenceSource getPersistenceSource() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionBuilder tx() {
        throw new UnsupportedOperationException();
    }

    public <T> T getSingleManagementBean(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public KernelData getKernelData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IdGeneratorFactory getIdGeneratorFactory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RelationshipTypeHolder getRelationshipTypeHolder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringLogger getMessageLog() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DiagnosticsManager getDiagnosticsManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransactionManager getTxManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public XaDataSourceManager getXaDataSourceManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LockManager getLockManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public NodeManager getNodeManager() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
    }

    public StoreId getStoreId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TxIdGenerator getTxIdGenerator() {
        throw new UnsupportedOperationException();
    }

    public DependencyResolver getDependencyResolver() {
        throw new UnsupportedOperationException();
    }
}
