/*
 * Erstellt am: 07.06.2019 10:50:52
 * Erstellt von: Jonas Michel
 */
package com.bazaarvoice.maven.plugin.process;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * @author Jonas Michel
 *
 */
public class HealthCheckConfig {

	public static class BasicAuth {

		@Parameter(property = "exec.healthcheck.basic-auth.username")
		protected String username;

		@Parameter(property = "exec.healthcheck.basic-auth.password")
		protected String password;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		@Override
		public String toString() {
			return "BasicAuth [username=" + username + ", password=" + password + "]";
		}

	}

	@Parameter(property = "exec.healthcheck.url")
	protected String url;

	@Parameter(property = "exec.healthcheck.basic-auth")
	protected BasicAuth basicAuth;

	@Parameter(property = "exec.healthcheck.body-regex")
	protected String bodyMatchExpression;

	@Parameter(property = "exec.healthckec.status", defaultValue = "200")
	protected Integer status;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public BasicAuth getBasicAuth() {
		return basicAuth;
	}

	public void setBasicAuth(BasicAuth basicAuth) {
		this.basicAuth = basicAuth;
	}

	public String getBodyMatchExpression() {
		return bodyMatchExpression;
	}

	public void setBodyMatchExpression(String bodyMatchExpression) {
		this.bodyMatchExpression = bodyMatchExpression;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "HealthCheckConfig [url=" + url + ", basicAuth=" + basicAuth + ", bodyMatchExpression=" + bodyMatchExpression + ", status=" + status + "]";
	}

}
