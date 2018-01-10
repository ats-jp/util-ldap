package jp.ats.util.ldap;

import java.util.Properties;

public class Config {

	private final Properties properties;

	public Config(Properties properties) {
		this.properties = properties;
	}

	/**
	 * @return 主LDAPサーバIPアドレス
	 */
	public String getPrimaryLdapServerAddress() {
		return properties.getProperty("primary-ldap-server-address");
	}

	/**
	 * @return 副LDAPサーバIPアドレス
	 */
	public String getSecondaryLdapServerAddress() {
		return properties.getProperty("secondary-ldap-server-address");
	}

	/**
	 * @return LDAP接続ユーザー
	 */
	public String getSecurityPrincipal() {
		return properties.getProperty("security-principal");
	}

	/**
	 * @return LDAP接続パスワード
	 */
	public String getSecurityCredentials() {
		return properties.getProperty("security-credentials");
	}

	/**
	 * @return LDAP サーチベース
	 */
	public String getBasedn() {
		return properties.getProperty("basedn");
	}

	/**
	 * @return LDAP SSL接続用証明書ストア
	 */
	public String getTruststorePath() {
		return properties.getProperty("truststore-path");
	}
}
