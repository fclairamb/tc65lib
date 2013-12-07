/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.javacint.i2c.mma7660fc;

/**
 *
 * @author Florent
 */
public interface MMA7660FCListener {

	public void shakeDetected(int x, int y, int z);

	public void tapDetected();

	public void baFroDetected(BackFront backFront);

	public void polaDetected(Polarity polarity);
}
