package com.eva.evamod;

/**
 * Central version constants so future releases can migrate data without breaking saves.
 * <p>
 * Rules for future changes:
 * <ul>
 *   <li>Bump {@link #DISPLAY} on every release.</li>
 *   <li>Bump {@link #PLAYER_SCHEMA} / {@link #WORLD_SCHEMA} only when persisted fields change.</li>
 *   <li>Always load older schemas via optional codec fields + {@code DataMigrations}.</li>
 *   <li>Never remove persisted keys in the same release that bumps schema — deprecate first.</li>
 *   <li>Bump network registrar version when payload shapes change.</li>
 * </ul>
 */
public final class ModVersions {
    public static final String DISPLAY = "3.0.0";
    public static final String CODENAME = "Companions";

    /** Persisted player attachment schema. */
    public static final int PLAYER_SCHEMA = 3;
    /** Persisted overworld attachment schema. */
    public static final int WORLD_SCHEMA = 1;
    /** Play networking protocol (PayloadRegistrar). */
    public static final int NETWORK = 3;

    /** First production line that is supported. 1.x jars are outdated. */
    public static final String MIN_SUPPORTED_DISPLAY = "2.0.0";

    private ModVersions() {
    }
}
