package net.cubebuilders.user;

import java.util.UUID;

public class UserFriend {
	public UUID uuid = null;
	public FriendStatus status = null;
	public long time = 0L;
	public enum FriendStatus {
		FRIENDS, INCOMING_REQUEST, OUTGOING_REQUEST, BLOCKED, NOT_FRIENDS;
	}
}
