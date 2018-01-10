package jp.ats.util.ldap;

import java.util.List;

/**
 * ADサーバに対する要求を表したインターフェイス
 */
public interface UserDirectory {

	/**
	 * AD上の全TAKETSURUクライアントユーザーを取得する
	 * 
	 * @return 全ユーザー
	 */
	List<User> all();

	/**
	 * ADのパスワードを変更する
	 * 
	 * @param cn
	 *            パスワードを変更する対象ユーザーのCommonName
	 * @param newPassword
	 *            新パスワード
	 */
	void changePassword(String cn, String newPassword);
}
