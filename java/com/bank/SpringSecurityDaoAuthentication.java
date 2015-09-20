package com.bank.manager.configs;



import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import com.bank.manager.beans.Account;
import com.bank.manager.metier.IManagerMetier;


/*
 * 	YOUR BUISINESS CLASS SHOULD CONTAIN TWO METHODS :
 *      1) - public Account findAccountByUsername(String username);
 *		2) - public String[] loadUserAuthorities(String username);
 *		IMPORTANT : USE THE EXCEPTION WHEN GATHERING DATA 
 *		FROM THE DB.
	
 *	THE ACCOUNT CLASS SHOULD CONTAIN AT LEAST THERE PROPERTIES :
	
 *		private String username;
 *		private String password;
 *		private Boolean enabled;
		
 *	THE AUTHORITY CLASS SHOULD ALSO CONTAIN AT LEAST :
	
 *		private String username;
 *		private String authority;
		
 *	FINALLY YOU CAN DO WHATEVER YOU WANT WITH YOUR MAPPINGS
 *		BECAUSE THIS CONFIGURATION DO NOT DEPEND TO ANY ORM 
 *		CONFIGURATION.
	
 */

@Transactional
public class SpringSecurityDaoAuthentication  extends JdbcDaoSupport implements UserDetailsService{
	/*
	 * 
	 * This method will work also 
	 * 
	 * @Autowired
	 * private IBuisiness buisiness;
	 *
	 * public void setBuisiness(IBuisiness buisiness)
	 * {
	 *	this.buisiness = buisiness;
	 * }
	 * 
	 * 
	 * */

	// ~ Instance fields
	// ================================================================================================

	protected final MessageSourceAccessor messages = SpringSecurityMessageSource
			.getAccessor();
	
	private String rolePrefix = "";
	private boolean usernameBasedPrimaryKey = true;
	private boolean enableAuthorities = true;
	private boolean enableGroups=false;
	
	/*
	 * You should add you Buisiness class here
	 * For example my Buisiness class Implements IManagerMetier
	 */
	
	private IManagerMetier buisiness;
	
	/*
	 * Now we will add an accessor for the buisiness reference
	 */
	
	public void setBuisiness(IManagerMetier buisiness)
	{
		this.buisiness = buisiness;
	}
	
	

	// ~ Constructors
	// ===================================================================================================

	public SpringSecurityDaoAuthentication() {}

	// ~ Methods
	// ========================================================================================================

	/**
	 * Allows subclasses to add their own granted authorities to the list to be returned
	 * in the <tt>UserDetails</tt>.
	 *
	 * @param username the username, for use by finder methods
	 * @param authorities the current granted authorities, as populated from the
	 * <code>authoritiesByUsername</code> mapping
	 */
	protected void addCustomAuthorities(String username,
			List<GrantedAuthority> authorities) {
	}


	protected void initDao() throws ApplicationContextException {
		Assert.isTrue(enableAuthorities || enableGroups,
				"Use of either authorities or groups must be enabled");
	}

	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		List<UserDetails> users = loadUsersByUsername(username);

		if (users.size() == 0) {
			logger.debug("Query returned no results for user '" + username + "'");

			throw new UsernameNotFoundException(messages.getMessage(
					"JdbcDaoImpl.notFound", new Object[] { username },
					"Username {0} not found"));
		}
		else
			logger.debug("IT IS ALRIGHT USER WITH USERNAME : "+username+" FOUND !");

		UserDetails user = users.get(0); // contains no GrantedAuthority[]

		Set<GrantedAuthority> dbAuthsSet = new HashSet<GrantedAuthority>();

		if (enableAuthorities) {
			for(GrantedAuthority a : loadUserAuthorities(user.getUsername()))
				logger.debug("User with username : "+user.getUsername()+" Have "
						+ "authority => "+a.getAuthority());
			dbAuthsSet.addAll(loadUserAuthorities(user.getUsername()));
		}

		List<GrantedAuthority> dbAuths = new ArrayList<GrantedAuthority>(dbAuthsSet);

		addCustomAuthorities(user.getUsername(), dbAuths);

		if (dbAuths.size() == 0) {
			logger.debug("User '" + username
					+ "' has no authorities and will be treated as 'not found'");

			throw new UsernameNotFoundException(messages.getMessage(
					"JdbcDaoImpl.noAuthority", new Object[] { username },
					"User {0} has no GrantedAuthority"));
		}

