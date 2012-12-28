package org.apertereports.ui;

/**
 * Interface containing method for close operation notification
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public interface CloseListener {

    /**
     * Invoked when a kind of resource is closed
     */
    public void closed();
}
