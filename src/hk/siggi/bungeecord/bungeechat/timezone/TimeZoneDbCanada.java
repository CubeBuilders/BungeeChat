package hk.siggi.bungeecord.bungeechat.timezone;

import java.util.HashMap;
import java.util.Map;

class TimeZoneDbCanada extends TimeZoneDb {

	private final Map<String, String> timezones = new HashMap<>();

	public TimeZoneDbCanada() {
		// Timezone, offset, Has DST, No DST
		// Pacific, -8, America/Vancouver, --
		// Mountain, -7, America/Edmonton, America/Fort_Nelson
		// Central, -6, America/Winnipeg, America/Regina
		// Eastern, -5, America/Toronto, America/Atikokan
		// Atlantic, -4, America/Halifax, America/Blanc-Sablon
		// St John's, -3:30, America/St_Johns, --
		// <editor-fold defaultstate="collapsed" desc="All the timezones">

		// This is probably incomplete xD
		// St John's
		timezones.put("A", "America/St_Johns");

		// Atlantic
		timezones.put("B", "America/Halifax");
		timezones.put("C", "America/Halifax");
		timezones.put("E", "America/Halifax");

		// Eastern
		timezones.put("J", "America/Toronto");
		timezones.put("G", "America/Toronto");
		timezones.put("H", "America/Toronto");
		timezones.put("L", "America/Toronto");
		timezones.put("K", "America/Toronto");
		timezones.put("M", "America/Toronto");
		timezones.put("N", "America/Toronto");
		timezones.put("X0A", "America/Toronto");

		timezones.put("P0T1C0", "America/Atikokan"); // Atikokan, ON
		timezones.put("P0V2H0", "America/Atikokan"); // New Osnaburgh, ON
		timezones.put("P0V3A0", "America/Atikokan"); // Pickle Lake, ON
		timezones.put("X0C0C0", "America/Atikokan"); // Coral Harbour, NU
		timezones.put("X0C0A2", "America/Atikokan"); // Coral Harbour, NU
		timezones.put("X0C0C0", "America/Atikokan"); // Coral Harbour, NU

		// Central
		timezones.put("R", "America/Winnipeg");
		timezones.put("X0C", "America/Winnipeg");

		timezones.put("S", "America/Regina"); // Saskatchewan

		// Mountain
		timezones.put("T", "America/Edmonton");
		timezones.put("X0B", "America/Edmonton");

		// These following are Mountain Time with no DST
		// Peace River Regional District (including Fort Nelson)
		timezones.put("V1G0C2", "America/Fort_Nelson");
		timezones.put("V1G0B9", "America/Fort_Nelson");
		timezones.put("V1G4H8", "America/Fort_Nelson");
		timezones.put("V1J4N4", "America/Fort_Nelson");
		timezones.put("V0C", "America/Fort_Nelson");
		timezones.put("V1J4M6", "America/Fort_Nelson");
		timezones.put("V1G0C4", "America/Fort_Nelson");
		timezones.put("V1J6M7", "America/Fort_Nelson");
		timezones.put("V0J", "America/Fort_Nelson");
		timezones.put("V0C2C0", "America/Fort_Nelson");
		timezones.put("V1G4G4", "America/Fort_Nelson");
		timezones.put("V0C2W0", "America/Fort_Nelson");
		timezones.put("V1G4R8", "America/Fort_Nelson");
		timezones.put("V1J4M7", "America/Fort_Nelson");
		timezones.put("V1J3Z5", "America/Fort_Nelson");
		timezones.put("V0C2K0", "America/Fort_Nelson");
		timezones.put("V1G1P7", "America/Fort_Nelson");
		timezones.put("V1J4X3", "America/Fort_Nelson");
		timezones.put("V1J4H9", "America/Fort_Nelson");
		timezones.put("V1J1Y5", "America/Fort_Nelson");
		timezones.put("V0C1H0", "America/Fort_Nelson");
		timezones.put("V1J0C6", "America/Fort_Nelson");
		timezones.put("V1J5Z1", "America/Fort_Nelson");
		timezones.put("V1J4H7", "America/Fort_Nelson");
		timezones.put("V1J1Y7", "America/Fort_Nelson");
		timezones.put("V1J6W7", "America/Fort_Nelson");
		timezones.put("V1J4P6", "America/Fort_Nelson");
		timezones.put("V1J4S4", "America/Fort_Nelson");
		timezones.put("V1G4G2", "America/Fort_Nelson");

		// East Kootenay
		timezones.put("V0B1M3", "America/Edmonton");
		timezones.put("V0B1M7", "America/Edmonton");
		timezones.put("V0B1M6", "America/Edmonton");
		timezones.put("V0B2L1", "America/Edmonton");
		timezones.put("V0B1L2", "America/Edmonton");
		timezones.put("V0B2G3", "America/Edmonton");
		timezones.put("V0B2L0", "America/Edmonton");
		timezones.put("V0B1J0", "America/Edmonton");
		timezones.put("V0B2P0", "America/Edmonton");
		timezones.put("V0B2L2", "America/Edmonton");
		timezones.put("V0B1E0", "America/Edmonton");
		timezones.put("V0B1R0", "America/Edmonton");
		timezones.put("V0B1N0", "America/Edmonton");
		timezones.put("V0B1L0", "America/Edmonton");
		timezones.put("V0B1P0", "America/Edmonton");
		timezones.put("V0B2J0", "America/Edmonton");
		timezones.put("V0B2H0", "America/Edmonton");
		timezones.put("V0B1L1", "America/Edmonton");
		timezones.put("V0B2E0", "America/Edmonton");
		timezones.put("V0B1T4", "America/Edmonton");
		timezones.put("V0B1S0", "America/Edmonton");
		timezones.put("V0B1M4", "America/Edmonton");
		timezones.put("V0B1M0", "America/Edmonton");
		timezones.put("V0B1M5", "America/Edmonton");
		timezones.put("V0B1M1", "America/Edmonton");
		timezones.put("V0B1M2", "America/Edmonton");
		timezones.put("V0B1H0", "America/Edmonton");
		timezones.put("V0B2G2", "America/Edmonton");
		timezones.put("V0A1K5", "America/Edmonton");
		timezones.put("V0B2G0", "America/Edmonton");
		timezones.put("V0B2G1", "America/Edmonton");
		timezones.put("V0B1G9", "America/Edmonton");
		timezones.put("V0B2A0", "America/Edmonton");
		timezones.put("V0B1T0", "America/Edmonton");
		timezones.put("V0B1G2", "America/Edmonton");
		timezones.put("V0B2B0", "America/Edmonton");
		timezones.put("V0B1B0", "America/Edmonton");
		timezones.put("V0B2K0", "America/Edmonton");
		timezones.put("V0A1K0", "America/Edmonton");
		timezones.put("V0B1G0", "America/Fort_Nelson");
		timezones.put("V0B1G1", "America/Fort_Nelson");
		timezones.put("V0B1G3", "America/Fort_Nelson");
		timezones.put("V0B1G4", "America/Fort_Nelson");
		timezones.put("V0B1G5", "America/Fort_Nelson");
		timezones.put("V0B1G6", "America/Fort_Nelson");
		timezones.put("V0B1G8", "America/Fort_Nelson");

		// Pacific
		timezones.put("V", "America/Vancouver");
		timezones.put("Y", "America/Vancouver");
		// </editor-fold>
	}

	@Override
	public String[] getCountryStrings() {
		return new String[]{"CA", "Canada"};
	}

	@Override
	public String getTimeZone(String postalCode) {
		postalCode = postalCode.toUpperCase().replace(" ", "");
		if (postalCode.length() < 6) {
			return null;
		}
		String timezone = timezones.get(postalCode.substring(0, 6));
		if (timezone != null) {
			return timezone;
		}
		timezone = timezones.get(postalCode.substring(0, 5));
		if (timezone != null) {
			return timezone;
		}
		timezone = timezones.get(postalCode.substring(0, 4));
		if (timezone != null) {
			return timezone;
		}
		timezone = timezones.get(postalCode.substring(0, 3));
		if (timezone != null) {
			return timezone;
		}
		timezone = timezones.get(postalCode.substring(0, 2));
		if (timezone != null) {
			return timezone;
		}
		timezone = timezones.get(postalCode.substring(0, 1));
		if (timezone != null) {
			return timezone;
		}
		return null;
	}

}
