package jp.ats.util.ldap;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDirectoryImpl implements UserDirectory {

	private final Logger logger = LoggerFactory.getLogger(UserDirectoryImpl.class);

	private static String[] serverAddresses;

	private static final Object lock = new Object();

	private final String securityPrincipal;

	private final String securityCredentials;

	private final String basedn;

	public UserDirectoryImpl(Config config) {
		System.setProperty("javax.net.ssl.trustStore", config.getTruststorePath());

		synchronized (lock) {
			if (serverAddresses == null) {
				String primary = config.getPrimaryLdapServerAddress();
				String secondary = config.getSecondaryLdapServerAddress();
				serverAddresses = new String[] { primary, secondary != null ? secondary : primary };
			}
		}

		securityPrincipal = config.getSecurityPrincipal();
		securityCredentials = config.getSecurityCredentials();
		basedn = config.getBasedn();
	}

	@Override
	public List<User> all() {
		return search("*");
	}

	private static final Charset passwordCharset = StandardCharsets.UTF_16LE;

	@Override
	public void changePassword(String cn, String newPassword) {
		List<User> users = search(cn);

		if (users.size() != 1)
			throw new IllegalStateException(cn + " での検索結果が不正です。 size=" + users.size());

		User user = users.get(0);

		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_PROTOCOL, "ssl");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.REFERRAL, "ignore");
		env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
		env.put(Context.SECURITY_CREDENTIALS, securityCredentials);

		DirContext ctx = createContext(env, address -> "ldaps://" + address + ":636/");

		SearchControls constraints = new SearchControls();
		constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

		String quotedPassword = "\"" + newPassword + "\"";
		byte pwdArray[] = quotedPassword.getBytes(passwordCharset);

		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("unicodepwd", pwdArray));

		try {
			ctx.modifyAttributes(user.getDistinguishedName(), mods);
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}
	}

	private List<User> search(String cn) {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
		env.put(Context.SECURITY_CREDENTIALS, securityCredentials);

		DirContext ctx = createContext(env, address -> "ldap://" + address + "/");
		NamingEnumeration<SearchResult> nenum;
		try {
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);

			nenum = ctx.search(basedn, "(&(cn=" + cn + ")(description=*))", constraints);

			List<User> users = new LinkedList<>();

			while (nenum.hasMoreElements()) {
				Attributes attr = nenum.next().getAttributes();

				String useraccountcontrol = value(attr.get("useraccountcontrol"));

				BitSet flags = BitSet
						.valueOf(new long[] { useraccountcontrol == null ? 0 : Long.parseLong(useraccountcontrol) });

				User user = new User(value(attr.get("samaccountname")), value(attr.get("description")),
						value(attr.get("cn")), value(attr.get("givenname")), value(attr.get("sn")),
						value(attr.get("physicaldeliveryofficename")), value(attr.get("whencreated")),
						value(attr.get("whenchanged")), value(attr.get("distinguishedname")), flags.get(1));

				users.add(user);
			}

			return users;
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		} finally {
			try {
				ctx.close();
			} catch (NamingException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	private DirContext createContext(Hashtable<String, String> env, Function<String, String> providerUrlFunction) {
		synchronized (lock) {
			String url = providerUrlFunction.apply(serverAddresses[0]);
			env.put(Context.PROVIDER_URL, url);
		}

		{
			DirContext ctx = createContext(env);
			if (ctx != null)
				return ctx;
		}

		synchronized (lock) {
			String first = serverAddresses[0];
			String last = serverAddresses[1];

			serverAddresses = new String[] { last, first };

			String url = providerUrlFunction.apply(serverAddresses[0]);
			env.put(Context.PROVIDER_URL, url);
		}

		{
			DirContext ctx = createContext(env);
			if (ctx != null)
				return ctx;

			synchronized (lock) {
				throw new IllegalStateException(
						"[" + serverAddresses[1] + "], [" + serverAddresses[0] + "] ともに接続できませんでした。");
			}
		}
	}

	private DirContext createContext(Hashtable<String, String> env) {
		try {
			return new InitialDirContext(env);
		} catch (CommunicationException e) {
			logger.warn(e.getMessage(), e);
			return null;
		} catch (NamingException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final String value(Attribute attribute) throws NamingException {
		if (attribute == null)
			return null;
		return (String) attribute.get();
	}
}
