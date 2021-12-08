package net.cubebuilders.user;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.UUID;
import net.cubebuilders.user.Punishment.PunishmentAction;

public class CBUser {

	// <editor-fold defaultstate="collapsed" desc="Class initializer">
	static {
		gsonForNetwork = CBUserGsonBuilder.build(false);
		gsonForFile = CBUserGsonBuilder.build(true);
	}
	// </editor-fold>
	private static final Gson gsonForFile;
	private static final Gson gsonForNetwork;

	CBUser prepare() {
		boolean needSave = false;
		if (userData == null) {
			userData = new UserData();
		}
		if (userData.prepare(uuid)) {
			needSave = true;
		}
		if (userId > 0 && email != null && emailVerified) {
			userData.isMember = true;
		}
		if (needSave) {
			saveData();
		}
		return this;
	}

	private static Saver saver = null;

	static void setSaver(Saver saver) {
		CBUser.saver = saver;
	}

	public void saveData() {
		if (saver != null) {
			saver.save(this);
		}
	}

	static class Saver {

		public void save(CBUser user) {
			user.lastSyncWithFilesystem = System.currentTimeMillis();
		}
	}

	private static UUIDConverter uuidConverter = null;

	public static void setUUIDConverter(UUIDConverter converter) {
		CBUser.uuidConverter = converter;
	}

	public static abstract class UUIDConverter {

		public abstract String uuidToUsername(UUID uuid);

		public abstract UUID usernameToUUID(String username);
	}

	CBUser(int userId, UUID uuid) {
		this.lastSyncWithFilesystem = this.registerDate = System.currentTimeMillis();
		this.userId = userId;
		this.uuid = uuid;
	}

	boolean shouldResync(long fsTime) {
		return fsTime > lastSyncWithFilesystem;
	}

	public CBUser copyFrom(CBUser user) {
		if (!user.uuid.equals(uuid)) {
			throw new IllegalArgumentException("Mismatch UUID in CBUser objects.");
		}
		lastSyncWithFilesystem = System.currentTimeMillis();
		if (user.userId != -1) {
			userId = user.userId;
		}
		if (user.registerDate != -1L) {
			registerDate = user.registerDate;
		}
		if (user.password != null) {
			password = user.password;
		}
		if (user.twoFactorSecret != null) {
			twoFactorSecret = user.twoFactorSecret;
		}
		if (user.email != null) {
			email = user.email;
		}
		emailVerified = user.emailVerified;
		emailDidBounce = user.emailDidBounce;
		emailVerificationCode = user.emailVerificationCode;
		subscribedToMailingList = user.subscribedToMailingList;
		subscribedToImportantEmails = user.subscribedToImportantEmails;
		allowEmails = user.allowEmails;
		emailConfirmedDate = user.emailConfirmedDate;
		lastLogin = user.lastLogin;
		lastMissYouEmail = user.lastMissYouEmail;
		missYouGift = user.missYouGift;
		if (user.userData != null) {
			userData = user.userData;
		}
		return this;
	}
	// don't remove saveLock, it's to ensure only one save is occurring at a time.
	final Object saveLock = new Object();
	long lastSyncWithFilesystem = 0L;
	int userId = -1;
	final UUID uuid;
	long registerDate = -1L;
	String password = null;
	String passwordSalt = null;
	String twoFactorSecret = null;
	boolean twoFactorEnabled = false;
	String email = null;
	boolean emailVerified = false;
	boolean emailDidBounce = false;
	String emailVerificationCode = null;
	boolean subscribedToMailingList = false;
	boolean subscribedToImportantEmails = true;
	boolean allowEmails = false;
	long emailConfirmedDate = 0L;
	long lastLogin = 0L;
	long lastMissYouEmail = 0L;
	boolean missYouGift = false;
	UserData userData = null;

	public void login(String username, String ip) {
		lastLogin = System.currentTimeMillis();
		saveData();
		if (missYouGift) {
			long now = System.currentTimeMillis();
			long previousExpire = 0L;
			UserDonation latestRank = getUserData().getLatestRank();
			if (latestRank != null) {
				previousExpire = latestRank.endTime;
			}
			long expire = Math.max(now, previousExpire) + (86400000L * 7L);
			missYouGift = false;
			UserDonation plusPaid = new UserDonation();
			plusPaid.rank = "pluspaid";
			plusPaid.paymentRef = "MissYouGift";
			plusPaid.itemType = "rank";
			plusPaid.startTime = now;
			plusPaid.endTime = expire;
			addDonation(plusPaid);
			UserDonation cubeTokens2K = new UserDonation();
			cubeTokens2K.rank = "cubetokens2K";
			cubeTokens2K.paymentRef = "MissYouGift";
			cubeTokens2K.itemType = "item";
			cubeTokens2K.startTime = now;
			cubeTokens2K.endTime = 0L;
			addDonation(cubeTokens2K);
		}
		userData.updatePreviousNames(this, uuid, username);
	}

