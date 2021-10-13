package nemethi.xrate.core.integ;

import nemethi.xrate.api.CurrencyConverter;
import nemethi.xrate.core.PluginLoader;
import nemethi.xrate.core.integ.util.ProviderConfigExtension;
import nemethi.xrate.core.integ.util.TestCurrencyConverter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PluginLoaderIT {

    @Nested
    @ExtendWith(ProviderConfigExtension.class)
    class PluginIsPresent {

        @Test
        void loadsPluginFromClasspath() {
            PluginLoader loader = new PluginLoader();

            Optional<CurrencyConverter> plugin = loader.findFirstPlugin();

            assertThat(plugin).isPresent();
            CurrencyConverter converter = plugin.get();
            assertThat(converter).isInstanceOf(TestCurrencyConverter.class);
        }
    }

    @Nested
    class PluginIsNotPresent {

        @Test
        void returnsEmptyOptionalIfNotFound() {
            PluginLoader loader = new PluginLoader();
            Optional<CurrencyConverter> plugin = loader.findFirstPlugin();
            assertThat(plugin).isNotPresent();
        }
    }
}
