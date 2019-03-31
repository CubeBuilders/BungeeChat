package hk.siggi.bungeecord.bungeechat.geolocation;

public final class Geolocation {

	public final String countryCode;
	public final String countryName;
	public final String regionName;
	public final String cityName;
	public final String latitude;
	public final String longitude;
	public final String zipCode;
	public final String timeZone;

	Geolocation(String countryCode, String countryName, String regionName, String cityName, String latitude, String longitude, String zipCode, String timeZone) {
		this.countryCode = countryCode;
		this.countryName = countryName;
		this.regionName = regionName;
		this.cityName = cityName;
		this.latitude = latitude;
		this.longitude = longitude;
		this.zipCode = zipCode;
		this.timeZone = timeZone;
	}

	@Override
	public String toString() {
		return "location: "+cityName+", "+regionName+", "+countryName+"/"+countryCode + ", lat="+latitude+", lon="+longitude+", zip="+zipCode+", timezone="+timeZone;
	}
}
