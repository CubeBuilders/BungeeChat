package hk.siggi.bungeecord.bungeechat.timezone;

class TimeZoneDbPhilippines extends TimeZoneDb {

	@Override
	public String[] getCountryStrings() {
		return new String[]{"PH", "Philippines"};
	}

	@Override
	public String getTimeZone(String zip) {
		return "Asia/Manila";
	}
}
