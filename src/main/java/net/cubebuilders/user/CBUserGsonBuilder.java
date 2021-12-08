package net.cubebuilders.user;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class CBUserGsonBuilder {

	private CBUserGsonBuilder() {
	}

	public static final Gson gson;
	public static final TypeAdapter<CBUser> cbUser;
	public static final TypeAdapter<Punishment> punishment;
	public static final TypeAdapter<OffenceEvidence> offenceEvidence;
	public static final TypeAdapter<PunishmentHistory> punishmentHistory;

	static {
		// <editor-fold defaultstate="collapsed" desc="CBUser TypeAdapter">
		cbUser = new TypeAdapter<CBUser>() {
			@Override
			public CBUser read(JsonReader reader) throws IOException {
				int userId = -1;
				UUID uuid = null;
				String password = null;
				String passwordSalt = null;
				String twoFactorSecret = null;
				boolean twoFactorEnabled = false;
				String email = null;
				boolean emailVerified = false;
				boolean emailDidBounce = false;
				String emailVerificationCode = null;
				boolean subscribedToMailingList = false;
				boolean allowEmails = false;
				boolean subscribedToImportantEmails = true;
				long emailConfirmedDate = 0L;
				long registerDate = 0L;
				long lastLogin = 0L;
				long lastMissYouEmail = 0L;
				boolean missYouGift = false;
				UserData userData = null;
				reader.beginObject();
				while (reader.hasNext()) {
					JsonToken peek = reader.peek();
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equalsIgnoreCase("uid") && peek == JsonToken.NUMBER) {
						userId = reader.nextInt();
					} else if (key.equalsIgnoreCase("uuid") && peek == JsonToken.STRING) {
						String uuidString = reader.nextString();
						uuidString = uuidString.replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5");
						uuid = UUID.fromString(uuidString);
					} else if (key.equalsIgnoreCase("registerDate") && peek == JsonToken.NUMBER) {
						registerDate = reader.nextLong();
					} else if (key.equalsIgnoreCase("password") && peek == JsonToken.STRING) {
						password = reader.nextString();
					} else if (key.equalsIgnoreCase("passwordSalt") && peek == JsonToken.STRING) {
						passwordSalt = reader.nextString();
					} else if (key.equalsIgnoreCase("twoFactorSecret") && peek == JsonToken.STRING) {
						twoFactorSecret = reader.nextString();
					} else if (key.equalsIgnoreCase("twoFactorEnabled") && peek == JsonToken.BOOLEAN) {
						twoFactorEnabled = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("email") && peek == JsonToken.STRING) {
						email = reader.nextString();
					} else if (key.equalsIgnoreCase("emailVerified") && peek == JsonToken.BOOLEAN) {
						emailVerified = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("emailDidBounce") && peek == JsonToken.BOOLEAN) {
						emailDidBounce = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("emailVerificationCode") && peek == JsonToken.STRING) {
						emailVerificationCode = reader.nextString();
					} else if (key.equalsIgnoreCase("subscribedToMailingList") && peek == JsonToken.BOOLEAN) {
						subscribedToMailingList = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("allowEmails") && peek == JsonToken.BOOLEAN) {
						allowEmails = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("emailConfirmedDate") && peek == JsonToken.NUMBER) {
						emailConfirmedDate = reader.nextLong();
					} else if (key.equalsIgnoreCase("subscribedToImportantEmails") && peek == JsonToken.BOOLEAN) {
						subscribedToImportantEmails = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("lastLogin") && peek == JsonToken.NUMBER) {
						lastLogin = reader.nextLong();
					} else if (key.equalsIgnoreCase("lastMissYouEmail") && peek == JsonToken.NUMBER) {
						lastMissYouEmail = reader.nextLong();
					} else if (key.equalsIgnoreCase("missYouGift") && peek == JsonToken.BOOLEAN) {
						missYouGift = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("userdata") && peek == JsonToken.BEGIN_OBJECT) {
						userData = gson.fromJson(reader, UserData.class);
					}
				}
				reader.endObject();
				CBUser cbUser = new CBUser(userId, uuid);
				cbUser.password = password;
				cbUser.passwordSalt = passwordSalt;
				cbUser.twoFactorSecret = twoFactorSecret;
				cbUser.twoFactorEnabled = twoFactorEnabled;
				cbUser.email = email;
				cbUser.emailVerified = emailVerified;
				cbUser.emailDidBounce = emailDidBounce;
				cbUser.emailVerificationCode = emailVerificationCode;
				cbUser.subscribedToMailingList = subscribedToMailingList;
				cbUser.allowEmails = allowEmails;
				cbUser.emailConfirmedDate = emailConfirmedDate;
				cbUser.subscribedToImportantEmails = subscribedToImportantEmails;
				cbUser.lastLogin = lastLogin;
				cbUser.lastMissYouEmail = lastMissYouEmail;
				cbUser.missYouGift = missYouGift;
				cbUser.userData = userData;
				if (registerDate != 0L) {
					cbUser.registerDate = registerDate;
				}
				return cbUser;
			}

			@Override
			public void write(JsonWriter writer, CBUser cbUser) throws IOException {
				writer.beginObject();
				writer.name("uid").value(cbUser.userId);
				writer.name("uuid").value(cbUser.uuid.toString().replaceAll("-", "").toLowerCase());
				writer.name("registerDate").value(cbUser.registerDate);
				if (cbUser.password != null) {
					writer.name("password").value(cbUser.password);
				}
				if (cbUser.passwordSalt != null) {
					writer.name("passwordSalt").value(cbUser.passwordSalt);
				}
				if (cbUser.twoFactorSecret != null) {
					writer.name("twoFactorSecret").value(cbUser.twoFactorSecret);
				}
				writer.name("twoFactorEnabled").value(cbUser.twoFactorEnabled);
				if (cbUser.email != null) {
					writer.name("email").value(cbUser.email);
				}
				writer.name("emailVerified").value(cbUser.emailVerified);
				writer.name("emailDidBounce").value(cbUser.emailDidBounce);
				if (cbUser.emailVerificationCode != null) {
					writer.name("emailVerificationCode").value(cbUser.emailVerificationCode);
				}
				writer.name("subscribedToMailingList").value(cbUser.subscribedToMailingList);
				writer.name("subscribedToImportantEmails").value(cbUser.subscribedToImportantEmails);
				writer.name("allowEmails").value(cbUser.allowEmails);
				if (cbUser.emailConfirmedDate > 0L) {
					writer.name("emailConfirmedDate").value(cbUser.emailConfirmedDate);
				}
				if (cbUser.lastLogin > 0L) {
					writer.name("lastLogin").value(cbUser.lastLogin);
				}
				if (cbUser.lastMissYouEmail > 0L) {
					writer.name("lastMissYouEmail").value(cbUser.lastMissYouEmail);
				}
				if (cbUser.missYouGift) {
					writer.name("missYouGift").value(cbUser.missYouGift);
				}
				if (cbUser.userData != null) {
					writer.name("userdata");
					gson.toJson(cbUser.userData, UserData.class, writer);
				}
				writer.endObject();
			}
		};
		// </editor-fold>
		// <editor-fold defaultstate="collapsed" desc="Punishment TypeAdapter">
		punishment = new TypeAdapter<Punishment>() {
			@Override
			public Punishment read(JsonReader reader) throws IOException {
				Punishment.PunishmentAction action = null;
				String offence = "manual";
				Punishment.PunishmentStatus status = Punishment.PunishmentStatus.ACTIVE;
				long time = -1L;
				long startTime = -1L;
				long length = 0L;
				String reason = null;
				UUID issuedBy = null;
				UUID issuedTo = null;
				int appeal = -1;
				boolean cancelled = false;
				ArrayList<OffenceEvidence> evidence = null;
				ArrayList<PunishmentHistory> history = null;
				reader.beginObject();
				while (reader.hasNext()) {
					JsonToken peek = reader.peek();
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equalsIgnoreCase("action") && peek == JsonToken.STRING) {
						String action_str = reader.nextString();
						try {
							action = Punishment.PunishmentAction.fromString(action_str);
						} catch (Exception e) {
						}
					} else if (key.equalsIgnoreCase("offence") && peek == JsonToken.STRING) {
						offence = reader.nextString();
					} else if (key.equalsIgnoreCase("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equalsIgnoreCase("startTime") && peek == JsonToken.NUMBER) {
						startTime = reader.nextLong();
					} else if (key.equalsIgnoreCase("length") && peek == JsonToken.NUMBER) {
						length = reader.nextLong();
					} else if (key.equalsIgnoreCase("reason") && peek == JsonToken.STRING) {
						reason = reader.nextString();
					} else if (key.equalsIgnoreCase("status") && peek == JsonToken.STRING) {
						String status_str = reader.nextString();
						try {
							status = Punishment.PunishmentStatus.fromString(status_str);
						} catch (Exception e) {
						}
					} else if (key.equalsIgnoreCase("cancelled") && peek == JsonToken.BOOLEAN) {
						cancelled = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("history") && peek == JsonToken.BEGIN_ARRAY) {
						reader.beginArray();
						history = new ArrayList<>();
						while (reader.hasNext()) {
							if (reader.peek() != JsonToken.BEGIN_OBJECT) {
								reader.skipValue();
								continue;
							}
							PunishmentHistory historyItem = (PunishmentHistory) gson.fromJson(reader, PunishmentHistory.class);
							if (historyItem != null) {
								history.add(historyItem);
							}
						}
						reader.endArray();
					} else if (key.equalsIgnoreCase("evidence") && peek == JsonToken.BEGIN_ARRAY) {
						reader.beginArray();
						evidence = new ArrayList<>();
						while (reader.hasNext()) {
							if (reader.peek() != JsonToken.BEGIN_OBJECT) {
								reader.skipValue();
								continue;
							}
							OffenceEvidence evidenceItem = (OffenceEvidence) gson.fromJson(reader, OffenceEvidence.class);
							if (evidenceItem != null) {
								evidence.add(evidenceItem);
							}
						}
						reader.endArray();
					} else if (key.equalsIgnoreCase("issuedBy") && peek == JsonToken.STRING) {
						String uuidString = reader.nextString();
						uuidString = uuidString.replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5");
						try {
							issuedBy = UUID.fromString(uuidString);
						} catch (Exception e) {
						}
					} else if (key.equalsIgnoreCase("issuedTo") && peek == JsonToken.STRING) {
						String uuidString = reader.nextString();
						uuidString = uuidString.replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5");
						try {
							issuedTo = UUID.fromString(uuidString);
						} catch (Exception e) {
						}
					} else if (key.equalsIgnoreCase("appeal") && peek == JsonToken.NUMBER) {
						appeal = reader.nextInt();
					}
				}
				reader.endObject();
				if (action == null || time == -1L || startTime == -1L || reason == null || issuedBy == null) {
					return null;
				}
				Punishment p = new Punishment(action, offence, time, startTime, length, reason, issuedBy, issuedTo);
				p.cancelled = cancelled;
				if (history != null) {
					p.history = history;
				}
				if (evidence != null) {
					p.evidence = evidence;
				}
				p.status = status;
				p.appeal = appeal;
				return p;
			}

			@Override
			public void write(JsonWriter writer, Punishment punishment) throws IOException {
				writer.beginObject();
				writer.name("action").value(punishment.action.toString());
				writer.name("offence").value(punishment.offence);
				writer.name("time").value(punishment.time);
				writer.name("startTime").value(punishment.startTime);
				if (punishment.length != 0L) {
					writer.name("length").value(punishment.length);
				}
				writer.name("reason").value(punishment.reason);
				writer.name("status").value(punishment.status.toString());
				if (punishment.cancelled) {
					writer.name("cancelled").value(punishment.cancelled);
				}
				ArrayList<PunishmentHistory> history = punishment.history;
				if (history != null && history.size() > 0) {
					writer.name("history");
					writer.beginArray();
					for (PunishmentHistory i : history) {
						gson.toJson(i, PunishmentHistory.class, writer);
					}
					writer.endArray();
				}
				ArrayList<OffenceEvidence> evidence = punishment.evidence;
				if (evidence != null && evidence.size() > 0) {
					writer.name("evidence");
					writer.beginArray();
					for (OffenceEvidence i : evidence) {
						gson.toJson(i, OffenceEvidence.class, writer);
					}
					writer.endArray();
				}
				writer.name("issuedBy").value(punishment.issuedBy.toString().replaceAll("-", ""));
				// UNCOMMENT the next line on BungeeCord plugins!
				writer.name("issuedTo").value(punishment.issuedTo.toString().replaceAll("-", ""));
				if (punishment.appeal != -1) {
					writer.name("appeal").value(punishment.appeal);
				}
				writer.endObject();
			}
		};
		// </editor-fold>
		// <editor-fold defaultstate="collapsed" desc="OffenceEvidence TypeAdapter">
		offenceEvidence = new TypeAdapter<OffenceEvidence>() {
			@Override
			public OffenceEvidence read(JsonReader reader) throws IOException {
				OffenceEvidence.EvidenceType type = null;
				long time = 0L;
				String url = null;
				UUID uuid = null;
				UUID addedBy = null;
				reader.beginObject();
				while (reader.hasNext()) {
					JsonToken peek = reader.peek();
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equalsIgnoreCase("type") && peek == JsonToken.STRING) {
						type = OffenceEvidence.EvidenceType.fromString(reader.nextString());
					} else if (key.equalsIgnoreCase("time") && peek == JsonToken.NUMBER) {
						time = reader.nextLong();
					} else if (key.equalsIgnoreCase("url") && peek == JsonToken.STRING) {
						url = reader.nextString();
					} else if (key.equalsIgnoreCase("uuid") && peek == JsonToken.STRING) {
						String uuidString = reader.nextString();
						uuidString = uuidString.replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5");
						try {
							uuid = UUID.fromString(uuidString);
						} catch (Exception e) {
						}
					} else if (key.equalsIgnoreCase("addedBy") && peek == JsonToken.STRING) {
						String uuidString = reader.nextString();
						uuidString = uuidString.replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5");
						try {
							addedBy = UUID.fromString(uuidString);
						} catch (Exception e) {
						}
					}
				}
				reader.endObject();
				if (type == null) {
					return null;
				}
				if (type == OffenceEvidence.EvidenceType.CHATLOG && time == 0L) {
					return null;
				}
				if (type == OffenceEvidence.EvidenceType.REPLAY && uuid == null) {
					return null;
				}
				if (type == OffenceEvidence.EvidenceType.SCREENSHOT && url == null) {
					return null;
				}
				return new OffenceEvidence(type, time, url, uuid, addedBy);
			}

			@Override
			public void write(JsonWriter writer, OffenceEvidence t) throws IOException {
				writer.beginObject();
				writer.name("type").value(t.type.toString());
				if (t.time != 0L) {
					writer.name("time").value(t.time);
				}
				if (t.url != null) {
					writer.name("url").value(t.url);
				}
				if (t.uuid != null) {
					writer.name("uuid").value(t.uuid.toString());
				}
				if (t.addedBy != null) {
					writer.name("addedBy").value(t.addedBy.toString());
				}
				writer.endObject();
			}
		};
		// </editor-fold>
		// <editor-fold defaultstate="collapsed" desc="PunishmentHistory TypeAdapter">
		punishmentHistory = new TypeAdapter<PunishmentHistory>() {
			@Override
			public PunishmentHistory read(JsonReader reader) throws IOException {
				UUID user = null;
				long editTime = 0L;
				long startTime = 0L;
				Punishment.PunishmentAction action = null;
				Punishment.PunishmentStatus status = null;
				long length = 0L;
				String reason = null;
				boolean cancelled = false;
				String editReason = null;
				reader.beginObject();
				while (reader.hasNext()) {
					JsonToken peek = reader.peek();
					if (peek != JsonToken.NAME) {
						reader.skipValue();
						continue;
					}
					String key = reader.nextName();
					peek = reader.peek();
					if (key.equalsIgnoreCase("user") && peek == JsonToken.STRING) {
						String uuidString = reader.nextString();
						uuidString = uuidString.replaceAll("-", "").replaceAll("([0-9A-Fa-f]{8})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{4})([0-9A-Fa-f]{12})", "$1-$2-$3-$4-$5");
						try {
							user = UUID.fromString(uuidString);
						} catch (Exception e) {
						}
					} else if (key.equalsIgnoreCase("time") && peek == JsonToken.NUMBER) {
						editTime = reader.nextLong();
					} else if (key.equalsIgnoreCase("startTime") && peek == JsonToken.NUMBER) {
						startTime = reader.nextLong();
					} else if (key.equalsIgnoreCase("action") && peek == JsonToken.STRING) {
						action = Punishment.PunishmentAction.fromString(reader.nextString());
					} else if (key.equalsIgnoreCase("length") && peek == JsonToken.NUMBER) {
						length = reader.nextLong();
					} else if (key.equalsIgnoreCase("reason") && peek == JsonToken.STRING) {
						reason = reader.nextString();
					} else if (key.equalsIgnoreCase("status") && peek == JsonToken.STRING) {
						status = Punishment.PunishmentStatus.fromString(reader.nextString());
					} else if (key.equalsIgnoreCase("cancelled") && peek == JsonToken.BOOLEAN) {
						cancelled = reader.nextBoolean();
					} else if (key.equalsIgnoreCase("editReason") && peek == JsonToken.STRING) {
						editReason = reader.nextString();
					}
				}
				reader.endObject();
				if (action == null) {
					return null;
				}
				return new PunishmentHistory(user, editTime, startTime, action, length, reason, status, cancelled, editReason);
			}

			@Override
			public void write(JsonWriter writer, PunishmentHistory t) throws IOException {
				writer.beginObject();
				if (t.user != null) {
					writer.name("user").value(t.user.toString());
				}
				if (t.time != 0L) {
					writer.name("time").value(t.time);
				}
				if (t.startTime != 0L) {
					writer.name("startTime").value(t.startTime);
				}
				if (t.action != null) {
					writer.name("action").value(t.action.toString());
				}
				if (t.length != 0L) {
					writer.name("length").value(t.length);
				}
				if (t.reason != null) {
					writer.name("reason").value(t.reason);
				}
				if (t.status != null) {
					writer.name("status").value(t.status.toString());
				}
				if (t.cancelled) {
					writer.name("cancelled").value(t.cancelled);
				}
				if (t.editReason != null) {
					writer.name("editReason").value(t.editReason);
				}
				writer.endObject();
			}
		};
		// </editor-fold>
		gson = build(false);
	}

	public static Gson build(boolean prettyPrinting) {
		GsonBuilder builder = new GsonBuilder();

		register(builder);

		if (prettyPrinting) {
			builder.setPrettyPrinting();
		}
		return builder.create();
	}

	public static void register(GsonBuilder builder) {
		builder.registerTypeAdapter(CBUser.class, cbUser);
		builder.registerTypeAdapter(Punishment.class, punishment);
		builder.registerTypeAdapter(OffenceEvidence.class, offenceEvidence);
		builder.registerTypeAdapter(PunishmentHistory.class, punishmentHistory);
	}
}
