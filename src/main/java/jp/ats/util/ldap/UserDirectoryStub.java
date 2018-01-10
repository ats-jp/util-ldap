package jp.ats.util.ldap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UserDirectoryStub implements UserDirectory {

	private static final List<User> list = Collections.unmodifiableList(new LinkedList<>());

	@Override
	public List<User> all() {
		return list;
	}

	@Override
	public void changePassword(String targetUserId, String newPassword) {
	}
}
