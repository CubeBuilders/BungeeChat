package hk.siggi.bungeecord.bungeechat.timezone;

class TimeZoneDbHongKong extends TimeZoneDb {

	@Override
	public String[] getCountryStrings() {
		return new String[]{"HK", "HKSAR", "Hong Kong", "Hong Kong (SAR)"};
	}

	@Override
	public String getTimeZone(String zip) {
		return "Asia/Hong_Kong";
	}

}