		return createUserDetails(username, user, dbAuths);
	}

	/**
	 * Executes the SQL <tt>usersByUsernameQuery</tt> and returns a list of UserDetails
	 * objects. There should normally only be one matching user.
	 */
	protected List<UserDetails> loadUsersByUsername(String username) {
		if(buisiness == null)
			throw new IllegalArgumentException("You shoud wire you Buisiness class, it is setted to null !");
		Account account = buisiness.findAccountByUsername(username);
		List<UserDetails> userDetails = new ArrayList<UserDetails>();
		User user = new User(username, account.getPassword(), account.getEnabled(), true, true, true,
				AuthorityUtils.NO_AUTHORITIES);
		userDetails.add(user);
		return userDetails;
	}

	/**
	 * Loads authorities by executing the SQL from <tt>authoritiesByUsernameQuery</tt>.
	 *
	 * @return a list of GrantedAuthority objects for the user
	 */
	protected List<GrantedAuthority> loadUserAuthorities(String username) {
		if(buisiness == null)
			throw new IllegalArgumentException("You shoud wire you Buisiness class, it is setted to null !");
		String[] authsValues = buisiness.loadUserAuthorities(username);
		List<GrantedAuthority> final_auths = new ArrayList<GrantedAuthority>();
		for(int i = 0 ; i < authsValues.length ; i++)
			final_auths.add(new SimpleGrantedAuthority(authsValues[i]));
		return final_auths;
	}

	

	/**
	 * Can be overridden to customize the creation of the final UserDetailsObject which is
	 * returned by the <tt>loadUserByUsername</tt> method.
	 *
	 * @param username the name originally passed to loadUserByUsername
	 * @param userFromUserQuery the object returned from the execution of the
	 * @param combinedAuthorities the combined array of authorities from all the authority
	 * loading queries.
	 * @return the final UserDetails which should be used in the system.
	 */
	protected UserDetails createUserDetails(String username,
			UserDetails userFromUserQuery, List<GrantedAuthority> combinedAuthorities) {
		String returnUsername = userFromUserQuery.getUsername();

		if (!usernameBasedPrimaryKey) {
			returnUsername = username;
		}

		return new User(returnUsername, userFromUserQuery.getPassword(),
				userFromUserQuery.isEnabled(), true, true, true, combinedAuthorities);
	}

	

	/**
	 * Allows a default role prefix to be specified. If this is set to a non-empty value,
	 * then it is automatically prepended to any roles read in from the db. This may for
	 * example be used to add the <tt>ROLE_</tt> prefix expected to exist in role names
	 * (by default) by some other Spring Security classes, in the case that the prefix is
	 * not already present in the db.
	 *
	 * @param rolePrefix the new prefix
	 */
	public void setRolePrefix(String rolePrefix) {
		this.rolePrefix = rolePrefix;
	}

	protected String getRolePrefix() {
		return rolePrefix;
	}

	/**
	 * If <code>true</code> (the default), indicates the
	 * {@link #getUsersByUsernameQuery()} returns a username in response to a query. If
	 * <code>false</code>, indicates that a primary key is used instead. If set to
	 * <code>true</code>, the class will use the database-derived username in the returned
	 * <code>UserDetails</code>. If <code>false</code>, the class will use the
	 * {@link #loadUserByUsername(String)} derived username in the returned
	 * <code>UserDetails</code>.
	 *
	 * @param usernameBasedPrimaryKey <code>true</code> if the mapping queries return the
	 * username <code>String</code>, or <code>false</code> if the mapping returns a
	 * database primary key.
	 */
	public void setUsernameBasedPrimaryKey(boolean usernameBasedPrimaryKey) {
		this.usernameBasedPrimaryKey = usernameBasedPrimaryKey;
	}

	protected boolean isUsernameBasedPrimaryKey() {
		return usernameBasedPrimaryKey;
	}

	protected boolean getEnableAuthorities() {
		return enableAuthorities;
	}

	/**
	 * Enables loading of authorities (roles) from the authorities table. Defaults to true
	 */
	public void setEnableAuthorities(boolean enableAuthorities) {
		this.enableAuthorities = enableAuthorities;
	}

	protected boolean getEnableGroups() {
		return enableGroups;
	}

	/**
	 * Enables support for group authorities. Defaults to false
	 * @param enableGroups
	 */
	public void setEnableGroups(boolean enableGroups) {
		this.enableGroups = enableGroups;
	}
}
