package hk.siggi.bungeecord.bungeechat.timezone;

class TimeZoneDbThailand extends TimeZoneDb {

	@Override
	public String[] getCountryStrings() {
		return new String[]{"TH", "Thailand"};
	}

	@Override
	public String getTimeZone(String zip) {
		return "Asia/Bangkok";
	}
}
