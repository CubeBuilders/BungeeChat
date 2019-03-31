package hk.siggi.bungeecord.bungeechat.timezone;

class TimeZoneDbChina extends TimeZoneDb {

	@Override
	public String[] getCountryStrings() {
		return new String[]{"CN", "China"};
	}

	@Override
	public String getTimeZone(String zip) {
		return "Asia/Shanghai";
	}
}
