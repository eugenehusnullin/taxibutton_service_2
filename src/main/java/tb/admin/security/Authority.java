package tb.admin.security;

import java.io.Serializable;

public class Authority implements Serializable {
	private static final long serialVersionUID = -2418784496018936004L;
	// The default is "select username, authority from authorities where username = ?"
	private String username;
	private String authority;

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
}
