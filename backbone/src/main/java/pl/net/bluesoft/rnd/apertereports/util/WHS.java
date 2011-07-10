package pl.net.bluesoft.rnd.apertereports.util;

import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * This generic class handles the opening and closing Hibernate session.
 * It can also wrap an invocation into transaction. Designed to be used as an inner anonymous class.
 */
public abstract class WHS<resultType> {
    /**
     * A Hibernate session retrieved from {@link SQLUtil}.
     */
    protected Session sess;
    /**
     * Indicates whether to use a transaction or not.
     */
    private boolean transaction = true;

    public WHS() {
    }

    public WHS(boolean transaction) {
        this.transaction = transaction;
    }

    /**
     * This method should be implemented to use a current Hibernate session.
     *
     * @return Returns a result of type specified by the generic parameter.
     */
    public abstract resultType lambda();

    /**
     * The main workhorse of this class. Retrieves the session from {@link SQLUtil} and wraps the invocation of
     * {@link #lambda()} into transaction.
     *
     * @return The object returned by {@link #lambda()}
     */
    public resultType p() {
        sess = SQLUtil.getSession();
        Transaction tx = null;
        try {

            if (transaction) {
                tx = sess.beginTransaction();
            }
            resultType res = lambda();
            if (transaction) {
                tx.commit();
            }
            return res;
        }
        catch (RuntimeException e) {
            if (tx != null) {
                tx.rollback();
            }
            throw e;
        }
        finally {
            sess.close();
            sess = null;
        }

    }

}
