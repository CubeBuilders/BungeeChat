package hk.siggi.bungeecord.bungeechat.timezone;

class TimeZoneDbIceland extends TimeZoneDb {

	@Override
	public String[] getCountryStrings() {
		return new String[]{"IS", "Iceland"};
	}

	@Override
	public String getTimeZone(String zip) {
		return "Atlantic/Reykjavik";
	}
}
