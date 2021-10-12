package nemethi.xrate.core.integ;

import nemethi.xrate.api.CurrencyConverter;
import nemethi.xrate.core.PluginLoader;
import nemethi.xrate.core.integ.util.TestCurrencyConverter;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PluginLoaderIT {

    @Test
    void loadsPluginFromClasspath() {
        PluginLoader loader = new PluginLoader();

        Optional<CurrencyConverter> plugin = loader.findFirstPlugin();

        assertThat(plugin).isPresent();
        CurrencyConverter converter = plugin.get();
        assertThat(converter).isInstanceOf(TestCurrencyConverter.class);
    }
}
