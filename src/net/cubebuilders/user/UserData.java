package net.cubebuilders.user;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.UUID;
import net.cubebuilders.user.UserFriend.FriendStatus;

public class UserData {

	private static final Punishment[] zeroPunishments = new Punishment[0];
	private static final UserDonation[] zeroDonations = new UserDonation[0];
	private static final NameHistory[] zeroHistory = new NameHistory[0];
	private static final UserFriend[] zeroFriends = new UserFriend[0];

	public String staffRank = null;
	public boolean hiddenStaff = false;
	public ArrayList<Punishment> punishments = null;
	public ArrayList<UserDonation> donations = null;
	public ArrayList<UserFriend> friends = null;
	public NameHistory[] nameHistory = null;
	public boolean chatCensor = true;
	public String nickname = null;
	public String teamSpeakID = null;
	public boolean permanentlyBanned = false;
	public String banMessageString = null;
	public boolean isMember = false;
	public int storeCredit = 0;
	public String timezone = null;
	private transient TimeZone jTimeZone = null;
	public long lastLogoutAll = 0L;
	public String skinUrl = null;

	public TimeZone getTimeZone() {
		if (timezone == null) {
			jTimeZone = null;
			return null;
		}
		try {
			if (jTimeZone == null || !jTimeZone.getID().equals(timezone)) {
				jTimeZone = null;
				jTimeZone = TimeZone.getTimeZone(timezone);
			}
		} catch (Exception e) {
		}
		return jTimeZone;
	}

	// <editor-fold defaultstate="collapsed" desc="prepare() method">
	boolean prepare(UUID uuid) {
		boolean needSave = false;
		if (punishments == null) {
			punishments = new ArrayList<>();
		}
		if (donations == null) {
			donations = new ArrayList<>();
		}
		if (nameHistory == null) {
			nameHistory = zeroHistory;
		}
		if (friends == null) {
			friends = new ArrayList<>();
		}
		int pSize = punishments.size();
		for (int i = 0; i < pSize; i++) {
			Punishment punishment = punishments.get(i);
			if (punishment == null) {
				punishments.remove(i);
				i -= 1;
				pSize -= 1;
				continue;
			}
			punishment.issuedTo = uuid;
		}
		punishments.sort((Punishment o1, Punishment o2) -> {
			if (o1.time < o2.time) {
				return -1;
			} else if (o1.time > o2.time) {
				return 1;
			} else {
				return 0;
			}
		});
		donations.sort((UserDonation o1, UserDonation o2) -> {
			if (o1.startTime < o2.startTime) {
				return -1;
			} else if (o1.startTime > o2.startTime) {
				return 1;
			} else {
				return 0;
			}
		});
		UserDonation latestRank = getLatestRank();
		if (latestRank != null) {
			long now = System.currentTimeMillis();
			switch (latestRank.rank) {
				case "iron": {
					UserDonation newDonation = new UserDonation();
					newDonation.rank = "pluspaid";
					newDonation.itemType = "rank";
					newDonation.paymentRef = "ConvertedRank";
					newDonation.startTime = now;
					newDonation.endTime = latestRank.endTime;
					newDonation.paypalAmount = 0;
					newDonation.creditAmount = 0;
					storeCredit += 500;
					donations.add(newDonation);
					needSave = true;
				}
				break;
				case "gold": {
					UserDonation newDonation = new UserDonation();
					newDonation.rank = "pluspaid";
					newDonation.itemType = "rank";
					newDonation.paymentRef = "ConvertedRank";
					newDonation.startTime = now;
					newDonation.endTime = latestRank.endTime;
					newDonation.paypalAmount = 0;
					newDonation.creditAmount = 0;
					storeCredit += 1500;
					donations.add(newDonation);
					needSave = true;
				}
				break;
				case "diamond": {
					UserDonation newDonation = new UserDonation();
					newDonation.rank = "pluspaid";
					newDonation.itemType = "rank";
					newDonation.paymentRef = "ConvertedRank";
					newDonation.startTime = now;
					newDonation.endTime = latestRank.endTime;
					newDonation.paypalAmount = 0;
					newDonation.creditAmount = 0;
					storeCredit += 3500;
					donations.add(newDonation);
					needSave = true;
				}
				break;
				case "emerald": {
					UserDonation newDonation = new UserDonation();
					newDonation.rank = "pluspaid";
					newDonation.itemType = "rank";
					newDonation.paymentRef = "ConvertedRank";
					newDonation.startTime = now;
					newDonation.endTime = latestRank.endTime;
					newDonation.paypalAmount = 0;
					newDonation.creditAmount = 0;
					storeCredit += 7500;
					donations.add(newDonation);
					needSave = true;
				}
				break;
			}
		}
		return needSave;
	}
	// </editor-fold>

	// <editor-fold defaultstate="collapsed" desc="Previous Names Fetcher">
	public void updatePreviousNames(final CBUser user, final UUID player, final String username) {
	}
	// </editor-fold>

	public void setFriendStatus(UUID uuid, FriendStatus status, long time) {
		for (UserFriend friend : friends) {
			if (friend.uuid.equals(uuid)) {
				if (status == FriendStatus.NOT_FRIENDS) {
					friends.remove(friend);
					return;
				}
				friend.status = status;
				friend.time = time;
				return;
			}
		}
		if (status == FriendStatus.NOT_FRIENDS) {
			return;
		}
		UserFriend friend = new UserFriend();
		friend.uuid = uuid;
		friend.status = status;
		friend.time = time;
	}

	public Punishment[] getPunishments() {
		return punishments.toArray(zeroPunishments);
	}

	public UserDonation[] getDonations() {
		return donations.toArray(zeroDonations);
	}

	public UserFriend[] getFriends() {
		return friends.toArray(zeroFriends);
	}

	public UserDonation getLatestRank() {
		UserDonation latest = null;
		for (UserDonation donation : donations) {
			if (!donation.cancelled && donation.itemType.equals("rank")) {
				latest = donation;
			}
		}
		return latest;
	}
}
