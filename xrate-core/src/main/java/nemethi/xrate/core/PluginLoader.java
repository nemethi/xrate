package nemethi.xrate.core;

import nemethi.xrate.api.CurrencyConverter;

import java.util.Optional;
import java.util.ServiceLoader;

public class PluginLoader {

    private final ServiceLoader<CurrencyConverter> serviceLoader;

    public PluginLoader() {
        serviceLoader = ServiceLoader.load(CurrencyConverter.class);
    }

    public Optional<CurrencyConverter> findFirstPlugin() {
        return serviceLoader.findFirst();
    }
}
