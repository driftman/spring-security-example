


@Entity
@SuppressWarnings("serial")
public class Authority implements Serializable{
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private String username;
	private String authority;
	private Boolean enabled;
	@ManyToOne(targetEntity=Account.class, fetch=FetchType.EAGER)
	private Account account;
	public Authority() {}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAuthority() {
		return authority;
	}
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	public Boolean getEnabled() {
		return enabled;
	}
	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	public Authority(String username, String authority, Boolean enabled) {
		super();
		this.username = username;
		this.authority = authority;
		this.enabled = enabled;
	}
	
	
	
}
