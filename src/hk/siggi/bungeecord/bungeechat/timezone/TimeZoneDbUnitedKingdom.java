package hk.siggi.bungeecord.bungeechat.timezone;

class TimeZoneDbUnitedKingdom extends TimeZoneDb {

	@Override
	public String[] getCountryStrings() {
		return new String[]{"UK", "GB", "UnitedKingdom", "United Kingdom",
			"GreatBritain", "Great Britain", "England", "Ireland"};
	}

	@Override
	public String getTimeZone(String zip) {
		return "Europe/London";
	}

}
