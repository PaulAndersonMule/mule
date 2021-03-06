/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.module.db.internal.domain.transaction;

import org.mule.api.MuleContext;
import org.mule.api.transaction.TransactionException;
import org.mule.config.i18n.CoreMessages;
import org.mule.module.db.internal.i18n.DbMessages;
import org.mule.transaction.AbstractSingleResourceTransaction;
import org.mule.transaction.IllegalTransactionStateException;
import org.mule.transaction.TransactionRollbackException;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Provides a database transactions
 */
public class DbTransaction extends AbstractSingleResourceTransaction
{

    public DbTransaction(MuleContext muleContext)
    {
        super(muleContext);
    }

    @Override
    public void bindResource(Object key, Object resource) throws TransactionException
    {
        if (!(key instanceof DataSource) || !(resource instanceof Connection))
        {
            throw new IllegalTransactionStateException(
                    CoreMessages.transactionCanOnlyBindToResources("javax.sql.DataSource/java.sql.Connection"));
        }
        Connection con = (Connection) resource;
        try
        {
            if (con.getAutoCommit())
            {
                con.setAutoCommit(false);
            }
        }
        catch (SQLException e)
        {
            throw new TransactionException(DbMessages.transactionSetAutoCommitFailed(), e);
        }
        super.bindResource(key, resource);
    }

    @Override
    protected void doBegin() throws TransactionException
    {
        // Do nothing
    }

    @Override
    protected void doCommit() throws TransactionException
    {
        if (resource == null)
        {
            logger.warn(CoreMessages.commitTxButNoResource(this));
            return;
        }

        try
        {
            ((Connection) resource).commit();
            ((Connection) resource).close();
        }
        catch (SQLException e)
        {
            throw new TransactionException(CoreMessages.transactionCommitFailed(), e);
        }
    }

    @Override
    protected void doRollback() throws TransactionException
    {
        if (resource == null)
        {
            logger.warn(CoreMessages.rollbackTxButNoResource(this));
            return;
        }

        try
        {
            ((Connection) resource).rollback();
            ((Connection) resource).close();
        }
        catch (SQLException e)
        {
            throw new TransactionRollbackException(CoreMessages.transactionRollbackFailed(), e);
        }
    }

    @Override
    protected Class<Connection> getResourceType()
    {
        return Connection.class;
    }

    @Override
    protected Class<DataSource> getKeyType()
    {
        return DataSource.class;
    }
}