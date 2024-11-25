package no.sikt.nva.pubchannels.utils;

public class FakeAppConfig implements AppConfig {

    private final boolean cacheEnabled;

    public FakeAppConfig(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    public boolean shouldUseCache() {
        return cacheEnabled;
    }
}