	void setUserId(int userId) {
		this.userId = userId;
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername() {
		if (uuidConverter == null) {
			NameHistory[] hist = userData.nameHistory;
			if (hist == null || hist.length == 0) {
				return null;
			}
			return hist[hist.length - 1].getName();
		}
		return uuidConverter.uuidToUsername(uuid);
	}

	public String getEmail() {
		return email;
	}

	public boolean isEmailVerified() {
		return emailVerified;
	}

	public boolean didEmailBounce() {
		return emailDidBounce;
	}

	public String getEmailVerificationCode() {
		return emailVerificationCode;
	}

	public boolean isSubscribedToMailingList() {
		return subscribedToMailingList;
	}

	public boolean isSubscribedToImportantEmails() {
		return subscribedToImportantEmails;
	}

	public boolean allowEmails() {
		return allowEmails;
	}

	public long getLastLogin() {
		return lastLogin;
	}

	public long getEmailConfirmedDate() {
		return emailConfirmedDate;
	}

	public boolean isPasswordNull() {
		return password == null;
	}

	public UUID getUUID() {
		return uuid;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEmailVerified(boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public void setEmailDidBounce(boolean emailDidBounce) {
		this.emailDidBounce = emailDidBounce;
	}

	public void setEmailVerificationCode(String emailVerificationCode) {
		this.emailVerificationCode = emailVerificationCode;
	}

	public void setSubscribedToMailingList(boolean subscribedToMailingList) {
		this.subscribedToMailingList = subscribedToMailingList;
	}

	public void setSubscribedToImportantEmails(boolean subscribedToImportantEmails) {
		this.subscribedToImportantEmails = subscribedToImportantEmails;
	}

	public void setAllowEmails(boolean allowEmails) {
		this.allowEmails = allowEmails;
	}

	public void setEmailConfirmedDate(long emailConfirmedDate) {
		this.emailConfirmedDate = emailConfirmedDate;
	}

	public void setPassword(String password) {
		throw new UnsupportedOperationException("Cannot set password from Bungee.");
	}

	public boolean verifyPassword(String password) {
		throw new UnsupportedOperationException("Cannot verify password from Bungee.");
	}

	public boolean isTwoFactorEnabled() {
		return twoFactorEnabled;
	}

	public void setTwoFactorSecret(String twoFactorSecret) {
		throw new UnsupportedOperationException("Cannot set two factor secret from Bungee.");
	}

	public String getTwoFactorSecret() {
		throw new UnsupportedOperationException("Cannot get two factor secret from Bungee.");
	}

	public long getRegisterDate() {
		return registerDate;
	}

	public void setRegisterDate(long registerDate) {
		this.registerDate = registerDate;
	}

	public boolean isMuted() {
		return getExpiry(PunishmentAction.MUTE) != 0L;
	}

	public boolean isBanned() {
		return getExpiry(PunishmentAction.BAN) != 0L;
	}

	/**
	 * Get expiry time of a punishment action.
	 *
	 * @param action Type of punishment to check.
	 * @return Time in milliseconds after Jan 1 1970. 0 if no punishment active,
	 * or -1 if punishment does not expire.
	 */
	public long getExpiry(PunishmentAction action) {
		if (action == PunishmentAction.WARNING) {
			return 0L;
		}
		long now = System.currentTimeMillis();
		long start = 0L;
		long end = 0L;
		Punishment[] punishments = userData.getPunishments();
		for (Punishment p : punishments) {
			if (p.getAction() != action || p.isCancelled() || p.getStartTime() > now) {
				continue;
			}
			if (p.getLength() == -1L) {
				return -1L;
			}
			if (p.getStartTime() < end) {
				end += p.getLength();
			} else {
				start = p.getStartTime();
				end = start + p.getLength();
			}
		}
		if (end > now) {
			return end;
		}
		return 0L;
	}

	public void addDonation(UserDonation donation) {
		userData.donations.add(donation);
		saveData();
		triggerUpdate(false);
	}

	public void postOffence(Punishment punishment) {
		add:
		{
			for (int i = 0; i < userData.punishments.size(); i++) {
				if (userData.punishments.get(i).equals(punishment)) {
					userData.punishments.set(i, punishment);
					break add;
				}
			}
			userData.punishments.add(punishment);
		}
		saveData();
		triggerUpdate(true);
	}

	public void triggerUpdate() {
		triggerUpdate(false);
	}

	public void triggerUpdate(boolean ban) {
	}

	private CBUser copy() {
		CBUser copy = new CBUser(userId, uuid);
		copy.copyFrom(this);
		return copy;
	}

	private CBUser withoutSensitiveData() {
		CBUser copy = copy();
		copy.password = null;
		copy.twoFactorSecret = null;
		return copy;
	}

	public JsonObject toJsonObject() {
		return toJsonObject(true);
	}

	public JsonObject toJsonObject(boolean withSensitiveData) {
		CBUser toJson = withSensitiveData ? this : withoutSensitiveData();
		return (JsonObject) gsonForNetwork.toJsonTree(toJson);
	}

	public String toJson(boolean prettyPrinting) {
		return toJson(prettyPrinting, true);
	}

	public String toJson(boolean prettyPrinting, boolean withSensitiveData) {
		CBUser toJson = withSensitiveData ? this : withoutSensitiveData();
		return (prettyPrinting ? gsonForFile : gsonForNetwork).toJson(toJson);
	}

	public static CBUser fromJson(String json) {
		return gsonForFile.fromJson(json, CBUser.class).prepare();
	}

	static Punishment punishmentFromJson(String json) {
		return gsonForFile.fromJson(json, Punishment.class);
	}

	static String punishmentToJson(Punishment punishment, boolean prettyPrinting) {
		return (prettyPrinting ? gsonForFile : gsonForNetwork).toJson(punishment);
	}

	static NameHistory nameHistoryFromJson(String json) {
		return gsonForFile.fromJson(json, NameHistory.class);
	}

	static String nameHistoryToJson(NameHistory nameHistory, boolean prettyPrinting) {
		return (prettyPrinting ? gsonForFile : gsonForNetwork).toJson(nameHistory);
	}

	static NameHistory[] nameHistoryArrayFromJson(String json) {
		return gsonForFile.fromJson(json, NameHistory[].class);
	}

	static String nameHistoryToJson(NameHistory[] nameHistory, boolean prettyPrinting) {
		return (prettyPrinting ? gsonForFile : gsonForNetwork).toJson(nameHistory);
	}
}
