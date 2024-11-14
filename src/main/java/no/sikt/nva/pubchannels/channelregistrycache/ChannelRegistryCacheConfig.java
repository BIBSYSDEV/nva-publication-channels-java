package no.sikt.nva.pubchannels.channelregistrycache;

import nva.commons.core.Environment;

public final class ChannelRegistryCacheConfig {

    public static final String CACHE_BUCKET = new Environment().readEnv("CHANNEL_REGISTER_CACHE_BUCKET");
    public static final String CHANNEL_REGISTER_CACHE_S3_OBJECT = new Environment().readEnv(
        "CHANNEL_REGISTER_CACHE_S3_OBJECT");
    private ChannelRegistryCacheConfig() {
    }
}
