/**
 * 
 */
package org.apertereports.engine;

import java.util.Map;

import org.apertereports.common.exception.ARException;

/**
 * @author MW
 * 
 */
public interface SubreportProvider {

	Map<String, Subreport> getSubreports(String... reportNames) throws ARException;

	public class Subreport {
		private String name;
		private String cacheId;
		private byte[] content;

		public Subreport(String name, String cacheId, byte[] content) {
			super();
			this.name = name;
			this.cacheId = cacheId;
			this.content = content;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCacheId() {
			return cacheId;
		}

		public void setCacheId(String cacheId) {
			this.cacheId = cacheId;
		}

		public byte[] getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content.getBytes();
		}

		public void setContent(byte[] content) {
			this.content = content;
		}
	}
}
