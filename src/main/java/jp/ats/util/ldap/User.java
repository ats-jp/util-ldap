package jp.ats.util.ldap;

public class User {

	private final String userId;

	private final String edyNo;

	private final String commonName;

	private final String firstName;

	private final String lastName;

	private final String deptName;

	private final String whenCreated;

	private final String whenChanged;

	private final String distinguishedName;

	private final boolean invalidated;

	public User(String userId, String edyNo, String commonName, String firstName, String lastName, String deptName,
			String whenCreated, String whenChanged, String distinguishedName, boolean invalidated) {
		this.userId = userId;
		this.edyNo = edyNo;
		this.commonName = commonName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.deptName = deptName;
		this.whenCreated = whenCreated;
		this.whenChanged = whenChanged;
		this.distinguishedName = distinguishedName;
		this.invalidated = invalidated;
	}

	public String getUserId() {
		return userId;
	}

	public String getEdyNo() {
		return edyNo;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getDeptName() {
		return deptName;
	}

	public String getWhenCreated() {
		return whenCreated;
	}

	public String getWhenChanged() {
		return whenChanged;
	}

	public String getDistinguishedName() {
		return distinguishedName;
	}

	public boolean isInvalidated() {
		return invalidated;
	}
}
