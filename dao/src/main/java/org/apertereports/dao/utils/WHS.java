package org.apertereports.dao.utils;

import javax.naming.InitialContext;

import org.apertereports.common.exception.ARRuntimeException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


/**
 * This generic class handles the opening and closing Hibernate session. It can
 * also wrap an invocation into transaction. Designed to be used as an inner
 * anonymous class.
 *
 * @param <ResultType> Type of result
 */
public abstract class WHS<ResultType> {

    /**
     * A Hibernate session retrieved from {@link SQLUtil}.
     */
    protected Session sess;
   
    /**
     * Indicates whether to use a transaction or not.
     */
    private boolean transaction;

    /**
     * Creates transactional WHS object
     */
    public WHS() {
        this(true);
    }

    /**
     * Creates WHS object. It can be transactional or not
     *
     * @param transaction Determines if WHS instance is transactional or not
     */
    public WHS(boolean transaction) {
        this.transaction = transaction;
    }

    /**
     * This method should be implemented to use a current Hibernate session.
     *
     * @return Returns a result of type specified by the generic parameter.
     */
    public abstract ResultType lambda();

    /**
     * The main workhorse of this class. Retrieves the session from {@link SQLUtil}
     * and wraps the invocation of
     * {@link #lambda()} into transaction.
     * @return The object returned by {@link #lambda()}
     */
    public ResultType p() {
 
        sess = SQLUtil.getSession();
        Transaction tx = null;
        try {

            if (transaction) {
                tx = sess.beginTransaction();
            }
            ResultType res = lambda();
            if (transaction) {
                tx.commit();
            }
            return res;
        } catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
            }
            throw new ARRuntimeException(e);
        } finally {
            sess.close();
            sess = null;
        }

    }
}