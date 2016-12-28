package io.bootique.jetty;

import com.google.inject.Module;
import io.bootique.BQModuleProvider;
import io.bootique.jetty.server.ServerFactory;

import java.util.Collections;
import java.util.Map;

public class JettyModuleProvider implements BQModuleProvider {

	@Override
	public Module module() {
		return new JettyModule();
	}

	/**
	 * @since 0.19
	 * @return a single entry map with {@link ServerFactory}.
	 */
	@Override
	public Map<String, Class<?>> configs() {
		// TODO: config prefix is hardcoded. Refactor away from ConfigModule, and make provider
		// generate config prefix, reusing it in metadata...
		return Collections.singletonMap("jetty", ServerFactory.class);
	}
}
