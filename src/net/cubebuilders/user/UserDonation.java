package net.cubebuilders.user;

public class UserDonation {

	public String rank;
	public String paymentRef;
	public String itemType;
	public long startTime;
	public long endTime = 0L;
	public boolean activeIngame = false;
	public boolean activeForum = false;
	public boolean cancelled = false;
	public long chargebackDate = -1L;
	public int paypalAmount = 0;
	public int paypalFeeAmount = 0;
	public int creditAmount = 0;
	public int grandfatherFrequency = 0;
	public int grandfatherRate = 0;

	public boolean isActive() {
		return isActive(System.currentTimeMillis());
	}

	public boolean isActive(long time) {
		return !cancelled && time >= startTime && (time < endTime || endTime <= 0L);
	}

	public int getTotalPaid() {
		return paypalAmount + creditAmount;
	}
}
